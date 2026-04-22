package syntacticAnalyzer;

import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class SyntacticAnalyzer {
    List<Token> tokens;

    public SyntacticAnalyzer() {
        tokens = new ArrayList<>();
    }

    public void analyze(List<Token> tokens) {
        this.tokens = tokens;
        unit();
    }

    private boolean consume(TokenType toConsume) {
        Token firstToken = tokens.getFirst();

        if(firstToken.getType() == toConsume) {
            tokens.removeFirst();
            return true;
        }

        return false;
    }

    private String getLineAndColumnForError() {
        if(tokens != null && !tokens.isEmpty()) {
            Token firstToken = tokens.getFirst();
            StringBuilder lineAndColumn = new StringBuilder();

            lineAndColumn.append(firstToken.getLine());
            lineAndColumn.append(":");
            lineAndColumn.append(firstToken.getColumn());

            return lineAndColumn.toString();
        }
        return "";
    }

    // rule: unit: ( structDef | fnDef | varDef )* END
    private void unit() {
        while(structDef() || fnDef() || varDef());

        if(!consume(TokenType.TKN_EOF)) {
            throw new Error("File should only contain struct, function or variable definitions");
        }
    }

    // rule: structDef: STRUCT ID LACC varDef* RACC SEMICOLON
    private boolean structDef() {
        if(!consume(TokenType.TKN_STRUCT_IDENT)) {
            return false;
        }

        if(!consume(TokenType.TKN_IDENT)) {
            throw new Error("Missing identifier in structure definition at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_LACC)) {
            throw new Error("Missing '{' in structure definition at " + getLineAndColumnForError());
        }

        while(varDef());

        if(!consume(TokenType.TKN_RACC)) {
            throw new Error("Missing '}' in structure definition at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' in structure definition at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: varDef: typeBase ID arrayDecl? SEMICOLON
    private boolean varDef() {
        if(!typeBase()) {
            return false;
        }

        if(!consume(TokenType.TKN_IDENT)) {
            throw new Error("Missing identifier in declaration at " + getLineAndColumnForError());
        }

        arrayDecl();

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' in declaration at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: typeBase: INT | DOUBLE | CHAR | STRUCT ID
    private boolean typeBase() {
        boolean typeBase = consume(TokenType.TKN_INT_IDENT) || consume(TokenType.TKN_DOUBLE_IDENT) || consume(TokenType.TKN_CHAR_IDENT)
                || consume(TokenType.TKN_STRUCT_IDENT);

        if(!typeBase) {
            return false;
        }

        return true;
    }

    // rule: arrayDecl: LBRACKET CT_INT? RBRACKET
    private boolean arrayDecl() {
        if(!consume(TokenType.TKN_LBRACKET))
            return false;

        consume(TokenType.TKN_NUM_DEC);

        if(!consume(TokenType.TKN_RBRACKET))
            throw new Error("Missing ']' in array declaration at " + getLineAndColumnForError());

        return true;
    }

    private boolean fnDef() {
        return false;
        //throw new Error("TO IMPLEMENT fnDef");
    }
}
