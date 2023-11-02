/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.entity;

import ch.hslu.cobau.minij.ast.AstElement;
import ch.hslu.cobau.minij.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

public class Unit extends AstElement {
    private final List<Declaration> globals;
    private final List<Function> functions;
    private final List<Struct> structs;

    public Unit(List<Declaration> globals, List<Function> functions, List<Struct> structs) {
        Objects.requireNonNull(globals);
        Objects.requireNonNull(functions);
        Objects.requireNonNull(structs);

        this.globals = globals;
        this.functions = functions;
        this.structs = structs;
    }

    public List<Declaration> getGlobals() {
        return globals;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public List<Struct> getStructs() {
        return structs;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        globals.forEach(global -> global.accept(astVisitor));
        functions.forEach(procedure -> procedure.accept(astVisitor));
        structs.forEach(recordStructure -> recordStructure.accept(astVisitor));
    }
}
