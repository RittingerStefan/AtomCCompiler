package syntSemAnalyzer.semantic;

public class TypeHelper {

    private static boolean isTypeBase(Type t) {
        return t.getTypeBase() == null;
    }

    private static boolean isArrayType(Type t) {
        return t.arrayCount > -1;
    }

    public static Type getArithType(Type a, Type b, boolean isCast) {

        if(!a.getTypeName().equals(b.getTypeName())) {
            throw new Error("Trying to convert symbols of different types. " + a.getTypeName() + " into " + a.getTypeName());
        }

        if(isArrayType(a) && isArrayType(b)) {
            if(!a.getTypeName().equals(b.getTypeName())) {
                throw new Error("Cannot convert arrays of different types. " + a.getTypeName() + " into " + a.getTypeName());
            }

            return a;
        }
        else if(!isTypeBase(a) && !isTypeBase(b)) {
            if(!a.getTypeName().equals(b.getTypeName())) {
                throw new Error("Cannot convert incompatible struct types. " + a.getTypeName() + " into " + a.getTypeName());
            }
            return a;
        }
        else if(isTypeBase(a) && isTypeBase(b)) {
            if(isCast) {
                return b;
            }
            return a.getTypeBase().priority >= b.getTypeBase().priority ? a : b;
        }

        throw new Error("Incompatible types. " + a.getTypeName() + " into " + a.getTypeName());
    }
}
