package syntSemAnalyzer;

import lexer.Token;
import lexer.TokenType;
import syntSemAnalyzer.semantic.*;

import java.util.ArrayList;
import java.util.List;

public class SyntSemAnalyzer {
    List<Token> tokens;
    SymbolTable symbolTable;

    public SyntSemAnalyzer() {
        tokens = new ArrayList<>();
        symbolTable = new SymbolTable();
        symbolTable.addDomain(); // the global area
        // predefined functions
        symbolTable.addSymbol(new Symbol("put_s", "void", List.of("ARRAY:s:char:0")));
        symbolTable.addSymbol(new Symbol("get_s", "void", List.of("ARRAY:s:char:0")));
        symbolTable.addSymbol(new Symbol("put_i", "void", List.of("SIMPLE:i:int")));
        symbolTable.addSymbol(new Symbol("get_i ", "int", null));
        symbolTable.addSymbol(new Symbol("put_d", "void", List.of("SIMPLE:d:double")));
        symbolTable.addSymbol(new Symbol("get_d", "double", null));
        symbolTable.addSymbol(new Symbol("put_c", "void", List.of("SIMPLE:c:char")));
        symbolTable.addSymbol(new Symbol("get_c", "char", null));
        symbolTable.addSymbol(new Symbol("seconds", "double", null));
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
        if(typeBase() == null) {
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
        if(typeBase() == null) {
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
    private Type typeBase() {
        if(consume(TokenType.TKN_INT_IDENT)) {
            return new Type(TypeBase.INT);
        }

        if(consume(TokenType.TKN_DOUBLE_IDENT)) {
            return new Type(TypeBase.DOUBLE);
        }

        if(consume(TokenType.TKN_CHAR_IDENT)) {
            return new Type(TypeBase.CHAR);
        }

        if(consume(TokenType.TKN_BOOL_IDENT)) {
            return new Type(TypeBase.BOOL);
        }

        Token typeName = tokens.getFirst();
        if(consume(TokenType.TKN_IDENT)) {
            return new Type(typeName.getValue().toUpperCase());
        }

        return null;
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
        if(!(typeBase() != null || consume(TokenType.TKN_VOID_IDENT))) {
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

        Symbol newFunc = new Symbol(functionName.getValue(), returnType.getValue(), fnParams);
        if(!symbolTable.checkIfDefined(newFunc)) {
            symbolTable.addSymbol(newFunc);
        }
        else {
            throw new Error("Redefining function at " + getLineAndColumnForError());
        }

        if(!stmCompound(fnParamSymbols)) {
            throw new Error("Error in body of function at " + getLineAndColumnForError());
        }
        return true;
    }

    // rule: fnParam: typeBase ID arrayDecl?
    private Symbol fnParam() {
        Token type = tokens.getFirst();
        if(typeBase() == null) {
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

        if(expr() == null) {
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

        if(expr() == null) {
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
    private Type expr() {
        return exprAssign(null);
    }

    // rule: exprAssign: exprAssignable ASSIGN exprAssign | exprOr
    private Type exprAssign(Type expr) {
        Type assignee = exprAssignable();
        if(assignee != null) {
            if(consume(TokenType.TKN_ASSIGN)) {
                if(checkNextToken(TokenType.TKN_SEMICOLON)) {
                    throw new Error("Missing value for assignment at " + getLineAndColumnForError());
                }
                Type exprAssign = exprAssign(assignee);
                if(exprAssign != null) {
                    TypeHelper.getArithType(assignee, exprAssign, false);
                    return assignee;
                }
            }
            else
                TypeHelper.getArithType(assignee, expr, false);
                return assignee;
        }
        else {
            Type exprOr = exprOr();
            if(exprOr != null) {
                return exprOr;
            }
        }

        return null;
    }

    // rule: exprAssignable: ID ((LBRACKET exprOR  RBRACKET)? (DOT ID)?)*
    private Type exprAssignable() {
        Token varName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            return null;
        }

        Symbol s = symbolTable.getSymbolWithId(varName.getValue());
        if(s == null) {
            throw new Error("Undeclared identifier used at " + getLineAndColumnForError());
        }

        if(!checkNextToken(TokenType.TKN_LBRACKET) && !checkNextToken(TokenType.TKN_DOT)) {
            if(checkNextToken(TokenType.TKN_ASSIGN)) {
                return new Type(s);
            }

            tokens.addFirst(varName);
            return null;
        }

        Type t = new Type(s);
        boolean readArray = false;

        while(true) {
            if(consume(TokenType.TKN_LBRACKET)) {
                if(readArray) {
                    throw new Error("Multidimensional arrays are not permitted in this language at " + getLineAndColumnForError());
                }

                if(!s.isArray()) {
                    throw new Error("Trying to index non array at " + getLineAndColumnForError());
                }

                Type exprOr = exprOr();
                if(exprOr == null) {
                    throw new Error("Wrong expression as array index at " + getLineAndColumnForError());
                }

                if(!exprOr.isTypeBase() && exprOr.getTypeBase() != TypeBase.INT) {
                    throw new Error("Trying to index with non-integer type at " + getLineAndColumnForError());
                }

                if(!consume(TokenType.TKN_RBRACKET)) {
                    throw new Error("Missing ']' after array index at " + getLineAndColumnForError());
                }

                boolean isTypeBase = false;

                try {
                    TypeBase tb = TypeBase.valueOf(s.getContentType().toUpperCase());
                    isTypeBase = true;
                } catch(Exception _) {}

                // if typeBase is false, then we have a structure
                if(!isTypeBase) {
                    List<String> fields = symbolTable.getFieldsForStruct(s.getContentType());
                    s = new Symbol(s.getContentType(), fields);
                }
                else s = new Symbol("aux", s.getContentType());

                t = new Type(s.getContentType());
                readArray = true;
                continue;
            }
            if(consume(TokenType.TKN_DOT)) {
                Token fieldName = tokens.getFirst();
                if(!consume(TokenType.TKN_IDENT)) {
                    throw new Error("Expected identifier of struct member at " + getLineAndColumnForError());
                }

                if(!s.isStructContent()) {
                    throw new Error("Attempting to access member of non struct at " + getLineAndColumnForError());
                }

                String fieldInfo = symbolTable.getFieldForStruct(s.getContentType(), fieldName.getValue());
                if(fieldInfo == null) {
                    throw new Error("Struct type " + s.getName() + " has no field " + fieldName);
                }

                String[] split = fieldInfo.split(":");
                String type = split[0].replace("{", "");
                String contentType = split[2].replace("}", "");

                switch(SymbolType.valueOf(type)) {
                    case SymbolType.SIMPLE:
                        s = new Symbol("aux", contentType);
                        t = new Type(contentType);
                        break;
                    case ARRAY:
                        int count = Integer.parseInt(split[3].replace("}", ""));
                        s = new Symbol("aux", contentType, count);
                        t = new Type(contentType, count);
                        break;
                    case STRUCT:
                        String name = split[1];
                        List<String> fields = symbolTable.getFieldsForStruct(name);
                        s = new Symbol(name, fields);
                        t = new Type(name);
                        break;
                }

                readArray = false;
                continue;
            }
            break;
        }

        return t;
    }

    // rule: exprOr: exprOr OR exprAnd | exprAnd
    // rewritten: exprAnd exprOrAux
    private Type exprOr() {
        Type exprAnd = exprAnd();
        if (exprAnd == null) {
            return null;
        }

        Type exprOrAux = exprOrAux(exprAnd);
        if(exprOrAux == null) {
            throw new Error("Wrong OR expression at " + getLineAndColumnForError());
        }

        return TypeHelper.getArithType(exprAnd, exprOrAux, false);
    }

    // rule: exprOrAux: OR exprAnd exprOrAux | eps
    private Type exprOrAux(Type expr) {
        if(consume(TokenType.TKN_OR)) {
            Type exprAnd = exprAnd();
            if(exprAnd != null) {
                return TypeHelper.getArithType(exprAnd, exprOrAux(exprAnd), false);
            }
            else throw new Error("Missing '||' in expression at " + getLineAndColumnForError());
        }

        return expr;
    }

    // rule: exprAnd: exprAnd AND exprEq | exprEq
    // rewritten: exprAnd: exprEq exprAndAux
    private Type exprAnd() {
        Type exprEq = exprEq();
        if(exprEq == null) {
            return null;
        }

        Type exprAndAux = exprAndAux(exprEq);
        if(exprAndAux == null) {
            throw new Error("Wrong AND expression at " + getLineAndColumnForError());
        }

        return TypeHelper.getArithType(exprEq, exprAndAux, false);
    }


    // rule: exprAndAux: AND exprEq exprAndAux | eps
    private Type exprAndAux(Type expr) {
        if(consume(TokenType.TKN_AND)) {
            Type exprEq = exprEq();
            if(exprEq != null) {
                Type exprAndAux = exprAndAux(exprEq);
                if(exprAndAux != null) {
                    return TypeHelper.getArithType(exprAndAux, exprEq, false);
                }
            } throw new Error("Missing '&&' in expression at " + getLineAndColumnForError());
        }

        return expr;
    }

    // rule: exprEq: exprEq ( EQUAL | NOTEQ ) exprRel | exprRel
    // rewritten: exprEq: exprRel exprEqAux
    private Type exprEq() {
        Type exprRel = exprRel();
        if(exprRel == null) {
            return null;
        }

        Type exprEqAux = exprEqAux(exprRel);
        if(exprEqAux == null) {
            throw new Error("Wrong EQUAL expression at " + getLineAndColumnForError());
        }

        return TypeHelper.getArithType(exprRel, exprEqAux, false);
    }

    // rule: exprEqAux: (EQUAL | NOTEQ) exprRel exprEqAux | eps
    private Type exprEqAux(Type expr) {
        if(consume(TokenType.TKN_EQ) || consume(TokenType.TKN_NOT_EQ)) {
            Type exprRel = exprRel();
            if(exprRel != null) {
                Type exprEqAux = exprEqAux(exprRel);
                if(exprEqAux != null) {
                    return TypeHelper.getArithType(exprRel, exprEqAux, false);
                }
            } else throw new Error("Missing '==' or '!=' in expression at  " + getLineAndColumnForError());
        }

        return expr;
    }


    // rule: exprRel: exprRel ( LESS | LESSEQ | GREATER | GREATEREQ ) exprAdd | exprAdd
    // rewritten: exprRel: exprAdd exprRelAux
    private Type exprRel() {
        Type exprAdd = exprAdd();
        if(exprAdd == null) {
            return null;
        }

        Type exprRelAux = exprRelAux(exprAdd);
        if(exprRelAux == null) {
            throw new Error("Wrong REL expression at " + getLineAndColumnForError());
        }

        return TypeHelper.getArithType(exprAdd, exprRelAux, false);
    }

    // rule: exprRelAux: ( LESS | LESSEQ | GREATER | GREATEREQ ) exprAdd exprRelAux | eps
    private Type exprRelAux(Type expr) {
        if(consume(TokenType.TKN_LT) || consume(TokenType.TKN_GT) || consume(TokenType.TKN_LE) || consume(TokenType.TKN_GE)) {
            Type exprAdd = exprAdd();
            if(exprAdd != null) {
                Type exprRelAux = exprRelAux(exprAdd);
                if(exprRelAux != null) {
                    return TypeHelper.getArithType(exprAdd, exprRelAux, false);
                }
            } throw new Error("Mission comparison in expression at " + getLineAndColumnForError());
        }

        return expr;
    }

    // rule: exprAdd: exprAdd ( ADD | SUB ) exprMul | exprMul
    // rewritten: exprAdd: exprMul exprAddAux
    private Type exprAdd() {
        Type exprMul = exprMul();
        if(exprMul == null) {
            return null;
        }

        Type exprAddAux = exprAddAux(exprMul);
        if(exprAddAux == null) {
            throw new Error("Wrong ADD expression at " + getLineAndColumnForError());
        }

        return TypeHelper.getArithType(exprMul, exprAddAux, false);
    }

    // rule: exprAddAux: ( ADD | SUB ) exprMul exprAddAux | eps
    private Type exprAddAux(Type expr) {
        if(consume(TokenType.TKN_ADD) || consume(TokenType.TKN_SUB)) {
            Type exprMul = exprMul();
            if(exprMul != null) {
                Type exprAddAux = exprAddAux(exprMul);
                if(exprAddAux != null) {
                    return TypeHelper.getArithType(exprMul, exprAddAux, false);
                }
            } else throw new Error("Missing '+' or '-' in expression at " + getLineAndColumnForError());
        }

        return expr;
    }

    // rule: exprMul: exprMul ( MUL | DIV ) exprCast | exprCast
    // rewritten: exprMul: exprCast exprMulAux
    private Type exprMul() {
        Type exprCast = exprCast();
        if(exprCast == null) {
            return null;
        }

        Type exprMulAux = exprMulAux(exprCast);
        if(exprMulAux == null) {
            throw new Error("Wrong MUL expression at " + getLineAndColumnForError());
        }

        return TypeHelper.getArithType(exprCast, exprMulAux, false);
    }

    // rule: exprMulAux: ( MUL | DIV ) exprCast exprMulAux | eps
    private Type exprMulAux(Type expr) {
        if(consume(TokenType.TKN_MUL) || consume(TokenType.TKN_DIV)) {
            Type exprCast = exprCast();
            if(exprCast != null) {
                Type exprMulAux = exprMulAux(exprCast);
                if(exprMulAux != null) {
                    return TypeHelper.getArithType(exprCast, exprMulAux, false);
                }
            } else throw new Error("Missing '*' or '/' in expression at " + getLineAndColumnForError());
        }

        return expr;
    }

    // rule: exprCast: LPAR typeBase arrayCastType? RPAR exprCast | exprUnary
    private Type exprCast() {
        Token firstToken = tokens.getFirst();
        if(!consume(TokenType.TKN_LPAREN)) {
            return exprUnary();
        }

        Token secondToken = tokens.getFirst();

        Type tb = typeBase();
        if(tb == null) {
            tokens.addFirst(firstToken);
            return exprUnary();
        }

        boolean arrayCastType = arrayCastType();
        if(!arrayCastType) {
            tokens.addFirst(secondToken);
            tokens.addFirst(firstToken);
            return exprUnary();
        }

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in expression cast at " + getLineAndColumnForError());
        }

        if(exprCast() == null) {
            throw new Error("Wrong expr cast at " + getLineAndColumnForError());
        }

        if(arrayCastType()) {
            return new Type(tb.getTypeName(), 0);
        }
        return new Type(tb.getTypeName(), 0);
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
    private Type exprUnary() {
        if(!(consume(TokenType.TKN_SUB) || consume(TokenType.TKN_NOT))) {
            return exprPostfix();
        }

        Type exprUnary = exprUnary();
        if(exprUnary == null) {
            throw new Error("Wrong UNARY expr at " + getLineAndColumnForError());
        }
        return exprUnary;
    }


    // rule: exprPostfix: exprPostfix LBRACKET expr RBRACKET
    //	    | exprPostfix DOT ID
    //	    | exprPrimary
    // rewritten: exprPostFix: exprPrimary exprPostfixAux
    private Type exprPostfix() {
        Type exprPrimary = exprPrimary();
        if(exprPrimary == null) {
            return null;
        }

        Type exprPostfixAux = exprPostfixAux(exprPrimary);
        if(exprPostfixAux == null) {
            throw new Error("Wrong PRIMARY expression at " + getLineAndColumnForError());
        }

        return exprPostfixAux;
    }

    // rule: exprPostfixAux: ( DOT ID exprPostfixAux ) | ( LBRACKET expr RBRACKET exrpPostfixAux) | eps
    private Type exprPostfixAux(Type exprPrimary) {
        if(consume(TokenType.TKN_DOT)) {
            Type t = null;
            if(exprPrimary.isTypeBase() || exprPrimary.isArray()) {
                throw new Error("Cannot access modifier of type " + exprPrimary.getTypeName() + " at " + getLineAndColumnForError());
            }

            Token field = tokens.getFirst();
            if(!consume(TokenType.TKN_IDENT)) {
                throw new Error("Missing identifier for struct direct member access at " + getLineAndColumnForError());
            }

            List<String> fields = symbolTable.getFieldsForStruct(exprPrimary.getTypeName());

            String fieldInfo = symbolTable.getFieldForStruct(exprPrimary.getTypeName(), field.getValue());

            if(fieldInfo == null) {
                throw new Error("Struct type " + exprPrimary.getTypeName() + " has no field " + field);
            }

            String[] split = fieldInfo.split(":");
            String type = split[0];
            String contentType = split[2];

            switch(SymbolType.valueOf(type)) {
                case SymbolType.SIMPLE:
                    t = new Type(contentType);
                    break;
                case ARRAY:
                    int count = Integer.parseInt(split[3]);
                    t = new Type(contentType, count);
                    break;
                case STRUCT:
                    String name = split[1];
                    t = new Type(name);
                    break;
            }

            Type exprPostfixAux = exprPostfixAux(t);
            if(exprPostfixAux != null) {
                return exprPostfixAux;
            }
        }
        else if(consume(TokenType.TKN_LBRACKET)) {
            Type expr = expr();
            if(expr == null) {
                throw new Error("Wrong PRIMARY expression at " + getLineAndColumnForError());
            }

            if(!exprPrimary.isArray()) {
                throw new Error("Cannot index non array type at " + getLineAndColumnForError());
            }

            if(expr.isTypeBase() || expr.isArray() || expr.getTypeBase() != TypeBase.INT) {
                throw new Error("Attempting to index array with non number at " + getLineAndColumnForError());
            }

            if(!consume(TokenType.TKN_RBRACKET)) {
                throw new Error("Missing ']' in expression postfix at " + getLineAndColumnForError());
            }

            String type = exprPrimary.getTypeName().replace("[]", "");
            Type t = new Type(type);

            Type exprPostfixAux = exprPostfixAux(t);
            if(exprPostfixAux != null) {
                return exprPostfixAux;
            }
        }

        return exprPrimary;
    }

    // rule: exprPrimary: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )?
    //	    | CT_INT | CT_REAL | CT_CHAR | CT_STRING | LPAR expr RPAR
    private Type exprPrimary() {
        if(consume(TokenType.TKN_NUM_DEC)) {
            return new Type(TypeBase.INT);
        }
        if(consume(TokenType.TKN_NUM_HEX)) {
            return new Type(TypeBase.INT);
        }
        if(consume(TokenType.TKN_NUM_OCT)) {
            return new Type(TypeBase.INT);
        }
        if(consume(TokenType.TKN_REAL)) {
            return new Type(TypeBase.DOUBLE);
        }
        if(consume(TokenType.TKN_CHAR)) {
            return new Type(TypeBase.CHAR);
        }

        Token str = tokens.getFirst();
        if(consume(TokenType.TKN_STR)) {
            return new Type(TypeBase.CHAR, str.getValue().length());
        }

        Type ret1 = exprPrimaryAux1();
        if(ret1 != null) {
            return ret1;
        }

        Type ret2 = exprPrimaryAux2();
        if(ret2 != null) {
            return ret2;
        }

        return null;
    }

    // rule: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )?
    private Type exprPrimaryAux1() {
        Token idName = tokens.getFirst();
        if(!consume(TokenType.TKN_IDENT)) {
            return null;
        }

        Symbol s = symbolTable.getSymbolWithId(idName.getValue());
        if(!symbolTable.checkIfDefined(idName.getValue())) {
            throw new Error("Undefined identifier '" + idName.getValue() + "' at " + getLineAndColumnForError());
        }

        if(consume(TokenType.TKN_LPAREN)) {
            List<Type> types = new ArrayList<>();
            do {
                Type t = expr();
                types.add(t);
            } while(consume(TokenType.TKN_COMMA));

            if(!consume(TokenType.TKN_RPAREN)) {
                throw new Error("Missing ')' in expression primary at " + getLineAndColumnForError());
            }

            Symbol func = symbolTable.findFunctionWithSameParameters(idName.getValue(), types);
            if(func == null) {
                throw new Error("Found no function with these parameters at " + getLineAndColumnForError());
            }

            return new Type(func);
        }

        return new Type(s);
    }

    // rule: LPAR expr RPAR
    private Type exprPrimaryAux2() {
        if(!consume(TokenType.TKN_LPAREN)) {
            return null;
        }

        Type ret = expr();
        if(ret == null) {
            throw new Error("Wrong primary expression at " + getLineAndColumnForError());
        }

        if(!consume(TokenType.TKN_RPAREN)) {
            throw new Error("Missing ')' in primary expression at " + getLineAndColumnForError());
        }

        return ret;
    }
}
