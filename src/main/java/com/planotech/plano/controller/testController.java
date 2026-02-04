package com.planotech.plano.controller;

import com.planotech.plano.helper.FileUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testController {

    @Autowired
    FileUploader fileUploader;

    @PostMapping("/test")
    public void test(@RequestParam String url){
        fileUploader.deleteFile(url);
    }

}
