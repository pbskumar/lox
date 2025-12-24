# Bug file and TODOs

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

## REPL
1. In REPL, `RuntimeError` kills the runtime :(


## To-Dos for Tree-Walk Interpreter
These are mostly challenges from the chapters or ideas that I thought would be useful. 
Will get to implement them after I finish the Tree-Walk Interpreter part of the book.

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

