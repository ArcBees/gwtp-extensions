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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class PresenterCollector extends AbstractReWriter{
    
    private static final Logger LOGGER = Logger.getGlobal();

    private HashMap<String, String> extendsMap = new HashMap<>();

    public Set<String> getPresenters() {
        return getAllClassesThatExtend(
                "com.gwtplatform.mvp.client.PresenterWidget",
                "com.gwtplatform.mvp.client.Presenter",
                "com.gwtplatform.mvp.client.TabContainerPresenter");
    }

    void processCompilationUnit() {
        for (TypeDeclaration t : getCompilationUnit().getTypes()) {
            if (t instanceof ClassOrInterfaceDeclaration) {
                processClassOrInterface(getCompilationUnit().getImports(),
                        getCompilationUnit().getPackage(),
                        (ClassOrInterfaceDeclaration) t);
            }
        }
    }

    private Set<String> getAllClassesThatExtend(String... clazzNames) {
        Set<String> result = new HashSet<>();
        for (String cName : clazzNames) {
            for (Entry<String, String> e : extendsMap.entrySet()) {
                if (e.getValue().equals(cName)) {
                    if (result.add(e.getKey())) {
                        result.addAll(getAllClassesThatExtend(e.getKey()));
                    }
                }
            }
        }
        return result;
    }

    private void processClassOrInterface(List<ImportDeclaration> imports,
            PackageDeclaration packageDeclaration,
            ClassOrInterfaceDeclaration cOrI) {
        if (imports == null) {
            return;
        }
        String fullyQualified = packageDeclaration.getName().toString() + "."
                + cOrI.getName();
        if (cOrI.getExtends() != null) {
            for (ClassOrInterfaceType ext : cOrI.getExtends()) {
                for (ImportDeclaration i : imports) {
                    if (i.getName().toString().endsWith("." + ext.getName())) {
                        extendsMap.put(fullyQualified, i.getName().toString());
                    }
                }
            }
        }
    }
}
