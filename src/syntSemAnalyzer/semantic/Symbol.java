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
        this.content = arguments;
        this.returnType = returnType;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public String getName() {
        return name;
    }

    public String getContentType() throws WrongSymbolTypeException {
        if(this.symbolType != SymbolType.ARRAY) {
            throw new WrongSymbolTypeException("Content type is only for arrays");
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
        if(this.symbolType != SymbolType.STRUCT) {
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
                if(!symbol.getArguments().get(i).equals(this.getArguments().get(i))) return false;
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

    @Override
    public String toString() {
        switch (symbolType) {
            case SIMPLE:
                return "{" + name + ":" + contentType + "}";
            case ARRAY:
                return "{" + name + ":" + contentType + ":" + elementCount + "}";
            case FUNCTION:
                return "{" + name + ":" + returnType + ":args:" + getArguments() + "}";
            case STRUCT:
                return "{" + name + ":fields:" + getFields() + "}";
        }

        return null;
    }
}
