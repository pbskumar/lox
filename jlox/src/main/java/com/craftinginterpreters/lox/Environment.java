package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.common.errors.RuntimeError;
import com.craftinginterpreters.lox.common.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(final Environment environment) {
        this.enclosing = environment;
    }

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

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '%s'.".formatted(name.lexeme()));
    }

    public void assign(final Token name, final Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '%s'.".formatted(name.lexeme()));
    }
}
