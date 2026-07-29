package com.bankingsystem.ledger.presentation;

import com.bankingsystem.ledger.application.LedgerPagePosition;
import com.bankingsystem.ledger.domain.LedgerEntryId;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.UUID;

final class TransactionCursorCodec {

    private static final int MAXIMUM_ENCODED_LENGTH = 256;

    private TransactionCursorCodec() {
    }

    static String encode(LedgerPagePosition position) {
        if (position == null) {
            return null;
        }
        String value = position.occurredAt() + "|" + position.entryId().value();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    static LedgerPagePosition decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        if (cursor.length() > MAXIMUM_ENCODED_LENGTH) {
            throw malformedCursor();
        }
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8);
            String[] fields = decoded.split("\\|", -1);
            if (fields.length != 2) {
                throw malformedCursor();
            }
            return new LedgerPagePosition(
                    Instant.parse(fields[0]),
                    new LedgerEntryId(UUID.fromString(fields[1])));
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            throw malformedCursor();
        }
    }

    private static IllegalArgumentException malformedCursor() {
        return new IllegalArgumentException("transaction cursor is malformed");
    }
}
