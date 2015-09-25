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

import java.util.ArrayList;
import java.util.List;

import com.gwtplatform.dispatch.rest.rebind.resource.MethodDefinition;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.ClassDefinition;

public class DelegateDefinition extends ClassDefinition {
    private final ResourceDefinition resourceDefinition;
    private final List<MethodDefinition> methodDefinitions;

    public DelegateDefinition(
            String packageName,
            String className,
            ResourceDefinition resourceDefinition,
            List<MethodDefinition> methodDefinitions) {
        super(packageName, className);

        this.resourceDefinition = resourceDefinition;
        this.methodDefinitions = methodDefinitions;
    }

    public ResourceDefinition getResourceDefinition() {
        return resourceDefinition;
    }

    public List<MethodDefinition> getMethodDefinitions() {
        return new ArrayList<MethodDefinition>(methodDefinitions);
    }
}
