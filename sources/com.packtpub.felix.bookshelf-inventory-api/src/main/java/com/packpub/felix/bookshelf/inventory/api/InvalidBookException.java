package com.packpub.felix.bookshelf.inventory.api;

public class InvalidBookException extends Exception {

    public InvalidBookException(String message){
        super(message);
    }
}
