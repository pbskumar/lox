package com.craftinginterpreters.lox.token;

// TODO: Add column number
public record Token (
        TokenType type,
        // Text from source identified as part of the Token
        String lexeme,
        // Value of the token
        Object literal,
        int line
) {
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
