package com.planotech.plano.exception;

import org.springframework.http.HttpStatus;

public class CustomJwtException extends  RuntimeException{

    public CustomJwtException(String message){
        super(message);
    }
}
