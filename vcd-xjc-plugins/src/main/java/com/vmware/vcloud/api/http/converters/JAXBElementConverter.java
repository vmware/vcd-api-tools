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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBElement;


/**
 * Converts an xsd-generated type into a {@link JAXBElement} wrapped representation
 */
public class JAXBElementConverter {

    private static class ObjectFactoryInvocation {
        final Object objectFactory;
        final Method factoryMethod;

        ObjectFactoryInvocation(Object objectFactory, Method factoryMethod) {
            this.objectFactory = objectFactory;
            this.factoryMethod = factoryMethod;
        }

        public JAXBElement<?> convert(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return (JAXBElement<?>) factoryMethod.invoke(objectFactory, value);
        }
    }


    private final List<Object> objectFactories;

    private static final ConcurrentMap<Class<?>, ObjectFactoryInvocation> jaxbFactoryMethods =
            new ConcurrentHashMap<>();

    public JAXBElementConverter(final List<Object> objectFactories) {
        this.objectFactories = new ArrayList<>(objectFactories);
    }

    /**
     * Locates the factory method from among the {@code ObjectFactory}s this instance was seeded
     * with and converts the payload into its {@link JAXBElement} wrapped representation
     *
     * @param value
     *            xsd-generated type object to be wrapped within a {@link JAXBElement}
     * @return {@link JAXBElement} wrapped representation
     *
     * @throws IllegalAccessException
     *             if there was an error invoking the factory method.
     * @throws IllegalArgumentException
     *             if factory method to rejected the argument.
     * @throws NullPointerException
     *             They payload to be converted has no applicable factory method to effect the
     *             conversion.
     * @throws InvocationTargetException
     *             if there was an error invoking the factory method.
     */
    public JAXBElement<?> convertToJAXBElement(Object value) throws IllegalAccessException,
                                                             IllegalArgumentException,
                                                             NullPointerException,
                                                             InvocationTargetException {

        final ObjectFactoryInvocation jaxbConverter = getJAXBFactoryMethodInfo(value.getClass());
        final JAXBElement<?> result = jaxbConverter.convert(value);
        return result;
    }

    /**
     * For a payload type locate the {@code ObjectFactory} and the {@link Method} on that factory
     * that will effect the conversion
     * <P>
     * The pair is wrapped in an {@link ObjectFactoryInvocation} object and cached for quicker
     * lookups in the future
     *
     * @param elementCls
     *            - xsd-generated type's {@link Class}
     *
     * @return {@link ObjectFactoryInvocation} whose {@link ObjectFactoryInvocation#convert(Object)
     *         convert} method can be invoked to get the {@link JAXBElement}
     */
    private  ObjectFactoryInvocation getJAXBFactoryMethodInfo(Class<?> elementCls) {
        return jaxbFactoryMethods.computeIfAbsent(elementCls, this::getConverter);
    }

    private  ObjectFactoryInvocation getConverter(Class<?> elementCls) {
        for (Object factory : objectFactories) {
            Method[] methods = factory.getClass().getMethods();
            for (Method m : methods) {
                Class<?>[] pTypes = m.getParameterTypes();
                Class<?> pType = null;
                if (pTypes != null && pTypes.length == 1) {
                    pType = pTypes[0];
                } else {
                    continue;
                }

                Class<?> type = m.getReturnType();
                if (elementCls.equals(pType) && JAXBElement.class.isAssignableFrom(type)) {
                    return new ObjectFactoryInvocation(factory, m);
                }
            }
        }

        return null;
    }
}

