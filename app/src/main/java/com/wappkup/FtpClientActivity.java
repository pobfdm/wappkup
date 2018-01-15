package com.wappkup;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import  static com.wappkup.MainActivity.lblServerUri;

public class FtpClientActivity extends AppCompatActivity {
    ListView listViewFiles;

    public void updateFileView()
    {
        ListView list;
        final String[] files ={
                "Documenti",
                "Downloads",
                "Desktop",
                "Musica",
                "folder0",
                "Folder1",
                "Folder2",
                "folder3",
                "folder4",
                "folder5",
                "folder6",
                "folder7"
        };

        Integer[] imgicons= {
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px,
                R.drawable.folder_green_50px
        };
        CustomListAdapter adapter=new CustomListAdapter(this, files, imgicons);

        listViewFiles.setAdapter(adapter);
        listViewFiles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listViewFiles.setItemsCanFocus(false);
        listViewFiles.setLongClickable(true);

        //Callback at click
        listViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String Slecteditem= files[+position];
                Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();

            }
        });

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_client);

        listViewFiles = findViewById(R.id.listViewFiles);
        updateFileView();


    }

}
