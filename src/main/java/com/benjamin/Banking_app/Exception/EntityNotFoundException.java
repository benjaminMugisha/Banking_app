package com.benjamin.Banking_app.Exception;

public class EntityNotFoundException extends RuntimeException{ // so java knows it's an exception class

    public EntityNotFoundException(String message){
        super(message);
    }
    public EntityNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
