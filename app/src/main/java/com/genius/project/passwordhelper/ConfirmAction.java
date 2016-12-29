package com.genius.project.passwordhelper;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;

/**
 * Created by Genius on 29.11.2016.
 */

public class ConfirmAction extends DialogFragment {

    ConfirmDialogListener mListener;                                                                 // Use this instance of the interface to deliver action events

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());                       // Use the Builder class for convenient dialog construction
        builder.setMessage(R.string.confirmAction)
                .setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(ConfirmAction.this);                        //пробрасываем событие в слушатель, метод которого переопределен в MainActivity
                    }
                })
                .setNegativeButton(R.string.dismissPass_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(ConfirmAction.this);                        //пробрасываем событие в слушатель, метод которого переопределен в MainActivity
                    }
                });
        return builder.create();                                                                    // Create the AlertDialog object and return it
    }

    public interface ConfirmDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(Activity activity) {                                                       // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        super.onAttach(activity);
        try {                                                                                       // Verify that the host activity implements the callback interface
            mListener = (ConfirmDialogListener) activity;                                            // Instantiate the NoticeDialogListener so we can send events to the host
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());                                        // The activity doesn't implement the interface, throw exception
        }
    }
}
