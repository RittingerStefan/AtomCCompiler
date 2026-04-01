package lexer;

public class Token {
    private String value;
    private TokenType type;
    private int line;
    private int column;

    public Token(String value, TokenType type, int line, int column) {
        this.value = value;
        this.type = type;
        this.line = line;
        this.column = column;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Token [value=" + value + ", type=" + type + "] at " + line + ":" + column;
    }
}
