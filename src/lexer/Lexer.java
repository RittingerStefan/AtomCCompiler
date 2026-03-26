package lexer;

import java.io.*;
import java.util.ArrayList;

public class Lexer {
    private State state;
    private ArrayList<Character> SKIP_LIST = new ArrayList<>();

    public Lexer() {
        this.state = State.STATE_START;
        SKIP_LIST.add('\t');
        SKIP_LIST.add(' ');
        SKIP_LIST.add('\r');
    }

    public ArrayList<Token> parseFile(String filename) {
        ArrayList<Token> tokens = new ArrayList<>();
        try(PushbackInputStream input = new PushbackInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            Token t;

            do {
                t = getNextToken(input);
                tokens.add(t);
                System.out.println(t);
            } while(!t.getType().equals(TokenType.TKN_EOF));
        } catch(IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            return new ArrayList<>();
        }

        return tokens;
    }

    private Token getNextToken(PushbackInputStream input) throws IOException {
        StringBuilder value = new StringBuilder();

        while(true) {
            int c = input.read();

            if(SKIP_LIST.contains((char)c)) {
                continue;
            }

            value.append((char)c);

            switch(state) {
                case STATE_START:
                    // TODO: more eof checks will be needed, also for '\n'
                    if(c == -1) return new Token("", TokenType.TKN_EOF);
                    if(c == ',') return new Token(",", TokenType.TKN_COMMA);
                    if(c == ':') return new Token(":", TokenType.TKN_COLON);
                    if(c == ';') return new Token(";", TokenType.TKN_SEMICOLON);
                    if(c == '[') return new Token("[", TokenType.TKN_LBRACKET);
                    if(c == ']') return new Token("]", TokenType.TKN_RBRACKET);
                    if(c == '{') return new Token("{", TokenType.TKN_LACC);
                    if(c == '}') return new Token("}", TokenType.TKN_RACC);
                    if(c == '(') return new Token("(", TokenType.TKN_LPAREN);
                    if(c == ')') return new Token(")", TokenType.TKN_RPAREN);
                    if(c == '+') return new Token("+", TokenType.TKN_ADD);
                    if(c == '*') return new Token("*", TokenType.TKN_MUL);
                    if(c == '.') return new Token(".", TokenType.TKN_DOT);
                    if(c == '/') this.state = State.STATE_DIV;
                    else if(c == '!') this.state = State.STATE_NOT;
                    else if(c == '=') this.state = State.STATE_ASSIGN;
                    else if(c == '<') this.state = State.STATE_LESS;
                    else if(c == '>') this.state = State.STATE_GREATER;
                    else {
                        return new Token(value.toString(), TokenType.TKN_UNK);
                    }
                    break;
                case STATE_DIV:
                    if(c == '/') this.state = State.STATE_COMMENT;
                    else {
                        input.unread(c);
                        this.state = State.STATE_START;
                        return new Token("/", TokenType.TKN_DIV);
                    }
                    break;
                case STATE_COMMENT:
                    if(c == '\n') this.state = State.STATE_START;
                    break;
                case STATE_NOT:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token("!=", TokenType.TKN_NOT_EQ);
                    input.unread(c);
                    return new Token("!", TokenType.TKN_NOT);
                case STATE_ASSIGN:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token("==", TokenType.TKN_EQ);
                    input.unread(c);
                    return new Token("=", TokenType.TKN_ASSIGN);
                case STATE_LESS:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token("<=", TokenType.TKN_LE);
                    input.unread(c);
                    return new Token("<", TokenType.TKN_LT);
                case STATE_GREATER:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token(">=", TokenType.TKN_GE);
                    input.unread(c);
                    return new Token(">", TokenType.TKN_GT);
            }
        }
    }
}
