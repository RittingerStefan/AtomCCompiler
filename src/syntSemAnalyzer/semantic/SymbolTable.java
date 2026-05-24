package syntSemAnalyzer.semantic;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    List<List<Symbol>> symbolTable;

    public SymbolTable() {
        this.symbolTable = new ArrayList<>();
    }

    public void addSymbol(Symbol symbol) {
        this.symbolTable.getLast().add(symbol);
    }

    public void addDomain() {
        this.symbolTable.add(new ArrayList<>());
    }

    public void removeDomain() {
        this.symbolTable.removeLast();
    }

    public boolean checkIfDefined(Symbol symbol) {
        for(List<Symbol> domain : this.symbolTable.reversed()) {
            if(domain.contains(symbol)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkIfDefined(String name) {
        for(List<Symbol> domain : this.symbolTable.reversed()) {
            for(Symbol symbol : domain) {
                if(symbol.getName().equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkIfDefinedInCurrentDomain(Symbol symbol) {
        return this.symbolTable.getLast().contains(symbol);
    }

    @Override
    public String toString() {
        StringBuilder table = new StringBuilder();
        for(List<Symbol> domain : this.symbolTable) {
            for(Symbol symbol : domain) {
                table.append(symbol.toString());
                table.append(", ");
            }
            table.append("\n");
        }
        return table.toString();
    }

    public boolean checkIfStructDefined(String value) {
        for(List<Symbol> domain : this.symbolTable.reversed()) {
            for(Symbol symbol : domain) {
                if(symbol.getName().equals(value) && symbol.getSymbolType() == SymbolType.STRUCT) {
                    return true;
                }
            }
        }

        return false;
    }

    public Symbol getSymbolWithId(String id) {
        for(List<Symbol> domain : this.symbolTable.reversed()) {
            for(Symbol symbol : domain.reversed()) {
                if(symbol.getName().equals(id)) {
                    return symbol;
                }
            }
        }
        return null;
    }

    public Symbol findFunctionWithSameParameters(String id, List<Type> types) {
        for(List<Symbol> domain : this.symbolTable.reversed()) {
            for(Symbol symbol : domain.reversed()) {
                if(symbol.getName().equals(id)) {
                    List<Type> arguments = createTypesArrayFromArgs(symbol.getArguments());
                    if(compareArguments(arguments, types)) {
                        return symbol;
                    }
                }
            }
        }
        return null;
    }

    private List<Type> createTypesArrayFromArgs(List<String> arguments) {
        List<Type> types = new ArrayList<>();

        for(String argument: arguments) {
            String[] split = argument.split(":");
            String type = split[0].replace("{", "");
            String name = split[1];
            String contentType = split[2].replace("}", "");

            Type t = null;

            switch(SymbolType.valueOf(type)) {
                case SymbolType.STRUCT:
                    t = new Type(name);
                    break;
                case SIMPLE:
                    try {
                        TypeBase b = TypeBase.valueOf(contentType.toUpperCase());
                        t = new Type(b);
                    } catch(Exception e) {
                        t = new Type(contentType);
                    }
                    break;
                case ARRAY:
                    int count = Integer.parseInt(split[3].replace("}", ""));
                    try {
                        TypeBase b = TypeBase.valueOf(contentType.toUpperCase());
                        t = new Type(b, count);
                    } catch(Exception e) {
                        t = new Type(contentType, count);
                    }
                    break;
            }

            if(t == null) {
                throw new Error("Error parsing the aguments list of function. List is " + arguments + ".");
            }

            types.add(t);
        }

        return types;
    }

    private boolean compareArguments(List<Type> arguments, List<Type> types) {
        if(arguments.size() != types.size()) {
            return false;
        }

        for(int i = 0; i < arguments.size(); i++) {
            if(!arguments.get(i).equals(types.get(i))) {
                return false;
            }
        }
        return true;
    }

    public List<String> getFieldsForStruct(String name) {
        for(List<Symbol> domain : this.symbolTable.reversed()) {
            for(Symbol symbol : domain) {
                if(symbol.getName().equals(name)) {
                    return symbol.getFields();
                }
            }
        }
        throw new Error("Struct " + name + " not defined.");
    }

    public String getFieldForStruct(String structName, String fieldName) {
        if(!checkIfDefined(structName)) {
            throw new WrongSymbolTypeException("Fields are only for structs");
        }

        List<String> fields = getFieldsForStruct(structName);

        if(fields == null) {
            return null;
        }

        for(String f: fields) {
            String[] split = f.split(":");
            String name = split[1];

            if(name.equals(fieldName)) {
                return f;
            }
        }

        return null;
    }
}
