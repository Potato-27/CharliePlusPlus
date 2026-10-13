import java.util.ArrayList;
import java.util.List;
import lexer.Token;
import lexer.TokenType;


public class Lexer{
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    
    private int start = 0;   // Points to the first character in the token being scanned
    private int current = 0; // Points to the character currently being looked at
    private int line = 1;
    
    public Lexer(String source){
        this.source = source;
    }
    
    // method returing the list of tokens, iterates through every character, called from the instantiated Lexer object
    public List<Token> scanTokens(){
        while (current < source.length()){
            start = current;
            scanToken();
        }
        
        tokens.add(new Token("", TokenType.EOF, null, line));
        return tokens;
    }
    

    // primary logic method that reads the current character and reacts accordingly, adding simple tokens or referring to handler methods
    private void scanToken() {
        char c = advance();

        switch (c) {
            // Simple Single Characters
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.STAR);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '%' -> addToken(TokenType.MOD);
            case '/' -> {
                if (peek() == '/') {
                    advance(); 
                    while (peek() != '\n' && peek() != '\0') {
                        advance();
                    }
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            
            
            // special characters
            case '<' -> comparisonHandler();
            case '>' -> comparisonHandler();
            case '=' -> comparisonHandler();
            case '!' -> comparisonHandler();
            case '"' -> stringHandler();
                                
            // skip spaces, tabs, increment line upon newline 
            case ' ', '\r', '\t' -> {  }
            case '\n' -> {line++; tokens.add(new Token("\n", TokenType.NEWLINE, '\n', line));}
    
            // referring to identifier and number handlers, or returning error if character is completely unrecognized
            default -> {if (isAlpha(c)){identifierHandler();} 
                        else if (isNumber(c)){numberHandler();}
                        else {System.err.println("Line " + line + ": Unrecognized character '" + c + "'");}}
        }
    }
    

    // specific handler methods for strings, identifiers and keyword identification, numerical values, and multiple-character comparisons like == or !=
    private void stringHandler(){
        if (current + 1 >= source.length()){
            System.err.println("Line " + line + ": ClosingQuotationError '\"'");
            return;
        }
        char c = advance();
        while (c != '"'){
            if (current + 1 <= source.length()){
                c = advance();
            } else {
                System.err.println("Line " + line + ": ClosingQuotationError '" + c + "'");
                return;
            }
        }
        tokens.add(new Token(source.substring(start, current), TokenType.STRING, source.substring(start + 1, current-1), line));
    }

    private void identifierHandler(){
        String temp;
        while (isAlpha(peek())){
            advance();
        }
        temp = source.substring(start, current);
        
        switch (temp){
            case "if" -> tokens.add(new Token(temp, TokenType.IF, null, line));
            case "else" -> tokens.add(new Token(temp, TokenType.ELSE, null, line));
            case "for" -> tokens.add(new Token(temp, TokenType.FOR, null, line));
            case "while" -> tokens.add(new Token(temp, TokenType.WHILE, null, line));
            case "return" -> tokens.add(new Token(temp, TokenType.RETURN, null, line));
            case "fun" -> tokens.add(new Token(temp, TokenType.FUNCTION, null, line));
            case "fayzan" -> tokens.add(new Token(temp, TokenType.FAYZAN, null, line));
            case "class" -> tokens.add(new Token(temp, TokenType.CLASS, null, line));
            case "let" -> tokens.add(new Token(temp, TokenType.KEYWORD_LET, null, line));
            // boolean literals
            case "true" -> tokens.add(new Token(temp, TokenType.BOOLEAN, true, line));
            case "false" -> tokens.add(new Token(temp, TokenType.BOOLEAN, false, line));
            // identifier
            default -> tokens.add(new Token(temp, TokenType.IDENTIFIER, null, line));
        }
        


    }
    
    private void comparisonHandler(){
        if (current <= source.length()-1 && source.charAt(current) == '='){
            advance();
            switch (source.charAt(current-2)){
                case '=' -> tokens.add(new Token(source.substring(start, current), TokenType.EQUAL_EQUAL, null, line));
                case '>' -> tokens.add(new Token(source.substring(start, current), TokenType.GREATER_EQUAL, null, line));
                case '<' -> tokens.add(new Token(source.substring(start, current), TokenType.LESS_EQUAL, null, line));
                case '!' -> tokens.add(new Token(source.substring(start, current), TokenType.NOT_EQUAL, null, line));
            }
        } else {
            char c = source.charAt(current-1);
            switch (c){
                case '=' -> addToken(TokenType.EQUAL);
                case '>' -> addToken(TokenType.GREATER);
                case '<' -> addToken(TokenType.LESS);
                case '!' -> addToken(TokenType.NOT);
            }
        }
        
    }
    
    private void numberHandler(){
        String temp;
        
        while (isNumber(peek())){
            advance();
        }
        temp = source.substring(start, current);
        
        tokens.add(new Token(temp, TokenType.NUMBER, Integer.parseInt(temp), line));
        
    }


    // specific helper methods for checking the 'type' of a character (i.e. alphabetical or numerical)
    private boolean isAlpha(char c) {
        return switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 '_' -> true;
            default -> false;
        };
    }
    
    private boolean isNumber(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        };
    }


    // quick function for adding simple tokens without literal values
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(text, type, literal, line));
    }
    

    // navigational helper methods for iterating through the text file
    private char advance() {
        return source.charAt(current++);
    }

    private char peek(){
        if (current >= source.length()){
            return '\0';
        } else {
            return source.charAt(current);
        }
    }
    
    
}