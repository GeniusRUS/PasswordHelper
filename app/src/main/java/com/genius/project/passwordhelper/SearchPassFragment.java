package com.genius.project.passwordhelper;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;


public class SearchPassFragment extends DialogFragment implements DialogInterface.OnClickListener{

    public final static String CNST_DB = "DATAPASS";
    Cursor cursorSearch;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())                                               //реализация в виде, совместимом с API 25-
                .setPositiveButton(R.string.search_button, this)                                                  //также отображает диалог, в зависимости от того
                .setNegativeButton(R.string.dismissPass_button, this)                                                  //кто вызывает его (фрагмент/активность)
                .setTitle(R.string.search_header)
                .setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_search_pass_fragment, null))
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getShowsDialog()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        return inflater.inflate(R.layout.dialog_search_pass_fragment, container, false);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int number) {

        int cursor_out;
        String search_type_string = "";
        Dialog dialogForm = (Dialog) dialogInterface;
        Spinner spinner_items = (Spinner) dialogForm.findViewById(R.id.search_spinner_items);
        int search_type_id = spinner_items.getSelectedItemPosition();
        EditText search_field = (EditText) dialogForm.findViewById(R.id.search_field);
        String search_request = search_field.getText().toString();
        View fab = getActivity().findViewById(R.id.fab);
        FloatingActionButton floatingActionButton = (FloatingActionButton) fab;

        switch (search_type_id) {
            case 0: {
                search_type_string = "SITE";
                break;
            }
            case 1: {
                search_type_string = "LOGIN";
                break;
            }
            case 2: {
                search_type_string = "PASS";
                break;
            }
            case 3: {
                search_type_string = "INFO";
                break;
            }
        }

        switch(number) {
            case Dialog.BUTTON_POSITIVE: {
                try{
                    if(!search_request.equals("")) {
                        ListView listViewPasswords = (ListView) getActivity().findViewById(R.id.listPasswords);     //заполнение ListView
                            SQLiteOpenHelper helperSearch = new PasswordDatabaseHelper(getActivity());              //вызывается после каждой операции ввода/удаления/изменения
                            SQLiteDatabase DBSearch = helperSearch.getReadableDatabase();
                            cursorSearch = DBSearch.query(CNST_DB,
                                    new String[]{"_id", "SITE"},
                                    "upper("+search_type_string+") LIKE ?",                            //регистронезависимый поиск
                                    new String[]{'%'+search_request.toUpperCase()+'%'}, null, null, null);
                            cursor_out = cursorSearch.getCount();

                        if(cursor_out != 0) {
                            CursorAdapter cursorSearchAdapter = new SimpleCursorAdapter(getActivity().getBaseContext(),
                                    android.R.layout.simple_list_item_1,
                                    cursorSearch,
                                    new String[]{"SITE"},
                                    new int[]{android.R.id.text1}, 0);
                            listViewPasswords.setAdapter(cursorSearchAdapter);

                            dialogForm.dismiss();
                            Snackbar.make(fab, getResources().getString(R.string.search_result_is) + " " + cursor_out,
                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            floatingActionButton.hide();
                            DBSearch.close();
                            break;
                        } else {
                            dialogForm.dismiss();
                            Snackbar.make(fab, getResources().getString(R.string.search_result_is_null),
                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            DBSearch.close();
                            break;
                        }
                    } else {
                        dialogForm.dismiss();
                        Snackbar.make(fab, R.string.search_request_is_empty,
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        break;
                    }
                } catch(SQLiteException e) {
                    Snackbar.make(fab, R.string.database_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
            case Dialog.BUTTON_NEGATIVE: {
                dialogForm.dismiss();
                break;
            }
        }
    }
}
