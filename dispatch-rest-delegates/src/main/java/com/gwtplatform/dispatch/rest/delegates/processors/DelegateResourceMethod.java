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

package com.gwtplatform.dispatch.rest.delegates.processors;

import java.util.Collection;

import javax.lang.model.element.ExecutableElement;

import com.google.common.base.Optional;
import com.gwtplatform.dispatch.rest.processors.details.EndPointDetails;
import com.gwtplatform.dispatch.rest.processors.endpoint.EndPoint;
import com.gwtplatform.dispatch.rest.processors.endpoint.EndPointMethod;
import com.gwtplatform.dispatch.rest.processors.endpoint.EndPointMethodFactory;
import com.gwtplatform.dispatch.rest.processors.resource.Resource;
import com.gwtplatform.dispatch.rest.processors.resource.ResourceMethod;
import com.gwtplatform.processors.tools.domain.Method;
import com.gwtplatform.processors.tools.utils.Primitives;

import static com.gwtplatform.processors.tools.utils.Primitives.VOID;
import static com.gwtplatform.processors.tools.utils.Primitives.findByPrimitive;

public class DelegateResourceMethod implements ResourceMethod {
    private final EndPointMethodFactory endPointMethodFactory;
    private final Resource parentResource;
    private final ExecutableElement element;

    private EndPointMethod endPointMethod;

    public DelegateResourceMethod(
            EndPointMethodFactory endPointMethodFactory,
            Resource parentResource,
            ExecutableElement element) {
        this.endPointMethodFactory = endPointMethodFactory;
        this.parentResource = parentResource;
        this.element = element;
    }

    @Override
    public Resource getParentResource() {
        return getEndPointMethod().getParentResource();
    }

    @Override
    public Method getMethod() {
        return getEndPointMethod().getMethod();
    }

    public EndPoint getEndPoint() {
        return getEndPointMethod().getEndPoint();
    }

    @Override
    public EndPointDetails getEndPointDetails() {
        return getEndPointMethod().getEndPointDetails();
    }

    public String getDefaultReturnValue() {
        Optional<Primitives> primitive = findByPrimitive(getMethod().getReturnType().getQualifiedParameterizedName());

        if (primitive.isPresent()) {
            if (primitive.get() == VOID) {
                return null;
            }

            return primitive.get().getDefaultValueLiteral();
        }

        return "null";
    }

    @Override
    public Collection<String> getImports() {
        return getEndPointMethod().getImports();
    }

    private EndPointMethod getEndPointMethod() {
        if (endPointMethod == null) {
            endPointMethod = endPointMethodFactory.create(parentResource, element);
        }

        return endPointMethod;
    }
}
