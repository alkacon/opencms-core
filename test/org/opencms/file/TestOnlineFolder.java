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

package org.opencms.file;

import org.opencms.db.CmsModificationContext;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsUUID;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.FailableFunction;

import junit.framework.Test;

/**
 * Test cases for the online folder feature.
 */
public class TestOnlineFolder extends OpenCmsTestCase {

    public static final String ONLINE_FOLDER = "/shared/online";

    private CmsObject m_onlineCms;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestOnlineFolder(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        return generateSetupTestWrapper(
            TestOnlineFolder.class,
            "simpletest",
            "/",
            "/../org/opencms/search/solr/defaultconfig");
    }

    public void testCategories() throws Exception {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        String path = ONLINE_FOLDER + "/" + name;
        for (String folderPath : List.of("/system/categories", "/system/categories/cat1", "/system/categories/cat2")) {
            if (!cms.existsResource(folderPath)) {
                cms.createResource(folderPath, 0);
            }
        }
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        List<CmsResource> categories = cms.readResources("/system/categories", CmsResourceFilter.ALL, true);
        CmsResource res = cms.createResource(
            path,
            1,
            bytes("the quick brown fox jumps over the lazy dog"),
            new ArrayList<>());
        CmsCategoryService.getInstance().addResourceToCategory(cms, path, "cat1");
        CmsCategoryService.getInstance().addResourceToCategory(cms, path, "cat2");
        Set<String> onlineCategories = CmsCategoryService.getInstance().readResourceCategories(
            onlineCms,
            path).stream().map(cat -> cat.getPath()).collect(Collectors.toSet());
        assertEquals(Set.of("cat1/", "cat2/"), onlineCategories);

    }

    public void testChacc() throws Exception {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        String path = ONLINE_FOLDER + "/" + name;
        CmsResource res = cms.createResource(path, 1);
        cms.chacc(path, I_CmsPrincipal.PRINCIPAL_USER, "Admin", "+r+w");
        List<CmsAccessControlEntry> entries = onlineCms.getAccessControlEntries(path);
        entries = entries.stream().filter(entry -> entry.getResource().equals(res.getResourceId())).collect(
            Collectors.toList());
        assertEquals(1, entries.size());
        CmsAccessControlEntry entry = entries.get(0);
        assertEquals(
            CmsPermissionSet.PERMISSION_READ | CmsPermissionSet.PERMISSION_WRITE,
            entry.getAllowedPermissions());
        assertEquals(0, entry.getDeniedPermissions());
        assertEquals(cms.readUser("Admin").getId(), entry.getPrincipal());

    }

    public void testChangeType() throws Exception {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        String path = ONLINE_FOLDER + "/" + name;
        CmsResource res = cms.createResource(path, 1);
        I_CmsResourceType jspType = OpenCms.getResourceManager().getResourceType("jsp");
        cms.chtype(res, jspType);
        assertEquals(
            jspType.getTypeId(),
            OpenCms.getResourceManager().getResourceType(onlineCms.readResource(path)).getTypeId());
    }

    public void testCopy() throws Throwable {

        String name = getName();
        {
            createOnlineFolder();
            CmsObject cms = getCmsObject();
            String workFolder = "/system/" + name;
            cms.createResource(workFolder, 0);
            cms.createResource(workFolder + "/example.txt", 1, bytes("hello world"), new ArrayList<>());
            cms.copyResource(workFolder, ONLINE_FOLDER + "/" + name);
        }
        checkEqualsOfflineOnline(_cms -> {
            return _cms.readResource(ONLINE_FOLDER + "/" + name + "/example.txt").toString();
        });
    }

    public void testDelete() throws Exception {

        String name = getName();

        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        cms.createResource(ONLINE_FOLDER + "/" + name, 0);
        cms.createResource(ONLINE_FOLDER + "/" + name + "/test.txt", 1);
        assertTrue("Should be available online", onlineCms.existsResource(ONLINE_FOLDER + "/" + name));
        cms.deleteResource(ONLINE_FOLDER + "/" + name, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertFalse("Should have been removed online", onlineCms.existsResource(ONLINE_FOLDER + "/" + name));
        assertFalse(
            "Should have been removed online",
            onlineCms.existsResource(ONLINE_FOLDER + "/" + name + "/test.txt"));
    }

    public void testMove() throws Exception {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        cms.createResource(ONLINE_FOLDER + "/" + name, 0);
        cms.createResource(ONLINE_FOLDER + "/" + name + "/alpha", 0);
        cms.createResource(ONLINE_FOLDER + "/" + name + "/alpha/beta", 0);
        cms.moveResource(ONLINE_FOLDER + "/" + name, ONLINE_FOLDER + "/" + name + "_moved");
        assertTrue(onlineCms.existsResource(ONLINE_FOLDER + "/" + name + "_moved/alpha/beta"));
    }

    public void testMoveIntoOnlineFolder() throws Exception {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        cms.createResource("/system/" + name, 1);
        cms.moveResource("/system/" + name, ONLINE_FOLDER + "/" + name);
        assertEquals(CmsResource.STATE_UNCHANGED, cms.readResource(ONLINE_FOLDER + "/" + name).getState());
        assertTrue(onlineCms.existsResource(ONLINE_FOLDER + "/" + name));
    }

    public void testMoveOutOfOnlineFolder() throws Exception {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        cms.createResource(ONLINE_FOLDER + "/" + name, 1);
        cms.moveResource(ONLINE_FOLDER + "/" + name, "/system/" + name);
        assertEquals(CmsResource.STATE_CHANGED, cms.readResource("/system/" + name).getState());
    }

    public void testOnlineFolder() throws Throwable {

        CmsObject offlineCms = getCmsObject();
        CmsObject onlineCms = OpenCms.initCmsObject(offlineCms);
        onlineCms.getRequestContext().setCurrentProject(offlineCms.readProject("Online"));
        String onlineFolder = "/shared/online/";
        String file1 = onlineFolder + getName() + ".txt";
        createOnlineFolder();
        CmsProperty prop = new CmsProperty("Title", "mytitle", null);
        offlineCms.createResource(file1, 1, "foo".getBytes(StandardCharsets.UTF_8), new ArrayList<>());
        CmsResource res = offlineCms.readResource(file1, CmsResourceFilter.ALL);
        assertEquals(CmsResource.STATE_UNCHANGED, res.getState());
        checkEqualsOfflineOnline(cms -> {
            return new String(cms.readFile(file1).getContents(), StandardCharsets.UTF_8);
        });
        checkEqualsOfflineOnline(cms -> {
            return cms.readPropertyObject(file1, "Title", false).getValue();
        });

        CmsFile file = offlineCms.readFile(file1);
        file.setContents("the quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8));
        offlineCms.writeFile(file);
        res = offlineCms.readResource(file1, CmsResourceFilter.ALL);
        assertEquals(CmsResource.STATE_UNCHANGED, res.getState());
        checkEqualsOfflineOnline(cms -> {
            return new String(cms.readFile(file1).getContents(), StandardCharsets.UTF_8);
        });

        prop = new CmsProperty("Description", "foo", null);
        offlineCms.writePropertyObject(file1, prop);
        assertEquals(CmsResource.STATE_UNCHANGED, res.getState());
        checkEqualsOfflineOnline(cms -> {
            return cms.readPropertyObject(file1, "Description", false).getValue();
        });
    }

    public void testPublishEvents() throws Throwable {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        List<CmsEvent> events = new ArrayList<>();
        I_CmsEventListener listener = new I_CmsEventListener() {

            @Override
            public void cmsEvent(CmsEvent event) {

                events.add(event);

            }
        };
        OpenCms.getEventManager().addCmsEventListener(listener, new int[] {I_CmsEventListener.EVENT_PUBLISH_PROJECT});
        String path = ONLINE_FOLDER + "/" + name;
        String path2 = path + "-v2";
        CmsModificationContext.doWithModificationContext(cms.getRequestContext(), () -> {

            cms.createResource(path, 1, bytes("hello world"), new ArrayList<>());
            cms.createResource(path2, 1, bytes("hello world"), new ArrayList<>());
            cms.chflags(path, CmsResource.FLAG_INTERNAL);
            return null;
        });
        OpenCms.getEventManager().removeCmsEventListener(listener);
        assertEquals("Only one publish event expected", 1, events.size());
        checkEqualsOfflineOnline(_cms -> {
            return Integer.valueOf(_cms.readResource(path).getFlags());
        });
        CmsEvent event = events.get(0);
        assertEquals(Boolean.TRUE, event.getData().get(I_CmsEventListener.KEY_INSTANT_PUBLISH));
        CmsUUID publishId = new CmsUUID("" + event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
        List<CmsPublishedResource> publishedResources = cms.readPublishedResources(publishId);
        Set<String> changedPaths = publishedResources.stream().map(res -> res.getRootPath()).collect(
            Collectors.toSet());
        assertEquals(Set.of(path, path2), changedPaths);

    }

    public void testSearch() throws Exception {

        String name = getName();
        createOnlineFolder();
        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        String path = ONLINE_FOLDER + "/" + name;
        List<CmsProperty> props = new ArrayList<>();
        props.add(new CmsProperty("Title", "qux", null));
        CmsResource res = cms.createResource(path, 1, bytes("the quick brown fox jumps over the lazy dog"), props);
        Thread.sleep(500 + CmsModificationContext.getOnlineFolderOptions().getIndexingInterval());
        CmsSolrIndex index = (CmsSolrIndex)OpenCms.getSearchManager().getIndex("Solr Online");
        CmsSolrResultList resultList = index.search(cms, "fq=id:\"" + res.getStructureId() + "\"");
        assertEquals(1, resultList.size());
        assertEquals(res.getStructureId(), resultList.get(0).getStructureId());

    }

    protected void createOnlineFolder() throws CmsException {

        CmsObject cms = getCmsObject();
        if (!cms.existsResource(ONLINE_FOLDER)) {
            cms.createResource(ONLINE_FOLDER, 0);

        }
    }

    private byte[] bytes(String text) {

        return text.getBytes(StandardCharsets.UTF_8);
    }

    private void checkEqualsOfflineOnline(FailableFunction<CmsObject, Object, Exception> action) throws Exception {

        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCms();
        Object offlineResult = action.apply(cms);
        Object onlineResult = action.apply(onlineCms);
        assertEquals(offlineResult, onlineResult);

    }

    private CmsObject getOnlineCms() {

        try {
            if (m_onlineCms == null) {
                m_onlineCms = OpenCms.initCmsObject(getCmsObject());
                m_onlineCms.getRequestContext().setCurrentProject(
                    getCmsObject().readProject(CmsProject.ONLINE_PROJECT_ID));
            }
            return m_onlineCms;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
