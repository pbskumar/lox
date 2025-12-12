# lox: A toy programming language

This repo is an implementation of `lox` language based on [Crafting Interpreters](https://craftinginterpreters.com).


## Why?
I built the SQL parsing and transpiling library for [IoT SiteWise Query Language](https://docs.aws.amazon.com/iot-sitewise/latest/userguide/sql.html). I never expected to enjoy working at the nitty-gritty level of AST traversals and optimizations, but it turned out to be a lot of fun. Coming from a non-CS background, I’ve always been fascinated by the “magic” behind languages and their interpreters or compilers. Now that I’ve gotten my hands dirty with lexing, parsing, AST traversal, and transpiling (all thanks to Visitor Pattern!), I want to go all the way—build a language from scratch and really understand what happens under the hood of interpreters. And what better way than college class styled book!

## jlox - Java based interpreter for lox

1. Setting up `gradle` for Mac

   `brew install gradle`

2. Build `jlox` interpreter

   `cd jlox && ./gradlew build`

3. Add `jlox` to your environment temporarily

   You could run `jlox` using the long format like `./gradlew run` or `build/install/jlox/bin/jlox <fname>`. 
Second option is possible because we configured Gradle to create a distribution. 
But, it is much easier to have the distribution in our path. 
   
   Run `source build/jlox-env.sh` to add `jlox` to your env

4. Run instructions 
   1. To enter REPL mode: `jlox`
   2. To interpret a file: `jlox <filename>`
