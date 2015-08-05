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

package com.gwtplatform.dispatch.rest.delegates.rebind;

import java.util.List;

import com.gwtplatform.dispatch.rest.rebind.Parameter;

public class VariableNameProvider {
    public String getVariableName(List<Parameter> parameters, String defaultName) {
        return getVariableName(parameters, defaultName, 0);
    }

    private String getVariableName(List<Parameter> parameters, final String defaultName, final int index) {
        final String suffix = index == 0 ? "" : String.valueOf(index);

        String variableName = defaultName + suffix;
        String existingVariable = tryFind(parameters, variableName);

        if (existingVariable != null) {
            return getVariableName(parameters, defaultName, index + 1);
        }

        return variableName;
    }

    private String tryFind(List<Parameter> parameters, String value) {
        for (Parameter parameter : parameters) {
            if (value.equals(parameter.getVariableName())) {
                return value;
            }
        }

        return null;
    }
}
