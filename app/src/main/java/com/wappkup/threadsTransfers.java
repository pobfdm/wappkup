package com.wappkup;

import android.net.Uri;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class threadsTransfers extends Thread
{
    public boolean active=false;
    private enum fileType {DIR,PNG,PDF,JPG,TXT,APK};
    public String serverUri,origin, saveFile;
    public boolean OpenAfterDownload=false;
    public enum type {DOWNLOAD,UPLOAD};
    public int progressPercentage=0;
    private boolean abort=false;
    private boolean success=false;
    public FTPClient ftpClient;
    public static int countNotification;
    public int idNotification=0;

    public threadsTransfers(String serverUri,String origin, String saveFile)
    {
        this.serverUri=serverUri;
        this.origin=origin;
        this.saveFile=saveFile;
        countNotification++;
        idNotification=countNotification;
    }



    public void abortNow()
    {
        this.abort=true;
        //non finita
    }

    public String getSorceFile()
    {
        return this.origin;
    }
    public String getDestFile()
    {
        return this.saveFile;
    }





    public long getRemoteFileSize(String path)
    {
        //Connection
        Uri ftpUri = Uri.parse(serverUri);
        String[] userInfo=ftpUri.getUserInfo().split(":");
        String username= userInfo[0];
        String password =userInfo[1];
        String hostname =ftpUri.getHost();
        int port = ftpUri.getPort();
        long remoteFileSize=0;

        FTPClient ftpClientM = new FTPClient();
        try {
            // connect and login to the server
            ftpClientM.connect(hostname, port);
            ftpClientM.login(username, password);
            ftpClientM.setFileType(FTP.BINARY_FILE_TYPE);

            // use local passive mode to pass firewall
            ftpClientM.enterLocalPassiveMode();
            System.out.println("Connected");

            //get file size
            FTPFile remoteFtpFile = null;
            remoteFtpFile = ftpClientM.mlistFile(path);
            remoteFileSize = remoteFtpFile.getSize();
            ftpClientM.logout();
            ftpClientM.disconnect();
            return remoteFileSize;

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (ftpClientM.isConnected()) {
                try {
                    ftpClientM.disconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return remoteFileSize;
    }

    public  long getLocalFileSize(String path)
    {
        File f = new File(path);
        long fsize = f.length();
        if (fsize<1)fsize=1;
        return fsize;
    }

    public boolean isSuccess()
    {
        return success;
    }


    private boolean downloadSingleFile() throws IOException
    {
        //get remote file size
        final long remoteFileSize = getRemoteFileSize(origin);
        System.out.printf("%s size: %d\n\n",origin,remoteFileSize);




        //Connection
        Uri ftpUri = Uri.parse(serverUri);
        String[] userInfo=ftpUri.getUserInfo().split(":");
        String username= userInfo[0];
        String password =userInfo[1];
        String hostname =ftpUri.getHost();
        int port = ftpUri.getPort();

        ftpClient = new FTPClient();
        try {
            // connect and login to the server
            ftpClient.connect(hostname, port);
            ftpClient.login(username, password);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();
            System.out.println("Connected");

        } catch (IOException ex) {
            ex.printStackTrace();
        }



        //For count progress
        CopyStreamAdapter streamListener = new CopyStreamAdapter()
        {
            @Override
            public void bytesTransferred(long totalBytesTransferred,
                                         int bytesTransferred, long streamSize)
            {
                if (remoteFileSize>0) {
                    progressPercentage = (int) (totalBytesTransferred * 100 / remoteFileSize);
                    System.out.printf("Progress %d percent", progressPercentage);
                }

            }
        };

        // APPROACH #1: using retrieveFile(String, OutputStream)
        ftpClient.setCopyStreamListener(streamListener);
        File downloadFile = new File(saveFile);
        OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile));

        success = ftpClient.retrieveFile(origin, outputStream1);
        outputStream1.close();

        if (success) {
            System.out.println("File #1 has been downloaded successfully.");
        }
        return  success;


        // FTP Download APPROACH #2: using InputStream retrieveFileStream(String)
        /*File downloadFile = new File(saveFile);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
        InputStream inputStream = ftpClient.retrieveFileStream(origin);
        byte[] bytesArray = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(bytesArray)) != -1)   {
            outputStream.write(bytesArray, 0, bytesRead);
        }

        success = ftpClient.completePendingCommand();
        if (success) {
            System.out.println("File has been downloaded successfully.");
        }
        outputStream.close();
        inputStream.close();

        return success;*/

    }

    public void run()
    {
        try {
            active=true;
            downloadSingleFile();
            ftpClient.logout();
            ftpClient.disconnect();
            active=false;
            if (isSuccess())System.out.println("Success");


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
