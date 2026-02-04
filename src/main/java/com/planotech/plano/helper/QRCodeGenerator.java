package com.planotech.plano.helper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
public class QRCodeGenerator {

    public BufferedImage generateQrCodeImage(String content) {

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (WriterException ex) {
            throw new IllegalStateException("Failed to generate QR Code", ex);
        }
    }
}
