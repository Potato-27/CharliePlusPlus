import nodes.*;
import java.util.List;

import environment.*;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;


public class CodeGenerator {
    public StringBuilder javaCode = null;
    private final List<Stmt> programAst;
    private Environment currentEnv; 
    private TypeResolver resolver;
    private Map<String, FunctionType> functionalInterfaces = new LinkedHashMap<>();

    public CodeGenerator(List<Stmt> programAst, Environment globalEnv, TypeResolver resolver){
        javaCode = new StringBuilder();
        this.programAst = programAst;
        this.currentEnv = globalEnv;
        this.resolver = resolver;
    }

    // public method called on the instantiated object that manages the final generation of the java file, iterating through each node from the parser and calling the according helper method
    // after generating the files AI code automatically runs the main file created
    public void generate() {
        //javaCode.append("public class Main {\n");

        javaCode.append("\tpublic static final java.util.Scanner _in = new java.util.Scanner(System.in);");
        List<String> generatedFiles = new ArrayList<>();
        generatedFiles.add("Main.java");

        for (Stmt node : programAst) {
            if (node instanceof VarDeclNode) {
                generateGlobalVariable((VarDeclNode) node, javaCode);
            } else if (node instanceof FunDeclNode) {
                generateTopLevelFunction((FunDeclNode) node);
            }
        }

        javaCode.append("}\n");
        
        StringBuilder interfaceBuilder = new StringBuilder();
        for (Map.Entry<String, FunctionType> entry : new ArrayList<>(functionalInterfaces.entrySet())){
            FunctionType funType = entry.getValue();

            interfaceBuilder.append("@FunctionalInterface\n");
            interfaceBuilder.append("interface " + entry.getKey() + "{\n");

            String returnType = typeString(funType.returnType);

            String argsJoined = "";
            if (funType.args.size() == 0){
                argsJoined = "";
            } else if (funType.args.size() == 1){
                argsJoined = typeString(funType.args.get(0)) + " a0";
            } else {
                for (int i = 0; i < funType.args.size(); i++){
                    if (i != funType.args.size()-1){
                        argsJoined += typeString(funType.args.get(i)) + " a" + i + ", ";
                    } else {
                        argsJoined += typeString(funType.args.get(i)) + " a" + i;
                    }
                }
            }

            interfaceBuilder.append("\t" + returnType + " apply(" + argsJoined + ");\n");

            interfaceBuilder.append("}\n\n");
        }

        String finalJavaCode = "public class Main {\n" + interfaceBuilder.toString() + javaCode.toString();

        //AI generated code fo filewriting and boilerplate java plumbing I still don't know what's going on here
        File baseDir = Compiler.getBaseDir();
        File outDir = new File(baseDir, "out");
        outDir.mkdirs();

        File mainFile = new File(outDir, "Main.java");
        try {
            FileWriter fileWriter = new FileWriter(mainFile);
            fileWriter.write(finalJavaCode);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing Main.java");
            e.printStackTrace();
        }

    }


    // helper methods for generating specific elements 

    // method that handles the transcription of declaration of global variables in the Main java class or other classes (uses "public static" prefix )
    public void generateGlobalVariable(VarDeclNode node, StringBuilder buffer){
        String javaType = typeString(currentEnv.lookup(node.identifier.lexeme));
        if (node.initializer == null){
            buffer.append("\tpublic static " + javaType + " " + node.identifier.lexeme + ";\n");
        } else{
            String expressionStr = generateExpression(node.initializer);
            buffer.append("\tpublic static " + javaType + " " + node.identifier.lexeme + " = " + expressionStr + ";\n");
        }
    }

    // method that handles the transcription of local variables within functions (does not use "public static")
    public void generateLocalVariable(VarDeclNode node, StringBuilder buffer){
        String javaType = typeString(currentEnv.lookup(node.identifier.lexeme));
        if (node.initializer == null) {
            if (javaType.equals("int")) {
                buffer.append("\t" + javaType + " " + node.identifier.lexeme + " = 0;\n");
            } else if (javaType.equals("boolean")) {
                buffer.append("\t" + javaType + " " + node.identifier.lexeme + " = false;\n");
            } else if (javaType.equals("double")) {
                buffer.append("\t" + javaType + " " + node.identifier.lexeme + " = 0.0;\n");
            } else {
                buffer.append("\t" + javaType + " " + node.identifier.lexeme + " = null;\n");
            }
            
        } else if (node.initializer instanceof NullLiteralNode){
            buffer.append("\t" + javaType + " " + node.identifier.lexeme + " = null;\n");
        } else {
            String expressionStr = generateExpression(node.initializer);
            buffer.append("\t" + javaType + " " + node.identifier.lexeme + " = " + expressionStr + ";\n");
        }
    }

    // method that handles the declaration of a function not within a class (within the Main java class) 
    public void generateTopLevelFunction(FunDeclNode node){
        // if main method
        if (node.name.lexeme.equals("main")){
            javaCode.append("\tpublic static void main(String[] args)");
            generateStatement(node.block, javaCode, false);
        } else { 
            FunctionType funType = (FunctionType) currentEnv.lookup(node.name.lexeme);;
            String funTypeStr = typeString(funType.returnType);

            javaCode.append("public static " + funTypeStr + " " + node.name.lexeme + "(");
            Environment previousEnv = currentEnv;
            if (node.block.localEnv != null) {
                currentEnv = node.block.localEnv;
            } else {
                currentEnv = new Environment(previousEnv);
            }

            for (int i = 0; i < node.parameters.size(); i++){
                String typeStr = typeString(currentEnv.lookup(node.parameters.get(i).lexeme));
                javaCode.append(typeStr + " " + node.parameters.get(i).lexeme);
                if (i < node.parameters.size()-1){
                    javaCode.append(", ");
                }
            }
            javaCode.append(")");

            javaCode.append(" {\n");
            for (Stmt s : node.block.block){
                generateStatement(s, javaCode, false);
            }
            javaCode.append("}\n");
            currentEnv = previousEnv;
        }
    }

    // method that handles the declaration of functions within classes (does not include "static")
    public void generateLocalFunction(FunDeclNode node, StringBuilder buffer){
        buffer.append("\tpublic void " + node.name.lexeme + "(");
        
        for (int i = 0; i < node.parameters.size(); i++){
            String typeStr = typeString(currentEnv.lookup(node.parameters.get(i).lexeme));
            buffer.append(typeStr + " " + node.parameters.get(i).lexeme);
            if (i < node.parameters.size()-1){
                buffer.append(", ");
            }
        }
        buffer.append(")");

        generateStatement(node.block, buffer, false);
    }

    // general method that handles the transcription of any and all statements, testing each statements it is called on to see what type and acting accordingly
    public void generateStatement(Stmt node, StringBuilder buffer, boolean isElse){
        
        if (node instanceof VarDeclNode){
            generateLocalVariable((VarDeclNode) node, buffer);
        } else if (node instanceof ExpressionStatementNode){
            Expr expr = ((ExpressionStatementNode)node).expression;
            
            String exprStr = generateExpression(expr);
            buffer.append("\t\t" + exprStr + ";\n");

        } else if (node instanceof IfNode){
            IfNode ifNode = (IfNode) node;
        
            String conditionStr = generateExpression(ifNode.condition);
            
            if (isElse){
                buffer.append("if (" + conditionStr + ")");
            } else{
                buffer.append("\t\tif (" + conditionStr + ")");
            }
            generateStatement(ifNode.trueStatement, buffer, false);

            if (ifNode.falseStatement != null){
                if (ifNode.falseStatement instanceof IfNode){
                    buffer.append(" else ");
                    generateStatement(ifNode.falseStatement, buffer, true);
                } else if (ifNode.falseStatement instanceof BlockNode){
                    buffer.append(" else ");
                    generateStatement(ifNode.falseStatement, buffer, false);
                }
            } else {
                buffer.append("\n");
            }

        } else if (node instanceof WhileNode){
            WhileNode whileNode = (WhileNode) node;

            String conditionStr = generateExpression(whileNode.condition);
            buffer.append("\t\twhile (" + conditionStr + ")");
            generateStatement(whileNode.trueStatement, buffer, false);
        } else if (node instanceof BlockNode){
            BlockNode blockNode = (BlockNode) node;

            Environment previousEnv = currentEnv;
            if (blockNode.localEnv != null) {
                currentEnv = blockNode.localEnv;
            } else {
                currentEnv = new Environment(previousEnv);
            }
            buffer.append(" {\n");
            for (Stmt s : blockNode.block){
                generateStatement(s, buffer, false);
            }
            buffer.append("}\n");
            currentEnv = previousEnv;
        } else if (node instanceof ReturnNode){
            ReturnNode returnNode = (ReturnNode) node;

            if (returnNode.expression == null){
                buffer.append("\treturn;\n");
            } else {
                buffer.append("\treturn " + generateExpression(returnNode.expression) + ";\n");
            }
        } else if (node instanceof ForNode){
            ForNode forNode = (ForNode) node;

            if (forNode.iterable instanceof RangeNode range){
                buffer.append("for (double " + forNode.variable.lexeme + " = " + generateExpression(range.left) + "; " + forNode.variable.lexeme + " <= " + generateExpression(range.right) + "; " + forNode.variable.lexeme + " = " + forNode.variable.lexeme + " + 1)");
                if (forNode.condition != null){
                    buffer.append("{\nif (" + generateExpression(forNode.condition) + ")");
                    generateStatement(forNode.block, buffer, false);
                    buffer.append("}");
                }else {
                    generateStatement(forNode.block, buffer, false);
                }
            } else {
                
                String type = typeString(forNode.block.localEnv.lookup(forNode.variable.lexeme));
                String rawVar = "_raw_" + forNode.variable.lexeme;
                buffer.append("for (Object " + rawVar + " : " + generateExpression(forNode.iterable) + "){\n");
                buffer.append(type + " " + forNode.variable.lexeme + " = (" + type + ") " + rawVar + ";\n");
                if (forNode.condition != null){
                    Environment previousEnv = currentEnv;
                    currentEnv = forNode.block.localEnv;
                    buffer.append("{\nif (" + generateExpression(forNode.condition) + ")");
                    currentEnv = previousEnv;
                    generateStatement(forNode.block, buffer, false);
                    buffer.append("}");
                } else {
                    generateStatement(forNode.block, buffer, false);
                }
                buffer.append("}");
            }
        }
    }

    // method that returns the java equivalent string of the expression in the argument when called
    public String generateExpression(Expr expr){
        if (expr instanceof LiteralNode){
            
            LiteralNode localExpr = (LiteralNode) expr;
            if (localExpr.value instanceof Number num){
                return String.valueOf(num.doubleValue());
            } else if (localExpr.value instanceof String){
                return "\"" + localExpr.value + "\"";
            } else if (localExpr.value instanceof Boolean){
                return localExpr.value.toString();
            } 
        } else if (expr instanceof NullLiteralNode){
            return "null";
        } else if (expr instanceof VariableNode){

            VariableNode localExpr = (VariableNode) expr;
            if (localExpr.inferredType instanceof FunctionType funType && !funType.template.isEmpty()){
                return "Main::" + funType.template;
            }
            if (localExpr.identifier.lexeme.equals("e")){
                return "Math.E";
            } else if (localExpr.identifier.lexeme.equals("pi")){
                return "Math.PI";
            }
            return localExpr.identifier.lexeme;
        } else if (expr instanceof CallNode){

            CallNode localExpr = (CallNode) expr;

            List<String> args = new ArrayList<>();
            for (Expr argument : localExpr.arguments){
                args.add(generateExpression(argument));
            }

            String argsJoined = String.join(", ", args);

            if (localExpr.isImplicitMult){
                return generateExpression(localExpr.name) + " * (" + argsJoined + ")";
            }
            if (localExpr.isInterfaceCall){
                return generateExpression(localExpr.name) + ".apply(" + argsJoined + ")";
            }

            if (localExpr.name instanceof VariableNode){
                
                VariableNode name = (VariableNode) localExpr.name;
                // explicitly printing names for built=in functions, like print() -> System.out.println() or sin() -> Math.sin()

                if (name.identifier.lexeme.equals("print")){
                    return "System.out.println(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("sin")){
                    return "Math.sin(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("cos")){
                    return "Math.cos(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("tan")){
                    return "Math.tan(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("ln")){
                    return "Math.log(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("log")){
                    return "Math.log10(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("arcsin")){
                    return "Math.asin(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("arccos")){
                    return "Math.acos(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("arctan")){
                    return "Math.atan(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("int")){
                    return "((int)" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("floor")){
                    return "Math.floor(" + argsJoined + ")";
                } else if (name.identifier.lexeme.equals("inputString")){
                    return "_in.nextLine()";
                } else if (name.identifier.lexeme.equals("inputNum")){
                    // circumvents java nextLine() issue
                    return "Double.parseDouble(_in.nextLine())";
                }
                
                
                else {
                    FunctionType funType = (FunctionType) currentEnv.lookup(name.identifier.lexeme);
                    if (typeString(funType.returnType).equals("Object") && !typeString(localExpr.inferredType).equals("Object")  && !typeString(localExpr.inferredType).equals("void")){
                        return "(("+ typeString(localExpr.inferredType) + ") " + name.identifier.lexeme + "(" + argsJoined + "))";
                    }
                    return name.identifier.lexeme + "(" + argsJoined + ")";
                }
            }

        } else if (expr instanceof BinaryNode){
            BinaryNode localExpr = (BinaryNode) expr;
    
            

            String leftStr = generateExpression(localExpr.left);
            String rightStr = generateExpression(localExpr.right);
            String opStr = localExpr.operator.lexeme;

            if (resolver.prune(localExpr.leftType) instanceof ListType && resolver.prune(localExpr.rightType) instanceof ListType){
                if (opStr.equals("+")){
                    return "ChppArray.addElements(" + leftStr + ", " + rightStr + ")";
                } else if (opStr.equals("-")){
                    return "ChppArray.subElements(" + leftStr + ", " + rightStr + ")";
                } else if (opStr.equals("*")){
                    return "ChppArray.multElements(" + leftStr + ", " + rightStr + ")";
                } else if (opStr.equals("/")){
                    return "ChppArray.divElements(" + leftStr + ", " + rightStr + ")";
                }
            } else if (resolver.prune(localExpr.leftType) instanceof ListType || resolver.prune(localExpr.rightType) instanceof ListType) {
                if (opStr.equals("+")){
                    return "ChppArray.addScalar(" + leftStr + ", " + rightStr + ")";
                } else if (opStr.equals("-")){
                    return "ChppArray.subScalar(" + leftStr + ", " + rightStr + ")";
                } else if (opStr.equals("*")){
                    return "ChppArray.multiplyScalar(" + leftStr + ", " + rightStr + ")";
                } else if (opStr.equals("/")){
                    return "ChppArray.divScalar(" + leftStr + ", " + rightStr + ")";
                }
            }

            if (localExpr.operator.lexeme.equals("^")){
                return "Math.pow(" + leftStr + ", " + rightStr + ")";
            }
            return leftStr + " " + opStr + " " + rightStr;
        } else if (expr instanceof AssignmentNode){
            AssignmentNode localExpr = (AssignmentNode) expr;
            if (localExpr.variable instanceof IndexNode node){
                return generateExpression(node.target) + ".set((int)" + generateExpression(node.index) + ", " + generateExpression(localExpr.value) + ")";
            }
            String leftStr = generateExpression(localExpr.variable);
            String rightStr = generateExpression(localExpr.value);
            String opStr = localExpr.modifier.lexeme;
            return leftStr + " " + opStr + " " + rightStr;
        } else if (expr instanceof UnaryNode){
            UnaryNode localExpr = (UnaryNode) expr;


            String rightStr = generateExpression(localExpr.right);
            if (localExpr.inferredType instanceof ListType){
                return "ChppArray.negate(" + rightStr + ")";
            }
            return localExpr.operator.lexeme + rightStr;
        } else if (expr instanceof GroupingNode){
            GroupingNode localExpr = (GroupingNode) expr;

            String exprStr = generateExpression(localExpr.expression);
            return "(" + exprStr + ")";
        } else if (expr instanceof ListNode localExpr){
            
            List<String> args = new ArrayList<>();
            for (Expr argument : localExpr.list){
                args.add(generateExpression(argument));
            }

            String argsJoined = String.join(", ", args);

            return "new ChppArray(" + argsJoined + ")";
        } else if (expr instanceof IndexNode localExpr){
            return "((" + typeString(localExpr.inferredType) + ")" + generateExpression(localExpr.target) + ".get((int)(" + generateExpression(localExpr.index) + ")))";
        }


        System.err.println("Temp error message generateExpression()");
        return "";
    }

    // helper method that takes Type type and returns the string version of that type, applicable for functions and varaible declarations
    private String typeString(Type type){
        type = resolver.prune(type);
        if (type == PrimitiveType.NUMBER){
            return "double";
        } else if (type == PrimitiveType.BOOLEAN){
            return "boolean";
        } else if (type == PrimitiveType.STRING){
            return "String";
        } else if (type == PrimitiveType.VOID){
            return "void";
        } else if (type instanceof TypeVariable typeVar){
            if (typeVar.constraint != Constraint.NONE){
                return "double";
            }
        } else if (type instanceof ListType){
            return "ChppArray";
        } else if (type instanceof FunctionType funType){
            String newName = "Fn_" + resolver.mangle(type);
            functionalInterfaces.putIfAbsent(newName, funType);
            return newName;

        }
        return "Object";
    }

}
