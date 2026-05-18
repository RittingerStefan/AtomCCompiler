package syntSemAnalyzer;

import lexer.Token;
import lexer.TokenType;
import syntSemAnalyzer.semantic.Symbol;
import syntSemAnalyzer.semantic.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class SyntSemAnalyzer {
    List<Token> tokens;
    SymbolTable symbolTable;

    public SyntSemAnalyzer() {
        tokens = new ArrayList<>();
        symbolTable = new SymbolTable();
        symbolTable.addDomain(); // the global area
        // TODO: Add globally defined functions, like print
    }

    public void analyze(List<Token> tokens) {
        this.tokens = tokens;
        unit();
    }

    private boolean consume(TokenType toConsume) {
        Token firstToken = tokens.getFirst();

        //System.out.print("Reading: " + firstToken + "; Comparing with: " + toConsume);
        if(firstToken.getType() == toConsume) {
            tokens.removeFirst();
            //System.out.println(" -> consumed");
            return true;
        }

        //System.out.println();

        return false;
    }

    private boolean checkNextToken(TokenType toCheck) {
        Token firstToken = tokens.getFirst();

        return firstToken.getType() == toCheck;
    }

    private String getLineAndColumnForError() {
        if(tokens != null && !tokens.isEmpty()) {
            Token firstToken = tokens.getFirst();
            StringBuilder lineAndColumn = new StringBuilder();

            lineAndColumn.append(firstToken.getLine());
            lineAndColumn.append(":");
            lineAndColumn.append(firstToken.getColumn());
            lineAndColumn.append("; Current token is: ");
            lineAndColumn.append(firstToken);

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

        Token structName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            throw new Error("Missing identifier in structure definition at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_LACC)) {
            throw new Error("Missing '{' in structure definition at " + getLineAndColumnForError());
        }

        List<String> fields = new ArrayList<>();
        Symbol field = varDefStruct();
        while(field != null) {
            fields.add(field.toString());
            field = varDefStruct();
        }

        if(!consume(TokenType.TKN_RACC)) {
            throw new Error("Missing '}' in structure definition at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' in structure definition at " + getLineAndColumnForError());
        }

        symbolTable.addSymbol(new Symbol(structName.getValue(), fields));

        return true;
    }

    // rule: varDef: typeBase ID arrayDecl? SEMICOLON
    // duplicated to make adding struct fields easier
    private Symbol varDefStruct() {
        Token typeName = tokens.getFirst();
        if(!typeBase()) {
            return null;
        }

        Token varName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            tokens.addFirst(typeName);
            return null;
        }

        int arrayElementValue = arrayDecl();

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' in declaration at " + getLineAndColumnForError());
        }

        // check that types are defined
        if(typeName.getType() == TokenType.TKN_IDENT) {
            if(!symbolTable.checkIfStructDefined(typeName.getValue())) {
                throw new Error("Unknown type '" + typeName.getValue() +  "' at " + getLineAndColumnForError());
            }
        }

        // instead of adding value to domain, it is returned
        Symbol newVar;

        if(arrayElementValue >= 0)
            newVar = new Symbol(varName.getValue(), typeName.getValue(), arrayElementValue);
        else
            newVar = new Symbol(varName.getValue(), typeName.getValue());

        return newVar;
    }

    // rule: varDef: typeBase ID arrayDecl? SEMICOLON
    private boolean varDef() {
        Token typeName = tokens.getFirst();
        if(!typeBase()) {
            return false;
        }

        Token varName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            tokens.addFirst(typeName);
            return false;
        }

        int arrayElementValue = arrayDecl();

        if(!consume(TokenType.TKN_SEMICOLON)) {
            throw new Error("Missing ';' in declaration at " + getLineAndColumnForError());
        }


        // check that types are defined
        if(typeName.getType() == TokenType.TKN_IDENT) {
            if(!symbolTable.checkIfStructDefined(typeName.getValue())) {
                throw new Error("Unknown type '" + typeName.getValue() +  "' at " + getLineAndColumnForError());
            }
        }

        // add value to current domain
        Symbol newVar;

        if(arrayElementValue >= 0)
            newVar = new Symbol(varName.getValue(), typeName.getValue(), arrayElementValue);
        else
            newVar = new Symbol(varName.getValue(), typeName.getValue());


        if(symbolTable.checkIfDefinedInCurrentDomain(newVar)) {
            throw new Error("Duplicate variable definition at " + getLineAndColumnForError());
        }

        symbolTable.addSymbol(newVar);
        return true;
    }

    // rule: typeBase: INT | DOUBLE | CHAR | BOOL | STRUCT ID
    private boolean typeBase() {
        return consume(TokenType.TKN_INT_IDENT) || consume(TokenType.TKN_DOUBLE_IDENT)
                || consume(TokenType.TKN_CHAR_IDENT) || consume(TokenType.TKN_BOOL_IDENT)
                || consume(TokenType.TKN_IDENT);
    }

    // rule: arrayDecl: LBRACKET CT_INT? RBRACKET
    private int arrayDecl() {
        int value = 0;
        if(!consume(TokenType.TKN_LBRACKET)) {
            return -1;
        }

        Token elementCount = tokens.getFirst();
        if(consume(TokenType.TKN_NUM_DEC)) {
            value = Integer.parseInt(elementCount.getValue());
        }

        if(!consume(TokenType.TKN_RBRACKET)) {
            throw new Error("Missing ']' in array declaration at " + getLineAndColumnForError());
        }

        return value;
    }

    // rule: fnDef: ( typeBase | VOID ) ID
    //	            LPAR ( fnParam ( COMMA fnParam )* )? RPAR
    //	            stmCompound
    private boolean fnDef() {
        Token returnType = tokens.getFirst();
        if(!(typeBase() || consume(TokenType.TKN_VOID_IDENT))) {
            return false;
        }

        Token functionName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            throw new Error("Missing identifier in declaration at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_LPAREN)) {
            tokens.addFirst(functionName);
            tokens.addFirst(returnType);
            return false;
        }

        List<String> fnParams = new ArrayList<>();
        List<Symbol> fnParamSymbols = new ArrayList<>();
        Symbol param;
        do {
            param = fnParam();
            if(param != null) {
                fnParamSymbols.add(param);
                fnParams.add(param.toString());
            }
        } while (consume(TokenType.TKN_COMMA));

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in function declaration at " + getLineAndColumnForError());
        }

        symbolTable.addSymbol(new Symbol(functionName.getValue(), returnType.getValue(), fnParams));

        if(!stmCompound(fnParamSymbols)) {
            throw new Error("Error in body of function at " + getLineAndColumnForError());
        }
        return true;
    }

    // rule: fnParam: typeBase ID arrayDecl?
    private Symbol fnParam() {
        Token type = tokens.getFirst();
        if(!typeBase()) {
            return null;
        }

        Token paramName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            throw new Error("Missing identifier in parameter declaration at " + getLineAndColumnForError());
        }

        int arrayElementValue = arrayDecl();

        // check that types are defined
        if(type.getType() == TokenType.TKN_IDENT) {
            if(!symbolTable.checkIfStructDefined(type.getValue())) {
                throw new Error("Unknown type '" + type.getValue() +  "' at " + getLineAndColumnForError());
            }
        }

        Symbol newVar;

        if(arrayElementValue >= 0)
            newVar = new Symbol(paramName.getValue(), type.getValue(), arrayElementValue);
        else
            newVar = new Symbol(paramName.getValue(), type.getValue());

        return newVar;
    }

    // rule: stmCompound: LACC ( varDef | stm )* RACC
    private boolean stmCompound(List<Symbol> fnParamSymbols) {
        if(!consume(TokenType.TKN_LACC)) {
            return false;
        }

        this.symbolTable.addDomain();
        if(fnParamSymbols != null) {
            for(Symbol fnParam : fnParamSymbols) {
                this.symbolTable.addSymbol(fnParam);
            }
        }

        while(varDef() || stm());

        if(!consume(TokenType.TKN_RACC)) {
            throw new Error("Missing '}' in statement at " + getLineAndColumnForError());
        }
        this.symbolTable.removeDomain();

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
        return stmCompound(null) || ifStm() || whileStm() || forStm() || breakStm() || returnStm() || exprStm();
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
            return false;
        }

        return true;
    }

    // rule: expr: exprAssign
    private boolean expr() {
        return exprAssing();
    }

    // rule: exprAssign: exprAssignable ASSIGN exprAssign | exprOr
    private boolean exprAssing() {
        if(exprAssignable()) {
            if(consume(TokenType.TKN_ASSIGN)) {
                if(checkNextToken(TokenType.TKN_SEMICOLON)) {
                    throw new Error("Missing value for assignment at " + getLineAndColumnForError());
                }
                if(exprAssing()) {
                    return true;
                }
            }
            else
                return false;
        }
        else if(exprOr()) {
            return true;
        }

        return false;
    }

    // rule: exprAssignable: ID ((LBRACKET exprOR  RBRACKET)? (DOT ID)?)*
    private boolean exprAssignable() {
        Token varName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            return false;
        }

        if(!symbolTable.checkIfDefined(varName.getValue())) {
            throw new Error("Undeclared identifier used at " + getLineAndColumnForError());
        }

        if(!checkNextToken(TokenType.TKN_LBRACKET) && !checkNextToken(TokenType.TKN_DOT)) {
            if(checkNextToken(TokenType.TKN_ASSIGN)) {
                return true;
            }

            tokens.addFirst(varName);
            return false;
        }

        while(true) {
            if(consume(TokenType.TKN_LBRACKET)) {
                if(!exprOr()) {
                    throw new Error("Wrong expression as array index at " + getLineAndColumnForError());
                }
                if(!consume(TokenType.TKN_RBRACKET)) {
                    throw new Error("Missing ']' after array index at " + getLineAndColumnForError());
                }
                continue;
            }
            if(consume(TokenType.TKN_DOT)) {
                if(!consume(TokenType.TKN_IDENT)) {
                    throw new Error("Expected identifier of struct member at " + getLineAndColumnForError());
                }
                continue;
            }
            break;
        }

        return true;
    }

    // rule: exprOr: exprOr OR exprAnd | exprAnd
    // rewritten: exprAnd exprOrAux
    private boolean exprOr() {
        if (!exprAnd()) {
            return false;
        }

        if(!exprOrAux()) {
            throw new Error("Wrong OR expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprOrAux: OR exprAnd exprOrAux | eps
    private boolean exprOrAux() {
        if(consume(TokenType.TKN_OR)) {
            if(exprAnd()) {
                if (exprOrAux()) {
                    return true;
                }
            }
            else throw new Error("Missing '||' in expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprAnd: exprAnd AND exprEq | exprEq
    // rewritten: exprAnd: exprEq exprAndAux
    private boolean exprAnd() {
        if(!exprEq()) {
            return false;
        }

        if(!exprAndAux()) {
            throw new Error("Wrong AND expression at " + getLineAndColumnForError());
        }

        return true;
    }


    // rule: exprAndAux: AND exprEq exprAndAux | eps
    private boolean exprAndAux() {
        if(consume(TokenType.TKN_AND)) {
            if(exprEq()) {
                if(exprAndAux()) {
                    return true;
                }
            } throw new Error("Missing '&&' in expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprEq: exprEq ( EQUAL | NOTEQ ) exprRel | exprRel
    // rewritten: exprEq: exprRel exprEqAux
    private boolean exprEq() {
        if(!exprRel()) {
            return false;
        }

        if(!exprEqAux()) {
            throw new Error("Wrong EQUAL expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprEqAux: (EQUAL | NOTEQ) exprRel exprEqAux | eps
    private boolean exprEqAux() {
        if(consume(TokenType.TKN_EQ) || consume(TokenType.TKN_NOT_EQ)) {
            if(exprRel()) {
                if(exprEqAux()) {
                    return true;
                }
            } else throw new Error("Missing '==' or '!=' in expression at  " + getLineAndColumnForError());
        }

        return true;
    }


    // rule: exprRel: exprRel ( LESS | LESSEQ | GREATER | GREATEREQ ) exprAdd | exprAdd
    // rewritten: exprRel: exprAdd exprRelAux
    private boolean exprRel() {
        if(!exprAdd()) {
            return false;
        }

        if(!exprRelAux()) {
            throw new Error("Wrong REL expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprRelAux: ( LESS | LESSEQ | GREATER | GREATEREQ ) exprAdd exprRelAux | eps
    private boolean exprRelAux() {
        if(consume(TokenType.TKN_LT) || consume(TokenType.TKN_GT) || consume(TokenType.TKN_LE) || consume(TokenType.TKN_GE)) {
            if(exprAdd()) {
                if(exprRelAux()) {
                    return true;
                }
            } throw new Error("Mission comparison in expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprAdd: exprAdd ( ADD | SUB ) exprMul | exprMul
    // rewritten: exprAdd: exprMul exprAddAux
    private boolean exprAdd() {
        if(!exprMul()) {
            return false;
        }

        if(!exprAddAux()) {
            throw new Error("Wrong ADD expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprAddAux: ( ADD | SUB ) exprMul exprAddAux | eps
    private boolean exprAddAux() {
        if(consume(TokenType.TKN_ADD) || consume(TokenType.TKN_SUB)) {
            if(exprMul()) {
                if(exprAddAux()) {
                    return true;
                }
            } else throw new Error("Missing '+' or '-' in expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprMul: exprMul ( MUL | DIV ) exprCast | exprCast
    // rewritten: exprMul: exprCast exprMulAux
    private boolean exprMul() {
        if(!exprCast()) {
            return false;
        }

        if(!exprMulAux()) {
            throw new Error("Wrong MUL expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprMulAux: ( MUL | DIV ) exprCast exprMulAux | eps
    private boolean exprMulAux() {
        if(consume(TokenType.TKN_MUL) || consume(TokenType.TKN_DIV)) {
            if(exprCast()) {
                if(exprMulAux()) {
                    return true;
                }
            } else throw new Error("Missing '*' or '/' in expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprCast: LPAR typeBase arrayCastType? RPAR exprCast | exprUnary
    private boolean exprCast() {
        Token firstToken = tokens.getFirst();
        if(!consume(TokenType.TKN_LPAREN)) {
            return exprUnary();
        }

        Token secondToken = tokens.getFirst();

        if(!typeBase()) {
            tokens.addFirst(firstToken);
            return exprUnary();
        }

        if(!arrayCastType()) {
            tokens.addFirst(secondToken);
            tokens.addFirst(firstToken);
            return exprUnary();
        }

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in expression cast at " + getLineAndColumnForError());
        }

        if(!exprCast()) {
            throw new Error("Wrong expr cast at " + getLineAndColumnForError());
        }

        return true;
    }

    // used to optionally allow [] as type cast
    private boolean arrayCastType() {
        Token firstToken = tokens.getFirst();

        if(!consume(TokenType.TKN_LBRACKET)) {
            return false;
        }

        if(!consume(TokenType.TKN_RBRACKET)) {
            tokens.addFirst(firstToken);
            return false;
        }

        return true;
    }

    // rule: exprUnary: ( SUB | NOT ) exprUnary | exprPostfix
    private boolean exprUnary() {
        if(!(consume(TokenType.TKN_SUB) || consume(TokenType.TKN_NOT))) {
            return exprPostfix();
        }

        if(!exprUnary()) {
            throw new Error("Wrong UNARY expr at " + getLineAndColumnForError());
        }
        return true;
    }


    // rule: exprPostfix: exprPostfix LBRACKET expr RBRACKET
    //	    | exprPostfix DOT ID
    //	    | exprPrimary
    // rewritten: exprPostFix: exprPrimary exprPostfixAux
    private boolean exprPostfix() {
        if(!exprPrimary()) {
            return false;
        }

        if(!exprPostfixAux()) {
            throw new Error("Wrong PRIMARY expression at " + getLineAndColumnForError());
        }

        return true;
    }

    // rule: exprPostfixAux: ( DOT ID exprPostfixAux ) | ( LBRACKET expr RBRACKET exrpPostfixAux) | eps
    private boolean exprPostfixAux() {
        if(consume(TokenType.TKN_DOT)) {
            if(!consume(TokenType.TKN_IDENT)) {
                throw new Error("Missing identifier for struct direct member access at " + getLineAndColumnForError());
            }

            if(exprPostfixAux()) {
                return true;
            }
        }
        else if(consume(TokenType.TKN_LBRACKET)) {
            if(!expr()) {
                throw new Error("Wrong PRIMARY expression at " + getLineAndColumnForError());
            }

            if(!consume(TokenType.TKN_RBRACKET)) {
                throw new Error("Missing ']' in expression postfix at " + getLineAndColumnForError());
            }

            if(exprPostfixAux()) {
                return true;
            }
        }
        return true;
    }

    // rule: exprPrimary: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )?
    //	    | CT_INT | CT_REAL | CT_CHAR | CT_STRING | LPAR expr RPAR
    private boolean exprPrimary() {
        return exprPrimaryAux1() || consume(TokenType.TKN_NUM_DEC) || consume(TokenType.TKN_NUM_HEX) || consume(TokenType.TKN_NUM_OCT)
                || consume(TokenType.TKN_REAL) || consume(TokenType.TKN_CHAR) || consume(TokenType.TKN_STR) || exprPrimaryAux2();
    }

    // rule: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )?
    private boolean exprPrimaryAux1() {
        Token idName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            return false;
        }

        if(!symbolTable.checkIfDefined(idName.getValue())) {
            throw new Error("Undefined identifier '" + idName.getValue() + "' at " + getLineAndColumnForError());
        }

        if(consume(TokenType.TKN_LPAREN)) {
            do {
                expr();
            } while(consume(TokenType.TKN_COMMA));

            if(!consume(TokenType.TKN_RPAREN)) {
                throw new Error("Missing ')' in expression primary at " + getLineAndColumnForError());
            }
        }

        return true;
    }

    // rule: LPAR expr RPAR
    private boolean exprPrimaryAux2() {
        if(!consume(TokenType.TKN_LPAREN)) {
            return false;
        }

        if(!expr()) {
            throw new Error("Wrong primary expression at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in primary expression at " + getLineAndColumnForError());
        }

        return true;
    }


}
