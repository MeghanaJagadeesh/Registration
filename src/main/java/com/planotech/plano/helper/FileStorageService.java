package com.planotech.plano.helper;

import com.jcraft.jsch.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileStorageService {

        public static final int SFTP_PORT = 22;
    public static final String SFTP_USER = "dh_nw536f";
    public static final String SFTP_PASSWORD = "Srikrishna@0700";
    public static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
    public static final String SFTP_DIRECTORY = "/home/dh_nw536f/aws.quantumparadigm.in/documents/";
    public static final String BASE_URL = "https://aws.quantumparadigm.in/documents/";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Async("backgroundExecutor")
    public CompletableFuture<String> handleFileUploadAsync(
            BufferedImage qrImage,
            String eventName,
            String eventKey) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] fileBytes = convertImageToBytes(qrImage);

                String uniqueFileName = generateUniqueFileName();

                return uploadFileViaSFTP(
                        fileBytes,
                        uniqueFileName,
                        eventName,
                        eventKey
                );

            } catch (Exception e) {
                throw new RuntimeException("QR upload failed", e);
            }
        }, executorService);
    }

    private String uploadFileViaSFTP(
            byte[] fileBytes,
            String fileName,
            String eventName,
            String eventKey) {

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

            // 1️⃣ Build safe event folder
            String eventFolder =
                    eventName.replaceAll("[^a-zA-Z0-9-_]", "_") + "_"
                            + eventKey.replaceAll("[^a-zA-Z0-9-_]", "_");

            // 2️⃣ Move to base directory
            sftpChannel.cd(SFTP_DIRECTORY);

            // 3️⃣ Create folder if not exists
            if (!directoryExists(sftpChannel, eventFolder)) {
                sftpChannel.mkdir(eventFolder);
            }

            // 4️⃣ Move inside event folder
            sftpChannel.cd(eventFolder);

            // 5️⃣ Upload file (ONLY filename here)
            try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                sftpChannel.put(inputStream, fileName);
            }

            // 6️⃣ Return public URL
            System.out.println(BASE_URL + "/" + eventFolder + "/" + fileName);
            return BASE_URL + "/" + eventFolder + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("SFTP upload failed", e);
        } finally {

            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private byte[] convertImageToBytes(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert image", e);
        }
    }

    private String generateUniqueFileName() {
        return "qr_" +
                UUID.randomUUID()
                        .toString()
                        .replaceAll("[^a-zA-Z0-9]", "")
                        .substring(0, 8)
                + ".png";
    }

    private boolean directoryExists(ChannelSftp sftpChannel, String directoryName) {
        try {
            SftpATTRS attrs = sftpChannel.stat(directoryName);
            return attrs.isDir();
        } catch (SftpException e) {
            return false;
        }
    }
}
