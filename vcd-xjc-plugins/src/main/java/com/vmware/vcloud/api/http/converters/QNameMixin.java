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

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A Jackson Mixin to assist in parsing {@link QName} from its string representation
 * <p>
 * {@link QName}, which primarily consists of a {@code namespace URI} and a {@code localpart}, is
 * serialized in {@code json} representation of our legacy API types as per the most commonly
 * accepted way of representing it as <a href="http://jclark.com/xml/xmlns.htm">defined by James
 * Clark.</a>. <br>
 * Parsing such a string can be accomplished by {@link QName#valueOf(String)} method.
 * <p>
 * {@link ObjectMapper}'s order of preference causes {@link QName#QName(String)} single argument
 * constructor to be preferred over this method over a similar single argument factory method (which
 * would be {@code QName#valueOf(String)}. As a result, a single string, that is formatted as above,
 * is interpreted as a {@code QName} that is comprised solely of the {@code localpart} instead of
 * potentially both, {@code namespace URI} and {@code localpart}.
 * <p>
 * This mixin instructs the {@link ObjectMapper} to ignore that constructor and instead use the
 * {@link QName#valueOf(String) valueOf method} as a factory method to parse the incoming string.
 * <p>
 * De-serializing the payload is handled by {@link QName#toString()} method
 * <p>
 * Ref:<A href="https://github.com/FasterXML/jackson-docs/wiki/JacksonMixInAnnotations">Official
 * Jackson MixIn Annotations Documentation</A>
 *
 * @see QName#toString()
 * @see QName#valueOf(String)
 */
public abstract class QNameMixin {

    @JsonIgnore
    public QNameMixin(String localPart) {}

    @JsonCreator
    public static QName valueOf(String qNameAsString) {
        return QName.valueOf(qNameAsString);
    }

}

