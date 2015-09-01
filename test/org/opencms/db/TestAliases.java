/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import junit.framework.Test;

/**
 * Test class for alias methods.
 */
public class TestAliases extends OpenCmsTestCase {

    /**
     * Creates a new instance.<p>
     *
     * @param name the test name
     */
    public TestAliases(String name) {

        super(name);
    }

    /**
     * Creates a test suite instance.<p>
     *
     * @return the test suite instance
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestAliases.class, "systemtest", "/");
    }

    /**
     * Checks whether a resource's alias paths exactly match a given list of paths, and throws an exception
     * otherwise.<p>
     *
     * @param resource the resource
     * @param aliasPaths the paths which should match the alias paths of the resource
     *
     * @throws Exception if the aliases don't match
     */
    public void checkAliases(CmsResource resource, String... aliasPaths) throws Exception {

        List<CmsAlias> aliases = OpenCms.getAliasManager().getAliasesForStructureId(
            getCmsObject(),
            resource.getStructureId());
        Map<String, Boolean> aliasMapFromDb = new HashMap<String, Boolean>();
        for (CmsAlias alias : aliases) {
            aliasMapFromDb.put(alias.getAliasPath(), Boolean.TRUE);
        }
        Map<String, Boolean> aliasMapFromParameters = new HashMap<String, Boolean>();
        for (String aliasPath : aliasPaths) {
            aliasMapFromParameters.put(aliasPath, Boolean.TRUE);
        }
        MapDifference<String, Boolean> difference = Maps.difference(aliasMapFromDb, aliasMapFromParameters);
        assertTrue(
            "Aliases for "
                + resource.getRootPath()
                + " (left) don't match expected aliases (right): "
                + difference.toString(),
            difference.areEqual());
    }

    /**
     * Basic test for aliases.
     *
     * @throws Exception if something goes wrong
     */
    public void testAddAlias() throws Exception {

        CmsObject cms = getCmsObject();
        CmsAliasManager aliasManager = OpenCms.getAliasManager();
        CmsResource foo1 = cms.createResource("/system/foo1", CmsResourceTypePlain.getStaticTypeId());
        CmsResource bar1 = cms.createResource("/system/bar1", CmsResourceTypePlain.getStaticTypeId());
        CmsAlias alias = new CmsAlias(foo1.getStructureId(), "", "/xyzzy1", CmsAliasMode.page);
        CmsAlias alias2 = new CmsAlias(bar1.getStructureId(), "", "/xyzzy2", CmsAliasMode.page);
        aliasManager.saveAliases(cms, foo1.getStructureId(), Collections.singletonList(alias));
        aliasManager.saveAliases(cms, bar1.getStructureId(), Collections.singletonList(alias2));
        checkAliases(foo1, "/xyzzy1");
        checkAliases(bar1, "/xyzzy2");
        CmsAlias alias3 = new CmsAlias(foo1.getStructureId(), "", "/xyzzy3", CmsAliasMode.page);
        CmsAlias alias4 = new CmsAlias(foo1.getStructureId(), "", "/xyzzy4", CmsAliasMode.page);
        List<CmsAlias> aliases = new ArrayList<CmsAlias>();
        aliases.add(alias3);
        aliases.add(alias4);
        aliasManager.saveAliases(cms, foo1.getStructureId(), aliases);
        checkAliases(foo1, "/xyzzy3", "/xyzzy4");
        checkAliases(bar1, "/xyzzy2");
        assertTrue("At least 3 aliases", aliasManager.getAliasesForSite(cms, "").size() >= 3);
    }

    /**
     * Tests reading/writing rewrite aliases.<p>
     *
     * @throws Exception
     */
    public void testRewrites() throws Exception {

        CmsUUID id = new CmsUUID();
        String siteRoot = "/sites/default";
        String patternString = "/foo/(.*)";
        String replacementString = "/bar/(.*)";
        CmsAliasMode mode = CmsAliasMode.permanentRedirect;
        CmsRewriteAlias alias = new CmsRewriteAlias(id, siteRoot, patternString, replacementString, mode);
        CmsAliasManager aliasManager = OpenCms.getAliasManager();
        aliasManager.saveRewriteAliases(getCmsObject(), siteRoot, Collections.singletonList(alias));
        List<CmsRewriteAlias> aliases = aliasManager.getRewriteAliases(getCmsObject(), "/sites/default");
        checkRewriteAlias(alias, aliases);
        assertEquals(1, aliases.size());
        aliasManager.saveRewriteAliases(getCmsObject(), siteRoot, Collections.<CmsRewriteAlias> emptyList());
        aliases = aliasManager.getRewriteAliases(getCmsObject(), "/sites/default");
        assertEquals(0, aliases.size());

        CmsRewriteAlias alias2 = new CmsRewriteAlias(
            new CmsUUID(),
            siteRoot,
            patternString,
            replacementString,
            CmsAliasMode.redirect);
        List<CmsRewriteAlias> aliasesToSave = new ArrayList<CmsRewriteAlias>();
        aliasesToSave.add(alias);
        aliasesToSave.add(alias2);
        aliasManager.saveRewriteAliases(getCmsObject(), siteRoot, aliasesToSave);
        aliases = aliasManager.getRewriteAliases(getCmsObject(), "/sites/default");
        assertEquals(2, aliases.size());
        checkRewriteAlias(alias, aliases);
        checkRewriteAlias(alias2, aliases);
    }

    /**
     * Checks if a rewrite alias is contained in a list of rewrite aliases.<p>
     *
     * @param alias the alias to search
     * @param aliasesToSearch the list of  aliases in which to search the alias
     */
    private void checkRewriteAlias(CmsRewriteAlias alias, List<CmsRewriteAlias> aliasesToSearch) {

        boolean found = false;
        for (CmsRewriteAlias currentAlias : aliasesToSearch) {
            if (currentAlias.getId().equals(alias.getId())) {
                assertEquals(alias.getSiteRoot(), currentAlias.getSiteRoot());
                assertEquals(alias.getPatternString(), currentAlias.getPatternString());
                assertEquals(alias.getReplacementString(), currentAlias.getReplacementString());
                assertEquals(alias.getMode(), currentAlias.getMode());
                found = true;
            }
        }
        assertTrue(found);
    }

}
