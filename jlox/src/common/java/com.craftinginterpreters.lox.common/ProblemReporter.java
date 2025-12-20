package com.craftinginterpreters.lox.common;

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
