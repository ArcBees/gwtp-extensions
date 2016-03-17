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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import com.gwtplatform.dispatch.rest.delegates.client.ResourceDelegate;
import com.gwtplatform.dispatch.rest.delegates.processors.methods.DelegateMethodProcessors;
import com.gwtplatform.processors.tools.AbstractContextProcessor;
import com.gwtplatform.processors.tools.bindings.BindingContext;
import com.gwtplatform.processors.tools.bindings.BindingsProcessors;
import com.gwtplatform.processors.tools.domain.Type;
import com.gwtplatform.processors.tools.logger.Logger;
import com.gwtplatform.processors.tools.outputter.CodeSnippet;
import com.gwtplatform.processors.tools.outputter.Outputter;
import com.gwtplatform.processors.tools.utils.Utils;

import static com.gwtplatform.dispatch.rest.processors.NameUtils.findRestModuleType;
import static com.gwtplatform.processors.tools.bindings.BindingContext.newBinding;

public class DelegateProcessor extends AbstractContextProcessor<Delegate, Void> {
    private static final String TEMPLATE = "com/gwtplatform/dispatch/rest/delegates/processors/Delegate.vm";

    private final BindingsProcessors bindingsProcessors;
    private final DelegateMethodProcessors delegateMethodProcessors;
    private final Set<Type> processedDelegateTypes;

    public DelegateProcessor(
            Logger logger,
            Utils utils,
            Outputter outputter) {
        init(logger, utils, outputter);

        bindingsProcessors = new BindingsProcessors(logger, utils, outputter);
        delegateMethodProcessors = new DelegateMethodProcessors(logger, utils, outputter);
        processedDelegateTypes = new HashSet<>();
    }

    @Override
    public Void process(Delegate delegate) {
        if (!processedDelegateTypes.contains(delegate.getType())) {
            doProcess(delegate);
        }

        return null;
    }

    private void doProcess(Delegate delegate) {
        Type type = delegate.getType();
        List<CodeSnippet> processedMethods = delegateMethodProcessors.processAll(delegate.getMethods());

        outputter.configure(TEMPLATE)
                .withParam("isRootResource", delegate.isRootResource())
                .withParam("resourceType", delegate.getResourceType())
                .withParam("methods", processedMethods)
                .writeTo(type);
        processedDelegateTypes.add(type);

        createBinding(delegate);
        processSubDelegates(delegate);
    }

    private void createBinding(Delegate delegate) {
        if (delegate.isRootResource()) {
            Type superType = new Type(ResourceDelegate.class, Arrays.asList(delegate.getResourceType()));
            BindingContext bindingContext =
                    newBinding(findRestModuleType(utils), superType, delegate.getType(), Singleton.class);

            bindingsProcessors.process(bindingContext);
        }
    }

    private void processSubDelegates(Delegate delegate) {
        delegate.getSubDelegates().forEach(this::process);
    }
}
