package lexer;
public enum TokenType{
    // symbols
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    DOT, COMMA, PLUS, MINUS, STAR, SEMICOLON, NEWLINE, SLASH, MOD,
    
    //comparison
    EQUAL_EQUAL, NOT, NOT_EQUAL, EQUAL,
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL, OR, AND,
    
    //types
    NUMBER, IDENTIFIER, STRING, BOOLEAN,
    
    // keywords
    IF, ELSE, FOR, WHILE, RETURN, FUNCTION, FAYZAN, CLASS,
    KEYWORD_LET, TRUE, FALSE,
    
    //end of file marker
    EOF
}