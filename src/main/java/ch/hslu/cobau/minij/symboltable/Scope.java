package ch.hslu.cobau.minij.symboltable;

import ch.hslu.cobau.minij.ast.AstElement;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.type.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Scope {
    private final String identifier;
    private final Scope parent;
    private final Map<String, Declaration> symbols = new HashMap<>();

    public Scope(String identifier, Scope parent) {
        this.identifier = identifier;
        this.parent = parent;
    }

    public Scope() {
        this("global", null);
    }

    public boolean addSymbol(String identifier, Declaration element) {
        if (hasSymbol(identifier)) {
            return false;
        }
        symbols.put(identifier, element);
        return true;
    }

    public boolean hasSymbol(String identifier) {
        Scope currentScope = this;
        do {
            if (currentScope.symbols.containsKey(identifier)) {
                return true;
            }
            currentScope = currentScope.parent;
        } while (currentScope != null);
        return false;
    }

    public Scope getParent() {
        return parent;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Declaration getSymbol(String identifier) {
        Scope currentScope = this;
        do {
            if (currentScope.symbols.containsKey(identifier)) {
                return currentScope.symbols.get(identifier);
            }
            currentScope = currentScope.parent;
        } while (currentScope != null);
        return null;
    }
}
