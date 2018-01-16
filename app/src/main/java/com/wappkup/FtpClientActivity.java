package com.wappkup;

import android.content.Context;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import  static com.wappkup.MainActivity.lblServerUri;

public class FtpClientActivity extends AppCompatActivity {
    ListView listViewFiles;
    ArrayList<String> filesList = new ArrayList<String>();
    ArrayList<Integer> imgiconsList = new ArrayList<Integer>();
    String CurrDir="/";

    protected void toast(String msg)
    {
        Context context = getApplicationContext();
        CharSequence text = (CharSequence) msg;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER|Gravity.CENTER, 20, 100);
        toast.show();
    }



    public void updateFiles()  {
        Uri ftpUri = Uri.parse(lblServerUri.getText().toString());
        String[] userInfo=ftpUri.getUserInfo().split(":");
        String username= userInfo[0];
        String password =userInfo[1];
        String hostname =ftpUri.getHost();
        int port = ftpUri.getPort();


        // Valori statici per debug
        /*String username="fabio";
        String password="secret";
        String hostname="192.168.1.4";
        int port=2221;*/

        System.out.println("------------Miei dati-------------");
        System.out.printf("Username: %s\n",username);
        System.out.printf("Password: %s\n",password);
        System.out.printf("Hostname: %s\n",hostname);
        System.out.printf("Port: %d\n",port);
        System.out.println("-------------------------");

        //Clear the list of files
        filesList.clear();
        imgiconsList.clear();


        FTPClient client = new FTPClient();
        try {
            /*client.type(FTP.BINARY_FILE_TYPE);
            client.setDefaultPort(port);
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setFileType(FTP.BINARY_FILE_TYPE, FTP.ASCII_FILE_TYPE);
            client.setFileTransferMode(FTP.BINARY_FILE_TYPE);*/

            client.connect(hostname, port);
            client.enterLocalPassiveMode();
            client.login(username, password);

            FTPFile[] files = client.listFiles(CurrDir);
            for (final FTPFile file : files) {
                System.out.println(file.getName());

                if (file.isDirectory())
                {
                    filesList.add(file.getName());
                    imgiconsList.add(R.drawable.folder_green_50px);
                }else{
                    String ext=getFileExtension(file.getName());
                    switch(ext) {

                        case "pdf":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.pdf);
                            break;
                        case "png":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.png);
                            break;
                        case "jpg":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.jpg);
                            break;
                        case "jpeg":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.jpg);
                            break;
                        case "JPG":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.jpg);
                            break;

                        default:
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.file);

                    }

                }
            }

        } catch (IOException ex) {
            System.out.println("Errore nella connessione: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //At the end update gui
        runOnUiThread(new Runnable() {
            public void run() {
                updateFileView();
            }
        });


    }

    public void updateFileView() {

        final String[] files ;
        files= new String[filesList.size()];
        for (int i=0 ; i<=filesList.size()-1;i++) files[i]=filesList.get(i);

        final Integer[] imgicons;
        imgicons = new Integer[imgiconsList.size()];
        for (int i=0;i<=imgiconsList.size()-1;i++) imgicons[i]=imgiconsList.get(i);

        CustomListAdapter adapter=new CustomListAdapter(this, files, imgicons);

        listViewFiles.setAdapter(adapter);
        //listViewFiles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //listViewFiles.setItemsCanFocus(false);
        //listViewFiles.setLongClickable(true);

        //Callback at click
        listViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {

                String elemName= files[+position]; //grab filename
                int elemImg=imgicons[+position]; //grab icon's constant
                Toast.makeText(getApplicationContext(), elemName, Toast.LENGTH_SHORT).show();
                if (elemImg==R.drawable.folder_green_50px) goInsideFolder(elemName);

            }
        });

    }

    public String getFileExtension(String f)
    {
        return FilenameUtils.getExtension(f);
    }

    public void goBackFolder()
    {
        if (!CurrDir.equals("/"))
        {
            File file = new File(CurrDir);
            String parentPath = file.getAbsoluteFile().getParent();
            CurrDir=parentPath;

            new Thread(new Runnable(){
                @Override
                public void run() {
                    updateFiles();
                }
            }).start();

        }else{
            this.finish();
        }
    }

    public void goInsideFolder(String folder)
    {
        CurrDir=CurrDir+"/"+folder;
        new Thread(new Runnable(){
            @Override
            public void run() {
                updateFiles();
            }
        }).start();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_client);

        listViewFiles = findViewById(R.id.listViewFiles);

        new Thread(new Runnable(){
            @Override
            public void run() {
                updateFiles();
            }
        }).start();


    }

    @Override
    public void onBackPressed() {
        goBackFolder();
    }


}
