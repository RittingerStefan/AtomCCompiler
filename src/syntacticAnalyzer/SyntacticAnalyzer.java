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
        return consume(TokenType.TKN_INT_IDENT) || consume(TokenType.TKN_DOUBLE_IDENT)
                || consume(TokenType.TKN_CHAR_IDENT) || consume(TokenType.TKN_IDENT);
    }

    // rule: arrayDecl: LBRACKET CT_INT? RBRACKET
    private boolean arrayDecl() {
        if(!consume(TokenType.TKN_LBRACKET)) {
            return false;
        }

        consume(TokenType.TKN_NUM_DEC);

        if(!consume(TokenType.TKN_RBRACKET)) {
            throw new Error("Missing ']' in array declaration at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: fnDef: ( typeBase | VOID ) ID
    //	            LPAR ( fnParam ( COMMA fnParam )* )? RPAR
    //	            stmCompound
    private boolean fnDef() {
        Token firstToken = tokens.getFirst();
        if(!(typeBase() || consume(TokenType.TKN_VOID_IDENT))) {
            return false;
        }

        Token secondToken = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            throw new Error("Missing identifier in declaration at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_LPAREN)) {
            tokens.addFirst(secondToken);
            tokens.addFirst(firstToken);
            return false;
        }

        do {
            fnParam();
        } while (consume(TokenType.TKN_COMMA));

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in function declaration at " + getLineAndColumnForError());
        }

        if(!stmCompound()) {
            throw new Error("Error in body of function at " + getLineAndColumnForError());
        }
        return true;
    }

    // rule: fnParam: typeBase ID arrayDecl?
    private boolean fnParam() {
        if(!typeBase()) {
            return false;
        }

        if(!consume(TokenType.TKN_IDENT)) {
            throw new Error("Missing identifier in parameter declaration at " + getLineAndColumnForError());
        }

        arrayDecl();

        return true;
    }

    // rule: stmCompound: LACC ( varDef | stm )* RACC
    private boolean stmCompound() {
        if(!consume(TokenType.TKN_LACC)) {
            throw new Error("Missing '{' in statement at " + getLineAndColumnForError());
        }

        while(varDef() || stm());

        if(!consume(TokenType.TKN_RACC)) {
            throw new Error("Missing '}' in statement at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule:stm: stmCompound
    //	    | IF LPAR expr RPAR stm ( ELSE stm )?
    //	    | WHILE LPAR expr RPAR stm
    //	    | FOR LPAR expr? SEMICOLON expr? SEMICOLON expr? RPAR stm
    //	    | BREAK SEMICOLON
    //	    | RETURN expr? SEMICOLON
    //	    | expr? SEMICOLON
    private boolean stm() {
        return stmCompound() || ifStm() || whileStm() || forStm() || breakStm() || returnStm() || exprStm();
    }

    // rule: IF LPAR expr RPAR stm ( ELSE stm )?
    private boolean ifStm() {
        if(!consume(TokenType.TKN_IF_IDENT)) {
            return false;
        }

        if(!consume(TokenType.TKN_LPAREN)) {
            throw new Error("Missing '(' in if statement at " + getLineAndColumnForError());
        }

        if(!expr()) {
            throw new Error("Missing expression in if statement at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in if statement at " + getLineAndColumnForError());
        }

        if(!stm()) {
            throw new Error("Wrong statement in if block at " + getLineAndColumnForError());
        }

        if(consume(TokenType.TKN_ELSE_IDENT)) {
            if(!stm()) {
                throw new Error("Wrong statement in else block at " + getLineAndColumnForError());
            }
        }

        return true;
    }

    // rule: WHILE LPAR expr RPAR stm
    private boolean whileStm() {
        if(!consume(TokenType.TKN_WHILE_IDENT)) {
            return false;
        }

        if(!consume(TokenType.TKN_LPAREN)) {
            throw new Error("Missing '(' in while statement at " + getLineAndColumnForError());
        }

        if(!expr()) {
            throw new Error("Missing expression in while statement at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in while statement at " + getLineAndColumnForError());
        }

        if(!stm()) {
            throw new Error("Wrong statement in while block at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: FOR LPAR expr? SEMICOLON expr? SEMICOLON expr? RPAR stm
    private boolean forStm() {
        if(!consume(TokenType.TKN_FOR_IDENT)) {
            return false;
        }

        if(!consume(TokenType.TKN_LPAREN)) {
            throw new Error("Missing '(' in for statement at " + getLineAndColumnForError());
        }

        expr();

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' in for statement at " + getLineAndColumnForError());
        }

        expr();

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' in for statement at " + getLineAndColumnForError());
        }

        expr();

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in for statement at " + getLineAndColumnForError());
        }

        if(!stm()) {
            throw new Error("Wrong statement in for block at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: BREAK SEMICOLON
    private boolean breakStm() {
        if(!consume(TokenType.TKN_BREAK_IDENT)) {
            return false;
        }

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' after break at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: RETURN expr? SEMICOLON
    private boolean returnStm() {
        if(!consume(TokenType.TKN_RETURN_IDENT)) {
            return false;
        }

        expr();

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' after return at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: expr? SEMICOLON
    private boolean exprStm() {
        expr();

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' after expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // TODO
    private boolean expr() {
        return true;
    }
}
