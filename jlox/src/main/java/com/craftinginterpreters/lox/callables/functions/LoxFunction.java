package com.craftinginterpreters.lox.callables.functions;

import com.craftinginterpreters.lox.Environment;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.callables.LoxCallable;
import com.craftinginterpreters.lox.callables.classes.LoxInstance;
import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;

    private final Boolean isInitializer;

    public LoxFunction(final Stmt.Function declaration, final Environment closure) {
        this(declaration, closure, false);
    }

    public LoxFunction(final Stmt.Function declaration, final Environment closure, final Boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < arity(); i++) {
            environment.define(declaration.params.get(i).lexeme(), arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (final Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "this");

        // If there's no explicit return, then return `nil`
        return null;
    }

    @Override
    public String toString() {
        return "<fn %s>".formatted(declaration.name.lexeme());
    }

    public LoxFunction bind(final LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }
}

