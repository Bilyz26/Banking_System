package com.bankingsystem.transfer.domain;

import com.bankingsystem.shared.domain.DomainException;

public final class SameAccountTransferException extends DomainException {

    public SameAccountTransferException() {
        super("Source and destination accounts must be different");
    }
}

