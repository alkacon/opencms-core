/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestSiblings.java,v $
 * Date   : $Date: 2004/05/28 16:01:13 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.lock.CmsLock;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.List;

/**
 * Unit test for operations on siblings.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
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
     * @throws Throwable if something goes wrong
     */
    public void testSiblings() throws Throwable {

        String source = "/release/installation.html";
        String target1 = "/release/installation_sibling1.html";
        String target2 = "/release/installation_sibling2.html";
        
        CmsObject cms = setupOpenCms("simpletest", "/sites/default/");

        echo("Copying " + source + " as a new sibling to " + target1);
        copyResourceAsSibling(this, cms, source, target1);

        echo("Unlocking " + target1 + " for the next test");
        cms.unlockResource(target1, false);
        
        echo("Creating a new sibling " + target2 + " from " + source);
        createSibling(this, cms, source, target2);

        removeOpenCms();
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
        cms.copyResource(source, target, true, true, I_CmsConstants.C_COPY_AS_SIBLING);

        // validate the source sibling
        
        // validate if all unmodified fields on the source resource are still equal
        tc.assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);
        // validate if the last-modified-in-project field is the current project
        tc.assertProject(cms, source, cms.getRequestContext().currentProject());
        // validate if the sibling count field has been incremented
        tc.assertSiblingCountIncremented(cms, source);
        // validate if the sibling does not have a red flag
        tc.assertHasRedFlag(cms, source, false);
        // validate if the lock is an exclusive shared lock for the current user
        tc.assertLock(cms, source, CmsLock.C_TYPE_SHARED_EXCLUSIVE);

        // validate the target sibling
        
        // validate the fields that both in the existing and the new sibling have to be equal
        tc.assertFilter(cms, source, target, OpenCmsTestResourceFilter.FILTER_EXISTING_AND_NEW_SIBLING);
        // validate if the state of the new sibling is "new" (blue)
        tc.assertState(cms, target, I_CmsConstants.C_STATE_NEW);
        // validate if the new sibling has a red flag
        tc.assertHasRedFlag(cms, target, true);
        // validate if the lock is an exclusive lock for the current user
        tc.assertLock(cms, target, CmsLock.C_TYPE_EXCLUSIVE);        
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
        cms.createSibling(target, source, properties);

        // validate the source sibling
        
        // validate if all unmodified fields of the source are still equal
        tc.assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);        
        // validate if the last-modified-in-project field is the current project
        tc.assertProject(cms, source, cms.getRequestContext().currentProject());
        // validate if the sibling count field has been incremented
        tc.assertSiblingCountIncremented(cms, source);
        // validate if the sibling does not have a red flag
        tc.assertHasRedFlag(cms, source, false);
        // validate if the lock is an exclusive shared lock for the current user
        tc.assertLock(cms, source, CmsLock.C_TYPE_SHARED_EXCLUSIVE);        

        // validate the target sibling
        
        // validate the fields that both in the existing and the new sibling have to be equal
        tc.assertFilter(cms, source, target, OpenCmsTestResourceFilter.FILTER_EXISTING_AND_NEW_SIBLING);
        // validate if the state of the new sibling is "new" (blue)
        tc.assertState(cms, target, I_CmsConstants.C_STATE_NEW);
        // validate if the new sibling has a red flag
        tc.assertHasRedFlag(cms, target, true);
        // validate if the lock is an exclusive lock for the current user
        tc.assertLock(cms, target, CmsLock.C_TYPE_EXCLUSIVE);        
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
    }
    */

}