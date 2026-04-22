import lexer.Lexer;
import lexer.Token;
import syntacticAnalyzer.SyntacticAnalyzer;

import java.util.ArrayList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        SyntacticAnalyzer syntacticAnalyzer = new SyntacticAnalyzer();

        ArrayList<Token> tokens = lexer.parseFile("src/hello-world.c");
        syntacticAnalyzer.analyze(tokens);
    }
}