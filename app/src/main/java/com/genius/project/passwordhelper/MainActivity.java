package com.genius.project.passwordhelper;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.github.orangegangsters.lollipin.lib.PinCompatActivity;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

import static com.genius.project.passwordhelper.PasswordDatabaseHelper.CNST_DB;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_PREF;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_SCREEN_DEFENCE;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_SECURITY_ENABLE;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_SECONDS_AUTH;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_THEME;
import static com.genius.project.passwordhelper.SortPassFragment.SORTING_ORDER;
import static com.genius.project.passwordhelper.SortPassFragment.SORTING_TYPE;

public class MainActivity extends PinCompatActivity implements ConfirmAction.ConfirmDialogListener, SwipeRefreshLayout.OnRefreshListener{

    public static LockManager<CustomPinActivity> lockManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SharedPreferences preferences;
    private SQLiteDatabase dataBaseMain;
    private Cursor displayMainCursor;
    private SQLiteOpenHelper dbHelper;
    private DialogFragment dialogAddPass;
    private ListView listViewPasswords;
    private FloatingActionButton fab;
    private int selectedItemId;

    public String sort() {
        StringBuilder sort = new StringBuilder();
        String sortTypeIn = preferences.getString(SORTING_TYPE, "SITE");
        String sortOrderIn = preferences.getString(SORTING_ORDER, "ASC");

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

    @Override
    public void onRefresh() {
        listViewPasswords = (ListView) findViewById(R.id.listPasswords);
        mSwipeRefreshLayout.setRefreshing(true);
        try{
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
        boolean security_enable = preferences.getBoolean(PASSHELPER_SECURITY_ENABLE, false);
        long timedOut = preferences.getLong(PASSHELPER_SECONDS_AUTH, AppLock.DEFAULT_TIMEOUT);
        lockManager = LockManager.getInstance();

        if (preferences.getBoolean(PASSHELPER_THEME, false)) {
            setTheme(R.style.AppThemeDark_NoActionBar);
        } else {
            setTheme(R.style.AppThemeLight_NoActionBar);
        }

        super.onCreate(savedInstanceState);

        if (security_enable) {
            lockManager.enableAppLock(this, CustomPinActivity.class);
            lockManager.getAppLock().setTimeout(timedOut);

            if(!lockManager.getAppLock().isPasscodeSet()) {
                Intent intent = new Intent(MainActivity.this, CustomPinActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                startActivity(intent);
            }
        } else {
            lockManager.disableAppLock();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(preferences.getBoolean(PASSHELPER_SCREEN_DEFENCE, false)) {                              //после отрисовки setContentView позволяет деласть превьюшки "недавним приложениям"
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        dialogAddPass = new AddPassFragment();

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
        if (preferences.getBoolean(PASSHELPER_THEME, false)) {
            getMenuInflater().inflate(R.menu.menu_main_blacked, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           //обработчик списка меню
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
            case R.id.action_generate: {
                DialogFragment generateFragment = new PasswordGeneratorFragment();
                generateFragment.show(getFragmentManager(), "generateFragment");
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
