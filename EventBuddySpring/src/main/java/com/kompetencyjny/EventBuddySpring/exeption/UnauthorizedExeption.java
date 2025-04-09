package com.kompetencyjny.EventBuddySpring.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedExeption extends RuntimeException{
    public UnauthorizedExeption(String message){
        super(message);
    }
}
