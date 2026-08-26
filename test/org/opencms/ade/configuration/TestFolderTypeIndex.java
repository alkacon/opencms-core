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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsVfsUtil;

import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests the folder type index of the sitemap configuration, which maps the content folder of a
 * configured resource type to that type. The detail page lookup uses it to decide whether a path can
 * be a detail content at all, so a wrong entry silently disables detail links for the type.<p>
 *
 * The interesting case is a type whose folder is configured by name in a master configuration: the
 * master has no location of its own, so the folder is relative to the configuration that references
 * it, and the index entry has to be built from that configuration's base path.<p>
 */
public class TestFolderTypeIndex extends OpenCmsTestRunner {

    /** The base path of the subsite that references the master configuration. */
    private static final String BASE = "/sites/default/foldertypes";

    /** The name of the content folder configured for the type. */
    private static final String FOLDER_NAME = "myarticles";

    /** The type the master configuration declares. */
    private static final String TYPE = "article1";

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "ade-config", "/");
    }

    /**
     * A type whose folder is configured by name in a master configuration is indexed under the
     * content folder of the configuration that references the master, not under the site root of
     * whatever context the index happens to be built with.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testFolderNameOfMasterConfigIsRelativeToTheReferencingConfig() throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        I_CmsResourceType masterType = OpenCms.getResourceManager().getResourceType("sitemap_master_config");
        I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType("sitemap_config");
        CmsResource master = cms.createResource(
            "/system/foldertypes-master.xml",
            masterType,
            masterConfig().getBytes("UTF-8"),
            Collections.<CmsProperty> emptyList());
        CmsVfsUtil.createFolder(cms, BASE + "/.content");
        cms.createResource(
            BASE + "/.content/.config",
            configType,
            siteConfig(master).getBytes("UTF-8"),
            Collections.<CmsProperty> emptyList());
        OpenCms.getADEManager().waitForCacheUpdate(false);

        assertTrue(

            OpenCms.getADEManager().getParentFolderTypes(
                false,
                BASE + "/.content/" + FOLDER_NAME + "/entry.xml").contains(TYPE),
            "the type's content folder below the referencing configuration should be indexed");
        assertTrue(
            OpenCms.getADEManager().getParentFolderTypes(false, "/.content/" + FOLDER_NAME + "/entry.xml").isEmpty(),
            "the folder name must not be indexed relative to the root site");
    }

    /**
     * Returns a master configuration that declares the type with a folder name.<p>
     *
     * @return the master configuration XML
     */
    private String masterConfig() {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<SitemapMasterConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_master_config.xsd\">\n"
            + "  <SitemapMasterConfiguration language=\"en\">\n"
            + "    <ResourceType>\n"
            + "      <TypeName><![CDATA["
            + TYPE
            + "]]></TypeName>\n"
            + "      <Disabled>false</Disabled>\n"
            + "      <Folder>\n"
            + "        <Name><![CDATA["
            + FOLDER_NAME
            + "]]></Name>\n"
            + "      </Folder>\n"
            + "      <NamePattern><![CDATA[entry_%(number).xml]]></NamePattern>\n"
            + "    </ResourceType>\n"
            + "  </SitemapMasterConfiguration>\n"
            + "</SitemapMasterConfigurations>\n";
    }

    /**
     * Returns a sitemap configuration that references the master configuration.<p>
     *
     * @param master the master configuration resource
     *
     * @return the sitemap configuration XML
     */
    private String siteConfig(CmsResource master) {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<SitemapConfigurationsV2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\n"
            + "  <SitemapConfigurationV2 language=\"en\">\n"
            + "    <MasterConfig>\n"
            + "      <link type=\"WEAK\">\n"
            + "        <target><![CDATA["
            + master.getRootPath()
            + "]]></target>\n"
            + "        <uuid>"
            + master.getStructureId()
            + "</uuid>\n"
            + "      </link>\n"
            + "    </MasterConfig>\n"
            + "  </SitemapConfigurationV2>\n"
            + "</SitemapConfigurationsV2>\n";
    }
}
