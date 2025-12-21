package com.craftinginterpreters.lox.common;

import com.craftinginterpreters.lox.common.token.Token;
import com.craftinginterpreters.lox.common.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public final class ProblemReporter {

    private final List<String> errors = new ArrayList<>();

    public void error(int line, String message) {
        report(line, "", message);
    }

    public void error(int line, String where, String message) {
        report(line, where, message);
    }

    public void error(final Token token, final String message) {
        if (token.type().equals(TokenType.EOF)) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '%s'".formatted(token.lexeme()), message);
        }
    }

    private void report(int line, String where, String message) {
        errors.add("[line " + line + "] Error" + where + ": " + message);
    }

    public List<String> errors() {
        return List.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    // Bad idea. but, that's okay
    public void printErrors() {
        errors.forEach(System.out::println);
    }

    public void clear() {
        errors.clear();
    }
}
