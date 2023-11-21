package ch.hslu.cobau.minij.symboltable;

import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.type.RecordType;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Scope global;
    Map<String, Scope> scopes = new HashMap<>();
    Map<String, Function> functions = new HashMap<>();
    Map<String, Struct> records = new HashMap<>();

    public SymbolTable() {
        this.global = new Scope();
    }

    public Scope getScope(String identifier) {
        return scopes.get(identifier);
    }

    public Scope addScope(String identifier, Scope parent) {
        Scope scope = new Scope(identifier, parent);
        scopes.put(identifier, scope);
        return scope;
    }

    public Scope getGlobal() {
        return global;
    }

    public boolean addFunction(Function function) {
        if (functions.containsKey(function.getIdentifier())) {
            return false;
        }
        functions.put(function.getIdentifier(), function);
        return true;
    }

    public boolean hasFunction(String identifier) {
        return functions.containsKey(identifier);
    }

    public Function getFunction(String identifier) {
        return functions.get(identifier);
    }

    public boolean addRecordType(Struct record) {
        if (records.containsKey(record.getIdentifier())) {
            return false;
        }
        records.put(record.getIdentifier(), record);
        return true;
    }

    public boolean hasRecordType(String identifier) {
        return records.containsKey(identifier);
    }

    public Struct getRecordType(String identifier) {
        return records.get(identifier);
    }
}
