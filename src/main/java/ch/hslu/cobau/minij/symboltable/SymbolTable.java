package ch.hslu.cobau.minij.symboltable;

import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.type.IntegerType;
import ch.hslu.cobau.minij.ast.type.RecordType;
import ch.hslu.cobau.minij.ast.type.VoidType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private final Scope global;
    Map<String, Scope> scopes = new HashMap<>();
    Map<String, Function> functions = new HashMap<>();
    Map<String, Struct> records = new HashMap<>();

    public SymbolTable() {
        this.global = new Scope();
        functions.put("writeInt", new Function("writeInt", new VoidType(), List.of(new Declaration("i", new IntegerType(), false)), List.of()));
        functions.put("readInt", new Function("readInt", new IntegerType(), List.of(), List.of()));
        functions.put("writeChar", new Function("writeChar", new VoidType(), List.of(new Declaration("c", new IntegerType(), false)), List.of()));
        functions.put("readChar", new Function("readChar", new IntegerType(), List.of(), List.of()));
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
