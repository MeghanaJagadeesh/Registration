package com.planotech.plano.controller;

import com.planotech.plano.helper.FileUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileUploaderController {

    @Autowired
    FileUploader fileUploader;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {

        String url = fileUploader.handleFileUploadAsync(file).join();
        if (url != null && !url.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Uploaded Successfully",
                            "code", 200,
                            "url", url,
                            "status", "success"
                    )
            );
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Something went wrong, Please try again later",
                    "code", 500,
                    "status", "failed"
            ));
        }
    }
}
