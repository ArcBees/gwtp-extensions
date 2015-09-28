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

import javax.annotation.processing.ProcessingEnvironment;

import com.google.auto.service.AutoService;
import com.gwtplatform.dispatch.rest.delegates.processors.methods.DelegateMethodFactories;
import com.gwtplatform.dispatch.rest.processors.resource.ResourcePostProcessor;
import com.gwtplatform.dispatch.rest.processors.resource.RootResource;
import com.gwtplatform.processors.tools.logger.Logger;
import com.gwtplatform.processors.tools.utils.Utils;

@AutoService(ResourcePostProcessor.class)
public class DelegateResourcePostProcessor implements ResourcePostProcessor {
    private DelegateProcessor delegateProcessor;
    private DelegateMethodFactories delegateMethodFactories;

    @Override
    public void init(ProcessingEnvironment environment) {
        Logger logger = new Logger(environment.getMessager(), environment.getOptions());
        Utils utils = new Utils(environment.getTypeUtils(), environment.getElementUtils());

        delegateMethodFactories = new DelegateMethodFactories(logger, utils);
        delegateProcessor = new DelegateProcessor(environment);
    }

    @Override
    public void postProcess(RootResource resource) {
        Delegate delegate = new Delegate(delegateMethodFactories, resource);

        delegateProcessor.process(delegate);
    }
}
