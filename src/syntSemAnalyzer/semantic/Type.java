package syntSemAnalyzer.semantic;

import java.util.Objects;

public class Type {
    public TypeBase typeBase;
    public int arrayCount;
    public String typeName;

    public Type(TypeBase typeBase) {
        this.typeBase = typeBase;
        this.arrayCount = -1;
        this.typeName = typeBase.toString();
    }

    public Type(String typeName) {
        this.typeName = typeName;
        try {
            this.typeBase = TypeBase.valueOf(typeName.toUpperCase());
        }
        catch (Exception e) {
            this.typeBase = null;
        }
        this.arrayCount = -1;
    }

    public Type(TypeBase typeBase, int arrayCount) {
        this.typeBase = null;
        this.arrayCount = arrayCount;
        this.typeName = typeBase.toString();
    }

    public Type(String typeName, int arrayCount) {
        this.typeName = typeName;
        this.typeBase = null;
        this.arrayCount = arrayCount;
    }

    public Type(Symbol s) {

        switch(s.getSymbolType()) {
            case SIMPLE:
                this.typeName = s.getContentType();
                try {
                    this.typeBase = TypeBase.valueOf(s.getContentType().toUpperCase());
                } catch (Exception e) {
                    this.typeBase = null;
                }
                this.arrayCount = -1;
                break;

            case ARRAY:
                this.typeName = s.getContentType();
                this.arrayCount = s.getElementCount();
                this.typeBase = null;
                break;

            case FUNCTION:
                this.typeName = s.getReturnType();
                this.arrayCount = s.getReturnType().contains("[") ? 0 : -1;
                try {
                    this.typeBase = TypeBase.valueOf(s.getContentType().toUpperCase());
                } catch (Exception e) {
                    this.typeBase = null;
                }
                break;

            case STRUCT:
                this.typeBase = null;
                this.typeName = s.getReturnType();
                this.arrayCount = -1;
                break;
        }
    }

    public TypeBase getTypeBase() {
        return typeBase;
    }

    public String getTypeName() {
        if(arrayCount > -1) {
            return typeName + "[]";
        }
        return typeName;
    }

    public boolean isArray() {
        return arrayCount > -1;
    }

    public boolean isTypeBase() {
        try {
            typeBase = TypeBase.valueOf(typeName.toUpperCase());
        }
        catch (Exception e) {
            typeBase = null;
        }

        return typeBase != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return  typeBase == type.typeBase && typeName.equalsIgnoreCase(type.typeName) && this.isArray() == type.isArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeBase, arrayCount, typeName);
    }

    @Override
    public String toString() {
        return "Type{" +
                "typeBase=" + typeBase +
                ", arrayCount=" + arrayCount +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
