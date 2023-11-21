package ch.hslu.cobau.minij.symboltable;


import ch.hslu.cobau.minij.ast.type.Type;

public record Symbol(String identifier, Entity entity, Type type) {
}
