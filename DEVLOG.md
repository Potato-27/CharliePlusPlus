# charlie++ devlog

## Entry 1: first entry and baseline, after first code cleanup (October 2026)

### What's working and done
-  Lexer: Largely completely done. I still need to add detection for boolean logic with and and or (|, &), but otherwise it is completely functional and error free except for one issue with the StringHandler when there is just a single quotation in the entire file.
-  Parser: Significant progress made, can handle a large amount of basic syntax like while loops and conditionals, functions and calls, class blocks and variable declarations and assignments, plus basic operations. Error handling still needs to be sorted out, as right now only temporary error markers have been used.
- Code Generator: complete for most any given code that the parser may output, with the exception if the declaration of variables in local scopes because the typeresolver cannot account for those yet. It is able to write 1 or more java files and automatically runs the main file.

### recent changes + clean up
- Went through each file and quickly organized and commented code to make it easier to read and navigate, and cleared up old testing print statements.
- The typeresolver pipeline was recently integrated, although it is very limited as of right now.

## current issues
- Type Resolver: very recently developed file, it only can handle global variable declarations as of right now, and only for simple binary/unary expressions on the right side. I need to integrate the architecture for local environments, and after that develop type inferencing for function parameters so they do not have to be explicitly written by the user.

### the next steps
1. sort out the local environment architecture for variable names for when inside functions or other classes.
2. Learn, figure out, and implement the proper needed architecture for variable inferencing for parameters when declaring functions.