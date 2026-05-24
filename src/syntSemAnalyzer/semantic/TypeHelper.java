package syntSemAnalyzer.semantic;

public class TypeHelper {

    public static Type getArithType(Type a, Type b, boolean isCast) {

        if(a.isArray() && b.isArray()) {
            if(!a.getTypeName().equals(b.getTypeName())) {
                throw new Error("Cannot convert arrays of different types. " + a.getTypeName() + " into " + b.getTypeName());
            }

            return a;
        }
        else if(!a.isTypeBase() && !b.isTypeBase()) {
            if(!a.getTypeName().equals(b.getTypeName())) {
                throw new Error("Cannot convert incompatible struct types. " + a.getTypeName() + " into " + b.getTypeName());
            }
            return a;
        }
        else if(a.isTypeBase() && b.isTypeBase()) {
            if(isCast) {
                return b;
            }
            return a.getTypeBase().priority >= b.getTypeBase().priority ? a : b;
        }

        throw new Error("Incompatible types. " + a.getTypeName() + " into " + b.getTypeName());
    }
}
