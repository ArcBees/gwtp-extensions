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
import com.gwtplatform.dispatch.rest.rebind.action.ActionDefinition;
import com.gwtplatform.dispatch.rest.rebind.action.ActionMethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodContext;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;

public class DelegatedActionMethodGenerator extends AbstractDelegatedMethodGenerator {
    private static final String TEMPLATE = "com/gwtplatform/dispatch/rest/delegates/rebind/DelegatedActionMethod.vm";

    @Inject
    DelegatedActionMethodGenerator(
            Logger logger,
            GeneratorContext context,
            VelocityEngine velocityEngine) {
        super(logger, context, velocityEngine);
    }

    @Override
    public boolean canGenerate(MethodContext context) throws UnableToCompleteException {
        return context instanceof DelegatedMethodContext
                && ((DelegatedMethodContext) context).getMethodDefinition() instanceof ActionMethodDefinition;
    }

    @Override
    public MethodDefinition generate(MethodContext context) throws UnableToCompleteException {
        // TODO: Don't replace into other output
        setContext(context);

        StringBuilder outputBuilder = new StringBuilder(getMethodDefinition().getOutput());
        StringWriter writer = new StringWriter();
        MethodDefinition delegateDefinition = new MethodDefinition(getMethodDefinition());

        mergeTemplate(writer);
        replaceMethodContent(outputBuilder, writer.toString());

        delegateDefinition.setOutput(outputBuilder.toString());

        return delegateDefinition;
    }

    @Override
    protected void populateTemplateVariables(Map<String, Object> variables) {
        ActionDefinition actionDefinition = getMethodDefinition().getActionDefinitions().get(0);
        JClassType resultType = actionDefinition.getResultType();

        variables.put("resultType", resultType.getParameterizedQualifiedSourceName());
        variables.put("methodName", getMethod().getName());
        variables.put("parameters", getMethodDefinition().getParameters());
    }

    @Override
    protected ActionMethodDefinition getMethodDefinition() {
        return (ActionMethodDefinition) super.getMethodDefinition();
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }
}
