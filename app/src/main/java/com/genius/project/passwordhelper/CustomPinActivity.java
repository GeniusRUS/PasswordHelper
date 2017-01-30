package com.genius.project.passwordhelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

import static com.genius.project.passwordhelper.PasswordDatabaseHelper.CNST_DB;

public class CustomPinActivity extends AppLockActivity {

    @Override
    public void showForgotDialog() {
        new AlertDialog.Builder(CustomPinActivity.this)
                .setNegativeButton(R.string.dismissPass_button, null)
                .setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteOpenHelper databaseHelper = new PasswordDatabaseHelper(CustomPinActivity.this);
                        SQLiteDatabase database = databaseHelper.getWritableDatabase();
                        database.delete(CNST_DB, null, null);
                        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
                        lockManager.getAppLock().disableAndRemoveConfiguration();
                        database.close();
                        CustomPinActivity.this.finish();
                    }
                })
                .setTitle(R.string.drop_db)
                .setMessage(R.string.forgot_message)
                .create()
                .show();
    }

    @Override
    public void onPinFailure(int attempts) {
        if(attempts == 5) {
            SQLiteOpenHelper databaseHelper = new PasswordDatabaseHelper(CustomPinActivity.this);
            SQLiteDatabase database = databaseHelper.getWritableDatabase();
            database.delete(CNST_DB, null, null);
            LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
            lockManager.getAppLock().disableAndRemoveConfiguration();
            database.close();
            CustomPinActivity.this.finish();
        }
    }

    @Override
    public void onPinSuccess(int attempts) {
        
    }

    @Override
    public int getPinLength() {
        return super.getPinLength();
    }
}
