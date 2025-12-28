package com.craftinginterpreters.lox.callables.classes;

public class LoxClass {

    final String name;

    public LoxClass(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
