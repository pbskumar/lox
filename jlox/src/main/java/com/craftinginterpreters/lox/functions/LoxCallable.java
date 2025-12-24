package com.craftinginterpreters.lox.functions;

import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public interface LoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
