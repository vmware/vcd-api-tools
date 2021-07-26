/* **********************************************************************
 * Copyright 2018 VMware, Inc.  All rights reserved. VMware Confidential
 * *********************************************************************/

package com.vmware.vcloud.api.http.converters;

/*-
 * #%L
 * vcd-xjc-plugins :: Custom plugins for XML to Java Compilation
 * %%
 * Copyright (C) 2018 - 2021 VMware, Inc.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper to provide access to OpenAPI query results.
 *
 * @since 9.7
 */
public class QueryResultJsonRecords {

    private static final String TOTAL_METHOD = "getResultTotal";
    private static final String COUNT_METHOD = "getPageCount";
    private static final String PAGE_METHOD = "getPage";
    private static final String PAGE_SIZE_METHOD = "getPageSize";
    private static final String GET_VALUES_METHOD = "getValues";
    private static final String SET_VALUES_METHOD = "setValues";

    private final int resultTotal;
    private final int pageCount;
    private final int page;
    private final int pageSize;
    private final List<Object> values;

    public QueryResultJsonRecords(Object openApiResults) {
        try {
            this.resultTotal = getInt(openApiResults, TOTAL_METHOD);
            this.pageCount = getInt(openApiResults, COUNT_METHOD);
            this.page = getInt(openApiResults, PAGE_METHOD);
            this.pageSize = getInt(openApiResults, PAGE_SIZE_METHOD);
            List<Object> valueResults = getList(openApiResults, GET_VALUES_METHOD);

            // Client might set valueResults to be immutable list but this class assume list is mutable in MultisiteOpenApiFilter.
            if (valueResults == null || valueResults.size() == 0) {
                this.values = new ArrayList<>();
            } else {
                this.values = new ArrayList<>(valueResults);
            }
            setList(openApiResults, SET_VALUES_METHOD, values);

        } catch (SecurityException | ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to create JSON record from provided object", e);
        }
    }

    private int getInt(Object object, String methodName) throws ReflectiveOperationException, SecurityException {
        final Object invokeMethod = invokeMethod(object, methodName);
        if (!(invokeMethod instanceof Integer)) {
            throwException(object, methodName);
        }

        return (int) invokeMethod;
    }

    @SuppressWarnings("unchecked")
    private List<Object> getList(Object object, String methodName) throws ReflectiveOperationException, SecurityException {
        final Object invokeMethod = invokeMethod(object, methodName);
        if (!(invokeMethod instanceof List<?>)) {
            throwException(object, methodName);
        }

        return (List<Object>) invokeMethod;
    }

    @SuppressWarnings("unchecked")
    private void setList(Object object, String methodName, List<Object> values) throws ReflectiveOperationException, SecurityException {
        final Method method = object.getClass().getMethod(methodName, List.class);
        method.invoke(object, values);
    }

    private void throwException(Object object, String methodName) throws ReflectiveOperationException {
        throw new ReflectiveOperationException("Unexpected return type from " + methodName + " on " + object.getClass().getSimpleName());
    }

    /**
     * Invokes {@code methodName} on the provided {@link Object} and returns
     * the result.
     */
    private Object invokeMethod(Object object, String methodName) throws ReflectiveOperationException, SecurityException {
        final Method method = object.getClass().getMethod(methodName);
        final Object result = method.invoke(object);
        return result;
    }

    public int getResultTotal() {
        return resultTotal;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<Object> getValues() {
        return values;
    }

}
