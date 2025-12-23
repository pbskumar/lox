package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.common.errors.RuntimeError;
import com.craftinginterpreters.lox.common.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    // Inner scope by name w/ nesting or enum w/ flat structure?
    private final Map<String, Object> values = new HashMap<>();

    public void define(final String name, final Object value) {
        // Should we or should we not allow double declaration?
        // Book says REPL should allow it.
        // Rust allows shadowing... this is exactly that.
        // Maybe do a type check? IDK, let's see where this goes.
        values.put(name, value);
    }

    // Why is this a Token and not String like in define()?
    public Object get(final Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        throw new RuntimeError(name, "Undefined variable '%s'.".formatted(name.lexeme()));
    }
}
