package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.scanner.Scanner;
import com.craftinginterpreters.lox.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static boolean hadError = false;

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
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        final InputStreamReader input = new InputStreamReader(System.in);
        final BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            final String line = reader.readLine();
            if (line == null) break;
            run(line);

            // reset session
            hadError = false;
        }
    }

    // Converts to a list of tokens
    // Next step is make sense of the tokens.
    private static void run(final String source) {
        final Scanner scanner = new Scanner(source);
        final List<Token> tokens = scanner.scanTokens();

        for (final Token token : tokens) {
            System.out.println("Token: " + token);
        }
    }

    public static void error(final int line, final String message) {
        report(line, "", message);
    }

    public static void error(final int line, final String where, final String message) {
        report(line, where, message);
    }

    private static void report(final int line, final String where, final String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}

