package com.vmware.vcloud.api.annotation;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.xml.bind.JAXBElement;

import com.vmware.vcloud.api.rest.version.ApiVersion;


/**
 * Indicates that a handler method can handle multi-site requests. A list of collection field names
 * to be merged in the result body can be optionally specified. A custom response builder class can
 * also be optionallyspecified to handle aggregation of the federated responses. If neither is
 * specified, default aggregation implementation will be applied to the results.
 */
@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiSite {

    /**
     * Specifies the list of collection names in a {@link JAXBElement} type that should be merged to
     * create a multi-site response. i.e. the contents of independent response objects from multiple
     * associated Org endpoints will be merged into a single collection in the response object
     */
    String[] mergeCollections() default {};

    /**
     * Specifies a custom class to use to build a multi-site response object. If none specified, a
     * default multi-site response builder will be used to construct a multi-site response for the
     * request. Any mrgeCollections value is ignored if a custom response builder is specified
     */
    Class<?> customResponseBuilder() default Object.class;

    /**
     * Specifies an {@link ApiVersion.Alias} to add support for multi-site request fanout. If none
     * specified, a default {@code ApiVersion.Alias.MULTI_SITE} is used
     */
    ApiVersion.Alias addedIn() default ApiVersion.Alias.MULTI_SITE;

    /**
     * Specifies an {@link ApiVersion.Alias} to remove support for multi-site request fanout. If
     * none specified, a default {@code ApiVersion.Alias.FUTURE} is used
     */
    ApiVersion.Alias removedIn() default ApiVersion.Alias.FUTURE;
}
