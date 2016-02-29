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

package com.gwtplatform.dispatch.rest.delegates.processors.methods;

import java.util.ServiceLoader;

import com.gwtplatform.dispatch.rest.processors.resource.ResourceMethod;
import com.gwtplatform.processors.tools.exceptions.UnableToProcessException;
import com.gwtplatform.processors.tools.logger.Logger;
import com.gwtplatform.processors.tools.utils.Utils;

public class DelegateMethodFactories {
    private static final String NO_FACTORIES_FOUND = "Can not find a factory to handle the delegate method.";

    private static ServiceLoader<DelegateMethodFactory> factories;

    private final Logger logger;
    private final Utils utils;

    public DelegateMethodFactories(
            Logger logger,
            Utils utils) {
        this.logger = logger;
        this.utils = utils;

        if (factories == null) {
            initFactories();
        }
    }

    private void initFactories() {
        assert factories == null;
        factories = ServiceLoader.load(DelegateMethodFactory.class, getClass().getClassLoader());

        for (DelegateMethodFactory factory : factories) {
            factory.init(logger, utils);
        }
    }

    public DelegateMethod process(ResourceMethod method) {
        for (DelegateMethodFactory factory : factories) {
            if (factory.canCreate(method)) {
                return factory.create(method);
            }
        }

        logger.error().context(method.getMethod().getElement()).log(NO_FACTORIES_FOUND);
        throw new UnableToProcessException();
    }
}
