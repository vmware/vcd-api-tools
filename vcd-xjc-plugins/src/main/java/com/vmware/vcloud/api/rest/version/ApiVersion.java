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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.vmware.vcloud.api.annotation.Supported;

/**
 * <p>
 * Enum with supported API version.
 * <p>
 * <b>IMPORTANT!</b> Versions are compared using the natural enum order, so keep them in ascending
 * order!
 * </p>
 * See https://wiki.eng.vmware.com/CloudAPI/CompatibilityAndVersioning for rules related to adding
 * new API versions
 * @since 1.5.0
 */
public enum ApiVersion {

    /** Introduced in product version 1.0 / Redwood */
    @Deprecated
    VERSION_1_0("1.0", true),

    /** Introduced in product version 1.5 / Toledo */
    @Deprecated
    VERSION_1_5("1.5", true),

    /** Introduced in product version 5.1 / T2 */
    @Deprecated
    VERSION_5_1("5.1", true),

    /** Introduced in product version 5.5 / OP */
    @Deprecated
    VERSION_5_5("5.5", true),

    /** Introduced in product version 5.6 / Northstar */
    @Deprecated
    VERSION_5_6("5.6", true),

    /** Introduced in product version 5.7 */
    @Deprecated
    VERSION_5_7("5.7", true),

    /** Introduced in product version 5.8 */
    @Deprecated
    VERSION_5_8("5.8", true),

    /** Introduced in product version 5.9 */
    @Deprecated
    VERSION_5_9("5.9", true),

    /** Introduced in product version 6.1 */
    @Deprecated
    VERSION_5_10("5.10", true),

    /** Introduced in product version 6.2 */
    @Deprecated
    VERSION_5_11("5.11", true),

    /** Introduced in product version 6.3 */
    @Deprecated
    VERSION_5_12("5.12", true),

    /** Introduced in product version 6.4 */
    @Deprecated
    VERSION_6_0("6.0", true),

    /** Introduced in product version 6.5 */
    @Deprecated
    VERSION_7_0("7.0", true),

    /** Reserved for potential use in a release between 6.5 and 7.1 */
    //VERSION_8_0("8.0", true),

    /** Introduced in product version 7.1 */
    @Deprecated
    VERSION_9_0("9.0", true),

    /** Introduced in product version 8.1 */
    @Deprecated
    VERSION_11_0("11.0", true),

    /** Introduced in product version 8.2 */
    @Deprecated
    VERSION_12_0("12.0", true),

    /** Introduced in product version 8.3 */
    @Deprecated
    VERSION_13_0("13.0", true),

    /** Introduced in product version 8.4 */
    @Deprecated
    VERSION_14_0("14.0", true),

    /** Introduced in product version 8.6 */
    @Deprecated
    VERSION_16_0("16.0", true),

    /** Introduced in product version 8.7 */
    @Deprecated
    VERSION_17_0("17.0", true),

    /** Introduced in product version 8.8 */
    @Deprecated
    VERSION_18_0("18.0", true),

    /** Introduced in product version 8.9 */
    @Deprecated
    VERSION_19_0("19.0", true),

    /** Introduced in product version 8.10 / Riverfront */
    @Deprecated
    VERSION_20_0("20.0", true),

    /** Introduced in product version 8.11 */
    @Deprecated
    VERSION_21_0("21.0", true),

    /** Introduced in product version 8.12 */
    @Deprecated
    VERSION_22_0("22.0", true),

    /** Introduced in product version 8.13 */
    @Deprecated
    VERSION_23_0("23.0", true),

    /** Introduced in product version 8.14 */
    @Deprecated
    VERSION_24_0("24.0", true),

    /** Introduced in product version 8.15 */
    @Deprecated
    VERSION_25_0("25.0", true),

    /** Introduced in product version 8.16 */
    @Deprecated
    VERSION_26_0("26.0", true),

    /** Introduced in product version Sunglow (8.20)
     *  Despite being deprecated, this version will stick around for a long time.
     */
    @Deprecated
    VERSION_27_0("27.0", true),

    /** Introduced in product version 8.21 **/
    @Deprecated
    VERSION_28_0("28.0", true),

    /** Introduced in product version Trifecta (9.0) **/
    @Deprecated
    VERSION_29_0("29.0", true),

    /** Introduced in product version Ulysses (9.1) **/
    VERSION_30_0("30.0"),

    /** Introduced in product version Vulcan (9.5) **/
    VERSION_31_0("31.0"),

    /** Introduced in product version Wellington (9.7) **/
    VERSION_32_0("32.0"),

    /** Introduced in product version Xendi **/
    VERSION_33_0("33.0"),

    /** Larger than all versions. Keep last! */
    VERSION_MAX("");

    /**
     * This enum will help manage all API version alias mappings.
     */
    public enum Alias {
        MIN_SUPPORTED(ApiVersion.VERSION_27_0),
        OBJECT_EXTENSIBILITY(ApiVersion.VERSION_16_0),
        VM_AFFINITY_RULES(ApiVersion.VERSION_20_0),
        MAX_SUPPORTED(ApiVersion.VERSION_33_0),
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
        ;

        private final ApiVersion mapping;

        private final Alias parentAlias;

        Alias(ApiVersion mapping) {
            this.mapping = mapping;
            this.parentAlias = null;
        }

        Alias(Alias alias) {
            this.mapping = null;
            this.parentAlias = alias;
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

    private final String version;

    private final boolean isDeprecated;

    ApiVersion(String version) {
        this.version = version;
        this.isDeprecated = false;
    }

    ApiVersion(String version, boolean isDeprecated) {
        this.version = version;
        this.isDeprecated = isDeprecated;
    }

    /**
     * @return the version as string
     */
    public String value() {
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
        if (!ApiVersionCacheHelper.instance.isCached(v)) {
            if (!ApiVersionCacheHelper.instance.isAliasCached(v)) {
                throw new IllegalArgumentException("Unknown API version: " + v);
            }
            return ApiVersionCacheHelper.instance.getAliasValue(v);
        }
        return ApiVersionCacheHelper.instance.getValue(v);
    }

    public static boolean isValidApiVersion(String v) {
        if (!ApiVersionCacheHelper.instance.isCached(v)) {
            if (!ApiVersionCacheHelper.instance.isAliasCached(v)) {
                return false;
            }
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
        // TODO This check shouldn't be here. It only masks a real problem
        // somewhere, as the requested version should never be equal to
        // VERSION_MAX.
        if (max == VERSION_MAX) {
            return min.compareTo(this) <= 0;
        }

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

        final int startIndex = minApiVersion.ordinal();
        final int endIndex = maxApiVersion.ordinal() + 1;

        final ApiVersion[] values = ApiVersion.values();

        return Arrays.asList(Arrays.copyOfRange(values, startIndex, endIndex));
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
}

