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

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vmware.vcloud.api.annotation.Supported;

/**
 * API version and alias handling.
 * <p>
 * See https://confluence.eng.vmware.com/pages/viewpage.action?spaceKey=VCD&amp;title=API+Development+Guide#APIDevelopmentGuide-APIVersions
 * for details related to API versioning
 *
 * @since 1.5.0
 */
public class ApiVersion implements Comparable<ApiVersion>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ALPHA_VERSION_TAIL = ".0-alpha";

    private static final String MAX_VERSION_ALIAS = ""; // TODO RENAME AS PART OF VTEN-4458

    private static final Comparator<String> VERSION_STRING_COMPARATOR = new Comparator<>() {
        private final Function<String, int[]> split = s -> {
            final String[] tokens = s.split("\\.");
            if (tokens.length > 2) {
                throw new AssertionError("Comparator must be updated to handle x.y.z");
            }
            return new int[] { Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]) };
        };
        @Override
        public int compare(String s1, String s2) {
            final int[] v1 = split.apply(s1);
            final int[] v2 = split.apply(s2);
            if (v1[0] == v2[0]) {
                return Integer.compare(v1[1], v2[1]);
            }
            return Integer.compare(v1[0], v2[0]);
        }
    };

    /**
     * Map holding all valid API versions, mapped to their version string.
     * <p>
     * The highest supported version will always (and only) indicate alpha support.
     */
    private static final NavigableMap<String, ApiVersion> VERSIONS = new TreeMap<>(VERSION_STRING_COMPARATOR);

    /*
     * New API versions should be added below, each will automatically be
     * added to the above map used by the rest of the {@code ApiVersion} class.
     * <p>
     * Order of the below doesn't strictly matter since the containing map will be
     * ordered based on the version string, not insertion order. But adding items
     * in their appropriate order is encouraged for increased readability.
     * <p>
     * When adding a version corresponding to a new product version (i.e. not a
     * patch) you will typically want to <i>also</i> deprecate the API version(s)
     * corresponding to the oldest non-deprecated product version. See above-linked
     * confluence for further guidance.
     */

    /** Introduced in product version 1.0 / Redwood */
    @Deprecated
    public static final ApiVersion VERSION_1_0 = new ApiVersion(1, 0, true);

    /** Introduced in product version 1.5 / Toledo */
    @Deprecated
    public static final ApiVersion VERSION_1_5 = new ApiVersion(1, 5, true);

    /** Introduced in product version 5.1 / T2 */
    @Deprecated
    public static final ApiVersion VERSION_5_1 = new ApiVersion(5, 1, true);

    /** Introduced in product version 5.5 / OP */
    @Deprecated
    public static final ApiVersion VERSION_5_5 = new ApiVersion(5, 5, true);

    /** Introduced in product version 5.6 / Northstar */
    @Deprecated
    public static final ApiVersion VERSION_5_6 = new ApiVersion(5, 6, true);

    /** Introduced in product version 5.7 */
    @Deprecated
    public static final ApiVersion VERSION_5_7 = new ApiVersion(5, 7, true);

    /** Introduced in product version 5.8 */
    @Deprecated
    public static final ApiVersion VERSION_5_8 = new ApiVersion(5, 8, true);

    /** Introduced in product version 5.9 */
    @Deprecated
    public static final ApiVersion VERSION_5_9 = new ApiVersion(5, 9, true);

    /** Introduced in product version 6.1 */
    @Deprecated
    public static final ApiVersion VERSION_5_10 = new ApiVersion(5, 10, true);

    /** Introduced in product version 6.2 */
    @Deprecated
    public static final ApiVersion VERSION_5_11 = new ApiVersion(5, 11, true);

    /** Introduced in product version 6.3 */
    @Deprecated
    public static final ApiVersion VERSION_5_12 = new ApiVersion(5, 12, true);

    /** Introduced in product version 6.4 */
    @Deprecated
    public static final ApiVersion VERSION_6_0 = new ApiVersion(6, 0, true);

    /** Introduced in product version 6.5 */
    @Deprecated
    public static final ApiVersion VERSION_7_0 = new ApiVersion(7, 0, true);

    /** Introduced in product version 7.1 */
    @Deprecated
    public static final ApiVersion VERSION_9_0 = new ApiVersion(9, 0, true);

    /** Introduced in product version 8.1 */
    @Deprecated
    public static final ApiVersion VERSION_11_0 = new ApiVersion(11, 0, true);

    /** Introduced in product version 8.2 */
    @Deprecated
    public static final ApiVersion VERSION_12_0 = new ApiVersion(12, 0, true);

    /** Introduced in product version 8.3 */
    @Deprecated
    public static final ApiVersion VERSION_13_0 = new ApiVersion(13, 0, true);

    /** Introduced in product version 8.4 */
    @Deprecated
    public static final ApiVersion VERSION_14_0 = new ApiVersion(14, 0, true);

    /** Introduced in product version 8.6 */
    @Deprecated
    public static final ApiVersion VERSION_16_0 = new ApiVersion(16, 0, true);

    /** Introduced in product version 8.7 */
    @Deprecated
    public static final ApiVersion VERSION_17_0 = new ApiVersion(17, 0, true);

    /** Introduced in product version 8.8 */
    @Deprecated
    public static final ApiVersion VERSION_18_0 = new ApiVersion(18, 0, true);

    /** Introduced in product version 8.9 */
    @Deprecated
    public static final ApiVersion VERSION_19_0 = new ApiVersion(19, 0, true);

    /** Introduced in product version 8.10 / Riverfront */
    @Deprecated
    public static final ApiVersion VERSION_20_0 = new ApiVersion(20, 0, true);

    /** Introduced in product version 8.11 */
    @Deprecated
    public static final ApiVersion VERSION_21_0 = new ApiVersion(21, 0, true);

    /** Introduced in product version 8.12 */
    @Deprecated
    public static final ApiVersion VERSION_22_0 = new ApiVersion(22, 0, true);

    /** Introduced in product version 8.13 */
    @Deprecated
    public static final ApiVersion VERSION_23_0 = new ApiVersion(23, 0, true);

    /** Introduced in product version 8.14 */
    @Deprecated
    public static final ApiVersion VERSION_24_0 = new ApiVersion(24, 0, true);

    /** Introduced in product version 8.15 */
    @Deprecated
    public static final ApiVersion VERSION_25_0 = new ApiVersion(25, 0, true);

    /** Introduced in product version 8.16 */
    @Deprecated
    public static final ApiVersion VERSION_26_0 = new ApiVersion(26, 0, true);

    /** Introduced in product version Sunglow (8.20) */
    @Deprecated
    public static final ApiVersion VERSION_27_0 = new ApiVersion(27, 0, true);

    /** Introduced in product version 8.21 **/
    @Deprecated
    public static final ApiVersion VERSION_28_0 = new ApiVersion(28, 0, true);

    /** Introduced in product version Trifecta (9.0) **/
    @Deprecated
    public static final ApiVersion VERSION_29_0 = new ApiVersion(29, 0, true);

    /** Introduced in product version Ulysses (9.1) **/
    @Deprecated
    public static final ApiVersion VERSION_30_0 = new ApiVersion(30, 0, true);

    /** Introduced in product version Vulcan (9.5) **/
    @Deprecated
    public static final ApiVersion VERSION_31_0 = new ApiVersion(31, 0, true);

    /** Introduced in product version Wellington (9.7) **/
    @Deprecated
    public static final ApiVersion VERSION_32_0 = new ApiVersion(32, 0, true);

    /** Introduced in product version Xendi (10.0) **/
    public static final ApiVersion VERSION_33_0 = new ApiVersion(33, 0);

    /** Introduced in product version Yorktown (10.1) **/
    public static final ApiVersion VERSION_34_0 = new ApiVersion(34, 0);

    /** Introduced in product version Zeus (10.2) **/
    public static final ApiVersion VERSION_35_0 = new ApiVersion(35, 0);

    /** Zeus update 2 **/
    public static final ApiVersion VERSION_35_2 = new ApiVersion(35, 2);

    /** Product version Andromeda **/
    public static final ApiVersion VERSION_36_0 = new ApiVersion(36, 0);

    /** Product version Betelgeuse **/
    public static final ApiVersion VERSION_37_0 = new ApiVersion(37, 0);

    /** This is poorly named but widely referenced, will rename in VTEN-4458 **/
    public static final ApiVersion VERSION_MAX = new ApiVersion(Integer.MAX_VALUE, Integer.MAX_VALUE);

    /**
     * This enum will help manage all API version alias mappings.
     */
    public enum Alias {
        MIN_SUPPORTED(ApiVersion.VERSION_31_0),
        ALPHA(getAlphaVersion()),
        OBJECT_EXTENSIBILITY(ApiVersion.VERSION_16_0),
        VM_AFFINITY_RULES(ApiVersion.VERSION_20_0),
        MAX_SUPPORTED(ApiVersion.VERSION_36_0),
        VAPP_AUTO_NATURE(ApiVersion.VERSION_22_0),
        VDC_ADOPT_RP(ApiVersion.VERSION_22_0),
        PERSIST_TABLE_ACCESS(ApiVersion.VERSION_22_0),
        VDC_PERMISSIONS(ApiVersion.VERSION_11_0),
        OPTIMIZED_REVERT_VAPP_WORKFLOW(ApiVersion.VERSION_14_0),
        VDC_TEMPLATES(ApiVersion.VERSION_5_7),
        FUTURE(ApiVersion.VERSION_MAX),
        ORG_RIGHTS_ROLES(ApiVersion.VERSION_27_0),
        MULTI_SITE(ApiVersion.VERSION_29_0),
        VM_HOST_AFFINITY(ApiVersion.VERSION_27_0),
        ORG_LEASE_EXPIRE(ApiVersion.VERSION_25_0),
        AUTO_DISCOVER_VM_SETTINGS(ApiVersion.VERSION_27_0),
        DYNAMIC_HW_VERSION_SUPPORT(ApiVersion.VERSION_29_0),
        REGENERATE_BIOS_UUID(ApiVersion.VERSION_29_0),
        TENANT_STORAGE_MIGRATION(ApiVersion.VERSION_29_0),
        VXLAN_NETWORK_POOL(ApiVersion.VERSION_29_0),
        PORTAL_BRANDING(ApiVersion.VERSION_30_0),
        RECOMPOSE_BLANK_VM(ApiVersion.VERSION_30_0),
        REMOVED_VERSION_15_51(ApiVersion.VERSION_30_0),
        VRO_WORKFLOW_SUPPORT(ApiVersion.VERSION_30_0),
        JWT_LOGIN_SUPPORT(ApiVersion.VERSION_30_0),
        ENABLE_OVA_DOWNLOAD(ApiVersion.VERSION_30_0),
        ORG_VDC_ROLLUP(ApiVersion.VERSION_30_0),
        IMPORT_VM_STANDALONE(ApiVersion.VERSION_30_0),
        IMPROVED_SITE_NAME(ApiVersion.VERSION_30_0),
        VCENTER_ROOT_FOLDER(ApiVersion.VERSION_31_0),
        MULTI_SITE_NETWORKING(ApiVersion.VERSION_31_0),
        CROSS_VDC_NETWORKING(ApiVersion.VERSION_31_0),
        NSX_T_SUPPORT(ApiVersion.VERSION_31_0),
        TENANT_VM_GROUP(ApiVersion.VERSION_31_0),
        API_VERSION_POST9_1_UPDATE(ApiVersion.VERSION_31_0),
        CHANGE_VAPP_TEMPLATE_OWNER(ApiVersion.VERSION_31_0),
        VMC_SUPPORT(ApiVersion.VERSION_31_0),
        PVDC_TAGGING_SUPPORT(ApiVersion.VERSION_31_0),
        ORG_ASSOC_RESP_UPDATE(ApiVersion.VERSION_31_0),
        OIDC_SUPPORT(ApiVersion.VERSION_31_0),
        @Deprecated
        PROBE_REMOTE_LIBRARY(ApiVersion.VERSION_31_0),
        RBAC_2(ApiVersion.VERSION_31_0),
        RESERVED_BUS_UNIT_NUMBER(ApiVersion.VERSION_31_0),
        EMAIL_SETTINGS_CLOUD_API(ApiVersion.VERSION_31_0),
        LDAP_SYNC_TEST_CLOUD_API(ApiVersion.VERSION_31_0),
        DEPRECATED_HOST_FEATURES(ApiVersion.VERSION_31_0),
        ORG_VDC_ROLLUP2(ApiVersion.VERSION_31_0),
        SITE_ORG_ASSOCIATIONS_QUERY(ApiVersion.VERSION_31_0),
        ORG_VDC_NETWORKING(ApiVersion.VERSION_32_0),
        CPOM(ApiVersion.VERSION_32_0),
        CPOM_PROVIDER(ApiVersion.VERSION_33_0),
        GENERIC_VDC_TYPE(ApiVersion.VERSION_32_0),
        VC_NONE_NETWORK(ApiVersion.VERSION_32_0),
        PREFERENCES(ApiVersion.VERSION_32_0),
        ADDED_OAUTH_SETTINGS_DELETE(ApiVersion.VERSION_32_0),
        SERVICE_APPS(ApiVersion.VERSION_32_0),
        EDGE_CLUSTER(ApiVersion.VERSION_32_0),
        ADDED_UNIVERSAL_NETWORK_POOL_TO_EXT_PVDC(ApiVersion.VERSION_32_0),
        VDC_COMPUTE_POLICY_ADMIN_VIEW(ApiVersion.VERSION_32_0),
        NSXT_ROUTER_IMPORT(ApiVersion.VERSION_32_0),
        EDIT_VM_COMPUTE_POLICY(ApiVersion.VERSION_32_0),
        VDC_CAPABILITIES(ApiVersion.VERSION_32_0),
        NSXT_EDGE_DNS(ApiVersion.VERSION_32_0),
        CREATE_BLANK_VM(ApiVersion.VERSION_32_0),
        INSTANTIATE_VM_TEMPLATE(ApiVersion.VERSION_32_0),
        VAPP_LOCALID_VM_QUERY(ApiVersion.VERSION_33_0),
        NSXT_SERVICES(ApiVersion.VERSION_33_0),
        VM_SIZING_POLICY(ApiVersion.VERSION_33_0),
        AUTH_HEADERS_LOGIN_ONLY(ApiVersion.VERSION_33_0),
        VM_REAPPLY_COMPUTE_POLICY(ApiVersion.VERSION_33_0),
        VDC_MAX_COMPUTE_POLICY_CREATE(ApiVersion.VERSION_33_0),
        VC_RESOURCE_POOLS(ApiVersion.VERSION_33_0),
        ALLOW_ACTIVITY_ACCESS_IN_MAINT_MODE(ApiVersion.VERSION_33_0),
        API_EXPLORER_VIEW(ApiVersion.VERSION_33_0),
        AUDIT_TRAIL(ApiVersion.VERSION_33_0),
        SECURITY_CONTEXT_CACHE_IN_DB(ApiVersion.VERSION_33_0),
        INCLUDE_API_VERSION_IN_AUTH_LOCATION(ApiVersion.VERSION_33_0),
        VDC_COMPUTE_POLICIES(ApiVersion.VERSION_33_0),
        CENTRAL_SSL_MANAGEMENT(ApiVersion.VERSION_34_0),
        GATEWAY_EDGE_CLUSTER_CONFIGURATION(ApiVersion.VERSION_34_0),
        STORAGE_POLICY_CAPABILITIES(ApiVersion.VERSION_34_0),
        PROXY_CONFIGURATION(ApiVersion.VERSION_34_0),
        QUERY_LIST_GENERATED_OPERATIONS(ApiVersion.VERSION_34_0),
        GENERAL_ACL(ApiVersion.VERSION_35_0),
        QUOTA_POLICY(ApiVersion.VERSION_35_0),
        AVI_SUPPORT(ApiVersion.VERSION_35_0),
        SLAAC_SUPPORT(ApiVersion.VERSION_35_0),
        CPOM_MULTISITE(ApiVersion.VERSION_35_0),
        EVENT_PROCESSING_SUPPORT(ApiVersion.VERSION_35_0),
        FLEX_VDC_GROUP_SUPPORT(ApiVersion.VERSION_35_0),
        EXTENSION_TOKENS(ApiVersion.VERSION_35_0),
        PVDC_INHERITABLE_SETTINGS(ApiVersion.VERSION_35_0),
        VDC_KUBERNETES_POLICY(ApiVersion.VERSION_35_0),
        FILTER_ENCODED_REMOVED(ApiVersion.VERSION_35_0),
        HOST_NUM_CPU(ApiVersion.VERSION_35_2),
        CPOM_ANDROMEDA(ApiVersion.VERSION_36_0),
        OAUTH_PROVIDER_CONFIG(ApiVersion.VERSION_36_0),
        LDAP_SYNC_FLAG(ApiVersion.VERSION_35_2),
        ALLOWED_ORIGINS(ApiVersion.VERSION_36_0),
        SITE_DB_COLLATION(ApiVersion.VERSION_36_0),
        DISK_MIGRATION(ApiVersion.VERSION_36_0),
        STORAGE_ENTITY_TYPE_LIMITING(ApiVersion.VERSION_35_2),
        ETAG_SUPPORT(ApiVersion.VERSION_35_2),
        FIPS(ApiVersion.VERSION_35_2),
        TAG(ApiVersion.VERSION_36_0),
        RDE_HOOKS(ApiVersion.VERSION_36_0),
        ;

        private final ApiVersion mapping;

        private final Alias parentAlias;

        Alias(ApiVersion mapping) {
            this.mapping = mapping;
            this.parentAlias = null;
        }

        /**
         * @return the aliased API Version
         */
        public ApiVersion getMapping() {
            if (parentAlias != null) {
                return parentAlias.getMapping();
            }

            return mapping;
        }
    }

    private static final String VERSION_FORMAT = "%d.%d";

    private final String version;

    private final boolean isDeprecated;

    private final int majorVersion;

    private final int minorVersion;

    private Boolean isAlpha = null;

    private ApiVersion(int majorVersion, int minorVersion) {
        this(majorVersion, minorVersion, false);
    }

    private ApiVersion(int majorVersion, int minorVersion, boolean isDeprecated) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.isDeprecated = isDeprecated;
        if (majorVersion == Integer.MAX_VALUE && minorVersion == Integer.MAX_VALUE) {
            this.version = "";
            // This does NOT get put in VERSIONS because it's not a "real" version, just a convenience
        } else {
            this.version = String.format(VERSION_FORMAT, majorVersion, minorVersion);
            VERSIONS.put(version, this);
        }
    }

    private boolean isAlpha() {
        if (isAlpha == null) {
            isAlpha = VERSIONS.lastKey().equals(this.version);
        }
        return isAlpha;
    }

    /**
     * @return the version as string
     */
    public String value() {
        if (isAlpha()) {
            return version + ALPHA_VERSION_TAIL;
        }
        return version;
    }

    /**
     * @return flag indicating if the API version is marked for deprecation
     */
    public boolean isDeprecated() {
        return isDeprecated;
    }

    /**
     * @param v version as string
     * @return version as enum item
     * @throws IllegalArgumentException if the version is not recognized
     */
    public static ApiVersion fromValue(String v) {
        if (v == null || MAX_VERSION_ALIAS.equals(v)) {
            return VERSION_MAX;
        }

        final String ver;
        if (v.contains(ALPHA_VERSION_TAIL)) {
            /* Strip out anything after the Alpha tail (this may include a build
             * identifier but such information is only for consumer's benefit, not
             * for limiting compatibility across alpha requests). */
            ver = v.replaceFirst(ALPHA_VERSION_TAIL + ".*", ALPHA_VERSION_TAIL);
        } else {
            ver = v;
        }

        if (!ApiVersionCacheHelper.instance.isCached(ver)) {
            if (!ApiVersionCacheHelper.instance.isAliasCached(ver)) {
                throw new IllegalArgumentException("Unknown API version: " + ver);
            }
            return ApiVersionCacheHelper.instance.getAliasValue(ver);
        }
        return ApiVersionCacheHelper.instance.getValue(ver);
    }

    public static boolean isValidApiVersion(String v) {
        if (!ApiVersionCacheHelper.instance.isCached(v) && !MAX_VERSION_ALIAS.equals(v)) {
            return ApiVersionCacheHelper.instance.isAliasCached(v);
        }
        return true;
    }

    /**
     * Checks if the version is in the range supported by <b>s</b>.
     *
     * @param s the supported range
     * @return {@code true} if s.addedIn &lt;= this &lt; s.removedIn or if s is {@code null}
     */
    public boolean isInRange(Supported s) {
        return s == null || isInRange(fromValue(s.addedIn()), fromValue(s.removedIn()));
    }

    /**
     * Checks if the current version is in range [min..max).
     *
     * @param min
     * @param max
     * @return {@code true} if min &lt;= this &lt; max
     */
    public boolean isInRange(final ApiVersion min, final ApiVersion max) {
        return min.compareTo(this) <= 0 && this.compareTo(max) < 0;
    }

    /**
     * Tests if this {@code ApiVersion} is greater than or equal to the given one.
     *
     * @param version
     *            the version to compare this version to
     * @return true if this {@code ApiVersion} is greater than or equal to {@code version}, false
     *         otherwise
     */
    public boolean isAtLeast(final ApiVersion version) {
        return this.compareTo(version) >= 0;
    }

    /**
     * Tests if the Api Version represented by this {@code ApiVersion} is greater than or equal to
     * the given {@code Alias}.
     * @param alias Alias to compare to
     * @return true if this {@code ApiVersion} is greater than or equal to {@code alias}, false
     *         otherwise
     */
    public boolean isAtLeast(final Alias alias) {
        return isAtLeast(alias.getMapping());
    }

    /**
     * Tests if this {@code ApiVersion} is greater than the given one.
     *
     * @param version
     *            the version to compare this version to
     * @return true if this {@code ApiVersion} is greater than or equal to {@code version}, false
     *         otherwise
     */
    public boolean isGreaterThan(final ApiVersion version) {
        return this.compareTo(version) > 0;
    }

    /**
     * Tests if this {@code ApiVersion} is less than or equal to the given one.
     *
     * @param version
     *            the version to compare this version to
     * @return true if this {@code ApiVersion} is less than or equal to {@code version}, false
     *         otherwise
     */
    public boolean isAtMost(final ApiVersion version) {
        return this.compareTo(version) <= 0;
    }

    /**
     * Tests if this {@code ApiVersion} is less than the given one.
     *
     * @param version
     *            the version to compare this version to
     * @return true if this {@code ApiVersion} is less than {@code version}, false
     *         otherwise
     */
    public boolean isLessThan(final ApiVersion version) {
        return this.compareTo(version) < 0;
    }

    /**
     * Tests if this {@code ApiVersion} is less than the given ${@code ApiVersion.Alias}.
     *
     * @param alias
     *            the alias to compare this version to
     * @return true if this {@code ApiVersion} is less than {@code alias}, false
     *         otherwise
     */
    public boolean isLessThan(final Alias alias) {
        return isLessThan(alias.getMapping());
    }

    /**
     * @return {@code true} if this version falls within the supported range
     * of the {@link Supported} annotation (including if no range is provided)
     */
    public boolean isSupported(Supported supported) {
        if (supported == null) {
            /* Use of @Supported annotation isn't required so if not
             * present assume it's valid for this API version */
            return true;
        }

        return this.isInRange(supported);
    }

    /**
     * @return the set of keys used in the cache.
     */
    public static Set<String> getAllKeys() {
        return ApiVersionCacheHelper.instance.getAllKeys();
    }

    /**
     * @param aliasStr
     *            alias string
     * @return true if an Alias, false otherwise
     */
    public static boolean isAnAlias(String aliasStr) {
        return ApiVersionCacheHelper.instance.isAliasCached(aliasStr);
    }

    /**
     * Gets a list of ApiVersion between the min and max apiVersions (Inclusive).
     *
     * @param minApiVersion
     *            Min version
     * @param maxApiVersion
     *            Max version
     * @return List of ApiVersion
     */
    public static List<ApiVersion> getRange(final ApiVersion minApiVersion,
            final ApiVersion maxApiVersion) {
        return VERSIONS.values().stream()
                .filter(api -> api.isAtLeast(minApiVersion) && api.isAtMost(maxApiVersion))
                .collect(Collectors.toList());
    }

    /**
     * Gets a list of ApiVersion from the min version to the max supported version for this vCD
     * instance. (Inclusive)
     *
     * @param minApiVersion
     *            Min version
     * @return List of ApiVersion
     */
    public static List<ApiVersion> getRangeAbove(final ApiVersion minApiVersion) {
        return getRange(minApiVersion, Alias.MAX_SUPPORTED.getMapping());
    }

    /**
     * Returns the ApiVersion preceding the given ApiVersion intended for use only for ApiVersion
     * 29.0 and above.
     *
     * @param apiVersion
     * @return ApiVersion preceding given version.
     * @throws UnsupportedOperationException
     *             for ApiVersion 28.0 and below
     */
    public static ApiVersion getPreviousVersion(final ApiVersion apiVersion)
            throws UnsupportedOperationException {
        if (apiVersion.isLessThan(VERSION_29_0)) {
            throw new UnsupportedOperationException(
                    "Only supported for API version 29.0 and above.");
        }
        /* We could compute this for each version and cache it but at present
         * the only non-test call to this is a single call from a CMT command
         * so there's no need to optimize it presently. */
        ApiVersion previous = null;
        for (ApiVersion version : VERSIONS.values()) {
            if (version.equals(apiVersion)) {
                return previous;
            }
            previous = version;
        }
        return null;
    }

    @Override
    public int compareTo(ApiVersion other) {
        if (other == null) {
            return 1;
        }

        if (this.majorVersion == other.majorVersion) {
            return Integer.compare(this.minorVersion, other.minorVersion);
        }

        return Integer.compare(this.majorVersion, other.majorVersion);
    }

    @Override
    public String toString() {
        return value();
    }

    public static ApiVersion getAlphaVersion() {
        return VERSIONS.lastEntry().getValue();
    }

    public static ApiVersion[] values() {
        return VERSIONS.values().toArray(new ApiVersion[0]);
    }

    public static ApiVersion valueOf(String apiVersion) {
        return fromValue(apiVersion);
    }
}
