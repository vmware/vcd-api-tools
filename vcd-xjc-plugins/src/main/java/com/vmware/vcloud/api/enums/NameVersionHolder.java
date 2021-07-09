package com.vmware.vcloud.api.enums;

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


import java.util.HashMap;
import java.util.Map;

import com.vmware.vcloud.api.rest.version.Supported;

/**
 * A class which allows validation of {@link String} to {@code Enum} conversions.
 */
public class NameVersionHolder<T extends Supported> {

    /*
     * Do not use this under ordinary circumstances
     */
    protected static volatile EnumValidator<Supported> DEFAULT_ENUM_VALIDATOR = null;

    private final Map<String, T> cachedValues = new HashMap<String, T>();

    /**
     * Associate the <code>key</code> string with the given <code>typeValue</code>.
     *
     * @param key
     * @param typeValue
     * @return
     */
    public final T put(String key, T typeValue) {
        return cachedValues.put(key, typeValue);
    }

    public final boolean contains(String v) {
        return cachedValues.containsKey(v);
    }

    /**
     * Resolves the passed string <code>key</code> to an enum <code>T</code>
     *
     * @param key
     *          the string value key to resolve the enum.
     * @return a <code>T</code> associated with the given <code>key</code>.
     *
     */
    public final T fromValue(String key) {
        if (key == null) {
            return null;
        }

        final T t = cachedValues.get(key);

        return validateValue(key, t);
    }

    /*
     * Do nothing by default but allow a default validator for all
     * enums that use this class. This default validator should only
     * be used by the server side to support more advanced validation.
     */
    protected T validateValue(String key, T value) {
        if (DEFAULT_ENUM_VALIDATOR != null) {
            DEFAULT_ENUM_VALIDATOR.validate(key, value);
        }

        return value;
    }
}

