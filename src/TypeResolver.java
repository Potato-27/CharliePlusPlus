import nodes.*;
import environment.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;




public class TypeResolver {
    public Environment environment;
    private final List<Stmt> programAst;
    private int nextId = 0;
    private final Map<TypeVariable, Type> substitutions = new HashMap<>();
    private Type currentReturnType = null;
    

    public TypeResolver(List<Stmt> parsedCode){
        this.programAst = parsedCode;
        this.environment = new Environment();

        // setting up built-in functions (i.e. print)
        List<Type> printArgs = new ArrayList<>();
        printArgs.add(PrimitiveType.STRING);
        Type printType = new FunctionType(printArgs, PrimitiveType.VOID);
        this.environment.define("print", printType);
    }

    // primary function iterating through the list of nodes from the parser, calling resolveStatement for each node
    public void resolve(){
        for (Stmt node : programAst){
            firstResolve(node);
        }
        
        for (Stmt node : programAst){
            resolveStatement(node);
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
            }

        } else if (expr instanceof VariableNode){
            VariableNode variableNode = (VariableNode) expr;

            return environment.lookup(variableNode.identifier.lexeme);
        } else if (expr instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) expr;
            
            Type leftType = resolveExpression(binaryNode.left);
            Type rightType = resolveExpression(binaryNode.right);


            if (List.of("-", "*", "/", "%").contains(binaryNode.operator.lexeme)){
                unify(leftType, PrimitiveType.INT);
                unify(rightType, PrimitiveType.INT);
                return PrimitiveType.INT;
            } else if (binaryNode.operator.lexeme.equals("+")){
                Type prunedLeft = prune(leftType);
                Type prunedRight = prune(rightType);
                if (prunedRight == PrimitiveType.STRING || prunedLeft == PrimitiveType.STRING){
                    unify(leftType, PrimitiveType.STRING);
                    unify(rightType, PrimitiveType.STRING);
                    return PrimitiveType.STRING;
                } else if (prunedLeft == PrimitiveType.INT) {
                    
                    unify(leftType, PrimitiveType.INT);
                    unify(rightType, PrimitiveType.INT);
                    return PrimitiveType.INT;
                } else {
                    unify(leftType, rightType);
                    return leftType;
                }
            } else if (List.of(">", "<", "<=", ">=").contains(binaryNode.operator.lexeme)){
                unify(leftType, PrimitiveType.INT);
                unify(rightType, PrimitiveType.INT);
                return PrimitiveType.BOOLEAN;
            } else if (binaryNode.operator.lexeme.equals("==") || binaryNode.operator.lexeme.equals("!=")){
                unify(leftType, rightType);
                return PrimitiveType.BOOLEAN;
            } else if (binaryNode.operator.lexeme.equals("&&") || binaryNode.operator.lexeme.equals("||")){
                unify(leftType, PrimitiveType.BOOLEAN);
                unify(rightType, PrimitiveType.BOOLEAN);
                return PrimitiveType.BOOLEAN;
            }
        }  else if (expr instanceof UnaryNode unaryNode){
            Type rightType = resolveExpression(unaryNode.right);

            if (unaryNode.operator.lexeme.equals("-")){
                unify(rightType, PrimitiveType.INT);
                return PrimitiveType.INT;
            } else if (unaryNode.operator.lexeme.equals("!")){
                unify(rightType, PrimitiveType.BOOLEAN);
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
            return returnType;
        } else if (expr instanceof AssignmentNode){
            AssignmentNode assignmentNode = (AssignmentNode) expr;
            Type varType = resolveExpression(assignmentNode.variable);
            Type valType = resolveExpression(assignmentNode.value);
            unify(varType, valType);
            return valType;
        }

        return null; 
    }

    // goes through the substitutions list to find the last type value which a chain of types point to (i.e. T0 -> T1 and T1 -> int, so T0 -> int)
    public Type prune(Type type){
        if (type instanceof TypeVariable){
            if (substitutions.containsKey(type)){
                return prune(substitutions.get(type));
            } else {
                return type;
            }
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
                substitutions.put((TypeVariable) t1, t2);
                return;
            } else if (t2 instanceof TypeVariable){
                if (occursIn((TypeVariable) t2, t1)){
                    System.err.println("Cyclic type infinite loop");
                    System.exit(0);
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
        } else {
            return false;
        }
    } 

    // creates a new placeholder type, and increments the counter
    public TypeVariable newVariable(){
        //System.out.println("DEBUG: Created TypeVariable T" + nextId);
        return new TypeVariable(nextId++);
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
        } 
    }

}
