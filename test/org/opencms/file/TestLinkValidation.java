/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestLinkValidation.java,v $
 * Date   : $Date: 2006/11/29 15:04:06 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms link validation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.3 $
 */
public class TestLinkValidation extends OpenCmsTestCase {

    private static final int MODE_XMLCONTENT_BOTH = 1;
    private static final int MODE_XMLCONTENT_FILEREF_ONLY = 3;
    private static final int MODE_XMLCONTENT_HTML_ONLY = 2;
    private static final int MODE_XMLPAGE = 0;

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestLinkValidation(String arg0) {

        super(arg0);
    }

    /**
     * Sets the content of a resource.<p>
     * 
     * @param cms the cms context
     * @param filename the resource name
     * @param content the content to set
     * 
     * @throws CmsException if something goes wrong 
     */
    public static void setContent(CmsObject cms, String filename, String content) throws CmsException {

        CmsResource res = cms.readResource(filename);
        CmsFile file = CmsFile.upgrade(res, cms);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file, true);
        if (!page.hasValue("test", Locale.ENGLISH)) {
            page.addValue("test", Locale.ENGLISH);
        }
        page.setStringValue(cms, "test", Locale.ENGLISH, content);
        file.setContents(page.marshal());
        cms.lockResource(filename);
        cms.writeFile(file);
    }

    /**
     * Sets the content of a xmlcontent resource.<p>
     * 
     * @param cms the cms context
     * @param filename the resource name
     * @param html the content to set in the text field
     * @param link the vfs file reference to set
     * 
     * @throws CmsException if something goes wrong 
     */
    public static void setXmlContent(CmsObject cms, String filename, String html, String link) throws CmsException {

        CmsResource res = cms.readResource(filename);
        CmsFile file = CmsFile.upgrade(res, cms);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
        if (!content.hasValue("Text", Locale.ENGLISH, 0)) {
            content.addValue(cms, "Text", Locale.ENGLISH, 0);
        }
        content.getValue("Text", Locale.ENGLISH, 0).setStringValue(cms, html);
        if (!content.hasValue("Homepage", Locale.ENGLISH, 0)) {
            content.addValue(cms, "Homepage", Locale.ENGLISH, 0);
        }
        content.getValue("Homepage", Locale.ENGLISH, 0).setStringValue(cms, link);
        file.setContents(content.marshal());
        cms.lockResource(filename);
        cms.writeFile(file);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestLinkValidation.class.getName());

        suite.addTest(new TestLinkValidation("testLinkValidationXmlPages"));
        suite.addTest(new TestLinkValidation("testLinkValidationXmlContents"));
        suite.addTest(new TestLinkValidation("testLinkValidationXmlContentsHtml"));
        suite.addTest(new TestLinkValidation("testLinkValidationXmlContentsFileRef"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test link validation for xml contents with html and file references.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLinkValidationXmlContents() throws Throwable {

        echo("Testing link validation for xml contents with html and file references");

        testLinkValidation(MODE_XMLCONTENT_BOTH);
    }

    /**
     * Test link validation for xml contents with only file references.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLinkValidationXmlContentsFileRef() throws Throwable {

        echo("Testing link validation for xml contents with only file references");

        testLinkValidation(MODE_XMLCONTENT_FILEREF_ONLY);
    }

    /**
     * Test link validation for xml contents with only html.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLinkValidationXmlContentsHtml() throws Throwable {

        echo("Testing link validation for xml contents with only html");

        testLinkValidation(MODE_XMLCONTENT_HTML_ONLY);
    }

    /**
     * Test link validation for xml pages.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLinkValidationXmlPages() throws Throwable {

        echo("Testing link validation for xml pages");

        testLinkValidation(MODE_XMLPAGE);
    }

    /**
     * Deletes a resource and publish it.<p>
     * 
     * @param cms the cms context
     * @param resName the resource name
     * @param report the report
     * 
     * @throws Exception if something goes wrong
     */
    private void delete(CmsObject cms, String resName, CmsShellReport report) throws Exception {

        cms.lockResource(resName);
        cms.deleteResource(resName, CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.unlockResource(resName);
        cms.publishResource(resName, true, report);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Restores the first version of a resource.<p>
     * 
     * @param cms the cms context
     * @param resName the resource name
     * @param report the report
     * 
     * @throws Exception if something goes wrong
     */
    private void restore(CmsObject cms, String resName, CmsShellReport report) throws Exception {

        // restore the first backup
        cms.createResource(resName, CmsResourceTypeXmlPage.getStaticTypeId());
        List backups = cms.readAllBackupFileHeaders(resName);
        CmsBackupResource backup = (CmsBackupResource)backups.get(backups.size() - 1);
        cms.restoreResourceBackup(resName, backup.getTagId());
        cms.unlockResource(resName);
        cms.publishResource(resName, true, report);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.lockResource(resName);
    }

    /**
     * Sets the content of a xmlcontent resource.<p>
     * 
     * @param cms the cms context
     * @param filename the resource name
     * @param link1 the 1st vfs file reference to set
     * @param link2 the 2nd vfs file reference to set
     * 
     * @throws CmsException if something goes wrong 
     */
    private void setXmlContentFileRef(CmsObject cms, String filename, String link1, String link2) throws CmsException {

        CmsResource res = cms.readResource(filename);
        CmsFile file = CmsFile.upgrade(res, cms);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
        if (!content.hasValue("Homepage", Locale.ENGLISH, 0) && (link1 != null)) {
            content.addValue(cms, "Homepage", Locale.ENGLISH, 0);
        }
        if (link1 != null) {
            content.getValue("Homepage", Locale.ENGLISH, 0).setStringValue(cms, link1);
        } else {
            if (content.hasValue("Homepage", Locale.ENGLISH, 0)) {
                content.removeValue("Homepage", Locale.ENGLISH, 0);
            }
        }
        if (!content.hasValue("Homepage", Locale.ENGLISH, 1) && (link2 != null)) {
            content.addValue(cms, "Homepage", Locale.ENGLISH, 1);
        }
        if (link2 != null) {
            content.getValue("Homepage", Locale.ENGLISH, 1).setStringValue(cms, link2);
        } else {
            if (content.hasValue("Homepage", Locale.ENGLISH, 1)) {
                content.removeValue("Homepage", Locale.ENGLISH, 1);
            }
        }
        file.setContents(content.marshal());
        cms.lockResource(filename);
        cms.writeFile(file);
    }

    /**
     * Sets the content of a xmlcontent resource.<p>
     * 
     * @param cms the cms context
     * @param filename the resource name
     * @param html the content to set in the text field
     * 
     * @throws CmsException if something goes wrong 
     */
    private void setXmlContentHtml(CmsObject cms, String filename, String html) throws CmsException {

        CmsResource res = cms.readResource(filename);
        CmsFile file = CmsFile.upgrade(res, cms);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
        if (!content.hasValue("Text", Locale.ENGLISH, 0)) {
            content.addValue(cms, "Text", Locale.ENGLISH, 0);
        }
        content.getValue("Text", Locale.ENGLISH, 0).setStringValue(cms, html);
        file.setContents(content.marshal());
        cms.lockResource(filename);
        cms.writeFile(file);
    }

    /**
     * Test the link validation for different resource types.<p>
     * 
     * @param mode the mode: 
     *      <code>0</code> for hmlpage, 
     *      <code>1</code> for xmlcontent with html and file ref, 
     *      <code>2</code> for xmlcontent with html only, 
     *      <code>3</code> for xmlcontent with file ref only
     *       
     * @throws Exception if something goes wrong 
     */
    private void testLinkValidation(int mode) throws Exception {

        CmsObject cms = getCmsObject();
        String filename1, filename2, filename3, filename4, filename5, filename6, filename7, filename8;
        switch (mode) {
            case MODE_XMLCONTENT_BOTH:
                filename1 = "/xmlcontent1.html";
                filename2 = "/xmlcontent2.html";
                filename3 = "/xmlcontent3.html";
                filename4 = "/xmlcontent4.html";
                filename5 = "/xmlcontent5.html";
                filename6 = "/xmlcontent6.html";
                filename7 = "/xmlcontent7.html";
                filename8 = "/xmlcontent8.html";
                break;
            case MODE_XMLCONTENT_HTML_ONLY:
                filename1 = "/xmlcontent1html.html";
                filename2 = "/xmlcontent2html.html";
                filename3 = "/xmlcontent3html.html";
                filename4 = "/xmlcontent4html.html";
                filename5 = "/xmlcontent5html.html";
                filename6 = "/xmlcontent6html.html";
                filename7 = "/xmlcontent7html.html";
                filename8 = "/xmlcontent8html.html";
                break;
            case MODE_XMLCONTENT_FILEREF_ONLY:
                filename1 = "/xmlcontent1ref.html";
                filename2 = "/xmlcontent2ref.html";
                filename3 = "/xmlcontent3ref.html";
                filename4 = "/xmlcontent4ref.html";
                filename5 = "/xmlcontent5ref.html";
                filename6 = "/xmlcontent6ref.html";
                filename7 = "/xmlcontent7ref.html";
                filename8 = "/xmlcontent8ref.html";
                break;
            default:
                filename1 = "/xmlpage1.html";
                filename2 = "/xmlpage2.html";
                filename3 = "/xmlpage3.html";
                filename4 = "/xmlpage4.html";
                filename5 = "/xmlpage5.html";
                filename6 = "/xmlpage6.html";
                filename7 = "/xmlpage7.html";
                filename8 = "/xmlpage8.html";
                break;
        }

        // create files
        int type;
        if (mode > MODE_XMLPAGE) {
            type = 12; // article
        } else {
            type = CmsResourceTypeXmlPage.getStaticTypeId();
        }
        CmsResource res1 = cms.createResource(filename1, type);
        CmsResource res2 = cms.createResource(filename2, type);
        CmsResource res3 = cms.createResource(filename3, type);
        CmsResource res4 = cms.createResource(filename4, type);
        CmsResource res5 = cms.createResource(filename5, type);
        CmsResource res6 = cms.createResource(filename6, type);
        CmsResource res7 = cms.createResource(filename7, type);

        // set the content
        String content1 = "<a href='" + filename2 + "'>file2</a><br><a href='" + filename3 + "'>file3</a>";
        String content4 = "<a href='" + filename2 + "'>file2</a>";
        String content5 = "<a href='" + filename6 + "'>file6</a>";
        String content6 = "<a href='" + filename5 + "'>file5</a>";
        CmsRelationType relType1 = null;
        switch (mode) {
            case MODE_XMLCONTENT_BOTH:
                setXmlContent(cms, filename1, content1, filename2);
                setXmlContent(cms, filename4, content4, filename2);
                setXmlContent(cms, filename5, content5, filename6);
                setXmlContent(cms, filename6, content6, filename5);
                relType1 = CmsRelationType.HYPERLINK;
                break;
            case MODE_XMLCONTENT_HTML_ONLY:
                setXmlContentHtml(cms, filename1, content1);
                setXmlContentHtml(cms, filename4, content4);
                setXmlContentHtml(cms, filename5, content5);
                setXmlContentHtml(cms, filename6, content6);
                relType1 = CmsRelationType.HYPERLINK;
                break;
            case MODE_XMLCONTENT_FILEREF_ONLY:
                setXmlContentFileRef(cms, filename1, filename2, filename3);
                setXmlContentFileRef(cms, filename4, filename2, null);
                setXmlContentFileRef(cms, filename5, filename6, null);
                setXmlContentFileRef(cms, filename6, filename5, null);
                relType1 = CmsRelationType.XML_WEAK;
                break;
            default:
                setContent(cms, filename1, content1);
                setContent(cms, filename4, content4);
                setContent(cms, filename5, content5);
                setContent(cms, filename6, content6);
                relType1 = CmsRelationType.HYPERLINK;
                break;

        }

        // check the links before publishing
        CmsShellReport report = new CmsShellReport(cms.getRequestContext().getLocale());
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        Map validation = cms.validateRelations(Collections.singletonList(res1), report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename1)));
        List brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename1));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 3 : 2));
        assertTrue(brokenLinks.contains(new CmsRelation(res1, res2, relType1)));
        assertTrue(brokenLinks.contains(new CmsRelation(res1, res3, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res1, res2, CmsRelationType.XML_WEAK)));
        }

        validation = cms.validateRelations(Collections.singletonList(res2), report);
        assertTrue(validation.isEmpty());

        validation = cms.validateRelations(Collections.singletonList(res3), report);
        assertTrue(validation.isEmpty());

        validation = cms.validateRelations(Collections.singletonList(res4), report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename4)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename4));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res4, res2, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res4, res2, CmsRelationType.XML_WEAK)));
        }

        validation = cms.validateRelations(Collections.singletonList(res5), report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename5)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename5));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res5, res6, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res5, res6, CmsRelationType.XML_WEAK)));
        }
        validation = cms.validateRelations(Collections.singletonList(res6), report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename6)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename6));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res6, res5, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res6, res5, CmsRelationType.XML_WEAK)));
        }

        validation = cms.validateRelations(Collections.singletonList(res7), report);
        assertTrue(validation.isEmpty());

        List res56 = new ArrayList();
        res56.add(res5);
        res56.add(res6);
        validation = cms.validateRelations(res56, report);
        assertTrue(validation.isEmpty());

        List res123 = new ArrayList();
        res123.add(res1);
        res123.add(res2);
        res123.add(res3);
        validation = cms.validateRelations(res123, report);
        assertTrue(validation.isEmpty());

        List resAll = new ArrayList();
        resAll.add(res1);
        resAll.add(res2);
        resAll.add(res3);
        resAll.add(res4);
        resAll.add(res5);
        resAll.add(res6);
        resAll.add(res7);
        validation = cms.validateRelations(resAll, report);
        assertTrue(validation.isEmpty());

        // publish
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject(report);
        OpenCms.getPublishManager().waitWhileRunning();

        // check links after deletion
        touchResources(cms, resAll);

        validation = validateAfterDelete(cms, Collections.singletonList(res1), resAll, report);
        assertTrue(validation.isEmpty());

        validation = validateAfterDelete(cms, Collections.singletonList(res2), resAll, report);
        assertEquals(validation.size(), 2);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename1)));
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename4)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename1));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res1, res2, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res1, res2, CmsRelationType.XML_WEAK)));
        }
        
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename4));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res4, res2, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res4, res2, CmsRelationType.XML_WEAK)));
        }

        validation = validateAfterDelete(cms, Collections.singletonList(res3), resAll, report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename1)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename1));
        assertEquals(brokenLinks.size(), 1);
        assertTrue(brokenLinks.contains(new CmsRelation(res1, res3, relType1)));

        validation = validateAfterDelete(cms, Collections.singletonList(res4), resAll, report);
        assertTrue(validation.isEmpty());

        validation = validateAfterDelete(cms, Collections.singletonList(res5), resAll, report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename6)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename6));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res6, res5, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res6, res5, CmsRelationType.XML_WEAK)));
        }

        validation = validateAfterDelete(cms, Collections.singletonList(res6), resAll, report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename5)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename5));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res5, res6, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res5, res6, CmsRelationType.XML_WEAK)));
        }

        validation = validateAfterDelete(cms, Collections.singletonList(res7), resAll, report);
        assertTrue(validation.isEmpty());
        validation = validateAfterDelete(cms, res56, resAll, report);
        assertTrue(validation.isEmpty());

        // check links after modification

        // Publishing after deleting file5 and changing the link 
        // from file6 on file5 to file7 must generate no errors
        delete(cms, filename5, report);
        switch (mode) {
            case 1:
                setXmlContent(cms, filename6, "<a href='" + filename7 + "'>file7</a>", filename7);
                break;
            case 2:
                setXmlContentHtml(cms, filename6, "<a href='" + filename7 + "'>file7</a>");
                break;
            case 3:
                setXmlContentFileRef(cms, filename6, filename7, null);
                break;
            default:
                setContent(cms, filename6, "<a href='" + filename7 + "'>file7</a>");
                break;

        }
        List resources = new ArrayList(resAll);
        resources.remove(res5);
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        validation = cms.validateRelations(resources, report);
        assertTrue(validation.isEmpty());
        switch (mode) {
            case 1:
                setXmlContent(cms, filename6, content6, filename5);
                break;
            case 2:
                setXmlContentHtml(cms, filename6, content6);
                break;
            case 3:
                setXmlContentFileRef(cms, filename6, filename5, null);
                break;
            default:
                setContent(cms, filename6, content6);
                break;

        }
        restore(cms, filename5, report);

        // Publishing after deleting file2 and changing the link from file4 on file2 to file7 
        // and removing the link from file1 on file2 must generate no errors 
        delete(cms, filename2, report);
        switch (mode) {
            case 1:
                setXmlContent(cms, filename1, "no link!", null);
                setXmlContent(cms, filename4, "<a href='" + filename7 + "'>file7</a>", filename7);
                break;
            case 2:
                setXmlContentHtml(cms, filename1, "no link!");
                setXmlContentHtml(cms, filename4, "<a href='" + filename7 + "'>file7</a>");
                break;
            case 3:
                setXmlContentFileRef(cms, filename1, null, null);
                setXmlContentFileRef(cms, filename4, filename7, null);
                break;
            default:
                setContent(cms, filename1, "no link!");
                setContent(cms, filename4, "<a href='" + filename7 + "'>file7</a>");
                break;

        }
        resources = new ArrayList(resAll);
        resources.remove(res2);
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        validation = cms.validateRelations(resources, report);
        assertTrue(validation.isEmpty());
        switch (mode) {
            case 1:
                setXmlContent(cms, filename1, content1, filename2);
                setXmlContent(cms, filename4, content4, filename2);
                break;
            case 2:
                setXmlContentHtml(cms, filename1, content1);
                setXmlContentHtml(cms, filename4, content4);
                break;
            case 3:
                setXmlContentFileRef(cms, filename1, filename2, filename3);
                setXmlContentFileRef(cms, filename4, filename2, null);
                break;
            default:
                setContent(cms, filename1, content1);
                setContent(cms, filename4, content4);
                break;

        }
        restore(cms, filename2, report);

        // Publishing just file7 after creating a new file8 and creating a link 
        // from file7 to file8 must generate one error
        CmsResource res8 = cms.createResource(filename8, CmsResourceTypeXmlPage.getStaticTypeId());
        switch (mode) {
            case 1:
                setXmlContent(cms, filename7, "<a href='" + filename8 + "'>file8</a>", filename8);
                break;
            case 2:
                setXmlContentHtml(cms, filename7, "<a href='" + filename8 + "'>file8</a>");
                break;
            case 3:
                setXmlContentFileRef(cms, filename7, filename8, null);
                break;
            default:
                setContent(cms, filename7, "<a href='" + filename8 + "'>file8</a>");
                break;

        }
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        validation = cms.validateRelations(Collections.singletonList(res7), report);
        assertEquals(validation.size(), 1);
        assertTrue(validation.keySet().contains(cms.getRequestContext().addSiteRoot(filename7)));
        brokenLinks = (List)validation.get(cms.getRequestContext().addSiteRoot(filename7));
        assertEquals(brokenLinks.size(), (mode == MODE_XMLCONTENT_BOTH ? 2 : 1));
        assertTrue(brokenLinks.contains(new CmsRelation(res7, res8, relType1)));
        if (mode == MODE_XMLCONTENT_BOTH) {
            assertTrue(brokenLinks.contains(new CmsRelation(res7, res8, CmsRelationType.XML_WEAK)));
        }

        // Publishing after creating a new file8 and creating a link 
        // from file7 to file8 must generate no errors
        List res78 = new ArrayList();
        res78.add(res7);
        res78.add(res8);
        validation = cms.validateRelations(res78, report);
        assertTrue(validation.isEmpty());
    }

    /**
     * Touch all resources in the given list.<p>
     * 
     * @param cms the cms context
     * @param resources the resources to touch
     * @throws CmsException if something goes wrong
     */
    private void touchResources(CmsObject cms, List resources) throws CmsException {

        Iterator it = resources.iterator();
        while (it.hasNext()) {
            CmsResource resource = (CmsResource)it.next();
            String resName = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
            cms.lockResource(resName);
            cms.setDateLastModified(resName, System.currentTimeMillis(), false);
        }
    }

    /**
     * Validates the links after the deletion of all the given resources.<p>
     * 
     * @param cms the cms context
     * @param resources the resources to delete
     * @param allResources the resource context
     * @param report the report
     * 
     * @return the validation map
     * 
     * @throws Exception if something goes wrong
     */
    private Map validateAfterDelete(CmsObject cms, List resources, List allResources, CmsShellReport report)
    throws Exception {

        List otherRes = new ArrayList(allResources);
        Iterator itRes = resources.iterator();
        while (itRes.hasNext()) {
            CmsResource resource = (CmsResource)itRes.next();
            String resName = cms.getRequestContext().getSitePath(resource);
            delete(cms, resName, report);
            otherRes.remove(resource);
        }
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        Map validation = cms.validateRelations(otherRes, report);
        itRes = resources.iterator();
        while (itRes.hasNext()) {
            CmsResource resource = (CmsResource)itRes.next();
            String resName = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
            restore(cms, resName, report);
        }
        touchResources(cms, resources);
        return validation;
    }
}
