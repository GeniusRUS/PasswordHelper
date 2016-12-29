package com.genius.project.passwordhelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Genius on 10.12.2016.
 */

public class ConfirmWipe extends DialogFragment {

    ConfirmWipeListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());                       // Use the Builder class for convenient dialog construction
        builder.setMessage(R.string.confirmAction)
                .setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onWipePositiveClick(ConfirmWipe.this);
                    }
                })
                .setNegativeButton(R.string.dismissPass_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onWipeNegativeClick(ConfirmWipe.this);
                    }
                });
        return builder.create();
    }

    public interface ConfirmWipeListener {
        void onWipePositiveClick(DialogFragment dialog);
        void onWipeNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(Activity activity) {                                                       // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        super.onAttach(activity);
        try {                                                                                       // Verify that the host activity implements the callback interface
            mListener = (ConfirmWipe.ConfirmWipeListener) activity;                                            // Instantiate the NoticeDialogListener so we can send events to the host
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());                            // The activity doesn't implement the interface, throw exception
        }
    }
}
