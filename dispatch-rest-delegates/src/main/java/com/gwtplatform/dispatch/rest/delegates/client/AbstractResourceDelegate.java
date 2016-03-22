/*
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

package com.gwtplatform.dispatch.rest.delegates.client;

import com.gwtplatform.dispatch.client.DelegatingDispatchRequest;
import com.gwtplatform.dispatch.rest.client.AlwaysCallback;
import com.gwtplatform.dispatch.rest.client.FailureCallback;
import com.gwtplatform.dispatch.rest.client.RestCallback;
import com.gwtplatform.dispatch.rest.client.RestDispatch;
import com.gwtplatform.dispatch.rest.client.SuccessCallback;
import com.gwtplatform.dispatch.rest.shared.RestAction;
import com.gwtplatform.dispatch.shared.DispatchRequest;

/**
 * Common code used by generated implementations of {@link ResourceDelegate}.
 *
 * @param <R> The resource used by this delegate.
 */
public abstract class AbstractResourceDelegate<R>
        implements ResourceDelegate<R>, Cloneable {
    protected final RestDispatch dispatcher;

    protected RestCallback<?> callback;
    protected DelegatingDispatchRequest delegatingDispatchRequest;
    protected SuccessCallback<?> successCallback;
    protected AlwaysCallback alwaysCallback;
    protected FailureCallback failureCallback;

    protected AbstractResourceDelegate(RestDispatch dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public ResourceDelegate<R> withDelegatingDispatchRequest(DelegatingDispatchRequest delegatingDispatchRequest) {
        AbstractResourceDelegate<R> delegate = createCopy();
        delegate.delegatingDispatchRequest = delegatingDispatchRequest;

        return delegate;
    }

    @Override
    public <T> R withCallback(RestCallback<T> callback) {
        AbstractResourceDelegate<R> delegate = createCopy();
        delegate.callback = callback;

        return delegate.asResource();
    }

    @Override
    public <T> ResourceDelegate<R> success(SuccessCallback<T> successCallback) {
        AbstractResourceDelegate<R> delegate = createCopy();
        delegate.successCallback = successCallback;

        return delegate;
    }

    @Override
    public ResourceDelegate<R> always(AlwaysCallback alwaysCallback) {
        AbstractResourceDelegate<R> delegate = createCopy();
        delegate.alwaysCallback = alwaysCallback;

        return delegate;
    }

    @Override
    public ResourceDelegate<R> failure(FailureCallback failureCallback) {
        AbstractResourceDelegate<R> delegate = createCopy();
        delegate.failureCallback = failureCallback;

        return delegate;
    }

    @Override
    public R call() {
        callback = wrapCallbacks();
        return asResource();
    }

    protected <R> RestAction<R> execute(RestAction<R> action) {
        DispatchRequest dispatchRequest = dispatcher.execute(action, (RestCallback<R>) callback);

        if (delegatingDispatchRequest != null) {
            delegatingDispatchRequest.setDelegate(dispatchRequest);
        }

        return action;
    }

    protected void copyFields(AbstractResourceDelegate<?> delegate) {
        delegate.delegatingDispatchRequest = delegatingDispatchRequest;
        delegate.callback = callback;
        delegate.successCallback = successCallback;
        delegate.alwaysCallback = alwaysCallback;
        delegate.failureCallback = failureCallback;
    }

    protected abstract AbstractResourceDelegate<R> newInstance();

    protected abstract R asResource();

    private AbstractResourceDelegate<R> createCopy() {
        AbstractResourceDelegate<R> delegate = newInstance();
        copyFields(delegate);

        return delegate;
    }

    private RestCallback<?> wrapCallbacks() {
        return new RestCallbackWrapper(successCallback, alwaysCallback, failureCallback);
    }
}
