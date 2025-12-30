package com.craftinginterpreters.lox.flows;

public class Continue extends RuntimeException {

    public Continue() {
        super(null, null, false, false);
    }
}
