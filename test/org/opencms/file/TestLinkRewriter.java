/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.test.I_CmsLogHandler;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.spi.LoggingEvent;

import junit.framework.Test;

/**
 * Test class for the CmsLinkRewriter class.<p>
 */
/**
 *
 */
public class TestLinkRewriter extends OpenCmsTestCase {

    /**
     * A log handler which detects whether an error message containing a given string is written to the log.<p>
     */
    public class ExpectErrorLogHandler implements I_CmsLogHandler {

        /** The string which should be detected in log messages. */
        private String m_text;

        /** A flag which indicates whether the given string has been encountered in a log message. */
        private boolean m_triggered;

        /**
         * Creates a new instance.<p>
         *
         * @param text the text to detect in log messages
         */
        public ExpectErrorLogHandler(String text) {

            m_text = text;
        }

        /**
         * This method will be called when log events are triggered.<p>
         *
         * @param event the log event
         */
        public void handleLogEvent(LoggingEvent event) {

            if (event.getLevel().toString().equalsIgnoreCase("error")
                && event.getMessage().toString().toLowerCase().contains(m_text.toLowerCase())) {
                m_triggered = true;
            }
        }

        /**
         * Returns true if the text has been detected in log messages.<p>
         *
         * @return true if the text has been detected in log messages
         */
        public boolean isTriggered() {

            return m_triggered;
        }

    }

    /**
     * Creates a new test suite instance.<p>
     *
     * @param name test case init parameter
     */
    public TestLinkRewriter(String name) {

        super(name);

    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestLinkRewriter.class, "linkrewrite", "/");
    }

    /**
     * Helper method to compare sets by sorting them and converting them to strings first.<p>
     *
     * @param expected the expected value
     * @param actual the actual value to check
     */
    @SuppressWarnings("rawtypes")
    public void assertSetEquals(Set expected, Set actual) {

        // use sorted sets and convert them to strings so the test runner output for failed assertions is easier to read
        @SuppressWarnings("unchecked")
        TreeSet expected2 = new TreeSet(expected);
        @SuppressWarnings("unchecked")
        TreeSet actual2 = new TreeSet(actual);
        assertEquals(expected2.toString(), actual2.toString());
    }

    /**
     * Converts a path to a canonical form.<p>
     *
     * @param path the path to convert
     *
     * @return the canonical form of the path
     */
    public String canonicalize(String path) {

        path = CmsStringUtil.joinPaths("/", path);
        if (!path.equals("/")) {
            path = path.replaceFirst("/$", "");
        }
        return path;

    }

    /**
     * Copies the /system/base folder to a given path and adjusts the links for that path.<p>
     *
     * @param target the target path
     *
     * @throws CmsException if something goes wrong
     */
    public void copyAndAdjust(String target) throws CmsException {

        CmsObject cms = getCmsObject();
        cms.copyResource("/system/base", target);
        cms.unlockResource(target);
        cms.adjustLinks("/system/base", target);
    }

    /**
     * Creates a folder at a given path.<p>
     *
     * @param path the path of the folder
     *
     * @throws CmsException if something goes wrong
     */
    public void createFolder(String path) throws CmsException {

        CmsObject cms = getCmsObject();
        cms.createResource(path, 0);
    }

    /**
     * Helper method for deleting a directory.<p>
     *
     * @param directory the directory to remove
     *
     * @throws CmsException if something goes wrong
     */
    @Override
    public void delete(String directory) throws CmsException {

        CmsObject cms = getCmsObject();
        try {
            cms.lockResource(directory);
        } catch (CmsException e) {
            // noop
        }
        cms.deleteResource(directory, CmsResource.DELETE_PRESERVE_SIBLINGS);
    }

    /**
     * Gets the set of relation strings which a given folder should have if it had been copied from /system/base
     * and had its links adjusted afterwards.<p>
     *
     * @param root the target folder
     * @return the relation strings
     */
    public Set<String> getBaseLinks(String root) {

        Set<String> result = new HashSet<String>();
        String x = CmsStringUtil.joinPaths("/", root, "a/x.html");
        String y = CmsStringUtil.joinPaths("/", root, "a/y.html");
        String z = CmsStringUtil.joinPaths("/", root, "a/z.html");
        String j = CmsStringUtil.joinPaths("/", root, "a/j.jsp");
        String base = CmsStringUtil.joinPaths("/" + root);

        result.add("XML_WEAK:" + x + ":" + y);
        result.add("XML_WEAK:" + x + ":" + x);
        result.add("XML_WEAK:" + y + ":" + x);
        result.add("TESTRELATION1:" + z + ":" + y);
        result.add("TESTRELATION2:" + x + ":" + y);
        result.add("TESTRELATION1:" + base + ":" + z);
        result.add("TESTRELATION1:" + z + ":" + base);
        result.add("JSP_STRONG:" + j + ":" + x);

        return result;
    }

    /**
     * Gets a list of relations from resources in one subtree to resources in another.<p>
     *
     * @param firstFolder the root folder of the first subtree
     * @param secondFolder the root folder of the second subtree
     *
     * @return the list of relations between the two subtrees
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsRelation> getLinksBetween(String firstFolder, String secondFolder) throws CmsException {

        CmsObject cms = getCmsObject();
        firstFolder = CmsStringUtil.joinPaths(firstFolder, "/");
        secondFolder = CmsStringUtil.joinPaths(secondFolder, "/");
        List<CmsRelation> relations = cms.readRelations(
            CmsRelationFilter.SOURCES.filterPath(firstFolder).filterIncludeChildren());
        List<CmsRelation> result = new ArrayList<CmsRelation>();
        for (CmsRelation relation : relations) {
            if (relation.getTargetPath().startsWith(secondFolder)) {
                result.add(relation);
            }
        }
        return result;
    }

    /**
     * Returns the links for the multibase adjustment test, relocated to a given root path.<p>
     *
     * @param root the new root path
     * @return the set of re-rooted links for the multibase test
     */
    public Set<String> getMultiBaseLinks(String root) {

        String d1 = CmsStringUtil.joinPaths("/", root, "D1");
        String d2 = CmsStringUtil.joinPaths("/", root, "D1/D2.html");
        String d3 = CmsStringUtil.joinPaths("/", root, "D3.html");
        Set<String> result = new HashSet<String>();
        result.add("TESTRELATION1:" + d1 + ":" + d2);
        result.add("XML_WEAK:" + d2 + ":" + d3);
        result.add("XML_WEAK:" + d3 + ":" + d1);
        return result;
    }

    /**
     * Converts a single relation to a string of the form TYPE:SOURCE:TARGET.<p>
     *
     * @param relation the relation
     *
     * @return the string representing the relation
     */
    public String getRelationCode(CmsRelation relation) {

        String source = canonicalize(relation.getSourcePath());
        String target = canonicalize(relation.getTargetPath());
        String result = relation.getType().getName() + ":" + source + ":" + target;
        return result;
    }

    /**
     * Helper method to convert a collection of relations to a set of strings.<p>
     *
     * @param relations the relations
     *
     * @return a set of strings representing the relations
     */
    public Set<String> getRelationSet(Collection<CmsRelation> relations) {

        Set<String> result = new HashSet<String>();
        for (CmsRelation relation : relations) {
            result.add(getRelationCode(relation));
        }
        return result;
    }

    /**
     * Tests that the link rewriting process can handle missing files in the target folder structure.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testAdjustWithMissingFile() throws Exception {

        CmsObject cms = getCmsObject();
        cms.copyResource("/system/base", "/system/baseMissing");
        cms.deleteResource("/system/baseMissing/a/x.html", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.adjustLinks("/system/base", "/system/baseMissing");
        Set<String> expected = new HashSet<String>();
        expected.add("TESTRELATION1:/system/baseMissing/a/z.html:/system/baseMissing/a/y.html");
        expected.add("TESTRELATION1:/system/baseMissing:/system/baseMissing/a/z.html");
        expected.add("TESTRELATION1:/system/baseMissing/a/z.html:/system/baseMissing");
        assertSetEquals(expected, links("/system/baseMissing", "/system/baseMissing"));

        cms.copyResource("/system/base", "/system/baseMissing2");
        cms.deleteResource("/system/baseMissing2/a/y.html", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.adjustLinks("/system/base", "/system/baseMissing2");
        expected = new HashSet<String>();
        expected.add("XML_WEAK:/system/baseMissing2/a/x.html:/system/baseMissing2/a/x.html");
        expected.add("TESTRELATION1:/system/baseMissing2:/system/baseMissing2/a/z.html");
        expected.add("TESTRELATION1:/system/baseMissing2/a/z.html:/system/baseMissing2");
        expected.add("JSP_STRONG:/system/baseMissing2/a/j.jsp:/system/baseMissing2/a/x.html");
        assertSetEquals(expected, links("/system/baseMissing2", "/system/baseMissing2"));

    }

    /**
     * Tests whether the link rewriting works in the system folder.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testCopy() throws Exception {

        Set<String> expected = null;
        copyAndAdjust("/system/baseTestCopy");
        expected = getBaseLinks("/system/base");
        assertSetEquals(expected, links("/system/base", "/system/base"));

        assertSetEquals(Collections.emptySet(), links("/system/base", "/system/baseTestCopy"));

        assertSetEquals(Collections.emptySet(), links("/system/baseTestCopy", "/system/base"));

        expected = getBaseLinks("/system/baseTestCopy");
        assertSetEquals(expected, links("/system/baseTestCopy", "/system/baseTestCopy"));
    }

    /**
     * Tests whether the link rewriting works in the current site.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testCopyToSite() throws Exception {

        CmsObject cms = getCmsObject();
        cms.copyResource("/system/base", "/sitebase");
        cms.adjustLinks("/system/base", "/sitebase");
        cms.copyResource("/sitebase", "/sitebase2");
        cms.adjustLinks("/sitebase", "/sitebase2");

        assertEquals(0, links("/sites/default/sitebase", "/system/base").size());

        Set<String> expected = getBaseLinks("/sites/default/sitebase");
        assertSetEquals(expected, links("/sites/default/sitebase", "/sites/default/sitebase"));

        assertEquals(0, links("/sites/default/sitebase", "/sites/default/sitebase2").size());
        assertEquals(0, links("/sites/default/sitebase2", "/sites/default/sitebase").size());
        expected = getBaseLinks("/sites/default/sitebase2");
        assertSetEquals(expected, links("/sites/default/sitebase2", "/sites/default/sitebase2"));
    }

    /**
     * Tests whether the encoding is converted correctly when rewriting links.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testEncodingConversion() throws Exception {

        CmsObject cms = getCmsObject();
        cms.lockResource("/system/enc/e1.html");
        CmsResource res = cms.readResource("/system/enc/e1.html");
        CmsFile file = cms.readFile(res);
        String contentString = new String(file.getContents(), "UTF-8");
        String contentWithWrongEncoding = new String(file.getContents(), "ISO-8859-1");
        assertTrue(contentString.contains("\u00e4\u00f6\u00fc\u00df"));
        assertTrue(contentString.contains("UTF-8"));
        assertFalse(contentWithWrongEncoding.contains("\u00e4\u00f6\u00fc\u00df"));
        cms.copyResource("/system/enc", "/system/enc2");
        CmsProperty encoding = new CmsProperty("content-encoding", "ISO-8859-1", "ISO-8859-1");
        cms.lockResource("/system/enc2");
        cms.writePropertyObject("/system/enc2", encoding);
        cms.adjustLinks("/system/enc", "/system/enc2");
        res = cms.readResource("/system/enc2/e1.html");
        file = cms.readFile(res);
        String newContent = new String(file.getContents(), "ISO-8859-1");
        String newContentWithWrongEncoding = new String(file.getContents(), "UTF-8");
        assertTrue(newContent.contains("\u00e4\u00f6\u00fc\u00df"));
        assertTrue(newContent.contains("ISO-8859-1"));
        assertFalse(newContentWithWrongEncoding.contains("\u00e4\u00f6\u00fc\u00df"));
    }

    /**
     * Tests that the link rewriting fails if one of two folder arguments is a subfolder of the other.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testFailIfTargetIsSubdirectory() throws Exception {

        CmsObject cms = getCmsObject();
        String path1 = "/system/tf1";
        String path2 = "/system/tf1/tf2";
        createFolder(path1);
        createFolder(path2);
        Exception exception = null;
        try {
            cms.adjustLinks(path1, path2);
        } catch (CmsIllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    /**
     * Tests relation adjustments for multiple source files.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testMultiCopy() throws Exception {

        CmsObject cms = getCmsObject();
        cms.lockResource("/system/multibase2");
        cms.copyResource("/system/multibase/D1", "/system/multibase2/D1");
        cms.copyResource("/system/multibase/D3.html", "/system/multibase2/D3.html");
        List<String> sources = new ArrayList<String>();
        sources.add("/system/multibase/D1");
        sources.add("/system/multibase/D3.html");
        cms.adjustLinks(sources, "/system/multibase2");
        String external = "XML_WEAK:/system/multibase2/D4.html:/system/multibase/D4.html";
        assertSetEquals(Collections.singleton(external), links("/system/multibase2", "/system/multibase"));
        Set<String> expected = getMultiBaseLinks("/system/multibase2");
        assertSetEquals(expected, links("/system/multibase2", "/system/multibase2"));
        assertTrue(links("/system/multibase", "/system/multibase").contains(
            "XML_WEAK:/system/multibase/D4.html:/system/multibase/D4.html"));

    }

    /**
     * Tests that an error when rewriting a file will not throw an exception, but will be written to the log.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testNotAbort() throws Exception {

        CmsObject cms = getCmsObject();

        ExpectErrorLogHandler handler = new ExpectErrorLogHandler("xml validation error");
        try {
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCmsTestLogAppender.setHandler(handler);

            // Setup: Create a XML content that is wrong according to the schema
            String wrongContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "\r\n"
                + "<LinkSequences xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"internal://org/opencms/file/links.xsd\">\r\n"
                + "  <LinkSequence language=\"en\">\r\n"
                + "  <Text>Correct node</Text>\r\n"
                + "  <!-- This 2nd Text node is intentionally wrong according to the schema! --> \r\n"
                + "  <Text>Wrong node</Text>\r\n"
                + "\r\n"
                + "    <Link>\r\n"
                + "       <link type=\"WEAK\">\r\n"
                + "        <target><![CDATA[/system/w/wrong.html]]></target>\r\n"
                + "        <uuid>00000000-0020-0000-0000-000000000000</uuid>\r\n"
                + "      </link>\r\n"
                + "    </Link>\r\n"
                + "  </LinkSequence>\r\n"
                + "</LinkSequences>";

            // Must create the resource as plain and change to XmlContent otherwise creation will throw Exception
            cms.createResource(
                "/system/w/kaputt.html",
                CmsResourceTypePlain.getStaticTypeId(),
                wrongContent.getBytes(),
                null);
            // get type id from existing resource
            int type = cms.readResource("/system/w/wrong.html").getTypeId();
            // change type to XmlContent
            cms.chtype("/system/w/kaputt.html", type);

            printExceptionWarning();
            cms.copyResource("/system/w", "/system/w2");
            cms.adjustLinks("/system/w", "/system/w2");
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(true);
            OpenCmsTestLogAppender.setHandler(null);
            assertTrue(handler.isTriggered());
        }
    }

    /**
     * Test to verify that the files under /system/base have been created correctly by the test setup (not really necessary,
     * just to make sure that e.g. a faulty manifest.xml is not the cause of other test failures).
     *
     * @throws Exception in case the test fails
     */
    public void testVerifySetup() throws Exception {

        assertSetEquals(getBaseLinks("/system/base"), links("/system/base", "/system/base"));
        String additional = "XML_WEAK:/system/multibase/D4.html:/system/multibase/D4.html";
        Set<String> expected = getMultiBaseLinks("/system/multibase");
        expected.add(additional);
        assertSetEquals(expected, links("/system/multibase", "/system/multibase"));

    }

    /**
     * Gets the string representations of relations between two subtrees.<p>
     *
     * @param source the root of the first subtree
     * @param target the root of the second subtree
     *
     * @return the set of the string representations of the relations
     *
     * @throws CmsException if something goes wrong
     */
    protected Set<String> links(String source, String target) throws CmsException {

        return getRelationSet(getLinksBetween(source, target));
    }

}
