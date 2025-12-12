package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(final String path) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void runPrompt() throws IOException {
        final InputStreamReader input = new InputStreamReader(System.in);
        final BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            final String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(final String source) {
        System.out.println("lox: " + source);
    }
}

