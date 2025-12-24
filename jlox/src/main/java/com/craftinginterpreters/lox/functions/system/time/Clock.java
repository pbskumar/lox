package com.craftinginterpreters.lox.functions.system.time;

import com.craftinginterpreters.lox.functions.LoxCallable;
import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public class Clock implements LoxCallable {

    public static final double MILLIS_PER_SECOND = 1000.0;

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / MILLIS_PER_SECOND;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}
