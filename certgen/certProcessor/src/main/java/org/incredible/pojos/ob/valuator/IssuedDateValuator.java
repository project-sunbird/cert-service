package org.incredible.pojos.ob.valuator;

import org.incredible.exeptions.InvalidDateFormatException;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class IssuedDateValuator implements IEvaluator {

    private List<SimpleDateFormat> dateFormats = Arrays.asList(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            , new SimpleDateFormat("yyyy-MM-dd"));

    @Override
    public String evaluates(Object inputVal) throws InvalidDateFormatException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar cal = Calendar.getInstance();
        Date date = convertToDate((String) inputVal);
        cal.setTime(date);
        return simpleDateFormat.format(cal.getTime());

    }


    public Date convertToDate(String input) throws InvalidDateFormatException {
        Date date = null;
        for (SimpleDateFormat format : dateFormats) {
            try {
                format.setLenient(false);
                date = format.parse(input);
            } catch (ParseException e) {
            }
            if (date != null) {
                break;
            }
        }
        if (date == null) {
            throw new InvalidDateFormatException("issued date " + input + " is not in valid format");
        }
        return date;
    }

}
