import nodes.*;
import java.util.List;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;


public class CodeGenerator {
    public StringBuilder javaCode = null;
    private final List<Stmt> programAst;
    private final Environment globalEnv;

    public CodeGenerator(List<Stmt> programAst, Environment globalEnv){
        javaCode = new StringBuilder();
        this.programAst = programAst;
        this.globalEnv = globalEnv;
    }

    // public method called on the instantiated object that manages the final generation of the java file, iterating through each node from the parser and calling the according helper method
    // after generating the files AI code automatically runs the main file created
    public void generate() {
        javaCode.append("public class Main {\n");

        List<String> generatedFiles = new ArrayList<>();
        generatedFiles.add("Main.java");

        for (Stmt node : programAst) {
            if (node instanceof VarDeclNode) {
                generateGlobalVariable((VarDeclNode) node, javaCode);
            } else if (node instanceof FunDeclNode) {
                generateTopLevelFunction((FunDeclNode) node);
            } else if (node instanceof ClassDeclNode) {
                generateClass((ClassDeclNode) node);
                generatedFiles.add(((ClassDeclNode) node).name.lexeme + ".java");
            }
        }

        javaCode.append("}\n");
        
        String finalJavaCode = javaCode.toString();

        try {
            FileWriter fileWriter = new FileWriter("Main.java");
            fileWriter.write(finalJavaCode);
            fileWriter.close();
        } catch (IOException e){
            System.out.println("error");
            e.printStackTrace();
        }

        // AI code for automatically running the java files created START
        try {
            
            // 1. Prepare the compilation command: ["javac", "Main.java", "Rocket.java", ...]
            List<String> compileCommand = new ArrayList<>();
            compileCommand.add("javac");
            compileCommand.addAll(generatedFiles);

            // 2. Run the javac compiler process
            Process compileProcess = new ProcessBuilder(compileCommand)
                    .inheritIO() // Redirect error messages to your current terminal
                    .start();
            
            int compileExitCode = compileProcess.waitFor();
            if (compileExitCode != 0) {
                System.err.println("Charlie++ Compilation Error: Java compiler failed.");
                return;
            }


            // 3. Run the compiled Java program: ["java", "Main"]
            Process runProcess = new ProcessBuilder("java", "Main")
                    .inheritIO() // Seamlessly maps the generated output straight to your screen!
                    .start();

            runProcess.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error automatically executing the compiled program.");
            e.printStackTrace();
        }
        // AI code for automatically running the java files created END
    }


    // helper methods for generating specific elements 

    // method that handles the transcription of declaration of global variables in the Main java class or other classes (uses "public static" prefix )
    public void generateGlobalVariable(VarDeclNode node, StringBuilder buffer){
        if (node.initializer == null){
            buffer.append("\tpublic static int " + node.identifier.lexeme + ";\n");
        } else{
            String expressionStr = generateExpression(node.initializer);
            String javaType = typeString(globalEnv.lookup(node.identifier.lexeme));

            buffer.append("\tpublic static " + javaType + " " + node.identifier.lexeme + " = " + expressionStr + ";\n");
        }
    }

    // method that handles the transcription of local variables within functions (does not use "public static")
    public void generateLocalVariable(VarDeclNode node, StringBuilder buffer){
        if (node.initializer == null){
            buffer.append("\tint " + node.identifier.lexeme + ";\n");
        } else{
            String expressionStr = generateExpression(node.initializer);
            String javaType = typeString(globalEnv.lookup(node.identifier.lexeme));

            buffer.append("\t" + javaType + " " + node.identifier.lexeme + " = " + expressionStr + ";\n");
            
        }
    }

    // method that handles the declaration of a function not within a class (within the Main java class) 
    public void generateTopLevelFunction(FunDeclNode node){
        // if main method
        if (node.name.lexeme.equals("main")){
            javaCode.append("\tpublic static void main(String[] args){\n");
            for (Stmt localNode : node.block.block){
                generateStatement(localNode, javaCode, false);
            }

            javaCode.append("\t}\n\n");
        } else {
            javaCode.append("public static void " + node.name.lexeme + "(");

            for (int i = 0; i < node.parameters.size(); i++){
                javaCode.append("Object " + node.parameters.get(i).lexeme);
                if (i < node.parameters.size()-1){
                    javaCode.append(", ");
                }
            }
            javaCode.append("){\n");
            for (Stmt localNode : node.block.block){
                generateStatement(localNode, javaCode, false);
            }


            javaCode.append("\t}\n\n");
        }
    }

    // method that handles the declaration of functions within classes (does not include "static")
    public void generateLocalFunction(FunDeclNode node, StringBuilder buffer){
        buffer.append("\tpublic void " + node.name.lexeme + "(");
        
        for (int i = 0; i < node.parameters.size(); i++){
            buffer.append("Object " + node.parameters.get(i).lexeme);
            if (i < node.parameters.size()-1){
                buffer.append(", ");
            }
        }
        buffer.append("){\n");

        for (Stmt localNode : node.block.block){
            generateStatement(localNode, buffer, false);
        }

        buffer.append("\t}\n\n");
    }

    // method that handles the declaration of classes, creating a new StringBuilder for a separate java class file and writes its own file separate from Main.java
    public void generateClass(ClassDeclNode node){
        StringBuilder classCode = new StringBuilder();
        classCode.append("class " + node.name.lexeme + "{\n");
        for (VarDeclNode localVar : node.fields){
            generateLocalVariable((VarDeclNode) localVar, classCode);
        }
        for (FunDeclNode localFun : node.methods){
            generateLocalFunction((FunDeclNode) localFun, classCode);
        }

        classCode.append("}");

        String finalClassCode = classCode.toString();

        try {
            FileWriter fileWriter = new FileWriter(node.name.lexeme + ".java");
            fileWriter.write(finalClassCode);
            fileWriter.close();
        } catch (IOException e){
            System.out.println("error");
            e.printStackTrace();
        }
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
                buffer.append("if (" + conditionStr + ") {\n");
            } else{
                buffer.append("\t\tif (" + conditionStr + ") {\n");
            }
            for (Stmt s : ((BlockNode)ifNode.trueStatement).block) {
                generateStatement(s, buffer, false);
            }
            buffer.append("\t\t}");

            if (ifNode.falseStatement != null){
                if (ifNode.falseStatement instanceof IfNode){
                    buffer.append(" else ");
                    generateStatement(ifNode.falseStatement, buffer, true);
                } else if (ifNode.falseStatement instanceof BlockNode){
                    buffer.append(" else {\n");
                    for (Stmt s : ((BlockNode)ifNode.falseStatement).block) {
                        generateStatement(s, buffer, false);
                    }
                    buffer.append("\t\t}\n");
                }
            } else {
                buffer.append("\n");
            }

        } else if (node instanceof WhileNode){
            WhileNode ifNode = (WhileNode) node;

            String conditionStr = generateExpression(ifNode.condition);
            buffer.append("\t\twhile (" + conditionStr + ") {\n");
            for (Stmt s : ((BlockNode)ifNode.trueStatement).block) {
                generateStatement(s, buffer, false);
            }
            buffer.append("\t\t}\n");
        }
    }

    // method that returns the java equivalent string of the expression in the argument when called
    public String generateExpression(Expr expr){
        if (expr instanceof LiteralNode){
            
            LiteralNode localExpr = (LiteralNode) expr;
            if (localExpr.value instanceof Integer){
                return localExpr.value.toString();
            } else if (localExpr.value instanceof String){
                return "\"" + localExpr.value + "\"";
            }
        } else if (expr instanceof VariableNode){

            VariableNode localExpr = (VariableNode) expr;
            return localExpr.identifier.lexeme;
        } else if (expr instanceof CallNode){

            CallNode localExpr = (CallNode) expr;

            List<String> args = new ArrayList<>();
            for (Expr argument : localExpr.arguments){
                args.add(generateExpression(argument));
            }

            String argsJoined = String.join(", ", args);

            if (localExpr.name instanceof VariableNode){
                VariableNode name = (VariableNode) localExpr.name;
                if (name.identifier.lexeme.equals("print")){
                    return "System.out.println(" + argsJoined + ")";
                } else {
                    return name.identifier.lexeme + "(" + argsJoined + ")";
                }
            }
        } else if (expr instanceof BinaryNode){
            BinaryNode localExpr = (BinaryNode) expr;
    
            String leftStr = generateExpression(localExpr.left);
            String rightStr = generateExpression(localExpr.right);
            String opStr = localExpr.operator.lexeme;
            return leftStr + " " + opStr + " " + rightStr;
        } else if (expr instanceof AssignmentNode){
            AssignmentNode localExpr = (AssignmentNode) expr;

            String leftStr = generateExpression(localExpr.variable);
            String rightStr = generateExpression(localExpr.value);
            String opStr = localExpr.modifier.lexeme;
            return leftStr + " " + opStr + " " + rightStr;
        } else if (expr instanceof UnaryNode){
            UnaryNode localExpr = (UnaryNode) expr;

            String rightStr = generateExpression(localExpr.right);
            return localExpr.operator.lexeme + rightStr;
        }


        System.err.println("Temp error message generateExpression()");
        return "";
    }

    // helper method that takes Type type and returns the string version of that type, applicable for functions and varaible declarations
    private String typeString(Type type){
        if (type == Type.INT){
            return "int";
        } else if (type == Type.BOOLEAN){
            return "boolean";
        } else if (type == Type.STRING){
            return "String";
        } else if (type == Type.VOID){
            return "void";
        } else {
            System.err.println("Something went bad typeString");
            return "unknown";
        }
    }

}
