package com.wappkup;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;




public class threadsTransfers extends Thread
{
    public boolean active=false;
    private enum fileType {DIR,PNG,PDF,JPG,TXT,APK};
    public static String origin, saveFile;
    public boolean OpenAfterDownload=false;
    public enum type {DOWNLOAD,UPLOAD};
    private FTPClient ftpClient;
    public int progressPercentage=0;
    private boolean abort=false;


    public threadsTransfers(FTPClient ftpClient,String origin, String saveFile)
    {
        this.origin=origin;
        this.saveFile=saveFile;
        this.ftpClient=ftpClient;
    }

    public void abortNow()
    {
        this.abort=true;
        //non finita
    }

    public long getRemoteFileSize() throws IOException //problemi
    {
        FTPFile[] files = this.ftpClient.listFiles();
        for (FTPFile file : files) {
            String name= file.getName();
            if (name.equals(FilenameUtils.getName(this.origin)))
            {
                return file.getSize();
            }else{
                return -1;
            }
        }
        return -1;
    }

    public long getLocalFileSize()
    {
        File f = new File(saveFile);
        long fsize = f.length();
        if (fsize<1)fsize=1;
        return fsize;
    }



    public  boolean downloadSingleFile() throws IOException //it was static
    {
        //final long remoteFileSize=this.getRemoteFileSize();
        final long localFileSize=this.getLocalFileSize();

        final File downloadFile = new File(saveFile);

        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }

        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(downloadFile));
        try {
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            CopyStreamAdapter streamListener = new CopyStreamAdapter() {
                @Override
                public void bytesTransferred(long totalBytesTransferred,
                                             int bytesTransferred, long streamSize) {
                    progressPercentage = (int)(totalBytesTransferred*100/localFileSize);

                    System.out.printf("Scaricamento--> %d\n",progressPercentage);
                }

            };
            ftpClient.setCopyStreamListener(streamListener);


            return this.ftpClient.retrieveFile(origin, outputStream);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public void run()
    {
        try {
            downloadSingleFile();
            this.progressPercentage=100;
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
