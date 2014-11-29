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

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.gwtplatform.dispatch.rest.rebind.AbstractGenerator;
import com.gwtplatform.dispatch.rest.rebind.extension.ExtensionContext;
import com.gwtplatform.dispatch.rest.rebind.extension.ExtensionGenerator;
import com.gwtplatform.dispatch.rest.rebind.extension.ExtensionPoint;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.ClassDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;

public class DelegateExtensionGenerator extends AbstractGenerator implements ExtensionGenerator {
    private final DelegateGenerator delegateGenerator;

    private List<ClassDefinition> definitions;

    @Inject
    DelegateExtensionGenerator(
            Logger logger,
            GeneratorContext context,
            DelegateGenerator delegateGenerator) {
        super(logger, context);

        this.delegateGenerator = delegateGenerator;
    }

    @Override
    public boolean canGenerate(ExtensionContext context) {
        return context.getExtensionPoint() == ExtensionPoint.AFTER_RESOURCES
                && !context.getResourceDefinitions().isEmpty();
    }

    @Override
    public Collection<ClassDefinition> generate(ExtensionContext context) throws UnableToCompleteException {
        definitions = Lists.newArrayList();

        for (ResourceDefinition resourceDefinition : context.getResourceDefinitions()) {
            maybeGenerateDelegate(resourceDefinition);
        }

        return definitions;
    }

    private void maybeGenerateDelegate(ResourceDefinition resourceDefinition) throws UnableToCompleteException {
        if (delegateGenerator.canGenerate(resourceDefinition)) {
            DelegateDefinition definition = delegateGenerator.generate(resourceDefinition);
            definitions.add(definition);
        }
    }
}
