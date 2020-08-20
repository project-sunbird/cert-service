package org.incredible.exeptions;

import org.incredible.message.IResponseMessage;
import org.incredible.message.Localizer;


public class InvalidDateFormatException extends BaseException {

    private static Localizer localizer = Localizer.getInstance();

    public InvalidDateFormatException(String msg) {
        super(
                IResponseMessage.INVALID_REQUESTED_DATA,
                msg,
                400);
    }
}
