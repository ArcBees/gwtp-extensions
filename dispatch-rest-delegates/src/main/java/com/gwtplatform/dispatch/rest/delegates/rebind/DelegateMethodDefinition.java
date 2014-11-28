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

import java.util.List;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.gwtplatform.dispatch.rest.rebind.Parameter;
import com.gwtplatform.dispatch.rest.rebind.action.ActionMethodDefinition;

public class DelegateMethodDefinition extends ActionMethodDefinition {
    private final String actionMethodName;

    private String stubOutput;

    public DelegateMethodDefinition(
            JMethod method,
            List<Parameter> parameters,
            List<Parameter> inheritedParameters,
            JClassType resultType,
            String actionMethodName) {
        super(method, parameters, inheritedParameters, resultType);

        this.actionMethodName = actionMethodName;
    }

    public String getActionMethodName() {
        return actionMethodName;
    }

    public void setStubOutput(String stubOutput) {
        this.stubOutput = stubOutput;
    }

    public String getStubOutput() {
        return stubOutput;
    }
}
