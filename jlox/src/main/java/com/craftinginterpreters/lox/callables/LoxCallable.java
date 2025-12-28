package com.craftinginterpreters.lox.callables;

import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public interface LoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
