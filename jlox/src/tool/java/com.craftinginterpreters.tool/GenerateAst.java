package com.craftinginterpreters.tool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        final String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign     : Token name, Expr value",
                "Binary     : Expr left, Token operator, Expr right",
                "Call       : Expr callee, Token paren, List<Expr> arguments",
                "Get        : Expr object, Token name",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Logical    : Expr left, Token operator, Expr right",
                "Set        : Expr object, Token name, Expr value",
                "Super      : Token keyword, Token method",
                "This       : Token keyword",
                "Unary      : Token operator, Expr right",
                "Variable   : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block          : List<Stmt> statements",
                "Class          : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
                "Expression     : Expr expression",
                "Function       : Token name, List<Token> params, List<Stmt> body",
                "If             : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print          : Expr expression",
                "Return         : Token keyword, Expr value",
                "While          : Expr condition, Stmt body",
                "Var            : Token name, Expr initializer"
        ));
    }

    private static void defineAst(final String outputDir,
                                  final String baseName,
                                  final List<String> types) throws IOException {

        Path dir = Paths.get(outputDir, "com", "craftinginterpreters", "lox", "ast");
        Files.createDirectories(dir); // idempotent

        Path file = dir.resolve(baseName + ".java");

        try (final BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write("package com.craftinginterpreters.lox.ast;");
            writer.newLine(); writer.newLine();

            writer.write("import com.craftinginterpreters.lox.common.token.Token;");
            writer.newLine(); writer.newLine();

            writer.write("import java.util.List;");
            writer.newLine(); writer.newLine();

            writer.write("public abstract class %s {".formatted(baseName));
            writer.newLine();

            defineVisitorInterface(writer, baseName, types);

            // Adds base accept() method
            writer.newLine();
            writer.write("\tpublic abstract <R> R accept(Visitor<R> visitor);");
            writer.newLine(); writer.newLine();

            // AST types
            for (final String type : types) {
                final List<String> grammar = Stream.of(type.split(":")).map(String::trim).toList();
                // Dirty way to get the strings
                final String className = grammar.getFirst().trim();
                final String fields = grammar.getLast().trim();
                defineType(writer, baseName, className, fields);
            }

            writer.write("}");
            writer.newLine();
        }

    }

    private static void defineVisitorInterface(final BufferedWriter writer,
                                               final String baseName,
                                               final List<String> types) throws IOException {

        writer.newLine();
        writer.write("\tpublic interface Visitor<R> {");
        writer.newLine();

        for (final String type: types) {
            writer.newLine();
            final String typeName = type.split(":")[0].trim();
            writer.write("\t\tR visit%s%s(%s %s);".formatted(
                    typeName, baseName, typeName, baseName.toLowerCase(Locale.ROOT)));
            writer.newLine();
        }

        writer.write("\t}");
        writer.newLine();
    }

    private static void defineType(final BufferedWriter writer,
                                   final String baseName,
                                   final String className,
                                   final String fieldList) throws IOException {

        writer.newLine();
        writer.write("\tpublic static class %s extends %s {".formatted(className, baseName));
        writer.newLine();

        final List<Map.Entry<String, String>> fields = Arrays.stream(
                fieldList.split(","))
                .map(String::strip)
                .map(f -> {
                    String[] parts = f.split("\\s+");
                    return Map.entry(parts[0], parts[1]);
                })
                .toList();

        // Fields
        for (final Map.Entry<String, String> entry: fields) {
            writer.write("\t\tpublic final %s %s;".formatted(entry.getKey(), entry.getValue()));
            writer.newLine();
        }
        writer.newLine();

        // Constructor
        final String constructorArgs = fields
                .stream().map((entry) -> "final %s %s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        writer.write("\t\tpublic %s(%s) {".formatted(className, constructorArgs));
        writer.newLine();

        for (final Map.Entry<String, String> entry: fields) {
            writer.write("\t\t\tthis.%s = %s;".formatted(entry.getValue(), entry.getValue()));
            writer.newLine();
        }
        writer.write("\t\t}");
        writer.newLine();

        // visitor override
        writer.newLine();
        writer.write("\t\t@Override");
        writer.newLine();
        writer.write("\t\tpublic <R> R accept(Visitor<R> visitor) {");
        writer.newLine();
        writer.write("\t\t\treturn visitor.visit%s%s(this);".formatted(className, baseName));
        writer.newLine();
        writer.write("\t\t}"); writer.newLine();

        writer.write("\t}");
        writer.newLine();
    }
}
