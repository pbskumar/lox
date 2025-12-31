package com.craftinginterpreters.lox.parser;

import com.craftinginterpreters.lox.ast.Expr;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.common.ProblemReporter;
import com.craftinginterpreters.lox.common.token.Token;
import com.craftinginterpreters.lox.common.token.TokenType;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.common.token.TokenType.*;

public class Parser {

    public static final int FN_MAX_ARG_COUNT = 255;

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
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(BREAK)) return breakStatement();
        if (match(CONTINUE)) return continueStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt continueStatement() {
        Token keyword = previous();
        consume(SEMICOLON, "Expect ';' after continue statement");
        return new Stmt.Continue(keyword);
    }

    private Stmt breakStatement() {
        Token keyword = previous();
        consume(SEMICOLON, "Expect ';' after break statement");
        return new Stmt.Break(keyword);
    }

    private Stmt returnStatement() {
        Token keyword = previous();

        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value");
        return new Stmt.Return(keyword, value);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ') after for clauses.");

        Stmt body = statement();

        // Execute increment after executing body once.
        if (increment != null) {
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)
                    )
            );
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        // Implementing `for` as `while`
        if (initializer != null) {
            body = new Stmt.Block(
                    Arrays.asList(
                            initializer,
                            body
            ));
        }

        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");

        Stmt body = statement();

        return new Stmt.While(condition, body);

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

        if (match(EQUAL, MINUS_EQUAL, PLUS_EQUAL, STAR_EQUAL, SLASH_EQUAL)) {
            Token operator = previous();
            Expr value = switch (operator.type()) {
                case MINUS_EQUAL -> new Expr.Binary(expr, operator.withType(MINUS), assignment());
                case PLUS_EQUAL -> new Expr.Binary(expr, operator.withType(PLUS), assignment());
                case STAR_EQUAL -> new Expr.Binary(expr, operator.withType(STAR), assignment());
                case SLASH_EQUAL -> new  Expr.Binary(expr, operator.withType(SLASH), assignment());
                case EQUAL -> assignment();
                // Realistically this is not possible unless we change the top level match condition
                default -> throw new IllegalStateException("Unexpected value: " + operator.type());
            };

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get getterExpr) {
                return new Expr.Set(getterExpr.object, getterExpr.name, value);
            }

            error(operator, "Invalid assignment target");
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
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (final ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        final Token name = consume(IDENTIFIER, "Expect class name.");

        Expr.Variable superClass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superClass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expect '{' before class body.");

        final List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body");

        return new Stmt.Class(name, superClass, methods);
    }

    private Stmt expressionStatement() {
        final Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect %s name.".formatted(kind));

        consume(LEFT_PAREN, "Expect '(' after %s name.".formatted(kind));
        List<Token> params = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (params.size() >= FN_MAX_ARG_COUNT) {
                    error(peek(), "Cant' have more than %d parameters.".formatted(FN_MAX_ARG_COUNT));
                }
                params.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before %s body.".formatted(kind));

        List<Stmt> body = block();

        return new Stmt.Function(name, params, body);
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

        while (match(SLASH, STAR, MODULUS)) {
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
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        final List<Expr> args = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (args.size() >= FN_MAX_ARG_COUNT) {
                    error(peek(), "Can't have more than %d arguments".formatted(FN_MAX_ARG_COUNT));
                }
                args.add(expression());
            } while (match(COMMA));
        }

        final Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, args);
    }

    private Expr primary() {
        if (match(FALSE))   return new Expr.Literal(false);
        if (match(TRUE))    return new Expr.Literal(true);
        if (match(NIL))     return new Expr.Literal(null);

        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal());

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super'.");
            Token method = consume(IDENTIFIER, "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(THIS)) return new Expr.This(previous());

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
