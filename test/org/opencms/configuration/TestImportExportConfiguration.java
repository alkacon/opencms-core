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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.loader.CmsFsImageCache;
import org.opencms.loader.CmsS3ImageCache;
import org.opencms.staticexport.CmsImageCacheConfiguration;
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
        assertFalse(configuration.getImageCacheConfiguration().isConfigured());

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("opencms");
        configuration.generateXml(root);

        assertEquals(null, root.element(CmsImportExportConfiguration.N_STOREDCONTENTDELIVERY));
    }

    /**
     * Tests parsing and writing stored content delivery settings.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoredContentDeliveryConfigurationRoundtrip() throws Exception {

        CmsImportExportConfiguration configuration = parseImportExportConfiguration(
            createImportExportXml(
                "true",
                CmsS3ImageCache.class.getName(),
                "<params>"
                    + "<param name=\"bucket\">opencms-media</param>"
                    + "<param name=\"prefix\">/imagecache//</param>"
                    + "</params>",
                "<enabledsuffixes>"
                    + "<suffix key=\".JPG\" />"
                    + "<suffix key=\"pdf\" />"
                    + "<suffix key=\".mp4\" />"
                    + "</enabledsuffixes>"));

        CmsStoredContentDeliveryConfiguration storedContentDelivery = configuration.getStoredContentDeliveryConfiguration();
        CmsImageCacheConfiguration imageCache = configuration.getImageCacheConfiguration();

        assertTrue(storedContentDelivery.isEnabled());
        assertTrue(storedContentDelivery.hasEnabledSuffixes());
        assertEquals(set(".jpg", ".pdf", ".mp4"), storedContentDelivery.getEnabledSuffixes());
        assertTrue(storedContentDelivery.isSuffixEnabled("/sites/default/test.JPG"));
        assertTrue(storedContentDelivery.isSuffixEnabled("/sites/default/test.pdf"));
        assertFalse(storedContentDelivery.isSuffixEnabled("/sites/default/test.svg"));
        assertTrue(imageCache.isConfigured());
        assertEquals(CmsS3ImageCache.class.getName(), imageCache.getClassName());
        assertEquals("opencms-media", imageCache.getConfiguration().get("bucket"));
        assertEquals("/imagecache//", imageCache.getConfiguration().get("prefix"));

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

        Element imageCacheElement = root.element(CmsImportExportConfiguration.N_IMAGECACHE);
        assertNotNull(imageCacheElement);
        assertEquals(CmsS3ImageCache.class.getName(), imageCacheElement.attributeValue(I_CmsXmlConfiguration.A_CLASS));
        Element paramsElement = imageCacheElement.element(CmsImportExportConfiguration.N_PARAMS);
        assertNotNull(paramsElement);
        assertEquals("opencms-media", findParam(paramsElement, "bucket"));
        assertEquals("/imagecache//", findParam(paramsElement, "prefix"));
    }

    /**
     * Tests parsing file system image cache settings.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoredContentDeliveryFsImageCacheConfiguration() throws Exception {

        CmsImportExportConfiguration configuration = parseImportExportConfiguration(
            createImportExportXml(
                "false",
                CmsFsImageCache.class.getName(),
                "<params><param name=\"path\">/var/opencms/imagecache</param></params>",
                ""));

        CmsStoredContentDeliveryConfiguration storedContentDelivery = configuration.getStoredContentDeliveryConfiguration();
        CmsImageCacheConfiguration imageCache = configuration.getImageCacheConfiguration();

        assertFalse(storedContentDelivery.isEnabled());
        assertFalse(storedContentDelivery.hasEnabledSuffixes());
        assertTrue(storedContentDelivery.isSuffixEnabled("/sites/default/test.svg"));
        assertTrue(imageCache.isConfigured());
        assertEquals(CmsFsImageCache.class.getName(), imageCache.getClassName());
        assertEquals("/var/opencms/imagecache", imageCache.getConfiguration().get("path"));
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
            + "<repositories />"
            + "</opencms>";
    }

    /**
     * Creates a minimal import/export configuration XML.<p>
     *
     * @param enabled the stored content delivery enabled flag
     * @param imageCacheClass the image cache class
     * @param imageCacheBackendXml the image cache backend XML
     * @param enabledSuffixesXml the enabled suffixes XML
     * @return the XML
     */
    private String createImportExportXml(
        String enabled,
        String imageCacheClass,
        String imageCacheBackendXml,
        String enabledSuffixesXml) {

        return createImportExportXml(
            "<storedcontentdelivery enabled=\""
                + enabled
                + "\">"
                + enabledSuffixesXml
                + "</storedcontentdelivery>"
                + "<imagecache class=\""
                + imageCacheClass
                + "\">"
                + imageCacheBackendXml
                + "</imagecache>");
    }

    /**
     * Finds a parameter value.<p>
     *
     * @param paramsElement the params element
     * @param name the parameter name
     * @return the parameter value
     */
    private String findParam(Element paramsElement, String name) {

        for (Object paramObject : paramsElement.elements(CmsImportExportConfiguration.N_PARAM)) {
            Element paramElement = (Element)paramObject;
            if (name.equals(paramElement.attributeValue(I_CmsXmlConfiguration.A_NAME))) {
                return paramElement.getText();
            }
        }
        return null;
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
