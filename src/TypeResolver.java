import nodes.*;
import java.util.List;

public class TypeResolver {
    public Environment environment;
    private final List<Stmt> programAst;

    public TypeResolver(List<Stmt> parsedCode){
        this.programAst = parsedCode;
        this.environment = new Environment();
    }

    // primary function iterating through the list of nodes from the parser, calling resolveStatement for each node
    public void resolve(){
        for (Stmt node : programAst){
            resolveStatement(node);
        }

    }

    // checks if the node argument is a node that needs to be resolved (i.e. VarDeclNode) and calls resolveExpression() and adds the key and associated value top the current environment
    public void resolveStatement(Stmt node){
        if (node instanceof VarDeclNode){
            VarDeclNode varDeclNode = (VarDeclNode) node;

            environment.define(varDeclNode.identifier.lexeme, resolveExpression(varDeclNode.initializer));
        }
    }

    // main method for handling the computation and inferencing of types for expressions
    public Type resolveExpression(Expr expr){
        if (expr instanceof LiteralNode){
            LiteralNode literalNode = (LiteralNode) expr;

            if (literalNode.value instanceof Integer){
                return Type.INT;
            } else if (literalNode.value instanceof String) {
                return Type.STRING;
            } else if (literalNode.value instanceof Boolean){
                return Type.BOOLEAN;
            }

        } else if (expr instanceof VariableNode){
            VariableNode variableNode = (VariableNode) expr;

            return environment.lookup(variableNode.identifier.lexeme);
        } else if (expr instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) expr;
            
            Type leftType = resolveExpression(binaryNode.left);
            Type rightType = resolveExpression(binaryNode.right);

            if (leftType == Type.UNKNOWN || rightType == Type.UNKNOWN){
                return Type.UNKNOWN;
            }

            if (List.of("-", "*", "/", "%").contains(binaryNode.operator.lexeme)){
                if (leftType == Type.INT && rightType == Type.INT){
                    return Type.INT;
                } else {
                    System.err.println("Incompatible types error");
                    return Type.UNKNOWN;
                }
            } else if (binaryNode.operator.lexeme.equals("+")){
                if (leftType == Type.INT && rightType == Type.INT){
                    return Type.INT;
                } else if (leftType == Type.STRING || rightType == Type.STRING){
                    return Type.STRING;
                } else {
                    System.err.println("Incompatible types error");
                    return Type.UNKNOWN;
                }
            } else if (List.of(">", "<", "<=", ">=").contains(binaryNode.operator.lexeme)){
                if (leftType == Type.INT && rightType == Type.INT){
                    return Type.BOOLEAN;
                } else {
                    System.err.println("Incompatible types error");
                    return Type.UNKNOWN;
                }
            } else if (binaryNode.operator.lexeme.equals("==") || binaryNode.operator.lexeme.equals("!=")){
                if (leftType == rightType){
                    return Type.BOOLEAN;
                } else {
                    System.err.println("Incompatible types error");
                    return Type.UNKNOWN;
                }
            }
        }

        return Type.UNKNOWN;
    }


}
