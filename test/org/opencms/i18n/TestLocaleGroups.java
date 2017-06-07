/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Sets;

import junit.framework.Test;

/**
 * Test cases for locale variants.
 */
public class TestLocaleGroups extends OpenCmsTestCase {

    public static int NAME_COUNTER = 10;

    /**
     * Creates a new instance.<p>
     *
     * @param name the test name
     */
    public TestLocaleGroups(String name) {
        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestLocaleGroups.class, "ade-config", "/");
    }

    public CmsResource makeResource(String path, Locale locale) throws Exception {

        CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, locale.toString(), null);
        CmsObject cms = getCmsObject();
        return cms.createResource(path, CmsResourceTypePlain.getStaticTypeId(), null, Arrays.asList(prop));
    }

    public void testFailWhenAddingResourceFromExistingGroup() throws Exception {

        CmsObject cms = getCmsObject();
        int index = NAME_COUNTER++;
        String basepath = "/test_" + index + "_";
        CmsResource r1 = makeResource(basepath + "1", Locale.ENGLISH);
        CmsResource r2 = makeResource(basepath + "2", Locale.GERMAN);
        CmsResource r3 = makeResource(basepath + "3", Locale.ENGLISH);
        CmsResource r4 = makeResource(basepath + "4", Locale.GERMAN);
        CmsLocaleGroupService service = new CmsLocaleGroupService(cms);
        service.attachLocaleGroup(r1, r2);
        service.attachLocaleGroup(r3, r4);
        try {
            service.attachLocaleGroup(r1, r3);
            fail("Should not be able to attach a resource in a locale group to another locale group");
        } catch (Exception e) {

        }

        try {
            service.attachLocaleGroup(r1, r4);
            fail("Should not be able to attach a resource in a locale group to another locale group");
        } catch (Exception e) {

        }

        try {
            service.attachLocaleGroup(r2, r3);
            fail("Should not be able to attach a resource in a locale group to another locale group");
        } catch (Exception e) {

        }

        try {
            service.attachLocaleGroup(r2, r4);
            fail("Should not be able to attach a resource in a locale group to another locale group");
        } catch (Exception e) {

        }

    }

    public void testJoinLocaleGroup() throws Exception {

        CmsObject cms = getCmsObject();
        String sourcePath = "/test_jlg";
        String targetPath = "/test_jlg_target";
        CmsResource r1 = makeResource(sourcePath, Locale.ENGLISH);
        CmsResource r2 = makeResource(targetPath, Locale.GERMAN);
        CmsLocaleGroupService service = new CmsLocaleGroupService(cms);
        service.attachLocaleGroup(r1, r2);
        CmsLocaleGroup localeGroup = service.readLocaleGroup(r2);
        assertEquals("wrong primary resource in locale group", r2, localeGroup.getPrimaryResource());
        assertEquals(
            "wrong secondary resources in locale group",
            Sets.newHashSet(r1),
            localeGroup.getSecondaryResources());

        CmsLocaleGroup localeGroup2 = service.readLocaleGroup(r1);
        assertEquals("wrong primary resource in locale group", r2, localeGroup2.getPrimaryResource());
        assertEquals(
            "wrong secondary resources in locale group",
            Sets.newHashSet(r1),
            localeGroup2.getSecondaryResources());

    }

    public void testLocales1() throws Exception {

        CmsObject cms = getCmsObject();
        int index = NAME_COUNTER++;
        String basepath = "/test_" + index + "_";
        CmsResource r1 = makeResource(basepath + "1", Locale.ENGLISH);
        CmsResource r2 = makeResource(basepath + "2", Locale.GERMAN);
        CmsResource r3 = makeResource(basepath + "3", Locale.FRENCH);
        CmsLocaleGroupService service = new CmsLocaleGroupService(cms);
        service.attachLocaleGroup(r1, r2);
        service.attachLocaleGroup(r3, r2);
        CmsLocaleGroup group = service.readLocaleGroup(r1);
        assertEquals("wrong primary resource", r2, group.getPrimaryResource());
        assertEquals(
            "wrong secondary resources",
            Sets.newHashSet(r1, r3),
            Sets.newHashSet(group.getSecondaryResources()));
        assertEquals(
            "wrong resource for French",
            Sets.newHashSet(r3),
            Sets.newHashSet(group.getResourcesForLocale(Locale.FRENCH)));
        assertEquals(
            "wrong resource for German",
            Sets.newHashSet(r2),
            Sets.newHashSet(group.getResourcesForLocale(Locale.GERMAN)));
    }

    /**
     * Test method.
     *
     * @throws Exception
     */
    public void testLocaleVariantRelationsNotCopied() throws Exception {

        CmsObject cms = getCmsObject();
        String sourcePath = "/test1";
        CmsResource r1 = makeResource(sourcePath, Locale.ENGLISH);
        CmsResource r2 = makeResource("/test1_target", Locale.GERMAN);
        cms.addRelationToResource(r1, r2, CmsRelationType.LOCALE_VARIANT.getName());
        String copyPath = "/test1_copy";
        cms.copyResource(sourcePath, copyPath, CmsResource.COPY_AS_NEW);
        CmsResource copy = cms.readResource(copyPath);
        List<CmsRelation> rels = cms.readRelations(CmsRelationFilter.ALL.filterStructureId(copy.getStructureId()));
        assertEquals("no relations should exist on copy of locale variant", new ArrayList<CmsRelation>(), rels);
    }

    public void testNormalizeRelations1() throws Exception {

        CmsObject cms = getCmsObject();
        String path1 = "/testnormalize-1-1.txt";
        String path2 = "/testnormalize-1-2.txt";
        CmsResource r1 = makeResource(path1, Locale.ENGLISH);
        CmsResource r2 = makeResource(path2, Locale.GERMAN);

        cms.addRelationToResource(r1, r2, CmsRelationType.LOCALE_VARIANT.getName());
        cms.addRelationToResource(r2, r1, CmsRelationType.LOCALE_VARIANT.getName());
        cms.addRelationToResource(r1, r2, CmsRelationType.CATEGORY.getName());

        assertEquals(
            0,
            cms.readRelations(
                CmsRelationFilter.relationsFromStructureId(r1.getStructureId()).filterType(
                    CmsRelationType.LOCALE_VARIANT)).size());
        assertEquals(
            1,
            cms.readRelations(
                CmsRelationFilter.relationsFromStructureId(r2.getStructureId()).filterType(
                    CmsRelationType.LOCALE_VARIANT)).size());

        assertEquals(
            0,
            cms.readRelations(
                CmsRelationFilter.relationsToStructureId(r2.getStructureId()).filterType(
                    CmsRelationType.LOCALE_VARIANT)).size());
        assertEquals(
            1,
            cms.readRelations(
                CmsRelationFilter.relationsToStructureId(r1.getStructureId()).filterType(
                    CmsRelationType.LOCALE_VARIANT)).size());

        assertEquals(1, cms.readRelations(CmsRelationFilter.relationsFromStructureId(r1.getStructureId())).size());

    }

    public void testNormalizeRelations2() throws Exception {

        CmsObject cms = getCmsObject();
        String path1 = "/testnormalize-2-1.txt";
        String path2 = "/testnormalize-2-2.txt";
        String path3 = "/testnormalize-2-3.txt";
        String path4 = "/testnormalize-2-4.txt";
        CmsResource r1 = makeResource(path1, Locale.ENGLISH);
        CmsResource r2 = makeResource(path2, Locale.GERMAN);
        CmsResource r3 = makeResource(path3, Locale.ITALIAN);
        CmsResource r4 = makeResource(path4, Locale.FRENCH);

        cms.addRelationToResource(r2, r1, CmsRelationType.LOCALE_VARIANT.getName());
        cms.addRelationToResource(r3, r1, CmsRelationType.LOCALE_VARIANT.getName());
        cms.addRelationToResource(r4, r3, CmsRelationType.LOCALE_VARIANT.getName());

        assertEquals(0, cms.readRelations(CmsRelationFilter.relationsFromStructureId(r3.getStructureId())).size());
        assertEquals(1, cms.readRelations(CmsRelationFilter.relationsToStructureId(r1.getStructureId())).size());
        assertEquals(1, cms.readRelations(CmsRelationFilter.relationsToStructureId(r3.getStructureId())).size());

    }

    public void testSameLocaleGroupWhenReadFromDifferentResources() throws Exception {

        CmsObject cms = getCmsObject();
        String sourcePath = "/test_3";
        String sourcePath2 = "/test_3_2";
        String targetPath = "/test_3_target";

        CmsResource r1 = makeResource(sourcePath, Locale.ENGLISH);
        CmsResource r2 = makeResource(sourcePath2, Locale.FRANCE);
        CmsResource r3 = makeResource(targetPath, Locale.GERMAN);

        CmsLocaleGroupService service = new CmsLocaleGroupService(cms);
        service.attachLocaleGroup(r1, r3);
        service.attachLocaleGroup(r2, r3);
        CmsLocaleGroup localeGroup = service.readLocaleGroup(r1);
        CmsLocaleGroup localeGroup2 = service.readLocaleGroup(r2);
        CmsLocaleGroup localeGroup3 = service.readLocaleGroup(r3);
        assertEquals(localeGroup.getPrimaryResource(), localeGroup2.getPrimaryResource());
        assertEquals(localeGroup.getSecondaryResources(), localeGroup2.getSecondaryResources());

        assertEquals(localeGroup2.getPrimaryResource(), localeGroup3.getPrimaryResource());
        assertEquals(localeGroup2.getSecondaryResources(), localeGroup3.getSecondaryResources());
    }

    public void testSingleResourceGroup() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/test_single";
        CmsResource r1 = makeResource(path, Locale.ENGLISH);
        CmsLocaleGroupService service = new CmsLocaleGroupService(cms);
        CmsLocaleGroup group = service.readLocaleGroup(r1);
        assertFalse("single-resource groups are not 'real' groups", group.isRealGroup());
        assertEquals(r1, group.getPrimaryResource());

    }

}
