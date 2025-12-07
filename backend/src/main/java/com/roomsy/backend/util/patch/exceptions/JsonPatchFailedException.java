package com.roomsy.backend.util.patch.exceptions;

public class JsonPatchFailedException extends RuntimeException {
    public JsonPatchFailedException(String message) {
        super(message);
    }
}
