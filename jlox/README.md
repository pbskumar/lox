# jlox

Java based interpreter for `lox`. I'm using this doc to track issues I've found so far and the features I might want to handle.

IK, I should create tests and tickets. Logging issues I've found so far to simplify tracking.

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

[ ] Supporting import statements

[ ] Supporting packages - This is another system in itself. At least, figure out how it works in a basic way

[ ] Add test suite for all features in exec mode and REPL mode.

[ ] `RuntimeError` kills the REPL runtime :(

[ ] Allow multi-line input in REPL

[ ] Basic exception handling

[ ] Add basic library functions:
  * think about what makes sense to add.

### Scanning
[ ] Add support for C-style block comments (`/* ... */`)

[ ] Store comments. I think this would be useful in auto-doc generation.

### Parsing
[ ] Throw error when a binary operator appears without the left expression.

### Statements and State
[ ] Support expression evaluation in REPL to print values directly to terminal.

[ ] Differentiate between implicit `nil` (uninitialized variable) vs explicit `nil` (explicit initialization)

### Control Flow
[ ] Support `continue`, `break` statements

### Resolving and binding
[ ] Implement immutable env to use persistent data structures

[ ] Extend resolver to report error if local variable is never used


## Classes
[ ] Define static class members instead of this largely unwieldy and open class structure.
  * may be a new keyword?

[ ] Add support for static methods for classes

[ ] Support Getters

