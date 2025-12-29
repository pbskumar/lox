package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.Environment;
import com.craftinginterpreters.lox.ast.Expr;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.callables.classes.LoxClass;
import com.craftinginterpreters.lox.callables.classes.LoxInstance;
import com.craftinginterpreters.lox.common.ProblemReporter;
import com.craftinginterpreters.lox.common.errors.RuntimeError;
import com.craftinginterpreters.lox.common.token.Token;
import com.craftinginterpreters.lox.common.token.TokenType;
import com.craftinginterpreters.lox.callables.LoxCallable;
import com.craftinginterpreters.lox.callables.functions.LoxFunction;
import com.craftinginterpreters.lox.callables.functions.Return;
import com.craftinginterpreters.lox.callables.functions.system.time.Clock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    public final Environment globals = new Environment();

    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter() {
        globals.define("clock", new Clock());
    }

    public void interpret(final List<Stmt> statements, final ProblemReporter reporter) {
        try {
            for (final Stmt statement : statements) {
                execute(statement);
            }
        } catch (final RuntimeError error) {
            reporter.runtimeError(error);
        }
    }

    public void resolve(final Expr expr, int depth) {
        locals.put(expr, depth);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme(), null);
        final Map<String , LoxFunction> methods = new HashMap<>();

        for (final Stmt.Function method : stmt.methods) {
            final LoxFunction function = new LoxFunction(method, environment, method.name.lexeme().equals("init"));
            methods.put(method.name.lexeme(), function);
        }

        final LoxClass klass = new LoxClass(stmt.name.lexeme(), methods);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        // Registration and Scoping for inner and outer functions
        environment.define(stmt.name.lexeme(), function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        final Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;

        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme(), value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        final Integer hops = locals.get(expr);
        if (hops != null) {
            environment.assignAt(hops, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case PLUS ->
            {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String) left).concat((String) right);
                }

                // Allows "scone" + 4 = "scone4"
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                final Double result = (double) left / (double) right;
                if (result.isNaN()) throw new RuntimeError(expr.operator, "0/0 is not not allowed.");
                return result;
            }
            case STAR ->
            {
                // Here is where python does weird stuff :P
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        final List<Object> args = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            args.add(evaluate(arg));
        }

        if (!(callee instanceof LoxCallable callable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }

        if (args.size() != callable.arity()) {
            throw new RuntimeError(
                    expr.paren,
                    "Expected %d arguments but got %d.".formatted(callable.arity(), args.size())
            );
        }

        return callable.call(this, args);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);

        if (object instanceof LoxInstance instance) {
            return instance.get(expr.name);
        }
        return null;
    }

    private void checkNumberOperands(final Token operator, final Object left, final Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        // Short-circuiting
        if (expr.operator.type().equals(TokenType.OR)) {
            if (isTruthy(left)) return left;
        } else {
            // We could write ELSE IF to check for AND, but it is pointless yet as we only have 'AND' and 'OR'
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case BANG -> {
                return !isTruthy(right);
            }
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return - (double) right;
            }
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(final Token name, final Expr expr) {
        final Integer hops = locals.get(expr);

        if (hops != null) {
            return environment.getAt(hops, name);
        } else {
            return globals.get(name);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(final Stmt statement) {
        statement.accept(this);
    }

    public void executeBlock(final List<Stmt> statements, final Environment environment) {
        final Environment previousEnv = this.environment;
        try {
            this.environment = environment;

            for (final Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previousEnv;
        }
    }

    private void checkNumberOperand(final Token operator, final Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isTruthy(Object object) {
        return switch (object) {
            case null -> false;
            case Boolean b -> b;
            case String s when s.isBlank() -> false;
            case Double d when d.equals(0.0) -> false;
            default -> true;
        };
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(final Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            final String text = object.toString();
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
