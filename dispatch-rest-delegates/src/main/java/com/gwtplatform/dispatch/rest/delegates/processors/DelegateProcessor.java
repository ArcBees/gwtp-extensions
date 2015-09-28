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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import com.gwtplatform.processors.tools.AbstractContextProcessor;
import com.gwtplatform.processors.tools.domain.Type;

public class DelegateProcessor extends AbstractContextProcessor<Delegate, Void> {
    private static final String TEMPLATE = "com/gwtplatform/dispatch/rest/delegates/processors/Delegate.vm";

    private final Set<Type> processedDelegateTypes;

    public DelegateProcessor(ProcessingEnvironment environment) {
        init(environment);

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
        outputter.withTemplateFile(TEMPLATE).writeTo(delegate.getType());
        processedDelegateTypes.add(delegate.getType());

        processSubDelegates(delegate);
    }

    private void processSubDelegates(Delegate delegate) {
        for (Delegate subDelegate : delegate.getSubDelegates()) {
            process(subDelegate);
        }
    }
}
