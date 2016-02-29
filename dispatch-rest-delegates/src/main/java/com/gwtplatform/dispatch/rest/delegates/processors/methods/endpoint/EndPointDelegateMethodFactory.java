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

package com.gwtplatform.dispatch.rest.delegates.processors.methods.endpoint;

import com.google.auto.service.AutoService;
import com.gwtplatform.dispatch.rest.delegates.processors.methods.DelegateMethod;
import com.gwtplatform.dispatch.rest.delegates.processors.methods.DelegateMethodFactory;
import com.gwtplatform.dispatch.rest.processors.endpoint.EndPointMethod;
import com.gwtplatform.dispatch.rest.processors.resource.ResourceMethod;
import com.gwtplatform.processors.tools.logger.Logger;
import com.gwtplatform.processors.tools.utils.Utils;

@AutoService(DelegateMethodFactory.class)
public class EndPointDelegateMethodFactory implements DelegateMethodFactory {
    @Override
    public void init(Logger logger, Utils utils) {
    }

    @Override
    public boolean canCreate(ResourceMethod method) {
        return method instanceof EndPointMethod;
    }

    @Override
    public DelegateMethod create(ResourceMethod method) {
        return new EndPointDelegateMethod((EndPointMethod) method);
    }
}
