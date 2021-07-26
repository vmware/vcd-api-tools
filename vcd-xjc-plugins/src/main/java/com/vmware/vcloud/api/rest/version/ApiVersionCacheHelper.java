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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vmware.vcloud.api.rest.version.ApiVersion.Alias;

/**
 * This class helps with the lazy-init of the cached values of {@link ApiVersion} and
 * {@link ApiVersion.Alias}. Because an Alias enum references an ApiVersion enum, building a static
 * cache in the traditional pattern causes the Groovy compiler to throw an NPE at the declaration of
 * the Alias enum. This helper and its pattern solves that problem.
 */
class ApiVersionCacheHelper {

    /** Initialization-on-demand holder idiom */
    static final ApiVersionCache instance = new ApiVersionCache();

    /**
     * This inner static class builds the cache upon construction. It also provides a read-only
     * contract to the cache maps.
     */
    static class ApiVersionCache {

        private final Map<String, ApiVersion> cachedValues;
        private final Map<String, ApiVersion> cachedAliasValues;

        ApiVersionCache() {

            // Build alias cache
            final Map<String, ApiVersion> aliases = new HashMap<String, ApiVersion>();
            for (Alias v : Alias.values()) {
                String key = v.name();
                aliases.put(key, v.getMapping());
            }
            cachedAliasValues = Collections.unmodifiableMap(aliases);

            // Build ordinary cache
            final Map<String, ApiVersion> directVerions = new HashMap<String, ApiVersion>();
            for (ApiVersion v : ApiVersion.values()) {
                String key = v.value();
                directVerions.put(key, v);
            }
            cachedValues = Collections.unmodifiableMap(directVerions);
        }

        /**
         * @param key
         *            ordinary version string
         * @return true if version string is a key in cache, false otherwise
         */
        boolean isCached(String key) {
            return cachedValues.containsKey(key);
        }

        /**
         * @param key
         *            alias string
         * @return true if alias string is a key in cache, false otherwise
         */
        boolean isAliasCached(String key) {
            return cachedAliasValues.containsKey(key);
        }

        ApiVersion getValue(String key) {
            return cachedValues.get(key);
        }

        ApiVersion getAliasValue(String key) {
            return cachedAliasValues.get(key);
        }

        Set<String> getAllKeys() {
            final Set<String> keys = new HashSet<String>();
            keys.addAll(cachedValues.keySet());
            keys.addAll(cachedAliasValues.keySet());
            return keys;
        }
    }
}
