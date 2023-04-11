package com.digdes.school.someexceptions;
/*
 * Класс исключения,
 * возникающего при вводе неверного именования полей (колонок) */
public class InvalidNameFieldException extends ParametersException{
    public InvalidNameFieldException(String message) {
        super(message);
    }
}
