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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.gwtplatform.dispatch.rest.processors.resource.Resource;
import com.gwtplatform.dispatch.rest.processors.resource.ResourceMethod;
import com.gwtplatform.dispatch.rest.processors.subresource.SubResource;
import com.gwtplatform.dispatch.rest.processors.subresource.SubResourceMethod;
import com.gwtplatform.processors.tools.domain.HasImports;
import com.gwtplatform.processors.tools.domain.HasType;
import com.gwtplatform.processors.tools.domain.Type;

public class Delegate implements HasType, HasImports {
    private static final String SIMPLE_NAME_SUFFIX = "$$Delegate";

    private final Resource resource;

    private Type type;
    private List<Delegate> subDelegates;

    public Delegate(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Type getType() {
        if (type == null) {
            Type resourceType = resource.getType();

            type = new Type(
                    resourceType.getPackageName(),
                    resourceType.getEnclosingNames(),
                    resourceType.getSimpleName() + SIMPLE_NAME_SUFFIX,
                    resourceType.getTypeArguments());
        }

        return type;
    }

    public List<Delegate> getSubDelegates() {
        if (subDelegates == null) {
            processSubResources();
        }

        return subDelegates;
    }

    private void processSubResources() {
        subDelegates = new ArrayList<>();

        for (ResourceMethod resourceMethod : resource.getMethods()) {
            processResourceMethod(resourceMethod);
        }

        subDelegates = Collections.unmodifiableList(subDelegates);
    }

    private void processResourceMethod(ResourceMethod resourceMethod) {
        if (resourceMethod instanceof SubResourceMethod) {
            SubResource subResource = ((SubResourceMethod) resourceMethod).getSubResource();
            Delegate subDelegate = new Delegate(subResource);

            subDelegates.add(subDelegate);
        }
    }

    @Override
    public Collection<String> getImports() {
        return type.getImports();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", getType())
                .add("subDelegates", getSubDelegates())
                .toString();
    }
}
