import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import nodes.*;
import lexer.Token;
import java.util.List;


public class Compiler {
    public static void main(String[] args) {

        String fileContent = "";
        
        // this file is largely AI generated just for the reading files aspect, the instantiation and calling of objects is all legitimate

        try {
            File inputFile = new File("src\\tester.chpp");
            Scanner in = new Scanner(inputFile);
            StringBuilder sb = new StringBuilder();
            
            while (in.hasNextLine()) {
                sb.append(in.nextLine());
                
                if (in.hasNextLine()) {
                    sb.append("\n");
                }
            }
            in.close();
            
            fileContent = sb.toString();
            
            // primary pipeline for the compiler, source -> lexer -> parser -> typeResolver -> codegenerator (which then runs the new .java file)

            Lexer lexer = new Lexer(fileContent);
            List<Token> tokens = lexer.scanTokens();

            Parser parser = new Parser(tokens);
            List<Stmt> parsedCode = parser.parse();

            TypeResolver resolver = new TypeResolver(parsedCode);
            resolver.resolve();

            CodeGenerator generator = new CodeGenerator(parsedCode, resolver.environment);
            generator.generate(); 

        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }
}