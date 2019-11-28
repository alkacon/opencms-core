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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSitesConfiguration;
import org.opencms.file.CmsResource;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.workplace.tools.sites.CmsSiteBean;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Tests that conversion from CmsSite to CmsSiteBean and back preserves data.
 */
public class TestSiteConfigSerialization extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSiteConfigSerialization(String arg0) {

        super(arg0, false);
    }

    /**
     * Test case.
     *
     * @throws Exception -
     */
    public void testSiteBeanConversion() throws Exception {

        // get the file name of the input resource
        String inputFile = CmsResource.getParentFolder(
            OpenCmsTestProperties.getResourcePathFromClassloader("org/opencms/configuration/opencms.xml"));

        // generate the configuration manager
        CmsConfigurationManager manager = new CmsConfigurationManager(inputFile);

        // now digest the XML
        manager.loadXmlConfiguration();
        CmsSitesConfiguration config = (CmsSitesConfiguration)manager.getConfiguration(CmsSitesConfiguration.class);

        CmsSiteMatcher key1 = new CmsSiteMatcher("http://localhost:8080");
        CmsSiteMatcher key2 = new CmsSiteMatcher("http://other.localhost:8080");
        CmsSite site1 = config.getSiteManager().matchSite(key1);
        CmsSite site2 = config.getSiteManager().matchSite(key2);
        checkSiteBeanConversion(config.getSiteManager(), site1);
        checkSiteBeanConversion(config.getSiteManager(), site2);

    }

    /**
     * Test case.
     * @throws Exception -
     */
    public void testSiteBeanEquals() throws Exception {

        // get the file name of the input resource
        String inputFile = CmsResource.getParentFolder(
            OpenCmsTestProperties.getResourcePathFromClassloader("org/opencms/configuration/opencms.xml"));

        // generate the configuration manager
        CmsConfigurationManager manager = new CmsConfigurationManager(inputFile);

        // now digest the XML
        manager.loadXmlConfiguration();
        CmsSitesConfiguration config = (CmsSitesConfiguration)manager.getConfiguration(CmsSitesConfiguration.class);

        CmsSiteMatcher key1 = new CmsSiteMatcher("http://localhost:8080");
        CmsSiteMatcher key2 = new CmsSiteMatcher("http://other.localhost:8080");
        CmsSite site1 = config.getSiteManager().matchSite(key1);
        CmsSite site2 = config.getSiteManager().matchSite(key2);
        CmsSiteBean bean1 = new CmsSiteBean(site1);
        byte[] data = SerializationUtils.serialize(bean1);
        CmsSiteBean deserializedBean1 = SerializationUtils.deserialize(data);

        CmsSiteBean bean2 = new CmsSiteBean(site2);
        byte[] data2 = SerializationUtils.serialize(bean2);
        CmsSiteBean deserializedBean2 = SerializationUtils.deserialize(data2);
        CmsSiteBean deserializedBean2Copy = SerializationUtils.deserialize(data2);

        assertEquals(bean1, deserializedBean1);
        assertEquals(bean2, deserializedBean2);
        assertEquals(deserializedBean2, deserializedBean2Copy);
        deserializedBean2Copy.setSiteRoot("asfafasf");
        assertNotSame(deserializedBean2, deserializedBean2Copy);
    }

    /**
     * Generates string representation of CmsSite via reflection, for the purpose of comparisons in test cases.
     *
     * @param site the site
     * @return the string format
     */
    String siteToString(CmsSite site) {

        // use short prefix style to avoid object addresses in toString format.
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(site, ToStringStyle.SHORT_PREFIX_STYLE);
        return builder.build();
    }

    /**
     * Checks that converting the site to a serialized site bean and back doesn't lose data.
     *
     * @param siteManager the site manager
     * @param site the site
     *
     * @throws Exception if things go wrong
     */
    private void checkSiteBeanConversion(CmsSiteManagerImpl siteManager, CmsSite site) throws Exception {

        CmsSiteBean bean = new CmsSiteBean(site);
        byte[] data = SerializationUtils.serialize(bean);
        CmsSiteBean deserializedBean = SerializationUtils.deserialize(data);
        CmsSite deserializedSite = deserializedBean.toCmsSite(siteManager);
        assertEquals(
            ReflectionToStringBuilder.toString(bean, ToStringStyle.SHORT_PREFIX_STYLE),
            ReflectionToStringBuilder.toString(deserializedBean, ToStringStyle.SHORT_PREFIX_STYLE));
        assertEquals(siteToString(site), siteToString(deserializedSite));
    }
}
