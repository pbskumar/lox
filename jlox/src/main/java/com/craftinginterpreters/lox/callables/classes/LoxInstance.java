package com.craftinginterpreters.lox.callables.classes;

public class LoxInstance {

    private LoxClass klass;

    public LoxInstance(final LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return "<Instance#%s>".formatted(klass.toString());
    }
}
