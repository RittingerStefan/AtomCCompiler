package lexer;

public enum State {
    STATE_START,
    STATE_DIV,
    STATE_COMMENT,
    STATE_NOT,
    STATE_ASSIGN,
    STATE_LESS,
    STATE_GREATER,
    STATE_AND_AUX,
    STATE_OR_AUX,
}
