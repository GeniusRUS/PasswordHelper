package com.genius.project.passwordhelper;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;

/**
 * Created by Genius on 29.11.2016.
 */

public class OptionPassDetail extends DialogFragment{                                              //обработчик свойств вызываемых по долгому тапу

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = this.getArguments();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.dialogOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        DialogFragment editPass = new EditPassFragment();
                        editPass.setArguments(bundle);
                        editPass.show(getFragmentManager(), "EditPass");
                        break;
                    }
                    case 1: {
                        DialogFragment confirm = new ConfirmAction();
                        confirm.show(getFragmentManager(), "ConfirmDialog");
                        break;
                    }
                }
            }
        });
        return builder.create();
    }
}
