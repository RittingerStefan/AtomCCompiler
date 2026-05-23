package syntSemAnalyzer.semantic;

public class Type {
    public TypeBase typeBase;
    public int arrayCount;
    public String typeName;

    public Type(TypeBase typeBase, int arrayCount) {
        this.typeBase = typeBase;
        this.arrayCount = arrayCount;
        this.typeName = typeBase.toString();
    }

    public Type(String typeName, int arrayCount) {
        this.typeName = typeName;
        this.typeBase = null;
        this.arrayCount = arrayCount;
    }

    public TypeBase getTypeBase() {
        return typeBase;
    }

    public int getArrayCount() {
        return arrayCount;
    }

    public String getTypeName() {
        if(arrayCount > -1) {
            return typeName + "[]";
        }
        return typeName;
    }
}
