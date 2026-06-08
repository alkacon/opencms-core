/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.storage.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Engine for evaluating storage policies to determine the appropriate storage location for binary content.<p>
 *
 * This engine allows the registration of rules and whitelist exceptions. It evaluates whether a blob
 * should be stored in a specialized storage.
 */
public class CmsStoragePolicyEngine {

    /**
     * A rule implementation that combines multiple rules using a logical AND operation.
     */
    public static class AndRule implements StorageRule {

        /** The list of nested rules. */
        private final List<StorageRule> rules;

        /**
         * Creates a new AND-rule.
         * @param rules the rules to combine
         */
        public AndRule(StorageRule... rules) {

            this.rules = Arrays.asList(rules);
        }

        /**
         * @see org.opencms.db.storage.policy.CmsStoragePolicyEngine.StorageRule#shouldStoreOffloaded(CmsStoragePolicyContext)
         */
        @Override
        public Boolean shouldStoreOffloaded(CmsStoragePolicyContext context) {

            for (StorageRule rule : rules) {
                Boolean result = rule.shouldStoreOffloaded(context);
                if ((result == null) || !result) {
                    return null;
                }
            }
            return true;
        }
    }

    /**
     * Functional interface for a single storage evaluation rule.
     */
    @FunctionalInterface
    public interface StorageRule {

        /**
         * Evaluates if the content should be stored in the external storage.
         *
         * @param context the storage policy context
         *
         * @return {@link Boolean#TRUE} if it should be offloaded,
         *         {@link Boolean#FALSE} if it should stay local,
         *         or <code>null</code> if the rule does not apply.
         */
        Boolean shouldStoreOffloaded(CmsStoragePolicyContext context);
    }

    /** The list of whitelist rules. */
    private final List<StorageRule> whitelist = new ArrayList<>();

    /** The list of storage rules. */
    private final List<StorageRule> storageRules = new ArrayList<>();

    /**
     * Adds a rule that triggers offloading.
     * @param storageRule the rule to add
     */
    public void addStorageRule(StorageRule storageRule) {

        storageRules.add(storageRule);
    }

    /**
     * Adds a whitelist rule. If this rule matches, the content is always stored locally.
     * @param storageRule the rule to add
     */
    public void addWhitelistRule(StorageRule storageRule) {

        whitelist.add(storageRule);
    }

    /**
     * Returns true if the given content matches any whitelist rule.
     *
     * @param context the storage policy context
     *
     * @return <code>true</code> if whitelisted
     */
    public boolean isWhitelisted(CmsStoragePolicyContext context) {

        return whitelist.stream().anyMatch(r -> Boolean.TRUE.equals(r.shouldStoreOffloaded(context)));
    }

    /**
     * Evaluates if the given content should be stored in the external storage.
     * <p>
     * First checks the whitelist. If not whitelisted, it checks if any storage rule applies.
     * </p>
     *
     * @param context the storage policy context
     *
     * @return <code>true</code> if external storage is required
     */
    public boolean shouldStoreInStorage(CmsStoragePolicyContext context) {

        if (isWhitelisted(context)) {
            return false;
        }

        return storageRules.stream().map(r -> r.shouldStoreOffloaded(context)).filter(Objects::nonNull).anyMatch(
            result -> result);
    }
}
