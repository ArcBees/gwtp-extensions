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

package com.gwtplatform.dispatch.rest.delegates.processors.methods.subresource;

import java.util.Collection;

import com.gwtplatform.dispatch.rest.delegates.processors.methods.DelegateMethod;
import com.gwtplatform.dispatch.rest.processors.details.Method;
import com.gwtplatform.dispatch.rest.processors.subresource.SubResourceMethod;
import com.gwtplatform.processors.tools.domain.Type;

import static com.gwtplatform.dispatch.rest.delegates.processors.Delegate.DELEGATE_SUFFIX;

public class SubResourceDelegateMethod implements DelegateMethod {
    private final SubResourceMethod method;

    private Type subDelegateType;

    public SubResourceDelegateMethod(SubResourceMethod method) {
        this.method = method;
    }

    public Type getSubDelegateType() {
        if (subDelegateType == null) {
            Type subResourceType = method.getSubResource().getType();
            subDelegateType =
                    new Type(subResourceType.getPackageName(), subResourceType.getSimpleName() + DELEGATE_SUFFIX);
        }

        return subDelegateType;
    }

    @Override
    public Method getMethod() {
        return method.getMethod();
    }

    @Override
    public Collection<String> getImports() {
        return method.getImports();
    }
}
