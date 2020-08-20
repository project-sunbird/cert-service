package org.incredible.exceptions;

import org.incredible.message.IResponseMessage;


public class InvalidDateFormatException extends BaseException {

    public InvalidDateFormatException(String msg) {
        super(
                IResponseMessage.INVALID_REQUESTED_DATA,
                msg,
                400);
    }
}
