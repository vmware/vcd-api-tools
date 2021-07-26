package com.vmware.vcloud.api.utils;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * A set of utilities for processing URNs
 */
public class UrnUtils {
    protected static final int NAMESPACE_IND = 1;
    protected static final int TYPE_IND = 2;
    private static final int NSS_IND = 3;
    protected static final int ID_IND = 4;

    protected static final String URN_DENOTATION = "urn";
    protected static final String URN_SEPARATOR = ":";

    private static final String NAMESPACE_REGEX = "([a-z0-9][a-z0-9-]{0,31})";
    private static final String TYPE_REGEX = "([^\\/\\s" + URN_SEPARATOR + "]+)";
    private static final String NSS_REGEX = "?([^\\/\\s]*)";
    private static final String UUID_REGEX =
            "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})";
    protected static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);
    private static final String VALUE_REGEX = "([\\w-]+)";
    private static final String URN_FORMAT_REGEX =
            URN_DENOTATION + URN_SEPARATOR + NAMESPACE_REGEX + URN_SEPARATOR + TYPE_REGEX
                    + URN_SEPARATOR + NSS_REGEX + URN_SEPARATOR + VALUE_REGEX;
    private static final String URN_REGEX =
            URN_DENOTATION + URN_SEPARATOR + NAMESPACE_REGEX + URN_SEPARATOR + TYPE_REGEX
                    + URN_SEPARATOR + NSS_REGEX + URN_SEPARATOR + UUID_REGEX;
    private static final Pattern URN_PATTERN = Pattern.compile("^" + URN_REGEX + "$", 0);
    private static final Pattern URN_FORMAT_PATTERN = Pattern.compile("^" + URN_FORMAT_REGEX + "$", 0);

    protected static String[] getParts(String vcdId) {
        if (StringUtils.isBlank(vcdId)) {
            return null;
        }

        final Matcher idMatcher = URN_FORMAT_PATTERN.matcher(vcdId);
        if (!idMatcher.matches()) {
            return null;
        }

        return new String[]{
                URN_DENOTATION, idMatcher.group(NAMESPACE_IND), idMatcher.group(TYPE_IND),
                idMatcher.group(NSS_IND), idMatcher.group(ID_IND)};
    }

    /**
     * Gets entity id from VCD ID.
     *
     * @param vcdId a VCD ID
     * @return the corresponding id or null if the vcd id cannot be parsed
     */
    public static String getEntityId(String vcdId) {
        final String[] parts = getParts(vcdId);
        if (parts == null) {
            return null;
        }
        return parts[UrnUtils.ID_IND];
    }


    /**
     * Checks whether provided href is a valid urn with a UUID value
     * @param href
     * @return true is href is actually urn
     */
    public static boolean isUrn(String href) {
        if (StringUtils.isBlank(href)) {
            return false;
        }
        return URN_PATTERN.matcher(href).matches();
    }

    /**
     * Checks that the href is in urn format
     */
    public static boolean hasValidUrnFormat(String href) {
        if (StringUtils.isBlank(href)) {
            return false;
        }
        return URN_FORMAT_PATTERN.matcher(href).matches();
    }
}
