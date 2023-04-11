package com.digdes.school.someexceptions;
/*
* Базовый класс исключений,
* возникающих при вводе некорректных данных
* в параметрах запроса*/
public class ParametersException extends Exception{
    public ParametersException(String message){
        super(message);
    }
}
