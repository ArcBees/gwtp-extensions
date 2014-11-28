/**
 * Copyright 2014 ArcBees Inc.
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

package com.gwtplatform.dispatch.rest.delegates.rebind;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.velocity.app.VelocityEngine;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.gwtplatform.dispatch.rest.client.ResourceDelegate;
import com.gwtplatform.dispatch.rest.rebind.AbstractVelocityGenerator;
import com.gwtplatform.dispatch.rest.rebind.GeneratorWithInput;
import com.gwtplatform.dispatch.rest.rebind.Parameter;
import com.gwtplatform.dispatch.rest.rebind.action.ActionDefinition;
import com.gwtplatform.dispatch.rest.rebind.action.ActionMethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.events.RegisterGinBindingEvent;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.subresource.SubResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.subresource.SubResourceMethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.ClassDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;
import com.gwtplatform.dispatch.rest.shared.RestAction;

public class DelegateGenerator extends AbstractVelocityGenerator
        implements GeneratorWithInput<ResourceDefinition, DelegateDefinition> {
    private static final String TEMPLATE = "com/gwtplatform/dispatch/rest/delegates/rebind/Delegate.vm";
    private static final String IMPL = "Delegate";

    private final EventBus eventBus;

    private ResourceDefinition resourceDefinition;
    private Set<String> imports;
    private List<String> methods;

    @Inject
    DelegateGenerator(
            Logger logger,
            GeneratorContext context,
            VelocityEngine velocityEngine,
            EventBus eventBus) {
        super(logger, context, velocityEngine);

        this.eventBus = eventBus;
    }

    @Override
    public boolean canGenerate(ResourceDefinition resourceDefinition) throws UnableToCompleteException {
        this.resourceDefinition = resourceDefinition;

        return findType(getClassDefinition().getQualifiedName()) == null;
    }

    @Override
    public DelegateDefinition generate(ResourceDefinition resourceDefinition) throws UnableToCompleteException {
        this.resourceDefinition = resourceDefinition;

        JClassType resourceInterface = resourceDefinition.getResourceInterface();

        imports = Sets.newHashSet(RestAction.class.getName(), resourceInterface.getQualifiedSourceName(),
                resourceDefinition.getQualifiedName());

        generateMethods();

        PrintWriter printWriter = tryCreate();
        mergeTemplate(printWriter);
        commit(printWriter);

        registerGinBinding();

        return new DelegateDefinition(getPackageName(), getImplName());
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPackageName() {
        return resourceDefinition.getPackageName();
    }

    @Override
    protected String getImplName() {
        return resourceDefinition.getResourceInterface().getName() + IMPL;
    }

    @Override
    protected void populateTemplateVariables(Map<String, Object> variables) {
        JClassType resourceInterface = resourceDefinition.getResourceInterface();

        variables.put("resourceType", resourceInterface.getSimpleSourceName());
        variables.put("methods", methods);
        variables.put("imports", imports);
    }

    private void generateMethods() {
        // TODO: Create method generators for all 3 kinds, w/ higher priority, ensure input is DelegateDefinition

        methods = Lists.newArrayList();

        for (MethodDefinition definition : resourceDefinition.getMethodDefinitions()) {
            imports.addAll(definition.getImports());

            if (definition instanceof DelegateMethodDefinition) {
                generateMethod((DelegateMethodDefinition) definition);
            } else if (definition instanceof ActionMethodDefinition) {
                generateMethod((ActionMethodDefinition) definition);
            } else {
                generateMethod((SubResourceMethodDefinition) definition);
            }
        }
    }

    private void generateMethod(DelegateMethodDefinition definition) {
        StringBuilder outputBuilder = new StringBuilder(definition.getStubOutput());
        StringBuilder executeBuilder = generateExecuteAction(definition, definition.getActionMethodName());

        int braceIndex = outputBuilder.indexOf("{");
        outputBuilder.insert(braceIndex + 1, executeBuilder);

        methods.add(outputBuilder.toString());
    }

    private void generateMethod(ActionMethodDefinition definition) {
        StringBuilder outputBuilder = new StringBuilder(definition.getOutput());
        StringBuilder executeBuilder = generateExecuteAction(definition, definition.getMethod().getName())
                .append("\n        return action;");

        replaceMethodContent(outputBuilder, executeBuilder);

        methods.add(outputBuilder.toString());
    }

    private StringBuilder generateExecuteAction(ActionMethodDefinition definition, String methodName) {
        ActionDefinition actionDefinition = definition.getActionDefinitions().get(0);

        StringBuilder executeBuilder = new StringBuilder("\n        RestAction<")
                .append(actionDefinition.getResultType().getParameterizedQualifiedSourceName())
                .append("> action = ");

        appendResourceVariable(executeBuilder, definition)
                .append(".")
                .append(methodName);

        return appendParameters(executeBuilder, definition)
                .append("        execute(action);");
    }

    private void generateMethod(SubResourceMethodDefinition definition) {
        SubResourceDefinition subResourceDefinition =
                (SubResourceDefinition) definition.getResourceDefinitions().get(0);
        ClassDefinition subDelegateDefinition = new ClassDefinition(subResourceDefinition.getPackageName(),
                subResourceDefinition.getResourceInterface().getSimpleSourceName() + IMPL);

        imports.add(subDelegateDefinition.getQualifiedName());

        StringBuilder outputBuilder = new StringBuilder(definition.getOutput());

        StringBuilder subDelegateBuilder = new StringBuilder().append("\n        ")
                .append(definition.getMethod().getReturnType().getParameterizedQualifiedSourceName())
                .append(" subResource = ")
                .append("resource.")
                .append(definition.getMethod().getName());

        appendParameters(subDelegateBuilder, definition)
                .append("        ")
                .append(subDelegateDefinition.getClassName())
                .append(" delegate = new ")
                .append(subDelegateDefinition.getClassName())
                .append("(dispatcher, subResource);\n")
                .append("\n")
                .append("        copyFields(delegate);\n")
                .append("\n")
                .append("        return delegate;");

        replaceMethodContent(outputBuilder, subDelegateBuilder);

        methods.add(outputBuilder.toString());
    }

    private StringBuilder appendParameters(StringBuilder executeBuilder, MethodDefinition definition) {
        List<Parameter> parameters = definition.getParameters();

        executeBuilder.append("(");

        for (Parameter parameter : parameters) {
            executeBuilder.append(parameter.getVariableName()).append(", ");
        }

        if (!parameters.isEmpty()) {
            int length = executeBuilder.length();
            executeBuilder.delete(length - 2, length);
        }

        return executeBuilder.append(");\n");
    }

    private StringBuilder appendResourceVariable(StringBuilder builder, ActionMethodDefinition definition) {
        if (definition instanceof DelegateMethodDefinition) {
            builder.append("((")
                    .append(resourceDefinition.getClassName())
                    .append(") resource)");
        } else {
            builder.append("resource");
        }

        return builder;
    }

    private void replaceMethodContent(StringBuilder methodBuilder, StringBuilder newContentBuilder) {
        int openBraceIndex = methodBuilder.indexOf("{");
        int closeBraceIndex = methodBuilder.lastIndexOf("}");

        newContentBuilder.append("\n    ");

        methodBuilder.replace(openBraceIndex + 1, closeBraceIndex, newContentBuilder.toString());
    }

    private void registerGinBinding() throws UnableToCompleteException {
        JGenericType resourceDelegateType = getType(ResourceDelegate.class).isGenericType();
        JParameterizedType parameterizedResourceDelegateType = getContext().getTypeOracle().getParameterizedType(
                resourceDelegateType, new JClassType[]{resourceDefinition.getResourceInterface()});
        ClassDefinition definition = new ClassDefinition(parameterizedResourceDelegateType);

        RegisterGinBindingEvent.postSingleton(eventBus, definition, getClassDefinition());
    }
}
