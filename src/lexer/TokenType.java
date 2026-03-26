package lexer;

public enum TokenType {
    TKN_EOF("TKN_EOF"),
    TKN_COMMA("TKN_COMMA"),
    TKN_COLON("TKN_COLON"),
    TKN_SEMICOLON("TKN_SEMICOLON"),
    TKN_LBRACKET("TKN_LBRACKET"),
    TKN_RBRACKET("TKN_RBRACKET"),
    TKN_LACC("TKN_LACC"),
    TKN_RACC("TKN_RACC"),
    TKN_LPAREN("TKN_LPAREN"),
    TKN_RPAREN("TKN_RPAREN"),
    TKN_ADD("TKN_ADD"),
    TKN_SUB("TKN_SUB"),
    TKN_MUL("TKN_MUL"),
    TKN_DIV("TKN_DIV"),
    TKN_DOT("TKN_DOT"),
    TKN_ASSIGN("TKN_ASSIGN"),
    TKN_EQ("TKN_EQ"),
    TKN_NOT_EQ("TKN_NOT_EQ"),
    TKN_LT("TKN_LT"),
    TKN_GT("TKN_GT"),
    TKN_LE("TKN_LE"),
    TKN_NOT("TKN_NOT"),
    TKN_GE("TKN_GE"),
    TKN_UNK("TKN_UNK");

    String type;
    TokenType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
