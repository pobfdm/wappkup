package com.wappkup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;

    public static TextView lblServerUri;

    ToggleButton toggleFtpServer;
    TextView txtStatus, txtUrl, txtPassword;
    Spinner spinnerMountpoints;
    Button btnScanQr;

    FtpServer server;
    FtpServerFactory serverFactory ;
    String password;
    public static String serverUri; //for connection with external server

    List<String> mountPoints = new ArrayList<String>();




    public class checkThread extends Thread {

        public void run(){

            for(;;) {
                if (checkWifiOnAndConnected() == false) {
                    server.suspend();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            toggleFtpServer.setChecked(false);
                            spinnerMountpoints.setEnabled(true);
                            txtStatus.setText(R.string.wifi_is_down);
                            txtUrl.setVisibility(View.INVISIBLE);
                            txtPassword.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPassword()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("wappkup", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String password=pref.getString("password", null);

        if (password==null|| password.isEmpty())
        {
            Random generator = new Random();
            int i = generator.nextInt(9000) + 1000;
            return String.valueOf(i);
        }else {
            return password;
        }
    }

    public String getWhatsAppFolder()
    {
        String res;
        File root= new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/WhatsApp");
        if (root.exists())
        {
            res=Environment.getExternalStorageDirectory().getAbsolutePath()+"/WhatsApp";
        }else{
            res=Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return res;
    }

    public String getSdcard0MountPoint()
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getSdcardsFolder()
    {
        File mf = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        print("Folder: "+mf.getParent()+"/");
        return  mf.getParent()+"/";

    }

    public boolean checkpermissions()
    {
        int permissionCheckWrite = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheckWrite== PackageManager.PERMISSION_GRANTED
                && permissionCheckRead==PackageManager.PERMISSION_GRANTED )
        {
            return true;
        }else{
            return false;
        }
    }

    public void requestPermission()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&

                ContextCompat.checkSelfPermission(this,
                        READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED

                )
        {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    READ_EXTERNAL_STORAGE)) {

                alert( this.getString(R.string.i_need_Read_Write_permissions));


            } else {
                //Richiedo i permessi di lettura e scrittura sulle memorie esterne
                ActivityCompat.requestPermissions(this,
                        new String[]{WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                ActivityCompat.requestPermissions(this,
                        new String[]{READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_EXTERNAL_STORAGE);





            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        int p=0;

        if (requestCode ==  MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                toast(this.getString(R.string.writing_permissions_granted));
                p++;

            } else {
                // Permission request was denied.
                //toast("Permessi  scrittura NEGATI !!!");

            }
        }
        if (requestCode ==  MY_PERMISSIONS_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                toast(this.getString(R.string.reading_permissions_granted));
                p++;
            } else {
                // Permission request was denied.
                //toast("Permessi  lettura NEGATI !!!");

            }

            if(p==2)
            {
                updateMountPoints();
                updateSpinner();
                initFtp();
            }

        }



    }


    public void updateSpinner()
    {
        //Per lo spinner
        String array_spinner[];
        array_spinner=new String[mountPoints.size()+1];

        array_spinner[0]=getFileNameFromPath(getWhatsAppFolder());
        for (int i = 0; i<mountPoints.size();i++)
        {
            array_spinner[i+1]=getFileNameFromPath(mountPoints.get(i));
        }
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, array_spinner);
        spinnerMountpoints.setAdapter(adapter);
    }

    public void updateMountPoints()
    {

        //Detect mountpoint in alternative way (getenv)
        String primary_sd = System.getenv("EXTERNAL_STORAGE");
        if(primary_sd != null) mountPoints.add(primary_sd);

        String secondary_sd = System.getenv("SECONDARY_STORAGE");
        if(secondary_sd != null)  mountPoints.add(secondary_sd);


        //scan sdcards dir
        File directory = new File(getSdcardsFolder());
        if(directory.isDirectory() && directory.canRead())
        {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (!files[i].getAbsolutePath().equals(primary_sd)
                        &&!files[i].getAbsolutePath().equals(secondary_sd)) mountPoints.add(files[i].getAbsolutePath());
            }
        }






        //Detect mountpoint in alternative way (getExternalFilesDirs)
        /*if (primary_sd==null)
        {
            File[] dirs = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
            for (int i = 0; i < dirs.length; i++) {
                if (folderUsable(dirs[i].toString())) {
                     mountPoints.add(dirs[i].toString());
                }
            }
        }*/

    }

    public String getFileNameFromPath(String p)
    {
        String basename = FilenameUtils.getBaseName(p);
        return basename;
    }


    public void print(String s)
    {
        System.out.println(s);
    }

    public void alert(String m)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(this.getString(R.string.alert));
        alertDialog.setMessage(m);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public boolean folderUsable(String f)
    {
        File folder = new File(f);
        if (folder.isDirectory() && folder.canRead() && folder.canWrite())
        {
            return true;
        }else{
            return false;
        }
    }

    public void initFtp()
    {

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(2221); //la porta
        serverFactory.addListener("default", factory.createListener());


        //Genera una password
        password=getPassword();
        txtPassword.setText("Password: "+password);

        //Utente wapp
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());

        if(folderUsable(getWhatsAppFolder())) {
            BaseUser wapp = new BaseUser();
            wapp.setName("wapp");
            wapp.setPassword(password);
            wapp.setHomeDirectory(getWhatsAppFolder());
            //solo per wapp
            try {
                serverFactory.getUserManager().save(wapp);
            } catch (FtpException e) {
                e.printStackTrace();
            }
            wapp.setAuthorities(authorities);
        }

        //update mountpoints and spinner

        if (mountPoints.size()>0)
        {
            //Dichiaro un array per gli utenti;
            BaseUser[] users = new BaseUser[mountPoints.size()];
            for (int i =0; i< mountPoints.size(); i++ )
            {
                if (folderUsable(mountPoints.get(i))) {
                    serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
                    users[i] = new BaseUser();
                    users[i].setName(getFileNameFromPath(mountPoints.get(i)));
                    users[i].setPassword(password);
                    users[i].setHomeDirectory(mountPoints.get(i));
                    users[i].setAuthorities(authorities);
                    try {
                        //salvo le preferenze
                        serverFactory.getUserManager().save(users[i]);
                    } catch (FtpException e) {
                        e.printStackTrace();
                    }
                }
            }
        }









        //avvio il server e lo sospendo
        try {
            server.start();
            server.suspend();
        } catch (FtpException e) {
            e.printStackTrace();
            txtStatus.setText(R.string.error_on_start_ftp_server);
        }

    }

    public String getIp()
    {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
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

    public void shareFtpLink(View v)
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, txtUrl.getText()+"\n\nPassword: "+ password);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, this.getString(R.string.share_with)));
    }




    public void onToggleServerClick(View v)
    {
        if (!checkpermissions())
        {
            txtStatus.setText(R.string.read_Write_on_external_devices_denided);
            toggleFtpServer.setChecked(false);
            return;
        }


        if (checkWifiOnAndConnected()==false)
        {
            txtStatus.setText(R.string.wifi_is_down);
            toggleFtpServer.setChecked(false);
            return;
        }

        if (server.isStopped())
        {
            txtStatus.setText(R.string.error_on_start_ftp_server);
            return;
        }
        if (server.isSuspended()) {
            server.resume();
            if (!server.isSuspended()) {
                txtStatus.setText(R.string.server_started);
                String lblUser=spinnerMountpoints.getSelectedItem().toString();
                if (lblUser.equals("WhatsApp")) lblUser="wapp";
                spinnerMountpoints.setEnabled(false);
                txtUrl.setText("ftp://"+lblUser+"@"+getIp()+":2221/");
                txtUrl.setVisibility(View.VISIBLE);
                txtPassword.setVisibility(View.VISIBLE);
                toast(this.getString(R.string.click_on_url_to_share));
                //qrcode
                qrcodeShow("ftp://"+lblUser+":"+password+"@"+getIp()+":2221/");
            }
        }else{
            server.suspend();
            if (server.isSuspended()) txtStatus.setText(R.string.server_stopped);
            txtUrl.setText("");
            txtUrl.setVisibility(View.INVISIBLE);
            txtPassword.setVisibility(View.INVISIBLE);
            spinnerMountpoints.setEnabled(true);
        }



    }

    public void scanQr(View v)
    {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();

    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null)
        {
            String scanFormat = scanningResult.getFormatName();
            String scanContent = scanningResult.getContents();
            lblServerUri.setText(scanContent);

            runFtpClient(null);

        }else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void runFtpClient(View v)
    {
        if (!lblServerUri.getText().toString().isEmpty())
        {
            //start ftp client...
            Intent ftpClientIntent = new Intent(getApplicationContext(), FtpClientActivity.class);
            startActivity(ftpClientIntent);
        }else{
            toast(getString(R.string.url_wrong));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toggleFtpServer = findViewById(R.id.toggleFtpServer);
        toggleFtpServer.setChecked(false);

        txtStatus = findViewById(R.id.txtStatus);
        txtUrl = findViewById(R.id.txtUrl);
        txtPassword= findViewById(R.id.txtPassword);
        txtPassword.setVisibility(View.INVISIBLE);
        txtUrl.setVisibility(View.INVISIBLE);

        spinnerMountpoints = findViewById(R.id.spinnerMountPoints);

        btnScanQr=findViewById(R.id.btnScanQr);

        serverFactory = new FtpServerFactory();
        server = serverFactory.createServer();

        if (checkpermissions()==true) {
            //Server ftp start
            updateMountPoints();
            updateSpinner();
            initFtp();

            //Controllore connessione
            checkThread controller = new checkThread();
            controller.start();
        }else{
            print("Permission errors");
            requestPermission();
        }

        //Tab Host
        TabHost host = (TabHost)findViewById(R.id.tabhostMain);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("tab1");
        spec.setContent(R.id.tab1);
        spec.setIndicator(this.getString(R.string.share));
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("tab2");
        spec.setContent(R.id.tab2);
        spec.setIndicator(this.getString(R.string.browse));
        host.addTab(spec);

        lblServerUri= findViewById(R.id.lblServerUri);


    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setMessage(this.getString(R.string.are_you_sure_you_want_to_exit))
                .setCancelable(false)
                .setPositiveButton(this.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void AboutDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Author : Fabio Di Matteo \npobapplications@gmail.com")
                .setTitle(this.getString(R.string.about));
        builder.setPositiveButton(this.getString(R.string.okay), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //noxthing
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void qrcodeShow(String str)
    {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ((ImageView) findViewById(R.id.imgQR)).setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu0, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.mnuPreferences:
                Intent intent = new Intent(MainActivity.this, Preferences.class);
                startActivity(intent);
                break;
            case R.id.mnuAbout:
                AboutDialog();
                break;
            case R.id.mnuExit:
                onBackPressed();
        }
        return false;
    }

}
