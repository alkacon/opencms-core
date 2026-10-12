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

package org.opencms.ade.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.ade.configuration.CmsADEConfigDataInternal.AttributeValue;
import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReference;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.file.quota.CmsFolderReportEntry;
import org.opencms.file.quota.CmsFolderSizeTable;
import org.opencms.file.quota.CmsQuotaConfig;
import org.opencms.file.quota.CmsQuotaDefinition;
import org.opencms.file.quota.CmsQuotaManager;
import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Tests local, master and connector budget settings without a Tenants module.<p>
 */
public class TestQuotaAttributes {

    /**
     * Test configuration with explicit masters and attributes.<p>
     */
    private static class Config extends CmsADEConfigDataInternal {

        /** The configuration resource. */
        private final CmsResource m_resource;

        /** Local attributes. */
        private final Map<String, AttributeValue> m_attributes = new HashMap<>();

        /**
         * Creates a test configuration.<p>
         *
         * @param root the website or subsite root path, or null for a master configuration
         * @param file the configuration file path
         * @param masters the referenced master configurations
         */
        Config(String root, String file, Config... masters) {

            super(root);
            m_resource = new CmsResource(
                new CmsUUID(),
                new CmsUUID(),
                file,
                0,
                false,
                0,
                null,
                CmsResourceState.STATE_UNCHANGED,
                0,
                null,
                0,
                null,
                0,
                0,
                1,
                0,
                0,
                1);
            m_masterConfigs = new ArrayList<>();
            for (Config master : masters) {
                m_masterConfigs.add(new ConfigReference(master.getResource().getStructureId()));
            }
        }

        @Override
        public Map<String, AttributeValue> getAttributes() {

            return m_attributes;
        }

        @Override
        public CmsResource getResource() {

            return m_resource;
        }

        /**
         * Adds a local attribute.<p>
         *
         * @param key the attribute name
         * @param value the attribute value
         *
         * @return this configuration
         */
        Config attribute(String key, String value) {

            m_attributes.put(key, new AttributeValue(value, m_resource.getRootPath()));
            return this;
        }
    }

    @Test
    public void testConnectorPriorityAndFreshSnapshots() {

        String key = CmsQuotaConfig.ATTR_UPLOAD_QUOTA_DEFAULT;
        Config master = new Config(null, "/masters/base.config").attribute(key, "2");
        Config site = new Config("/site/", "/site/.content/.config", master).attribute(key, "3");
        CmsADEConfigCacheState cache = cache(master, site);
        Map<String, AttributeValue> injected = new HashMap<>();
        injected.put(key, new AttributeValue("4", "first connector"));
        I_CmsSitemapExtraInfo first = path -> injected;
        I_CmsSitemapExtraInfo last = path -> Collections.singletonMap(
            key,
            new AttributeValue("unlimited", "last connector"));
        List<String> roots = Arrays.asList("/site/");
        assertEquals(
            "unlimited",
            cache.getLocalAttributesByPath(roots, Arrays.asList(first, last)).get("/site/").get(key).getValue());
        assertEquals(
            "4",
            cache.getLocalAttributesByPath(roots, Arrays.asList(first)).get("/site/").get(key).getValue());
        injected.clear();
        assertEquals(
            "3",
            cache.getLocalAttributesByPath(roots, Arrays.asList(first)).get("/site/").get(key).getValue());
        site.getAttributes().clear();
        assertEquals(
            "2",
            cache.getLocalAttributesByPath(roots, Arrays.asList(first)).get("/site/").get(key).getValue());
    }

    @Test
    public void testDefaultsDoNotCreateScopes() {

        String scope = CmsQuotaConfig.ATTR_UPLOAD_QUOTA_SCOPE;
        String defaults = CmsQuotaConfig.ATTR_UPLOAD_QUOTA_DEFAULT;
        Config master = new Config(null, "/masters/default.config").attribute(defaults, "3");
        Config site = new Config("/site/", "/site/.content/.config");
        Config ordinary = new Config("/site/language/", "/site/language/.content/.config", master).attribute(
            CmsQuotaConfig.ATTR_UPLOAD_QUOTA,
            "unlimited");
        Config child = new Config("/site/language/tenant/", "/site/language/tenant/.content/.config").attribute(
            scope,
            "true");
        List<String> roots = Arrays.asList("/site/", "/site/language/", "/site/language/tenant/");
        Map<String, Map<String, AttributeValue>> local = cache(master, site, ordinary, child).getLocalAttributesByPath(
            roots,
            Collections.emptyList());
        Map<String, CmsQuotaStatus> statuses = CmsQuotaManager.calculate(
            CmsQuotaManager.getDefinitions(local, Collections.singleton("/site/")),
            Collections.emptyMap(),
            CmsQuotaDefinition.BYTES_PER_GB);
        assertEquals(2, statuses.size());
        assertEquals(CmsQuotaDefinition.BYTES_PER_GB, statuses.get("/site/").getQuotaBytes());
        assertEquals(3 * CmsQuotaDefinition.BYTES_PER_GB, statuses.get("/site/language/tenant/").getQuotaBytes());
        assertFalse(statuses.get("/site/language/tenant/").isIndividualQuota());
    }

    @Test
    public void testIndividualMasterSettingDoesNotLeakToChildren() {

        String individual = CmsQuotaConfig.ATTR_UPLOAD_QUOTA;
        Config master = new Config(null, "/masters/base.config").attribute(individual, "5");
        Config site = new Config("/site/", "/site/.content/.config", master);
        Config child = new Config("/site/child/", "/site/child/.content/.config");
        Config explicit = new Config("/site/child/explicit/", "/site/child/explicit/.content/.config", master);
        Map<String, Map<String, AttributeValue>> local = cache(master, site, child, explicit).getLocalAttributesByPath(
            Arrays.asList("/site/", "/site/child/", "/site/child/explicit/"),
            Collections.emptyList());
        assertEquals("5", local.get("/site/").get(individual).getValue());
        assertNull(local.get("/site/child/").get(individual));
        assertEquals("5", local.get("/site/child/explicit/").get(individual).getValue());
    }

    @Test
    public void testMixedSourcesAndDeepScopes() {

        String individual = CmsQuotaConfig.ATTR_UPLOAD_QUOTA;
        String defaults = CmsQuotaConfig.ATTR_UPLOAD_QUOTA_DEFAULT;
        Config masterBase = new Config(null, "/masters/base.config").attribute(defaults, "2");
        Config master = new Config(null, "/masters/main.config", masterBase).attribute(defaults, "3");
        Config site = new Config("/site/", "/site/.content/.config", master).attribute(individual, "10");
        Config child = new Config("/site/child/", "/site/child/.content/.config").attribute(
            CmsQuotaConfig.ATTR_UPLOAD_QUOTA_SCOPE,
            "true");
        Config deep = new Config("/site/child/deep/", "/site/child/deep/.content/.config", master).attribute(
            defaults,
            "4").attribute(CmsQuotaConfig.ATTR_UPLOAD_QUOTA_SCOPE, "true");
        Config leaf = new Config("/site/child/deep/leaf/", "/site/child/deep/leaf/.content/.config").attribute(
            CmsQuotaConfig.ATTR_UPLOAD_QUOTA_SCOPE,
            "true");
        List<String> roots = Arrays.asList(
            "/site/",
            "/site/child/",
            "/site/child/deep/",
            "/site/child/deep/leaf/",
            "/empty/");
        CmsADEConfigCacheState cache = cache(masterBase, master, site, child, deep, leaf);
        I_CmsSitemapExtraInfo connector = path -> path.equals("/site/")
        ? Collections.singletonMap(individual, new AttributeValue("8", "Tenant: /site/"))
        : null;
        Map<String, Map<String, AttributeValue>> local = cache.getLocalAttributesByPath(
            roots,
            Arrays.asList(connector));
        assertEquals("8", local.get("/site/").get(individual).getValue());
        assertEquals("3", local.get("/site/").get(defaults).getValue());
        assertNull(local.get("/site/child/").get(defaults));
        assertTrue(local.get("/empty/").isEmpty());
        Map<String, CmsQuotaStatus> statuses = CmsQuotaManager.calculate(
            CmsQuotaManager.getDefinitions(local, Arrays.asList("/site/", "/empty/")),
            Collections.emptyMap());
        long gb = CmsQuotaDefinition.BYTES_PER_GB;
        assertEquals(8 * gb, statuses.get("/site/").getQuotaBytes());
        assertEquals(3 * gb, statuses.get("/site/child/").getQuotaBytes());
        assertEquals(4 * gb, statuses.get("/site/child/deep/").getQuotaBytes());
        assertEquals(4 * gb, statuses.get("/site/child/deep/leaf/").getQuotaBytes());
        assertFalse(statuses.get("/site/child/").isIndividualQuota());
        // Removing a connector setting reveals the locally configured value again.
        local = cache.getLocalAttributesByPath(roots, Collections.emptyList());
        assertEquals("10", local.get("/site/").get(individual).getValue());
    }

    @Test
    public void testOrdinarySubsitesShareUsageAndScopeChangesRedistributeIt() throws Exception {

        String scope = CmsQuotaConfig.ATTR_UPLOAD_QUOTA_SCOPE;
        Config site = new Config("/site/", "/site/.content/.config");
        Config de = new Config("/site/de/", "/site/de/.content/.config");
        Config en = new Config("/site/en/", "/site/en/.content/.config");
        Config child = new Config("/site/en/tenant/", "/site/en/tenant/.content/.config").attribute(scope, "true");
        Config deep = new Config("/site/en/tenant/deep/", "/site/en/tenant/deep/.content/.config").attribute(
            scope,
            "true");
        List<String> roots = Arrays.asList(
            "/site/",
            "/site/de/",
            "/site/en/",
            "/site/en/tenant/",
            "/site/en/tenant/deep/");
        CmsADEConfigCacheState cache = cache(site, de, en, child, deep);
        CmsFolderSizeTable sizes = new CmsFolderSizeTable(null, false);
        java.lang.reflect.Field field = CmsFolderSizeTable.class.getDeclaredField("m_folders");
        field.setAccessible(true);
        TreeMap<String, Long> folders = new TreeMap<>();
        folders.put("/site/", 100L);
        folders.put("/site/de/", 200L);
        folders.put("/site/en/", 300L);
        folders.put("/site/en/tenant/", 400L);
        folders.put("/site/en/tenant/deep/", 500L);
        field.set(sizes, folders);
        Collection<CmsQuotaDefinition> definitions = CmsQuotaManager.getDefinitions(
            cache.getLocalAttributesByPath(roots, Collections.emptyList()),
            Collections.singleton("/site/"));
        Map<String, CmsFolderReportEntry> report = sizes.getFolderReport(
            definitions.stream().map(CmsQuotaDefinition::getRootPath).collect(Collectors.toList()));
        Map<String, CmsQuotaStatus> statuses = CmsQuotaManager.calculate(definitions, report);
        assertEquals(1500, statuses.get("/site/").getTotalBytes());
        assertEquals(600, statuses.get("/site/").getUsedBytes());
        assertEquals(400, statuses.get("/site/en/tenant/").getUsedBytes());
        assertEquals(500, statuses.get("/site/en/tenant/deep/").getUsedBytes());
        child.getAttributes().clear();
        definitions = CmsQuotaManager.getDefinitions(
            cache.getLocalAttributesByPath(roots, Collections.emptyList()),
            Collections.singleton("/site/"));
        report = sizes.getFolderReport(
            definitions.stream().map(CmsQuotaDefinition::getRootPath).collect(Collectors.toList()));
        statuses = CmsQuotaManager.calculate(definitions, report);
        assertEquals(2, statuses.size());
        assertEquals(1000, statuses.get("/site/").getUsedBytes());
        assertEquals(500, statuses.get("/site/en/tenant/deep/").getUsedBytes());
    }

    @Test
    public void testScopeMarkersFromMastersAndConnectorsAreLocal() {

        String scope = CmsQuotaConfig.ATTR_UPLOAD_QUOTA_SCOPE;
        Config master = new Config(null, "/masters/scope.config").attribute(scope, "true");
        Config site = new Config("/site/", "/site/.content/.config").attribute(scope, "false");
        Config child = new Config("/site/child/", "/site/child/.content/.config", master);
        Config ordinary = new Config("/site/child/ordinary/", "/site/child/ordinary/.content/.config");
        Config disabled = new Config("/site/disabled/", "/site/disabled/.content/.config", master).attribute(
            scope,
            "false");
        List<String> roots = Arrays.asList("/site/", "/site/child/", "/site/child/ordinary/", "/site/disabled/");
        CmsADEConfigCacheState cache = cache(master, site, child, ordinary, disabled);
        Map<String, Map<String, AttributeValue>> local = cache.getLocalAttributesByPath(roots, Collections.emptyList());
        assertEquals(
            new java.util.HashSet<>(Arrays.asList("/site/", "/site/child/")),
            CmsQuotaManager.getDefinitions(local, Collections.singleton("/site")).stream().map(
                CmsQuotaDefinition::getRootPath).collect(Collectors.toSet()));
        I_CmsSitemapExtraInfo connector = path -> path.equals("/site/disabled/")
        ? Collections.singletonMap(scope, new AttributeValue("true", "Tenant"))
        : null;
        local = cache.getLocalAttributesByPath(roots, Collections.singletonList(connector));
        assertEquals(3, CmsQuotaManager.getDefinitions(local, Collections.singleton("/site/")).size());
    }

    /**
     * Creates a configuration cache without folder type resolution.<p>
     *
     * @param configs the configurations to include
     *
     * @return the cache state
     */
    private CmsADEConfigCacheState cache(Config... configs) {

        Map<CmsUUID, CmsADEConfigDataInternal> map = new HashMap<>();
        for (Config config : configs) {
            map.put(config.getResource().getStructureId(), config);
        }
        return new CmsADEConfigCacheState(
            null,
            map,
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap()) {

            @Override
            public SetMultimap<String, String> computeFolderTypes() {

                return HashMultimap.create();
            }
        };
    }
}
