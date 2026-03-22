package lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Lexer {
    public ArrayList<Token> parseFile(String filename) {
        ArrayList<Token> tokens = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            Token t;

            do {
                t = getNextToken(br);
                tokens.add(t);
            } while(!t.getType().equals(TokenType.TKN_EOF));
        } catch(IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            return new ArrayList<>();
        }

        return tokens;
    }

    private Token getNextToken(BufferedReader br) throws IOException {
        int c = br.read();

        if(c == -1) {
            return new Token("", TokenType.TKN_EOF);
        }

        // TODO: Actual state logic
        System.out.println("Read: " + (char)c + "\n");

        return new Token(String.valueOf(c), TokenType.TKN_NONE);
    }
}
