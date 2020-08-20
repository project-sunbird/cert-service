package org.sunbird;

import org.incredible.exceptions.BaseException;

@FunctionalInterface
public interface RequestValidatorFunction<T, R> {
    R apply(T t) throws BaseException;
}