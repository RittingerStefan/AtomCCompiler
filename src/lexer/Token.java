package lexer;

import java.util.Objects;

public class Token {
    private final String value;
    private final TokenType type;
    private final int line;
    private final int column;

    public Token(String value, TokenType type, int line, int column) {
        this.value = value;
        this.type = type;
        this.line = line;
        this.column = column;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return type == token.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        return "Token [value=" + value + ", type=" + type + "] at " + line + ":" + column;
    }
}
