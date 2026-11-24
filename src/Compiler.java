import java.io.File;
import java.nio.file.Files;
import nodes.*;
import lexer.Token;
import lexer.TokenType;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;


public class Compiler {
    public static void main(String[] args) {

        File baseDir = getBaseDir();
        String targetPath = (args.length > 0) ? args[0] : new File(baseDir, "examples/tester.chpp").getAbsolutePath();
        File inputFile = new File(targetPath);

        try {
            String fileContent = Files.readString(inputFile.toPath());
            File outDir = new File(baseDir, "out");
            outDir.mkdirs();

            File mainJava = new File(outDir, "Main.java");
            File runtimeArray = new File(baseDir, "runtime/ChppArray.java");

            // 1. Lex -> Parse -> TypeResolve -> Emit out/Main.java
            Lexer lexer = new Lexer(fileContent);
            List<Token> tokens = lexer.scanTokens();

            Parser parser = new Parser(tokens);
            List<Stmt> parsedCode = parser.parse();

            parsedCode = resolveImports(parsedCode, baseDir, inputFile);

            TypeResolver resolver = new TypeResolver(parsedCode);
            List<Stmt> finalAst = resolver.resolve();

            CodeGenerator generator = new CodeGenerator(finalAst, resolver.environment, resolver);
            generator.generate();

            // 2. Compile out/Main.java + runtime/ChppArray.java into out/
            ProcessBuilder compilePb = new ProcessBuilder(
                "javac",
                "-d", outDir.getAbsolutePath(),
                mainJava.getAbsolutePath(),
                runtimeArray.getAbsolutePath()
            );
            compilePb.inheritIO();
            int compileExit = compilePb.start().waitFor();
            if (compileExit != 0) {
                System.err.println("Charlie++ Compilation Error: Java compiler failed.");
                System.exit(compileExit);
            }

            // 3. Run Main from out/
            ProcessBuilder runPb = new ProcessBuilder(
                "java",
                "-cp", outDir.getAbsolutePath(),
                "Main"
            );
            runPb.inheritIO();
            runPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // using ai to generate boilerplate java file plumbing because I have no clue what's going on here
    public static File getBaseDir() {
        File cwd = new File(System.getProperty("user.dir"));
        if (new File(cwd, "src").exists() || new File(cwd, "stdlib").exists()) {
            return cwd;
        }
        try {
            File loc = new File(Compiler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (loc.isDirectory() && (loc.getName().equals("build") || loc.getName().equals("bin"))) {
                return loc.getParentFile();
            }
            return loc.isDirectory() ? loc : loc.getParentFile();
        } catch (Exception e) {
            return cwd;
        }
    }

    public static List<Stmt> resolveImports(List<Stmt> stmts, File baseDir, File currentFile){
        List<Stmt> result = new ArrayList<>();
        Set<String> loaded = new HashSet<>();
        for (Stmt stmt : stmts){
            if (stmt instanceof ImportNode imp ){
                String modName = imp.name.lexeme;
                if (!loaded.add(modName)){
                    continue;
                }
                File modFile = new File(baseDir, "stdlib/" + modName + ".chpp");
                if (!modFile.exists()){
                    modFile = new File(currentFile.getParentFile(), modName + ".chpp");
                }
                
                try{
                    Set<String> functions = new HashSet<>();
                    String fileContent = Files.readString(modFile.toPath());
                    Lexer lexer = new Lexer(fileContent);
                    List<Token> tokens = lexer.scanTokens();

                    for (int i = 0; i < tokens.size() - 1; i++) {
                        if (tokens.get(i).type == TokenType.FUNCTION && tokens.get(i + 1).type == TokenType.IDENTIFIER) {
                            functions.add(tokens.get(i + 1).lexeme);
                        }
                    }

                    for (int i = 0; i < tokens.size(); i++) {
                        Token t = tokens.get(i);
                        if (t.type == TokenType.IDENTIFIER && functions.contains(t.lexeme)) {
                            boolean afterDot = (i > 0 && tokens.get(i - 1).type == TokenType.DOT);
                            if (!afterDot) {
                                tokens.set(i, new Token(modName + "__" + t.lexeme, t.type, t.literal, t.line));
                            }
                        }
                    }


                    Parser parser = new Parser(tokens);
                    List<Stmt> parsedCode = parser.parse();

                    parsedCode = resolveImports(parsedCode, baseDir, modFile);
                    result.addAll(parsedCode);



                } catch (Exception e){
                    e.printStackTrace();
                    System.exit(1);
                }
                

            } else {
                result.add(stmt);
            }
        }
        return result;
    }

    /* 
    public static void prefixCalls(Stmt stmt, String modName, Set<String> modFunctions, Set<String> locals){
        if (stmt == null){
            return;
        } else if (stmt instanceof FunDeclNode node){
            Set<String> funLocals = new HashSet<>(locals);
            for (Token param : node.parameters){
                funLocals.add(param.lexeme);
            }
            prefixCalls(node.block, modName, modFunctions, funLocals);
        } else if (stmt instanceof BlockNode node){
            Set<String> blockLocals = new HashSet<>(locals);
            for (Stmt stmt2 : node.block){
                prefixCalls(stmt2, modName, modFunctions, blockLocals);
            }
        } else if (stmt instanceof VarDeclNode node){
            Set<String> varLocals = new HashSet<>(locals);
            prefixExpr(node.initializer, modName, modFunctions, varLocals);
            locals.add(node.identifier.lexeme);
        } else if (stmt instanceof ForNode node){
            Set<String> forLocals = new HashSet<>(locals);
            forLocals.add(node.variable.lexeme);
            prefixCalls(node.block, modName, modFunctions, forLocals);
            prefixExpr(node.iterable, modName, modFunctions, forLocals);
            prefixExpr(node.condition, modName, modFunctions, forLocals);
        } else if (stmt instanceof IfNode node){
            Set<String> newLocals = new HashSet<>(locals);
            prefixCalls(node.trueStatement, modName, modFunctions, newLocals);
            prefixCalls(node.falseStatement, modName, modFunctions, newLocals);
            prefixExpr(node.condition, modName, modFunctions, newLocals);
        } else if (stmt instanceof WhileNode node){
            Set<String> newLocals = new HashSet<>(locals);
            prefixCalls(node.trueStatement, modName, modFunctions, newLocals);
            prefixExpr(node.condition, modName, modFunctions, newLocals);
        } else if (stmt instanceof ExpressionStatementNode node){
            Set<String> newLocals = new HashSet<>(locals);
            prefixExpr(node.expression, modName, modFunctions, newLocals);
        } else if (stmt instanceof AssignmentNode node){
            Set<String> newLocals = new HashSet<>(locals);
            prefixExpr(node.value, modName, modFunctions, newLocals);
        } else if (stmt instanceof ReturnNode node){
            Set<String> newLocals = new HashSet<>(locals);
            prefixExpr(node.expression, modName, modFunctions, newLocals);
        }
    }*/

     


}