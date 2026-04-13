package com.nexus.onebook.exception;

/**
 * Thrown when a journal transaction's total debits do not equal
 * total credits, violating the fundamental double-entry principle.
 */
public class UnbalancedTransactionException extends RuntimeException {

    public UnbalancedTransactionException(String message) {
        super(message);
    }
}
