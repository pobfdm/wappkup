package com.wappkup;

import android.net.Uri;

import org.apache.commons.io.FilenameUtils;
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
    public String serverUri,origin, saveFile;
    public boolean OpenAfterDownload=false;
    public enum type {DOWNLOAD,UPLOAD};
    public int progressPercentage=0;
    public FTPClient ftpClient;
    public static int countNotification;
    public int idNotification=0;


    private enum fileType {DIR,PNG,PDF,JPG,TXT,APK};
    private boolean abort=false;
    private boolean success=false;
    private OutputStream outputStream;





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
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        long fileSize=0;
        String currDir= FilenameUtils.getFullPath(origin);

        FTPClient ftpClientM = new FTPClient();
        try {
            // connect and login to the server
            ftpClientM.connect(hostname, port);
            ftpClientM.login(username, password);
            ftpClientM.setFileType(FTP.BINARY_FILE_TYPE);

            // use local passive mode to pass firewall
            ftpClientM.enterLocalPassiveMode();
            FTPFile[] files = ftpClientM.listFiles(currDir);

            for (final FTPFile file : files)
            {
                if (FilenameUtils.getName(file.getName()).equals(FilenameUtils.getName(origin)))
                {
                    fileSize=file.getSize();
                }
            }

            ftpClientM.logout();
            ftpClientM.disconnect();
            return fileSize;

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

        return fileSize;
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
        outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));

        success = ftpClient.retrieveFile(origin, outputStream);
        outputStream.close();

        if (success) {
            System.out.println("File #1 has been downloaded successfully.");
        }
        return  success;

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
