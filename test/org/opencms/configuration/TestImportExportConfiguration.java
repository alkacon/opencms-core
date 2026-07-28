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

package org.opencms.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.staticexport.CmsSharedCacheConfiguration;
import org.opencms.staticexport.CmsSharedCachePolicy;
import org.opencms.staticexport.CmsStoredContentDeliveryConfiguration;

import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.digester3.Digester;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

/**
 * Tests the import/export configuration.<p>
 */
public class TestImportExportConfiguration {

    /**
     * Tests that missing shared cache settings are treated as an internal default only.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testMissingSharedCacheConfigurationIsNotSerialized() throws Exception {

        CmsImportExportConfiguration configuration = parseImportExportConfiguration(createImportExportXml(null));

        CmsSharedCacheConfiguration sharedCache = configuration.getSharedCacheConfiguration();

        assertFalse(sharedCache.isConfigured());
        assertFalse(sharedCache.isEnabled());
        assertTrue(sharedCache.getCachePolicies().isEmpty());
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("opencms");
        configuration.generateXml(root);

        assertEquals(null, root.element(CmsImportExportConfiguration.N_SHAREDCACHE));
    }

    /**
     * Tests that missing stored content delivery settings are treated as an internal default only.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testMissingStoredContentDeliveryConfigurationIsNotSerialized() throws Exception {

        CmsImportExportConfiguration configuration = parseImportExportConfiguration(createImportExportXml(null));

        CmsStoredContentDeliveryConfiguration storedContentDelivery = configuration.getStoredContentDeliveryConfiguration();

        assertFalse(storedContentDelivery.isConfigured());
        assertFalse(storedContentDelivery.isEnabled());
        assertFalse(storedContentDelivery.hasEnabledSuffixes());
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("opencms");
        configuration.generateXml(root);

        assertEquals(null, root.element(CmsImportExportConfiguration.N_STOREDCONTENTDELIVERY));
    }

    /**
     * Tests that stored content delivery and shared cache delivery are mutually exclusive.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSharedCacheAndStoredContentDeliveryConflict() throws Exception {

        CmsImportExportConfiguration configuration = parseImportExportConfiguration(
            createImportExportXml("<storedcontentdelivery enabled=\"true\" />", createSharedCacheXml()));

        assertThrows(CmsConfigurationException.class, configuration::validate);
    }

    /**
     * Tests that shared cache delivery rejects a conflicting static export Cache-Control header.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSharedCacheCacheControlHeaderConflict() throws Exception {

        String xml = createImportExportXml(null, createSharedCacheXml()).replace(
            "<rendersettings>",
            "<exportheaders><header>Cache-Control: public, max-age=60</header></exportheaders><rendersettings>");
        CmsImportExportConfiguration configuration = parseImportExportConfiguration(xml);

        assertThrows(CmsConfigurationException.class, configuration::validate);
    }

    /**
     * Tests parsing, validation and writing of shared cache settings.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSharedCacheConfigurationRoundtrip() throws Exception {

        CmsImportExportConfiguration configuration = parseImportExportConfiguration(
            createImportExportXml(
                null,
                "<sharedcache enabled=\"true\">"
                    + "<cachepolicy>"
                    + "<clientmaxage>0</clientmaxage>"
                    + "<sharedmaxage>86400</sharedmaxage>"
                    + "<staleiferror>604800</staleiferror>"
                    + "</cachepolicy>"
                    + "<cachepolicy contenttype=\"IMAGE/*\">"
                    + "<clientmaxage>60</clientmaxage>"
                    + "<sharedmaxage>3600</sharedmaxage>"
                    + "</cachepolicy>"
                    + "</sharedcache>"));

        configuration.validate();
        CmsSharedCacheConfiguration sharedCache = configuration.getSharedCacheConfiguration();

        assertTrue(sharedCache.isEnabled());
        assertEquals(2, sharedCache.getCachePolicies().size());
        CmsSharedCachePolicy defaultPolicy = sharedCache.getDefaultCachePolicy();
        assertNotNull(defaultPolicy);
        assertEquals(0, defaultPolicy.getClientMaxAge());
        assertEquals(86400, defaultPolicy.getSharedMaxAge());
        assertEquals(604800, defaultPolicy.getStaleIfError());
        CmsSharedCachePolicy imagePolicy = sharedCache.getCachePolicies().get(1);
        assertEquals("image/*", imagePolicy.getContentType());
        assertEquals(60, imagePolicy.getClientMaxAge());
        assertEquals(3600, imagePolicy.getSharedMaxAge());
        assertEquals(CmsSharedCachePolicy.DURATION_UNSET, imagePolicy.getStaleIfError());
        assertEquals(imagePolicy, sharedCache.getCachePolicy("image/jpeg"));
        assertEquals(imagePolicy, sharedCache.getCachePolicy("IMAGE/PNG; charset=binary"));
        assertEquals(defaultPolicy, sharedCache.getCachePolicy("text/css"));
        assertEquals(defaultPolicy, sharedCache.getCachePolicy(null));

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("opencms");
        configuration.generateXml(root);

        Element sharedCacheElement = root.element(CmsImportExportConfiguration.N_SHAREDCACHE);
        assertNotNull(sharedCacheElement);
        assertEquals("true", sharedCacheElement.attributeValue(I_CmsXmlConfiguration.A_ENABLED));
        assertEquals(2, sharedCacheElement.elements(CmsImportExportConfiguration.N_SHAREDCACHE_CACHEPOLICY).size());
        Element serializedImagePolicy = (Element)sharedCacheElement.elements(
            CmsImportExportConfiguration.N_SHAREDCACHE_CACHEPOLICY).get(1);
        assertEquals("image/*", serializedImagePolicy.attributeValue("contenttype"));
        assertEquals(
            "3600",
            serializedImagePolicy.elementText(CmsImportExportConfiguration.N_SHAREDCACHE_SHAREDMAXAGE));
        assertEquals(null, serializedImagePolicy.element(CmsImportExportConfiguration.N_SHAREDCACHE_STALEIFERROR));
    }

    /**
     * Tests validation of shared cache policies.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSharedCachePolicyValidation() throws Exception {

        CmsImportExportConfiguration missingDefault = parseImportExportConfiguration(
            createImportExportXml(
                null,
                "<sharedcache enabled=\"true\">"
                    + "<cachepolicy contenttype=\"image/*\">"
                    + "<clientmaxage>0</clientmaxage>"
                    + "<sharedmaxage>86400</sharedmaxage>"
                    + "</cachepolicy>"
                    + "</sharedcache>"));
        assertThrows(CmsConfigurationException.class, missingDefault::validate);

        CmsImportExportConfiguration negativeDuration = parseImportExportConfiguration(
            createImportExportXml(
                null,
                "<sharedcache enabled=\"true\">"
                    + "<cachepolicy>"
                    + "<clientmaxage>0</clientmaxage>"
                    + "<sharedmaxage>-1</sharedmaxage>"
                    + "</cachepolicy>"
                    + "</sharedcache>"));
        assertThrows(CmsConfigurationException.class, negativeDuration::validate);
    }

    /**
     * Tests that shared cache delivery requires static export and an on-demand export handler.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSharedCacheStaticExportValidation() throws Exception {

        String disabledStaticExportXml = createImportExportXml(null, createSharedCacheXml()).replace(
            "<staticexport enabled=\"true\">",
            "<staticexport enabled=\"false\">");
        CmsImportExportConfiguration disabledStaticExport = parseImportExportConfiguration(disabledStaticExportXml);
        assertThrows(CmsConfigurationException.class, disabledStaticExport::validate);

        String afterPublishXml = createImportExportXml(null, createSharedCacheXml()).replace(
            "org.opencms.staticexport.CmsOnDemandStaticExportHandler",
            "org.opencms.staticexport.CmsAfterPublishStaticExportHandler");
        CmsImportExportConfiguration afterPublish = parseImportExportConfiguration(afterPublishXml);
        assertThrows(CmsConfigurationException.class, afterPublish::validate);
    }

    /**
     * Tests parsing and writing stored content delivery settings.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoredContentDeliveryConfigurationRoundtrip() throws Exception {

        CmsImportExportConfiguration configuration = parseImportExportConfiguration(
            createImportExportXmlWithStoredContentDelivery(
                "true",
                "<enabledsuffixes>"
                    + "<suffix key=\".JPG\" />"
                    + "<suffix key=\"pdf\" />"
                    + "<suffix key=\".mp4\" />"
                    + "</enabledsuffixes>"));

        CmsStoredContentDeliveryConfiguration storedContentDelivery = configuration.getStoredContentDeliveryConfiguration();

        assertTrue(storedContentDelivery.isEnabled());
        assertTrue(storedContentDelivery.hasEnabledSuffixes());
        assertEquals(set(".jpg", ".pdf", ".mp4"), storedContentDelivery.getEnabledSuffixes());
        assertTrue(storedContentDelivery.isSuffixEnabled("/sites/default/test.JPG"));
        assertTrue(storedContentDelivery.isSuffixEnabled("/sites/default/test.pdf"));
        assertFalse(storedContentDelivery.isSuffixEnabled("/sites/default/test.svg"));
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("opencms");
        configuration.generateXml(root);

        Element storedContentDeliveryElement = root.element(CmsImportExportConfiguration.N_STOREDCONTENTDELIVERY);
        assertNotNull(storedContentDeliveryElement);
        assertEquals("true", storedContentDeliveryElement.attributeValue(I_CmsXmlConfiguration.A_ENABLED));

        Element enabledSuffixesElement = storedContentDeliveryElement.element(
            CmsImportExportConfiguration.N_STOREDCONTENTDELIVERY_ENABLEDSUFFIXES);
        assertNotNull(enabledSuffixesElement);
        assertEquals(3, enabledSuffixesElement.elements(CmsImportExportConfiguration.N_STATICEXPORT_SUFFIX).size());
        assertEquals(set(".jpg", ".pdf", ".mp4"), collectSuffixes(enabledSuffixesElement));

    }

    /**
     * Collects suffixes from an XML element.<p>
     *
     * @param enabledSuffixesElement the enabled suffixes element
     * @return the suffixes
     */
    private Set<String> collectSuffixes(Element enabledSuffixesElement) {

        Set<String> result = new LinkedHashSet<String>();
        for (Object suffixObject : enabledSuffixesElement.elements(
            CmsImportExportConfiguration.N_STATICEXPORT_SUFFIX)) {
            Element suffixElement = (Element)suffixObject;
            result.add(suffixElement.attributeValue(I_CmsXmlConfiguration.A_KEY));
        }
        return result;
    }

    /**
     * Creates a minimal import/export configuration XML.<p>
     *
     * @param storedContentDeliveryXml the stored content delivery XML, or <code>null</code>
     * @return the XML
     */
    private String createImportExportXml(String storedContentDeliveryXml) {

        return createImportExportXml(storedContentDeliveryXml, null);
    }

    /**
     * Creates a minimal import/export configuration XML.<p>
     *
     * @param storedContentDeliveryXml the stored content delivery XML, or <code>null</code>
     * @param sharedCacheXml the shared cache XML, or <code>null</code>
     * @return the XML
     */
    private String createImportExportXml(String storedContentDeliveryXml, String sharedCacheXml) {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<opencms>"
            + "<importexport>"
            + "<importexporthandlers />"
            + "<import>"
            + "<overwrite>true</overwrite>"
            + "<convert>true</convert>"
            + "<importversions />"
            + "<immutables />"
            + "<principaltranslations />"
            + "<ignoredproperties />"
            + "</import>"
            + "<export />"
            + "</importexport>"
            + "<staticexport enabled=\"true\">"
            + "<staticexporthandler>org.opencms.staticexport.CmsOnDemandStaticExportHandler</staticexporthandler>"
            + "<linksubstitutionhandler>org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler</linksubstitutionhandler>"
            + "<exportpath>export</exportpath>"
            + "<exportworkpath>temp</exportworkpath>"
            + "<exportbackups>2</exportbackups>"
            + "<defaultpropertyvalue>false</defaultpropertyvalue>"
            + "<defaultsuffixes><suffix key=\".jpg\" /></defaultsuffixes>"
            + "<rendersettings>"
            + "<rfs-prefix>${CONTEXT_NAME}/export</rfs-prefix>"
            + "<vfs-prefix>${CONTEXT_NAME}</vfs-prefix>"
            + "<userelativelinks>false</userelativelinks>"
            + "<exporturl>http://127.0.0.1:8080${CONTEXT_NAME}/handle404</exporturl>"
            + "<plainoptimization>true</plainoptimization>"
            + "<testresource uri=\"/system/config/page.dtd\" />"
            + "<resourcestorender><regex>/sites/.*</regex></resourcestorender>"
            + "</rendersettings>"
            + "</staticexport>"
            + (storedContentDeliveryXml == null ? "" : storedContentDeliveryXml)
            + (sharedCacheXml == null ? "" : sharedCacheXml)
            + "<repositories />"
            + "</opencms>";
    }

    /**
     * Creates a minimal import/export configuration XML.<p>
     *
     * @param enabled the stored content delivery enabled flag
     * @param enabledSuffixesXml the enabled suffixes XML
     * @return the XML
     */
    private String createImportExportXmlWithStoredContentDelivery(String enabled, String enabledSuffixesXml) {

        return createImportExportXml(
            "<storedcontentdelivery enabled=\"" + enabled + "\">" + enabledSuffixesXml + "</storedcontentdelivery>");
    }

    /**
     * Creates a valid shared cache configuration XML.<p>
     *
     * @return the shared cache XML
     */
    private String createSharedCacheXml() {

        return "<sharedcache enabled=\"true\">"
            + "<cachepolicy>"
            + "<clientmaxage>0</clientmaxage>"
            + "<sharedmaxage>86400</sharedmaxage>"
            + "<staleiferror>604800</staleiferror>"
            + "</cachepolicy>"
            + "</sharedcache>";
    }

    /**
     * Parses an import/export configuration.<p>
     *
     * @param xml the XML
     * @return the parsed configuration
     *
     * @throws Exception if parsing fails
     */
    private CmsImportExportConfiguration parseImportExportConfiguration(String xml) throws Exception {

        CmsImportExportConfiguration configuration = new CmsImportExportConfiguration();
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        digester.setValidating(false);
        digester.push(configuration);
        configuration.addXmlDigesterRules(digester);
        digester.parse(new InputSource(new StringReader(xml)));
        return configuration;
    }

    /**
     * Creates a set from the given values.<p>
     *
     * @param values the values
     * @return the set
     */
    private Set<String> set(String... values) {

        return new LinkedHashSet<String>(Arrays.asList(values));
    }
}
