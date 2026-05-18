import lexer.Lexer;
import lexer.Token;
import syntSemAnalyzer.SyntSemAnalyzer;

import java.util.ArrayList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        SyntSemAnalyzer syntSemAnalyzer = new SyntSemAnalyzer();

        ArrayList<Token> tokens = lexer.parseFile("src/hello-world.c");
        syntSemAnalyzer.analyze(tokens);
    }
}