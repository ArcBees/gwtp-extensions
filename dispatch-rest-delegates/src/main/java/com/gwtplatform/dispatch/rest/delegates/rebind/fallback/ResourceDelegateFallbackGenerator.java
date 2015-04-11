/*
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

package com.gwtplatform.dispatch.rest.delegates.rebind.fallback;

import java.io.PrintWriter;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.gwtplatform.dispatch.rest.delegates.client.ResourceDelegate;

public class ResourceDelegateFallbackGenerator extends Generator {
    private TreeLogger logger;
    private GeneratorContext context;
    private TypeOracle typeOracle;
    private String typeName;

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName)
            throws UnableToCompleteException {
        this.logger = logger;
        this.context = context;
        this.typeName = typeName;
        this.typeOracle = context.getTypeOracle();

        try {
            tryGenerateDelegate();
            return typeName + "Impl";
        } catch (NotFoundException e) {
            logger.log(Type.ERROR, "Can't find resource delegate." , e);
        }

        throw new UnableToCompleteException();
    }

    private void tryGenerateDelegate() throws NotFoundException, UnableToCompleteException {
        JClassType resourceDelegateType = typeOracle.getType(ResourceDelegate.class.getName());

        JClassType type = typeOracle.getType(typeName);
        JClassType[] implementedInterfaces = type.getImplementedInterfaces();

        for (JClassType implementedInterface : implementedInterfaces) {
            if (implementedInterface.isAssignableTo(resourceDelegateType)) {
                doGenerate(type, implementedInterface);
                return;
            }
        }

        throw new UnableToCompleteException();
    }

    private void doGenerate(JClassType type, JClassType implementedInterface) {
        JClassType resourceType = extractResourceType(implementedInterface);

        String packageName = type.getPackage().getName();
        String className = type.getSimpleSourceName() + "Impl";

        PrintWriter printWriter = context.tryCreate(logger, packageName, className);
        if (printWriter != null) {
            try {
                compose(type, resourceType, packageName, className, printWriter);
            } finally {
                printWriter.close();
            }
        }
    }

    private JClassType extractResourceType(JClassType implementedInterface) {
        JParameterizedType parameterized = implementedInterface.isParameterized();
        JClassType[] typeArgs = parameterized.getTypeArgs();
        return typeArgs[0];
    }

    private void compose(JClassType type, JClassType resourceType, String packageName, String className,
            PrintWriter printWriter) {
        ClassSourceFileComposerFactory composer
                = new ClassSourceFileComposerFactory(packageName, className);
        composer.addImplementedInterface(type.getName());
        composer.setSuperclass(resourceType.getQualifiedSourceName() + "Delegate");

        SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);
        sourceWriter.commit(logger);
    }
}
