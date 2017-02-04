package com.genius.project.passwordhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.genius.project.passwordhelper.PasswordDatabaseHelper.CNST_DB;
import static com.genius.project.passwordhelper.PasswordDatabaseHelper.ID;
import static com.genius.project.passwordhelper.PasswordDatabaseHelper.INFO;
import static com.genius.project.passwordhelper.PasswordDatabaseHelper.LOGIN;
import static com.genius.project.passwordhelper.PasswordDatabaseHelper.PASS;
import static com.genius.project.passwordhelper.PasswordDatabaseHelper.SITE;

public class DetailPassFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_show_pass_fragment, null);
        Bundle bundleIn = this.getArguments();
        long id = bundleIn.getLong("itemId");

        SQLiteOpenHelper passHelper = new PasswordDatabaseHelper(getActivity());
        SQLiteDatabase database = passHelper.getReadableDatabase();
        Cursor cursor = database.query(CNST_DB, new String[]{SITE, LOGIN, PASS, INFO}, ID + " = ?", new String[]{Long.toString(id)}, null, null, null);

        if(cursor.moveToFirst()) {
            TextView siteView = (TextView) view.findViewById(R.id.site_show);
            TextView loginView = (TextView) view.findViewById(R.id.login_show);
            TextView passView = (TextView) view.findViewById(R.id.pass_show);
            TextView infoView = (TextView) view.findViewById(R.id.info_show);

            siteView.setText(cursor.getString(0));
            loginView.setText(cursor.getString(1));
            passView.setText(cursor.getString(2));
            infoView.setText(cursor.getString(3));

            database.close();
            cursor.close();
        } else {
            database.close();
            cursor.close();
        }

        return new AlertDialog.Builder(getActivity())                                               //реализация в виде, совместимом с API 25-
                .setTitle(R.string.editPassLayer)
                .setView(view)                                                                      //передача отрисованного View в build диалога
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {                                           //реализация в виде, совместимом с API 25-
                                                                                                    //также отображает диалог, в зависимости от того
        if (getShowsDialog()) {                                                                     //кто вызывает его (фрагмент/активность)
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        return inflater.inflate(R.layout.dialog_show_pass_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
