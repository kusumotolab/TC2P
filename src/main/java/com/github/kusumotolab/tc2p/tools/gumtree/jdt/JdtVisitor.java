/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com> *
 */


package com.github.kusumotolab.tc2p.tools.gumtree.jdt;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JdtVisitor extends AbstractJdtVisitor {

    public JdtVisitor() {
        super();
    }

    @Override
    public void preVisit(ASTNode n) {
        if (isMethodName(n)) {
            return;
        }
        pushNode(n, getLabel(n));
    }
    protected String getLabel(ASTNode n) {
        if (n instanceof Name) {
            return ((Name) n).getFullyQualifiedName();
        }
        if (n instanceof Type) {
            return n.toString();
        }
        if (n instanceof Modifier) {
            return n.toString();
        }
        if (n instanceof StringLiteral) {
            return ((StringLiteral) n).getEscapedValue();
        }
        if (n instanceof NumberLiteral) {
            return ((NumberLiteral) n).getToken();
        }
        if (n instanceof CharacterLiteral) {
            return ((CharacterLiteral) n).getEscapedValue();
        }
        if (n instanceof BooleanLiteral) {
            return ((BooleanLiteral) n).toString();
        }
        if (n instanceof InfixExpression) {
            return ((InfixExpression) n).getOperator().toString();
        }
        if (n instanceof PrefixExpression) {
            return ((PrefixExpression) n).getOperator().toString();
        }
        if (n instanceof PostfixExpression) {
            return ((PostfixExpression) n).getOperator().toString();
        }
        if (n instanceof Assignment) {
            return ((Assignment) n).getOperator().toString();
        }
        if (n instanceof MethodInvocation) {
            return ((MethodInvocation) n).getName().toString();
        }
        if (n instanceof TextElement) {
            return n.toString();
        }
        if (n instanceof TagElement) {
            return ((TagElement) n).getTagName();
        }
        if (n instanceof TypeDeclaration) {
            return ((TypeDeclaration) n).isInterface() ? "interface" : "class";
        }

        return "";
    }

    @Override
    public boolean visit(final SimpleName node) {
        return false;
    }

    @Override
    public boolean visit(TagElement e) {
        return true;
    }

    @Override
    public boolean visit(QualifiedName name) {
        return false;
    }

    @Override
    public void postVisit(ASTNode n) {
        if (isMethodName(n)) {
            return;
        }
        popNode();
    }

    private boolean isMethodName(final ASTNode n) {
        return n instanceof SimpleName
            && n.getParent() instanceof MethodInvocation
            && ((MethodInvocation) n.getParent()).getName().equals(n);
    }

}
