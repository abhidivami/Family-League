package com.familyleague.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a prediction or action is attempted after the lock window has passed. */
public class PredictionLockedException extends AppException {

    public PredictionLockedException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
