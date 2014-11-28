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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.velocity.app.VelocityEngine;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracleException;
import com.gwtplatform.dispatch.rest.rebind.Parameter;
import com.gwtplatform.dispatch.rest.rebind.action.ActionContext;
import com.gwtplatform.dispatch.rest.rebind.action.ActionDefinition;
import com.gwtplatform.dispatch.rest.rebind.action.ActionGenerator;
import com.gwtplatform.dispatch.rest.rebind.resource.AbstractResourceMethodGenerator;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceMethodContext;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;
import com.gwtplatform.dispatch.rest.shared.RestAction;

import static com.gwtplatform.dispatch.rest.rebind.utils.Generators.getGenerator;

public class DelegateMethodGenerator extends AbstractResourceMethodGenerator {
    private static final String TEMPLATE_ACTION =
            "com/gwtplatform/dispatch/rest/delegates/rebind/DelegateRestActionMethod.vm";
    private static final String TEMPLATE_STUB =
            "com/gwtplatform/dispatch/rest/delegates/rebind/DelegateStubActionMethod.vm";
    private static final String ACTION_METHOD_SUFFIX = "$action";

    private final Set<ActionGenerator> actionGenerators;

    private DelegateMethodDefinition methodDefinition;
    private String template;

    @Inject
    DelegateMethodGenerator(
            Logger logger,
            GeneratorContext context,
            VelocityEngine velocityEngine,
            Set<ActionGenerator> actionGenerators) {
        super(logger, context, velocityEngine);

        this.actionGenerators = actionGenerators;
    }

    @Override
    public boolean canGenerate(ResourceMethodContext methodContext) throws UnableToCompleteException {
        setContext(methodContext);

        JType returnType = getMethod().getReturnType();

        return returnType.isClass() != null
                || returnType.isPrimitive() != null;
    }

    @Override
    public MethodDefinition generate(ResourceMethodContext methodContext) throws UnableToCompleteException {
        setContext(methodContext);

        List<Parameter> parameters = resolveParameters();
        List<Parameter> inheritedParameters = resolveInheritedParameters();
        JClassType resultType = parseResultType();
        String actionMethodName = getMethod().getName() + ACTION_METHOD_SUFFIX;

        methodDefinition = new DelegateMethodDefinition(getMethod(), parameters, inheritedParameters, resultType,
                actionMethodName);
        methodDefinition.addImport(RestAction.class.getName());

        generateAction();
        generateMethods();

        return methodDefinition;
    }

    @Override
    protected String getTemplate() {
        return template;
    }

    @Override
    protected void populateTemplateVariables(Map<String, Object> variables) {
        String resultTypeName = methodDefinition.getResultType().getParameterizedQualifiedSourceName();
        String returnValue = resolveReturnValue();

        List<Parameter> actionParameters = methodDefinition.getInheritedParameters();
        actionParameters.addAll(methodDefinition.getParameters());

        variables.put("resultType", resultTypeName);
        variables.put("returnType", getMethod().getReturnType().getParameterizedQualifiedSourceName());
        variables.put("returnValue", returnValue);
        variables.put("methodName", getMethod().getName());
        variables.put("actionMethodName", methodDefinition.getActionMethodName());
        variables.put("methodParameters", methodDefinition.getParameters());
        variables.put("actionParameters", actionParameters);
        variables.put("action", methodDefinition.getActionDefinitions().get(0));
    }

    private String resolveReturnValue() {
        String returnValue = null;
        JType returnType = getMethod().getReturnType();
        JPrimitiveType primitiveType = returnType.isPrimitive();

        if (primitiveType != null) {
            if (primitiveType != JPrimitiveType.VOID) {
                returnValue = primitiveType.getUninitializedFieldExpression();
            }
        } else {
            returnValue = "null";
        }

        return returnValue;
    }

    private void generateAction() throws UnableToCompleteException {
        ActionContext actionContext = new ActionContext(getMethodContext(), methodDefinition);
        ActionGenerator generator = getGenerator(getLogger(), actionGenerators, actionContext);
        ActionDefinition definition = generator.generate(actionContext);

        methodDefinition.addAction(definition);
    }

    private void generateMethods() throws UnableToCompleteException {
        StringWriter actionMethodWriter = new StringWriter();
        StringWriter stubActionWriter = new StringWriter();

        template = TEMPLATE_ACTION;
        mergeTemplate(actionMethodWriter);

        template = TEMPLATE_STUB;
        mergeTemplate(stubActionWriter);

        String stubMethodOutput = stubActionWriter.toString();
        methodDefinition.setStubOutput(stubMethodOutput);

        actionMethodWriter.append(stubMethodOutput);
        methodDefinition.setOutput(actionMethodWriter.toString());
    }

    private JClassType parseResultType() throws UnableToCompleteException {
        JType returnType = getMethod().getReturnType();
        JPrimitiveType primitiveType = returnType.isPrimitive();
        JClassType classType;

        if (primitiveType != null) {
            classType = convertPrimitiveToBoxed(primitiveType);
        } else {
            classType = returnType.isClass();
        }

        return classType;
    }

    private JClassType convertPrimitiveToBoxed(JPrimitiveType primitive) throws UnableToCompleteException {
        try {
            String boxedSourceName = primitive.getQualifiedBoxedSourceName();
            return getContext().getTypeOracle().parse(boxedSourceName).isClass();
        } catch (TypeOracleException e) {
            return getLogger().die("Unable to convert '" + primitive + "' to a boxed type.");
        }
    }
}
