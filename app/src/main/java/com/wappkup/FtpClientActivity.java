package com.wappkup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import  static com.wappkup.MainActivity.lblServerUri;



public class FtpClientActivity extends AppCompatActivity {
    ListView listViewFiles;
    ProgressBar progressBar;
    ArrayList<String> filesList = new ArrayList<String>();
    ArrayList<String> filesListDescr = new ArrayList<String>();
    ArrayList<Integer> imgiconsList = new ArrayList<Integer>();
    String CurrDir="/";

    ArrayList<threadsTransfers> listTrasfers = new ArrayList<threadsTransfers>();


    public void alert(String m, final boolean afterExit)
    {
        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setMessage(m)
                .setCancelable(false)
                .setPositiveButton(this.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (afterExit==true) FtpClientActivity.this.finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    protected void toast(String msg)
    {
        Context context = getApplicationContext();
        CharSequence text = (CharSequence) msg;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER|Gravity.CENTER, 20, 100);
        toast.show();
    }

    public static boolean downloadSingleFile(FTPClient ftpClient,
                                             String remoteFilePath, String savePath) throws IOException
    {
        File downloadFile = new File(savePath);

        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }

        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(downloadFile));
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }


    public void OpenFileByMime(final File temp_file)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(temp_file),getMimeType(temp_file.getAbsolutePath()));
                startActivity(intent);
            }
        });
    }

    private String getMimeType(String url)
    {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void scanListTrasfers()
    {
       for (;;)
       {
           try {
               Thread.sleep(2000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }

           //Open a file after download
           for (int i = 0; i < listTrasfers.size(); i++) {
               if (listTrasfers.get(i).OpenAfterDownload == true &&
                    listTrasfers.get(i).progressPercentage == 100) {
                    File f = new File(listTrasfers.get(i).saveFile);
                    OpenFileByMime(f);
                    listTrasfers.remove(i);
               }
           }
       }
    }


    public void openFile(String src)
    {
        Uri ftpUri = Uri.parse(lblServerUri.getText().toString());
        String[] userInfo=ftpUri.getUserInfo().split(":");
        String username= userInfo[0];
        String password =userInfo[1];
        String hostname =ftpUri.getHost();
        int port = ftpUri.getPort();

        FTPClient ftpClient = new FTPClient();

        try {
            // connect and login to the server
            ftpClient.connect(hostname, port);
            ftpClient.login(username, password);

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();

            System.out.println("Connected");

            String remoteFilePath = src;
            final String saveFilePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            +"/wappkup/"+FilenameUtils.getName(src);

            //downloadSingleFile(ftpClient, remoteFilePath,  saveFilePath);
            threadsTransfers tr = new threadsTransfers(ftpClient,remoteFilePath,saveFilePath);
            tr.OpenAfterDownload=true;
            listTrasfers.add(tr);
            tr.start();



            //only for debug
            /*runOnUiThread(new Runnable() {
                public void run() {
                    alert(saveFilePath,false);
                }
            });*/



            // log out and disconnect from the server
            //ftpClient.logout();
            //ftpClient.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

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
        filesListDescr.clear();

        runOnUiThread(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });



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

            for (final FTPFile file : files)
            {
                System.out.println(file.getName());
                if (!file.isDirectory())
                {
                    String ext=getFileExtension(file.getName());
                    switch(ext) {

                        case "pdf":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.pdf);
                            filesListDescr.add(getString(R.string.file));
                            break;
                        case "png":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.png);
                            filesListDescr.add(getString(R.string.file));
                            break;
                        case "jpg":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.jpg);
                            filesListDescr.add(getString(R.string.file));
                            break;
                        case "jpeg":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.jpg);
                            filesListDescr.add(getString(R.string.file));
                            break;
                        case "JPG":
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.jpg);
                            filesListDescr.add(getString(R.string.file));
                            break;

                        default:
                            filesList.add(file.getName());
                            imgiconsList.add(R.drawable.file);
                            filesListDescr.add(getString(R.string.file));
                    }

                }else{
                    //is directory
                    filesList.add(file.getName());
                    imgiconsList.add(R.drawable.folder_green_50px);
                    filesListDescr.add(getString(R.string.folder));
                }
            }

        } catch (IOException ex) {
            System.out.println("Errore nella connessione: " + ex.getMessage());
            ex.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                    alert(getString(R.string.error_on_connection),true);
                }
            });

        } finally {
            try {
                if (!client.isConnected())
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            alert(getString(R.string.error_on_connection),true);
                        }
                    });
                    return;
                }


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
                progressBar.setVisibility(View.INVISIBLE);
            }
        });


    }

    public void updateFileView() {

        //Filename
        final String[] files ;
        files= new String[filesList.size()];
        for (int i=0 ; i<=filesList.size()-1;i++) files[i]=filesList.get(i);

        //File icon
        final Integer[] imgicons;
        imgicons = new Integer[imgiconsList.size()];
        for (int i=0;i<=imgiconsList.size()-1;i++) imgicons[i]=imgiconsList.get(i);

        //File Descr
        final String[] descr;
        descr = new String[filesListDescr.size()];
        for (int i=0;i<=filesListDescr.size()-1;i++) descr[i]=filesListDescr.get(i);

        CustomListAdapter adapter=new CustomListAdapter(this, files, imgicons,descr);

        listViewFiles.setAdapter(adapter);
        //listViewFiles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //listViewFiles.setItemsCanFocus(false);
        //listViewFiles.setLongClickable(true);

        //Callback at click on listView item
        listViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                final String elemName= files[+position]; //grab filename
                String elemDescr=descr[+position]; //grab description
                int elemImg=imgicons[+position]; //grab icon's constant

                //if i click on folder
                if (elemImg==R.drawable.folder_green_50px)
                {
                    goInsideFolder(elemName);
                }else{
                    Toast.makeText(getApplicationContext(), elemName, Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            openFile(CurrDir+ "/"+elemName);
                        }
                    }).start();

                }

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
    protected void onResume()
    {
        super.onResume();

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
        progressBar =findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        new Thread(new Runnable(){
            @Override
            public void run() {
                scanListTrasfers();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        goBackFolder();
    }


}
