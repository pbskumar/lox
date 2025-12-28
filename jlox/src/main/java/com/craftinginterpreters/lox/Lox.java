package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.ast.Expr;
import com.craftinginterpreters.lox.ast.Stmt;
import com.craftinginterpreters.lox.common.ProblemReporter;
import com.craftinginterpreters.lox.common.scanner.Scanner;
import com.craftinginterpreters.lox.common.token.Token;
import com.craftinginterpreters.lox.parser.Parser;
import com.craftinginterpreters.lox.visitors.Interpreter;
import com.craftinginterpreters.lox.visitors.Resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        switch (args.length) {
            case 0: runPrompt(); break;
            case 1: runFile(args[0]); break;
            default:
                System.out.println("Usage: jlox [script]");
                System.exit(64);
        }
    }

    private static void runFile(final String path) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get(path));
        final ProblemReporter problemReporter = new ProblemReporter();
        run(new String(bytes, Charset.defaultCharset()), problemReporter);

        if (problemReporter.hasErrors()) {
            problemReporter.printErrors();
            System.exit(65);
        }

        if (problemReporter.hasRuntimeErrors()) {
            problemReporter.printRuntimeErrors();
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        final InputStreamReader input = new InputStreamReader(System.in);
        final BufferedReader reader = new BufferedReader(input);
        final ProblemReporter reporter = new ProblemReporter();

        while (true) {
            System.out.print("> ");
            final String line = reader.readLine();
            if (line == null) break;
            run(line, reporter);

            // reset session
            reporter.printErrors();
            reporter.printRuntimeErrors();
            reporter.clear();
        }
    }

    // Converts to a list of tokens
    // Next step is make sense of the tokens.
    private static void run(final String source, final ProblemReporter reporter) {
        final Scanner scanner = new Scanner(source, reporter);
        final List<Token> tokens = scanner.scanTokens();
        if (reporter.hasErrors()) return;

        final Parser parser = new Parser(tokens, reporter);
        final List<Stmt> statements = parser.parse();
        if (statements.isEmpty() || reporter.hasErrors() || reporter.hasRuntimeErrors()) return;

        final Resolver resolver = new Resolver(interpreter, reporter);
        resolver.resolve(statements);
        if (reporter.hasErrors() || reporter.hasRuntimeErrors()) return;

        interpreter.interpret(statements, reporter);
    }
}

