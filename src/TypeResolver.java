import nodes.*;
import environment.*;
import lexer.Token;
import lexer.TokenType;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;




public class TypeResolver {
    public Environment environment;
    public Environment globalEnv;
    private List<Stmt> programAst;
    private int nextId = 0;
    private final Map<TypeVariable, Type> substitutions = new HashMap<>();
    private Type currentReturnType = null;
    private Map<String, FunDeclNode> templates = new HashMap<>();
    private List<FunDeclNode> specialFunctions = new ArrayList<>();
    

    public TypeResolver(List<Stmt> parsedCode){
        this.programAst = parsedCode;
        this.environment = new Environment();
        this.globalEnv = this.environment;

        // setting up built-in functions (e.g. print, sin) 
        List<Type> printArgs = new ArrayList<>();
        printArgs.add(newVariable(Constraint.ANY));
        this.environment.define("print", new FunctionType(printArgs, PrimitiveType.VOID));

        List<Type> sinArgs = new ArrayList<>();
        sinArgs.add(PrimitiveType.NUMBER);
        this.environment.define("sin", new FunctionType(sinArgs, PrimitiveType.NUMBER));

        List<Type> cosArgs = new ArrayList<>();
        cosArgs.add(PrimitiveType.NUMBER);
        this.environment.define("cos", new FunctionType(cosArgs, PrimitiveType.NUMBER));

        List<Type> tanArgs = new ArrayList<>();
        tanArgs.add(PrimitiveType.NUMBER);
        this.environment.define("tan", new FunctionType(tanArgs, PrimitiveType.NUMBER));

        List<Type> lnArgs = new ArrayList<>();
        lnArgs.add(PrimitiveType.NUMBER);
        this.environment.define("ln", new FunctionType(lnArgs, PrimitiveType.NUMBER));

        List<Type> logArgs = new ArrayList<>();
        logArgs.add(PrimitiveType.NUMBER);
        this.environment.define("log", new FunctionType(logArgs, PrimitiveType.NUMBER));

        List<Type> arcsinArgs = new ArrayList<>();
        arcsinArgs.add(PrimitiveType.NUMBER);
        this.environment.define("arcsin", new FunctionType(arcsinArgs, PrimitiveType.NUMBER));

        List<Type> arccosArgs = new ArrayList<>();
        arccosArgs.add(PrimitiveType.NUMBER);
        this.environment.define("arccos", new FunctionType(arccosArgs, PrimitiveType.NUMBER));

        List<Type> arctanArgs = new ArrayList<>();
        arctanArgs.add(PrimitiveType.NUMBER);
        this.environment.define("arctan", new FunctionType(arctanArgs, PrimitiveType.NUMBER));

        List<Type> intArgs = new ArrayList<>();
        intArgs.add(PrimitiveType.NUMBER);
        this.environment.define("int", new FunctionType(intArgs, PrimitiveType.NUMBER));

        List<Type> floorArgs = new ArrayList<>();
        floorArgs.add(PrimitiveType.NUMBER);
        this.environment.define("floor", new FunctionType(floorArgs, PrimitiveType.NUMBER));

        List<Type> inputStringArgs = new ArrayList<>();
        this.environment.define("inputString", new FunctionType(inputStringArgs, PrimitiveType.STRING));  

        List<Type> inputNumArgs = new ArrayList<>();
        this.environment.define("inputNum", new FunctionType(inputNumArgs, PrimitiveType.NUMBER));  

        this.environment.define("e", PrimitiveType.NUMBER);
        this.environment.define("pi", PrimitiveType.NUMBER);


    }

    // primary function iterating through the list of nodes from the parser, calling resolveStatement for each node
    public List<Stmt> resolve(){
        //first pass, adding all user-made functions to template and doing firstResolve on main, resolving global variables
        for (Stmt node : programAst){
            if (node instanceof FunDeclNode funNode){
                if (funNode.name.lexeme.equals("main")){
                    firstResolve(node);
                } else {
                    templates.put(funNode.name.lexeme, funNode);
                }
            } else if (node instanceof VarDeclNode){
                resolveStatement(node);
            }
        }
        
        // resolving main
        for (Stmt node : programAst){
            if (node instanceof FunDeclNode funNode && funNode.name.lexeme.equals("main")){
                resolveStatement(node);
            }
        }

        // defaulting to VOID return type if needed
        for (FunDeclNode funNode : specialFunctions){
            FunctionType funType = (FunctionType) environment.lookup(funNode.name.lexeme);
            for (Type arg : funType.args){
                Type pruned = prune(arg);
                if (pruned instanceof TypeVariable typeVar && typeVar.constraint != Constraint.NONE){
                    unify(typeVar, PrimitiveType.NUMBER);
                } 
                
            }
            Type prunedReturn = prune(funType.returnType);
            if (prunedReturn instanceof TypeVariable returnVar && returnVar.constraint != Constraint.NONE) {
                unify(returnVar, PrimitiveType.NUMBER);
            } else if (prunedReturn instanceof TypeVariable returnVar && returnVar.constraint == Constraint.NONE){
                unify(returnVar, PrimitiveType.VOID);
            }
        }

        finalizeEnv(this.environment);

        for (Stmt node : programAst){
            if (node instanceof VarDeclNode){
                finalizeStmt(node);
            } else if (node instanceof FunDeclNode funNode && funNode.name.lexeme.equals("main")){
                finalizeStmt(node);
            }
        }

        for (FunDeclNode node :specialFunctions){
            finalizeStmt(node);
        }

        templates.forEach((key, value) -> {
            programAst.remove(value);
             
        });

        programAst.addAll(specialFunctions);
        return programAst;
    }

    // first pass which resolves all the function declarations using placeholder variables
    public void firstResolve(Stmt node){
        if (node instanceof FunDeclNode){
            FunDeclNode funDeclNode = (FunDeclNode) node;
            List<Type> params = new ArrayList<>();

            //Environment previousEnv = this.environment;
            //this.environment = new Environment(previousEnv);
            for (int i = 0; i < funDeclNode.parameters.size(); i++){
                params.add(newVariable());
            }
            //funDeclNode.block.localEnv = environment;
            //this.environment = previousEnv;

            Type returnType = newVariable();
            Type functionType = new FunctionType(params, returnType);
            environment.define(funDeclNode.name.lexeme, functionType);
        }
    }

    // checks if the node argument is a node that needs to be resolved (i.e. VarDeclNode) and calls resolveExpression() and adds the key and associated value top the current environment
    public void resolveStatement(Stmt node){
        if (node instanceof VarDeclNode){
            VarDeclNode varDeclNode = (VarDeclNode) node;
            if (varDeclNode.initializer == null){
                environment.define(varDeclNode.identifier.lexeme, newVariable());
            } else {
                
                environment.define(varDeclNode.identifier.lexeme, resolveExpression(varDeclNode.initializer));
            }
            
        } else if (node instanceof BlockNode){
            BlockNode blockNode = (BlockNode) node;
            Environment previousEnv = this.environment;
            this.environment = new Environment(previousEnv);
            for (Stmt stmt : blockNode.block){
                resolveStatement(stmt);
            }
            blockNode.localEnv = this.environment;
            this.environment = previousEnv;
        } else if (node instanceof FunDeclNode){
            FunDeclNode funDeclNode = (FunDeclNode) node;

            FunctionType functionType = (FunctionType) this.environment.lookup(funDeclNode.name.lexeme);
            List<Type> paramTypes = functionType.args;

            Type previousReturnType = currentReturnType;
            currentReturnType = functionType.returnType;

            Environment previousEnv = this.environment;
            this.environment = new Environment(previousEnv);

            for (int i = 0; i < funDeclNode.parameters.size(); i++) {
                this.environment.define(funDeclNode.parameters.get(i).lexeme, paramTypes.get(i));
            }

            for (Stmt stmt : funDeclNode.block.block){
                resolveStatement(stmt);
            }

            

            funDeclNode.block.localEnv = environment;
            this.environment = previousEnv;

            this.environment.map.remove(funDeclNode.name.lexeme);
            this.environment.define(funDeclNode.name.lexeme, functionType);

            currentReturnType = previousReturnType;

        } else if (node instanceof IfNode){
            IfNode ifNode = (IfNode) node;
            Type conditionType = resolveExpression(ifNode.condition);
            unify(conditionType, PrimitiveType.BOOLEAN);
            resolveStatement(ifNode.trueStatement);
            if (ifNode.falseStatement != null){
                resolveStatement(ifNode.falseStatement); 
            }
        } else if (node instanceof WhileNode){
            WhileNode whileNode = (WhileNode) node;
            Type conditionType = resolveExpression(whileNode.condition);
            unify(conditionType, PrimitiveType.BOOLEAN);
            resolveStatement(whileNode.trueStatement);
        } else if (node instanceof ExpressionStatementNode){
            ExpressionStatementNode exprStmtNode = (ExpressionStatementNode) node;

            resolveExpression(exprStmtNode.expression);
        } else if (node instanceof ReturnNode){
            ReturnNode returnNode = (ReturnNode) node;

            Type returnType;
            if (returnNode.expression == null){
                returnType = PrimitiveType.VOID;
            } else {
                returnType = resolveExpression(returnNode.expression);
            }
            //System.out.println("RETURN DEBUG: Unifying currentFunctionReturnType (" + this.currentReturnType + ") with computed " + returnType);

            unify(currentReturnType, returnType);
            
        } else if (node instanceof ForNode){
            ForNode forNode = (ForNode) node;

            Environment previousEnv = this.environment;
            this.environment = new Environment(previousEnv);

            Type iterableType = resolveExpression(forNode.iterable);
            if (forNode.iterable instanceof RangeNode){
                this.environment.define(forNode.variable.lexeme, PrimitiveType.NUMBER);
            } else {
                Type elemType = newVariable();
                unify(iterableType, new ListType(elemType));
                this.environment.define(forNode.variable.lexeme, elemType);
            }
            if (forNode.condition != null){
                unify(resolveExpression(forNode.condition), PrimitiveType.BOOLEAN);
            }
            
            for (Stmt stmt : forNode.block.block){
                resolveStatement(stmt);
            }

            forNode.block.localEnv = environment;
            this.environment = previousEnv;
        }
    }

    // main method for handling the computation and inferencing of types for expressions
    public Type resolveExpression(Expr expr){
        if (expr instanceof LiteralNode){
            LiteralNode literalNode = (LiteralNode) expr;

            if (literalNode.value instanceof Number){
                return PrimitiveType.NUMBER;
            } else if (literalNode.value instanceof String) {
                return PrimitiveType.STRING;
            } else if (literalNode.value instanceof Boolean){
                return PrimitiveType.BOOLEAN;
            }

        } else if (expr instanceof NullLiteralNode){
            return newVariable();
        } else if (expr instanceof VariableNode){
            VariableNode variableNode = (VariableNode) expr;

            if (this.environment.lookup(variableNode.identifier.lexeme) != null) {
                return this.environment.lookup(variableNode.identifier.lexeme);
            }
            if (templates.containsKey(variableNode.identifier.lexeme)){
                FunDeclNode funNode = templates.get(variableNode.identifier.lexeme);
                List<Type> args = new ArrayList<>();
                for (int i = 0; i < funNode.parameters.size(); i++){
                    args.add(newVariable());
                }
                FunctionType placeholder = new FunctionType(args, newVariable());
                placeholder.template = variableNode.identifier.lexeme;
                variableNode.inferredType = placeholder;
                return placeholder;
            }

            Type type = environment.lookup(variableNode.identifier.lexeme);
            return type;
        } else if (expr instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) expr;
            
            Type leftType = resolveExpression(binaryNode.left);
            Type rightType = resolveExpression(binaryNode.right);
            binaryNode.leftType = leftType;
            binaryNode.rightType = rightType;


            if (List.of("-", "*", "/", "%", "^").contains(binaryNode.operator.lexeme)){
                


                Type resultType = newVariable(Constraint.ARITHMETIC);
                Type newVarLeft = newVariable(Constraint.ARITHMETIC);
                Type newVarRight = newVariable(Constraint.ARITHMETIC);
                unify(leftType, newVarLeft);
                unify(rightType, newVarRight);

                Type left = prune(leftType);
                Type right = prune(rightType);
                if (left instanceof ListType && right instanceof ListType){
                    unify(left, right);
                    unify(resultType, doubleBase((ListType) left));
                } else if (left instanceof ListType){
                    unify(resultType, doubleBase((ListType) left));
                } else if (right instanceof ListType){
                    unify(resultType, doubleBase((ListType) right));
                } else if (left == PrimitiveType.NUMBER && right == PrimitiveType.NUMBER){
                    unify(resultType, PrimitiveType.NUMBER);
                }
                binaryNode.inferredType = resultType;
                return resultType;
            } else if (binaryNode.operator.lexeme.equals("+")){
                Type resultType = newVariable(Constraint.ADDABLE);
                Type newVarLeft = newVariable(Constraint.ADDABLE);
                Type newVarRight = newVariable(Constraint.ADDABLE);
                unify(leftType, newVarLeft);
                unify(rightType, newVarRight);

                Type left = prune(leftType);
                Type right = prune(rightType);
                if (left instanceof ListType && right instanceof ListType){
                    if (getBaseType(left) == PrimitiveType.STRING){
                        unify(resultType, left);
                    } else {
                        unify(resultType, doubleBase((ListType) left));
                    }
                    
                } else if (prune(leftType) == PrimitiveType.STRING || prune(rightType) == PrimitiveType.STRING){
                    unify(resultType, PrimitiveType.STRING);
                } else if (left instanceof ListType){
                    unify(right, PrimitiveType.NUMBER);
                    unify(resultType, doubleBase((ListType) left));
                } else if (right instanceof ListType){
                    unify(left, PrimitiveType.NUMBER);
                    unify(resultType, doubleBase((ListType) right));
                } else if (prune(leftType)==PrimitiveType.NUMBER || prune(rightType)==PrimitiveType.NUMBER){
                    unify(resultType, PrimitiveType.NUMBER);
                }
                binaryNode.inferredType = resultType;
                return resultType;

            } else if (List.of(">", "<", "<=", ">=").contains(binaryNode.operator.lexeme)){
                
                unify(leftType, PrimitiveType.NUMBER);
                unify(rightType, PrimitiveType.NUMBER);
                binaryNode.inferredType = PrimitiveType.BOOLEAN;
                return PrimitiveType.BOOLEAN;
            } else if (binaryNode.operator.lexeme.equals("==") || binaryNode.operator.lexeme.equals("!=")){
                unify(leftType, rightType);
                binaryNode.inferredType = PrimitiveType.BOOLEAN;
                return PrimitiveType.BOOLEAN;
            } else if (binaryNode.operator.lexeme.equals("&&") || binaryNode.operator.lexeme.equals("||")){
                unify(leftType, PrimitiveType.BOOLEAN);
                unify(rightType, PrimitiveType.BOOLEAN);
                binaryNode.inferredType = PrimitiveType.BOOLEAN;
                return PrimitiveType.BOOLEAN;
            }
        } else if (expr instanceof UnaryNode unaryNode){
            Type rightType = resolveExpression(unaryNode.right);
        
            if (unaryNode.operator.lexeme.equals("-")){
                unify(rightType, newVariable(Constraint.ARITHMETIC));
                Type returnType = prune(rightType);
                unaryNode.inferredType = returnType;
                return returnType;
            } else if (unaryNode.operator.lexeme.equals("!")){
                unify(rightType, PrimitiveType.BOOLEAN);
                return PrimitiveType.BOOLEAN;
            }
        } else if (expr instanceof GroupingNode){
            GroupingNode groupingNode = (GroupingNode) expr;
            return resolveExpression(groupingNode.expression);
        } else if (expr instanceof CallNode){
            CallNode callNode = (CallNode) expr;
            
            List<Type> args = new ArrayList<>();
            for (Expr arg : callNode.arguments){
                args.add(resolveExpression(arg));
            }

            String baseName = ((VariableNode) callNode.name).identifier.lexeme;


            // if function parameter or local variable
            if (this.environment.lookup(baseName) != null && globalEnv.lookup(baseName) == null){
                Type localType = prune(this.environment.lookup(baseName));
                if (localType instanceof FunctionType funType){
                    if (funType.args.size() == args.size()){
                        for (int i = 0; i < args.size(); i++){
                            unify(funType.args.get(i), args.get(i));
                        }
                        callNode.isInterfaceCall = true;
                    } else {
                        System.err.println("Argument count mismatch in interface call");
                        System.exit(0);
                    }
                } else if (localType == PrimitiveType.NUMBER || localType instanceof ListType){
                    if (args.size() != 1){
                        System.err.println("Must have one exrpession for implicit multiplication");
                        System.exit(0);
                    }
                    return resolveImplicitMult((VariableNode)callNode.name, localType, args.get(0), callNode);
                }
            }
            // if it is a user-generated function
            else if (templates.containsKey(baseName)){
                String newName = specializeTemplate(baseName, args);
                for (Type arg : args){
                    Type pruned = prune(arg);
                    if (pruned instanceof FunctionType funType && !funType.template.isEmpty() && templates.containsKey(funType.template)){
                        funType.template = specializeTemplate(funType.template, funType.args);
                        unify(funType, globalEnv.lookup(funType.template));
                    }
                }
                callNode.name = new VariableNode(new Token(newName, TokenType.IDENTIFIER, null, 0));
            }

            

            Type calleeType = resolveExpression(callNode.name);
            Type returnType = newVariable();
            unify(calleeType, new FunctionType(args, returnType));
            callNode.inferredType = returnType;
            return returnType;


        } else if (expr instanceof AssignmentNode){
            AssignmentNode assignmentNode = (AssignmentNode) expr;
            Type varType = resolveExpression(assignmentNode.variable);
            Type valType = resolveExpression(assignmentNode.value);
            unify(varType, valType);
            return valType;
        } else if (expr instanceof ListNode listNode){
            if (listNode.list.isEmpty()){
                return new ListType(newVariable());
            }
            Type elemType = newVariable();
            for (Expr expr2 : listNode.list){
                unify(elemType, resolveExpression(expr2));
            }
            return new ListType(elemType);
        } else if (expr instanceof IndexNode indexNode){
            Type targetType = resolveExpression(indexNode.target);
            Type indexType = resolveExpression(indexNode.index);
            unify(indexType, PrimitiveType.NUMBER);
            Type elemType = newVariable();
            unify(targetType, new ListType(elemType));
            indexNode.inferredType = elemType;
            return elemType;
        } else if (expr instanceof RangeNode rangeNode){
            Type leftType = resolveExpression(rangeNode.left);
            Type rightType = resolveExpression(rangeNode.right);
            unify(leftType, PrimitiveType.NUMBER);
            unify(rightType, PrimitiveType.NUMBER);
            return PrimitiveType.NUMBER;
        }

        return null; 
    }

    // helper methods for lists
    //returns the base type of a list, like INT if the list passed is List<List<INT>>
    public Type getBaseType(Type t){
        while (t instanceof ListType list){
            t = prune(list.elementType);
        }
        return t;
    }

    // creates the new name for a function with the used parameters when called, and if that combination of parameters had not yet been called it makes a new FunDeclNode with that specific function
    public String specializeTemplate(String baseName, List<Type> argTypes){
        String newName = baseName;
        for (Type arg : argTypes){
            newName += mangle(prune(arg));
        }
        if (globalEnv.lookup(newName) == null){
            FunDeclNode template = templates.get(baseName);
            FunDeclNode newNode = new FunDeclNode(new Token(newName, TokenType.IDENTIFIER, null, 0), (BlockNode)cloneStmt(template.block), template.parameters);

            Environment previousEnv = this.environment;
            this.environment = globalEnv;
            List<Type> prunedArgs = new ArrayList<>();
            for (Type arg : argTypes){
                prunedArgs.add(prune(arg));
            }
            
            FunctionType funType = new FunctionType(prunedArgs, newVariable());
            this.environment.define(newName, funType);
            resolveStatement(newNode);

            String oldName = newName;
            newName = baseName;
            for (Type arg : prunedArgs){
                newName += mangle(prune(arg));
            }
            if (!newName.equals(oldName)){
                newNode.name = new Token(newName, TokenType.IDENTIFIER, null, 0);
                if (globalEnv.lookup(newName) == null){
                    globalEnv.define(newName, funType);
                    specialFunctions.add(newNode);
                }
            } else {
                specialFunctions.add(newNode);
            }
            this.environment = previousEnv;
        }
        
        return newName;
    }

    //
    public Type resolveImplicitMult(VariableNode target, Type targetType, Type argType, CallNode callNode){
        if (callNode.arguments.size() == 1){
            unify(argType, newVariable(Constraint.ARITHMETIC));
            callNode.isImplicitMult = true;
            if (prune(targetType) instanceof ListType){
                callNode.inferredType = targetType;
                return targetType;
            } else if (prune(argType) instanceof ListType){
                callNode.inferredType = argType;
                return argType;
            } else if (prune(targetType) == PrimitiveType.NUMBER && prune(argType) == PrimitiveType.NUMBER){
                callNode.inferredType = PrimitiveType.NUMBER;
                return PrimitiveType.NUMBER;
            } else {
                callNode.inferredType = prune(argType);
                return prune(argType);
            }
        }
        System.err.println("Cannot do implicit multiplication on a group of multiple elemtns");
        System.exit(0);
        return null;
    }

    public String mangle(Type t){
        if (t instanceof ListType list){
            return "_Listof" + mangle(prune(list.elementType));
        } else if (t instanceof FunctionType fun){
            String returnString = "_FUN";
            for (Type arg : fun.args){
                returnString += mangle(prune(arg));
            }
            returnString += "_to" + mangle(prune(fun.returnType));
            return returnString;
        } else {
            return "_" + prune(t);
        }
    }



    // Makes the base type of a list into a double for clean math operations
    public ListType doubleBase(ListType t){
        if (t.elementType instanceof ListType listType){
            return new ListType(doubleBase(listType));
        } else {
            return new ListType(PrimitiveType.NUMBER); 
        }
    }



    // goes through the substitutions list to find the last type value which a chain of types point to (i.e. T0 -> T1 and T1 -> int, so T0 -> int)
    public Type prune(Type type){
        if (type instanceof TypeVariable){
            if (substitutions.containsKey(type)){
                return prune(substitutions.get(type));
            } else {
                return type;
            }
        } else if (type instanceof ListType){
            Type type2 = ((ListType) type).elementType;
            return new ListType(prune(type2));
        } else {
            return type;
        }
    }

    // ensures two values have the same type, passing through if they are and returning an error if not
    private void unify(Type t1, Type t2){
        t1 = prune(t1);
        t2 = prune(t2);
        if (t1 == t2){
            return;
        } else {
            if (t1 instanceof TypeVariable){
                
                if (occursIn((TypeVariable) t1, t2)){
                    System.err.println("Cyclic type infinite loop");
                    System.exit(0);
                }

                if (((TypeVariable) t1).constraint != Constraint.NONE){
                    if (t2 instanceof TypeVariable) {
                        if (((TypeVariable) t2).constraint == Constraint.NONE){
                            ((TypeVariable) t2).constraint = ((TypeVariable) t1).constraint;
                        } else if (((TypeVariable) t2).constraint == Constraint.ADDABLE && ((TypeVariable) t1).constraint == Constraint.ARITHMETIC) {
                            ((TypeVariable) t2).constraint = ((TypeVariable) t1).constraint;
                        } else if (((TypeVariable) t2).constraint == Constraint.ANY || ((TypeVariable) t1).constraint == Constraint.ANY){
                            return;
                        }
                        substitutions.put((TypeVariable) t1, t2);
                        return;
                    } else {
                        Type base = getBaseType(t2);
                        if (base instanceof TypeVariable baseVar){
                            if (baseVar.constraint == Constraint.NONE){
                                baseVar.constraint = ((TypeVariable)t1).constraint;
                            } else if (baseVar.constraint == Constraint.ADDABLE && ((TypeVariable) t1).constraint == Constraint.ARITHMETIC) {
                                baseVar.constraint = ((TypeVariable) t1).constraint;
                            } else if (baseVar.constraint == Constraint.ANY || ((TypeVariable) t1).constraint == Constraint.ANY){
                                return;
                            }
                            substitutions.put((TypeVariable)t1, t2);
                            return;
                        }

                        if (((TypeVariable) t1).constraint == Constraint.ARITHMETIC && base == PrimitiveType.NUMBER) {
                            substitutions.put((TypeVariable) t1, t2);
                            return;
                        } else if (((TypeVariable) t1).constraint == Constraint.ADDABLE && (base == PrimitiveType.NUMBER || base == PrimitiveType.STRING)){
                            substitutions.put((TypeVariable) t1, t2);
                            return;
                        } else if (((TypeVariable) t1).constraint == Constraint.ANY){
                            return;
                        } else {
                            System.err.println("Invalid Type: " + t2);
                            System.exit(0);
                        }
                    }
                }

                substitutions.put((TypeVariable) t1, t2);
                return;


            } else if (t2 instanceof TypeVariable){
                if (occursIn((TypeVariable) t2, t1)){
                    System.err.println("Cyclic type infinite loop");
                    System.exit(0);
                }

                if (((TypeVariable) t2).constraint != Constraint.NONE){
                    if (t1 instanceof TypeVariable) {
                        if (((TypeVariable) t1).constraint == Constraint.NONE){
                            ((TypeVariable) t1).constraint = ((TypeVariable) t2).constraint;
                        } else if (((TypeVariable) t1).constraint == Constraint.ADDABLE && ((TypeVariable) t2).constraint == Constraint.ARITHMETIC) {
                            ((TypeVariable) t1).constraint = ((TypeVariable) t2).constraint;
                        } else if (((TypeVariable) t1).constraint == Constraint.ANY || ((TypeVariable) t2).constraint == Constraint.ANY){
                            return;
                        }
                        substitutions.put((TypeVariable) t2, t1);
                        return;
                    } else {
                        Type base = getBaseType(t1);
                        if (base instanceof TypeVariable baseVar){
                            if (baseVar.constraint == Constraint.NONE){
                                baseVar.constraint = ((TypeVariable)t2).constraint;
                            } else if (baseVar.constraint == Constraint.ADDABLE && ((TypeVariable) t2).constraint == Constraint.ARITHMETIC) {
                                baseVar.constraint = ((TypeVariable) t2).constraint;
                            } else if (baseVar.constraint == Constraint.ANY || ((TypeVariable) t2).constraint == Constraint.ANY){
                                return;
                            }
                            substitutions.put((TypeVariable)t2, t1);
                            return;
                        }

                        if (((TypeVariable) t2).constraint == Constraint.ARITHMETIC && base == PrimitiveType.NUMBER) {
                            substitutions.put((TypeVariable) t2, t1);
                            return;
                        } else if (((TypeVariable) t2).constraint == Constraint.ADDABLE && (base == PrimitiveType.NUMBER || base == PrimitiveType.STRING)){
                            substitutions.put((TypeVariable) t2, t1);
                            return;
                        } else if (((TypeVariable) t2).constraint == Constraint.ANY){
                            return;
                        } else {
                            System.err.println("Invalid Type: " + t1);
                            System.exit(0);
                        }
                    }
                }

                substitutions.put((TypeVariable) t2, t1);
                return;
            } else if (t1 instanceof FunctionType && t2 instanceof FunctionType){
                FunctionType ft1 = (FunctionType) t1;
                FunctionType ft2 = (FunctionType) t2;
                if (ft1.args.size() != ft2.args.size()){
                    System.err.println("Argument count mismatch");
                    System.exit(0);
                    return;
                } else{
                    for (int i = 0; i < ft1.args.size(); i++){
                        unify(ft1.args.get(i), ft2.args.get(i));
                    }
                    unify(ft1.returnType, ft2.returnType);
                    return;
                }
            } else if (t1 instanceof ListType && t2 instanceof ListType){
                ListType lt1 = (ListType) t1;
                ListType lt2 = (ListType) t2;
                unify(lt1.elementType, lt2.elementType);
                return;
            } else {
                System.err.println("Type error: Cannot unify " + t1 + " with " + t2);
                
                System.exit(1);
            } 
        }
    }

    // checks for an infinite loop error (ex. if T0 - > T1 and also T1 -> T0)
    public boolean occursIn(TypeVariable var, Type type){
        type = prune(type);
        if (type instanceof TypeVariable){
            TypeVariable typeVar = (TypeVariable) type;
            if (typeVar.id == var.id){
                return true;
            } else {
                return false;
            }
        } else if (type instanceof FunctionType){
            FunctionType funType = (FunctionType) type;
            for (Type arg : funType.args){
                if (occursIn(var, arg)){
                    return true;
                }
            }
            if (occursIn(var, funType.returnType)){
                return true;
            }
            return false;
        } else if (type instanceof ListType){
            ListType listType = (ListType) type;
            return occursIn(var, listType.elementType);
        } else {
            return false;
        }
    } 

    // creates a new placeholder type, and increments the counter
    public TypeVariable newVariable(){
        //System.out.println("DEBUG: Created TypeVariable T" + nextId);
        return new TypeVariable(nextId++);
    }

    public TypeVariable newVariable(Constraint constraint){
        //System.out.println("DEBUG: Created TypeVariable T" + nextId);
        return new TypeVariable(nextId++, constraint);
    }

    // runs through the given environment's map and reassigns all the names to their pruned types
    public void finalizeEnv(Environment env){
        env.map.forEach((key, value) -> {
            Type pruned = prune(value);
            //System.out.println("DEBUG Finalize: " + key + " started as " + value + " and pruned to " + pruned);
            env.map.put(key, pruned);
             
        });
    }

    // takes a statement and runs its block through the finalizeEnv() method
    public void finalizeStmt(Stmt stmt){
        if (stmt instanceof BlockNode){
            BlockNode blockNode = (BlockNode) stmt;
            for (Stmt statement : blockNode.block){
                finalizeStmt(statement);
            }
            finalizeEnv(blockNode.localEnv);
        } else if (stmt instanceof WhileNode){
            WhileNode whileNode = (WhileNode) stmt;
            finalizeStmt(whileNode.trueStatement);
        } else if (stmt instanceof FunDeclNode){
            FunDeclNode funDeclNode = (FunDeclNode) stmt;

            FunctionType funType = (FunctionType) environment.lookup(funDeclNode.name.lexeme);
            for (int i = 0; i < funType.args.size(); i++){
                funType.args.set(i, prune(funType.args.get(i)));
            }
            funType.returnType = prune(funType.returnType);

            finalizeStmt(funDeclNode.block);
        } else if (stmt instanceof IfNode){
            IfNode ifNode = (IfNode) stmt;
            finalizeStmt(ifNode.trueStatement);
            if (ifNode.falseStatement != null){
                finalizeStmt(ifNode.falseStatement);
            }
        } else if (stmt instanceof ForNode){
            ForNode forNode = (ForNode) stmt;
            finalizeStmt(forNode.block);
        }
    }

    public Stmt cloneStmt(Stmt stmt){
        if (stmt instanceof BlockNode node){
            List<Stmt> statements = new ArrayList<>();
            for (Stmt s : node.block){
                statements.add(cloneStmt(s));
            }
            return new BlockNode(statements);
        } else if (stmt instanceof VarDeclNode node){
            if (node.initializer == null){
                return new VarDeclNode(node.identifier);
            }
            return new VarDeclNode(node.identifier, cloneExpr(node.initializer));
        } else if (stmt instanceof IfNode node){
            if (node.falseStatement == null){
                return new IfNode(cloneExpr(node.condition), cloneStmt(node.trueStatement));
            }
            return new IfNode(cloneExpr(node.condition), cloneStmt(node.trueStatement), cloneStmt(node.falseStatement));
        } else if (stmt instanceof WhileNode node){
            return new WhileNode(cloneExpr(node.condition), cloneStmt(node.trueStatement));
        } else if (stmt instanceof ReturnNode node) {
            return new ReturnNode(cloneExpr(node.expression));
        } else if (stmt instanceof ExpressionStatementNode node){
            return new ExpressionStatementNode(cloneExpr(node.expression));
        } else if (stmt instanceof ForNode node){
            return new ForNode(node.variable, cloneExpr(node.iterable), (BlockNode)cloneStmt(node.block), cloneExpr(node.condition));
        }
        return null;
    }

    public Expr cloneExpr(Expr expr){
        if (expr == null){
            return null;
        } else if (expr instanceof LiteralNode || expr instanceof NullLiteralNode || expr instanceof VariableNode){
            return expr;
        } else if (expr instanceof BinaryNode node){
            return new BinaryNode(cloneExpr(node.left), cloneExpr(node.right), node.operator);
        } else if (expr instanceof GroupingNode node){
            return new GroupingNode(cloneExpr(node.expression));
        } else if (expr instanceof UnaryNode node){
            return new UnaryNode(cloneExpr(node.right), node.operator);
        } else if (expr instanceof AssignmentNode node){
            return new AssignmentNode(cloneExpr(node.variable), node.modifier, cloneExpr(node.value));
        } else if (expr instanceof CallNode node){
            List<Expr> args = new ArrayList<>();
            for (Expr arg : node.arguments){
                args.add(cloneExpr(arg));
            }
            return new CallNode(cloneExpr(node.name), args);
        } else if (expr instanceof ListNode node){
            List<Expr> list = new ArrayList<>();
            for (Expr elem : node.list){
                list.add(cloneExpr(elem));
            }
            return new ListNode(list);
        } else if (expr instanceof IndexNode node){
            return new IndexNode(cloneExpr(node.target), cloneExpr(node.index));
        } else if (expr instanceof RangeNode node){
            return new RangeNode(cloneExpr(node.left), cloneExpr(node.right));
        }
        return null;
    }
}
