package com.roomsy.backend.dto;

/**
 * Defines Jackson JsonView levels for controlling JSON serialization.
 * Each view determines which fields are included in the response.
 */
public final class Views {
    private Views() {} 

    public interface Basic {}
    public interface Summary extends Basic {}
    public interface Detailed extends Summary {}
}