package org.sunbird;

import org.incredible.exeptions.BaseException;

@FunctionalInterface
public interface RequestValidatorFunction<T, R> {
    R apply(T t) throws BaseException;
}