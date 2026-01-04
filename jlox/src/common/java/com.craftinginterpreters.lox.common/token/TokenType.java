package com.craftinginterpreters.lox.common.token;

import java.util.Map;
import java.util.Set;

public enum TokenType {

    // Single character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, MODULUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // Compound operators
    // C-style
    MINUS_MINUS, PLUS_PLUS,
    // Python-style augmented assignment
    MINUS_EQUAL, PLUS_EQUAL, SLASH_EQUAL, STAR_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, BREAK, CATCH, CLASS, CONTINUE, DO, ELSE, FALSE, FUN, FOR, IF, IN, LET, NEW,
    NIL, NULL, OR, PRINT, RETURN, SUPER, THIS, TRUE, TRY, VAL, VAR, WHILE,

    COMMENT,
    EOF;

    public static final Map<TokenType, TokenType> OPEN_TO_CLOSE_TOKEN_TYPE_PAIR = Map.of(
            LEFT_PAREN, RIGHT_PAREN,
            LEFT_BRACE, RIGHT_BRACE
    );

    public static final Set<TokenType> CONTINUATION_TOKEN_TYPES = Set.of(LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE);
}
