package com.craftinginterpreters.lox.parser;

import com.craftinginterpreters.lox.ast.Expr;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.common.ProblemReporter;
import com.craftinginterpreters.lox.common.token.Token;
import com.craftinginterpreters.lox.common.token.TokenType;

import java.util.ArrayList;
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

    public List<Stmt> parse() {
        final List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt statement() {
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    // Else is tagged to the closest if statement due to greedy parsing
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        final Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        final Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token op = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, op, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token op = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, op, right);
        }
        return expr;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (final ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt expressionStatement() {
        final Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new Stmt.Expression(expr);
    }

    private Stmt printStatement() {
        final Expr value = expression();
        consume(SEMICOLON, "Expected ';' after value in print statement.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        final Token name = consume(IDENTIFIER, "Expect variable name.");

        // Kotlin excels in places like these!
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
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

        if (match(IDENTIFIER)) return new Expr.Variable(previous());

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression. ");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected an expression. Found %s".formatted(peek().type()));
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
