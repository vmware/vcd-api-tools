package com.vmware.vcloud.bindings.generator.python;

/*-
 * #%L
 * vcd-bindings-generator :: Bindings generation utility
 * %%
 * Copyright (C) 2020 VMware, Inc.
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

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.reflect.TypeUtils;

public class PythonClass extends PythonFile {
    /* A list of fields with schema names that have Java keyword collisions, but
     * have normalized getters/setters. Since the JSON mapping uses the getter/setter names
     * for serialization, so must the JSON payload.
     *
     * The @XmlAttribute and @XmlElement metadata was proven to be an unreliable way to determine
     * getter/setter name, so resorting to more brutish methods.
     */
    private static final Map<String, String> RESERVED_NAMES;
    static {
        final Map<String, String> reserved = new HashMap<>();
        reserved.put("_default", "default");
        reserved.put("_interface", "interface");
        
        RESERVED_NAMES = Collections.unmodifiableMap(reserved);
    }

    private Class<?> parent;
    private boolean isAbstract;

    PythonClass(final Class<?> clazz) {
        setClazz(clazz);

        if (clazz.getSuperclass() != Object.class) {
            this.parent = clazz.getSuperclass();
            addImport(clazz, this.parent);
        }

        isAbstract = Modifier.isAbstract(clazz.getModifiers());
    }

    public String getParent() {
        return parent == null ? null : parent.getSimpleName();
    }

    public boolean getIsAbstract() {
        return isAbstract;
    }

    @Override
    protected void processField(java.lang.reflect.Field field, Class<?> clazz) {
        if (Modifier.isStatic(field.getModifiers())) {
            return;
        }

        final PythonMapper mapper = new PythonMapper();

        Type genericType = field.getGenericType();
        boolean isArray = TypeUtils.isArrayType(genericType) || TypeUtils.isAssignable(genericType, Collection.class);

        Type actualType = genericType;
        while (actualType instanceof ParameterizedType) {
            actualType = ((ParameterizedType) actualType).getActualTypeArguments()[0];
            if (actualType instanceof WildcardType) {
                actualType = ((WildcardType) actualType).getUpperBounds()[0];
            }
        }

        final String typescriptFieldName = RESERVED_NAMES.getOrDefault(field.getName(), field.getName());
        addField(typescriptFieldName, mapper.getTypeName(actualType) + ((isArray) ? "[]" : ""));
        if (!mapper.isBuiltInType(actualType) && !actualType.getTypeName().startsWith("java")) {
            addImport(clazz, (Class <?>) actualType);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", getName())
            .append("isAbstract", isAbstract).toString();
    }
}
