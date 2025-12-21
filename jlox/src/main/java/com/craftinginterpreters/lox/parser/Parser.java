package com.craftinginterpreters.lox.parser;

import com.craftinginterpreters.lox.ast.Expr;
import com.craftinginterpreters.lox.common.ProblemReporter;
import com.craftinginterpreters.lox.common.token.Token;
import com.craftinginterpreters.lox.common.token.TokenType;

import java.util.List;

import static com.craftinginterpreters.lox.common.token.TokenType.*;

/**
 *  expression      -> equality ;
 *  equality        -> comparison ( ( "!=" | "==" ) comparison )* ;
 *  comparison      -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 *  term            -> factor ( ( "-" | "+" ) factor )* ;
 *  factor          -> unary ( ( "/" | "* " ) unary )* ;
 *  unary           -> ( "!" | "-" ) unary
 *                  | primary ;
 *  primary         -> NUMBER | STRING | "true" | "false" | "nil"
 *                  | "(" expression ")" ;
 */

public class Parser {

    private static class ParseError extends RuntimeException { }

    private final ProblemReporter reporter;
    private final List<Token> tokens;
    private int current = 0;

    public Parser(final List<Token> tokens, final ProblemReporter reporter) {
        this.tokens = tokens;
        this.reporter = reporter;
    }

    public Expr parse() {
        try {
            return expression();
        } catch (final ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE))   return new Expr.Literal(false);
        if (match(TRUE))    return new Expr.Literal(true);
        if (match(NIL))     return new Expr.Literal(null);

        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal());

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression. ");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(final TokenType type, final String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(final Token token, final String message) {
        reporter.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type().equals(SEMICOLON)) return;

            switch (peek().type()) {
                case CLASS:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }
            advance();
        }
    }

    private boolean match(final TokenType ... types) {
        for (final TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(final TokenType type) {
        if (isAtEnd()) return false;
        return peek().type().equals(type);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type().equals(EOF);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
