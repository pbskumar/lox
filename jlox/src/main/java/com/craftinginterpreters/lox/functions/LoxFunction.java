package com.craftinginterpreters.lox.functions;

import com.craftinginterpreters.lox.Environment;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;

    public LoxFunction(final Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);

        for (int i = 0; i < arity(); i++) {
            environment.define(declaration.params.get(i).lexeme(), arguments.get(i));
        }

        interpreter.executeBlock(declaration.body, environment);

        // No return values for now (as in every call is returning nill for now by default)
        return null;
    }

    @Override
    public String toString() {
        return "<fn %s>".formatted(declaration.name.lexeme());
    }
}

