package lexer;

import java.io.*;
import java.util.ArrayList;

public class Lexer {
    private State state;
    private ArrayList<Character> SKIP_LIST = new ArrayList<>();
    private int line;
    private int column;

    public Lexer() {
        this.state = State.STATE_START;
        SKIP_LIST.add('\t');
        SKIP_LIST.add(' ');
        SKIP_LIST.add('\r');
    }

    public ArrayList<Token> parseFile(String filename) {
        ArrayList<Token> tokens = new ArrayList<>();
        this.line = 1;
        this.column = 1;

        try(PushbackInputStream input = new PushbackInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            Token t;

            do {
                t = getNextToken(input);
                tokens.add(t);
                this.column += t.getValue().length();
            } while(!t.getType().equals(TokenType.TKN_EOF));
        } catch(IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            return new ArrayList<>();
        }
        return tokens;
    }

    public TokenType getTypeForIdentifier(String identifier) {
        if(identifier.equalsIgnoreCase("break")) return TokenType.TKN_BREAK_IDENT;
        if(identifier.equalsIgnoreCase("char")) return TokenType.TKN_CHAR_IDENT;
        if(identifier.equalsIgnoreCase("double")) return TokenType.TKN_DOUBLE_IDENT;
        if(identifier.equalsIgnoreCase("int")) return TokenType.TKN_INT_IDENT;
        if(identifier.equalsIgnoreCase("void")) return TokenType.TKN_VOID_IDENT;
        if(identifier.equalsIgnoreCase("if")) return TokenType.TKN_IF_IDENT;
        if(identifier.equalsIgnoreCase("else")) return TokenType.TKN_ELSE_IDENT;
        if(identifier.equalsIgnoreCase("while")) return TokenType.TKN_WHILE_IDENT;
        if(identifier.equalsIgnoreCase("for")) return TokenType.TKN_FOR_IDENT;
        if(identifier.equalsIgnoreCase("return")) return TokenType.TKN_RETURN_IDENT;
        if(identifier.equalsIgnoreCase("struct")) return TokenType.TKN_STRUCT_IDENT;

        return TokenType.TKN_IDENT;
    }

    private Token getNextToken(PushbackInputStream input) throws IOException {
        StringBuilder value = new StringBuilder();

        while(true) {
            int c = input.read();
            if(c == '\n') {
                this.line++;
                this.column = 0;
            }

            if(SKIP_LIST.contains((char)c) && this.state == State.STATE_START) {
                if(c == '\t') this.column += 4;
                else this.column++;
                continue;
            }

            value.append((char)c);

            switch(state) {
                case STATE_START:
                    // TODO: more eof checks will be needed, also for '\n'
                    if(c == -1) return new Token("", TokenType.TKN_EOF, line, column);
                    if(c == ',') return new Token(",", TokenType.TKN_COMMA, line, column);
                    if(c == ':') return new Token(":", TokenType.TKN_COLON, line, column);
                    if(c == ';') return new Token(";", TokenType.TKN_SEMICOLON, line, column);
                    if(c == '[') return new Token("[", TokenType.TKN_LBRACKET, line, column);
                    if(c == ']') return new Token("]", TokenType.TKN_RBRACKET, line, column);
                    if(c == '{') return new Token("{", TokenType.TKN_LACC, line, column);
                    if(c == '}') return new Token("}", TokenType.TKN_RACC, line, column);
                    if(c == '(') return new Token("(", TokenType.TKN_LPAREN, line, column);
                    if(c == ')') return new Token(")", TokenType.TKN_RPAREN, line, column);
                    if(c == '+') return new Token("+", TokenType.TKN_ADD, line, column);
                    if(c == '*') return new Token("*", TokenType.TKN_MUL, line, column);
                    if(c == '.') return new Token(".", TokenType.TKN_DOT, line, column);
                    if(c == '/') this.state = State.STATE_DIV;
                    else if(c == '!') this.state = State.STATE_NOT;
                    else if(c == '=') this.state = State.STATE_ASSIGN;
                    else if(c == '<') this.state = State.STATE_LESS;
                    else if(c == '>') this.state = State.STATE_GREATER;
                    else if(c == '&') this.state = State.STATE_AND_AUX;
                    else if(c == '|') this.state = State.STATE_OR_AUX;
                    else if(c == '\'') this.state = State.STATE_CHAR_START;
                    else if(c == '\"') this.state = State.STATE_STR_START;
                    else if(Character.isLetter(c)) this.state = State.STATE_IDENTIFIER;
                    else {
                        return new Token(value.toString(), TokenType.TKN_UNK, line, column);
                    }
                    break;
                case STATE_DIV:
                    if(c == '/') this.state = State.STATE_COMMENT;
                    else {
                        input.unread(c);
                        this.state = State.STATE_START;
                        return new Token("/", TokenType.TKN_DIV, line, column);
                    }
                    break;
                case STATE_COMMENT:
                    if(c == '\n') {
                        this.state = State.STATE_START;
                        value = new StringBuilder();
                        this.column++;
                    }
                    break;
                case STATE_NOT:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token("!=", TokenType.TKN_NOT_EQ, line, column);
                    input.unread(c);
                    return new Token("!", TokenType.TKN_NOT, line, column);
                case STATE_ASSIGN:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token("==", TokenType.TKN_EQ, line, column);
                    input.unread(c);
                    return new Token("=", TokenType.TKN_ASSIGN, line, column);
                case STATE_LESS:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token("<=", TokenType.TKN_LE, line, column);
                    input.unread(c);
                    return new Token("<", TokenType.TKN_LT, line, column);
                case STATE_GREATER:
                    this.state = State.STATE_START;
                    if(c == '=') return new Token(">=", TokenType.TKN_GE, line, column);
                    input.unread(c);
                    return new Token(">", TokenType.TKN_GT, line, column);
                case STATE_AND_AUX:
                    this.state = State.STATE_START;
                    if(c != '&') {
                        throw new Error("Error at " + line + ":" + column + " expected '&'.");
                    }
                    return new Token("&&", TokenType.TKN_AND, line, column);
                case STATE_OR_AUX:
                    this.state = State.STATE_START;
                    if(c != '|') {
                        throw new Error("Error at " + line + ":" + column + " expected '|'.");
                    }
                    return new Token("||", TokenType.TKN_OR, line, column);
                case STATE_CHAR_START:
                    if(c == '\\') this.state = State.STATE_CHAR_ESCAPE;
                    else if(c != '\'') this.state = State.STATE_CHAR_VAL;
                    else {
                        this.state = State.STATE_START;
                        throw new Error("Error at " + line + ":" + column + " expected value for character.");
                    }
                    break;
                case STATE_CHAR_ESCAPE:
                    if(c == ' ') {
                        this.state = State.STATE_START;
                        throw new Error("Error at " + line + ":" + column + " expected escape character.");
                    }else if(c != '\'') this.state = State.STATE_CHAR_VAL;
                    else {
                        this.state = State.STATE_START;
                        throw new Error("Error at " + line + ":" + column + " character not closed.");
                    }
                    break;
                case STATE_CHAR_VAL:
                    this.state = State.STATE_START;
                    if(c == '\'') return new Token(value.toString(), TokenType.TKN_CHAR, line, column);
                    else throw new Error("Error at " + line + ":" + column + " character not closed.");
                case STATE_STR_START:
                    if(c == '\"') {
                        this.state = State.STATE_START;
                        return new Token(value.toString(), TokenType.TKN_STR, line, column);
                    }
                    this.state = State.STATE_STR_VAL;
                    break;
                case STATE_STR_VAL:
                    if(c == '\"') {
                        this.state = State.STATE_START;
                        return new Token(value.toString(), TokenType.TKN_STR, line, column);
                    }
                    else if(c == '\\') {
                        this.state = State.STATE_STR_ESCAPE;
                    }
                    else if(c == '\n') {
                        this.state = State.STATE_START;
                        throw new Error("Error at " + line + ":" + column + " unclosed string.");
                    }
                    break;
                case STATE_STR_ESCAPE:
                    if(c == ' ') {
                        this.state = State.STATE_START;
                        throw new Error("Error at " + line + ":" + column + " expected escape character.");
                    }
                    else if(c == '\n' || c == '\"') {
                        this.state = State.STATE_START;
                        throw new Error("Error at " + line + ":" + column + " unclosed string.");
                    }
                    this.state = State.STATE_STR_VAL;
                    break;
                case STATE_IDENTIFIER:
                    if(!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) {
                        this.state = State.STATE_START;
                        input.unread(c);
                        value.deleteCharAt(value.length() - 1);
                        return new Token(value.toString(), this.getTypeForIdentifier(value.toString()), line, column);
                    }
                    break;
            }
        }
    }
}
