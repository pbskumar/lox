package com.craftinginterpreters.lox.callables.classes;

import com.craftinginterpreters.lox.callables.functions.LoxFunction;
import com.craftinginterpreters.lox.common.errors.RuntimeError;
import com.craftinginterpreters.lox.common.token.Token;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {

    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(final LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return "<Instance#%s>".formatted(klass.toString());
    }

    public Object get(final Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        final LoxFunction method = klass.findMethod(name.lexeme());
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '%s'.".formatted(name.lexeme()));
    }

    public void set(final Token name, final Object value) {
        fields.put(name.lexeme(), value);
    }
}
