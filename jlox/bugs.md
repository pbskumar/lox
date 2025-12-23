# Bug file

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