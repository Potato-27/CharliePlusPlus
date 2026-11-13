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
- Adding an integrated input method for testing and to allow for the creation of proper programs in charlie++ (this involves figuring out how to make `input()` make to the different scanner methods by type in java)

## Entry 4: Hindley-Milner beginning stages

### what's new and working
- **Parameters:** now functions can be declared with written parameters that can be used in that function. As of right now, it's inconsistent and buggy, but it does work for basic functions.
- **Hindley-Milner Algorithm:** The beginnings of the Hindley Milner type inferencing system is being implemented now. This is why it's been so long since the last log, as this was fairly complex and I was just too tired to work on it. But essentially, now the type resolver goes through every node from the AST and now runs it through a different type resolving method, where unknown type are assigned placeholders, and every time an expression exists logic is done to make that type point to another. For example, if a variable is temporarily assigned type `T0`, and the resolver figures out that `T0 -> T1`, it stores these substitutions in a map. Let's say that `T1 -> int`. Therefore that initial variable, through a method called `prune()`, is also an `int`. This is the core foundation of how the algorithm works, but as of right now it is littered with bugs that I do know about and more I probably do not.

### Some known bugs
- functions cannot evaluate parameters types if their usage is withing the arguments for another function call (e.g. `print(x * x)`)
- cannot assign `String` to parameters (defaults to `Object`)
- fails to properly assign a type to variables initilalized without a value (like `let x`), even when it should be able to (if `x = 75` exists later)

### Next steps
- Figure out the existing bugs
- Allow the type inferecer to look at function calls to inference the type for the parameters of a function
- add return statements

## Entry 5: Return statements, function types, bug fixes

### what's new and working
- **Return statements/function types:** now functions can return values! the function type is automatically inferenced, and values can be automatically returned. 
- **Bug fixes:** To be completely honest I do not remember what all of the little bugs were, but now parameters are properly assigned values based on how they are called and used, including as parameters in another function call
- **Examples:** I was having fun during a college visit having AI generate different programs in charlie++, and I put some in the new `examples` folder. I did not make any of the programs there myself because I don't fully understand them, but they just serve to stress-test charlie++ and makes some cool visuals like the truecolor mandelbrot set generator. Again I did not code those files myself I was just having fun and they also serve as good examples.

### persisting bugs
- The variable initialization without an initializer value still does not work, I have to figure out how to differentiate between a null initializer and a non-existant initializer
- right now if a function just says `print(x)` where the function has parameter `x`, it will just define the parameter type as what it is when first called, so I need to establish proper polymorphism and a `forall a.` type for functions like `print()`

### the next steps
- as previously mentioned, I have to implement polymorphic type shenanigans
- I'm most likely going to scrap the use of classes because it just adds complexity that is not really necessary for my language, but I'll consider adding them in the far future, but I just have too much on my plate right now as is.
- add float/double (decimal) values, and work that in with the TypeResolver, probably adding some sort of attribute to `TypeVariable` so it can work in with integers and addition
- code a vscode extension so there are colors when typing in chpp because colors are fun :)

## Entry 6: some bug fixes and updates, doubles, and cleanup

### what's new
- **Doubles:** Massive news everyone decimals are now working. Generally implementation was pretty simple except for suprise suprise, the TypeResolver. I had to implement a whole new system, which is described in the next bullet.
- **Constraints:** Now `TypeVariable` has an attribute `constraint`. There's right now `NUMBER`, `ADDABLE`, and `NONE`. This had to be done to allow things like integers anbd doubles to be multiplied and make the expression properly resolve to an integer or double, and how they interact with the overloaded `+` operator with string addition and all of that. I could have potentially just made it so all numbers are intrinsically doubles, but then printing would be wonky with everything having `.0` at the end for numbers, also in the future when I add lists/arrays, integers are used to access indicies, and if everything was doubles then I would have to do some casting shenanigans, which probably would have been easier but this was more fun :P
- **Classes are gone:** I deleted the class-related code in `CodeGenerator.java` yeah thats it
- **Null initializer fixed:** Added a new `NullLiteralNode` and `null` keyword, and fixed that with variable declaration without an initializing value (e.g. `let x`), so now initializers are optional and the `null` keyword can be used which is lovely lovely

### the next steps
- I still need my colors for charlie++
- Polymorphic type inferencing still needs to be implemented, but this will likely take a bit longer as it's complicated and I believe I will have to rework part of my program anyway
- after that I will finally be able to start doing lists and arrays which I am very excited for

## Entry 7: polymorphism

### what's new
- **Polymorphism:** This was really confusing for me to be honest, but it has been implemented. What it essentially does is for variables or functions, their type are not immediately set in stone. like for example, `fun id(x){ return x }` before this addition, after it is called as for example `id(5)`, it would be locked in as a function with a return type `int`, and compile to `public static int id(int x)`. This same concept applies for variable declarations.
- **Type Schemes:** Whenever a function is declared or a variable, it is wrapped in a new `Scheme` type, with free unresolved variables associated with that type being assigned to that Scheme's `bounds`. Every time a function is called, it uses the `instantiate()` method to creates and returns a new `FunctionType` by copying the previous `TypeVariables` into new ones, allowing each independent call to have its own associated types.
- **Inferred Return Types:** When a function returns an `Object`, it cannot be added to an `int` because java does not allow that, even if the object it is returning is a number. Now, `CallNode` has an `inferredType` attribute that is calculated through the methods mentioned previously, so when `id(x)` is called like `let y = id(5) + 7`, it will compile to `public static int y = ((int) id(5)) + 7;`, preventing any errors by casting the result to the `inferredType`.
- **Integer compatibility with doubles:** just a quick fix allowing `DOUBLE` to be unified with `INT` in specifically that order, so if a function has a parameter that is resolved to a `double` but is called with an `int`, it does not throw an error and allows it. However, the other way around does not work and does cause an error, as intended.
- **e, pi, and math functions:** I added integrated math functions and constants, specficially `e`, `pi`, `sin()`, and `cos()`. I recently decided to pivot my language to be more math-oriented, so I want to make it closer to writing actual standard math notation and make it convenient when writing scripts. This also gives my language a concrete purpose, which is something I did not really have before other than making a quality-of-life java-esc language. Also, `print()` using the new polymorphic pipeline, so that it can take anything as a parameter.

### the next steps
- Im procrastinating the colors
- More built-in math stuff, like the `^` operator for exponents, which will utilize `Math.pow()` in java. I also would like to add implicit multiplication, like if the variable `x` is defined and you want to write `let y = 3x + 1`.
- More math related stuff I want to add single-line function definitions so things like `f(x) = x^2 + 3x - 1` are valid syntax, I think it'll be neat. It will just map to a normal function like `public static double f()`
- I'm partially procrastinating arrays and lists, but these few things will be easier to implement so I should be able to get to them shortly. This last update was relatively large and arguably the most confusing one yet, so I'm going to first have some fun with this easy stuff then I'll get into the meat and potatoes.