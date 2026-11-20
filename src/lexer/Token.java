package lexer;
public class Token{
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    
    public Token(String lexeme, TokenType type, Object literal, int line){
        this.lexeme = lexeme;
        this.type = type;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString(){
        return "( " + lexeme + " | " + type + " | " + literal + " | " + line + " )";
    }
}