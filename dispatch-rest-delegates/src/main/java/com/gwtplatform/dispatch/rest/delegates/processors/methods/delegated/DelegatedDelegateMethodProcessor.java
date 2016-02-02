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

package com.gwtplatform.dispatch.rest.delegates.processors.methods.delegated;

import com.google.auto.service.AutoService;
import com.gwtplatform.dispatch.rest.delegates.processors.methods.DelegateMethod;
import com.gwtplatform.dispatch.rest.delegates.processors.methods.DelegateMethodProcessor;
import com.gwtplatform.dispatch.rest.processors.DispatchRestContextProcessor;
import com.gwtplatform.processors.tools.outputter.CodeSnippet;

import static com.gwtplatform.dispatch.rest.delegates.processors.DelegateResourceMethodProcessor.METHOD_SUFFIX;

@AutoService(DelegateMethodProcessor.class)
public class DelegatedDelegateMethodProcessor extends DispatchRestContextProcessor<DelegateMethod, CodeSnippet>
        implements DelegateMethodProcessor {
    private static final String TEMPLATE =
            "com/gwtplatform/dispatch/rest/delegates/processors/DelegatedDelegateMethod.vm";

    @Override
    public boolean canProcess(DelegateMethod context) {
        return context instanceof DelegatedDelegateMethod;
    }

    @Override
    public CodeSnippet process(DelegateMethod context) {
        DelegatedDelegateMethod method = (DelegatedDelegateMethod) context;

        return outputter.configure(TEMPLATE)
                .withParam("methodSuffix", METHOD_SUFFIX)
                .withParam("method", method.getMethod())
                .withParam("defaultReturnValue", method.getDefaultReturnValue())
                .withParam("resourceImplType", method.getParentType())
                .parse();
    }
}
