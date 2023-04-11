package com.digdes.school.someexceptions;
/*
 * Класс исключения,возникающего при вводе
 * некорректных данных в запросе */
public class InvalidRequestParametersException extends ParametersException{
    public InvalidRequestParametersException(String message) {
        super(message);
    }
}
