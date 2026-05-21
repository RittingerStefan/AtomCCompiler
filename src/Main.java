import lexer.Lexer;
import lexer.Token;
import syntSemAnalyzer.SyntSemAnalyzer;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        SyntSemAnalyzer syntSemAnalyzer = new SyntSemAnalyzer();

        ArrayList<Token> tokens = lexer.parseFile("src/hello-world.c");
        syntSemAnalyzer.analyze(tokens);
    }
}