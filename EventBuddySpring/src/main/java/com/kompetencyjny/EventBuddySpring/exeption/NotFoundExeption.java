package com.kompetencyjny.EventBuddySpring.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundExeption extends RuntimeException{
    public NotFoundExeption(String message){
        super(message);
    }
}
