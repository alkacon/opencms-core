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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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

package org.opencms.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.time.Duration;

import org.apache.commons.digester3.Digester;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

/**
 * Tests the image cache configuration.<p>
 */
public class TestCmsImageCacheConfiguration {

    /** Tests a policy whose retention is managed outside OpenCms. */
    @Test
    public void testExternalConfiguration() throws Exception {

        CmsSystemConfiguration systemConfiguration = parseSystemConfiguration(
            "<opencms><system><imagecache><retention mode=\"external\" /></imagecache></system></opencms>");
        CmsImageCacheConfiguration configuration = systemConfiguration.getImageCacheConfiguration();

        assertEquals(CmsImageCacheConfiguration.RetentionMode.external, configuration.getRetentionMode());
        assertEquals(null, configuration.getMaxAge());
        assertEquals(null, configuration.getRenewalWindow());
        assertEquals(null, configuration.getRenewalJitter());

        Document document = DocumentHelper.createDocument();
        Element retention = configuration.appendToXml(document.addElement("system")).element("retention");
        assertEquals("external", retention.attributeValue("mode"));
        assertEquals(null, retention.attributeValue("max-age"));
        assertEquals(null, retention.attributeValue("renewal-window"));
        assertEquals(null, retention.attributeValue("renewal-jitter"));
    }

    /** Tests that the fingerprint covers configuration changes. */
    @Test
    public void testFingerprint() {

        CmsImageCacheConfiguration first = createRenewOnUseConfiguration();
        CmsImageCacheConfiguration second = createRenewOnUseConfiguration();

        assertEquals(first.getPolicyFingerprint(), second.getPolicyFingerprint());
        second.setRetention("renew-on-use", "P60D", "P30D", "P14D");
        assertEquals(first.getPolicyFingerprint(), second.getPolicyFingerprint());
        second.setS3("500", "1", "2", "20", "10000");
        assertNotEquals(first.getPolicyFingerprint(), second.getPolicyFingerprint());
    }

    /** Tests the internal legacy configuration used when the XML element is absent. */
    @Test
    public void testLegacyConfiguration() {

        CmsImageCacheConfiguration configuration = CmsImageCacheConfiguration.createLegacyConfiguration();

        assertFalse(configuration.isConfigured());
        assertEquals(null, configuration.getRetentionMode());
        configuration.validate();

        Document document = DocumentHelper.createDocument();
        assertEquals(null, configuration.appendToXml(document.addElement("system")));
    }

    /** Tests a valid renew-on-use configuration. */
    @Test
    public void testRenewOnUseConfiguration() {

        CmsImageCacheConfiguration configuration = createRenewOnUseConfiguration();

        configuration.validate();
        assertTrue(configuration.isConfigured());
        assertEquals(CmsImageCacheConfiguration.RetentionMode.renewOnUse, configuration.getRetentionMode());
        assertEquals(Duration.ofDays(60), configuration.getMaxAge());
        assertEquals(Duration.ofDays(30), configuration.getRenewalWindow());
        assertEquals(Duration.ofDays(14), configuration.getRenewalJitter());
        assertEquals(Duration.ofMinutes(5), configuration.getCleanupMaxRuntime());
        assertEquals(1000, configuration.getS3DeleteBatchSize());
    }

    /** Tests validation of retention windows and S3 batch sizes. */
    @Test
    public void testValidation() {

        CmsImageCacheConfiguration missingMode = new CmsImageCacheConfiguration();
        assertThrows(IllegalArgumentException.class, missingMode::validate);

        CmsImageCacheConfiguration missingMaxAge = new CmsImageCacheConfiguration();
        missingMaxAge.setRetention("fixed", null, null, null);
        assertThrows(IllegalArgumentException.class, missingMaxAge::validate);

        CmsImageCacheConfiguration missingRenewalWindow = new CmsImageCacheConfiguration();
        missingRenewalWindow.setRetention("renew-on-use", "P210D", null, "P14D");
        assertThrows(IllegalArgumentException.class, missingRenewalWindow::validate);

        CmsImageCacheConfiguration missingRenewalJitter = new CmsImageCacheConfiguration();
        missingRenewalJitter.setRetention("renew-on-use", "P210D", "P30D", null);
        assertThrows(IllegalArgumentException.class, missingRenewalJitter::validate);

        CmsImageCacheConfiguration windowTooLarge = new CmsImageCacheConfiguration();
        windowTooLarge.setRetention("renew-on-use", "P210D", "P210D", "P14D");
        assertThrows(IllegalArgumentException.class, windowTooLarge::validate);

        CmsImageCacheConfiguration jitterTooLarge = new CmsImageCacheConfiguration();
        jitterTooLarge.setRetention("renew-on-use", "P210D", "P30D", "P31D");
        assertThrows(IllegalArgumentException.class, jitterTooLarge::validate);

        CmsImageCacheConfiguration fixedWithRenewal = new CmsImageCacheConfiguration();
        fixedWithRenewal.setRetention("fixed", "P210D", "P30D", null);
        assertThrows(IllegalArgumentException.class, fixedWithRenewal::validate);

        CmsImageCacheConfiguration externalWithMaxAge = new CmsImageCacheConfiguration();
        externalWithMaxAge.setRetention("external", "P210D", null, null);
        assertThrows(IllegalArgumentException.class, externalWithMaxAge::validate);

        CmsImageCacheConfiguration externalWithRenewal = new CmsImageCacheConfiguration();
        externalWithRenewal.setRetention("external", null, "P30D", "P14D");
        assertThrows(IllegalArgumentException.class, externalWithRenewal::validate);

        CmsImageCacheConfiguration fixed = new CmsImageCacheConfiguration();
        fixed.setRetention("fixed", "P210D", null, null);
        fixed.validate();
        Document document = DocumentHelper.createDocument();
        Element retention = fixed.appendToXml(document.addElement("system")).element("retention");
        assertEquals("P210D", retention.attributeValue("max-age"));
        assertEquals(null, retention.attributeValue("renewal-window"));
        assertEquals(null, retention.attributeValue("renewal-jitter"));

        CmsImageCacheConfiguration invalidBatchSize = new CmsImageCacheConfiguration();
        assertThrows(IllegalArgumentException.class, () -> invalidBatchSize.setS3("1001", null, null, null, null));
    }

    /** Tests parsing and writing the system XML element. */
    @Test
    public void testXmlParsingAndWriting() throws Exception {

        String xml = "<opencms><system><imagecache>"
            + "<retention mode=\"renew-on-use\" max-age=\"P60D\" renewal-window=\"P30D\" "
            + "renewal-jitter=\"P14D\" />"
            + "<cleanup max-deletes-per-run=\"5000\" max-runtime=\"PT3M\" />"
            + "<rfs touch-minimum-interval=\"PT1H\" />"
            + "<fs touch-concurrency=\"2\" />"
            + "<s3 delete-batch-size=\"500\" delete-concurrency=\"2\" copy-concurrency=\"3\" "
            + "max-copies-per-second=\"25\" renewal-queue-capacity=\"20000\" />"
            + "</imagecache></system></opencms>";
        CmsSystemConfiguration systemConfiguration = parseSystemConfiguration(xml);
        CmsImageCacheConfiguration configuration = systemConfiguration.getImageCacheConfiguration();

        assertTrue(configuration.isConfigured());
        assertEquals(Duration.ofDays(60), configuration.getMaxAge());
        assertEquals(Duration.ofDays(30), configuration.getRenewalWindow());
        assertEquals(Duration.ofDays(14), configuration.getRenewalJitter());
        assertEquals(5000, configuration.getCleanupMaxDeletesPerRun());
        assertEquals(Duration.ofMinutes(3), configuration.getCleanupMaxRuntime());
        assertEquals(2, configuration.getFsTouchConcurrency());
        assertEquals(500, configuration.getS3DeleteBatchSize());
        assertEquals(3, configuration.getS3CopyConcurrency());

        Document document = DocumentHelper.createDocument();
        Element imageCacheElement = configuration.appendToXml(document.addElement("system"));
        assertEquals("imagecache", imageCacheElement.getName());
        assertEquals("P60D", imageCacheElement.element("retention").attributeValue("max-age"));
        assertEquals("P30D", imageCacheElement.element("retention").attributeValue("renewal-window"));
        assertEquals("P14D", imageCacheElement.element("retention").attributeValue("renewal-jitter"));
        assertEquals("5000", imageCacheElement.element("cleanup").attributeValue("max-deletes-per-run"));
        assertEquals("2", imageCacheElement.element("fs").attributeValue("touch-concurrency"));
        assertEquals("500", imageCacheElement.element("s3").attributeValue("delete-batch-size"));
    }

    /**
     * Creates a valid renew-on-use configuration.<p>
     *
     * @return the configuration
     */
    private CmsImageCacheConfiguration createRenewOnUseConfiguration() {

        CmsImageCacheConfiguration result = new CmsImageCacheConfiguration();
        result.setRetention("renew-on-use", "P60D", "P30D", "P14D");
        result.setCleanup("10000", "PT5M");
        result.setRfs("PT1H");
        result.setFs("1");
        result.setS3("1000", "1", "2", "20", "10000");
        return result;
    }

    /**
     * Parses a minimal system configuration.<p>
     *
     * @param xml the XML
     * @return the parsed configuration
     * @throws Exception if parsing fails
     */
    private CmsSystemConfiguration parseSystemConfiguration(String xml) throws Exception {

        CmsSystemConfiguration configuration = new CmsSystemConfiguration();
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        digester.setValidating(false);
        digester.push(configuration);
        configuration.addXmlDigesterRules(digester);
        digester.parse(new InputSource(new StringReader(xml)));
        return configuration;
    }
}
