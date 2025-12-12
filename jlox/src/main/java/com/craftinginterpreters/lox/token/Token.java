package com.craftinginterpreters.lox.token;

// TODO: Add column number
public record Token (
        TokenType type,
        String lexeme,
        Object literal,
        int line
) {
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
