package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.Environment;
import com.craftinginterpreters.lox.ast.Expr;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.common.ProblemReporter;
import com.craftinginterpreters.lox.common.errors.RuntimeError;
import com.craftinginterpreters.lox.common.token.Token;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    public void interpret(final List<Stmt> statements, final ProblemReporter reporter) {
        try {
            for (final Stmt statement : statements) {
                execute(statement);
            }
        } catch (final RuntimeError error) {
            reporter.runtimeError(error);
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        final Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
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
        return environment.get(expr.name);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(final Stmt statement) {
        statement.accept(this);
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
