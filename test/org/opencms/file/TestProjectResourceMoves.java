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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package org.opencms.file;

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.test.OpenCmsTestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/** Tests that moves preserve the exact resource scope of every affected project. */
public class TestProjectResourceMoves extends OpenCmsTestRunner {

    private CmsObject m_cms;
    private String m_root;
    private int m_projectNumber;

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo info) {

        // Run the same integration tests with the legacy and storage VFS drivers,
        // without changing the checked-in database configuration.
        initConfiguration();
        Path configuration = null;
        try {
            configuration = Files.createTempDirectory("opencms-project-moves-");
            Path original = Path.of(getTestDataPath("WEB-INF/config." + getDbProduct() + "/"));
            try (Stream<Path> files = Files.list(original)) {
                for (Path file : files.toList()) {
                    if (Files.isRegularFile(file)) {
                        Files.copy(file, configuration.resolve(file.getFileName()));
                    }
                }
            }
            Path propertiesFile = configuration.resolve("opencms.properties");
            String properties = Files.readString(propertiesFile, StandardCharsets.ISO_8859_1);
            assertTrue(properties.contains("db.vfs.driver="));
            properties = properties.replaceAll(
                "(?m)^db\\.vfs\\.driver=.*$",
                "db.vfs.driver=org.opencms.db." + getDbProduct() + "." + getVfsDriverName());
            Files.writeString(propertiesFile, properties, StandardCharsets.ISO_8859_1);
            setupOpenCms(info, "simpletest", "/", configuration.toString(), null, true);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (configuration != null) {
                FileUtils.deleteQuietly(configuration.toFile());
            }
        }
    }

    @BeforeEach
    public void prepare(TestInfo info) throws Exception {

        m_cms = getCmsObject();
        m_cms.getRequestContext().setSiteRoot("");
        m_root = "/project-moves-" + info.getTestMethod().get().getName() + "/";
        folder(m_root);
    }

    /** A case-only rename must update every OU's default project and OU relation. */
    @Test
    public void testCaseOnlyRenameInOuProjects() throws Exception {

        String source = m_root + "B/";
        String target = m_root + "b/";
        folder(source);
        CmsOrganizationalUnit first = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            m_cms,
            "move-first/",
            "Move test",
            0,
            source);
        CmsOrganizationalUnit second = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            m_cms,
            "move-second/",
            "Move test",
            0,
            source);
        CmsProject firstProject = m_cms.readProject(first.getProjectId());
        CmsProject secondProject = m_cms.readProject(second.getProjectId());
        assertPaths(firstProject, source);
        assertPaths(secondProject, source);

        m_cms.moveResource(source, target);

        for (CmsOrganizationalUnit ou : Arrays.asList(first, second)) {
            assertPaths(m_cms.readProject(ou.getProjectId()), target);
            assertEquals(
                target,
                OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(m_cms, ou.getName()).get(
                    0).getRootPath());
        }
    }

    /** Deletion uses the same project lookup and must clear every matching project. */
    @Test
    public void testDeleteFolderInMultipleProjects() throws Exception {

        String source = m_root + "source/";
        folder(source);
        folder(source + "child/");
        CmsProject first = project(source);
        CmsProject second = project(source);
        CmsProject child = project(source + "child/");

        m_cms.deleteResource(source, CmsResource.DELETE_PRESERVE_SIBLINGS);

        assertPaths(first);
        assertPaths(second);
        assertPaths(child);
    }

    /** Nested OUs must retain their individual scopes when an ancestor or descendant is renamed. */
    @Test
    public void testMoveAncestorInNestedOuProjects() throws Exception {

        String source = m_root + "B/";
        String target = m_root + "b/";
        folder(source);
        folder(source + "child/");
        folder(source + "child/deep/");
        folder(m_root + "unrelated/");
        CmsOrganizationalUnit outer = ou("nested-move/", m_root);
        CmsOrganizationalUnit parent = ou("nested-move/parent/", source);
        CmsOrganizationalUnit sameScope = ou("nested-move/parent/same/", source);
        CmsOrganizationalUnit child = ou("nested-move/parent/child/", source + "child/");
        CmsOrganizationalUnit deep = ou("nested-move/parent/child/deep/", source + "child/deep/");
        CmsOrganizationalUnit unrelated = ou("nested-move/unrelated/", m_root + "unrelated/");
        assertOuPath(outer, m_root);
        assertOuPath(parent, source);
        assertOuPath(sameScope, source);
        assertOuPath(child, source + "child/");
        assertOuPath(deep, source + "child/deep/");
        assertOuPath(unrelated, m_root + "unrelated/");

        m_cms.moveResource(source, target);

        assertOuPath(outer, m_root);
        assertOuPath(parent, target);
        assertOuPath(sameScope, target);
        assertOuPath(child, target + "child/");
        assertOuPath(deep, target + "child/deep/");
        assertOuPath(unrelated, m_root + "unrelated/");

        // Moving only a descendant must leave the enclosing OU/project entries unchanged.
        m_cms.moveResource(target + "child/", target + "renamed/");

        assertOuPath(outer, m_root);
        assertOuPath(parent, target);
        assertOuPath(sameScope, target);
        assertOuPath(child, target + "renamed/");
        assertOuPath(deep, target + "renamed/deep/");
        assertOuPath(unrelated, m_root + "unrelated/");
        assertFalse(
            CmsProject.isInsideProject(m_cms.readProjectResources(m_cms.readProject(deep.getProjectId())), target));
    }

    /** Moving a published folder and undoing the move must update all project references. */
    @Test
    public void testMoveAndUndoInMultipleProjects() throws Exception {

        String source = m_root + "source/";
        String target = m_root + "destination/renamed/";
        folder(source);
        folder(m_root + "destination/");
        folder(m_root + "unrelated/");
        m_cms.unlockResource(m_root);
        OpenCms.getPublishManager().publishResource(m_cms, m_root);
        OpenCms.getPublishManager().waitWhileRunning();
        CmsProject first = project(source, m_root + "unrelated/");
        CmsProject second = project(source);
        m_cms.lockResource(source);

        m_cms.moveResource(source, target);
        assertPaths(first, target, m_root + "unrelated/");
        assertPaths(second, target);

        m_cms.undoChanges(target, CmsResource.UNDO_MOVE_CONTENT);
        assertPaths(first, source, m_root + "unrelated/");
        assertPaths(second, source);
    }

    /** An ancestor move must retain child-only scopes and multiple entries in one project. */
    @Test
    public void testMoveChildScopesWithoutAddingParent() throws Exception {

        String source = m_root + "source/";
        String target = m_root + "target/";
        folder(source);
        folder(source + "one/");
        folder(source + "two/");
        file(source + "file");
        // The child-only project sorts first, exposing the old prefix lookup bug.
        CmsProject children = project(source + "one/", source + "two/", source + "file");
        CmsProject parent = project(source);
        CmsProject child = project(source + "one/");
        CmsProject ancestor = project(m_root);

        m_cms.moveResource(source, target);

        assertPaths(children, target + "one/", target + "two/", target + "file");
        assertPaths(parent, target);
        assertPaths(child, target + "one/");
        assertPaths(ancestor, m_root);
        assertFalse(CmsProject.isInsideProject(m_cms.readProjectResources(children), target + "other/"));
    }

    /** Moving one file must not change a project containing a similarly named file. */
    @Test
    public void testMoveFileDoesNotMatchPrefix() throws Exception {

        file(m_root + "file");
        file(m_root + "file-other");
        CmsProject unrelated = project(m_root + "file-other");
        CmsProject first = project(m_root + "file");
        CmsProject second = project(m_root + "file");

        m_cms.moveResource(m_root + "file", m_root + "renamed");

        assertPaths(unrelated, m_root + "file-other");
        assertPaths(first, m_root + "renamed");
        assertPaths(second, m_root + "renamed");
    }

    /** Public API paths may omit folder slashes; project entries must remain canonical. */
    @Test
    public void testMoveFolderWithOptionalTrailingSlashes() throws Exception {

        CmsProject rootProject = project("/");
        for (boolean sourceSlash : new boolean[] {false, true}) {
            for (boolean targetSlash : new boolean[] {false, true}) {
                String suffix = sourceSlash + "-" + targetSlash;
                String source = m_root + "source-" + suffix;
                String target = m_root + "target-" + suffix;
                folder(source + "/");
                folder(source + "/child/");
                CmsProject first = project(source + "/");
                CmsProject second = project(source + "/");
                CmsProject child = project(source + "/child/");
                CmsProject assignedWithoutSlash = project(source + "/child/");
                // Exercise project assignment through a caller-supplied path without a slash as well.
                CmsProject previous = m_cms.getRequestContext().getCurrentProject();
                try {
                    m_cms.getRequestContext().setCurrentProject(assignedWithoutSlash);
                    m_cms.copyResourceToProject(source);
                    assertPaths(assignedWithoutSlash, source + "/");
                } finally {
                    m_cms.getRequestContext().setCurrentProject(previous);
                }
                assertEquals(source + "/", m_cms.readResource(source).getRootPath());
                assertEquals(source + "/", m_cms.readResource(source + "/").getRootPath());

                m_cms.moveResource(source + (sourceSlash ? "/" : ""), target + (targetSlash ? "/" : ""));

                assertPaths(first, target + "/");
                assertPaths(second, target + "/");
                assertPaths(child, target + "/child/");
                assertPaths(assignedWithoutSlash, target + "/");
                assertPaths(rootProject, "/");
            }
        }
    }

    /** Site-relative API arguments must be converted to full canonical project paths. */
    @Test
    public void testMoveFolderWithSiteRoot() throws Exception {

        folder(m_root + "source/");
        folder(m_root + "source/child/");
        CmsProject first = project(m_root + "source/");
        CmsProject second = project(m_root + "source/");
        CmsProject child = project(m_root + "source/child/");
        m_cms.getRequestContext().setSiteRoot(m_root);
        try {
            m_cms.moveResource("/source", "/target");
            assertPaths(first, m_root + "target/");
            assertPaths(second, m_root + "target/");
            assertPaths(child, m_root + "target/child/");
        } finally {
            m_cms.getRequestContext().setSiteRoot("");
        }
    }

    /** Underscores in resource names must not act as SQL LIKE wildcards. */
    @Test
    public void testMoveFolderWithUnderscore() throws Exception {

        folder(m_root + "a_b/");
        folder(m_root + "axb/");
        CmsProject unrelated = project(m_root + "axb/");
        CmsProject first = project(m_root + "a_b/");
        CmsProject second = project(m_root + "a_b/");

        m_cms.moveResource(m_root + "a_b/", m_root + "renamed/");

        assertPaths(unrelated, m_root + "axb/");
        assertPaths(first, m_root + "renamed/");
        assertPaths(second, m_root + "renamed/");
    }

    /** Publishing a folder deletion also uses the shared lookup, after the VFS entries are removed. */
    @Test
    public void testPublishDeletedFolderInMultipleProjects() throws Exception {

        String source = m_root + "source/";
        folder(source);
        folder(source + "child/");
        m_cms.unlockResource(m_root);
        OpenCms.getPublishManager().publishResource(m_cms, m_root);
        OpenCms.getPublishManager().waitWhileRunning();
        CmsProject first = project(source);
        CmsProject second = project(source);
        CmsProject child = project(source + "child/");

        m_cms.lockResource(source);
        m_cms.deleteResource(source.substring(0, source.length() - 1), CmsResource.DELETE_PRESERVE_SIBLINGS);
        m_cms.unlockResource(source);
        OpenCms.getPublishManager().publishResource(m_cms, source);
        OpenCms.getPublishManager().waitWhileRunning();

        assertPaths(first);
        assertPaths(second);
        assertPaths(child);
        assertFalse(m_cms.existsResource(source, CmsResourceFilter.ALL));
    }

    /** @return the VFS driver variant under test */
    protected String getVfsDriverName() {

        return "CmsVfsDriver";
    }

    private void assertOuPath(CmsOrganizationalUnit ou, String expected) throws Exception {

        assertPaths(m_cms.readProject(ou.getProjectId()), expected);
        List<CmsResource> resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
            m_cms,
            ou.getName());
        assertEquals(List.of(expected), resources.stream().map(CmsResource::getRootPath).toList(), ou.getName());
    }

    private void assertPaths(CmsProject project, String... expected) throws Exception {

        List<String> actual = m_cms.readProjectResources(project);
        assertEquals(new TreeSet<>(Arrays.asList(expected)), new TreeSet<>(actual), project.getName());
        assertEquals(expected.length, actual.size(), "No duplicate project paths: " + project.getName());
    }

    private void file(String path) throws Exception {

        m_cms.createResource(path, CmsResourceTypePlain.getStaticTypeId());
    }

    private void folder(String path) throws Exception {

        m_cms.createResource(path, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
    }

    private CmsOrganizationalUnit ou(String name, String path) throws Exception {

        return OpenCms.getOrgUnitManager().createOrganizationalUnit(m_cms, name, "Nested OU move test", 0, path);
    }

    private CmsProject project(String... paths) throws Exception {

        CmsProject previous = m_cms.getRequestContext().getCurrentProject();
        CmsProject project = m_cms.createProject(
            String.format("move-project-%03d", ++m_projectNumber),
            "Move regression test",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            CmsProject.PROJECT_TYPE_NORMAL);
        try {
            m_cms.getRequestContext().setCurrentProject(project);
            for (String path : paths) {
                m_cms.copyResourceToProject(path);
            }
            assertPaths(project, paths);
        } finally {
            m_cms.getRequestContext().setCurrentProject(previous);
        }
        return project;
    }
}
