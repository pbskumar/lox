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
                "Binary     : Expr left, Token operator, Expr right",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Unary      : Token operator, Expr right"
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

            writer.write("import com.craftinginterpreters.lox.token.Token;");
            writer.newLine(); writer.newLine();

            writer.write("import java.util.List;");
            writer.newLine(); writer.newLine();

            writer.write("abstract class %s {".formatted(baseName));
            writer.newLine();

            defineVisitor(writer, baseName, types);

            // Adds base accept() method
            writer.newLine();;
            writer.write("\tabstract <R> R accept(Visitor<R> visitor);");
            writer.newLine(); writer.newLine();

            // AST types
            for (final String type : types) {
                final List<String> grammar = Stream.of(type.split(":")).map(String::trim).toList();
                if (grammar.size() != 2) continue;
                // Dirty way to get the strings
                final String className = grammar.getFirst().trim();
                final String fields = grammar.getLast().trim();
                defineType(writer, baseName, className, fields);
            }

            writer.write("}");
            writer.newLine();
        }

    }

    private static void defineVisitor(final BufferedWriter writer,
                                      final String baseName,
                                      final List<String> types) throws IOException {

        writer.newLine();
        writer.write("\tinterface Visitor<R> {");
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
        writer.write("\tstatic class %s extends %s {".formatted(className, baseName));
        writer.newLine();

        final LinkedHashMap<String, String> fields = Arrays.stream(
                fieldList.split(","))
                .map(String::strip)
                .map(f -> f.split("\\s+"))
                .collect(Collectors.toMap(
                        parts -> parts[0].strip(),
                        parts -> parts[1].strip(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Fields
        for (final Map.Entry<String, String> entry: fields.sequencedEntrySet()) {
            writer.write("\t\tfinal %s %s;".formatted(entry.getKey(), entry.getValue()));
            writer.newLine();
        }
        writer.newLine();

        // Constructor
        final String constructorArgs = fields.sequencedEntrySet()
                .stream().map((entry) -> "final %s %s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        writer.write("\t\t%s(%s) {".formatted(className, constructorArgs));
        writer.newLine();

        for (final Map.Entry<String, String> entry: fields.sequencedEntrySet()) {
            writer.write("\t\t\tthis.%s = %s;".formatted(entry.getValue(), entry.getValue()));
            writer.newLine();
        }
        writer.write("\t\t}");
        writer.newLine();

        // visitor override
        writer.newLine();
        writer.write("\t\t@Override");
        writer.newLine();
        writer.write("\t\t<R> R accept(Visitor<R> visitor) {");
        writer.newLine();
        writer.write("\t\t\treturn visitor.visit%s%s(this);".formatted(className, baseName));
        writer.newLine();
        writer.write("\t\t}"); writer.newLine();

        writer.write("\t}");
        writer.newLine();
    }
}
