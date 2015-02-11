/**
 * Copyright 2015 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.arcbees.gwtp.upgrader;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

public class ObjectSlotRewriter extends AbstractReWriter{
    
    private final static Logger LOGGER = Logger.getGlobal();
    
    private Map<String, Set<String>> slotNames;

    private boolean upgrade;

    public ObjectSlotRewriter(Map<String, Set<String>> slotNames, boolean upgrade) {
        this.slotNames = slotNames;
        this.upgrade = upgrade;
    }

    @Override
    void processCompilationUnit() {
        if (slotNames.containsKey(getEnclosingClassName())) {
            processNode(getCompilationUnit());
        }
    }

    private void processNode(Node node) {
        if (node instanceof FieldDeclaration) {
            FieldDeclaration fDec = (FieldDeclaration) node;
            for (VariableDeclarator v: fDec.getVariables()) {
                if (slotNames.get(getEnclosingClassName()).contains(v.getId().getName())) {
                    if (upgrade) {
                        addImports("com.gwtplatform.mvp.client.presenter.slots.Slot","com.gwtplatform.mvp.client.PresenterWidget");
                    }
                    Type t = fDec.getType();
                    ReferenceType nt = ASTHelper.createReferenceType(upgrade ? "Slot<PresenterWidget<?>>" : "Object", 0);
                    if (t instanceof ReferenceType) {
                        ReferenceType rt = (ReferenceType) t;
                        rt.setType(nt);
                    }
                    Expression scope = null;
                    if (v.getInit() instanceof ObjectCreationExpr) {
                        scope = ((ObjectCreationExpr) v.getInit()).getScope();
                    }
                    v.setInit(new ObjectCreationExpr(scope, new ClassOrInterfaceType(upgrade ? "Slot<PresenterWidget<?>>" : "Object"), null));
                    markChanged();
                }
            }
        }
        for (Node child: node.getChildrenNodes()) {
            processNode(child);
        }
    }
}
