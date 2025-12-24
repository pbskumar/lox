package com.craftinginterpreters.lox.functions;

import com.craftinginterpreters.lox.Environment;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;

    public LoxFunction(final Stmt.Function declaration, final Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
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
            return returnValue.value;
        }

        // If there's no explicit return, then return `nil`
        return null;
    }

    @Override
    public String toString() {
        return "<fn %s>".formatted(declaration.name.lexeme());
    }
}

