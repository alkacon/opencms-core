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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Unit tests for the <code>{@link CmsJspVfsAccessBean}</code>.<p>
 *
 * @since 7.0.2
 */
public class TestCmsJspResourceAccessBean extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    @Test
    public void testReadPropertyLocale() throws CmsException {

        CmsObject cms = getCmsObject();

        String folderPath = "/test_read_property_locale";
        String resourcePath = "/test_read_property_locale/test.txt";
        CmsResource folder = cms.createResource(
            folderPath,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
        CmsResource resource = cms.createResource(
            resourcePath,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));

        CmsJspResourceAccessBean bean = new CmsJspResourceAccessBean(cms, resource);

        List<CmsProperty> directProperties = new ArrayList<CmsProperty>();
        List<CmsProperty> searchedProperties = new ArrayList<CmsProperty>();
        String directPropertyName = "direct";
        String searchedPropertyName = "searched";
        List<String> localeSuffixes = Arrays.asList(new String[] {"", "_de", "_de_DE", "_en"});
        Locale[] testLocales = new Locale[] {
            null,
            new Locale("de"),
            new Locale("de", "DE"),
            new Locale("en"),
            new Locale("en", "GB"),
            new Locale("it")};
        String[] expectedPostfix = new String[] {"", "_de", "_de_DE", "_en", "_en", ""};

        for (String suffix : localeSuffixes) {
            directProperties.add(
                new CmsProperty(directPropertyName + suffix, directPropertyName + suffix, directPropertyName + suffix));
            searchedProperties.add(
                new CmsProperty(
                    searchedPropertyName + suffix,
                    searchedPropertyName + suffix,
                    searchedPropertyName + suffix));
        }

        cms.writePropertyObjects(resource, directProperties);
        cms.writePropertyObjects(folder, searchedProperties);

        for (int i = 0; i < testLocales.length; i++) {
            assertEquals(
                directPropertyName + expectedPostfix[i],
                bean.getPropertyLocale().get(null != testLocales[i] ? testLocales[i].toString() : null).get(
                    directPropertyName));
            assertEquals(
                directPropertyName + expectedPostfix[i],
                bean.getPropertyLocale().get(testLocales[i]).get(directPropertyName));
            assertEquals(
                CmsProperty.getNullProperty().getValue(),
                bean.getPropertyLocale().get(null != testLocales[i] ? testLocales[i].toString() : null).get(
                    searchedPropertyName));
            assertEquals(
                CmsProperty.getNullProperty().getValue(),
                bean.getPropertyLocale().get(testLocales[i]).get(searchedPropertyName));
        }
    }
}
