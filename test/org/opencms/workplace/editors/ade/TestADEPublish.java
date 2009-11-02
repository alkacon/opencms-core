/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/workplace/editors/ade/Attic/TestADEPublish.java,v $
 * Date   : $Date: 2009/11/02 10:08:21 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishEventAdapter;
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.publish.I_CmsPublishEventListener;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms ADE publish functions.<p>
 *
 * @author Michael Moossen
 *  
 * @version $Revision: 1.3 $
 */
public class TestADEPublish extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestADEPublish(String arg0) {

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
        suite.setName(TestADEPublish.class.getName());

        suite.addTest(new TestADEPublish("testAlreadyPublished"));
        suite.addTest(new TestADEPublish("testLocked"));
        suite.addTest(new TestADEPublish("testPermissions"));
        suite.addTest(new TestADEPublish("testProjects"));
        suite.addTest(new TestADEPublish("testPublishDeletedResource"));
        suite.addTest(new TestADEPublish("testPublishRestoredResource"));
        suite.addTest(new TestADEPublish("testPublishCrossProject"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests trying to publish an already published resource.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testAlreadyPublished() throws Exception {

        CmsObject cms = getCmsObject();

        CmsADEPublish adePub = new CmsADEPublish(cms);

        // check before
        List<CmsPublishGroupBean> groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // touch
        String resourcename = "/index.html";
        cms.lockResource(resourcename);
        cms.setDateLastModified(resourcename, System.currentTimeMillis(), false);
        cms.unlockResource(resourcename);
        CmsResource resource = cms.readResource(resourcename);

        // check after
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());

        // change user
        CmsProject prj = cms.getRequestContext().currentProject();
        String site = cms.getRequestContext().getSiteRoot();
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(prj);
        cms.getRequestContext().setSiteRoot(site);

        // check before
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // touch
        cms.lockResource(resourcename);
        cms.setDateLastModified(resourcename, System.currentTimeMillis(), false);
        cms.unlockResource(resourcename);

        // check after
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());

        // change back
        cms = getCmsObject();

        // check before
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());

        // publish
        adePub.publishResources(Collections.singletonList(resource));
        OpenCms.getPublishManager().waitWhileRunning();

        // check after
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // change user again
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(prj);
        cms.getRequestContext().setSiteRoot(site);

        // check it
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getResources().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertEquals(CmsPublishResourceInfoBean.Type.PUBLISHED, groups.get(0).getResources().get(0).getInfo().getType());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());

        // remove it from the publish list
        OpenCms.getPublishManager().removeResourceFromUsersPubList(
            cms,
            Collections.singletonList(resource.getStructureId()));

        // check again
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());
    }

    /**
     * Tests trying to publish a locked resource.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testLocked() throws Exception {

        CmsObject cms = getCmsObject();

        // check before
        CmsADEPublish adePub = new CmsADEPublish(cms);
        List<CmsPublishGroupBean> groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // touch
        String resourcename = "/index.html";
        cms.lockResource(resourcename);
        cms.setDateLastModified(resourcename, System.currentTimeMillis(), false);
        cms.unlockResource(resourcename);
        CmsResource resource = cms.readResource(resourcename);

        // check after
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());

        // change user
        CmsProject prj = cms.getRequestContext().currentProject();
        String site = cms.getRequestContext().getSiteRoot();
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(prj);
        cms.getRequestContext().setSiteRoot(site);

        // lock
        cms.lockResource(resourcename);

        // change back
        cms = getCmsObject();

        // check again
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getResources().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertEquals(CmsPublishResourceInfoBean.Type.LOCKED, groups.get(0).getResources().get(0).getInfo().getType());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());
    }

    /**
     * Tests trying to publish a resource without enough permissions.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testPermissions() throws Exception {

        CmsObject cms = getCmsObject();

        // change user
        CmsProject prj = cms.getRequestContext().currentProject();
        String site = cms.getRequestContext().getSiteRoot();
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(prj);
        cms.getRequestContext().setSiteRoot(site);

        // check before
        CmsADEPublish adePub = new CmsADEPublish(cms);
        List<CmsPublishGroupBean> groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // touch
        String resourcename = "/index.html";
        cms.lockResource(resourcename);
        cms.setDateLastModified(resourcename, System.currentTimeMillis(), false);
        cms.unlockResource(resourcename);
        CmsResource resource = cms.readResource(resourcename);

        // check after
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getResources().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertEquals(
            CmsPublishResourceInfoBean.Type.PERMISSIONS,
            groups.get(0).getResources().get(0).getInfo().getType());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());

        // give permissions
        cms = getCmsObject();
        cms.lockResource(resourcename);
        cms.chacc(resourcename, I_CmsPrincipal.PRINCIPAL_USER, "test1", "+d");
        cms.unlockResource(resourcename);

        // change back
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(prj);
        cms.getRequestContext().setSiteRoot(site);

        // try again
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getResources().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertNull(groups.get(0).getResources().get(0).getInfo());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());

        // publish
        adePub.publishResources(Collections.singletonList(resource));
        OpenCms.getPublishManager().waitWhileRunning();

        // check again
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // the resource was put to admin's publist when setting permissions
        cms = getCmsObject();
        OpenCms.getPublishManager().removeResourceFromUsersPubList(
            cms,
            Collections.singleton(resource.getStructureId()));
    }

    /**
     * Tests retrieving the manageable projects.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testProjects() throws Exception {

        CmsObject cms = getCmsObject();

        CmsADEPublish adePub = new CmsADEPublish(cms);

        // projects
        List<CmsProjectBean> projects = adePub.getManageableProjects();
        CmsProject offline = cms.readProject("Offline");
        assertEquals(1, projects.size());
        assertEquals(offline.getName(), projects.get(0).getName());
        assertEquals(offline.getUuid(), projects.get(0).getId());

        // create new project
        CmsProject newProject = cms.createProject("test", "test", "Users", "Users");
        adePub = new CmsADEPublish(cms);
        projects = adePub.getManageableProjects();
        assertEquals(2, projects.size());
        assertEquals(offline.getName(), projects.get(0).getName());
        assertEquals(offline.getUuid(), projects.get(0).getId());
        assertEquals(newProject.getName(), projects.get(1).getName());
        assertEquals(newProject.getUuid(), projects.get(1).getId());

        // change user
        cms.loginUser("test1", "test1");
        adePub = new CmsADEPublish(cms);
        projects = adePub.getManageableProjects();
        assertEquals(1, projects.size());
        assertEquals(newProject.getName(), projects.get(0).getName());
        assertEquals(newProject.getUuid(), projects.get(0).getId());
    }

    /**
     * Tests publishing a resources from different projects.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testPublishCrossProject() throws Exception {

        CmsObject cms = getCmsObject();

        CmsADEPublish adePub = new CmsADEPublish(cms);

        // first check when empty
        List<CmsPublishGroupBean> groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // touch resource
        String resourcename = "/folder1/page1.html";
        cms.lockResource(resourcename);
        cms.setDateReleased(resourcename, System.currentTimeMillis(), false);
        CmsResource resource = cms.readResource(resourcename, CmsResourceFilter.ALL);

        // check the publish list
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getResources().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertNull(groups.get(0).getResources().get(0).getInfo());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());

        // change project
        cms.getRequestContext().setCurrentProject(cms.readProject("test"));

        // add resource to test project
        String resourcename2 = "/folder1/page2.html";
        cms.copyResourceToProject(resourcename2);

        // touch 2nd resource
        cms.lockResource(resourcename2);
        cms.setDateReleased(resourcename2, System.currentTimeMillis(), false);
        CmsResource resource2 = cms.readResource(resourcename2, CmsResourceFilter.ALL);

        // check the publish list
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).getResources().size());
        assertEquals(resource2.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertNull(groups.get(0).getResources().get(0).getInfo());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(1).getId());
        assertTrue(groups.get(0).getResources().get(1).isRemovable());
        assertNull(groups.get(0).getResources().get(1).getInfo());
        assertEquals(0, groups.get(0).getResources().get(1).getRelated().size());

        // publish
        I_CmsPublishEventListener listener = new CmsPublishEventAdapter() {

            /**
             * @see org.opencms.publish.CmsPublishEventAdapter#onEnqueue(org.opencms.publish.CmsPublishJobBase)
             */
            @Override
            public void onEnqueue(CmsPublishJobBase publishJob) {

                assertEquals(2, publishJob.getSize());
            }

            /**
             * @see org.opencms.publish.CmsPublishEventAdapter#onFinish(org.opencms.publish.CmsPublishJobRunning)
             */
            @Override
            public void onFinish(CmsPublishJobRunning publishJob) {

                assertEquals(2, publishJob.getSize());
            }

            /**
             * @see org.opencms.publish.CmsPublishEventAdapter#onStart(org.opencms.publish.CmsPublishJobEnqueued)
             */
            @Override
            public void onStart(CmsPublishJobEnqueued publishJob) {

                assertEquals(2, publishJob.getSize());
            }
        };
        OpenCms.getPublishManager().addPublishListener(listener);
        List<CmsResource> resources = new ArrayList<CmsResource>();
        resources.add(resource);
        resources.add(resource2);
        adePub.publishResources(resources);
        OpenCms.getPublishManager().removePublishListener(listener);

        // check the publish list
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());
    }

    /**
     * Tests publishing a deleted resource.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testPublishDeletedResource() throws Exception {

        CmsObject cms = getCmsObject();

        CmsADEPublish adePub = new CmsADEPublish(cms);

        // first check when empty
        List<CmsPublishGroupBean> groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // delete linked resource
        String resourcename = "/folder1/subfolder12/subsubfolder121/image1.gif";
        cms.lockResource(resourcename);
        cms.deleteResource(resourcename, CmsResource.DELETE_REMOVE_SIBLINGS);
        CmsResource resource = cms.readResource(resourcename, CmsResourceFilter.ALL);

        // check the publish list
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getResources().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertNull(groups.get(0).getResources().get(0).getInfo());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());

        // check the broken links
        CmsResource source = cms.readResource("/folder1/subfolder12/subsubfolder121/page1.html");
        List<CmsPublishResourceBean> broken = adePub.getBrokenResources(Collections.singletonList(resource));
        assertEquals(1, broken.size());
        assertEquals(source.getStructureId(), broken.get(0).getId());
        assertFalse(broken.get(0).isRemovable());
        assertNull(broken.get(0).getInfo());
        assertEquals(1, broken.get(0).getRelated().size());
        assertEquals(resource.getStructureId(), broken.get(0).getRelated().get(0).getId());
        assertFalse(broken.get(0).getRelated().get(0).isRemovable());
        assertEquals(CmsPublishResourceInfoBean.Type.BROKENLINK, broken.get(0).getRelated().get(0).getInfo().getType());
        assertEquals(0, broken.get(0).getRelated().get(0).getRelated().size());

        // check before publishing
        assertFalse(cms.existsResource(resourcename, CmsResourceFilter.DEFAULT));
        assertTrue(cms.existsResource(resourcename, CmsResourceFilter.ALL));

        // publish
        adePub.publishResources(Collections.singletonList(resource));
        OpenCms.getPublishManager().waitWhileRunning();

        // check after publishing
        assertFalse(cms.existsResource(resourcename, CmsResourceFilter.ALL));

        // publish list has to be empty again
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());

        // restore the deleted resource, for the next test case
        cms.restoreDeletedResource(resource.getStructureId());
    }

    /**
     * Tests publishing a new/restored resource.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testPublishRestoredResource() throws Exception {

        CmsObject cms = getCmsObject();

        String resourcename = "/folder1/subfolder12/subsubfolder121/image1.gif";
        CmsResource resource = cms.readResource(resourcename);

        // the resource is new/restored from previous test case
        CmsADEPublish adePub = new CmsADEPublish(cms);

        List<CmsPublishGroupBean> groups = adePub.getPublishGroups();
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getResources().size());
        assertEquals(resource.getStructureId(), groups.get(0).getResources().get(0).getId());
        assertTrue(groups.get(0).getResources().get(0).isRemovable());
        assertNull(groups.get(0).getResources().get(0).getInfo());
        assertEquals(0, groups.get(0).getResources().get(0).getRelated().size());

        List<CmsPublishResourceBean> broken = adePub.getBrokenResources(Collections.singletonList(resource));
        assertEquals(0, broken.size());

        // publish
        adePub.publishResources(Collections.singletonList(resource));
        OpenCms.getPublishManager().waitWhileRunning();

        // publish list has to be empty again
        adePub = new CmsADEPublish(cms);
        groups = adePub.getPublishGroups();
        assertEquals(0, groups.size());
    }
}