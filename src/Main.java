import lexer.Lexer;
import lexer.Token;

import java.util.ArrayList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();

        ArrayList<Token> tokens = lexer.parseFile("src/hello-world.c");

        for(Token token : tokens) {
            System.out.println(token);
        }
    }
}