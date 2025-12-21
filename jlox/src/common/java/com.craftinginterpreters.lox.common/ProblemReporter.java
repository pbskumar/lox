package com.craftinginterpreters.lox.common;

import com.craftinginterpreters.lox.common.errors.RuntimeError;
import com.craftinginterpreters.lox.common.token.Token;
import com.craftinginterpreters.lox.common.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public final class ProblemReporter {

    private final List<String> errors = new ArrayList<>();
    private final List<RuntimeError> runtimeErrors = new ArrayList<>();

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

    public void runtimeError(final RuntimeError error) {
        this.runtimeErrors.add(error);
    }

    private void report(int line, String where, String message) {
        errors.add("[line " + line + "] Error" + where + ": " + message);
    }

    public List<String> errors() {
        return List.copyOf(errors);
    }

    public List<RuntimeError> runtimeErrors() {
        return List.copyOf(runtimeErrors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasRuntimeErrors() {
        return !runtimeErrors.isEmpty();
    }

    // Bad idea. but, that's okay
    public void printErrors() {
        errors.forEach(System.out::println);
    }

    public void printRuntimeErrors() {
        runtimeErrors.forEach(e -> System.out.printf("RuntimeError: %s%n", e.getMessage()));
    }

    public void clear() {
        errors.clear();
        runtimeErrors.clear();
    }
}
