package com.craftinginterpreters.lox.callables.classes;

import com.craftinginterpreters.lox.callables.LoxCallable;
import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public class LoxClass implements LoxCallable {

    final String name;

    public LoxClass(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "<Class#%s>".formatted(name);
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final LoxInstance instance = new LoxInstance(this);
        return instance;
    }
}
