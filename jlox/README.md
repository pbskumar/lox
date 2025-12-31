# jlox

Java based interpreter for `lox`. I'm using this doc to track issues I've found so far and the features I might want to handle.

IK, I should create tests and tickets. Logging issues I've found so far to simplify tracking.

## Status
I've now completed going through part 1 of the book. Before I proceed to bytecode implementation, 
I'll work on a few features listed here. I may not get through all. But at least a few before moving on.

## Thoughts
It's been an incredible journey. I've previously used Visitor pattern with an additional function argument `context`.
This param to the visitor methods was serving the purpose of `environment` in `Interpreter`.

Would we have gone through this approach if we need to pass more than env between functions down the tree?
One challenge with this local context is that it is local! (Cheeky, IK!) 
But, at times, we would need functionality without really polluting the global context. 
May be as I go through these TODO features, I'd want to do it? Probably not.

I've felt challenged implementing `Classes` and `Inheritance`. This required a lot of wiring around the interpreter
and introduction of a new Resolver pass.

Also, the book validates the use of a distinct Visitor for each use case. 
Keeps the business logic isolated and less complex. 
But, one aspect I didn't really like is that Resolver runs first, but modifies the env of Interpreter which runs later.
This could have been implemented by taking out the env object out of interpreter and passing it around.

Resolving all the methods, classes, scopes for interpreter is a waste of resources. 
Every class/function definition is part of the interpreter environment whether it is used or not.  
Another inefficiency (not that the book is trying to be efficient), we are loading the entire source code and hold in memory.

The next section **A Bytecode Virtual Machine** would probably address this. (At least based on the title, I'd think so!)

## nil handling
```
> var a;
> var b;
> a = "assigned";
> print a;
assigned
> print b;
nil
> b + a;
> print b + a;
nilassigned  ---> this is not the correct behavior :|
```

## To-Dos for Tree-Walk Interpreter
These are mostly challenges from the chapters or ideas that I thought would be useful. 
Will get to implement them after I finish the Tree-Walk Interpreter part of the book.

### Functionality
[ ] Comments are currently being parsed, but they are causing parsing errors
  * This is because I deviated from the book and added comment Token in the scanner.
  * For now, I'm reverting to the book state. 
  * Will come back to implement comment handling correctly.

[ ] Support C style `++` and `--`
  * Pre
  * Post

[X] Support Python style `+=`, `-=`, `*=`, `/=` 

[ ] Supporting import statements

[ ] Supporting packages - This is another system in itself. At least, figure out how it works in a basic way

[ ] Add test suite for all features in exec mode and REPL mode.

[ ] `RuntimeError` kills the REPL runtime :(

[ ] Allow multi-line input in REPL

[ ] Basic exception handling

[ ] Add basic library functions:
  * think about what makes sense to add.

[X] Support modulus (`%`) operator

[ ] Support data structures like List, Set, etc.
  * I'd rather start with Boxed types instead of native grammar support. 
  * Honestly, may not get to this until I get to `import` and native libraries

### Scanning
[ ] Add support for C-style block comments (`/* ... */`)

[ ] Store comments. I think this would be useful in auto-doc generation.

### Parsing
[ ] Throw error when a binary operator appears without the left expression.

### Statements and State
[ ] Support expression evaluation in REPL to print values directly to terminal.

[ ] Differentiate between implicit `nil` (uninitialized variable) vs explicit `nil` (explicit initialization)

### Control Flow
[X] Support `break` statement

[X] Support `continue` statement

### Resolving and binding
[ ] Implement immutable env to use persistent data structures

[ ] Extend resolver to report error if local variable is never used

## Classes
[ ] Define static class members instead of this largely unwieldy and open class structure.
  * may be a new keyword?

[ ] Add support for static methods for classes

[ ] Support Getters

