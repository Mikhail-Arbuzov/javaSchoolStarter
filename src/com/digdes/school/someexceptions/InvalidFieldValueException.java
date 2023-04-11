package com.digdes.school.someexceptions;
/*
 * Класс исключения,
 * возникающего при вводе некорректных данных
 * в значения полей (колонок) */
public class InvalidFieldValueException extends ParametersException{
    public InvalidFieldValueException(String message) {
        super(message);
    }
}
