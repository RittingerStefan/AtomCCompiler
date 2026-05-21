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
}
