package ru.manakin.aucmonitor.controller.exceptions;

public class NotFoundUserException extends RuntimeException {

    public NotFoundUserException(
            String message
    ) {
        super(message);
    }
}
