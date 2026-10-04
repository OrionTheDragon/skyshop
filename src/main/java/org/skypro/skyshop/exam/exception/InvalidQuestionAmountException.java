package org.skypro.skyshop.exam.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidQuestionAmountException extends RuntimeException {
    public InvalidQuestionAmountException(int amount, int available) {
        super("Количество вопросов должно быть от 0 до " + available + ", запрошено: " + amount);
    }
}
