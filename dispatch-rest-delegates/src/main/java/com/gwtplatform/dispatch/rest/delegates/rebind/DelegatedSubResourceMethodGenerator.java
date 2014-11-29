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

import java.io.StringWriter;
import java.util.Map;

import javax.inject.Inject;

import org.apache.velocity.app.VelocityEngine;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodContext;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.subresource.SubResourceMethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.ClassDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;

public class DelegatedSubResourceMethodGenerator extends AbstractDelegatedMethodGenerator {
    private static final String TEMPLATE =
            "com/gwtplatform/dispatch/rest/delegates/rebind/DelegatedSubResourceMethod.vm";

    private ClassDefinition subDelegateTypeDefinition;
    private ClassDefinition subResourceTypeDefinition;

    @Inject
    DelegatedSubResourceMethodGenerator(
            Logger logger,
            GeneratorContext context,
            VelocityEngine velocityEngine) {
        super(logger, context, velocityEngine);
    }

    @Override
    public boolean canGenerate(MethodContext context) throws UnableToCompleteException {
        return context instanceof DelegatedMethodContext
                && ((DelegatedMethodContext) context).getMethodDefinition() instanceof SubResourceMethodDefinition;
    }

    @Override
    public MethodDefinition generate(MethodContext context) throws UnableToCompleteException {
        setContext(context);

        resolveTypeDefinitions();

        StringWriter writer = new StringWriter();
        mergeTemplate(writer);

        MethodDefinition delegateDefinition = new MethodDefinition(getMethodDefinition());
        delegateDefinition.addImport(subResourceTypeDefinition.getQualifiedName());
        delegateDefinition.addImport(subDelegateTypeDefinition.getQualifiedName());
        delegateDefinition.setOutput(writer.toString());

        return delegateDefinition;
    }

    @Override
    protected void populateTemplateVariables(Map<String, Object> variables) {
        variables.put("subResourceType", subResourceTypeDefinition.getParameterizedClassName());
        variables.put("subDelegateType", subDelegateTypeDefinition.getParameterizedClassName());
        variables.put("methodName", getMethod().getName());
        variables.put("parameters", getMethodDefinition().getParameters());
    }

    @Override
    protected SubResourceMethodDefinition getMethodDefinition() {
        return (SubResourceMethodDefinition) super.getMethodDefinition();
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    private void resolveTypeDefinitions() {
        ResourceDefinition subResourceDefinition = getMethodDefinition().getResourceDefinitions().get(0);
        JClassType subResourceInterface = subResourceDefinition.getResourceInterface();

        subResourceTypeDefinition = new ClassDefinition(subResourceInterface);
        subDelegateTypeDefinition = new ClassDefinition(subResourceTypeDefinition.getPackageName(),
                subResourceTypeDefinition.getClassName() + DelegateGenerator.IMPL);
    }
}
