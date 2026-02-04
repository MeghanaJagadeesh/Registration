package com.planotech.plano.helper;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class FileUploader {
    private static final Logger log =
            LoggerFactory.getLogger(FileUploader.class);


    public static final int SFTP_PORT = 22;
    public static final String SFTP_USER = "dh_nw536f";
    public static final String SFTP_PASSWORD = "Srikrishna@0700";
    public static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
    public static final String SFTP_DIRECTORY = "/home/dh_nw536f/aws.quantumparadigm.in/documents/";
    public static final String BASE_URL = "https://aws.quantumparadigm.in/documents/";

//    public static final int SFTP_PORT = 22;
//    public static final String SFTP_USER = "dh_gmj3vr";
//    public static final String SFTP_PASSWORD = "Srikrishna@0700";
//    public static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
//    public static final String SFTP_DIRECTORY = "/home/dh_gmj3vr/mantramatrix.in/documents/";
//    public static final String BASE_URL = "https://mantramatrix.in/documents/";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public CompletableFuture<String> handleFileUploadAsync(MultipartFile file) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] fileBytes = file.getBytes();
                String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
                String url = uploadFileViaSFTP(fileBytes, uniqueFileName);
                log.info("File uploaded: {} → {}", file.getOriginalFilename(), url);
                return url;
            } catch (Exception e) {
                log.error("File upload failed for {}", file.getOriginalFilename(), e);
                throw new RuntimeException(e.getMessage());
            }
        }, executorService);
    }

    private String uploadFileViaSFTP(byte[] fileBytes, String fileName) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            try (InputStream fileInputStream = new ByteArrayInputStream(fileBytes)) {
                sftpChannel.put(fileInputStream, SFTP_DIRECTORY + fileName);
            }
            return BASE_URL + fileName;
        } catch (JSchException | SftpException | IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (sftpChannel != null) {
                try {
                    sftpChannel.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            if (session != null) {
                try {
                    session.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "_" + originalFilename;
    }

    public ResponseEntity<Map<String, Object>> deleteFile(String fileUrl) {
        Map<String, Object> response = new HashMap<>();
        String fileName = fileUrl.replace(BASE_URL, "");
        String remoteFilePath = SFTP_DIRECTORY + fileName;
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;
        try {
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.rm(remoteFilePath);
            response.put("code", 200);
            response.put("status", "success");
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);

        } catch (JSchException | SftpException | RuntimeException e) {
            throw new RuntimeException(e.getMessage());

        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }


    public ResponseEntity<Map<String, Object>> deleteUrl(String fileurl) {
        Map<String, Object> response = new HashMap<>();
        String fileName = fileurl.replace(BASE_URL, "");
        String remoteFilePath = SFTP_DIRECTORY + fileName;
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;
        try {
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.rm(remoteFilePath);

            response.put("code", 200);
            response.put("status", "success");
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);

        } catch (JSchException | SftpException | RuntimeException e) {
            throw new RuntimeException(e.getMessage());

        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}