package com.vmware.vcloud.api.rest.version;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Generates a configuration file consumable by the rest-api-docgenerator project that serves as a
 * global source of API versions.
 * <p>
 * This global API version file allows each individual rest-api-docgenerator configuration to share
 * this data rather than duplicate it.
 */
public class ApiVersionGenerator {
    private static class GlobalConfiguration {
        public Map<String, Set<String>> mapOfVersionSet = new HashMap<String, Set<String>>();
        public Map<String, Map<String, String>> mapOfAliases = new HashMap<String, Map<String, String>>();
    }

    private static final String OUTPUT_FILE = "global.json";

    private static final String VCLOUD_KEY = "vcd";

    @SuppressWarnings("serial")
    private static final Set<String> VCLOUD_VERSION_SET = new LinkedHashSet<String>() {{
        add("0.9"); // Some of our XSD entities were added in 0.9
    }};

    private static final Map<String, String> VCLOUD_ALIAS_MAP = new LinkedHashMap<String, String>();

    public static void main(String[] args) throws Exception {
        final String outputDir = System.getProperty("apiVersionOutputDirectory");

        if (outputDir == null) {
            throw new IllegalStateException("System property 'apiVersionOutputDirectory' needs to be set.");
        }

        for (final ApiVersion apiVersion : ApiVersion.values()) {
            VCLOUD_VERSION_SET.add(apiVersion.value());
        }

        for (final ApiVersion.Alias alias : ApiVersion.Alias.values()) {
            if (VCLOUD_VERSION_SET.contains(alias.getMapping().value())) {
                VCLOUD_ALIAS_MAP.put(alias.name(), alias.getMapping().value());
            }
        }

        final GlobalConfiguration globalConfiguration = new GlobalConfiguration();

        globalConfiguration.mapOfVersionSet.put(VCLOUD_KEY, VCLOUD_VERSION_SET);
        globalConfiguration.mapOfAliases.put(VCLOUD_KEY, VCLOUD_ALIAS_MAP);

        final byte[] serializedConfiguration = serializeConfiguration(globalConfiguration);

        writeSerializedConfiguration(serializedConfiguration, new File(outputDir, OUTPUT_FILE));
    }

    private static byte[] serializeConfiguration(GlobalConfiguration globalConfiguration)
            throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        return objectMapper.writeValueAsBytes(globalConfiguration);
    }

    private static void writeSerializedConfiguration(byte[] serlializedConfiguration, File outputFile)
            throws IOException {

        final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        fileOutputStream.write(serlializedConfiguration);

        fileOutputStream.close();
    }
}

