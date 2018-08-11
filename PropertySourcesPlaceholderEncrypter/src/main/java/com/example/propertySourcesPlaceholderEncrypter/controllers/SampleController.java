package com.example.propertySourcesPlaceholderEncrypter.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
    @Value("${app.someTestEncString}")
    private String decryptedText;

    @GetMapping("/")
    public String decrypt(){
        return decryptedText;
    }
}
