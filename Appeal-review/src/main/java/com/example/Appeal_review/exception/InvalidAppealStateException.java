package com.example.Appeal_review.exception;


public class InvalidAppealStateException extends RuntimeException {
    public InvalidAppealStateException(String message) {
        super(message);
    }
}