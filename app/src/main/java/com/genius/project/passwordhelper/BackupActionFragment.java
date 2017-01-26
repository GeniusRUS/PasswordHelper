package com.genius.project.passwordhelper;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class BackupActionFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private Button button_save;
    private Button button_read;
    private Button button_info;
    private Button button_delete;
    private Context context;
    private String filename = "BackupPasswords.txt";
    private File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        View inflate = getActivity().getLayoutInflater().inflate(R.layout.dialog_backup_fragment, null);
        context = getActivity().getBaseContext();
        button_save = (Button) inflate.findViewById(R.id.backup_save_button);
        button_read = (Button) inflate.findViewById(R.id.backup_read_button);
        button_info = (Button) inflate.findViewById(R.id.backup_info_button);
        button_delete = (Button) inflate.findViewById(R.id.backup_delete_button);
        PasswordDatabaseHelper passwordDatabaseHelper = new PasswordDatabaseHelper(context);
        SQLiteDatabase database = passwordDatabaseHelper.getWritableDatabase();
        Cursor cursor = database.query("DATAPASS", new String[]{"SITE", "LOGIN", "PASS", "INFO"}, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            button_save.setEnabled(true);
            button_save.setActivated(true);
            if (file.exists()) {
                buttonSetter(true);
            } else {
                buttonSetter(false);
            }
        } else {
            button_save.setEnabled(false);
            button_save.setActivated(false);
            if (file.exists()) {
                buttonSetter(true);
            } else {
                buttonSetter(false);
            }
        }

        button_info.setText(getResources().getString(R.string.backup_info));
        button_save.setOnClickListener(new buttonSaveListener());
        button_read.setOnClickListener(new buttonReadListener());
        button_info.setOnClickListener(new buttonInfoListener());
        button_delete.setOnClickListener(new buttonDeleteListener());

        cursor.close();
        database.close();
        return new Builder(getActivity()).setNegativeButton(R.string.enterPass_button, this).setTitle(R.string.backup_header).setView(inflate).create();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return getShowsDialog() ? super.onCreateView(layoutInflater, viewGroup, bundle) : layoutInflater.inflate(R.layout.dialog_backup_fragment, viewGroup, false);
    }

    class buttonSaveListener implements View.OnClickListener {

        public void onClick(View view) {
            new backupSave().execute();
            buttonSetter(true);
        }
    }

    class buttonReadListener implements View.OnClickListener {
        public void onClick(View view) {
            new backupRead().execute();
        }
    }

    class buttonInfoListener implements View.OnClickListener {
        public void onClick(View view) {
            new backupInfo().execute();
        }
    }

    class buttonDeleteListener implements View.OnClickListener {
        public void onClick(View view) {
            new deleteFileBackup().execute();
            buttonSetter(false);
        }
    }

    class backupRead extends AsyncTask<Void, Integer, Void> {
        SQLiteDatabase database;
        PasswordDatabaseHelper passwordDatabaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            button_read.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int i = 0;
            try {
                String[] strArr = new String[4];
                passwordDatabaseHelper = new PasswordDatabaseHelper(context);
                database = passwordDatabaseHelper.getWritableDatabase();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)), "UTF8"));
                while (bufferedReader.ready()) {
                    publishProgress(++i);
                    String[] string_temp = bufferedReader.readLine().split(" :/:");
                    if (string_temp.length == 1) {
                        strArr[0] = string_temp[0].replace(" :/:", "").trim();
                        strArr[1] = "";
                        strArr[2] = "";
                        strArr[3] = "";
                    }
                    if (string_temp.length == 2) {
                        strArr[0] = string_temp[0].trim();
                        strArr[1] = string_temp[1].replace(" :/:", "").trim();
                        strArr[2] = "";
                        strArr[3] = "";
                    }
                    if (string_temp.length == 3) {
                        strArr[0] = string_temp[0].trim();
                        strArr[1] = string_temp[1].trim();
                        strArr[2] = string_temp[2].replace(" :/:", "").trim();
                        strArr[3] = "";
                    }
                    if (string_temp.length == 4) {
                        strArr[0] = string_temp[0].trim();
                        strArr[1] = string_temp[1].trim();
                        strArr[2] = string_temp[2].trim();
                        strArr[3] = string_temp[3].replace(" :/:", "").trim();
                    }
                    passwordDatabaseHelper.insertPass(database, strArr[0], strArr[1], strArr[2], strArr[3]);
                }
            } catch (IOException e) {
                Log.e("Exception", "File read failed: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            Toast.makeText(context, getResources().getString(R.string.backup_read_comlete), Toast.LENGTH_SHORT).show();
            button_read.setClickable(true);
            button_save.setActivated(true);
            button_save.setEnabled(true);
            button_read.setText(R.string.backup_read);
            database.close();
        }

        @Override
        protected void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            button_read.setText(getResources().getText(R.string.backup_indicate_read) + " " + numArr[0]);
        }
    }

    class backupSave extends AsyncTask<Void, Integer, Void> {
        SQLiteDatabase database;
        Cursor cursor;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            button_save.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... voidArr) {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)), "UTF8"));
                database = new PasswordDatabaseHelper(context).getReadableDatabase();
                cursor = database.query("DATAPASS", new String[]{"SITE", "LOGIN", "PASS", "INFO"}, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    publishProgress(1);
                    bufferedWriter.write(cursor.getString(0) + " :/: " + cursor.getString(1) + " :/: " + cursor.getString(2) + " :/: " + cursor.getString(3) + "\n");
                    bufferedWriter.flush();
                }
                bufferedWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            Toast.makeText(context, getResources().getString(R.string.backup_save_complete), Toast.LENGTH_SHORT).show();
            button_save.setClickable(true);
            button_save.setText(R.string.backup_create);
            database.close();
            cursor.close();
        }

        @Override
        protected void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            button_save.setText(getResources().getText(R.string.backup_indicate_prog) + " " + numArr[0]);
        }
    }

    class backupInfo extends AsyncTask<Void, Void, Void> {
        SQLiteDatabase database;
        PasswordDatabaseHelper passwordHepler;
        String name;
        int count_elements;
        Date date;
        long crc_hash;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

        @Override
        protected Void doInBackground(Void... voidArr) {
            try {
                count_elements = 0;
                passwordHepler = new PasswordDatabaseHelper(getActivity());
                database = this.passwordHepler.getWritableDatabase();
                InputStream stream = new FileInputStream(file);
                InputStream stream_crc = new FileInputStream(file);
                name = file.getName();
                crc_hash = BackupActionFragment.CRC32(stream_crc);
                Reader inputStreamReader = new InputStreamReader(stream, "UTF8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                this.date = new Date(file.lastModified());
                while (bufferedReader.ready()) {
                    bufferedReader.readLine();
                    this.count_elements++;
                }
                stream.close();
                stream_crc.close();
                inputStreamReader.close();
                bufferedReader.close();
            } catch (IOException e) {
                Log.e("Exception", "File info failed: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_backup_info_fragment, null);
            TextView text_name = (TextView) view.findViewById(R.id.backup_info_name);
            TextView text_count = (TextView) view.findViewById(R.id.backup_info_count);
            TextView text_crc = (TextView) view.findViewById(R.id.backup_info_crc);
            TextView text_date = (TextView) view.findViewById(R.id.backup_info_date_changed);
            String outName = getResources().getString(R.string.backup_info_name) + name;
            String outCount = getResources().getString(R.string.backup_info_count) + count_elements;
            String outCrc = getResources().getString(R.string.backup_info_crc) + String.format(Locale.ENGLISH, "%d", crc_hash);
            String outDate = getResources().getString(R.string.backup_info_date_changed) + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(file.lastModified());
            text_name.setText(outName);
            text_count.setText(outCount);
            text_crc.setText(outCrc);
            text_date.setText(outDate);
            Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.backup_info).setView(view).create();
            builder.create().show();
            database.close();
        }
    }

    class deleteFileBackup extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voidArr) {
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename).delete();
            return null;
        }

        @Override
        protected void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            Toast.makeText(context, getResources().getString(R.string.backup_file_deleted), Toast.LENGTH_SHORT).show();
        }
    }

    private void buttonSetter(boolean isEnable) {
        button_read.setEnabled(isEnable);
        button_read.setActivated(isEnable);
        button_info.setEnabled(isEnable);
        button_info.setActivated(isEnable);
        button_delete.setEnabled(isEnable);
        button_delete.setActivated(isEnable);
    }

    private static long CRC32(InputStream in) throws IOException {
        Checksum sum_control = new CRC32();
        for (int b = in.read(); b != -1; b = in.read()) {
            sum_control.update(b);
        }
        return sum_control.getValue();
    }
}