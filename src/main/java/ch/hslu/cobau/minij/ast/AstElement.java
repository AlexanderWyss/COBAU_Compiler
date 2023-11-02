/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast;

public abstract class AstElement {
    public abstract void accept(AstVisitor astVisitor);
    public void visitChildren(AstVisitor astVisitor) { }
}
