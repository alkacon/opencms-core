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

import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsUUID;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;

/**
 * Regression tests for unrelated data overwritten by stale CmsResource arguments.<p>
 *
 * Each scenario runs both with a freshly read argument and with a snapshot made stale
 * by real CmsObject operations. Assertions describe the desired behavior in both cases.
 * The regression cases deliberately fail for stale inputs until the corresponding
 * operations are fixed; the remaining target operations are included as controls.<p>
 */
public class TestStaleResource extends OpenCmsTestRunner {

    /** A scenario run with either a stale or a freshly read resource argument. */
    private interface I_Scenario {

        /**
         * Runs the scenario.
         *
         * @param stale whether to use the stale argument
         * @throws Exception if the scenario fails
         */
        void run(boolean stale) throws Exception;
    }

    /** Two versions of a resource, obtained before and after intervening changes. */
    private static class ResourceVersions {

        /** The current resource in the database. */
        private CmsResource m_current;

        /** The snapshot from before the intervening changes. */
        private CmsResource m_stale;

        /**
         * Creates the pair.
         *
         * @param stale the old snapshot
         * @param current the current resource
         */
        ResourceVersions(CmsResource stale, CmsResource current) {

            m_stale = stale;
            m_current = current;
        }

        /**
         * Gets the argument to pass to the operation.
         *
         * @param cms the CMS context
         * @param stale whether to use the stale snapshot
         * @return the argument
         * @throws Exception if reading fails
         */
        CmsResource argument(CmsObject cms, boolean stale) throws Exception {

            return stale ? m_stale : cms.readResource(m_current.getStructureId(), CmsResourceFilter.ALL);
        }
    }

    /** A fixed modification date before the intervening changes. */
    private static final long OLD_MODIFIED = 1234567890000L;

    /** A different modification date after the intervening changes. */
    private static final long CURRENT_MODIFIED = OLD_MODIFIED + 60000L;

    /** Release date set by the intervening changes. */
    private static final long RELEASED = 1234500000000L;

    /** Expiration date set by the intervening changes (year 2100). */
    private static final long EXPIRED = 4102444800000L;

    /** Unique resource names keep all scenarios independent. */
    private int m_resourceCounter;

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Checks the remaining operations which take a resource as their target.<p>
     *
     * Explicit snapshot writers (writeResource, writeFile, createResource and
     * importResource) intentionally consume the supplied metadata and are outside
     * the unrelated-data criterion. Read-only operations cannot write stale data.<p>
     *
     * @return the independent control cases
     */
    @TestFactory
    public List<DynamicTest> testOtherResourceArguments() {

        List<DynamicTest> tests = new ArrayList<>();
        for (String operation : List.of(
            "changeLock",
            "lockResource",
            "lockResourceShallow",
            "lockResourceTemporary",
            "unlockResource")) {
            addScenario(tests, operation + " preserves metadata", stale -> checkLock(stale, operation));
        }
        addScenario(tests, "copyResourceToProject preserves metadata", stale -> checkProject(stale, false));
        addScenario(tests, "writeProjectLastModified preserves metadata", stale -> checkProject(stale, true));
        addScenario(tests, "importAccessControlEntries preserves metadata", this::checkImportAccessControlEntries);
        return tests;
    }

    /**
     * Tests each operation with a fresh control and a stale argument.<p>
     *
     * @return the independent test cases
     */
    @TestFactory
    public List<DynamicTest> testStaleResourceArguments() {

        List<DynamicTest> tests = new ArrayList<>();
        addScenario(tests, "chtype preserves unrelated header fields", this::checkChangeType);
        addScenario(tests, "writePropertyObjects: structure property", stale -> checkProperties(stale, false));
        addScenario(tests, "writePropertyObjects: shared property", stale -> checkProperties(stale, true));
        addScenario(tests, "setDateExpired preserves release date and flags", this::checkDateExpired);
        addScenario(tests, "setDateReleased preserves expiration date and flags", this::checkDateReleased);
        addScenario(tests, "deleteResource: preserve siblings", stale -> checkDelete(stale, false));
        addScenario(tests, "deleteResource: remove siblings", stale -> checkDelete(stale, true));
        addScenario(tests, "setRestricted: add restriction", stale -> checkRestricted(stale, true));
        addScenario(tests, "setRestricted: remove restriction", stale -> checkRestricted(stale, false));
        addScenario(tests, "addRelationToResource preserves published status", stale -> checkRelation(stale, true));
        addScenario(
            tests,
            "deleteRelationsFromResource preserves published status",
            stale -> checkRelation(stale, false));
        return tests;
    }

    /**
     * Adds both variants of a scenario.
     *
     * @param tests the result list
     * @param name the scenario name
     * @param scenario the scenario
     */
    private void addScenario(List<DynamicTest> tests, String name, I_Scenario scenario) {

        tests.add(DynamicTest.dynamicTest(name + " [fresh control]", () -> scenario.run(false)));
        tests.add(DynamicTest.dynamicTest(name + " [stale argument]", () -> scenario.run(true)));
    }

    /**
     * Compares header fields outside an operation's intended changes.<p>
     *
     * Last-modified metadata and state are checked separately where applicable, since
     * several operations legitimately change them. The expected object is adjusted
     * only for the explicit type/date change requested by the operation.<p>
     *
     * @param expected the metadata immediately before the operation
     * @param actual the metadata read back from the database
     */
    private void assertMetadata(CmsResource expected, CmsResource actual) {

        assertAll(
            () -> assertEquals(expected.getTypeId(), actual.getTypeId(), "resource type"),
            () -> assertEquals(expected.getFlags(), actual.getFlags(), "resource flags"),
            () -> assertEquals(expected.getLength(), actual.getLength(), "content length"),
            () -> assertEquals(expected.getDateContent(), actual.getDateContent(), "content modification date"),
            () -> assertEquals(expected.getDateReleased(), actual.getDateReleased(), "release date"),
            () -> assertEquals(expected.getDateExpired(), actual.getDateExpired(), "expiration date"),
            () -> assertEquals(expected.getRootPath(), actual.getRootPath(), "resource path"),
            () -> assertEquals(expected.getResourceId(), actual.getResourceId(), "resource ID"),
            () -> assertEquals(expected.getDateCreated(), actual.getDateCreated(), "creation date"),
            () -> assertEquals(expected.getUserCreated(), actual.getUserCreated(), "creator"));
    }

    /**
     * Also checks state and last-modified metadata for operations which preserve them.
     *
     * @param expected the metadata before the operation
     * @param actual the metadata read from the database
     */
    private void assertUnchangedHeader(CmsResource expected, CmsResource actual) {

        assertMetadata(expected, actual);
        assertAll(
            () -> assertEquals(expected.getState(), actual.getState(), "resource state"),
            () -> assertEquals(expected.getDateLastModified(), actual.getDateLastModified(), "modification date"),
            () -> assertEquals(expected.getUserLastModified(), actual.getUserLastModified(), "modifying user"));
    }

    /**
     * Creates an old snapshot and makes it stale by changing independent metadata.
     *
     * @param cms the CMS context
     * @param publish whether to publish before taking the old snapshot
     * @return the two resource versions
     * @throws Exception if setup fails
     */
    private ResourceVersions changeMetadata(CmsObject cms, boolean publish) throws Exception {

        String path = createFile(cms);
        if (publish) {
            publishAndLock(cms, path);
        }
        CmsResource before = cms.readResource(path);
        cms.replaceResource(
            path,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypeBinary.getStaticTypeId()),
            "newer and longer content".getBytes(StandardCharsets.UTF_8),
            null);
        cms.chflags(path, CmsResource.FLAG_INTERNAL);
        cms.setDateReleased(path, RELEASED, false);
        cms.setDateExpired(path, EXPIRED, false);
        cms.setDateLastModified(path, CURRENT_MODIFIED, false);
        CmsResource current = cms.readResource(path);
        assertNotEquals(before.getFlags(), current.getFlags(), "fixture must contain stale flags");
        assertNotEquals(before.getLength(), current.getLength(), "fixture must contain a stale length");
        return new ResourceVersions(before, current);
    }

    /**
     * Checks a type change.
     *
     * @param stale whether to use the stale argument
     * @throws Exception if the test fails
     */
    private void checkChangeType(boolean stale) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, false);
        cms.chtype(
            versions.argument(cms, stale),
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeId()));
        versions.m_current.setType(CmsResourceTypePlain.getStaticTypeId());
        assertMetadata(versions.m_current, readPersisted(cms, versions.m_current));
    }

    /**
     * Checks an expiration date change.
     *
     * @param stale whether to use the stale argument
     * @throws Exception if the test fails
     */
    private void checkDateExpired(boolean stale) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, false);
        long requested = EXPIRED + 60000L;
        cms.setDateExpired(versions.argument(cms, stale), requested, false);
        versions.m_current.setDateExpired(requested);
        assertMetadata(versions.m_current, readPersisted(cms, versions.m_current));
    }

    /**
     * Checks a release date change.
     *
     * @param stale whether to use the stale argument
     * @throws Exception if the test fails
     */
    private void checkDateReleased(boolean stale) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, false);
        long requested = RELEASED + 60000L;
        cms.setDateReleased(versions.argument(cms, stale), requested, false);
        versions.m_current.setDateReleased(requested);
        assertMetadata(versions.m_current, readPersisted(cms, versions.m_current));
    }

    /**
     * Checks deletion of a published resource, which must keep its current metadata.
     *
     * @param stale whether to use the stale argument
     * @param removeSiblings whether to use DELETE_REMOVE_SIBLINGS
     * @throws Exception if the test fails
     */
    private void checkDelete(boolean stale, boolean removeSiblings) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, true);
        cms.deleteResource(
            versions.argument(cms, stale),
            removeSiblings ? CmsResource.DELETE_REMOVE_SIBLINGS : CmsResource.DELETE_PRESERVE_SIBLINGS);
        CmsResource actual = readPersisted(cms, versions.m_current);
        assertEquals(CmsResource.STATE_DELETED, actual.getState(), "requested deletion");
        assertMetadata(versions.m_current, actual);
    }

    /**
     * Checks importing permissions without overwriting the resource header.
     *
     * @param stale whether to use the stale argument
     * @throws Exception if the test fails
     */
    private void checkImportAccessControlEntries(boolean stale) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, false);
        CmsUUID groupId = cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers()).getId();
        CmsAccessControlEntry entry = new CmsAccessControlEntry(
            versions.m_current.getResourceId(),
            groupId,
            CmsPermissionSet.PERMISSION_READ,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_GROUP);
        cms.importAccessControlEntries(versions.argument(cms, stale), List.of(entry));
        assertTrue(cms.getAccessControlEntries(cms.getSitePath(versions.m_current), false).contains(entry));
        assertUnchangedHeader(versions.m_current, readPersisted(cms, versions.m_current));
    }

    /**
     * Checks the locking operations, including taking over another user's lock.
     *
     * @param stale whether to use the stale argument
     * @param operation the CmsObject method to call
     * @throws Exception if the test fails
     */
    private void checkLock(boolean stale, String operation) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, false);
        String path = cms.getSitePath(versions.m_current);
        if (operation.equals("changeLock")) {
            cms.chacc(path, "user", "test1", "+r+w+v");
            cms.unlockResource(path);
            CmsObject lockOwner = OpenCms.initCmsObject(cms);
            lockOwner.loginUser("test1", "test1");
            lockOwner.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
            lockOwner.lockResource(path);
            assertNotEquals(cms.getRequestContext().getCurrentUser().getId(), cms.getLock(path).getUserId());
        } else if (!operation.equals("unlockResource")) {
            cms.unlockResource(path);
        }
        CmsResource argument = versions.argument(cms, stale);
        CmsLockType expected = CmsLockType.EXCLUSIVE;
        switch (operation) {
            case "changeLock":
                cms.changeLock(argument);
                break;
            case "lockResource":
                cms.lockResource(argument);
                break;
            case "lockResourceShallow":
                cms.lockResourceShallow(argument);
                expected = CmsLockType.SHALLOW;
                break;
            case "lockResourceTemporary":
                cms.lockResourceTemporary(argument);
                expected = CmsLockType.TEMPORARY;
                break;
            case "unlockResource":
                cms.unlockResource(argument);
                expected = CmsLockType.UNLOCKED;
                break;
            default:
                throw new AssertionError(operation);
        }
        CmsResource actual = readPersisted(cms, versions.m_current);
        assertEquals(expected, cms.getLock(actual).getType(), "requested lock change");
        if (expected != CmsLockType.UNLOCKED) {
            assertEquals(cms.getRequestContext().getCurrentUser().getId(), cms.getLock(actual).getUserId());
        }
        assertUnchangedHeader(versions.m_current, actual);
    }

    /**
     * Checks project membership or the last-modified project reference.
     *
     * @param stale whether to use the stale argument
     * @param writeProject whether to write the reference instead of project membership
     * @throws Exception if the test fails
     */
    private void checkProject(boolean stale, boolean writeProject) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, false);
        CmsProject project = cms.createProject(
            "stale-resource-project-" + m_resourceCounter,
            "Stale resource test",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupProjectmanagers());
        if (writeProject) {
            cms.writeProjectLastModified(versions.argument(cms, stale), project);
            assertEquals(project.getUuid(), readPersisted(cms, versions.m_current).getProjectLastModified());
        } else {
            CmsProject original = cms.getRequestContext().getCurrentProject();
            try {
                cms.getRequestContext().setCurrentProject(project);
                assertFalse(cms.readProjectResources(project).contains(versions.m_current.getRootPath()));
                cms.copyResourceToProject(versions.argument(cms, stale));
                assertTrue(cms.readProjectResources(project).contains(versions.m_current.getRootPath()));
            } finally {
                cms.getRequestContext().setCurrentProject(original);
            }
        }
        assertUnchangedHeader(versions.m_current, readPersisted(cms, versions.m_current));
    }

    /**
     * Checks writing either kind of property.
     *
     * @param stale whether to use the stale argument
     * @param shared whether to write a shared resource property
     * @throws Exception if the test fails
     */
    private void checkProperties(boolean stale, boolean shared) throws Exception {

        CmsObject cms = getCmsObject();
        ResourceVersions versions = changeMetadata(cms, false);
        CmsProperty property = new CmsProperty("Title", shared ? null : "new title", shared ? "new title" : null);
        cms.writePropertyObjects(versions.argument(cms, stale), List.of(property));
        CmsResource actual = readPersisted(cms, versions.m_current);
        CmsProperty written = cms.readPropertyObject(actual, "Title", false);
        assertEquals("new title", shared ? written.getResourceValue() : written.getStructureValue());
        assertMetadata(versions.m_current, actual);
    }

    /**
     * Checks that relation changes do not restore the NEW state from before publication.
     *
     * @param stale whether to use the stale argument
     * @param add whether to add or delete the relation
     * @throws Exception if the test fails
     */
    private void checkRelation(boolean stale, boolean add) throws Exception {

        CmsObject cms = getCmsObject();
        String path = createFile(cms);
        CmsResource target = cms.readResource("/index.html");
        CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY);
        if (!add) {
            cms.addRelationToResource(cms.readResource(path), target, CmsRelationType.CATEGORY.getName());
        }
        CmsResource beforePublish = cms.readResource(path);
        assertEquals(CmsResource.STATE_NEW, beforePublish.getState());
        publishAndLock(cms, path);
        // A fresh relation operation must leave CHANGED as it is. This isolates an
        // unrelated state rollback from the normal UNCHANGED -> CHANGED transition.
        cms.setDateLastModified(path, CURRENT_MODIFIED, false);
        CmsResource current = cms.readResource(path);
        assertEquals(CmsResource.STATE_CHANGED, current.getState());
        ResourceVersions versions = new ResourceVersions(beforePublish, current);
        if (add) {
            cms.addRelationToResource(versions.argument(cms, stale), target, CmsRelationType.CATEGORY.getName());
        } else {
            cms.deleteRelationsFromResource(versions.argument(cms, stale), filter);
        }
        CmsResource actual = readPersisted(cms, current);
        assertEquals(add ? 1 : 0, cms.getRelationsForResource(actual, filter).size(), "requested relation change");
        assertEquals(CmsResource.STATE_CHANGED, actual.getState(), "already published resource must not become NEW");
    }

    /**
     * Checks that restriction changes preserve the current last-modified date.
     *
     * @param stale whether to use the stale argument
     * @param restrict whether to add or remove a restriction
     * @throws Exception if the test fails
     */
    private void checkRestricted(boolean stale, boolean restrict) throws Exception {

        CmsObject cms = getCmsObject();
        String path = createFile(cms);
        String group = OpenCms.getDefaultUsers().getGroupUsers();
        if (!restrict) {
            cms.setRestricted(cms.readResource(path), group, true);
        }
        cms.setDateLastModified(path, OLD_MODIFIED, false);
        CmsResource before = cms.readResource(path);
        cms.setDateLastModified(path, CURRENT_MODIFIED, false);
        CmsResource current = cms.readResource(path);
        ResourceVersions versions = new ResourceVersions(before, current);
        cms.setRestricted(versions.argument(cms, stale), group, restrict);
        CmsResource actual = readPersisted(cms, current);
        CmsUUID groupId = cms.readGroup(group).getId();
        boolean responsible = cms.getAccessControlEntries(path, false).stream().anyMatch(
            ace -> ace.getPrincipal().equals(groupId)
                && ((ace.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE) != 0));
        assertEquals(restrict, responsible, "requested restriction change");
        assertEquals(
            CURRENT_MODIFIED,
            actual.getDateLastModified(),
            "restriction change must preserve modification date");
    }

    /**
     * Creates a unique, locked file.
     *
     * @param cms the CMS context
     * @return the site path
     * @throws Exception if creation fails
     */
    private String createFile(CmsObject cms) throws Exception {

        String path = "/stale-resource-" + (++m_resourceCounter) + ".txt";
        cms.createResource(path, CmsResourceTypePlain.getStaticTypeId(), "old".getBytes(StandardCharsets.UTF_8), null);
        return path;
    }

    /**
     * Publishes a file and locks it again for the next operation.
     *
     * @param cms the CMS context
     * @param path the site path
     * @throws Exception if publication fails
     */
    private void publishAndLock(CmsObject cms, String path) throws Exception {

        cms.unlockResource(path);
        OpenCms.getPublishManager().publishResource(cms, path);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.lockResource(path);
        assertEquals(CmsResource.STATE_UNCHANGED, cms.readResource(path).getState(), "publication precondition");
    }

    /**
     * Reads the result after clearing resource caches, including deleted resources.
     *
     * @param cms the CMS context
     * @param resource the resource identity
     * @return the persisted resource
     * @throws Exception if reading fails
     */
    private CmsResource readPersisted(CmsObject cms, CmsResource resource) throws Exception {

        OpenCms.getMemoryMonitor().clearResourceCache();
        return cms.readResource(resource.getStructureId(), CmsResourceFilter.ALL);
    }
}
