package syntSemAnalyzer.semantic;

import java.util.List;
import java.util.Objects;

public class Symbol {
    SymbolType symbolType;
    String name;
    String contentType;
    // for array
    int elementCount;
    // for struct and functions
    List<String> content;
    // for functions
    String returnType;

    public Symbol(String name, String contentType) {
        this.symbolType = SymbolType.SIMPLE;
        this.contentType = contentType;
        this.name = name;
    }

    public Symbol(String name, String contentType, int elementCount) {
        this.symbolType = SymbolType.ARRAY;
        this.name = name;
        this.contentType = contentType;
        this.elementCount = elementCount;
    }

    public Symbol(String name, List<String> fields) {
        this.symbolType = SymbolType.STRUCT;
        this.name = name;
        this.content = fields;
    }

    public Symbol(String name, String returnType, List<String> arguments) {
        this.symbolType = SymbolType.FUNCTION;
        this.name = name;
        this.content = arguments != null ? arguments.stream().map(arg -> arg.replace("{", "").replace("}", "")).toList() : null;
        this.returnType = returnType;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public String getName() {
        return name;
    }

    public String getContentType() throws WrongSymbolTypeException {
        if(this.symbolType != SymbolType.ARRAY && this.symbolType != SymbolType.SIMPLE) {
            throw new WrongSymbolTypeException("Content type is only for arrays and simple types");
        }
        return contentType;
    }

    public int getElementCount() {
        if(this.symbolType != SymbolType.ARRAY) {
            throw new WrongSymbolTypeException("Element count is only for arrays");
        }
        return elementCount;
    }

    public List<String> getArguments() {
        if(this.symbolType != SymbolType.FUNCTION) {
            throw new WrongSymbolTypeException("Arguments are only for functions");
        }
        return content;
    }

    public String getReturnType() {
        if(this.symbolType != SymbolType.FUNCTION) {
            throw new WrongSymbolTypeException("Return type is only for functions");
        }
        return returnType;
    }

    public List<String> getFields() {
        if(!isStructContent()) {
            throw new WrongSymbolTypeException("Fields are only for functions");
        }
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        if(symbol.getSymbolType() != this.getSymbolType() || !symbol.getName().equals(this.getName()))
            return false;

        if(this.getSymbolType() == SymbolType.FUNCTION) {
            if(symbol.getArguments().size() != this.getArguments().size()) return false;
            for(int i = 0; i < this.getArguments().size(); i++) {
                String[] arg1 = symbol.getArguments().get(i).split(":");
                String[] arg2 = this.getArguments().get(i).split(":");

                // compare the fields at 0: type of symbol (simple, array, struct, function)
                // and field 3: type of the argument (char, struct name etc)
                // in this way, we can check arguments by type even if they have different names or element count
                if(!arg1[0].equals(arg2[0]) || !arg1[2].equals(arg2[2])) return false;
            }
            return true;
        }
        else
            return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbolType, name, contentType, elementCount, content, returnType);
    }

    private String getFieldsString() {
        StringBuilder s = new StringBuilder();
        List<String> fields = this.getFields();

        if(fields == null) return "{}";

        s.append("{");
        for(String field: fields) {
            s.append(field);
            s.append(",");
        }
        s.append("}");

        return s.toString();
    }

    private String getArgsString() {
        StringBuilder s = new StringBuilder();
        List<String> args = this.getArguments();

        if(args == null) return "{}";

        s.append("{");
        for(String arg: args) {
            s.append(arg);
            s.append(",");
        }
        s.append("}");

        return s.toString();
    }

    @Override
    public String toString() {
        switch (symbolType) {
            case SIMPLE:
                return "{SIMPLE:" + name + ":" + contentType + "}";
            case ARRAY:
                return "{ARRAY:" + name + ":" + contentType + ":" + elementCount + "}";
            case FUNCTION:
                return "{FUNCTION:" + name + ":" + returnType + ":args:" + getArgsString() + "}";
            case STRUCT:
                return "{STRUCT:" + name + ":fields:" + getFieldsString() + "}";
        }

        return null;
    }

    public boolean isArray() {
        return symbolType == SymbolType.ARRAY;
    }

    public boolean isStructContent() {
        try {
            TypeBase tb = TypeBase.valueOf(contentType.toUpperCase());
            return false;
        } catch(Exception _) {
            return true;
        }
    }
}
