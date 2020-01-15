package com.example.P2P_FileSharing_System;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileDownloadController {


    /**
     * Controller or endpoint to download the file of the given name
     * server generates a random number between 2-12
     * then generates a file size of that value MB
     * calculates the hash value and send it over to the requested node.
     */
    @RequestMapping("/download/{fileName:.+}")
    public void downloadResource(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable("fileName") String fileName) throws IOException {

        Random r = new Random();
        int low = 2;
        int high = 12;
        int fileSize = r.nextInt(high - low) + low; //generate random value for the file size between 2-12 MB
        File file = createFile(fileName, fileSize * 1024 * 1024);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try (InputStream is = Files.newInputStream(Paths.get(fileName));
             DigestInputStream dis = new DigestInputStream(is, md)) {
        }
        byte[] digest = md.digest(); //calculate hash value for the file
        String hash = toHexString(digest);
        System.out.println("File Name : " + fileName);
        System.out.println("Hash value for the file : " + hash);
        System.out.println("File Size : " + fileSize + " MB");

        if (file.exists()) {
            //get the mimetype
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                //unknown mimetype so set the mimetype to application/octet-stream
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

            // Here we have mentioned it to show as attachment
            // response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + file.getName() + "\""));
            response.setContentLength((int) file.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } else {
            System.out.println("File Does not exist");
        }
    }

    /**
     * method to create a file of random size
     * return : randomly generated file
     */
    private File createFile(final String filename, final long sizeInBytes) throws IOException {
        File file = new File(filename);
        file.createNewFile();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(sizeInBytes);
        raf.close();
        return file;
    }

    /**
     * get hash value of the file as a hex value
     * return : hash value of the given byte array in hex
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
