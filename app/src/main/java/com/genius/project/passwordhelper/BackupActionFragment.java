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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.CRC32;

import static com.genius.project.passwordhelper.PasswordDatabaseHelper.CNST_DB;

public class BackupActionFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private Button button_save;
    private Button button_read;
    private Button button_info;
    private Button button_delete;
    private Context context;
    private String filename = "BackupPasswords.csv";
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

    class backupSave extends AsyncTask<Void, Integer, Void> {
        SQLiteDatabase database;
        CSVWriter csvWrite;
        Cursor curCSV;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            button_save.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... voidArr) {
            int i = 0;
            try {
                csvWrite = new CSVWriter(new FileWriter(file));
                database = new PasswordDatabaseHelper(context).getReadableDatabase();
                curCSV = database.query(CNST_DB, new String[]{"SITE", "LOGIN", "PASS", "INFO"}, null, null, null, null, null);
                while(curCSV.moveToNext()) {
                    publishProgress(++i);
                    String arrStr[] = {curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3)};
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
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
            curCSV.close();
            database.close();
        }

        @Override
        protected void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            button_save.setText(getResources().getText(R.string.backup_indicate_prog) + " " + numArr[0]);
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
                passwordDatabaseHelper = new PasswordDatabaseHelper(context);
                database = passwordDatabaseHelper.getWritableDatabase();
                String next[] = {};

                CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)), "UTF8"));
                for(;;) {
                    next = reader.readNext();
                    if(next != null) {
                        publishProgress(++i);
                        passwordDatabaseHelper.insertPass(database, next[0], next[1], next[2], next[3]);
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                String error_string = getResources().getString(R.string.backup_read_error) + e.toString();
                Toast.makeText(context, error_string, Toast.LENGTH_SHORT).show();
                Log.e("READ EXCEPTION", "Error: " + e.toString());
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
                crc_hash = CRC32(stream_crc);
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

    private long CRC32(InputStream in) throws IOException {
        int gByte = 0;
        CRC32 gCRC = new CRC32();
        while ((gByte = in.read()) != -1) {
            gCRC.update(gByte);
        }
        in.close();
        return gCRC.getValue();
    }
}