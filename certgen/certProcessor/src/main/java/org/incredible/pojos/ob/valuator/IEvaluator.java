package org.incredible.pojos.ob.valuator;

import org.incredible.exceptions.InvalidDateFormatException;

public interface IEvaluator {

    String evaluates(Object inputVal) throws InvalidDateFormatException;
}
