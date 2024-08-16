/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.json.JSONObject;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.documents.CmsDocumentDependency;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsRequestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the Solr field mapping.<p>
 *
 * @since 8.5.0
 */
public class TestSolrFieldConfiguration extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSolrFieldConfiguration(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestSolrFieldConfiguration.class.getName());
        suite.addTest(new TestSolrFieldConfiguration("testAppinfoSolrField"));
        suite.addTest(new TestSolrFieldConfiguration("testAppinfoSearchTypeContent"));
        suite.addTest(new TestSolrFieldConfiguration("testContentLocalesField"));
        suite.addTest(new TestSolrFieldConfiguration("testDependencies"));
        suite.addTest(new TestSolrFieldConfiguration("testLanguageDetection"));
        suite.addTest(new TestSolrFieldConfiguration("testLocaleDependenciesField"));
        suite.addTest(new TestSolrFieldConfiguration("testLuceneMigration"));
        suite.addTest(new TestSolrFieldConfiguration("testOfflineIndexAccess"));
        suite.addTest(new TestSolrFieldConfiguration("testListSortOptionFields"));
        suite.addTest(new TestSolrFieldConfiguration("testListSearchFields"));
        // this test case must be the last one
        suite.addTest(new TestSolrFieldConfiguration("testIngnoreMaxRows"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("solrtest", "/", "/../org/opencms/search/solr");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests if the search content type "content" works correctly.<p>
     *
     * We check if the content is indexed correctly and if cyclic dependencies are correctly handled.
     *
     * '@see /sites/default/extractLinked/extract-linked.xsd'
     *
     * @throws Throwable if something goes wrong
     */
    public void testAppinfoSearchTypeContent() throws Throwable {

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrQuery squery = new CmsSolrQuery(
            null,
            CmsRequestUtil.createParameterMap("q=path:\"/sites/default/extractLinked/el1.xml\""));
        CmsSolrResultList results = index.search(getCmsObject(), squery);

        // Test the result count
        AllTests.printResults(getCmsObject(), results, false);
        assertEquals(1, results.size());

        // Test if the result contains the expected resource
        CmsSearchResource res = results.get(0);
        assertEquals("/sites/default/extractLinked/el1.xml", res.getRootPath());

        ////////////////
        // FIELD TEST //
        ////////////////
        String contentFieldValue = res.getField("content_en");
        assertNotNull(contentFieldValue);
        assertTrue(
            "Field should contain 'Hund' and 'Rind', i.e. the values of el1.xml and el2.xml",
            contentFieldValue.contains("Hund") && contentFieldValue.contains("Rind"));

    }

    /**
     * Tests the Solr field configuration that can be done in the XSD of an XML content.<p>
     *
     * '@see /sites/default/xmlcontent/article.xsd'
     *
     * @throws Throwable if something goes wrong
     */
    public void testAppinfoSolrField() throws Throwable {

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrQuery squery = new CmsSolrQuery(
            null,
            CmsRequestUtil.createParameterMap("q=path:\"/sites/default/xmlcontent/article_0001.html\""));
        CmsSolrResultList results = index.search(getCmsObject(), squery);

        /////////////////
        // RESULT TEST //
        /////////////////

        // Test the result count
        AllTests.printResults(getCmsObject(), results, false);
        assertEquals(1, results.size());

        // Test if the result contains the expected resource
        CmsSearchResource res = results.get(0);
        assertEquals("/sites/default/xmlcontent/article_0001.html", res.getRootPath());

        ////////////////
        // FIELD TEST //
        ////////////////

        // Test multiple language field
        String fieldValue = res.getField("ahtml_en");
        assertNotNull(fieldValue);
        fieldValue = res.getField("ahtml_de");
        assertNotNull(fieldValue);

        // Test the contents of the copy fields
        fieldValue = res.getField("test_text_de");
        assertTrue(fieldValue.contains("Alkacon Software German"));
        fieldValue = res.getField("test_text_en");
        assertTrue(fieldValue.contains("Alkacon Software German"));

        fieldValue = res.getField("explicit_title");
        assertTrue(fieldValue.contains("Sample article 1  (>>SearchEgg1<<)"));

        // Test locale restricted field
        fieldValue = res.getField("aauthor_de");
        assertTrue(fieldValue.equals("Alkacon Software German"));
        fieldValue = res.getField("aauthor_en");
        assertNull(fieldValue);

        // Test source field
        Date dateValue = res.getDateField("arelease_en_dt");
        assertTrue("1308210520000".equals(Long.valueOf(dateValue.getTime()).toString()));
        dateValue = res.getDateField("arelease_de_dt");
        assertTrue("1308210420000".equals(Long.valueOf(dateValue.getTime()).toString()));

        // test 'default' value for the whole field
        fieldValue = res.getField("ahomepage_de");
        assertTrue(fieldValue.equals("Homepage n.a."));
        fieldValue = res.getField("ahomepage_en");
        assertTrue(fieldValue.contains("/sites/default/index.html"));

        //////////////////
        // MAPPING TEST //
        //////////////////

        fieldValue = res.getField("Description_de");
        assertEquals(fieldValue, "My Special OpenCms Solr Description");

        // test the 'content' mapping
        fieldValue = res.getField("ateaser_en");
        assertTrue(fieldValue.contains("OpenCms Alkacon This is the article 1 text"));

        // test the 'item' mapping with default
        fieldValue = res.getField("ateaser_en");
        assertTrue(fieldValue.contains("/sites/default/index.html"));
        fieldValue = res.getField("ateaser_de");
        assertTrue(fieldValue.contains("Homepage n.a."));

        // test the property mapping
        fieldValue = res.getField("atitle_en");
        assertTrue(fieldValue.contains("Alkacon Software English"));
        fieldValue = res.getField("atitle_de");
        // properties are not localized
        assertEquals(false, fieldValue.contains("Alkacon Software German"));
        assertTrue(fieldValue.contains("Alkacon Software English"));
        // but the title item value is localized
        assertTrue(fieldValue.contains(">>GermanSearchEgg1<<"));

        // test the 'property-search' mapping
        fieldValue = res.getField("ateaser_en");
        assertTrue(fieldValue.contains("Cologne is a nice city"));

        // test the 'attribute' mapping
        fieldValue = res.getField("ateaser_en");
        // This is NOT the Lucene optimized String representation of the date.
        // For Solr just the milliseconds are stored to allow mapping to date fields.
        assertTrue(fieldValue.contains("1308210520000"));

        // test the 'dynamic' mapping with 'class' attribute
        fieldValue = res.getField("ateaser_en");
        assertTrue(fieldValue.contains("This is an amazing and very 'dynamic' content"));

        squery = new CmsSolrQuery(
            null,
            CmsRequestUtil.createParameterMap("q=path:\"/sites/default/xmlcontent/article_0002.html\""));
        results = index.search(getCmsObject(), squery);
        res = results.get(0);
        assertEquals("/sites/default/xmlcontent/article_0002.html", res.getRootPath());
        assertTrue(res.getMultivaluedField("ateaser2_en_txt").contains("This is teaser 2 in sample article 2."));

        // test multi nested elements
        List<String> teaser = res.getMultivaluedField("mteaser");
        assertTrue(
            teaser.get(0).equals("This is the sample article number 2. This is just a demo teaser. (>>SearchEgg2<<)"));
        assertTrue(teaser.get(1).equals("This is teaser 2 in sample article 2."));
        squery = new CmsSolrQuery(
            null,
            CmsRequestUtil.createParameterMap("q=path:\"/sites/default/flower/flower-0001.html\""));
        results = index.search(getCmsObject(), squery);
        assertEquals(1, results.size());
        res = results.get(0);
        assertEquals("/sites/default/flower/flower-0001.html", res.getRootPath());
        List<String> desc = res.getMultivaluedField("desc_en");
        assertEquals(3, desc.size());
        assertTrue(desc.get(0).equals("This is the first paragraph of the test flower."));
        assertTrue(desc.get(1).equals("This is the second paragraph of the test flower."));
        assertTrue(desc.get(2).equals("This is the third paragraph of the test flower."));
        desc = res.getMultivaluedField("desc_de");
        assertEquals(2, desc.size());
        assertTrue(desc.get(0).equals("Dies ist der erste Absatz der neuen Testblume ..."));
        assertTrue(desc.get(1).equals("Dies ist der sweite Absatz der neuen Testblume ..."));
        desc = res.getMultivaluedField("mteaser");
        assertTrue(desc.get(0).equals("First ocurence of a nested content"));
        assertTrue(desc.get(1).equals("Second ocurence of a nested content"));
        assertTrue(desc.get(2).equals("Third ocurence of a nested content"));

    }

    /**
     * Tests the locales stored in the index.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testContentLocalesField() throws Throwable {

        Map<String, List<String>> filenames = new HashMap<String, List<String>>();
        List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales();
        List<String> defaultLocalesString = new ArrayList<String>(defaultLocales.size());
        for (Locale l : defaultLocales) {
            defaultLocalesString.add(l.toString());
        }
        filenames.put("rabbit_en_GB.html", Collections.singletonList("en_GB"));
        filenames.put("rabbit_en_GB", Collections.singletonList("en_GB"));
        filenames.put("rabbit_en.html", Collections.singletonList("en"));
        filenames.put("rabbit_en", Collections.singletonList("en"));
        filenames.put("rabbit_en.", Collections.singletonList("en"));
        filenames.put("rabbit_enr", defaultLocalesString);
        filenames.put("rabbit_en.tar.gz", defaultLocalesString); //shouldn't this work differently?
        filenames.put("rabbit_de_en_GB.html", Collections.singletonList("en_GB"));
        filenames.put("rabbit_de_DE_EN_DE_en.html", Collections.singletonList("en"));
        filenames.put("rabbit_de_DE_EN_en_DE.html", Collections.singletonList("en_DE"));

        // create test folder
        String folderName = "/filenameTest/";
        CmsObject cms = getCmsObject();
        CmsResource folder = cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
        cms.unlockResource(folderName);
        for (String filename : filenames.keySet()) {
            // create master resource
            importTestResource(
                cms,
                "org/opencms/search/pdf-test-112.pdf",
                folderName + filename,
                CmsResourceTypeBinary.getStaticTypeId(),
                Collections.<CmsProperty> emptyList());
        }
        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrQuery query = new CmsSolrQuery();
        query.setSearchRoots(cms.getRequestContext().addSiteRoot(folderName));
        CmsSolrResultList results = index.search(cms, query);
        AllTests.printResults(cms, results, false);
        assertEquals(10, results.getNumFound());

        for (Map.Entry<String, List<String>> filename : filenames.entrySet()) {
            String absoluteFileName = cms.getRequestContext().addSiteRoot(folderName + filename.getKey());
            SolrQuery squery = new CmsSolrQuery();
            squery.addFilterQuery("path:\"" + absoluteFileName + "\"");
            results = index.search(cms, squery);
            assertEquals(1, results.size());
            CmsSearchResource res = results.get(0);
            List<String> fieldLocales = res.getMultivaluedField(CmsSearchField.FIELD_CONTENT_LOCALES);
            assertTrue(fieldLocales.size() == filename.getValue().size());
            assertTrue(fieldLocales.containsAll(filename.getValue()));
            assertTrue(filename.getValue().containsAll(fieldLocales));
        }

        CmsProperty localesAvailable = new CmsProperty("locale-available", "en", null);
        for (String filename : filenames.keySet()) {
            String resourceName = folderName + filename;
            cms.lockResource(resourceName);
            cms.writePropertyObjects(resourceName, Collections.singletonList(localesAvailable));
            cms.unlockResource(resourceName);
        }

        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        for (String filename : filenames.keySet()) {
            String absoluteFileName = cms.getRequestContext().addSiteRoot(folderName + filename);
            SolrQuery squery = new CmsSolrQuery();
            squery.addFilterQuery("path:\"" + absoluteFileName + "\"");
            results = index.search(cms, squery);
            assertEquals(1, results.size());
            CmsSearchResource res = results.get(0);
            List<String> fieldLocales = res.getMultivaluedField(CmsSearchField.FIELD_CONTENT_LOCALES);
            assertTrue(fieldLocales.size() == 1);
            assertTrue(fieldLocales.contains("en"));
        }

        SolrQuery squery = new CmsSolrQuery();
        squery.addFilterQuery("path:\"/sites/default/xmlcontent/article_0004.html\"");
        results = index.search(cms, squery);
        assertEquals(1, results.size());
        CmsSearchResource res = results.get(0);
        List<String> fieldLocales = res.getMultivaluedField(CmsSearchField.FIELD_CONTENT_LOCALES);
        assertTrue(fieldLocales.size() == 1);
        fieldLocales.contains(Collections.singletonList("en"));
    }

    /**
     *
     * @throws Throwable
     */
    public void testDependencies() throws Throwable {

        List<String> filenames = new ArrayList<String>();
        filenames.add("search_rabbit.pdf");
        filenames.add("search_rabbit_vi.pdf");
        filenames.add("search_rabbit_pt.pdf");
        filenames.add("search_rabbit_fr.pdf");
        filenames.add("search_rabbit_fr_001.pdf");
        filenames.add("search_rabbit_fr_002.pdf");

        // create test folder
        String folderName = "/testLocaleVariants/";
        CmsObject cms = getCmsObject();
        cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
        cms.unlockResource(folderName);
        for (String filename : filenames) {
            importTestResource(
                cms,
                "org/opencms/search/pdf-test-112.pdf",
                folderName + filename,
                CmsResourceTypeBinary.getStaticTypeId(),
                Collections.<CmsProperty> emptyList());
        }
        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrQuery query = new CmsSolrQuery();
        query.setSearchRoots(Collections.singletonList(cms.getRequestContext().addSiteRoot(folderName)));
        CmsSolrResultList results = index.search(cms, query, false);
        AllTests.printResults(cms, results, false);
        int count = 0;
        for (CmsSearchResource res : results) {
            System.out.println("---------------------");
            System.out.println("DOCUMENT: " + ++count);
            System.out.println("---------------------");

            System.out.println("TYPE: " + res.getField("dependencyType"));
            String depField = res.getField("dep_document");
            if (depField != null) {
                System.out.println("DEP_DOC: " + depField);
                CmsDocumentDependency d = CmsDocumentDependency.fromDependencyString(depField, res.getRootPath());
                String depCreated = d.toDependencyString(cms);
                assertEquals(depField, depCreated);
            }

            List<String> variants = res.getMultivaluedField("dep_variant");
            if (variants != null) {
                System.out.println("---");
                System.out.println("VARIANTS");
                for (String varField : variants) {
                    System.out.println("DEP_VAR: " + varField);
                    JSONObject varJson = new JSONObject(varField);
                    String path = varJson.getString("path");
                    CmsDocumentDependency var = CmsDocumentDependency.fromDependencyString(varField, path);
                    String varCreated = var.toDependencyString(cms);
                    assertEquals(varField, varCreated);
                }
            }

            List<String> attachments = res.getMultivaluedField("dep_attachment");
            if (attachments != null) {
                System.out.println("---");
                System.out.println("ATTACHMENTS");
                for (String attField : attachments) {
                    System.out.println("DEP_ATT: " + attField);
                    JSONObject attJson = new JSONObject(attField);
                    String path = attJson.getString("path");
                    CmsDocumentDependency att = CmsDocumentDependency.fromDependencyString(attField, path);
                    String varCreated = att.toDependencyString(cms);
                    assertEquals(attField, varCreated);
                }
            }
        }
    }

    /**
     * @throws Throwable if something goes wrong
     */
    public void testIngnoreMaxRows() throws Throwable {

        echo("Testing the ignore max rows argument.");
        String query = "?fq=con_locales:*&fq=parent-folders:*&fl=path&rows=99999";
        CmsSolrQuery squery = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(query));
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrResultList result = index.search(getCmsObject(), squery, true);
        int found = result.size();
        assertTrue(
            "The number of found documents must be greater than org.opencms.search.solr.CmsSolrIndex.ROWS_MAX",
            found > CmsSolrIndex.ROWS_MAX);
    }

    /**
     * @throws Throwable
     */
    public void testLanguageDetection() throws Throwable {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        // use a folder that only contains GERMAN content @see manifest.xml -> locale poperty
        String folderName = "/folder1/subfolder12/subsubfolder121/";

        List<CmsProperty> props = new ArrayList<CmsProperty>();
        props.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, "de", "de"));
        CmsResource master = importTestResource(
            cms,
            "org/opencms/search/solr/lang-detect-doc.pdf",
            folderName + "lang-detect-doc.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            props);

        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        // index.setLanguageDetection(true);
        // is the default configured in opencms-search.xml
        CmsSolrQuery query = new CmsSolrQuery(cms, null);
        query.setText("Language Detection Document");
        // even if the property is set to German this document should be detected as English
        // because the language inside the PDF is written in English
        query.setLocales(Collections.singletonList(Locale.GERMAN));
        CmsSolrResultList result = index.search(cms, query);
        assertTrue(!result.contains(master));
        query.setLocales(Collections.singletonList(Locale.ENGLISH));
        result = index.search(cms, query);
        assertTrue(result.contains(master));

        // Now test the other way around: German locale-available property with English content
        // Should be detected as German
        // This is the OpenCms default behavior: property wins!
        index.setLanguageDetection(false);
        props.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_AVAILABLE_LOCALES, "de", "de"));
        CmsResource master2 = importTestResource(
            cms,
            "org/opencms/search/solr/lang-detect-doc.pdf",
            folderName + "lang-detect-doc2.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            props);
        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        query.setLocales(Collections.singletonList(Locale.GERMAN));
        result = index.search(cms, query);
        assertTrue(result.contains(master2));
        query.setLocales(Collections.singletonList(Locale.ENGLISH));
        result = index.search(cms, query);
        assertTrue(!result.contains(master2));

        // restore configured value
        index.setLanguageDetection(true);
    }

    /**
     * Checks if the extra fields for list sort options are index correctly.
     * @throws Exception should not happen
     */
    public void testListSearchFields() throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        importModule(getCmsObject(), "org.opencms.test.modules.solr.listsearchfields");

        String query = "q=*:*&fq=parent-folders:\"/sites/default/containerpages/\"&rows=10";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        CmsSolrResultList results = index.search(cms, query);
        for (CmsSearchResource result : results) {
            switch (result.getRootPath()) {
                case "/sites/default/containerpages/index.html":
                    checkIndexedListSearchFields(
                        result,
                        "Container pages description set via the sitemap editor",
                        "Container pages keywords set via the sitemap editor");
                    break;
                case "/sites/default/containerpages/other.html":
                    checkIndexedListSearchFields(result, null, null);
                    break;
                case "/sites/default/containerpages/sub-page-without-extra-properties/index.html":
                    checkIndexedListSearchFields(result, null, null);
                    break;
                case "/sites/default/containerpages/subpage/index.html":
                    checkIndexedListSearchFields(
                        result,
                        "Sub page description set via the page editor",
                        "Sub page keywords set via the page editor");
                    break;
            }
        }
        // The update is more a test for the CmsVfsIndexer, but it's mostly relevant here, so we put it here
        CmsResource folderToAdjust = cms.readResource("/sites/default/containerpages");
        cms.lockResource(folderToAdjust);
        cms.writePropertyObjects(
            folderToAdjust,
            Collections.singletonList(
                new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, "Adjusted description from folder", null)));
        cms.unlockResource(folderToAdjust);
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        String queryForChangedResource = "q=*:*&fq=path:\"/sites/default/containerpages/index.html\"&rows=1";
        CmsSolrResultList changedResults = index.search(cms, queryForChangedResource);
        assertEquals(1, changedResults.size());
        CmsSearchResource defaultFile = changedResults.get(0);
        checkIndexedListSearchFields(
            defaultFile,
            "Adjusted description from folder",
            "Container pages keywords set via the sitemap editor");
    }

    /**
     * Checks if the extra fields for list sort options are index correctly.
     * @throws Exception should not happen
     */
    public void testListSortOptionFields() throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        importModule(getCmsObject(), "org.opencms.test.modules.solr.additionalsortfields");
        CmsResource g = cms.readResource("/sites/default/files/g.txt", CmsResourceFilter.ALL);
        CmsResource g1 = cms.readResource("/sites/default/files/1/g.txt", CmsResourceFilter.ALL);
        try (AutoCloseable c = CmsLockUtil.withLockedResources(cms, g, g1)) {
            cms.setDateExpired(g, System.currentTimeMillis() + 10000, false);
            cms.setDateExpired(g1, System.currentTimeMillis() + 10000, false);
        }
        OpenCms.getPublishManager().publishResource(cms, g.getRootPath());
        OpenCms.getPublishManager().publishResource(cms, g1.getRootPath());
        OpenCms.getPublishManager().waitWhileRunning();

        String query = "q=*:*&fq=parent-folders:\"/sites/default/files/\"&rows=20";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        CmsSolrResultList results = index.search(cms, query);
        assertEquals(16, results.size());
        for (CmsSearchResource result : results) {
            switch (result.getRootPath()) {
                case "/sites/default/files/a.txt":
                    checkIndexedSortOptionFields(result, "Title", "Title_en", 2, 3, "lastmodified");
                    break;
                case "/sites/default/files/b.txt":
                    checkIndexedSortOptionFields(result, "b.txt", "b.txt", 0, 0, "lastmodified");
                    break;
                case "/sites/default/files/c.txt":
                    checkIndexedSortOptionFields(result, "Title", "Title", 10, 10, "contentdate");
                    break;
                case "/sites/default/files/d.txt":
                    checkIndexedSortOptionFields(result, "d.txt", "Title_en", 0, 11, "lastmodified");
                    break;
                case "/sites/default/files/e.txt":
                    checkIndexedSortOptionFields(result, "e.txt", "e.txt", 0, 0, "lastmodified");
                    break;
                case "/sites/default/files/f.txt":
                    checkIndexedSortOptionFields(result, "f.txt", "f.txt", 0, 0, "released");
                    break;
                case "/sites/default/files/g.txt":
                    checkIndexedSortOptionFields(result, "g.txt", "g.txt", 0, 0, "expired");
                    break;
                case "/sites/default/files/h.txt":
                    checkIndexedSortOptionFields(result, "h.txt", "h.txt", 0, 0, "released");
                    break;
                case "/sites/default/files/1/a.txt":
                    checkIndexedSortOptionFields(result, "Title", "Title_en", 2, 3, "lastmodified");
                    break;
                case "/sites/default/files/1/b.txt":
                    checkIndexedSortOptionFields(result, "b.txt", "b.txt", 5, 6, "created");
                    break;
                case "/sites/default/files/1/c.txt":
                    checkIndexedSortOptionFields(result, "Title", "Title", 10, 6, "contentdate");
                    break;
                case "/sites/default/files/1/d.txt":
                    checkIndexedSortOptionFields(result, "d.txt", "Title_en", 5, 11, "lastmodified");
                    break;
                case "/sites/default/files/1/e.txt":
                    checkIndexedSortOptionFields(result, "e.txt", "e.txt", 5, 6, "lastmodified");
                    break;
                case "/sites/default/files/1/f.txt":
                    checkIndexedSortOptionFields(result, "f.txt", "f.txt", 5, 6, "released");
                    break;
                case "/sites/default/files/1/g.txt":
                    checkIndexedSortOptionFields(result, "g.txt", "g.txt", 5, 6, "expired");
                    break;
                case "/sites/default/files/1/h.txt":
                    checkIndexedSortOptionFields(result, "h.txt", "h.txt", 5, 6, "created");
                    break;
                default:
                    throw new IllegalArgumentException(
                        "The search result contained an unexpected file: " + result.getRootPath());
            }
        }

    }

    /**
     *
     * @throws Throwable
     */
    public void testLocaleDependenciesField() throws Throwable {

        Map<String, List<String>> filenames = new HashMap<String, List<String>>();
        filenames.put("search_rabbit.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_de.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_de_001.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_de_002.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_de_003.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_de_004.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_0001.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_0002.pdf", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit_0003.pdf", Collections.singletonList("de_DE"));

        filenames.put("search_rabbit_en.pdf", Collections.singletonList("en_EN"));
        filenames.put("search_rabbit_en_001.pdf", Collections.singletonList("en_EN"));
        filenames.put("search_rabbit_en_002.pdf", Collections.singletonList("en_EN"));
        filenames.put("search_rabbit_en_003.pdf", Collections.singletonList("en_EN"));
        filenames.put("search_rabbit_en_004.pdf", Collections.singletonList("en_EN"));

        filenames.put("search_rabbit2_0001.pdff", Collections.singletonList("de_DE"));
        filenames.put("search_rabbit2_0001.pdf", Collections.singletonList("de_DE"));

        filenames.put("tema_00001.html", Collections.singletonList("de_DE"));
        filenames.put("ar_00001.xml", Collections.singletonList("de_DE"));

        // create test folder
        String folderName = "/filenameTest2/";
        CmsObject cms = getCmsObject();
        cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
        cms.unlockResource(folderName);
        for (String filename : filenames.keySet()) {
            // create master resource
            importTestResource(
                cms,
                "org/opencms/search/pdf-test-112.pdf",
                folderName + filename,
                CmsResourceTypeBinary.getStaticTypeId(),
                Collections.<CmsProperty> emptyList());
        }
        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrQuery query = new CmsSolrQuery();
        query.setSearchRoots(Collections.singletonList(cms.getRequestContext().addSiteRoot(folderName)));
        CmsSolrResultList results = index.search(cms, query, false);
        AllTests.printResults(cms, results, false);
        // assertEquals(10, results.getNumFound());

        for (Map.Entry<String, List<String>> filename : filenames.entrySet()) {
            String absoluteFileName = cms.getRequestContext().addSiteRoot(folderName + filename.getKey());
            SolrQuery squery = new CmsSolrQuery();
            squery.addFilterQuery("path:" + absoluteFileName);
            results = index.search(cms, squery);
        }
    }

    /**
     * Tests if the field configuration in the 'opencms-search.xml' can also be used for Solr.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLuceneMigration() throws Throwable {

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);

        CmsSolrFieldConfiguration conf = (CmsSolrFieldConfiguration)index.getFieldConfiguration();
        assertNotNull(conf.getSolrFields().get("meta"));
        assertNotNull(conf.getSolrFields().get("description_txt"));
        assertNotNull(conf.getSolrFields().get("keywords_txt"));
        assertNotNull(conf.getSolrFields().get("special_txt"));

        CmsSolrQuery squery = new CmsSolrQuery(
            null,
            CmsRequestUtil.createParameterMap("q=path:\"/sites/default/xmlcontent/article_0001.html\""));
        CmsSolrResultList results = index.search(getCmsObject(), squery);

        CmsSearchResource res = results.get(0);
        String value = "Sample article 1  (>>SearchEgg1<<)";
        List<String> metaValue = res.getMultivaluedField("meta");
        assertEquals(value, metaValue.get(0));
    }

    /**
     * Tests the access of Offline indexes.<p>
     *
     * @throws Throwable if sth. goes wrong
     */
    public void testOfflineIndexAccess() throws Throwable {

        echo("Testing offline index access with the guest user.");
        CmsObject guest = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        CmsSolrIndex solrIndex = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        echo("First execute a search on the online index.");
        solrIndex.search(guest, "q=*:*&rows=0");
        echo("OK, search could be executed on the online index.");

        echo("Now try to execute a search on the Solr Offline index.");
        solrIndex = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_OFFLINE);
        solrIndex.setEnabled(true);
        try {
            solrIndex.search(guest, "q=*:*&rows=0");
        } catch (CmsSearchException e) {
            assertTrue(
                "The cause must be a CmsRoleViolationException",
                e.getCause() instanceof CmsRoleViolationException);
            if (!(e.getCause() instanceof CmsRoleViolationException)) {
                throw e;
            }
        }
        echo("OK, search could not be executed and the cause was a CmsRoleViolationException.");
        solrIndex.setEnabled(false);
    }

    /**
     * Checks if the extra fields for the list sort options are indexed correctly.
     * @param result the search result
     * @param title the expected display title
     * @param titleEn the expected display title for English
     * @param order the expected display order
     * @param orderEn the expected display order for English
     * @param instanceDateCopyField the field from which the instance date is expected to be copied.
     */
    private void checkIndexedListSearchFields(CmsSearchResource result, String description, String keywords) {

        echo("Checking resource \"" + result.getRootPath() + "\" ...");
        assertEquals(description, result.getField("description_en"));
        assertEquals(keywords, result.getField("keywords_en"));
    }

    /**
     * Checks if the extra fields for the list sort options are indexed correctly.
     * @param result the search result
     * @param title the expected display title
     * @param titleEn the expected display title for English
     * @param order the expected display order
     * @param orderEn the expected display order for English
     * @param instanceDateCopyField the field from which the instance date is expected to be copied.
     */
    private void checkIndexedSortOptionFields(
        CmsSearchResource result,
        String title,
        String titleEn,
        int order,
        int orderEn,
        String instanceDateCopyField) {

        echo("Checking resource \"" + result.getRootPath() + "\" ...");
        assertEquals(title, result.getField("disptitle_sort"));
        assertEquals(titleEn, result.getField("disptitle_en_sort"));
        assertEquals(order, Integer.parseInt(result.getField("disporder_i")));
        assertEquals(orderEn, Integer.parseInt(result.getField("disporder_en_i")));
        assertEquals(result.getDateField(instanceDateCopyField), result.getDateField("instancedate_dt"));
        assertEquals(result.getDateField(instanceDateCopyField), result.getDateField("instancedate_en_dt"));
        assertEquals(result.getDateField(instanceDateCopyField), result.getDateField("instancedatecurrenttill_dt"));
        assertEquals(result.getDateField(instanceDateCopyField), result.getDateField("instancedatecurrenttill_en_dt"));
    }
}
