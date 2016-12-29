package com.genius.project.passwordhelper;

import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_PREF;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_SECURITY_ENABLE;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_SECONDS_AUTH;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_THEME;

public class MainActivity extends AppCompatActivity implements ConfirmAction.ConfirmDialogListener, SwipeRefreshLayout.OnRefreshListener{

    private static final String KEY_NAME = "my_key";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[]{1, 2, 3, 4, 5, 6};
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    public static final String CNST_DB = "DATAPASS";

    private static int authenticationDurationSeconds;

    SwipeRefreshLayout mSwipeRefreshLayout;
    SharedPreferences preferences;
    protected static KeyguardManager mKeyguardManager;
    private SQLiteDatabase dataBaseMain;
    private Cursor displayMainCursor;
    SQLiteOpenHelper dbHelper;
    DialogFragment dialogAddPass;
    ListView listViewPasswords;
    FloatingActionButton fab;
    int selectedItemId;
    public static String sortTypeIn;
    public static String sortOrderIn;
    public static SharedPreferences prefSort;

    public String sort() {
        StringBuilder sort = new StringBuilder();
        sortTypeIn = prefSort.getString("SortingType", "SITE");
        sortOrderIn = prefSort.getString("SortingOrder", "ASC");

        if (sortTypeIn.equals("SITE")) {
            sort.append("SITE");
        } else {
            sort.append("_id");
        }
        if (sortOrderIn.equals("ASC")) {
            sort.append(" ");
            sort.append("ASC");
        } else {
            sort.append(" ");
            sort.append("DESC");
        }
        return sort.toString();
    }

    public void updateMainList(Boolean isShowSnackbar) {                                            //через SimpleCursorAdapter - оптимальная работа (?)
        listViewPasswords = (ListView) findViewById(R.id.listPasswords);                            //заполнение ListView
        try{                                                                                        //вызывается после каждой операции ввода/удаления/изменения
            dbHelper = new PasswordDatabaseHelper(this);
            dataBaseMain = dbHelper.getReadableDatabase();
            displayMainCursor = dataBaseMain.query(CNST_DB, new String[]{"_id", "SITE"}, null, null, null, null, sort());
            CursorAdapter cusrorDisplay = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_1,
                    displayMainCursor,
                    new String[]{"SITE"},
                    new int[]{android.R.id.text1}, 0);
            listViewPasswords.setAdapter(cusrorDisplay);
            if(isShowSnackbar) {
                Snackbar.make(fab, R.string.dataUpdated, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            fab.show();
        } catch(SQLiteException e) {
            Snackbar.make(fab, R.string.database_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private boolean tryEncrypt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
                Cipher cipher = Cipher.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                cipher.doFinal(SECRET_BYTE_ARRAY);
                showAlreadyAuthenticated();
                return true;
            } catch (UserNotAuthenticatedException e) {
                showAuthenticationScreen();
                return false;
            } catch (KeyPermanentlyInvalidatedException e) {
                Log.v("key_invalidated", "genuis_debug:" + R.string.keystore_keys_invalidated + e.getMessage());
                return false;
            } catch (BadPaddingException | IllegalBlockSizeException | KeyStoreException | CertificateException | UnrecoverableKeyException |
                    IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void createKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setUserAuthenticationValidityDurationSeconds(authenticationDurationSeconds)
                        .build());
                keyGenerator.generateKey();
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | KeyStoreException | CertificateException | IOException e) {
                throw new RuntimeException("Failed to create a symmetric key", e);
            }
        }
    }

    private void showAuthenticationScreen() {
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        if(intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            if(resultCode == RESULT_OK) {
                if(tryEncrypt()) {
                    showPurchaseConfirmation();
                }
            } else {
                super.finish();
            }
        }
    }

    private void showPurchaseConfirmation() {
        Log.v("access_granted", "genuis_debug: access granted");
    }

    private void showAlreadyAuthenticated() {
        Log.v("already_auth", "genuis_debug: already authenticated");
    }

    @Override
    public void onRefresh() {
        listViewPasswords = (ListView) findViewById(R.id.listPasswords);                            //заполнение ListView
        mSwipeRefreshLayout.setRefreshing(true);
        try{                                                                                        //вызывается после каждой операции ввода/удаления/изменения
            dbHelper = new PasswordDatabaseHelper(this);
            dataBaseMain = dbHelper.getReadableDatabase();
            displayMainCursor = dataBaseMain.query(CNST_DB, new String[]{"_id", "SITE"}, null, null, null, null, sort());
            CursorAdapter cusrorDisplay = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_1,
                    displayMainCursor,
                    new String[]{"SITE"},
                    new int[]{android.R.id.text1}, 0);
            listViewPasswords.setAdapter(cusrorDisplay);
                Snackbar.make(fab, R.string.dataUpdated, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            mSwipeRefreshLayout.setRefreshing(false);
            fab.show();
        } catch(SQLiteException e) {
            Snackbar.make(fab, R.string.database_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(resid, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences(PASSHELPER_PREF, Context.MODE_PRIVATE);
        prefSort = getSharedPreferences("PrefSort", Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean security_enable = preferences.getBoolean(PASSHELPER_SECURITY_ENABLE, false);
            authenticationDurationSeconds = preferences.getInt(PASSHELPER_SECONDS_AUTH, 30);
            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (!mKeyguardManager.isKeyguardSecure() && security_enable) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.security_not_set), Toast.LENGTH_LONG).show();
            } else {
                if (security_enable) {
                    createKey();
                    tryEncrypt();
                }
            }
        }
        if (preferences.getBoolean(PASSHELPER_THEME, false)) {
            setTheme(R.style.AppThemeDark_NoActionBar);
        } else {
            setTheme(R.style.AppThemeLight_NoActionBar);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar_white_buttons = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar_white_buttons);

        dialogAddPass = new AddPassFragment();                                                      //инициализиуем фрагмент AddPass

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);                                        //FloatingButton
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        p.setAnchorId(View.NO_ID);
        fab.setLayoutParams(p);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAddPass.show(getFragmentManager(), "enterPass");
            }
        });

        updateMainList(false);

        listViewPasswords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View v, int position, long id) {       //брать Id/arg3 для идентификации номера пароля!
                DialogFragment showDetail = new DetailPassFragment();
                Bundle bundle = new Bundle();
                bundle.putLong("itemId",id);
                showDetail.setArguments(bundle);
                showDetail.show(getFragmentManager(), "showDetail");
            }
        });

        listViewPasswords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                selectedItemId = (int) arg3;
                Bundle bundle = new Bundle();
                bundle.putLong("itemId", arg3);
                DialogFragment optionFragment = new OptionPassDetail();
                optionFragment.setArguments(bundle);
                optionFragment.show(getFragmentManager(), "editPass");
                return true;                                                                        //возвращение true перехватывает короткий тап
            }
        });
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {                                      //удаление элемента через конекстное меню (OptionalPassDetail) и подтверждение (ConfirmAction)
        DatabaseDeleteItem dbDelete = new DatabaseDeleteItem();
        dbDelete.execute(selectedItemId);
        dialog.dismiss();
    }

    class DatabaseDeleteItem extends AsyncTask<Integer, Void, Boolean>{                             //встроенный класс для удаления элементов через AsyncTask

        @Override
        protected Boolean doInBackground(Integer... selectedItemId) {
            try {
                SQLiteOpenHelper helperDeleter = new PasswordDatabaseHelper(MainActivity.this);
                SQLiteDatabase database = helperDeleter.getWritableDatabase();
                database.delete("DATAPASS", "_id = ?", new String[]{Integer.toString(selectedItemId[0])});
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean outBoolean) {
            if(!outBoolean) {
                Snackbar.make(fab, R.string.deleteError, Snackbar.LENGTH_SHORT).show();
            } else {
                updateMainList(false);
                Snackbar.make(fab, R.string.updateAfterDelete, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {                                      //редактирование элемента через конекстное меню (OptionaPassDetail)
        dialog.dismiss();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                                 //создание списка меню
        // Inflate the menu; this adds items to the action bar if it is present.
        if (preferences.getBoolean(PASSHELPER_THEME, false)) {
            getMenuInflater().inflate(R.menu.menu_main_blacked, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           //обработчик списка меню
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings: {
                Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);      //вызов активити меню
                startActivity(intentSettings);
                return true;
            }
            case R.id.action_search: {
                DialogFragment searchFragment = new SearchPassFragment();
                searchFragment.show(getFragmentManager(), "searchFragment");
                return true;
            }
            case R.id.action_sort: {
                DialogFragment sortFramgent = new SortPassFragment();
                sortFramgent.show(getFragmentManager(), "sortFragment");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataBaseMain.close();
        displayMainCursor.close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        this.updateMainList(false);
    }
}