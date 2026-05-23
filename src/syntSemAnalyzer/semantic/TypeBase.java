package syntSemAnalyzer.semantic;

public enum TypeBase {
    DOUBLE(0),
    INT(1),
    CHAR(2),
    BOOL(3);

    final int priority;
    private TypeBase(int priority) {
        this.priority =  priority;
    }

    public int getPriority() {
        return priority;
    }

    public String toString() {
        return name();
    }
}
