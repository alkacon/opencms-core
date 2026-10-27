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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestRunner;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/** Integration tests for category-preserving moves. */
public class TestMoveCategories extends OpenCmsTestRunner {

    private CmsObject m_cms;
    private Object m_originalMoveCheck;
    private String m_root;

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo info) {

        setupOpenCms(info, "simpletest", "/");
    }

    @BeforeEach
    public void prepare(TestInfo info) throws Exception {

        m_cms = getCmsObject();
        m_cms.getRequestContext().setSiteRoot("");
        m_originalMoveCheck = OpenCms.getRuntimeProperty(CmsSecurityManager.PARAM_CHECK_CATEGORIES_ON_MOVE);
        OpenCms.setRuntimeProperty(CmsSecurityManager.PARAM_CHECK_CATEGORIES_ON_MOVE, "true");
        m_root = "/categoryMove-" + info.getTestMethod().get().getName() + "/";
        folder(m_root);
        folder(m_root + "old/");
        folder(m_root + "new/");
        folder(m_root + "old/.categories/");
        folder(m_root + "old/.categories/topic/");
    }

    @AfterEach
    public void restore() throws Exception {

        m_cms.getRequestContext().setSiteRoot("");
        OpenCms.setRuntimeProperty(
            CmsSecurityManager.PARAM_CHECK_CATEGORIES_ON_MOVE,
            m_originalMoveCheck == null ? "" : m_originalMoveCheck);
    }

    @Test
    public void testAlreadyInvalidAndUncategorizedAllowed() throws Exception {

        String source = m_root + "new/file";
        file(source);
        assign(source, m_root + "old/.categories/topic/");
        folder(m_root + "other/");
        m_cms.moveResource(source, m_root + "other/file");
        file(m_root + "new/plain");
        m_cms.moveResource(m_root + "new/plain", m_root + "other/plain");
    }

    /** A bulk query must not include similarly named siblings of files or folders. */
    @Test
    public void testBulkQueryExcludesSiblings() throws Exception {

        folder(m_root + "old/tree/");
        file(m_root + "old/tree/file");
        folder(m_root + "old/tree-other/");
        file(m_root + "old/tree-other/file");
        assign(m_root + "old/tree-other/file", m_root + "old/.categories/topic/");
        m_cms.moveResource(m_root + "old/tree/", m_root + "new/tree/");
        assertTrue(m_cms.existsResource(m_root + "new/tree/file"));
        assertFalse(relations(m_root + "old/tree-other/file").isEmpty());

        file(m_root + "old/file");
        file(m_root + "old/file-other");
        assign(m_root + "old/file-other", m_root + "old/.categories/topic/");
        m_cms.moveResource(m_root + "old/file", m_root + "new/file");
        assertTrue(m_cms.existsResource(m_root + "new/file"));
        assertFalse(relations(m_root + "old/file-other").isEmpty());
    }

    @Test
    public void testCentralCategoryRetained() throws Exception {

        CmsCategoryService service = CmsCategoryService.getInstance();
        String category = service.createCategory(m_cms, null, "move-central", "Central", "", null).getRootPath();
        file(m_root + "old/file");
        assign(m_root + "old/file", category);
        m_cms.moveResource(m_root + "old/file", m_root + "new/file");
        assertEquals(category, relations(m_root + "new/file").get(0).getTargetPath());
    }

    @Test
    public void testCustomCategoryFolder() throws Exception {

        if (!m_cms.existsResource(CmsCategoryService.CENTRALIZED_REPOSITORY)) {
            folder(CmsCategoryService.CENTRALIZED_REPOSITORY);
        }
        CmsProperty original = m_cms.readPropertyObject(
            CmsCategoryService.CENTRALIZED_REPOSITORY,
            CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
            false);
        m_cms.lockResource(CmsCategoryService.CENTRALIZED_REPOSITORY);
        m_cms.writePropertyObject(
            CmsCategoryService.CENTRALIZED_REPOSITORY,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_DEFAULT_FILE, ".custom-categories", null));
        try {
            folder(m_root + "old/.custom-categories/");
            folder(m_root + "old/.custom-categories/topic/");
            file(m_root + "old/file");
            assign(m_root + "old/file", m_root + "old/.custom-categories/topic/");
            assertRejectedUnchanged(m_root + "old/file", m_root + "new/file");
        } finally {
            m_cms.writePropertyObject(
                CmsCategoryService.CENTRALIZED_REPOSITORY,
                new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                    original.getStructureValue() == null ? CmsProperty.DELETE_VALUE : original.getStructureValue(),
                    original.getResourceValue() == null ? CmsProperty.DELETE_VALUE : original.getResourceValue()));
        }
    }

    @Test
    public void testDeletedDescendantStillChecked() throws Exception {

        folder(m_root + "old/tree/");
        file(m_root + "old/tree/file");
        assign(m_root + "old/tree/file", m_root + "old/.categories/topic/");
        m_cms.unlockProject(m_cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(m_cms);
        OpenCms.getPublishManager().waitWhileRunning();
        m_cms.lockResource(m_root);
        m_cms.deleteResource(m_root + "old/tree/file", CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertRejectedUnchanged(m_root + "old/tree/", m_root + "new/tree/");
    }

    @Test
    public void testDestinationPropertyInheritance() throws Exception {

        file(m_root + "old/file");
        assign(m_root + "old/file", m_root + "old/.categories/topic/");
        repositoryProperty(m_root + "new/", m_root + "old/");
        m_cms.moveResource(m_root + "old/file", m_root + "new/file");
        assertFalse(relations(m_root + "new/file").isEmpty());
    }

    @Test
    public void testDirectMoveRejected() throws Exception {

        String source = m_root + "old/file";
        file(source);
        assign(source, m_root + "old/.categories/topic/");
        assertRejectedUnchanged(source, m_root + "new/file");
    }

    @Test
    public void testDisabledAllowsCategoryLoss() throws Exception {

        for (String setting : new String[] {"", "false"}) {
            OpenCms.setRuntimeProperty(CmsSecurityManager.PARAM_CHECK_CATEGORIES_ON_MOVE, setting);
            String name = setting.isEmpty() ? "empty" : "disabled";
            String source = m_root + "old/" + name;
            file(source);
            assign(source, m_root + "old/.categories/topic/");
            m_cms.moveResource(source, m_root + "new/" + name);
            assertTrue(m_cms.existsResource(m_root + "new/" + name));
            assertTrue(relations(m_root + "new/" + name).isEmpty());
        }
    }

    @Test
    public void testEquivalentCategoryRemapped() throws Exception {

        folder(m_root + "new/.categories/");
        folder(m_root + "new/.categories/topic/");
        String source = m_root + "old/file";
        file(source);
        assign(source, m_root + "old/.categories/topic/");
        m_cms.moveResource(source, m_root + "new/file");
        assertEquals(
            m_cms.readResource(m_root + "new/.categories/topic/").getStructureId(),
            relations(m_root + "new/file").get(0).getTargetId());
    }

    @Test
    public void testExpiredDescendantStillChecked() throws Exception {

        folder(m_root + "old/tree/");
        file(m_root + "old/tree/file");
        assign(m_root + "old/tree/file", m_root + "old/.categories/topic/");
        m_cms.setDateExpired(m_root + "old/tree/file", System.currentTimeMillis() - 10000, false);
        assertRejectedUnchanged(m_root + "old/tree/", m_root + "new/tree/");
    }

    @Test
    public void testFolderAssignmentRejected() throws Exception {

        String source = m_root + "old/folder/";
        folder(source);
        assign(source, m_root + "old/.categories/topic/");
        assertRejectedUnchanged(source, m_root + "new/folder/");
    }

    @Test
    public void testLiteralRepositoryReferenceNotRewritten() throws Exception {

        folder(m_root + "tree/");
        folder(m_root + "tree/repository/");
        folder(m_root + "tree/repository/.categories/");
        folder(m_root + "tree/repository/.categories/topic/");
        file(m_root + "tree/file");
        repositoryProperty(m_root + "tree/file", m_root + "tree/repository/");
        assign(m_root + "tree/file", m_root + "tree/repository/.categories/topic/");
        assertRejectedUnchanged(m_root + "tree/", m_root + "new/tree/");
    }

    @Test
    public void testNonRootSiteContext() throws Exception {

        file(m_root + "old/file");
        assign(m_root + "old/file", m_root + "old/.categories/topic/");
        m_cms.getRequestContext().setSiteRoot(m_root.substring(0, m_root.length() - 1));
        assertThrows(CmsException.class, () -> m_cms.moveResource("/old/file", "/new/file"));
        assertTrue(m_cms.existsResource("/old/file"));
        assertFalse(m_cms.existsResource("/new/file"));
        assertEquals(m_root.substring(0, m_root.length() - 1), m_cms.getRequestContext().getSiteRoot());
    }

    @Test
    public void testPropertyCarriedWithSubtree() throws Exception {

        String source = m_root + "new/tree/";
        folder(source);
        file(source + "file");
        repositoryProperty(source, m_root + "old/");
        assign(source + "file", m_root + "old/.categories/topic/");
        folder(m_root + "other/");
        m_cms.moveResource(source, m_root + "other/tree/");
        assertFalse(relations(m_root + "other/tree/file").isEmpty());
    }

    @Test
    public void testRepositoryMovesWithSubtree() throws Exception {

        String source = m_root + "old/file";
        file(source);
        assign(source, m_root + "old/.categories/topic/");
        m_cms.moveResource(m_root + "old/", m_root + "new/tree/");
        assertEquals(
            m_cms.readResource(m_root + "new/tree/.categories/topic/").getStructureId(),
            relations(m_root + "new/tree/file").get(0).getTargetId());
    }

    @Test
    public void testRetainedCategoryOnRename() throws Exception {

        file(m_root + "old/file");
        assign(m_root + "old/file", m_root + "old/.categories/topic/");
        m_cms.moveResource(m_root + "old/file", m_root + "old/renamed");
        assertFalse(relations(m_root + "old/renamed").isEmpty());
    }

    @Test
    public void testSourcePropertyInheritanceLost() throws Exception {

        folder(m_root + "other/");
        file(m_root + "new/file");
        repositoryProperty(m_root + "new/", m_root + "old/");
        assign(m_root + "new/file", m_root + "old/.categories/topic/");
        assertRejectedUnchanged(m_root + "new/file", m_root + "other/file");
    }

    @Test
    public void testUnreadableDescendantNotSkipped() throws Exception {

        String tree = m_root + "old/tree/";
        folder(tree);
        file(tree + "file");
        assign(tree + "file", m_root + "old/.categories/topic/");
        m_cms.chacc(m_root, I_CmsPrincipal.PRINCIPAL_USER, "test2", "+r+w+v+i");
        m_cms.chacc(tree + "file", I_CmsPrincipal.PRINCIPAL_USER, "test2", "-r+w+v+i");
        m_cms.unlockResource(m_root);
        CmsObject restricted = OpenCms.initCmsObject(m_cms);
        restricted.loginUser("test2", "test2");
        restricted.getRequestContext().setCurrentProject(restricted.readProject("Offline"));
        restricted.lockResource(tree);
        CmsException error = assertThrows(
            CmsException.class,
            () -> restricted.moveResource(tree, m_root + "new/tree/"));
        assertTrue(error.getMessage().contains("category") || error.getCause().getMessage().contains("category"));
        assertTrue(m_cms.existsResource(tree + "file"));
        assertFalse(m_cms.existsResource(m_root + "new/tree/"));
    }

    @Test
    public void testWholeSubtreeUnchanged() throws Exception {

        String source = m_root + "old/tree/";
        folder(source);
        file(source + "a-first");
        folder(source + "z-last/");
        file(source + "z-last/file");
        assign(source + "z-last/file", m_root + "old/.categories/topic/");
        m_cms.writePropertyObject(source + "a-first", new CmsProperty("Title", "Keep this", null));
        assertRejectedUnchanged(source, m_root + "new/tree/");
    }

    private void assertRejectedUnchanged(String source, String destination) throws Exception {

        List<CmsResource> before = new ArrayList<>();
        before.add(m_cms.readResource(source, CmsResourceFilter.ALL));
        if (source.endsWith("/")) {
            before.addAll(m_cms.readResources(source, CmsResourceFilter.ALL, true));
        }
        List<List<CmsRelation>> oldRelations = new ArrayList<>();
        List<List<CmsProperty>> oldProperties = new ArrayList<>();
        for (CmsResource resource : before) {
            oldRelations.add(relations(resource.getRootPath()));
            oldProperties.add(m_cms.readPropertyObjects(resource, false));
        }
        CmsException error = assertThrows(CmsException.class, () -> m_cms.moveResource(source, destination));
        assertTrue(error.getMessage().contains("category") || error.getCause().getMessage().contains("category"));
        assertFalse(m_cms.existsResource(destination, CmsResourceFilter.ALL));
        for (int i = 0; i < before.size(); i++) {
            CmsResource previous = before.get(i);
            CmsResource current = m_cms.readResource(previous.getStructureId(), CmsResourceFilter.ALL);
            assertEquals(previous.getRootPath(), current.getRootPath());
            assertEquals(previous.getState(), current.getState());
            assertEquals(previous.getResourceId(), current.getResourceId());
            assertEquals(previous.getDateLastModified(), current.getDateLastModified());
            assertEquals(oldRelations.get(i), relations(current.getRootPath()));
            List<CmsProperty> properties = m_cms.readPropertyObjects(current, false);
            assertEquals(oldProperties.get(i).size(), properties.size());
            for (CmsProperty property : oldProperties.get(i)) {
                assertTrue(property.isIdentical(properties.get(properties.indexOf(property))));
            }
        }
    }

    private void assign(String resource, String category) throws Exception {

        m_cms.addRelationToResource(resource, category, CmsRelationType.CATEGORY.getName());
    }

    private void file(String path) throws Exception {

        m_cms.createResource(path, CmsResourceTypePlain.getStaticTypeId());
    }

    private void folder(String path) throws Exception {

        m_cms.createResource(path, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
    }

    private List<CmsRelation> relations(String path) throws Exception {

        return m_cms.getRelationsForResource(path, CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY));
    }

    private void repositoryProperty(String path, String value) throws Exception {

        m_cms.writePropertyObject(
            path,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_CATEGORY_REPOSITORY, value, null));
    }
}
