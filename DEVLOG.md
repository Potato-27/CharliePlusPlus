# charlie++ devlog

## Entry 1: first entry and baseline, after first code cleanup (October 2026)

### What's working and done
-  Lexer: Largely completely done. I still need to add detection for boolean logic with and and or (|, &), but otherwise it is completely functional and error free except for one issue with the StringHandler when there is just a single quotation in the entire file.
-  Parser: Significant progress made, can handle a large amount of basic syntax like while loops and conditionals, functions and calls, class blocks and variable declarations and assignments, plus basic operations. Error handling still needs to be sorted out, as right now only temporary error markers have been used.
- Code Generator: complete for most any given code that the parser may output, with the exception if the declaration of variables in local scopes because the typeresolver cannot account for those yet. It is able to write 1 or more java files and automatically runs the main file.

### recent changes + clean up
- Went through each file and quickly organized and commented code to make it easier to read and navigate, and cleared up old testing print statements.
- The typeresolver pipeline was recently integrated, although it is very limited as of right now.

### current issues
- Type Resolver: very recently developed file, it only can handle global variable declarations as of right now, and only for simple binary/unary expressions on the right side. I need to integrate the architecture for local environments, and after that develop type inferencing for function parameters so they do not have to be explicitly written by the user.

### the next steps
1. sort out the local environment architecture for variable names for when inside functions or other classes.
2. Learn, figure out, and implement the proper needed architecture for variable inferencing for parameters when declaring functions.


## Entry 2: bug fixing, quality of life, scope management (10/12/2026)

### what's working and done
- **Fixed the quotation issue:** mentioned previously in the lexer, checks first whether it is the end of the file before handling strings.
- **Added comments:** can now use `//` to comment code in the .chpp file, the lexer completely ignores everything past it until the newline.
- **Global and local scoping:** `TypeResolver.java` now is able to create sub environments for every separate block (as determined by curly braces (`{}`)) for the local scoping and instantiation of variables. `BlockNode` now stores the local environments thyemselves.
- **Bolding:** I learned how to use bolded stuff in the devlog very exciting

### the next steps
- Learn, figure out, and implement the proper needed architecture for variable inferencing for parameters when declaring functions. This is something more to work towards, and it will come with implementing return statements.
- More immediate, smaller goal: add the boolean operators AND and OR (`&` and `|`)
- Implement parenthesis into the order of operations for arithmetic and boolean expressions.

## Entry 3: minor update

### what's new and working
- **AND and OR:** `|` and `&` are now fully functional and their layers in the recursive descent parser are fully implemented in `Parser.java`.
- **Grouping:** Parenthesis can now be used to group statements in order of operations, being a branch off of `primary()`. The previous optional parenthesis architecture has been fixed to account for this, and so has the Type Resolver.
- **Minor bug fix:** Previosuly I had forgotten to make the code generator work when you have a literal boolean value (i.e. `true` or `false`) so I patched that up.
- **Another bug fix with new features:** When making `logicalOR()` and `logicalAnd()` I copy and pasted the code from `factor()` because its the same exact structure but I forgot to change a call within it and was causing errors after I ran a tester script but I fixed it up.

### quick update
Recently schoolwork has been pretty draining and it's been eating all of my freetime, so my work on Charlie++ has been a little slow, so this is just a minor update with two easy-to-implement features.

### the next steps
- Still procrastinating working on the variable inferencing for functions and parameters, but that I think will be for the less stressed weekend Charlie
- Adding an integrated input method for testing and to allow for the creation of proper programs in `charlie++` (this involves figuring out how to make `input()` make to the different scanner methods by type in java)