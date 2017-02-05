package com.genius.project.passwordhelper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
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

import com.github.orangegangsters.lollipin.lib.managers.LockManager;

import static com.genius.project.passwordhelper.MainActivity.lockManager;
import static com.genius.project.passwordhelper.PasswordDatabaseHelper.CNST_DB;


public class SettingsActivity extends PreferenceActivity implements ConfirmWipe.ConfirmWipeListener{

    public static final String PASSHELPER_PREF = "MainPreferences";
    public static final String PASSHELPER_SECURITY_ENABLE = "SecEnab";
    public static final String PASSHELPER_SECONDS_AUTH = "SecSecAuth";
    public static final String PASSHELPER_THEME = "isBlackTheme";
    public static final String PASSHELPER_SCREEN_DEFENCE = "SS_disable";

    static final String SETTING_SORT = "preference_sort_button";
    static final String SETTING_BACKUP = "backup_button";
    static final String SETTING_WIPE = "preference_wipe_button";
    static final String SETTING_THEME = "theme_switch";
    static final String SETTING_SCREENSHOT = "screenshot_protect";
    static final String SETTING_SECURE = "security_enabler";
    static final String SETTING_AUTH_TIME = "preference_auth_time";
    static final String SETTING_ABOUT = "preference_about_button";
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private SharedPreferences preferences;

    public void dropDatabase() {
        SQLiteOpenHelper databaseHelper = new PasswordDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.delete(CNST_DB, null, null);
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

        Preference sort_backup = getPreferenceManager().findPreference(SETTING_SORT);
        sort_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment sortFragment = new SortPassFragment();
                sortFragment.show(getFragmentManager(), "sortFragment");
                return true;
            }
        });


        Preference button_backup = getPreferenceManager().findPreference(SETTING_BACKUP);
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

        Preference buttonWipe = getPreferenceManager().findPreference(SETTING_WIPE);
        buttonWipe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment wipeConfirm = new ConfirmWipe();
                wipeConfirm.show(getFragmentManager(), "wipeConfirm");
                return true;
            }
        });

        Preference themeSwitch = getPreferenceManager().findPreference(SETTING_THEME);
        themeSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getPreferenceManager().getSharedPreferences().getBoolean(SETTING_THEME, false)) {
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

        Preference screenshot_protect = getPreferenceManager().findPreference(SETTING_SCREENSHOT);
        screenshot_protect.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getPreferenceManager().getSharedPreferences().getBoolean(SETTING_SCREENSHOT, false)) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_SCREEN_DEFENCE, true);
                    editor.apply();
                    return true;
                } else {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_SCREEN_DEFENCE, false);
                    editor.apply();
                    return false;
                }
            }
        });

        Preference securityEnable = getPreferenceManager().findPreference(SETTING_SECURE);
        SwitchPreference secEnSw = (SwitchPreference) getPreferenceScreen().findPreference(SETTING_SECURE);
        secEnSw.setChecked(preferences.getBoolean(PASSHELPER_SECURITY_ENABLE, false));

        securityEnable.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getPreferenceManager().getSharedPreferences().getBoolean(SETTING_SECURE, false)) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_SECURITY_ENABLE, true);
                    editor.apply();
                    return true;
                } else {
                    if(lockManager.isAppLockEnabled()) {
                        lockManager.getAppLock().disableAndRemoveConfiguration();
                    } else {
                        lockManager.disableAppLock();
                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PASSHELPER_SECURITY_ENABLE, false);
                    editor.apply();
                    return false;
                }
            }
        });

        final EditTextPreference preference_auth_time = (EditTextPreference) findPreference(SETTING_AUTH_TIME);
        preference_auth_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences.Editor editor = preferences.edit();
                String timerString;
                long timer;
                try {
                    timerString = preference_auth_time.getText();
                    if (timerString.isEmpty()) {
                        timerString = "30";
                    }
                    timer = Integer.parseInt(timerString);
                    if (timer <= 0) {
                        timer = 10;
                    }
                    if (timer >= 60) {
                        timer = 59;
                    }
                } catch (IllegalArgumentException e) {
                    timer = 30;
                }
                    editor.putLong(PASSHELPER_SECONDS_AUTH, timer * 1000);
                    editor.apply();
                    return true;
            }
        });

        Preference buttonAbout = getPreferenceManager().findPreference(SETTING_ABOUT);
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
