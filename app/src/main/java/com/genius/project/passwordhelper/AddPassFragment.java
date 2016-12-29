package com.genius.project.passwordhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class AddPassFragment extends DialogFragment implements DialogInterface.OnClickListener {

    PasswordDatabaseHelper helper;
    SQLiteDatabase database;
    String site;
    String login;
    String pass;
    String info;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {                                       //отображает слой с фрагментом диалога
        return new AlertDialog.Builder(getActivity())                                               //реализация в виде, совместимом с API 25-
                .setPositiveButton(R.string.enterPass_button, this)                                                    //также отображает диалог, в зависимости от того
                .setNegativeButton(R.string.dismissPass_button, this)                                                  //кто вызывает его (фрагмент/активность)
                .setTitle(R.string.enter_new_pass_header)
                .setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_add_pass_fragment, null))
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,                          //реализует отрисовку начального макета
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
                EditText siteForm = (EditText) dialogForm.findViewById(R.id.site);
                EditText loginForm = (EditText) dialogForm.findViewById(R.id.login);
                EditText passForm = (EditText) dialogForm.findViewById(R.id.pass);
                EditText infoForm = (EditText) dialogForm.findViewById(R.id.info);

                if(siteForm.getText().toString().equals("")
                        && loginForm.getText().toString().equals("")
                        && passForm.getText().toString().equals("")
                        && infoForm.getText().toString().equals("")) {
                    dialogForm.dismiss();
                    View fab = getActivity().findViewById(R.id.fab);
                    Snackbar.make(fab, R.string.siteEmptyState, Snackbar.LENGTH_LONG)      //вывод снакбара
                            .setAction("Action", null).show();
                    break;
                } else {
                    if (siteForm.getText().toString().equals("")) {
                        siteForm.setText(R.string.siteEmpty);
                    }
                    site = siteForm.getText().toString();
                    login = loginForm.getText().toString();
                    pass = passForm.getText().toString();
                    info = infoForm.getText().toString();

                    backDb backDb = new backDb();                                                   //вызов асинхронного класса
                    backDb.execute();                                                               //записывающего новые значения в БД
                    dialogForm.dismiss();
                    main.updateMainList(true);
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
                                                                                                    //для записи нового пароля
        @Override
        protected Boolean doInBackground(Void... Void) {
            helper.insertPass(database, site, login,  pass, info);
            return true;
        }
    }
}
