package com.genius.project.passwordhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class EditPassFragment extends DialogFragment implements DialogInterface.OnClickListener {

    PasswordDatabaseHelper helper;
    SQLiteDatabase database;
    ContentValues updatedValues;
    EditText siteView;
    EditText loginView;
    EditText passView;
    EditText infoView;
    String itemId;
    Bundle bundle;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bundle = this.getArguments();
        itemId = Long.toString(bundle.getLong("itemId"));
        updatedValues = new ContentValues();

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_pass_fragment, null);

        SQLiteOpenHelper passHelper = new PasswordDatabaseHelper(getActivity());
        SQLiteDatabase database = passHelper.getReadableDatabase();
        Cursor cursor = database.query("DATAPASS", new String[]{"SITE", "LOGIN", "PASS", "INFO"}, "_id = ?", new String[]{itemId}, null, null, null);

        if(cursor.moveToFirst()) {
            siteView = (EditText) view.findViewById(R.id.site_editable);
            loginView = (EditText) view.findViewById(R.id.login_editable);
            passView = (EditText) view.findViewById(R.id.pass_editable);
            infoView = (EditText) view.findViewById(R.id.info_editable);

            siteView.setText(cursor.getString(0));
            loginView.setText(cursor.getString(1));
            passView.setText(cursor.getString(2));
            infoView.setText(cursor.getString(3));

            database.close();
            cursor.close();
        }

        return new AlertDialog.Builder(getActivity())                                               //реализация в виде, совместимом с API 25-
                .setPositiveButton(R.string.enterPass_button, this)                                 //также отображает диалог, в зависимости от того
                .setNegativeButton(R.string.dismissPass_button, this)                               //кто вызывает его (фрагмент/активность)
                .setTitle(R.string.editPass)
                .setView(view)
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {                                           //реализация в виде, совместимом с API 25-
        if (getShowsDialog()) {                                                                     //кто вызывает его (фрагмент/активность)
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        return inflater.inflate(R.layout.dialog_add_pass_fragment, container, false);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int number) {

        MainActivity main = (MainActivity) getActivity();
        helper = new PasswordDatabaseHelper(getActivity());
        database = helper.getWritableDatabase();

        Dialog dialogForm = (Dialog) dialogInterface;                                               //брать значения только из Dialog !!!
        switch(number) {
            case Dialog.BUTTON_POSITIVE: {
                siteView = (EditText) dialogForm.findViewById(R.id.site_editable);
                loginView = (EditText) dialogForm.findViewById(R.id.login_editable);
                passView = (EditText) dialogForm.findViewById(R.id.pass_editable);
                infoView = (EditText) dialogForm.findViewById(R.id.info_editable);

                if(siteView.getText().toString().equals("")
                        && loginView.getText().toString().equals("")
                        && passView.getText().toString().equals("")
                        && infoView.getText().toString().equals("")) {
                    DialogFragment confirm = new ConfirmAction();
                    confirm.show(getFragmentManager(), "ConfirmDialogThrowManualDeleting");
                    break;
                } else {
                    if (siteView.getText().toString().equals("")) {
                        siteView.setText(R.string.siteEmpty);
                    }
                    updatedValues.put("SITE", siteView.getText().toString());
                    updatedValues.put("LOGIN", loginView.getText().toString());
                    updatedValues.put("PASS", passView.getText().toString());
                    updatedValues.put("INFO", infoView.getText().toString());

                    EditPassFragment.backDb backDb = new EditPassFragment.backDb();                 //вызов асинхронного класса
                    backDb.execute();                                                               //записывающего новые значения в БД
                    dialogForm.dismiss();
                    main.onRefresh();
                    break;
                }
            }
            case Dialog.BUTTON_NEGATIVE: {
                dialogForm.dismiss();
                break;
            }
        }
    }

    class backDb extends AsyncTask<Void, Void, Boolean> {                                           //метод вызов асинхронного класса
                                                                                                    //для записи обновленного пароля
        @Override
        protected Boolean doInBackground(Void... Void) {
            database.update("DATAPASS", updatedValues, "_id = ?", new String[]{itemId});
            return true;
        }
    }
}
