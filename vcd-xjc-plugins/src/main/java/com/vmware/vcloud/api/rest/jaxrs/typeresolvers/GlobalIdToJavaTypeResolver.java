package com.vmware.vcloud.api.rest.jaxrs.typeresolvers;

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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;

import org.apache.cxf.common.util.ClasspathScanner;

/**
 * This class scans the rest schema classes and builds a map of simple class names to java classes.
 * This class is used by a DeserializationProblemHandler to resolve type names that cannot be
 * resolved by the standard deserialization process.
 *
 */
public final class GlobalIdToJavaTypeResolver extends MinimalClassNameIdResolver {

    private final List<String> apiClassPackages;

    private final Class<? extends Annotation> restApiJsonMarkerAnnotation;

    private final Class<?> schemaClass;

    /* This is an id-to-List<Class<?>> mapping because some id's can have more than one Class associated with them:
     * e.g., "Property"
     * ./com/vmware/vcloud/api/rest/schema/ovf/Property
     * ./com/vmware/vcloud/api/rest/schema/ovf/environment/Property
     */
    final private Map<String, List<Class<?>>> idToTypeList;

    final ObjectMapper mapper;


    public GlobalIdToJavaTypeResolver(final List<String> apiClassPackages,
            final Class<? extends Annotation> restApiJsonMarkerAnnotation,
            final Class<?> schemaClass, final ObjectMapper mapper) {
        super(mapper.getDeserializationConfig().constructType(java.lang.Object.class),
                mapper.getTypeFactory(), LaissezFaireSubTypeValidator.instance);
        this.apiClassPackages = apiClassPackages;
        this.restApiJsonMarkerAnnotation = restApiJsonMarkerAnnotation;
        this.schemaClass = schemaClass;
        idToTypeList = getIdToJavaTypeMap();
        this.mapper = mapper;
    }

    @Override
    public String idFromValue(Object value) {
        if (value instanceof JAXBElement) {
            return (((javax.xml.bind.JAXBElement<?>) value).getDeclaredType().getSimpleName());
            //TODO enhance to scan for JsonTypeName annotations: https://jira.eng.vmware.com/browse/VTEN-3181
        } else {
            return super.idFromValue(value);
        }
    }


    public JavaType getJavaTypeForId(final DeserializationContext ctxt, final String subTypeId) {

        if (idToTypeList.get(subTypeId) == null) {
            return null;
        }

        final Class<?> classForId = idToTypeList.get(subTypeId).get(0);
        return ctxt.constructType(classForId);
    }


    private Map<String, List<Class<?>>> getIdToJavaTypeMap() {
        final Map<String, List<Class<?>>> result = new HashMap<>();
        final List<Class<?>> apiClasses = getRestApiClassesWithJsonMarkerAnnotation();
        apiClasses.stream().forEach(c -> {
            final String classId = c.getSimpleName();
            List<Class<?>> classListForId = result.computeIfAbsent(classId, k -> new ArrayList<>());
            classListForId.add(c);
        });
        return result;
    }


    private List<Class<?>> getRestApiClassesWithJsonMarkerAnnotation() {

        final List<Class<?>> classes = new ArrayList<>();
        //Make sure we use the rest-api-schemas loader. If we use the class loader for rest-api-toolkit (the 'this' class loader),
        //it loads the wrong com.vmware.vcloud.api.rest package.
        final ClassLoader restSchemaClassLoader = schemaClass.getClassLoader();

        try {
            classes.addAll(ClasspathScanner
                    .findClasses(apiClassPackages, Arrays.asList(restApiJsonMarkerAnnotation),
                            restSchemaClassLoader)
                    .values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
        } catch (ClassNotFoundException | IOException e) {
            //Ignore
        }
        return classes;
    }

}
