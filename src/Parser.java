import lexer.*;
import nodes.*;
import java.util.List;
import java.util.ArrayList;

public class Parser {
    public final List<Token> tokens;

    public int current = 0;
    
    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    // primary method called on the instantiated object, returning the list of nodes in which the file has been parsed to, iterates through the tokens from the lexer
    public List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();

        while (peek().type != TokenType.EOF){
            if (match(TokenType.NEWLINE)){
                continue;
            } else {
                statements.add(declaration());
            }
        }

        return statements;
    }

    // main directing method, diverts to appropriate method depending on the leading keyword of a statement, if not a declaration statement then calling and returning statement()
    public Stmt declaration(){
        if (match(TokenType.KEYWORD_LET)){
            return varDeclaration();
        } else if (match(TokenType.FUNCTION)){
            return funDeclaration();
        } else if (match(TokenType.CLASS)){
            return classDeclaration();
        } else {
            return statement();
        }
    }

    // handles logic for the declaration of variables and returns a VarDeclNode
    public Stmt varDeclaration(){
        Token name = null;
        Expr initializer = null;
        
        if (check(TokenType.IDENTIFIER)){
            name = advance();
        } else {
            System.err.println("Temp error message varDeclaration()");
            return null;
        }

        if (match(TokenType.EQUAL)){
            initializer = expression();
        } else if (match(TokenType.NEWLINE, TokenType.EOF)){
            return new VarDeclNode(name);
        } else {
            System.err.println("Temp error message");
            return null;
        }

        if (match(TokenType.NEWLINE, TokenType.EOF)){
            return new VarDeclNode(name, initializer);
        } else {
            return null;
        }
    }

    // handles logic for the declaration of functions and calls Block() to get the list of statements within the block for said function and returns a FunDeclNode
    public Stmt funDeclaration(){
        Token name = null;
        List<Token> parameters = new ArrayList<>();
        BlockNode block = null;
        
        if (check(TokenType.IDENTIFIER)){
            name = advance();
        } else {
            System.err.println("Temp error message funDeclaration() (identifier)");
            return null;
        }

        if (check(TokenType.LEFT_PAREN)){
            advance();
        } else {
            System.err.println("Temp error message funDeclaration() (left paren)");
            return null;
        }

        if (check(TokenType.RIGHT_PAREN)){
            advance();
        } else if (match(TokenType.IDENTIFIER)){
            parameters.add(previous());
            while (!check(TokenType.RIGHT_PAREN)){
                if (match(TokenType.COMMA)){
                    continue;
                } else if (match(TokenType.IDENTIFIER)){
                    parameters.add(previous());
                }
            }
            advance();
        } else {
            System.err.println("Temp error message funDeclaration() (right paren)");
            return null;
        }

        if (match(TokenType.LEFT_BRACE)){
            block = block();
        } else {
            System.err.println("Temp error message funDeclaration() (brace)");
            return null;
        }

        return new FunDeclNode(name, block, parameters);
    }

    public Stmt classDeclaration(){
        Token name = null;
        List<VarDeclNode> fields = new ArrayList<>();
        List<FunDeclNode> methods = new ArrayList<>();

        if (match(TokenType.IDENTIFIER)){
            name = previous();
        } else {
            System.err.println("Temp error message classDeclaration() (name)");
            return null;
        }

        if (check(TokenType.LEFT_BRACE)){
            advance();
        } else {
            System.err.println("Temp error message classDeclaration() (left brace)");
                return null;
        }

        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)){
            if (match(TokenType.NEWLINE)){
                continue;
            } else if (match(TokenType.KEYWORD_LET)){
                fields.add((VarDeclNode) varDeclaration());
            } else if (match(TokenType.FUNCTION)){
                methods.add((FunDeclNode) funDeclaration());
            } else {
                System.err.println("Temp error message classDeclaration() (name)");
                return null;
            }
        }

        if (check(TokenType.RIGHT_BRACE)){
            advance();
        } else {
            System.err.println("Temp error message classDeclaration() (right brace)");
                return null;
        }

        return new ClassDeclNode(name, fields, methods);
    }

    // checks whether the current token is a while or if keyword and accordingly returns a WhileNode or IfNode, otherwise calls expression() and returns an ExpressionStatementNode
    public Stmt statement(){
        if (match(TokenType.IF)){
            Stmt ifBlock = null;
            Stmt elseBlock = null;

            Expr condition = expression();

            if (match(TokenType.LEFT_BRACE)){
                ifBlock = block();
            } else {
                System.err.println("Temp error message statement() (IF2)");
                return null;
            }

            if (match(TokenType.ELSE)){
                if (check(TokenType.IF)){
                    elseBlock = statement();
                } else if (match(TokenType.LEFT_BRACE)){
                    elseBlock = block();
                } else {
                    System.err.println("Temp error message statement() (ELSE)");
                    return null;
                }
            }

            return new IfNode(condition, ifBlock, elseBlock);

        } else if (match(TokenType.WHILE)){
            Stmt block = null;

            Expr condition = expression();

            if (match(TokenType.LEFT_BRACE)){
                block = block();
            } else {
                System.err.println("Temp error message statement() (WHILE2)");
                return null;
            }

            return new WhileNode(condition, block);
        } else if (match(TokenType.RETURN)){
            
            if (check(TokenType.NEWLINE) || check(TokenType.EOF)) {
                return new ReturnNode(null);
            }
            return new ReturnNode(expression());
        } else {
            Stmt stmt = new ExpressionStatementNode(expression());
            if (match(TokenType.NEWLINE, TokenType.EOF)){
                return stmt;
            } else {
                System.err.println("Temp error message statement() (else)");
                return null;
            }
        }
    }
    

    // Recursive tree for parsing exrpessions

    // for now passes straight through to and returns assignment()
    public Expr expression(){
        return assignment();
    }

    // parses expressions for variable assignment, returning an AssignmentNode, otherwise returns comparison()
    public Expr assignment(){
        Expr expr = logicalOr();
        if (match(TokenType.EQUAL)){
            Token modifier = previous();
            Expr value = expression();
            if (expr instanceof VariableNode){
                return new AssignmentNode(expr, modifier, value);
            } else {
                System.err.println("Temp error message assignment() (variable)");
                return null;
            }
        }
        return expr;
    }

    // parses expressions for the OR logical operator, returning a BinaryNode, otherwise returns logicalAnd
    public Expr logicalOr(){
        Expr expr = logicalAnd();
        while (match(TokenType.OR)){
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new BinaryNode(expr, right, operator);
        }
        return expr;
    }

    // parses expressions for the AND logical operator, returning a BinaryNode, otherwise returns logicalAnd
    public Expr logicalAnd(){
        Expr expr = comparison();
        while (match(TokenType.AND)){
            Token operator = previous();
            Expr right = comparison();
            expr = new BinaryNode(expr, right, operator);
        }
        return expr;
    }

    // parses expressions for comparisons, returning a BinaryNode with a comparison operator, otherwise returns term()
    public Expr comparison(){
        Expr expr = term();
        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.EQUAL_EQUAL, TokenType.LESS_EQUAL, TokenType.LESS, TokenType.NOT_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new BinaryNode(expr, right, operator);
        }
        return expr;
    }

    // parses expressions for binary arithmetic with addition or subtraction, returning a BinaryNode, otherwise returns factor()
    public Expr term(){
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new BinaryNode(expr, right, operator);
        }

        return expr;
    }

    // parses expressions for binary arithmetic with multiplication, division, or modulus, returning a BinaryNode, otherwise returns unary() (higher priority than addition and subtraction in term())
    public Expr factor(){
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.MOD)){
            Token operator = previous();
            Expr right = unary();
            expr = new BinaryNode(expr, right, operator);
        }

        return expr;
    }

    // parses expressions to check whether it has a unary operator (e.g. ! or -) and recursively calls itself if necessary, returning a UnaryNode, otherwise returning call()
    public Expr unary(){

        if (match(TokenType.MINUS, TokenType.NOT)){
            Token operator = previous();
            Expr right = unary();
            return new UnaryNode(right, operator);
        }

        return call();
    }

    // parses expressions to check whether it is a function call and creating and returning a CallNode accordingly, otherwise returns primary()
    public Expr call(){
        Expr expr = primary();
        if (match(TokenType.LEFT_PAREN)){
            List<Expr> arguments = new ArrayList<>();
            while(!check(TokenType.RIGHT_PAREN)){
                if(match(TokenType.COMMA)){
                    continue;
                } else {
                    arguments.add(expression());
                }
            }

            if (match(TokenType.RIGHT_PAREN)){
                expr = new CallNode(expr, arguments);
                return expr;
            }else {
                System.err.println("Temp error message call() (right paren)");
                return null;
            }
            

        }
        return expr;

    }

    // parses expressions to return raw literals or variables
    public Expr primary(){
        if (match(TokenType.NUMBER, TokenType.STRING, TokenType.BOOLEAN)){
            return new LiteralNode(previous().literal);
        } else if (match(TokenType.IDENTIFIER)){
            return new VariableNode(previous());
        } else if (match(TokenType.LEFT_PAREN)){
            GroupingNode node = new GroupingNode(expression());
            if (match(TokenType.RIGHT_PAREN)){
                return node;
            } else {
                System.err.println("Missing right parenthesis for grouping statement");
                return null;
            }
        } else {
            System.err.println("Temp error message in primary(): Stuck on token " + peek().type + " with text '" + peek().lexeme + "' on line: " + peek().line);
            return null;
        }
    }

    // parses the statements within a block ({}) and returns BlockNode with attribute List<Stmt> with the block contents
    public BlockNode block(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)){
            if (match(TokenType.NEWLINE)){
                continue;
            } else {
                statements.add(declaration());
            }
        }
        if(match(TokenType.RIGHT_BRACE)){
            return new BlockNode(statements);
        } else {
            System.err.println("Temp error block()");
            return null;
        }
        
    }


    // navigational helper methods for iterating through the token list
    public Token peek(){
        return tokens.get(current);
    }

    public Token previous(){
        return tokens.get(current - 1);
    }

    public Token advance(){
        if (peek().type != TokenType.EOF){
            return tokens.get(current++);
        } else {
            return peek();
        }
    }

    public boolean check(TokenType type){
        return peek().type == type;
    }

    public boolean match(TokenType... types){
        for (TokenType type : types){
            if (check(type)){
                advance();
                return true;
            }
        }
        return false;
    }
}
