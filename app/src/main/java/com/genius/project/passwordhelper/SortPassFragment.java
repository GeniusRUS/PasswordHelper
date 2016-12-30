package com.genius.project.passwordhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import static com.genius.project.passwordhelper.MainActivity.prefSort;
import static com.genius.project.passwordhelper.MainActivity.sortOrderIn;
import static com.genius.project.passwordhelper.MainActivity.sortTypeIn;

public class SortPassFragment extends DialogFragment implements DialogInterface.OnClickListener {

    SharedPreferences.Editor editor;
    Spinner spinner_type;
    Spinner spinner_order;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        sortTypeIn = prefSort.getString("SortingType", "");
        sortOrderIn = prefSort.getString("SortingOrder", "");
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sorting_pass_fragment, null);
        spinner_type = (Spinner) view.findViewById(R.id.sort_type);
        spinner_order = (Spinner) view.findViewById(R.id.sort_order);

        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0: {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(),
                        android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.sort_order_alpha));
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.notifyDataSetChanged();
                        spinner_order.setAdapter(adapter);
                        break;
                    }
                    case 1: {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(),
                        android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.sort_order_date));
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.notifyDataSetChanged();
                        spinner_order.setAdapter(adapter);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }
        });

        if(sortTypeIn.equals("SITE")) {
            spinner_type.setSelection(0);
        } else {
            spinner_type.setSelection(1);
        }

        if(sortOrderIn.equals("ASC")) {
            spinner_order.setSelection(0);
        } else {
            spinner_order.setSelection(1);
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
    public void onClick(DialogInterface dialogInterface, int i) {

        Dialog dialogForm = (Dialog) dialogInterface;
        spinner_type = (Spinner) dialogForm.findViewById(R.id.sort_type);
        spinner_order = (Spinner) dialogForm.findViewById(R.id.sort_order);
        int search_type_id = spinner_type.getSelectedItemPosition();
        int search_order_id = spinner_order.getSelectedItemPosition();

        switch (i) {
            case Dialog.BUTTON_POSITIVE: {
                editor = prefSort.edit();
                switch (search_type_id) {
                    case 0: {
                        sortTypeIn = "SITE";
                        editor.putString("SortingType", "SITE");
                        break;
                    }
                    case 1: {
                        sortTypeIn = "DATE";
                        editor.putString("SortingType", "_id");
                        break;
                    }
                }

                switch (search_order_id) {
                    case 0: {
                        sortOrderIn = "ASC";
                        editor.putString("SortingOrder", "ASC");
                        break;
                    }
                    case 1: {
                        sortOrderIn = "DESC";
                        editor.putString("SortingOrder", "DESC");
                        break;
                    }
                }
                editor.apply();
                MainActivity main = (MainActivity) getActivity();
                main.updateMainList(false);
                break;
            }
            case Dialog.BUTTON_NEGATIVE: {
                dialogForm.dismiss();
                break;
            }
        }
    }
}
