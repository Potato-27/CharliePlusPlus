# charlie++ (.chpp)

charlie++ is a programming language created using a transpiler built on java, creating execution-ready java files from a source .chpp file.

It features quality-of-life features such as optional parenthesis for conditional statements and type inferencing.

This transpiler was constructed from scratch, each file being built from the ground up to create a pipeline including an abstract syntax tree, tokenization, and syntax analysis. 

## System Architecture Pipeline

The compiler takes an input of a `.chpp` source file and passes it through the pipeline to create functional Java architecture.

1. **Lexing** (`Lexer.java`): Takes in the raw source string and iterates through and consumes each character, processing them into tokens and passing through a list of all of the tokens in order, with line-tracking and literal values.
2. **Parsing** (`Parser.java`): Takes the list of tokens from the lexer and iterates through each token, parsing them into appropriate nodes by passing them through a recursive descent parser, translating the token list into an Abstract Syntax Tree (AST).
3. **Type inferencing** (`TypeResolver.java`): Goes through the abstract syntax tree from the parser and resolves any undetermined types for variables and functions, passing along a map of variable and function names with their corresponding types.
4. **Code generation** (`CodeGenerator.java`): Takes in the type map from the type resolver and the AST from the parser and generates the java equivalent of each statement and expression and writes new java files, and automatically executes them.

## Language Design & Features

- Implicit Scoping: uses `let` as the keyword for variable declaration
- Custom Keywords: uses primarily similar keywords to java such as `while` and `class`, but uses some different keywords and native function names like `fun` for function and `print` instead of `System.out.println`.
- Optional Syntax: does not require parenthesis around conditional statements, and semicolons are not implemented at all, expressions/statements end at newlines