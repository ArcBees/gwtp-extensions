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

import org.apache.velocity.app.VelocityEngine;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.gwtplatform.dispatch.rest.rebind.AbstractVelocityGenerator;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodContext;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.resource.MethodGenerator;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;

public abstract class AbstractDelegatedMethodGenerator extends AbstractVelocityGenerator implements MethodGenerator {
    private DelegatedMethodContext context;

    protected AbstractDelegatedMethodGenerator(
            Logger logger,
            GeneratorContext context,
            VelocityEngine velocityEngine) {
        super(logger, context, velocityEngine);
    }

    @Override
    public byte getPriority() {
        // This really need to run before core method generators
        return (byte) (super.getPriority() - 5);
    }

    protected void setContext(MethodContext context) {
        this.context = (DelegatedMethodContext) context;
    }

    protected ResourceDefinition getResourceDefinition() {
        return context.getResourceDefinition();
    }

    protected MethodDefinition getMethodDefinition() {
        return context.getMethodDefinition();
    }

    protected JMethod getMethod() {
        return getMethodDefinition().getMethod();
    }

    @Override
    protected String getImplName() {
        return getResourceDefinition() + DelegateGenerator.IMPL + "#" + getMethod().getName();
    }

    @Override
    protected String getPackageName() {
        return getResourceDefinition().getPackageName();
    }
}
