package com.craftinginterpreters.lox.common.token;

public enum TokenType {

    // Single character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    PLUS_PLUS, MINUS_PLUS, PLUS_EQUAL, MINUS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, BREAK, CATCH, CLASS, CONTINUE, DO, ELSE, FALSE, FUN, FOR, IF, IN, LET, NEW,
    NIL, NULL, OR, PRINT, RETURN, SUPER, THIS, TRUE, TRY, VAL, VAR, WHILE,

    COMMENT,
    EOF
}
