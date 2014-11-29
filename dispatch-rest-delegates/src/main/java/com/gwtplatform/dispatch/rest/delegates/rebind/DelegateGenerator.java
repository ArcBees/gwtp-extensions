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
import com.gwtplatform.dispatch.rest.rebind.events.RegisterGinBindingEvent;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodGenerator;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.ClassDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.Generators;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;
import com.gwtplatform.dispatch.rest.shared.RestAction;

public class DelegateGenerator extends AbstractVelocityGenerator
        implements GeneratorWithInput<ResourceDefinition, DelegateDefinition> {
    static final String IMPL = "Delegate";

    private static final String TEMPLATE = "com/gwtplatform/dispatch/rest/delegates/rebind/Delegate.vm";

    private final EventBus eventBus;
    private final Set<MethodGenerator> methodGenerators;

    private ResourceDefinition resourceDefinition;
    private List<MethodDefinition> methodDefinitions;
    private Set<String> imports;

    @Inject
    DelegateGenerator(
            Logger logger,
            GeneratorContext context,
            VelocityEngine velocityEngine,
            EventBus eventBus,
            Set<MethodGenerator> methodGenerators) {
        super(logger, context, velocityEngine);

        this.eventBus = eventBus;
        this.methodGenerators = methodGenerators;
    }

    @Override
    public boolean canGenerate(ResourceDefinition resourceDefinition) throws UnableToCompleteException {
        this.resourceDefinition = resourceDefinition;

        return findType(getClassDefinition().getQualifiedName()) == null;
    }

    @Override
    public DelegateDefinition generate(ResourceDefinition resourceDefinition) throws UnableToCompleteException {
        this.resourceDefinition = resourceDefinition;

        imports = Sets.newTreeSet();
        imports.add(RestAction.class.getName());
        imports.add(resourceDefinition.getResourceInterface().getQualifiedSourceName());
        imports.add(resourceDefinition.getQualifiedName());

        methodDefinitions = Lists.newArrayList();

        generateMethods();

        PrintWriter printWriter = tryCreate();
        mergeTemplate(printWriter);
        commit(printWriter);

        registerGinBinding();

        return new DelegateDefinition(getPackageName(), getImplName(), resourceDefinition, methodDefinitions);
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
        variables.put("methods", methodDefinitions);
        variables.put("imports", imports);
    }

    private void generateMethods() throws UnableToCompleteException {
        for (MethodDefinition methodDefinition : resourceDefinition.getMethodDefinitions()) {
            generateMethod(methodDefinition);
        }
    }

    private void generateMethod(MethodDefinition methodDefinition) throws UnableToCompleteException {
        DelegatedMethodContext context = new DelegatedMethodContext(resourceDefinition, methodDefinition);
        MethodGenerator generator = Generators.findGenerator(getLogger(), methodGenerators, context);

        if (generator != null) {
            MethodDefinition delegatedDefinition = generator.generate(context);

            methodDefinitions.add(delegatedDefinition);
            imports.addAll(delegatedDefinition.getImports());
        } else {
            getLogger().die("Unable to find a delegated method generator for `%s#%s`",
                    resourceDefinition.getQualifiedName(), methodDefinition.getMethod().getName());
        }
    }

    private void registerGinBinding() throws UnableToCompleteException {
        JGenericType resourceDelegateType = getType(ResourceDelegate.class).isGenericType();
        JParameterizedType parameterizedResourceDelegateType = getContext().getTypeOracle().getParameterizedType(
                resourceDelegateType, new JClassType[]{resourceDefinition.getResourceInterface()});
        ClassDefinition definition = new ClassDefinition(parameterizedResourceDelegateType);

        RegisterGinBindingEvent.postSingleton(eventBus, definition, getClassDefinition());
    }
}
