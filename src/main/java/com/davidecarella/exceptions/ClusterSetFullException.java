package com.davidecarella.exceptions;

public class ClusterSetFullException extends RuntimeException {
    public ClusterSetFullException(String message) {
        super(message);
    }
}
