package com.wappkup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Preferences extends AppCompatActivity {

    Button btSaveAll;
    EditText editPersistentPassword;

    protected void toast(String msg)
    {
        Context context = getApplicationContext();
        CharSequence text = (CharSequence) msg;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER|Gravity.CENTER, 20, 100);
        toast.show();
    }

    public void alert(String m)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(m);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void savePassword()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("wappkup", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("password", editPersistentPassword.getText().toString());
        editor.commit();

    }

    public void loadPassword()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("wappkup", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String password=pref.getString("password", null);
        editPersistentPassword.setText(password);
    }

    public void saveAll(View v)
    {
        savePassword();
        toast(this.getString(R.string.wappkup_need_to_be_restarted));
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        btSaveAll = findViewById(R.id.btSaveAll);
        editPersistentPassword= findViewById(R.id.editPersistentPassword);
        loadPassword();
    }
}
