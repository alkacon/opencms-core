/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestSiblings.java,v $
 * Date   : $Date: 2006/08/19 13:40:37 $
 * Version: $Revision: 1.17.8.2 $
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

import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.util.CmsResourceTranslator;

import java.util.Collections;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for operations on siblings.<p>
 * 
 * @author Thomas Weckert  
 * @version $Revision: 1.17.8.2 $
 */
public class TestSiblings extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSiblings(String arg0) {

        super(arg0);
    }

    /**
     * Creates a copy of a resource as a new sibling.<p>
     * 
     * @param tc the OpenCms test case
     * @param cms the current user's Cms object
     * @param source path/resource name of the existing resource 
     * @param target path/resource name of the new sibling
     * @throws Throwable if something goes wrong
     */
    public static void copyResourceAsSibling(OpenCmsTestCase tc, CmsObject cms, String source, String target)
    throws Throwable {

        // save the source in the store
        tc.storeResources(cms, source);

        // copy source to target as a sibling, the new sibling should not be locked
        cms.copyResource(source, target, CmsResource.COPY_AS_SIBLING);

        // validate the source sibling

        // validate if all unmodified fields on the source resource are still equal
        tc.assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);
        // validate if the last-modified-in-project field is the current project
        tc.assertProject(cms, source, cms.getRequestContext().currentProject());
        // validate if the sibling count field has been incremented
        tc.assertSiblingCountIncremented(cms, source, 1);
        // validate if the sibling does not have a red flag
        tc.assertModifiedInCurrentProject(cms, source, false);
        // validate if the lock is an exclusive shared lock for the current user
        tc.assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);

        // validate the target sibling

        // validate the fields that both in the existing and the new sibling have to be equal
        tc.assertFilter(cms, source, target, OpenCmsTestResourceFilter.FILTER_EXISTING_AND_NEW_SIBLING);
        // validate if the state of the new sibling is "new" (blue)
        tc.assertState(cms, target, CmsResource.STATE_NEW);
        // validate if the new sibling has a red flag
        tc.assertModifiedInCurrentProject(cms, target, true);
        // validate if the lock is an exclusive lock for the current user
        tc.assertLock(cms, target, CmsLockType.EXCLUSIVE);
    }

    /**
     * Creates a new sibling of a resource.<p>
     * 
     * @param tc the OpenCms test case
     * @param cms the current user's Cms object
     * @param source path/resource name of the existing resource 
     * @param target path/resource name of the new sibling
     * @throws Throwable if something goes wrong
     */
    public static void createSibling(OpenCmsTestCase tc, CmsObject cms, String source, String target) throws Throwable {

        // save the source in the store
        tc.storeResources(cms, source);

        // create a new sibling from the source
        List properties = cms.readPropertyObjects(source, false);
        cms.createSibling(source, target, properties);

        // validate the source sibling

        // validate if all unmodified fields of the source are still equal
        tc.assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);
        // validate if the last-modified-in-project field is the current project
        tc.assertProject(cms, source, cms.getRequestContext().currentProject());
        // validate if the sibling count field has been incremented
        tc.assertSiblingCountIncremented(cms, source, 1);
        // validate if the sibling does not have a red flag
        tc.assertModifiedInCurrentProject(cms, source, false);
        // validate if the lock is an exclusive shared lock for the current user
        tc.assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);

        // validate the target sibling

        // validate the fields that both in the existing and the new sibling have to be equal
        tc.assertFilter(cms, source, target, OpenCmsTestResourceFilter.FILTER_EXISTING_AND_NEW_SIBLING);
        // validate if the state of the new sibling is "new" (blue)
        tc.assertState(cms, target, CmsResource.STATE_NEW);
        // validate if the new sibling has a red flag
        tc.assertModifiedInCurrentProject(cms, target, true);
        // validate if the lock is an exclusive lock for the current user
        tc.assertLock(cms, target, CmsLockType.EXCLUSIVE);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestSiblings.class.getName());

        suite.addTest(new TestSiblings("testSiblingsCopy"));
        suite.addTest(new TestSiblings("testSiblingsCreate"));
        suite.addTest(new TestSiblings("testSiblingIssueAfterImport"));
        suite.addTest(new TestSiblings("testDeleteAllSiblings"));
        suite.addTest(new TestSiblings("testSiblingStateIssue"));

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
     * Tests deletion of a resource together with all siblings.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testDeleteAllSiblings() throws Throwable {

        echo("Creating a new resource with 2 siblings, then deleting it with all siblings again");
        CmsObject cms = getCmsObject();

        String sib1Name = "/folder1/sib1.txt";
        String sib2Name = "/folder1/sib2.txt";
        String sib3Name = "/folder1/sib3.txt";

        cms.createResource(sib1Name, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(sib1Name, sib2Name, Collections.EMPTY_LIST);
        cms.createSibling(sib1Name, sib3Name, Collections.EMPTY_LIST);

        cms.deleteResource(sib1Name, CmsResource.DELETE_REMOVE_SIBLINGS);

        CmsResource sib2Resource = null;
        try {
            sib2Resource = cms.readResource(sib2Name);
        } catch (CmsVfsResourceNotFoundException e) {
            // intentionally left blank
        }

        if (sib2Resource != null) {
            fail("Sibling " + sib2Name + " has not been deleted!");
        }

        CmsResource sib3Resource = null;
        try {
            sib3Resource = cms.readResource(sib3Name);
        } catch (CmsVfsResourceNotFoundException e) {
            // intentionally left blank
        }

        if (sib3Resource != null) {
            fail("Sibling " + sib3Name + " has not been deleted!");
        }
    }

    /**
     * Error scenario where an import is deleted that contains siblings inside the same folders.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testSiblingIssueAfterImport() throws Exception {

        echo("Testing sibling issue after import");

        CmsResourceTranslator oldFolderTranslator = OpenCms.getResourceManager().getFolderTranslator();

        CmsResourceTranslator folderTranslator = new CmsResourceTranslator(new String[] {
            "s#^/sites/default/content/bodys(.*)#/system/bodies$1#",
            "s#^/sites/default/pics/system(.*)#/system/workplace/resources$1#",
            "s#^/sites/default/pics(.*)#/system/galleries/pics$1#",
            "s#^/sites/default/download(.*)#/system/galleries/download$1#",
            "s#^/sites/default/externallinks(.*)#/system/galleries/externallinks$1#",
            "s#^/sites/default/htmlgalleries(.*)#/system/galleries/htmlgalleries$1#",
            "s#^/sites/default/content(.*)#/system$1#"}, false);

        // set modified folder translator
        OpenCms.getResourceManager().setTranslators(folderTranslator, OpenCms.getResourceManager().getFileTranslator());

        try {

            CmsObject cms = getCmsObject();

            cms.getRequestContext().setSiteRoot("/");

            // need to create the "galleries" folder manually
            cms.createResource("/system/galleries", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
            cms.unlockResource("/system/galleries");

            cms.getRequestContext().setSiteRoot("/sites/default");

            // import the files
            String importFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testimport01.zip");
            OpenCms.getImportExportManager().importData(
                cms,
                importFile,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // clean up for the next test
            cms.getRequestContext().setSiteRoot("/");
            cms.lockResource("/sites/default");
            cms.lockResource("/system");

            // using the option "DELETE_REMOVE_SIBLINGS" caused an error here!
            cms.deleteResource("/sites/default/importtest", CmsResource.DELETE_REMOVE_SIBLINGS);
            cms.deleteResource("/system/galleries/pics", CmsResource.DELETE_REMOVE_SIBLINGS);

            cms.unlockResource("/sites/default");
            cms.unlockResource("/system");
            cms.publishProject();

        } finally {

            // reset the translation rules
            OpenCms.getResourceManager().setTranslators(
                oldFolderTranslator,
                OpenCms.getResourceManager().getFileTranslator());

        }
    }

    /**
     * Tests the "copy as new sibling" function.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsCopy() throws Throwable {

        CmsObject cms = getCmsObject();
        String source = "/index.html";
        String target = "/index_sibling.html";
        echo("Copying " + source + " as a new sibling to " + target);
        copyResourceAsSibling(this, cms, source, target);
    }

    /**
     * Does an "undo changes" from the online project on a resource with more than 1 sibling.<p>
     */
    /*
     public static void undoChangesWithSiblings(...) throws Throwable {
     // this test should do the following:
     // - create a sibling of a resource
     // - e.g. touch the black/unchanged sibling so that it gets red/changed
     // - make an "undo changes" -> the last-modified-in-project ID in the resource record 
     // of the resource must be the ID of the current project, and not 0
     // - this is to ensure that the new/changed/deleted other sibling still have a valid
     // state which consits of the last-modified-in-project ID plus the resource state
     // - otherwise this may result in grey flags

     Another issue:
     What happens if a user A has an exclusive lock on a resource X,
     and user B does a "copy as sibling Y" of X, or "create 
     new sibling Y" of X. The lock status of the resource X is exclusive
     to A, but test implies that it would be switched to B after operation!
     Maybe copy as / create new sibling must not be allowed if original is
     currently locked by another user? 

     }
     */

    /**
     * Tests the "copy as new sibling" function.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsCreate() throws Throwable {

        CmsObject cms = getCmsObject();
        String source = "/folder1/image1.gif";
        String target = "/folder1/image1_sibling.gif";
        echo("Creating a new sibling " + target + " from " + source);
        createSibling(this, cms, source, target);
    }

    /**
     * Tests if setting the flags of a sibling will do any modifications to other siblings.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testSiblingStateIssue() throws Throwable {

        echo("Tests issue with resource state and siblings");
        CmsObject cms = getCmsObject();

        // part 1: changes to the fields that are part of the resource table
        String resource1 = "/folder1/page1.html";
        String sibling1 = "/folder1/sibling1.html";

        // create a sibling 
        cms.copyResource(resource1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // now set the flags for the sibling
        cms.chflags(sibling1, 1024);
        cms.chtype(sibling1, CmsResourceTypeBinary.getStaticTypeId());

        // verify the state of the resources after the change
        assertState(cms, resource1, CmsResource.STATE_CHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // part 2: now the same operation with a new copy
        String copy1 = "/folder1/copy1.html";
        sibling1 = "/folder1/siblingofcopy1.html";

        // create a copy 
        cms.copyResource(resource1, copy1, CmsResource.COPY_AS_NEW);
        cms.copyResource(copy1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // now set the flags for the sibling
        cms.chflags(sibling1, 1024);
        cms.chtype(sibling1, CmsResourceTypeBinary.getStaticTypeId());

        // verify the state of the resources after the change
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // part 3: changes to the fields that are part of the structure table
        resource1 = "/folder1/page2.html";
        sibling1 = "/folder1/sibling2.html";

        // create a sibling 
        cms.copyResource(resource1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // after changes of dates the resource states must be the same        
        cms.setDateExpired(sibling1, System.currentTimeMillis() + 1000, false);
        cms.setDateReleased(sibling1, System.currentTimeMillis() - 1000, false);

        // verify the state of the resources after the change
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // step 4: changes to the fields that are part of the resource table
        cms.setDateLastModified(sibling1, System.currentTimeMillis(), false);

        // verify the state of the resources after the change
        assertState(cms, resource1, CmsResource.STATE_CHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // part 5: now the same operation with a new copy
        copy1 = "/folder1/copy2.html";
        sibling1 = "/folder1/siblingofcopy2.html";

        // create a copy
        cms.copyResource(resource1, copy1, CmsResource.COPY_AS_NEW);
        cms.copyResource(copy1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // change date of last modification
        cms.setDateLastModified(sibling1, System.currentTimeMillis(), false);

        // verify the state of the resources after the change
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // modifiy release info
        cms.setDateExpired(sibling1, System.currentTimeMillis() + 1000, false);
        cms.setDateReleased(sibling1, System.currentTimeMillis() - 1000, false);

        // verify the state of the resources after the change
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);
    }
}