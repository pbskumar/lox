package com.craftinginterpreters.lox.callables.classes;

import com.craftinginterpreters.lox.callables.LoxCallable;
import com.craftinginterpreters.lox.callables.functions.LoxFunction;
import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {

    final String name;
    private final Map<String, LoxFunction> methods;

    public LoxClass(final String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "<Class#%s>".formatted(name);
    }

    @Override
    public int arity() {
        final LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final LoxInstance instance = new LoxInstance(this);

        final LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    public LoxFunction findMethod(final String name) {
        return methods.getOrDefault(name, null);
    }
}
