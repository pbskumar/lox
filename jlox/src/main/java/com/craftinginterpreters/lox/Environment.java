package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.ast.Expr;
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

        // How do we detect null assignment vs null coming from unassigned declaration?
        // Maybe define a new type in grammar where Assignment is without a value??
        // Or, create a Set in addition to the HashMap. Unassigned declarations are stored in the Set
        // until an assignment is made. At which point, remove from set and put in HashMap.
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

    public Object getAt(final Integer hops, final Token name) {
        try {
            return ancestor(hops).values.get(name.lexeme());
        } catch (final RuntimeError error) {
            throw new RuntimeError(name, "Undefined variable '%s'.".formatted(name));
        }
    }

    public Object getAt(final Integer hops, final String name) {
        try {
            return ancestor(hops).values.get(name);
        } catch (final RuntimeError error) {
            throw new RuntimeError("Undefined variable '%s'.".formatted(name));
        }
    }

    private Environment ancestor(final Integer hops) {
        Environment env = this;
        for (int i = 0; i < hops; i++) {
            // need to add a null check
            if (env.enclosing == null) {
                throw new RuntimeError("");
            }
            env = env.enclosing;
        }
        return env;
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

    public void assignAt(final int hops, final Token name, final Object value) {
        ancestor(hops).values.put(name.lexeme(), value);
    }
}
