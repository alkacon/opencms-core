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

package org.opencms.ade.configuration.formatters;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsTestConfigData;
import org.opencms.ade.configuration.TestConfig;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for formatter configurations.<p>
 */
public class TestFormatterConfiguration extends OpenCmsTestCase {

    /** A resource type which is used for the formatter configuration tests. */
    public static final String TYPE_A = "article";

    /** A resource type which is used for the formatter configuration tests. */
    public static final String TYPE_B = "article1";

    /** Formatter resource. */
    static CmsResource m_exampleFormatter;

    /** Example content resource. */
    static CmsResource m_exampleResourceA;

    /** Example content resource. */
    static CmsResource m_exampleResourceB;

    /**
     * Test constructor.<p>
     *
     * @param name the name of the test
     */
    public TestFormatterConfiguration(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        try {
            TestSuite suite = generateTestSuite(TestFormatterConfiguration.class);
            TestSetup wrapper = new TestSetup(suite) {

                /**
                 * @see junit.extensions.TestSetup#setUp()
                 */
                @Override
                protected void setUp() {

                    CmsObject cms = setupOpenCms("simpletest", "/");
                    try {
                        CmsFormatterConfigurationCache.UPDATE_DELAY_MILLIS = 100;
                        m_exampleFormatter = cms.createResource("/system/f1.jsp", getTypeId("jsp"));
                        m_exampleResourceA = cms.createResource("/system/xa.xml", getTypeId(TYPE_A));
                        m_exampleResourceB = cms.createResource("/system/xb.xml", getTypeId(TYPE_B));
                        // add jsps referenced as formatters in article1.xsd
                        cms.createResource("/system/formatters", getTypeId("folder"));
                        cms.createResource("/system/formatters/article1_f1.jsp", getTypeId("jsp"));
                        cms.createResource("/system/formatters/article1_f2.jsp", getTypeId("jsp"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                /**
                 * @see junit.extensions.TestSetup#tearDown()
                 */
                @Override
                protected void tearDown() {

                    removeOpenCms();
                }
            };
            return wrapper;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the type id for a given name.<p>
     *
     * @param name the type name
     * @return the type id
     */
    static int getTypeId(String name) {

        try {
            return OpenCms.getResourceManager().getResourceType(name).getTypeId();
        } catch (CmsException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests adding a formatter through the configuration.
     *
     * @throws CmsException
     */
    public void testAddFormatter() throws CmsException {

        I_CmsFormatterBean f1 = createFormatter(TYPE_A, "f1", 1000, true);
        I_CmsFormatterBean f2 = createFormatter(TYPE_A, "f2", 1000, false);
        I_CmsFormatterBean f3 = createFormatter(TYPE_B, "f3", 1000, false);
        CmsTestConfigData config = createConfig("/", f1, f2, f3);
        CmsTestConfigData config2 = createConfig("/invalid-name", f1, f2, f3);
        config2.setParent(config);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Collections.<String> emptyList(),
            Arrays.asList("" + CmsUUID.getConstantUUID("f2"), "" + CmsUUID.getConstantUUID("f3")),
            null,
            false,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet);

        CmsFormatterConfiguration formatterConfig = config2.getFormatters(getCmsObject(), m_exampleResourceA);

        Set<String> actualNames = new HashSet<String>();
        Set<String> expectedNames = new HashSet<String>(Arrays.asList("f1", "f2"));
        for (I_CmsFormatterBean formatter : formatterConfig.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        assertEquals("Formatter names don't match the active formatters for this type", expectedNames, actualNames);
    }

    /**
     * Tests that the "AutoEnabled" setting on formatters works.
     *
     * @throws CmsException
     */
    public void testAutoEnabled() throws CmsException {

        I_CmsFormatterBean f1 = createFormatter(TYPE_A, "f1", 1000, true);
        I_CmsFormatterBean f2 = createFormatter(TYPE_A, "f2", 1000, false);
        I_CmsFormatterBean f3 = createFormatter(TYPE_A, "f3", 1000, true);
        I_CmsFormatterBean f4 = createFormatter(TYPE_A, "f4", 1000, false);
        I_CmsFormatterBean f5 = createFormatter(TYPE_B, "f5", 1000, true);
        CmsTestConfigData config = createConfig("/", f1, f2, f3, f4, f5);

        CmsFormatterConfiguration formatterConfig = config.getFormatters(getCmsObject(), m_exampleResourceA);
        Set<String> actualNames = new HashSet<String>();
        Set<String> expectedNames = new HashSet<String>(Arrays.asList("f1", "f3"));
        for (I_CmsFormatterBean formatter : formatterConfig.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        assertEquals(
            "Formatter names don't match the auto-enabled formatters for this type",
            expectedNames,
            actualNames);
    }

    /**
     * Tests default formatter selection.<p>
     *
     * @throws Exception
     */
    public void testDefaultFormatterSelection() throws Exception {

        I_CmsFormatterBean f1 = createWidthBasedFormatter("f1", 100, 100, 999);
        I_CmsFormatterBean f2 = createWidthBasedFormatter("f2", 100, 200, 999);
        I_CmsFormatterBean f3 = createWidthBasedFormatter("f3", 100, 300, 999);
        I_CmsFormatterBean f4 = createWidthBasedFormatter("f4", 100, 301, 349);
        CmsTestConfigData config = createConfig("/", f1, f2, f3, f4);
        CmsFormatterConfiguration formatterConfig = config.getFormatters(getCmsObject(), m_exampleResourceA);
        assertEquals(
            "Widest formtter with width < 250 should have matched",
            "f2",
            formatterConfig.getDefaultFormatter("foo", 250).getNiceName(java.util.Locale.ENGLISH));
        assertEquals(
            "Widest formatter with width < 350 and maxWidth >= 350 should have matched",
            "f3",
            formatterConfig.getDefaultFormatter("foo", 350).getNiceName(java.util.Locale.ENGLISH));

        I_CmsFormatterBean f5 = createTypeBasedFormatter("f5", 100, "foo");
        config = createConfig("/", f1, f2, f3, f4, f5);
        formatterConfig = config.getFormatters(getCmsObject(), m_exampleResourceA);
        assertEquals(
            "Type based formatter should have matched",
            "f5",
            formatterConfig.getDefaultFormatter("foo", 350).getNiceName(java.util.Locale.ENGLISH));

        I_CmsFormatterBean f6 = createWidthBasedFormatter("f6", 200, 200, 999);
        config = createConfig("/", f1, f2, f3, f4, f5, f6);
        formatterConfig = config.getFormatters(getCmsObject(), m_exampleResourceA);
        assertEquals(
            "Formatter with higher ranking should have matched",
            "f6",
            formatterConfig.getDefaultFormatter("foo", 350).getNiceName(java.util.Locale.ENGLISH));

    }

    /**
     * Tests that the formatter cache is updated correctly.
     *
     * @throws Exception
     */
    public void testLiveFormatterConfig() throws Exception {

        try {
            String formatterXml = createFormatterConfigXml("plain", "foobarx", true, 100);
            byte[] formatterBytes = formatterXml.getBytes("UTF-8");
            CmsObject cms = getCmsObject();
            cms.createResource(
                "/system/formatter1.fc",
                getTypeId("formatter_config"),
                formatterBytes,
                new ArrayList<CmsProperty>());

            String formatterXml2 = createFormatterConfigXml("plain", "foobar2", true, 100);
            formatterBytes = formatterXml2.getBytes("UTF-8");
            cms.createResource(
                "/system/formatter2.fc",
                getTypeId("formatter_config"),
                formatterBytes,
                new ArrayList<CmsProperty>());

            OpenCms.getADEManager().waitForFormatterCache(false);
            Collection<I_CmsFormatterBean> formatters = OpenCms.getADEManager().getCachedFormatters(
                false).getFormatters().values();
            assertTrue(
                "Formatter 'foobarx' should have been in cached formatter configuration, but was not there",
                getFormatterNames(formatters).contains("foobarx"));
            cms.deleteResource("/system/formatter1.fc", CmsResource.DELETE_PRESERVE_SIBLINGS);
            OpenCms.getADEManager().waitForFormatterCache(false);
            formatters = OpenCms.getADEManager().getCachedFormatters(false).getFormatters().values();
            assertTrue(
                "Formatter 'foobarx' should not be available anymore, but is.",
                !getFormatterNames(formatters).contains("foobarx"));

            assertTrue("Formatter 'foobar2' should be available.", getFormatterNames(formatters).contains("foobar2"));

        } finally {
            delete("/system/formatter1.fc");
            delete("/system/formatter2.fc");
        }

    }

    /**
     * Tests the shared setting / setting override feature.
     *
     * @throws Exception
     */
    public void testOverrideSettings() throws Exception {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        try {
            createFolder(cms, "/system/override-test");
            createFolder(cms, "/system/override-test/.content");
            String folder = "/system/override-test/";
            CmsResource jsp = createFile(cms, folder + "formatter.jsp", "jsp", "<div></div>");
            String settingsConfigText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SettingsConfigs xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/formatters/settings_config.xsd\">\n"
                + "  <SettingsConfig language=\"en\">\n"
                + "    <Setting>\n"
                + "      <IncludeName><![CDATA[include.foo]]></IncludeName>\n"
                + "      <PropertyName><![CDATA[foo]]></PropertyName>\n"
                + "      <DisplayName><![CDATA[value from shared setting]]></DisplayName>\n"
                + "      <Description><![CDATA[value from shared setting]]></Description>\n"
                + "      <Default><![CDATA[value from shared setting]]></Default>\n"
                + "      <WidgetConfig><![CDATA[value from shared setting]]></WidgetConfig>\n"
                + "      <Error><![CDATA[value from shared setting]]></Error>\n"
                + "    </Setting>\n"
                + "  </SettingsConfig>\n"
                + "</SettingsConfigs>\n"
                + "";
            CmsResource sharedSettings = createFile(
                cms,
                folder + "shared-settings.xml",
                "settings_config",
                settingsConfigText);

            String overrideConfigText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SettingsConfigs xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/formatters/settings_config.xsd\">\n"
                + "  <SettingsConfig language=\"en\">\n"
                + "    <Setting>\n"
                + "      <IncludeName><![CDATA[include.foo]]></IncludeName>\n"
                + "      <PropertyName><![CDATA[foo]]></PropertyName>\n"
                + "      <Default><![CDATA[value from override]]></Default>\n"
                + "    </Setting>\n"
                + "  </SettingsConfig>\n"
                + "</SettingsConfigs>\n"
                + "";
            CmsResource overrideFile = createFile(
                cms,
                folder + "override-settings.xml",
                "settings_config",
                overrideConfigText);

            String formatterConfigText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<NewFormatters xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/formatters/new_formatter.xsd\">\n"
                + "  <NewFormatter language=\"en\">\n"
                + "    <NiceName><![CDATA[test formatter]]></NiceName>\n"
                + "    <Type><![CDATA[binary]]></Type>\n"
                + "    <Key><![CDATA[test-formatter]]></Key>\n"
                + "    <Jsp>\n"
                + "      <link type=\"WEAK\">\n"
                + "        <target><![CDATA[/system/override-test/formatter.jsp]]></target>\n"
                + "        <uuid>"
                + jsp.getStructureId()
                + "</uuid>\n"
                + "      </link>\n"
                + "    </Jsp>\n"
                + "    <Rank><![CDATA[1000]]></Rank>\n"
                + "    <Match>\n"
                + "      <Types>\n"
                + "        <ContainerType><![CDATA[element]]></ContainerType>\n"
                + "      </Types>\n"
                + "    </Match>\n"
                + "    <AutoEnabled>true</AutoEnabled>\n"
                + "    <SearchContent>true</SearchContent>\n"
                + "    <StrictContainers>true</StrictContainers>\n"
                + "    <IncludeSettings>\n"
                + "      <link type=\"WEAK\">\n"
                + "        <target><![CDATA[/system/override-test/shared-settings.xml]]></target>\n"
                + "        <uuid>"
                + sharedSettings.getStructureId()
                + "</uuid>\n"
                + "      </link>\n"
                + "    </IncludeSettings>\n"
                + "    <Setting>\n"
                + "      <IncludeName><![CDATA[include.foo]]></IncludeName>\n"
                + "      <DisplayName><![CDATA[value from formatter]]></DisplayName>\n"
                + "      <Widget><![CDATA[string]]></Widget>\n"
                + "      <WidgetConfig><![CDATA[value from formatter]]></WidgetConfig>\n"
                + "    </Setting>\n"
                + "  </NewFormatter>\n"
                + "</NewFormatters>\n"
                + "";
            createFile(cms, folder + "formatter.xml", "formatter_config", formatterConfigText);

            String sitemapConfigText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SitemapConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\n"
                + "  <SitemapConfiguration language=\"en\">\n"
                + "    <UseFormatterKeys>true</UseFormatterKeys>\n"
                + "    <SharedSettingOverride>\n"
                + "      <link type=\"WEAK\">\n"
                + "        <target><![CDATA[/system/override-test/override-settings.xml]]></target>\n"
                + "        <uuid>"
                + overrideFile.getStructureId()
                + "</uuid>\n"
                + "      </link>\n"
                + "    </SharedSettingOverride>\n"
                + "  </SitemapConfiguration>\n"
                + "</SitemapConfigurations>\n"
                + "";
            createFile(cms, folder + ".content/.config", "sitemap_config", sitemapConfigText);
            OpenCms.getADEManager().waitForCacheUpdate(false);
            OpenCms.getADEManager().waitForFormatterCache(false);
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, "/system/override-test");
            I_CmsFormatterBean formatter = config.findFormatter("test-formatter");
            CmsXmlContentProperty setting = formatter.getSettings(config).get("foo");
            assertEquals("value from override", setting.getDefault());
            assertEquals("value from formatter", setting.getWidgetConfiguration());
            assertEquals("value from formatter", setting.getNiceName());
            assertEquals("value from shared setting", setting.getError());
        } finally {
            delete("/system/override-test");
        }
    }

    /**
     * Tests that the formatter cache is updated correctly.
     *
     * @throws Exception
     */
    public void testOverwriteFormatterConfig() throws Exception {

        try {
            String formatterXml = createFormatterConfigXml("plain", "foobarx", true, 100);
            byte[] formatterBytes = formatterXml.getBytes("UTF-8");
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());

            cms.createResource(
                "/system/formatter1.fc",
                getTypeId("formatter_config"),
                formatterBytes,
                new ArrayList<CmsProperty>());
            OpenCms.getADEManager().waitForFormatterCache(false);

            formatterXml = createFormatterConfigXml("plain", "foobar2", true, 100);
            formatterBytes = formatterXml.getBytes("UTF-8");
            CmsFile file = cms.readFile("/system/formatter1.fc");
            file.setContents(formatterBytes);
            cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
            cms.writeFile(file);
            OpenCms.getADEManager().waitForFormatterCache(false);
            Collection<I_CmsFormatterBean> formatters = OpenCms.getADEManager().getCachedFormatters(
                false).getFormatters().values();
            assertEquals(names("foobar2"), getFormatterNames(formatters));
        } finally {
            delete("/system/formatter1.fc");
            delete("/system/formatter2.fc");
        }
    }

    /**
     * Tests removal of all formatters through the configuration.<p>
     *
     * @throws CmsException
     */
    public void testRemoveAllFormattersAndAddExplicitly() throws CmsException {

        int typeB = OpenCms.getResourceManager().getResourceType(TYPE_B).getTypeId();

        I_CmsFormatterBean f1 = createFormatter(TYPE_A, "f1", 1000, true);
        I_CmsFormatterBean f2 = createFormatter(TYPE_A, "f2", 1000, true);
        I_CmsFormatterBean f3 = createFormatter(TYPE_B, "f3", 1000, true);
        I_CmsFormatterBean s3 = createFormatter(TYPE_B, "s3", 1000, true);

        CmsTestConfigData config = createConfig("/", f1, f2, f3);
        CmsTestConfigData config2 = createConfig("/invalid-name", f1, f2, f3);
        config.registerSchemaFormatters(typeB, CmsFormatterConfiguration.create(getCmsObject(), Arrays.asList(s3)));

        config2.setParent(config);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST,
            null,
            true,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet);

        config2.registerSchemaFormatters(typeB, CmsFormatterConfiguration.create(getCmsObject(), Arrays.asList(s3)));

        CmsFormatterConfiguration formatterConfigA = config2.getFormatters(getCmsObject(), m_exampleResourceA);
        CmsFormatterConfiguration formatterConfigB = config2.getFormatters(getCmsObject(), m_exampleResourceB);

        Set<String> actualNames = new HashSet<String>();
        Set<String> expectedNames = new HashSet<String>();
        for (I_CmsFormatterBean formatter : formatterConfigA.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        for (I_CmsFormatterBean formatter : formatterConfigB.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        assertEquals("There should be no formatters available at all.", expectedNames, actualNames);
        CmsFormatterChangeSet changeSet2 = new CmsFormatterChangeSet(
            Collections.EMPTY_LIST,
            Arrays.asList("" + CmsUUID.getConstantUUID("f1"), "type_" + TYPE_B),
            null,
            true,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet2);
        formatterConfigA = config2.getFormatters(getCmsObject(), m_exampleResourceA);
        formatterConfigB = config2.getFormatters(getCmsObject(), m_exampleResourceB);

        actualNames = new HashSet<String>();
        expectedNames = new HashSet<String>(Arrays.asList("f1", "s3"));
        for (I_CmsFormatterBean formatter : formatterConfigA.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        for (I_CmsFormatterBean formatter : formatterConfigB.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        assertEquals("Only explicitly added formatters should be available.", expectedNames, actualNames);
    }

    /**
     * Tests adding/removing schema formatters.<p>
     *
     * @throws CmsException
     */
    @SuppressWarnings("unchecked")
    public void testRemoveAndAddSchemaFormatters() throws CmsException {

        int typeA = OpenCms.getResourceManager().getResourceType(TYPE_A).getTypeId();
        int typeB = OpenCms.getResourceManager().getResourceType(TYPE_B).getTypeId();
        CmsObject cms = getCmsObject();
        I_CmsFormatterBean f1 = createFormatter(TYPE_A, "f1", 1000, true);
        I_CmsFormatterBean s1 = createFormatter(TYPE_A, "s1", 1000, false);
        I_CmsFormatterBean s2 = createFormatter(TYPE_A, "s2", 1000, false);
        I_CmsFormatterBean s3 = createFormatter(TYPE_B, "s3", 1000, false);

        CmsTestConfigData config = createConfig("/", f1);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Arrays.asList("type_" + TYPE_A),
            Collections.EMPTY_LIST,
            null,
            false,
            false,
            null,
            null);
        config.setFormatterChangeSet(changeSet);
        CmsTestConfigData config2 = createConfig("/", f1);
        config2.setParent(config);
        for (CmsTestConfigData currentConfig : Arrays.asList(config, config2)) {
            currentConfig.registerSchemaFormatters(typeA, CmsFormatterConfiguration.create(cms, Arrays.asList(s1, s2)));
            currentConfig.registerSchemaFormatters(typeB, CmsFormatterConfiguration.create(cms, Arrays.asList(s3)));
        }

        CmsFormatterChangeSet changeSet2 = new CmsFormatterChangeSet(
            Collections.EMPTY_LIST,
            Arrays.asList("type_" + TYPE_A),
            null,
            false,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet2);

        CmsFormatterConfiguration formatterConfig = config.getFormatters(cms, m_exampleResourceA);
        assertEquals(
            "Schema formatters for TYPE_A should have been removed",
            names("f1"),
            getFormatterNames(formatterConfig.getAllFormatters()));

        CmsFormatterConfiguration formatterConfig2 = config2.getFormatters(cms, m_exampleResourceA);
        assertEquals(
            "Schema formatters for TYPE_A should have been added again.",
            names("f1", "s1", "s2"),
            getFormatterNames(formatterConfig2.getAllFormatters()));

        CmsFormatterConfiguration formatterConfig3 = config2.getFormatters(cms, m_exampleResourceB);
        assertEquals(
            "Schema formatters for TYPE_B should not be affected",
            names("s3"),
            getFormatterNames(formatterConfig3.getAllFormatters()));

        CmsFormatterConfiguration formatterConfig4 = config.getFormatters(cms, m_exampleResourceB);
        assertEquals(
            "Schema formatters for TYPE_B should not be affected",
            names("s3"),
            getFormatterNames(formatterConfig4.getAllFormatters()));

    }

    /**
     * Tests removal of a formatter through the configuration.<p>
     *
     * @throws CmsException
     */
    public void testRemoveFormatter() throws CmsException {

        I_CmsFormatterBean f1 = createFormatter(TYPE_A, "f1", 1000, true);
        I_CmsFormatterBean f2 = createFormatter(TYPE_A, "f2", 1000, true);
        I_CmsFormatterBean f3 = createFormatter(TYPE_B, "f3", 1000, true);
        CmsTestConfigData config = createConfig("/", f1, f2, f3);
        CmsTestConfigData config2 = createConfig("/invalid-name", f1, f2, f3);
        config2.setParent(config);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Arrays.asList("" + CmsUUID.getConstantUUID("f2"), "" + CmsUUID.getConstantUUID("f3")),
            Collections.<String> emptyList(),
            null,
            false,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet);

        CmsFormatterConfiguration formatterConfig = config2.getFormatters(getCmsObject(), m_exampleResourceA);

        Set<String> actualNames = new HashSet<String>();
        Set<String> expectedNames = new HashSet<String>(Arrays.asList("f1"));
        for (I_CmsFormatterBean formatter : formatterConfig.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        assertEquals("Formatter names don't match the active formatters for this type", expectedNames, actualNames);
    }

    /**
     * Tests that formatters added in lower sitemap configurations override those with overlapping key sets, and take over their keys.
     *
     * @throws CmsException if something goes wrong
     */
    public void testReplaceFormatterWithOverlappingKeys1() throws CmsException {

        I_CmsFormatterBean f1 = createFormatterWithKey(TYPE_A, "f1", 1000, true, "alpha", "beta");
        I_CmsFormatterBean f2 = createFormatterWithKey(TYPE_A, "f2", 1000, false, "beta", "gamma");
        I_CmsFormatterBean f3 = createFormatterWithKey(TYPE_A, "f3", 1000, true, null);

        CmsTestConfigData config = createConfig("/", f1, f2, f3);
        CmsTestConfigData config2 = createConfig("/invalid-name", f1, f2, f3);
        config2.setParent(config);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Collections.<String> emptyList(),
            Arrays.asList("" + CmsUUID.getConstantUUID("f2")),
            null,
            false,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet);
        assertEquals("only two formatters should be active", 2, config2.getActiveFormatters().size());
        assertEquals("f2", config2.findFormatter("alpha").getNiceName(Locale.ENGLISH));
        assertEquals("f2", config2.findFormatter("beta").getNiceName(Locale.ENGLISH));
        assertEquals("f2", config2.findFormatter("gamma").getNiceName(Locale.ENGLISH));
        assertEquals(
            new HashSet<>(Arrays.asList("alpha", "beta", "gamma")),
            config2.findFormatter("alpha").getAllKeys());

    }

    /**
     * Tests that formatters added in lower sitemap configurations override those with overlapping key sets, and take over their keys.
     *
     * @throws CmsException if something goes wrong
     */
    public void testReplaceFormatterWithOverlappingKeys2() throws CmsException {

        I_CmsFormatterBean f1 = createFormatterWithKey(TYPE_A, "f1", 1000, true, "alpha", "beta");
        I_CmsFormatterBean f2 = createFormatterWithKey(TYPE_A, "f2", 1000, false, "gamma", "beta");
        I_CmsFormatterBean f3 = createFormatterWithKey(TYPE_A, "f3", 1000, true, null);

        CmsTestConfigData config = createConfig("/", f1, f2, f3);
        CmsTestConfigData config2 = createConfig("/invalid-name", f1, f2, f3);
        config2.setParent(config);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Collections.<String> emptyList(),
            Arrays.asList("" + CmsUUID.getConstantUUID("f2")),
            null,
            false,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet);
        assertEquals("only two formatters should be active", 2, config2.getActiveFormatters().size());
        assertEquals("f2", config2.findFormatter("alpha").getNiceName(Locale.ENGLISH));
        assertEquals("f2", config2.findFormatter("beta").getNiceName(Locale.ENGLISH));
        assertEquals("f2", config2.findFormatter("gamma").getNiceName(Locale.ENGLISH));
        assertEquals(
            new HashSet<>(Arrays.asList("alpha", "beta", "gamma")),
            config2.findFormatter("alpha").getAllKeys());

    }

    /**
     * Tests that formatters added in lower sitemap configurations override those with overlapping key sets, and take over their keys.
     *
     * @throws CmsException if something goes wrong
     */
    public void testReplaceFormatterWithOverlappingKeys3() throws CmsException {

        I_CmsFormatterBean f1 = createFormatterWithKey(TYPE_A, "f1", 1000, true, "alpha", "beta");
        I_CmsFormatterBean f2 = createFormatterWithKey(TYPE_A, "f2", 1000, false, "alpha", "gamma");
        I_CmsFormatterBean f3 = createFormatterWithKey(TYPE_A, "f3", 1000, true, null);

        CmsTestConfigData config = createConfig("/", f1, f2, f3);
        CmsTestConfigData config2 = createConfig("/invalid-name", f1, f2, f3);
        config2.setParent(config);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Collections.<String> emptyList(),
            Arrays.asList("" + CmsUUID.getConstantUUID("f2")),
            null,
            false,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet);
        assertEquals("only two formatters should be active", 2, config2.getActiveFormatters().size());
        assertEquals("f2", config2.findFormatter("alpha").getNiceName(Locale.ENGLISH));
        assertEquals("f2", config2.findFormatter("beta").getNiceName(Locale.ENGLISH));
        assertEquals("f2", config2.findFormatter("gamma").getNiceName(Locale.ENGLISH));
        assertEquals(
            new HashSet<>(Arrays.asList("alpha", "beta", "gamma")),
            config2.findFormatter("alpha").getAllKeys());

    }

    /**
     * Tests that formatters with the same key override formatters inherited from parent sitemap configurations.
     *
     * @throws CmsException if something goes wrong
     */
    public void testReplaceFormatterWithSameKey() throws CmsException {

        I_CmsFormatterBean f1 = createFormatterWithKey(TYPE_A, "f1", 1000, true, "alpha");
        I_CmsFormatterBean f2 = createFormatterWithKey(TYPE_A, "f2", 1000, true, "beta");
        I_CmsFormatterBean f3 = createFormatterWithKey(TYPE_A, "f3", 1000, true, (String)null);
        I_CmsFormatterBean f4 = createFormatterWithKey(TYPE_A, "f4", 1000, false, "beta");
        I_CmsFormatterBean f5 = createFormatterWithKey(TYPE_A, "f5", 1000, false, (String)null);

        CmsTestConfigData config = createConfig("/", f1, f2, f3, f4, f5);
        CmsTestConfigData config2 = createConfig("/invalid-name", f1, f2, f3, f4, f5);
        config2.setParent(config);
        CmsFormatterChangeSet changeSet = new CmsFormatterChangeSet(
            Collections.<String> emptyList(),
            Arrays.asList("" + CmsUUID.getConstantUUID("f4"), "" + CmsUUID.getConstantUUID("f5")),
            null,
            false,
            false,
            null,
            null);
        config2.setFormatterChangeSet(changeSet);

        CmsFormatterConfiguration formatterConfig = config2.getFormatters(getCmsObject(), m_exampleResourceA);

        Set<String> actualNames = new HashSet<String>();
        Set<String> expectedNames = new HashSet<String>(Arrays.asList("f1", "f3", "f4", "f5")); // not f2, because it should have overwritten
        for (I_CmsFormatterBean formatter : formatterConfig.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        assertEquals("Formatter names don't match the active formatters for this type", expectedNames, actualNames);

    }

    /**
     * Tests whether schema formatters are available.
     *
     * @throws CmsException
     */
    public void testSchemaFormatters() throws CmsException {

        int typeA = OpenCms.getResourceManager().getResourceType(TYPE_A).getTypeId();
        int typeB = OpenCms.getResourceManager().getResourceType(TYPE_B).getTypeId();
        CmsObject cms = getCmsObject();
        I_CmsFormatterBean f1 = createFormatter(TYPE_A, "f1", 1000, true);
        I_CmsFormatterBean s1 = createFormatter(TYPE_A, "s1", 1000, false);
        I_CmsFormatterBean s2 = createFormatter(TYPE_A, "s2", 1000, false);
        I_CmsFormatterBean s3 = createFormatter(TYPE_B, "s3", 1000, false);

        CmsTestConfigData config = createConfig("/", f1);
        config.registerSchemaFormatters(typeA, CmsFormatterConfiguration.create(cms, Arrays.asList(s1, s2)));
        config.registerSchemaFormatters(typeB, CmsFormatterConfiguration.create(cms, Arrays.asList(s3)));
        CmsFormatterConfiguration formatterConfig = config.getFormatters(cms, m_exampleResourceA);

        Set<String> actualNames = new HashSet<String>();
        Set<String> expectedNames = new HashSet<String>(Arrays.asList("f1", "s1", "s2"));
        for (I_CmsFormatterBean formatter : formatterConfig.getAllFormatters()) {
            actualNames.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        assertEquals("Formatter names don't match the active formatters for this type", expectedNames, actualNames);
    }

    /**
     * Creates a sitemap configuration bean with formatters.<p>
     *
     * @param path the sitemap configuration path
     * @param formatters the formatters
     * @return the sitemap configuration bean
     */
    private CmsTestConfigData createConfig(String path, I_CmsFormatterBean... formatters) {

        CmsTestConfigData config1 = new CmsTestConfigData(
            path,
            TestConfig.NO_TYPES,
            TestConfig.NO_PROPERTIES,
            TestConfig.NO_DETAILPAGES,
            TestConfig.NO_MODEL_PAGES);
        Map<CmsUUID, I_CmsFormatterBean> formatterMap = Maps.newHashMap();
        for (I_CmsFormatterBean formatter : formatters) {
            formatterMap.put(CmsUUID.getConstantUUID(formatter.getNiceName(java.util.Locale.ENGLISH)), formatter);
        }
        config1.setFormatters(new CmsFormatterConfigurationCacheState(formatterMap));
        return config1;
    }

    /**
     * Helper method to create a file with textual content.
     *
     * @param cms the CmsObject to use
     * @param path the path
     * @param typeName the type name
     * @param content the content string (will be saved as UTF-8)
     *
     * @return the new resource
     *
     * @throws CmsException if something goes wrong
     */
    private CmsResource createFile(CmsObject cms, String path, String typeName, String content) throws CmsException {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
        return cms.createResource(path, type, content.getBytes(StandardCharsets.UTF_8), new ArrayList<>());
    }

    /**
     * Helper method to create a folder.
     *
     * @param cms the CMS context
     * @param path the path
     * @throws CmsException if something goes wrong s
     */
    private void createFolder(CmsObject cms, String path) throws CmsException {

        cms.createResource(path, 0, null, new ArrayList<>());
    }

    /**
     * Creates a formatter bean with the given resource type, name, rank, and auto-enabled status.
     *
     * @param resType
     * @param name
     * @param rank1
     * @param enabled
     * @return the formatter bean
     */
    private CmsFormatterBean createFormatter(String resType, String name, int rank1, boolean enabled) {

        Set<String> containerTypes = new HashSet<String>();
        containerTypes.add("foo");
        String jspRootPath = "/system/f1.jsp";
        CmsUUID jspStructureId = null;
        int minWidth = -1;
        int maxWidth = 9999;
        boolean preview = true;
        boolean searchContent = true;
        String location = "/system/";

        List<String> cssHeadIncludes = Lists.newArrayList();
        String inlineCss = "";
        List<String> javascriptHeadIncludes = Lists.newArrayList();
        String inlineJavascript = "";
        String niceName = name;
        String resourceTypeName = resType;
        int rank = rank1;
        String id = "" + CmsUUID.getConstantUUID(name);

        boolean isFromConfigFile = true;
        boolean isAutoEnabled = enabled;
        boolean isDetail = true;
        CmsFormatterBean result = new CmsFormatterBean(
            containerTypes,
            jspRootPath,
            jspStructureId,
            null,
            new HashSet<>(),
            minWidth,
            maxWidth,
            preview,
            searchContent,
            location,

            cssHeadIncludes,
            inlineCss,
            javascriptHeadIncludes,
            inlineJavascript,
            Collections.emptyList(),
            niceName,
            null,
            Collections.singleton(resourceTypeName),
            rank,
            id,
            new CmsSettingConfiguration(),
            isFromConfigFile,
            isAutoEnabled,
            isDetail,
            null,
            false,
            false,
            false,
            null,
            Collections.emptyMap(),
            false);

        return result;

    }

    /**
     * Creates the XML content for a formatter.
     * @param type
     * @param name
     * @param enabled
     * @param rank
     * @return the formatter XML
     * @throws Exception
     */
    private String createFormatterConfigXml(String type, String name, boolean enabled, int rank) throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource formatter = cms.readResource("/system/f1.jsp");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<NewFormatters xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/formatters/new_formatter.xsd\">\n"
            + "  <NewFormatter language=\"en\">\n"
            + "    <NiceName>"
            + name
            + "</NiceName>\n"
            + "    <Type><![CDATA["
            + type
            + "]]></Type>\n"
            + "    <Jsp>\n"
            + link(formatter)
            + "    </Jsp>\n"
            + "    <Rank>"
            + rank
            + "</Rank>\n"
            + "    <Match>\n"
            + "      <Types>\n"
            + "        <ContainerType><![CDATA[foo]]></ContainerType>\n"
            + "      </Types>\n"
            + "    </Match>\n"
            + "    <Preview>true</Preview>\n"
            + "    <SearchContent>true</SearchContent>\n"
            + "    <AutoEnabled>"
            + enabled
            + "</AutoEnabled>\n"
            + "    <Detail>true</Detail>\n"
            + "    <Display>false</Display>"
            + "    <NestedContainers>false</NestedContainers>\n"
            + "  </NewFormatter>\n"
            + "</NewFormatters>\n"
            + "";
        return xml;
    }

    /**
     * Creates a formatter bean with the given resource type, name, rank, and auto-enabled status.
     *
     * @param resType
     * @param name
     * @param rank1
     * @param enabled
     * @return the formatter bean
     */
    private CmsFormatterBean createFormatterWithKey(
        String resType,
        String name,
        int rank1,
        boolean enabled,
        String key,
        String... keyAliases) {

        Set<String> containerTypes = new HashSet<String>();
        containerTypes.add("foo");
        String jspRootPath = "/system/f1.jsp";
        CmsUUID jspStructureId = null;
        int minWidth = -1;
        int maxWidth = 9999;
        boolean preview = true;
        boolean searchContent = true;
        String location = "/system/";

        List<String> cssHeadIncludes = Lists.newArrayList();
        String inlineCss = "";
        List<String> javascriptHeadIncludes = Lists.newArrayList();
        String inlineJavascript = "";
        String niceName = name;
        String resourceTypeName = resType;
        int rank = rank1;
        String id = "" + CmsUUID.getConstantUUID(name);
        boolean isFromConfigFile = true;
        boolean isAutoEnabled = enabled;
        boolean isDetail = true;
        CmsFormatterBean result = new CmsFormatterBean(
            containerTypes,
            jspRootPath,
            jspStructureId,
            key,
            new HashSet<>(Arrays.asList(keyAliases)),
            minWidth,
            maxWidth,
            preview,
            searchContent,
            location,

            cssHeadIncludes,
            inlineCss,
            javascriptHeadIncludes,
            inlineJavascript,
            Collections.emptyList(),
            niceName,
            null,
            Collections.singleton(resourceTypeName),
            rank,
            id,
            new CmsSettingConfiguration(),
            isFromConfigFile,
            isAutoEnabled,
            isDetail,
            null,
            false,
            false,
            false,
            null,
            Collections.emptyMap(),
            false);

        return result;

    }

    /**
     * Creates a formatter bean for matching by type.<p>
     *
     * @param name
     * @param rank1
     * @param containerTypesArray
     *
     * @return the created formatter bean
     */
    private CmsFormatterBean createTypeBasedFormatter(String name, int rank1, String... containerTypesArray) {

        Set<String> containerTypes = new HashSet<String>();
        for (String cntType : containerTypesArray) {
            containerTypes.add(cntType);
        }
        String jspRootPath = "/system/f1.jsp";
        CmsUUID jspStructureId = null;
        int minWidth = -1;
        int maxWidth = 9999;
        boolean preview = true;
        boolean searchContent = true;
        String location = "-- empty -- ";

        List<String> cssHeadIncludes = Lists.newArrayList();
        String inlineCss = "";
        List<String> javascriptHeadIncludes = Lists.newArrayList();
        String inlineJavascript = "";
        String niceName = name;
        String resourceTypeName = TYPE_A;
        int rank = rank1;
        String id = "" + CmsUUID.getConstantUUID(name);
        CmsSettingConfiguration settings = new CmsSettingConfiguration();
        boolean isFromConfigFile = true;
        boolean isAutoEnabled = true;
        boolean isDetail = true;
        CmsFormatterBean result = new CmsFormatterBean(
            containerTypes,
            jspRootPath,
            jspStructureId,
            null,
            new HashSet<>(),
            minWidth,
            maxWidth,
            preview,
            searchContent,
            location,
            cssHeadIncludes,
            inlineCss,
            javascriptHeadIncludes,
            inlineJavascript,
            Collections.emptyList(),
            niceName,
            null,
            Collections.singleton(resourceTypeName),
            rank,
            id,
            settings,
            isFromConfigFile,
            isAutoEnabled,
            isDetail,
            null,
            false,
            false,
            false,
            null,
            Collections.emptyMap(),
            false);

        return result;
    }

    /**
     * Creates a formatter bean for matching by width.<p>
     *
     * @param name
     * @param rank
     * @param width
     * @param maxWidth
     *
     * @return the created formatter bean
     */
    private CmsFormatterBean createWidthBasedFormatter(String name, int rank, int width, int maxWidth) {

        Set<String> containerTypes = new HashSet<String>();
        String jspRootPath = "/system/f1.jsp";
        CmsUUID jspStructureId = null;
        int minWidth = width;
        boolean preview = true;
        boolean searchContent = true;
        String location = "-- empty -- ";

        List<String> cssHeadIncludes = Lists.newArrayList();
        String inlineCss = "";
        List<String> javascriptHeadIncludes = Lists.newArrayList();
        String inlineJavascript = "";
        String niceName = name;
        String resourceTypeName = TYPE_A;
        String id = "" + CmsUUID.getConstantUUID(name);
        Map<String, CmsXmlContentProperty> settings = Maps.newHashMap();
        boolean isFromConfigFile = true;
        boolean isAutoEnabled = true;
        boolean isDetail = true;
        CmsFormatterBean result = new CmsFormatterBean(
            containerTypes,
            jspRootPath,
            jspStructureId,
            null,
            new HashSet<>(),
            minWidth,
            maxWidth,
            preview,
            searchContent,
            location,
            cssHeadIncludes,
            inlineCss,
            javascriptHeadIncludes,
            inlineJavascript,
            Collections.emptyList(),
            niceName,
            null,
            Collections.singleton(resourceTypeName),
            rank,
            id,
            new CmsSettingConfiguration(),
            isFromConfigFile,
            isAutoEnabled,
            isDetail,
            null,
            false,
            false,
            false,
            null,
            Collections.emptyMap(),
            false);
        return result;
    }

    /**
     * Gets the set of nice names of all formatters in a list.<p>
     *
     * @param formatters the formatter beans
     * @return the set of formatter names
     */
    private Set<String> getFormatterNames(Collection<I_CmsFormatterBean> formatters) {

        Set<String> result = new HashSet<String>();
        for (I_CmsFormatterBean formatter : formatters) {
            result.add(formatter.getNiceName(java.util.Locale.ENGLISH));
        }
        return result;
    }

    /**
     * Creates the XML for a weak link to a resource.<p>
     *
     * @param res the resource
     * @return the XML for the link
     */
    private String link(CmsResource res) {

        return "<link type=\"WEAK\">\n"
            + "<target><![CDATA["
            + res.getRootPath()
            + "]]></target>\n"
            + "<uuid>"
            + res.getStructureId()
            + "</uuid>\n"
            + "</link>\n";
    }

    /**
     * Creates a set of strings.<p>
     *
     * @param names the names
     *
     * @return the set of strings
     */
    private Set<String> names(String... names) {

        Set<String> result = new HashSet<String>();
        for (String name : names) {
            result.add(name);
        }
        return result;
    }

}
