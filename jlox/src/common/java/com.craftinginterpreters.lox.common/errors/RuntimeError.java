package com.craftinginterpreters.lox.common.errors;

import com.craftinginterpreters.lox.common.token.Token;

public class RuntimeError extends RuntimeException {

    final Token token;

    public RuntimeError(final Token token, final String message) {
        super(message);
        this.token = token;
    }
}
