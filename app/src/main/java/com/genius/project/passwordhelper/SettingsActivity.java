package com.genius.project.passwordhelper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity implements ConfirmWipe.ConfirmWipeListener{

    public static final String PASSHELPER_PREF = "SecPref";
    public static final String PASSHELPER_SECURITY_ENABLE = "SecEnab";
    public static final String PASSHELPER_SECONDS_AUTH = "SecSecAuth";
    public static final String PASSHELPER_THEME = "isBlackTheme";
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    SharedPreferences preferences;

    public void dropDatabase() {
        SQLiteOpenHelper databaseHelper = new PasswordDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.delete("DATAPASS", null, null);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences(PASSHELPER_PREF, Context.MODE_PRIVATE);
            if (preferences.getBoolean(PASSHELPER_THEME, false)) {
                setTheme(R.style.AppThemeDark_NoActionBar);
            } else {
                setTheme(R.style.AppThemeLight_NoActionBar);
            }

        super.onCreate(savedInstanceState);
        if (preferences.getBoolean(PASSHELPER_THEME, false)) {
            addPreferencesFromResource(R.xml.preferences_blacked);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }

        Preference button_backup = getPreferenceManager().findPreference("backup_button");
        button_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(SettingsActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    } else {
                        DialogFragment backupAction = new BackupActionFragment();
                        backupAction.show(getFragmentManager(), "backupAction");
                    }
                } else {
                    DialogFragment backupAction = new BackupActionFragment();
                    backupAction.show(getFragmentManager(), "backupAction");
                }
                return true;
            }
        });

        Preference buttonWipe = getPreferenceManager().findPreference("preference_wipe_button");
        buttonWipe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment wipeConfirm = new ConfirmWipe();
                wipeConfirm.show(getFragmentManager(), "wipeConfirm");
                return true;
            }
        });

        Preference themeSwitch = getPreferenceManager().findPreference("theme_switch");
        themeSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getPreferenceManager().getSharedPreferences().getBoolean("theme_switch", true)) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_THEME, true);
                    editor.apply();
                    Intent intent2 = getIntent();
                    finish();
                    startActivity(intent2);
                    return true;
                } else {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_THEME, false);
                    editor.apply();
                    Intent intent2 = getIntent();
                    finish();
                    startActivity(intent2);
                    return false;
                }
            }
        });

        Preference buttonAbout = getPreferenceManager().findPreference("preference_about_button");
        buttonAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder info = new AlertDialog.Builder(SettingsActivity.this);
                info.setTitle(R.string.app_name)
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage(R.string.about_desc);
                AlertDialog infoDialog = info.create();
                infoDialog.show();
                return true;
            }
        });

        Preference securityEnable = getPreferenceManager().findPreference("security_enabler");
        SwitchPreference secEnSw = (SwitchPreference) getPreferenceScreen().findPreference("security_enabler");
        secEnSw.setChecked(preferences.getBoolean(PASSHELPER_SECURITY_ENABLE, false));
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {                                        //проверка на API < 23
            secEnSw.setEnabled(false);
        } else {
            if (!mKeyguardManager.isKeyguardSecure()) {
                secEnSw.setEnabled(false);
            }
        }
        securityEnable.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getPreferenceManager().getSharedPreferences().getBoolean("security_enabler", true)) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_SECURITY_ENABLE, true);
                    editor.apply();
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.security_enabled), Toast.LENGTH_SHORT).show();
                    return true;
                } else {

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_SECURITY_ENABLE, false);
                    editor.apply();
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.security_disabled), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        final EditTextPreference preference_auth_time = (EditTextPreference) findPreference("preference_auth_time");
        preference_auth_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences.Editor editor = preferences.edit();
                String timerString;
                int timer;
                try {
                    timerString = preference_auth_time.getText();
                    if (timerString.isEmpty()) {
                        timerString = "30";
                    }
                    timer = Integer.parseInt(timerString);
                    if (timer <= 0) {
                        timer = 1;
                    }
                    if (timer >= 60) {
                        timer = 59;
                    }
                } catch (IllegalArgumentException e) {
                    timer = 30;
                }
                    editor.putInt(PASSHELPER_SECONDS_AUTH, timer);
                    editor.apply();
                    return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            DialogFragment backupAction = new BackupActionFragment();
            backupAction.show(getFragmentManager(), "backupAction");
        } else {
            Toast.makeText(this, getResources().getString(R.string.backup_permission_not_granted), Toast.LENGTH_SHORT).show();
        }
        return;
    }

    @Override
    public void onWipePositiveClick(DialogFragment dialog) {
        dropDatabase();
    }

    @Override
    public void onWipeNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}
