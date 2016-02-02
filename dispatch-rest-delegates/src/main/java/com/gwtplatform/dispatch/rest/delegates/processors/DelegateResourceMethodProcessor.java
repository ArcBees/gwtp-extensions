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

import com.google.auto.service.AutoService;
import com.gwtplatform.dispatch.rest.processors.DispatchRestContextProcessor;
import com.gwtplatform.dispatch.rest.processors.endpoint.EndPoint;
import com.gwtplatform.dispatch.rest.processors.endpoint.EndPointProcessor;
import com.gwtplatform.dispatch.rest.processors.resource.ResourceMethod;
import com.gwtplatform.dispatch.rest.processors.resource.ResourceMethodProcessor;
import com.gwtplatform.processors.tools.logger.Logger;
import com.gwtplatform.processors.tools.outputter.CodeSnippet;
import com.gwtplatform.processors.tools.outputter.Outputter;
import com.gwtplatform.processors.tools.utils.Utils;

import static com.gwtplatform.dispatch.rest.processors.NameUtils.qualifiedMethodName;

@AutoService(ResourceMethodProcessor.class)
public class DelegateResourceMethodProcessor extends DispatchRestContextProcessor<ResourceMethod, CodeSnippet>
        implements ResourceMethodProcessor {
    public static final String METHOD_SUFFIX = "$endPoint";

    private static final String TEMPLATE = "/com/gwtplatform/dispatch/rest/delegates/processors/DelegateMethod.vm";

    private final EndPointProcessor endPointProcessor;

    public DelegateResourceMethodProcessor() {
        endPointProcessor = new EndPointProcessor();
    }

    @Override
    public synchronized void init(Logger logger, Utils utils, Outputter outputter) {
        super.init(logger, utils, outputter);

        endPointProcessor.init(logger, utils, outputter);
    }

    @Override
    public boolean canProcess(ResourceMethod method) {
        return method instanceof DelegateResourceMethod;
    }

    @Override
    public CodeSnippet process(ResourceMethod resourceMethod) {
        DelegateResourceMethod delegateMethod = (DelegateResourceMethod) resourceMethod;
        String methodName = qualifiedMethodName(resourceMethod);
        EndPoint endPoint = delegateMethod.getEndPoint();

        logger.debug("Generating end-point method delegate `%s`.", methodName);

        CodeSnippet code = outputter.configure(TEMPLATE)
                .withParam("methodSuffix", METHOD_SUFFIX)
                .withParam("method", delegateMethod.getMethod())
                .withParam("defaultReturnValue", delegateMethod.getDefaultReturnValue())
                .withParam("endPointType", endPoint.getType())
                .withParam("endPointArguments", endPoint.getFields())
                .withErrorLogParameter(methodName)
                .parse();

        endPointProcessor.process(endPoint);

        return code;
    }

    @Override
    public void processLast() {
        endPointProcessor.processLast();
    }
}
