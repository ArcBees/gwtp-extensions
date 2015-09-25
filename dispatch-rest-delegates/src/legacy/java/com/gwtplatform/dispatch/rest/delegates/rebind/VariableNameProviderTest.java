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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.gwtplatform.dispatch.rest.rebind.Parameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class VariableNameProviderTest {
    private static final String ACTION = "action";
    private static final String SOME_VARIABLE_NAME = "someVariableName";

    private VariableNameProvider variableNameProvider;

    @Before
    public void setUp() {
        this.variableNameProvider = new VariableNameProvider();
    }

    @Test
    public void getVariableName_defaultValueNotPresent_returnsDefaultValue() throws Exception {
        Parameter parameter = mock(Parameter.class);
        given(parameter.getVariableName()).willReturn(SOME_VARIABLE_NAME);
        List<Parameter> parameters = Collections.singletonList(parameter);

        // when
        String variableName = variableNameProvider.getVariableName(parameters, ACTION);

        // then
        assertEquals(ACTION, variableName);
    }

    @Test
    public void getVariableName_defaultValuePresent_returnsDefaultValue1() throws Exception {
        List<Parameter> parameters = getActionParameters(1);

        // when
        String variableName = variableNameProvider.getVariableName(parameters, ACTION);

        // then
        assertEquals(ACTION + 1, variableName);
    }

    @Test
    public void getVariableName_defaultValuePresentTwice_returnsDefaultValue2() throws Exception {
        List<Parameter> parameters = getActionParameters(2);

        // when
        String variableName = variableNameProvider.getVariableName(parameters, ACTION);

        // then
        assertEquals(ACTION + 2, variableName);
    }

    private List<Parameter> getActionParameters(int count) {
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (int i = 0; i < count; i++) {
            Parameter parameter = mockActionParameter(i);
            parameters.add(parameter);
        }

        return parameters;
    }

    private Parameter mockActionParameter(int index) {
        Parameter parameter = mock(Parameter.class);

        given(parameter.getVariableName()).willReturn(ACTION + (index == 0 ? "" : index));

        return parameter;
    }
}
