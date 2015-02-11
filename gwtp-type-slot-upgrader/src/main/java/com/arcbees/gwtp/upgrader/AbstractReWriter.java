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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.QualifiedNameExpr;

public abstract class AbstractReWriter {
    private final static Logger LOGGER = Logger.getGlobal();
    private CompilationUnit compilationUnit;
    private List<ImportDeclaration> imports;
    private boolean hasChanged;
    private String enclosingClassName;

    public final void processJavaFile(File file) {
        CompilationUnit cu;
        try {
            cu = JavaParser.parse(file);
            if (processCompilationUnit(cu)) {
                LOGGER.info("Rewriting: " + file.getName());
                reWrite(file, cu);
            }
        } catch (ParseException | IOException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void addImports(String... names) {
        for (String name : names) {
            removeImport(name);
        }
        if (imports == null) {
            compilationUnit.setImports(new ArrayList<ImportDeclaration>());
            imports = compilationUnit.getImports();
        }
        for (String name : names) {
            imports.add(new ImportDeclaration(ASTHelper.createNameExpr(name), false, false));
            markChanged();
        }
    }

    protected Set<String> getFullyQualifiedName(String name) {
        Set<String> result = new HashSet<>();
        if (imports != null) {
            for (ImportDeclaration id : imports) {
                String iName = id.getName().toString();
                if (id.isAsterisk()) {
                    result.add(iName.replace("*", name));
                } else {
                    if (name.contains(".")) {
                        String[] names = name.split("\\.");
                        if (iName.endsWith("." + names[0]) || iName.equals(names[0])) {
                            for (int i = 1; i < names.length; i++) {
                                iName = iName + "." + names[i];
                            }
                            result.clear();
                            result.add(iName);
                            return result;
                        }
                    } else if (iName.endsWith("." + name) || iName.equals(name)) {
                        result.clear();
                        result.add(iName);
                        return result;
                    }
                }
            }
        }
        if (result.isEmpty()) {
            result.add(name);
        }
        return result;
    }

    protected void removeImport(String name) {
        if (imports != null) {
            Iterator<ImportDeclaration> it = imports.iterator();
            while (it.hasNext()) {
                if (it.next().getName().toString().equals(name)) {
                    it.remove();
                    markChanged();
                }
            }
        }
    }

    protected CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    protected void markChanged() {
        hasChanged = true;
    }

    protected String getEnclosingClassName() {
        if (enclosingClassName == null) {
            for (Node node : compilationUnit.getChildrenNodes()) {
                if (node instanceof ClassOrInterfaceDeclaration) {
                    QualifiedNameExpr packageName = (QualifiedNameExpr) compilationUnit.getPackage().getName();
                    String fqName = packageName.getQualifier() + "." + packageName.getName() + "." + ((ClassOrInterfaceDeclaration) node).getName();
                    enclosingClassName = fqName;
                    break;
                }
            }
        }
        return enclosingClassName;
    }

    abstract void processCompilationUnit();

    final boolean processCompilationUnit(CompilationUnit cu) {
        this.enclosingClassName = null;
        this.hasChanged = false;
        this.compilationUnit = cu;
        this.imports = compilationUnit.getImports();
        processCompilationUnit();
        return hasChanged();
    };

    boolean hasChanged() {
        return hasChanged;
    }

    private void reWrite(File file, CompilationUnit cu) {
        try {
            FileUtils.writeStringToFile(file, cu.toString());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
