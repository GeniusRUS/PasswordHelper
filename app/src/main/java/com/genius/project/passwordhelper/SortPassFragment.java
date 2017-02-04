package com.genius.project.passwordhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import static com.genius.project.passwordhelper.PasswordDatabaseHelper.ID;
import static com.genius.project.passwordhelper.PasswordDatabaseHelper.SITE;
import static com.genius.project.passwordhelper.SettingsActivity.PASSHELPER_PREF;

public class SortPassFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public final static String SORTING_TYPE = "SType";
    public final static String SORTING_ORDER = "SOrder";
    private Spinner spinner_type;
    private Spinner spinner_order;
    private SharedPreferences preferences;
    private String sortTypeIn;
    private String sortOrderIn;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sorting_pass_fragment, null);
        preferences = getActivity().getSharedPreferences(PASSHELPER_PREF, Context.MODE_PRIVATE);

        sortTypeIn = preferences.getString(SORTING_TYPE, "SITE");
        sortOrderIn = preferences.getString(SORTING_ORDER, "ASC");

        spinner_type = (Spinner) view.findViewById(R.id.sort_type);
        spinner_order = (Spinner) view.findViewById(R.id.sort_order);

        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String[] type;
                switch (position) {
                    case 0: {
                        type = getResources().getStringArray(R.array.sort_order_alpha);
                        break;
                    }
                    case 1: {
                        type = getResources().getStringArray(R.array.sort_order_date);
                        break;
                    }
                    default: {
                        type = getResources().getStringArray(R.array.sort_order_alpha);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, type);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.notifyDataSetChanged();
                spinner_order.setAdapter(adapter);

                if(sortOrderIn.equals("ASC")) {
                    spinner_order.setSelection(0);
                } else {
                    spinner_order.setSelection(1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        if (sortTypeIn.equals(SITE)) {
            spinner_type.setSelection(0);
        } else {
            spinner_type.setSelection(1);
        }

        return new AlertDialog.Builder(getActivity())                                               //реализация в виде, совместимом с API 25-
                .setPositiveButton(R.string.sort_button, this)                                                  //также отображает диалог, в зависимости от того
                .setNegativeButton(R.string.dismissPass_button, this)                                                  //кто вызывает его (фрагмент/активность)
                .setTitle(R.string.sort_header)
                .setView(view)
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getShowsDialog()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        return inflater.inflate(R.layout.dialog_sorting_pass_fragment, container, false);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int buttonId) {

        Dialog dialogForm = (Dialog) dialogInterface;
        spinner_type = (Spinner) dialogForm.findViewById(R.id.sort_type);
        spinner_order = (Spinner) dialogForm.findViewById(R.id.sort_order);

        switch (buttonId) {
            case Dialog.BUTTON_POSITIVE: {
                SharedPreferences.Editor editor = preferences.edit();
                switch (spinner_type.getSelectedItemPosition()) {
                    case 0: {
                        sortTypeIn = SITE;
                        editor.putString(SORTING_TYPE, SITE);
                        break;
                    }
                    case 1: {
                        sortTypeIn = "DATE";
                        editor.putString(SORTING_TYPE, ID);
                        break;
                    }
                }

                switch (spinner_order.getSelectedItemPosition()) {
                    case 0: {
                        sortOrderIn = "ASC";
                        editor.putString(SORTING_ORDER, "ASC");
                        break;
                    }
                    case 1: {
                        sortOrderIn = "DESC";
                        editor.putString(SORTING_ORDER, "DESC");
                        break;
                    }
                }
                editor.apply();
                break;
            }
            case Dialog.BUTTON_NEGATIVE: {
                dialogForm.dismiss();
                break;
            }
        }
    }
}
