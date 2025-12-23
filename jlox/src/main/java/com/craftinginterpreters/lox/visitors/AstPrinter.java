package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.ast.Expr;

public class AstPrinter implements Expr.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme(), expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "Var(%s)".formatted(expr.name.lexeme());
    }

    private String parenthesize(final String name, final Expr... exprs) {
        final StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (final Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
}
