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

import java.util.List;
import java.util.ServiceLoader;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.gwtplatform.processors.tools.exceptions.UnableToProcessException;
import com.gwtplatform.processors.tools.logger.Logger;
import com.gwtplatform.processors.tools.outputter.CodeSnippet;
import com.gwtplatform.processors.tools.outputter.Outputter;
import com.gwtplatform.processors.tools.utils.Utils;

public class DelegateMethodProcessors {
    private static final String NO_PROCESSORS_FOUND = "Can not find a delegate method processor for `%s`.";

    private static ServiceLoader<DelegateMethodProcessor> processors;
    private static boolean initialized;

    private final Logger logger;
    private final Utils utils;
    private final Outputter outputter;

    public DelegateMethodProcessors(
            Logger logger,
            Utils utils,
            Outputter outputter) {
        this.logger = logger;
        this.utils = utils;
        this.outputter = outputter;

        if (processors == null) {
            processors = ServiceLoader.load(DelegateMethodProcessor.class, getClass().getClassLoader());
        }
    }

    public List<CodeSnippet> processAll(List<DelegateMethod> methods) {
        return FluentIterable.from(methods)
                .transform(new Function<DelegateMethod, CodeSnippet>() {
                    @Override
                    public CodeSnippet apply(DelegateMethod resourceMethod) {
                        return process(resourceMethod);
                    }
                })
                .toList();
    }

    public CodeSnippet process(DelegateMethod context) {
        ensureInitialized();

        for (DelegateMethodProcessor processor : processors) {
            if (processor.canProcess(context)) {
                return processor.process(context);
            }
        }

        logger.error(NO_PROCESSORS_FOUND, context.getMethod().getName());
        throw new UnableToProcessException();
    }

    private void ensureInitialized() {
        if (!initialized) {
            for (DelegateMethodProcessor processor : processors) {
                processor.init(logger, utils, outputter);
            }

            initialized = true;
        }
    }
}
