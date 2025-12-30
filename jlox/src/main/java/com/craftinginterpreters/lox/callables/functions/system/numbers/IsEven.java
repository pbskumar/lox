package com.craftinginterpreters.lox.callables.functions.system.numbers;

import com.craftinginterpreters.lox.callables.LoxCallable;
import com.craftinginterpreters.lox.common.errors.RuntimeError;
import com.craftinginterpreters.lox.visitors.Interpreter;

import java.util.List;

public class IsEven implements LoxCallable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        if (arguments.size() != 1) {
            throw new RuntimeError("isEven(number) expects 1 number argument. Received " + arguments.size());
        }

        if (arguments.getFirst() instanceof Number number) {
            return number.doubleValue() % 2 == 0;
        }

        throw  new RuntimeError("isEven(number) expects a number argument. Received " + arguments.getFirst());
    }

    @Override
    public String toString() {
        return "<native fn#isEven(number)>";
    }
}
