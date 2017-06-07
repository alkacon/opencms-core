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

package org.opencms.relations;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.test.I_CmsLogHandler;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import org.apache.log4j.spi.LoggingEvent;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Tests for the memory monitor.<p>
 *
 * @since 6.0.0
 */
public class TestCategories extends OpenCmsTestCase {

    /** Stores an error. */
    private static String m_storedError;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCategories(String arg0) {

        super(arg0);
    }

    /**
     * Stores an error message which will cause the test to fail.<p>
     *
     * @param error the error message
     */
    public static void storeError(String error) {

        m_storedError = error;
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCategories.class.getName());

        // the order is important
        suite.addTest(new TestCategories("testCategoryTree"));
        suite.addTest(new TestCategories("testCategoryTreeAssign"));
        suite.addTest(new TestCategories("testCategoryBaseFolder"));
        suite.addTest(new TestCategories("testCategoryBaseFolderRepair"));
        suite.addTest(new TestCategories("testCategoryBaseFolderAssign"));
        suite.addTest(new TestCategories("testCategoryConflict"));
        suite.addTest(new TestCategories("testCategoryConflictAssign"));
        suite.addTest(new TestCategories("testCategoryConflictRepair"));
        suite.addTest(new TestCategories("testCopyValid"));
        // TODO: some more test cases, copyInvalid, moveValid, moveInvalid
        suite.addTest(new TestCategories("testPublishMovedResourceWithCategories1"));
        suite.addTest(new TestCategories("testPublishMovedResourceWithCategories2"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * @see junit.framework.TestCase#run(junit.framework.TestResult)
     */
    @Override
    public void run(TestResult result) {

        super.run(result);
        // We do this to fail the test if there is an error log message inside a different thread
        if (m_storedError != null) {
            String error = m_storedError;
            m_storedError = null;
            result.addError(this, new RuntimeException(error));
        }
    }

    /**
     * Tests changing the base folder name of the category repositories.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryBaseFolder() throws Exception {

        System.out.println("Testing changing the base folder name of the category repositories.");
        CmsObject cms = getCmsObject();

        // assert the starting situation
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catA = cats.get(0);
        CmsCategory catAA = cats.get(1);
        CmsCategory catB = cats.get(2);
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/", catA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/aa/", catAA.getRootPath());
        assertEquals(
            cms.getRequestContext().getSiteRoot()
                + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms)
                + "b/",
            catB.getRootPath());

        // change the category repositories base folder name
        // this will invalidate all local categories
        cms.writePropertyObject(
            CmsCategoryService.CENTRALIZED_REPOSITORY,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_DEFAULT_FILE, "mycats", null));

        // assert the category list
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catAA, cats.get(1));

        // create a new local category repository
        cms.createResource(
            CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms),
            CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // create a new category in the local repository
        CmsCategory catC = CmsCategoryService.getInstance().createCategory(cms, null, "c", "C", "C test", "index.html");
        // assert the new created category
        assertEquals("c/", catC.getPath());
        assertEquals("C", catC.getTitle());
        assertEquals("C test", catC.getDescription());
        assertEquals(
            cms.getRequestContext().getSiteRoot() + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms),
            catC.getBasePath());

        // assert the created resource
        CmsResource resC = cms.readResource(cms.getRequestContext().removeSiteRoot(catC.getRootPath()));
        assertEquals(catC.getId(), resC.getStructureId());
        assertEquals(catC.getRootPath(), resC.getRootPath());
        assertEquals(
            catC.getTitle(),
            cms.readPropertyObject(resC, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue());
        assertEquals(
            catC.getDescription(),
            cms.readPropertyObject(resC, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());

        // assert the category list
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catAA, cats.get(1));
        assertEquals(catC, cats.get(2));

    }

    /**
     * Tests category assignment when changing the base folder name of the category repositories.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryBaseFolderAssign() throws Exception {

        System.out.println(
            "Testing category assignment when changing the base folder name of the category repositories.");
        CmsObject cms = getCmsObject();

        // assert the starting situation of categories coming from #testCategoryBaseFolderRepair
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catA = cats.get(0);
        CmsCategory catAA = cats.get(1);
        CmsCategory catC = cats.get(2);
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/", catA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/aa/", catAA.getRootPath());
        assertEquals(
            cms.getRequestContext().getSiteRoot()
                + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms)
                + "c/",
            catC.getRootPath());

        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(1, cats.size());
        assertEquals(catA, cats.get(0));

        List<CmsResource> resources = CmsCategoryService.getInstance().readCategoryResources(
            cms,
            catA.getPath(),
            true,
            "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catC.getPath(), true, "index.html").isEmpty());

        // assign a local category from the new repository
        CmsCategoryService.getInstance().addResourceToCategory(cms, "index.html", catC.getPath());

        // assert resource categories after assignment
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catC, cats.get(1));

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catC.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // remove local category from the new repository
        CmsCategoryService.getInstance().removeResourceFromCategory(cms, "index.html", catC.getPath());

        // assert resource categories after removing
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(1, cats.size());
        assertEquals(catA, cats.get(0));

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catC.getPath(), true, "index.html").isEmpty());

        // change the category repositories base folder name back to the default
        // this will invalidate all local categories
        cms.writePropertyObject(
            CmsCategoryService.CENTRALIZED_REPOSITORY,
            new CmsProperty(
                CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                CmsCategoryService.REPOSITORY_BASE_FOLDER,
                null));

        CmsCategory catB = CmsCategoryService.getInstance().getCategory(
            cms,
            cms.readResource(CmsCategoryService.REPOSITORY_BASE_FOLDER + "b"));

        // assert the category list
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catAA, cats.get(1));
        assertEquals(catB, cats.get(2));
    }

    /**
     * Tests resource categories reparation after changing the base folder name of the category repositories.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryBaseFolderRepair() throws Exception {

        System.out.println(
            "Testing resource categories reparation after changing the base folder name of the category repositories.");
        CmsObject cms = getCmsObject();

        // assert the starting situation of categories coming from #testCategoryBaseFolder
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catA = cats.get(0);
        CmsCategory catAA = cats.get(1);
        CmsCategory catC = cats.get(2);
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/", catA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/aa/", catAA.getRootPath());
        assertEquals(
            cms.getRequestContext().getSiteRoot()
                + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms)
                + "c/",
            catC.getRootPath());

        printExceptionWarning();
        CmsResource resB = cms.readResource(CmsCategoryService.REPOSITORY_BASE_FOLDER + "b");
        try {
            CmsCategoryService.getInstance().getCategory(cms, resB);
            fail("Category B should be invalid");
        } catch (Exception e) {
            // ok, continue
        }

        // assert the starting situation of resources coming from #testCategoryTreeAssign
        OpenCmsTestLogAppender.setBreakOnError(false); // to prevent failure because of error log output
        echo("next error stack trace is expected.");
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        OpenCmsTestLogAppender.setBreakOnError(true);
        assertEquals(1, cats.size()); // catB is lost (as category, but still there as relation)
        assertEquals(catA, cats.get(0));

        List<CmsResource> resources = CmsCategoryService.getInstance().readCategoryResources(
            cms,
            catA.getPath(),
            true,
            "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catC.getPath(), true, "index.html").isEmpty());

        // now assert the relations
        List<CmsRelation> relations = cms.getRelationsForResource(
            "index.html",
            CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY));
        assertEquals(2, relations.size());
        CmsResource resource = cms.readResource("index.html");
        CmsResource resCatB = cms.readResource(".categories/b/");
        assertTrue(
            relations.contains(new CmsRelation(
                resource.getStructureId(),
                resource.getRootPath(),
                catA.getId(),
                catA.getRootPath(),
                CmsRelationType.CATEGORY)));
        assertTrue(
            relations.contains(new CmsRelation(
                resource.getStructureId(),
                resource.getRootPath(),
                resCatB.getStructureId(),
                resCatB.getRootPath(),
                CmsRelationType.CATEGORY)));

        // repair category associations
        CmsCategoryService.getInstance().repairRelations(cms, "index.html");

        // assert after reparation
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(1, cats.size());
        assertEquals(catA, cats.get(0));

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catC.getPath(), true, "index.html").isEmpty());

        // now assert the relations
        relations = cms.getRelationsForResource(
            "index.html",
            CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY));
        assertEquals(1, relations.size());
        resource = cms.readResource("index.html");
        assertTrue(
            relations.contains(new CmsRelation(
                resource.getStructureId(),
                resource.getRootPath(),
                catA.getId(),
                catA.getRootPath(),
                CmsRelationType.CATEGORY)));
    }

    /**
     * Tests the categories when several repositories define the same category.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryConflict() throws Exception {

        System.out.println("Testing the categories when several repositories define the same category.");
        CmsObject cms = getCmsObject();

        // assert the starting situation
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catA = cats.get(0);
        CmsCategory catAA = cats.get(1);
        CmsCategory catB = cats.get(2);
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/", catA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/aa/", catAA.getRootPath());
        assertEquals(
            cms.getRequestContext().getSiteRoot()
                + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms)
                + "b/",
            catB.getRootPath());

        // create a new category in the centralized repository, with the same name as a local category
        CmsCategory catB2 = CmsCategoryService.getInstance().createCategory(cms, null, "b", "B2", "B2 test", null);
        // assert the new created category
        assertEquals("b/", catB2.getPath());
        assertEquals("B2", catB2.getTitle());
        assertEquals("B2 test", catB2.getDescription());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY, catB2.getBasePath());

        // assert the created resource
        CmsResource resB2 = cms.readResource(cms.getRequestContext().removeSiteRoot(catB2.getRootPath()));
        assertEquals(catB2.getId(), resB2.getStructureId());
        assertEquals(catB2.getRootPath(), resB2.getRootPath());
        assertEquals(
            catB2.getTitle(),
            cms.readPropertyObject(resB2, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue());
        assertEquals(
            catB2.getDescription(),
            cms.readPropertyObject(resB2, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());

        // assert the category list
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catAA, cats.get(1));
        assertEquals(catB2, cats.get(2));
    }

    /**
     * Tests the categories assignment when several repositories define the same category.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryConflictAssign() throws Exception {

        System.out.println("Testing the categories assignment when several repositories define the same category.");
        CmsObject cms = getCmsObject();

        // assert the starting situation
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catA = cats.get(0);
        CmsCategory catAA = cats.get(1);
        CmsCategory catB2 = cats.get(2);
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/", catA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/aa/", catAA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "b/", catB2.getRootPath());

        // assign a centralized category
        CmsCategoryService.getInstance().addResourceToCategory(cms, "index.html", catB2.getPath());

        // assert resource categories after assignment
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catB2, cats.get(1));

        List<CmsResource> resources = CmsCategoryService.getInstance().readCategoryResources(
            cms,
            catA.getPath(),
            true,
            "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB2.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
    }

    /**
     * Tests resource categories reparation after deleting/adding a centralized category.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryConflictRepair() throws Exception {

        System.out.println("Testing resource categories reparation after deleting/adding a centralized category.");
        CmsObject cms = getCmsObject();

        // assert the starting situation
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catA = cats.get(0);
        CmsCategory catAA = cats.get(1);
        CmsCategory catB2 = cats.get(2);
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/", catA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/aa/", catAA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "b/", catB2.getRootPath());

        // assert resource categories
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catB2, cats.get(1));

        List<CmsResource> resources = CmsCategoryService.getInstance().readCategoryResources(
            cms,
            catA.getPath(),
            true,
            "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB2.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // delete category folder
        cms.deleteResource(catB2.getRootPath(), CmsResource.DELETE_PRESERVE_SIBLINGS);

        // assert categories after deletion
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catB = cats.get(2);
        assertEquals(catA.getRootPath(), cats.get(0).getRootPath());
        assertEquals(catAA.getRootPath(), cats.get(1).getRootPath());
        assertEquals(
            cms.getRequestContext().getSiteRoot()
                + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms)
                + "b/",
            catB.getRootPath());

        // assert resource categories after category folder deletion
        OpenCmsTestLogAppender.setBreakOnError(false); // next call will write the error log

        printExceptionWarning();
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        OpenCmsTestLogAppender.setBreakOnError(true);
        assertEquals(1, cats.size());
        assertEquals(catA, cats.get(0));

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html").isEmpty());

        // now repair
        CmsCategoryService.getInstance().repairRelations(cms, "index.html");

        // assert again
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catB, cats.get(1));
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // recreate global category
        CmsCategory catB2bis = CmsCategoryService.getInstance().createCategory(cms, null, "b", "B2", "B2 test", null);
        assertEquals(catB2.getRootPath(), catB2bis.getRootPath());

        // assert categories after recreation
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        assertEquals(catA.getRootPath(), cats.get(0).getRootPath());
        assertEquals(catAA.getRootPath(), cats.get(1).getRootPath());
        assertEquals(catB2.getRootPath(), cats.get(2).getRootPath());

        // assert resource categories after category folder recreation
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA.getRootPath(), cats.get(0).getRootPath());
        assertEquals(catB.getRootPath(), cats.get(1).getRootPath()); // this has to be repaired

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        // this is also inconsistent
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catB2.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html").isEmpty());

        // now repair
        CmsCategoryService.getInstance().repairRelations(cms, "index.html");

        // assert again
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA.getRootPath(), cats.get(0).getRootPath());
        assertEquals(catB2.getRootPath(), cats.get(1).getRootPath());

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB2.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
    }

    /**
     * Tests the category tree access with different repositories.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryTree() throws Exception {

        System.out.println("Testing the category tree access with different repositories.");
        CmsObject cms = getCmsObject();

        // create the centralized repository
        cms.createResource(CmsCategoryService.CENTRALIZED_REPOSITORY, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // assert the starting situation
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertTrue(cats.isEmpty());

        // create a new category in the centralized repository
        CmsCategory catA = CmsCategoryService.getInstance().createCategory(cms, null, "a", "A", "A test", null);
        // assert the new created category
        assertEquals("a/", catA.getPath());
        assertEquals("A", catA.getTitle());
        assertEquals("A test", catA.getDescription());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY, catA.getBasePath());

        // assert the created resource
        CmsResource resA = cms.readResource(cms.getRequestContext().removeSiteRoot(catA.getRootPath()));
        assertEquals(catA.getId(), resA.getStructureId());
        assertEquals(catA.getRootPath(), resA.getRootPath());
        assertEquals(
            catA.getTitle(),
            cms.readPropertyObject(resA, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue());
        assertEquals(
            catA.getDescription(),
            cms.readPropertyObject(resA, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());

        // assert the category list
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(1, cats.size());
        assertEquals(catA, cats.get(0));

        // create a new local category repository
        cms.createResource(
            CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms),
            CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // create a new category in the local repository
        CmsCategory catB = CmsCategoryService.getInstance().createCategory(cms, null, "b", "B", "B test", "index.html");
        // assert the new created category
        assertEquals("b/", catB.getPath());
        assertEquals("B", catB.getTitle());
        assertEquals("B test", catB.getDescription());
        assertEquals(
            cms.getRequestContext().getSiteRoot() + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms),
            catB.getBasePath());

        // assert the created resource
        CmsResource resB = cms.readResource(cms.getRequestContext().removeSiteRoot(catB.getRootPath()));
        assertEquals(catB.getId(), resB.getStructureId());
        assertEquals(catB.getRootPath(), resB.getRootPath());
        assertEquals(
            catB.getTitle(),
            cms.readPropertyObject(resB, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue());
        assertEquals(
            catB.getDescription(),
            cms.readPropertyObject(resB, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());

        // assert comparison
        assertTrue(catA.compareTo(catA) == 0);
        assertTrue(catB.compareTo(catB) == 0);
        assertTrue(catA.compareTo(catB) < 0);
        assertTrue(catB.compareTo(catA) > 0);

        // assert the category list
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catB, cats.get(1));

        // create a new sub category in the centralized repository
        CmsCategory catAA = CmsCategoryService.getInstance().createCategory(cms, catA, "aa", "AA", "AA test", null);
        // assert the new created category
        assertEquals("a/aa/", catAA.getPath());
        assertEquals("AA", catAA.getTitle());
        assertEquals("AA test", catAA.getDescription());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY, catAA.getBasePath());

        // assert the created resource
        CmsResource resAA = cms.readResource(cms.getRequestContext().removeSiteRoot(catAA.getRootPath()));
        assertEquals(catAA.getId(), resAA.getStructureId());
        assertEquals(catAA.getRootPath(), resAA.getRootPath());
        assertEquals(
            catAA.getTitle(),
            cms.readPropertyObject(resAA, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue());
        assertEquals(
            catAA.getDescription(),
            cms.readPropertyObject(resAA, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());

        // assert the category list
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catAA, cats.get(1));
        assertEquals(catB, cats.get(2));

        // assert the category list with a different reference path
        cms.getRequestContext().setSiteRoot("");
        cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catAA, cats.get(1));
    }

    /**
     * Tests the category assignment with different repositories.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCategoryTreeAssign() throws Exception {

        System.out.println("Testing the category assignment with different repositories.");
        CmsObject cms = getCmsObject();

        // assert the starting situation
        List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(cms, null, true, "index.html");
        assertEquals(3, cats.size());
        CmsCategory catA = cats.get(0);
        CmsCategory catAA = cats.get(1);
        CmsCategory catB = cats.get(2);
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/", catA.getRootPath());
        assertEquals(CmsCategoryService.CENTRALIZED_REPOSITORY + "a/aa/", catAA.getRootPath());
        assertEquals(
            cms.getRequestContext().getSiteRoot()
                + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms)
                + "b/",
            catB.getRootPath());

        // assert resource categories before assignment
        assertTrue(CmsCategoryService.getInstance().readResourceCategories(cms, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html").isEmpty());

        // assign resource to a first level category
        cms.lockResource("index.html");
        CmsCategoryService.getInstance().addResourceToCategory(cms, "index.html", catB);

        // assert resource categories after assignment
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(1, cats.size());
        assertEquals(catB, cats.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        List<CmsResource> resources = CmsCategoryService.getInstance().readCategoryResources(
            cms,
            catB.getPath(),
            true,
            "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // assign resource to the same first level category, should not change anything
        CmsCategoryService.getInstance().addResourceToCategory(cms, "index.html", catB);

        // assert resource categories after assignment
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(1, cats.size());
        assertEquals(catB, cats.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // assign resource to a deeper level category
        CmsCategoryService.getInstance().addResourceToCategory(cms, "index.html", catAA);

        // assert resource categories after assignment
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(3, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catAA, cats.get(1));
        assertEquals(catB, cats.get(2));

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // remove a deeper category
        CmsCategoryService.getInstance().removeResourceFromCategory(cms, "index.html", catAA);

        // assert resource categories after removing
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catB, cats.get(1));

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // remove a first level category
        CmsCategoryService.getInstance().removeResourceFromCategory(cms, "index.html", catA);

        // assert resource categories after assignment
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(1, cats.size());
        assertEquals(catB, cats.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html").isEmpty());
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));

        // assign resource to a category with sub categories
        CmsCategoryService.getInstance().addResourceToCategory(cms, "index.html", catA);

        // assert resource categories after assignment
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "index.html");
        assertEquals(2, cats.size());
        assertEquals(catA, cats.get(0));
        assertEquals(catB, cats.get(1));

        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catA.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, catAA.getPath(), true, "index.html").isEmpty());
        resources = CmsCategoryService.getInstance().readCategoryResources(cms, catB.getPath(), true, "index.html");
        assertEquals(1, resources.size());
        assertEquals(cms.readResource("index.html"), resources.get(0));
    }

    /**
     * Tests copying a file with assigned categories across different category contexts when the categories remain valid.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCopyValid() throws Exception {

        System.out.println(
            "Testing copying a file with assigned categories across different category contexts when the categories remain valid.");
        CmsObject cms = getCmsObject();

        // create starting situation
        cms.createResource(
            "/folder1" + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms),
            CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        CmsCategory d1 = CmsCategoryService.getInstance().createCategory(
            cms,
            null,
            "d",
            "D1",
            "D1 Test",
            "/folder1/index.html");
        cms.createResource(
            "/folder2" + CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms),
            CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        CmsCategory d2 = CmsCategoryService.getInstance().createCategory(
            cms,
            null,
            "d",
            "D2",
            "D2 Test",
            "/folder2/index.html");

        // assign category
        cms.lockResource("/folder1/index.html");
        CmsCategoryService.getInstance().addResourceToCategory(cms, "/folder1/index.html", d1.getPath());

        // assert assignment
        List<CmsCategory> cats = CmsCategoryService.getInstance().readResourceCategories(cms, "/folder1/index.html");
        assertEquals(1, cats.size());
        assertEquals(d1.getRootPath(), cats.get(0).getRootPath());

        // copy file
        cms.copyResource("/folder1/index.html", "/folder2/index2.html");

        // assert categories after copying
        cats = CmsCategoryService.getInstance().readResourceCategories(cms, "/folder2/index2.html");
        assertEquals(1, cats.size());
        assertEquals(d2.getRootPath(), cats.get(0).getRootPath());
    }

    /**
     * Tests the case where a moved resource is published, but its category has been deleted and re-created in a different repository.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testPublishMovedResourceWithCategories1() throws Exception {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        int typePlain = CmsResourceTypePlain.getStaticTypeId();
        int typeFolder = CmsResourceTypeFolder.getStaticTypeId();
        try {
            OpenCmsTestLogAppender.setBreakOnError(true);
            OpenCmsTestLogAppender.setHandler(new I_CmsLogHandler() {

                public void handleLogEvent(LoggingEvent event) {

                    if (event.getLevel().toString().contains("ERROR")) {
                        // TODO: This may be an error unrelated to the original error. Need more precise check.
                        storeError(event.getMessage().toString());
                    }
                }
            });
            cms.createResource("/publishRepair1", typeFolder);
            String baseCategories = "/publishRepair1" + "/" + ".categories";
            cms.createResource(baseCategories, typeFolder);
            cms.createResource(baseCategories + "/" + "publishRepair_cat1", typeFolder);
            String filePath = "/publishRepair1" + "/testpublishrepair.txt";
            cms.createResource(filePath, typePlain);
            CmsCategoryService service = CmsCategoryService.getInstance();
            service.addResourceToCategory(cms, filePath, "/" + "publishRepair_cat1");
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
            cms.lockResource(filePath);
            cms.moveResource(filePath, filePath + "_x");
            cms.lockResource(baseCategories + "/" + "publishRepair_cat1");
            cms.deleteResource(baseCategories + "/" + "publishRepair_cat1", CmsResource.DELETE_PRESERVE_SIBLINGS);
            if (true) {
                cms.createResource("/system/categories/" + "publishRepair_cat1", typeFolder);
            }
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCmsTestLogAppender.setHandler(null);
        }

    }

    /**
     * Tests the case where a moved resource is published, but its category has been deleted.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testPublishMovedResourceWithCategories2() throws Exception {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        int typePlain = CmsResourceTypePlain.getStaticTypeId();
        int typeFolder = CmsResourceTypeFolder.getStaticTypeId();
        try {
            OpenCmsTestLogAppender.setBreakOnError(true);
            OpenCmsTestLogAppender.setHandler(new I_CmsLogHandler() {

                public void handleLogEvent(LoggingEvent event) {

                    if (event.getLevel().toString().contains("ERROR")) {
                        storeError(event.getMessage().toString());
                    }
                }
            });
            cms.createResource("/publishRepair2", typeFolder);
            String baseCategories = "/publishRepair2" + "/" + ".categories";
            cms.createResource(baseCategories, typeFolder);
            cms.createResource(baseCategories + "/" + "publishRepair_cat2", typeFolder);
            String filePath = "/publishRepair2" + "/testpublishrepair.txt";
            cms.createResource(filePath, typePlain);
            CmsCategoryService service = CmsCategoryService.getInstance();
            service.addResourceToCategory(cms, filePath, "/" + "publishRepair_cat2");
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
            cms.lockResource(baseCategories + "/" + "publishRepair_cat2");
            cms.deleteResource(baseCategories + "/" + "publishRepair_cat2", CmsResource.DELETE_PRESERVE_SIBLINGS);
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
            cms.lockResource(filePath);
            cms.moveResource(filePath, filePath + "_x");
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCmsTestLogAppender.setHandler(null);
        }
    }

}
