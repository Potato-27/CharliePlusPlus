import nodes.*;
import environment.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;




public class TypeResolver {
    public Environment environment;
    private final List<Stmt> programAst;
    private int nextId = 0;
    private final Map<TypeVariable, Type> substitutions = new HashMap<>();
    private Type currentReturnType = null;
    Set<String> calledFunctions = new HashSet<>(); // Im using ai to give me advice on how to optimize it a little bit with all this new Object stuff now that lists are added
    

    public TypeResolver(List<Stmt> parsedCode){
        this.programAst = parsedCode;
        this.environment = new Environment();

        // setting up built-in functions (e.g. print, sin) 
        List<Type> printArgs = new ArrayList<>();
        printArgs.add(newVariable());
        this.environment.define("print", generalize(new FunctionType(printArgs, PrimitiveType.VOID)));

        List<Type> sinArgs = new ArrayList<>();
        sinArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("sin", generalize(new FunctionType(sinArgs, PrimitiveType.DOUBLE)));

        List<Type> cosArgs = new ArrayList<>();
        cosArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("cos", generalize(new FunctionType(cosArgs, PrimitiveType.DOUBLE)));

        List<Type> tanArgs = new ArrayList<>();
        tanArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("tan", generalize(new FunctionType(tanArgs, PrimitiveType.DOUBLE)));

        List<Type> lnArgs = new ArrayList<>();
        lnArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("ln", generalize(new FunctionType(lnArgs, PrimitiveType.DOUBLE)));

        List<Type> logArgs = new ArrayList<>();
        logArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("log", generalize(new FunctionType(logArgs, PrimitiveType.DOUBLE)));

        List<Type> arcsinArgs = new ArrayList<>();
        arcsinArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("arcsin", generalize(new FunctionType(arcsinArgs, PrimitiveType.DOUBLE)));

        List<Type> arccosArgs = new ArrayList<>();
        arccosArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("arccos", generalize(new FunctionType(arccosArgs, PrimitiveType.DOUBLE)));

        List<Type> arctanArgs = new ArrayList<>();
        arctanArgs.add(newVariable(Constraint.NUMBER));
        this.environment.define("arctan", generalize(new FunctionType(arctanArgs, PrimitiveType.DOUBLE)));

        this.environment.define("e", PrimitiveType.DOUBLE);
        this.environment.define("pi", PrimitiveType.DOUBLE);


    }

    // primary function iterating through the list of nodes from the parser, calling resolveStatement for each node
    public void resolve(){
        for (Stmt node : programAst){
            firstResolve(node);
        }
        
        for (Stmt node : programAst){
            if (node instanceof FunDeclNode funNode){
                if (funNode.name.lexeme.equals("main")){
                    continue;
                }
                resolveStatement(node);
            }
        }

        for (Stmt node : programAst){
            if (node instanceof FunDeclNode funNode){
                if (funNode.name.lexeme.equals("main")){
                    resolveStatement(node);
                }
            } else {
                resolveStatement(node);
            }
        }

        for (Stmt node : programAst){
            if (node instanceof FunDeclNode funNode){
                if (!calledFunctions.contains(funNode.name.lexeme)){
                    Scheme scheme = (Scheme) environment.lookup(funNode.name.lexeme);
                    FunctionType funType = (FunctionType)scheme.type;
                    for (Type arg : funType.args){
                        Type pruned = prune(arg);
                        if (pruned instanceof TypeVariable typeVar && (typeVar.constraint == Constraint.NUMBER || typeVar.constraint == Constraint.NONE)){
                            unify(typeVar, PrimitiveType.DOUBLE);
                        }
                    }
                }
            }
        }

        finalizeEnv(this.environment);

        for (Stmt node : programAst){
            finalizeStmt(node);
        }

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
                
                environment.define(varDeclNode.identifier.lexeme, generalize(resolveExpression(varDeclNode.initializer)));
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
            this.environment.define(funDeclNode.name.lexeme, generalize(functionType));

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
            System.out.println("RETURN DEBUG: Unifying currentFunctionReturnType (" + this.currentReturnType + ") with computed " + returnType);

            unify(currentReturnType, returnType);
            
        }
    }

    // main method for handling the computation and inferencing of types for expressions
    public Type resolveExpression(Expr expr){
        if (expr instanceof LiteralNode){
            LiteralNode literalNode = (LiteralNode) expr;

            if (literalNode.value instanceof Integer){
                return PrimitiveType.INT;
            } else if (literalNode.value instanceof String) {
                return PrimitiveType.STRING;
            } else if (literalNode.value instanceof Boolean){
                return PrimitiveType.BOOLEAN;
            } else if (literalNode.value instanceof Double){
                return PrimitiveType.DOUBLE;
            }

        } else if (expr instanceof NullLiteralNode){
            return newVariable();
        } else if (expr instanceof VariableNode){
            VariableNode variableNode = (VariableNode) expr;

            Type type = environment.lookup(variableNode.identifier.lexeme);
            if (type instanceof Scheme scheme){
                return instantiate(scheme);
            } else {
                return type;
            }
        } else if (expr instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) expr;
            
            Type leftType = resolveExpression(binaryNode.left);
            Type rightType = resolveExpression(binaryNode.right);
            binaryNode.leftType = leftType;
            binaryNode.rightType = rightType;


            if (List.of("-", "*", "/", "%", "^").contains(binaryNode.operator.lexeme)){
                


                Type resultType = newVariable(Constraint.NUMBER);
                Type newVarLeft = newVariable(Constraint.NUMBER);
                Type newVarRight = newVariable(Constraint.NUMBER);
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
                } else if (binaryNode.operator.lexeme.equals("^")){
                    unify(resultType, PrimitiveType.DOUBLE);
                } else if (left instanceof TypeVariable || right instanceof TypeVariable){
                    // whats up everyone this is a secret empty else if statement
                } else if (prune(leftType)==PrimitiveType.DOUBLE || prune(rightType)==PrimitiveType.DOUBLE){
                    unify(resultType, PrimitiveType.DOUBLE);
                } else if (prune(leftType) == PrimitiveType.INT && prune(rightType) == PrimitiveType.INT){
                    unify(resultType, PrimitiveType.INT);
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
                    
                } else if (left instanceof ListType){
                    unify(right, newVariable(Constraint.NUMBER));
                    unify(resultType, doubleBase((ListType) left));
                } else if (right instanceof ListType){
                    unify(left, newVariable(Constraint.NUMBER));
                    unify(resultType, doubleBase((ListType) right));
                } else if (left instanceof TypeVariable || right instanceof TypeVariable){
                    // whats up everyone this is a secret empty else if statement (the sequel)
                } else if (prune(leftType) == PrimitiveType.STRING || prune(rightType) == PrimitiveType.STRING){
                    unify(resultType, PrimitiveType.STRING);
                } else if (prune(leftType)==PrimitiveType.DOUBLE || prune(rightType)==PrimitiveType.DOUBLE){
                    unify(resultType, PrimitiveType.DOUBLE);
                } else if (prune(leftType) == PrimitiveType.INT && prune(rightType) == PrimitiveType.INT){
                    unify(resultType, PrimitiveType.INT);
                }
                binaryNode.inferredType = resultType;
                return resultType;

            } else if (List.of(">", "<", "<=", ">=").contains(binaryNode.operator.lexeme)){
                
                unify(leftType, newVariable(Constraint.NUMBER));
                unify(rightType, newVariable(Constraint.NUMBER));
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
                unify(rightType, newVariable(Constraint.NUMBER));
                TypeVariable resultType = newVariable(Constraint.NUMBER);
                if (prune(rightType) == PrimitiveType.INT){
                    unify(resultType, PrimitiveType.INT);
                } else if (prune(rightType) == PrimitiveType.DOUBLE){
                    unify(resultType, PrimitiveType.DOUBLE);
                }
                unaryNode.inferredType = resultType;
                return resultType;
            } else if (unaryNode.operator.lexeme.equals("!")){
                unify(rightType, PrimitiveType.BOOLEAN);
                unaryNode.inferredType = PrimitiveType.BOOLEAN;
                return PrimitiveType.BOOLEAN;
            }
        } else if (expr instanceof GroupingNode){
            GroupingNode groupingNode = (GroupingNode) expr;
            return resolveExpression(groupingNode.expression);
        } else if (expr instanceof CallNode){
            CallNode callNode = (CallNode) expr;
            Type calleeType = resolveExpression(callNode.name);
            List<Type> args = new ArrayList<>();
            for (Expr arg : callNode.arguments){
                args.add(resolveExpression(arg));
            }
            Type returnType = newVariable();
            Type expectedFunctionType = new FunctionType(args, returnType);
            unify(calleeType, expectedFunctionType);
            callNode.inferredType = returnType;
            if (callNode.name instanceof VariableNode varNode){
                calledFunctions.add(varNode.identifier.lexeme);
            }
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
            unify(indexType, PrimitiveType.INT);
            Type elemType = newVariable();
            unify(targetType, new ListType(elemType));
            indexNode.inferredType = elemType;
            return elemType;
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

    // Makes the base type of a list into a double for clean math operations
    public ListType doubleBase(ListType t){
        if (t.elementType instanceof ListType listType){
            return new ListType(doubleBase(listType));
        } else {
            return new ListType(PrimitiveType.DOUBLE); 
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
                        }
                        substitutions.put((TypeVariable) t1, t2);
                        return;
                    } else {
                        Type base = getBaseType(t2);
                        if (base instanceof TypeVariable baseVar){
                            if (baseVar.constraint == Constraint.NONE){
                                baseVar.constraint = ((TypeVariable)t1).constraint;
                            }
                            substitutions.put((TypeVariable)t1, t2);
                            return;
                        }

                        if (((TypeVariable) t1).constraint == Constraint.NUMBER && (base == PrimitiveType.INT || base == PrimitiveType.DOUBLE)){
                            substitutions.put((TypeVariable) t1, t2);
                            return;
                        } else if (((TypeVariable) t1).constraint == Constraint.ADDABLE && (base == PrimitiveType.INT || base == PrimitiveType.DOUBLE || base == PrimitiveType.STRING)){
                            substitutions.put((TypeVariable) t1, t2);
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
                        }
                        substitutions.put((TypeVariable) t2, t1);
                        return;
                    } else {
                        Type base = getBaseType(t1);
                        if (base instanceof TypeVariable baseVar){
                            if (baseVar.constraint == Constraint.NONE){
                                baseVar.constraint = ((TypeVariable)t2).constraint;
                            }
                            substitutions.put((TypeVariable)t2, t1);
                            return;
                        }
                        if (((TypeVariable) t2).constraint == Constraint.NUMBER && (base == PrimitiveType.INT || base == PrimitiveType.DOUBLE)){
                            substitutions.put((TypeVariable) t2, t1);
                            return;
                        } else if (((TypeVariable) t2).constraint == Constraint.ADDABLE && (base == PrimitiveType.INT || base == PrimitiveType.DOUBLE || base == PrimitiveType.STRING)){
                            substitutions.put((TypeVariable) t2, t1);
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
            } else  {
                if (t1 == PrimitiveType.DOUBLE && t2 == PrimitiveType.INT){
                    return;
                }
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
            if (value instanceof Scheme scheme){
                scheme.type = prune(scheme.type);
            }
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
            finalizeExpr(whileNode.condition);
            finalizeStmt(whileNode.trueStatement);
        } else if (stmt instanceof FunDeclNode){
            FunDeclNode funDeclNode = (FunDeclNode) stmt;

            Scheme scheme = (Scheme) environment.lookup(funDeclNode.name.lexeme);
            FunctionType funType = (FunctionType) scheme.type;
            for (int i = 0; i < funType.args.size(); i++){
                funType.args.set(i, prune(funType.args.get(i)));
            }
            funType.returnType = prune(funType.returnType);

            finalizeStmt(funDeclNode.block);
        } else if (stmt instanceof IfNode){
            IfNode ifNode = (IfNode) stmt;
            finalizeStmt(ifNode.trueStatement);
            finalizeExpr(ifNode.condition);
            if (ifNode.falseStatement != null){
                finalizeStmt(ifNode.falseStatement);
            }
        } else if (stmt instanceof VarDeclNode node){
            finalizeExpr(node.initializer);
        } else if (stmt instanceof ExpressionStatementNode node){
            finalizeExpr(node.expression);
        } else if (stmt instanceof ReturnNode node){
            finalizeExpr(node.expression);
        }
    }

    public void finalizeExpr(Expr expr){
        
        if (expr == null){
            return;
        } else if (expr instanceof CallNode callNode){
            callNode.inferredType = prune(callNode.inferredType);
            for (Expr arg : callNode.arguments){
                finalizeExpr(arg);
            }
        } else if (expr instanceof IndexNode indexNode){
            indexNode.inferredType = prune(indexNode.inferredType);
            finalizeExpr(indexNode.target);
        } else if (expr instanceof BinaryNode node){
            node.leftType = prune(node.leftType);
            node.rightType = prune(node.rightType);
            node.inferredType = prune(node.inferredType);
            finalizeExpr(node.left);
            finalizeExpr(node.right);
        } else if (expr instanceof GroupingNode node){
            finalizeExpr(node.expression);
        } else if (expr instanceof AssignmentNode node){
            finalizeExpr(node.value);
        } else if (expr instanceof UnaryNode node){
            node.inferredType = prune(node.inferredType);
            finalizeExpr(node.right);
        } else if (expr instanceof ListNode node){
            for (Expr expr2 : node.list){
                finalizeExpr(expr2);
            }
        }
    }

    public Set<TypeVariable> obtainVars(Type type){
        
        Type pruned = prune(type);
        if (pruned instanceof TypeVariable typeVariable){
            Set<TypeVariable> vars = new HashSet<>();
            vars.add(typeVariable);
            return vars;
        } else if (pruned instanceof FunctionType functionType){
            Set<TypeVariable> vars = new HashSet<>();
            for (Type arg : functionType.args){
                vars.addAll(obtainVars(arg));
            }
            vars.addAll(obtainVars(functionType.returnType));

            return vars;
        } else if (pruned instanceof PrimitiveType){
            return new HashSet<>();
        } else if (pruned instanceof Scheme scheme){
            Set<TypeVariable> vars = obtainVars(scheme.type);
            vars.removeAll(scheme.bounds);
            return vars;
        } else if (pruned instanceof ListType listType){
            Set<TypeVariable> vars = new HashSet<>();
            vars.addAll(obtainVars(listType.elementType));
            return vars;
        }

        return new HashSet<>();
    }

    public Set<TypeVariable> obtainEnvVars(Environment env){
        Set<TypeVariable> vars = new HashSet<>();
        env.map.forEach((key, value) -> {
            Type pruned = prune(value);
            vars.addAll(obtainVars(pruned));
        });

        if (env.enclosing != null){
            vars.addAll(obtainEnvVars(env.enclosing));
        }

        return vars;
    }

    public Scheme generalize(Type type){
        Type pruned = prune(type);
        Scheme scheme = new Scheme(pruned);
        Set<TypeVariable> typeVars = obtainVars(pruned);
        Set<TypeVariable> envVars = obtainEnvVars(environment);

        typeVars.removeAll(envVars);
        scheme.bounds.addAll(typeVars);
        return scheme;
    }

    public Type instantiate(Scheme scheme){
        Map<TypeVariable, TypeVariable> mapVars = new HashMap<>();
        for (TypeVariable var : scheme.bounds){
            mapVars.put(var, newVariable(var.constraint));
        }

        return substitute(scheme.type, mapVars);
    }

    public Type substitute(Type type, Map<TypeVariable, TypeVariable> mapping){
        Type pruned = prune(type);
        if (pruned instanceof TypeVariable typeVar){
            if (mapping.containsKey(typeVar)){
                return mapping.get(typeVar);
            }
            return pruned;
        } else if (pruned instanceof FunctionType funType){
            List<Type> args = new ArrayList<>();
            for (Type arg : funType.args){
                args.add(substitute(arg, mapping));
            }
            Type returnType = substitute(funType.returnType, mapping);
            return new FunctionType(args, returnType);
        } else if (pruned instanceof PrimitiveType){
            return pruned;
        } else if (pruned instanceof ListType listType){
            return new ListType(substitute(listType.elementType, mapping));
        }

        return newVariable();
    }

}
