package com.premiumorcracked.sync;

public final class SyncProtocolException extends Exception {
    public SyncProtocolException(String message) {
        super(message);
    }

    public SyncProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
