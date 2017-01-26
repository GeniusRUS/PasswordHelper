package com.genius.project.passwordhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by Genius on 26.01.2017.
 */

public class PasswordGeneratorFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static String generate(int countChars) {
        String pass = "";
        Random r = new Random();

        for (int i = 0; i < countChars; ++i) {
            char next = 0;
            int range = 10;

            switch(r.nextInt(3)) {
                case 0: {next = '0'; range = 10;} break;
                case 1: {next = 'a'; range = 26;} break;
                case 2: {next = 'A'; range = 26;} break;
            }

            pass += (char)((r.nextInt(range)) + next);
        }

        return pass;
    }

    private TextView password;
    private SeekBar countChars;
    private String pass;
    private TextView count;
    private AlertDialog dialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_pass_generator, null);
        dialog = new AlertDialog.Builder(getActivity())
                .setNeutralButton(R.string.generate_action, this)
                .setPositiveButton(R.string.enterPass_button, this)
                .setTitle(R.string.generator_header)
                .setView(view)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button neutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                neutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(countChars.getProgress() <= 0) {
                            Toast.makeText(getActivity(), R.string.generate_zero_count_error, Toast.LENGTH_SHORT).show();
                        } else {
                            pass = generate(countChars.getProgress());
                            password.setText(pass);
                            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("copied_password", pass);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getActivity(), R.string.generate_copy_in_clipboard, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        password = (TextView) view.findViewById(R.id.password);
        count = (TextView) view.findViewById(R.id.count);
        countChars = (SeekBar) view.findViewById(R.id.countChars);
        pass = generate(countChars.getProgress());
        password.setText(pass);

        countChars.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String string = String.valueOf(seekBar.getProgress()) + getResources().getString(R.string.generate_symbols);
                count.setText(string);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                String string = String.valueOf(seekBar.getProgress()) + getResources().getString(R.string.generate_symbols);
                count.setText(string);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String string = String.valueOf(seekBar.getProgress()) + getResources().getString(R.string.generate_symbols);
                count.setText(string);
            }
        });

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getShowsDialog()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        return inflater.inflate(R.layout.dialog_add_pass_fragment, container, false);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Dialog dialogForm = (Dialog) dialogInterface;
        switch(i) {
            case Dialog.BUTTON_POSITIVE: {
                dialogForm.dismiss();
                break;
            }
        }
    }
}