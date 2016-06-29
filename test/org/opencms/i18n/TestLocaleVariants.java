/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import junit.framework.Test;

/**
 * Test cases for locale variants.
 */
public class TestLocaleVariants extends OpenCmsTestCase {

    /**
     * Creates a new instance.<p>
     *
     * @param name the test name
     */
    public TestLocaleVariants(String name) {
        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestLocaleVariants.class, "ade-config", "/");
    }

    public CmsResource makeResource(String path, Locale locale) throws Exception {

        CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, locale.toString(), null);
        CmsObject cms = getCmsObject();
        return cms.createResource(path, CmsResourceTypePlain.getStaticTypeId(), null, Arrays.asList(prop));
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

}
