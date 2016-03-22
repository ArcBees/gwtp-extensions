/*
 * Copyright 2016 ArcBees Inc.
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

package com.gwtplatform.dispatch.rest.delegates.client;

import com.google.gwt.http.client.Response;
import com.gwtplatform.dispatch.rest.client.AlwaysCallback;
import com.gwtplatform.dispatch.rest.client.FailureCallback;
import com.gwtplatform.dispatch.rest.client.RestCallback;
import com.gwtplatform.dispatch.rest.client.SuccessCallback;

class RestCallbackWrapper<R> implements RestCallback<R> {
    private final SuccessCallback<R> successCallback;
    private final AlwaysCallback alwaysCallback;
    private final FailureCallback failureCallback;

    RestCallbackWrapper(
            SuccessCallback<R> successCallback,
            AlwaysCallback alwaysCallback,
            FailureCallback failureCallback) {

        this.successCallback = successCallback;
        this.alwaysCallback = alwaysCallback;
        this.failureCallback = failureCallback;
    }

    @Override
    public void onSuccess(R result, Response response) {
        if (successCallback != null) {
            successCallback.onSuccess(result, response);
        }
    }

    @Override
    public void always(Response response) {
        if (alwaysCallback != null) {
            alwaysCallback.always(response);
        }
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        if (failureCallback != null) {
            failureCallback.onFailure(throwable, response);
        }
    }
}
