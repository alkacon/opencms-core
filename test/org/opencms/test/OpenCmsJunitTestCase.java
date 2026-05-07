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

package org.opencms.test;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.setup.CmsSetupDb;

import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Jupiter base class: boots OpenCms once per test class via
 * {@link OpenCmsTestEnvironment#setupOpenCms(String, String)} and tears it down via
 * {@link OpenCmsTestEnvironment#removeOpenCms(String)}.<p>
 *
 * Subclasses can override {@link #openCmsSetUp()} and {@link #openCmsTearDown()}
 * to show their OpenCms setup explicitly.<p>
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class OpenCmsJunitTestCase {

    public static final String C_AUML_LOWER = "\u00e4";
    public static final String C_AUML_UPPER = "\u00c4";
    public static final String C_EURO = "\u20ac";
    public static final String DB_ORACLE = OpenCmsTestEnvironment.DB_ORACLE;
    public static final String C_OUML_LOWER = "\u00f6";
    public static final String C_OUML_UPPER = "\u00d6";
    public static final String C_SHARP_S = "\u00df";
    public static final String C_UUML_LOWER = "\u00fc";
    public static final String C_UUML_UPPER = "\u00dc";




    /** The OpenCms context. */
    private CmsObject m_cms;

    /** The OpenCms configuration initialization flag. */
    private boolean m_configurationInitialized;

    /** The current test method name. */
    private String m_currentTestName;

    /** The OpenCms test environment. */
    protected final OpenCmsTestEnvironment m_testEnvironment = new OpenCmsTestEnvironment();

    /**
     * Default constructor.<p>
     */
    protected OpenCmsJunitTestCase() {

    }

    /**
     * Returns the configured database product.<p>
     *
     * @return the configured database product
     */
    protected String getDatabaseProduct() {

        return m_testEnvironment.getDatabaseProduct();
    }

    /**
     * Returns the Admin {@link CmsObject} produced by setup.<p>
     *
     * @return the Admin {@link CmsObject} in the Offline project
     */
    protected CmsObject getCmsObject() {

        try {
            return m_testEnvironment.getCmsObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether OpenCms should be booted for this test class.<p>
     * Subclasses can override this to prevent OpenCms from booting.
     *
     * @return true to boot OpenCms, false otherwise
     */
    protected boolean shouldBootOpenCms() {

        return true;
    }

    /**
     * Returns whether OpenCms properties should be initialized for this test class.<p>
     * Subclasses can override this.
     *
     * @return true to init configuration, false otherwise
     */
    protected boolean shouldInitConfiguration() {

        return shouldBootOpenCms(); // Default to boot OpenCms -> init config
    }

    /**
     * Returns whether logged errors should fail the test after configuration-only setup.<p>
     * Subclasses can override this to preserve legacy tests which intentionally exercise
     * code paths that log errors without failing.
     *
     * @return true if logged errors should fail the test after init, false otherwise
     */
    protected boolean shouldBreakOnErrorAfterInitConfiguration() {

        return true;
    }

    /**
     * Returns whether the standard per-test start header should be logged from the base
     * {@link BeforeEach} callback.<p>
     *
     * Subclasses can override this if they need to preserve a legacy setup-first output order
     * and log the header themselves after custom setup.
     *
     * @return true to log from the base callback, false otherwise
     */
    protected boolean shouldLogTestStartBeforeEach() {

        return true;
    }

    /**
     * Boots OpenCms once before any test method in the class runs.<p>
     */
    @BeforeAll
    protected void openCmsSetUp() {

        if (!shouldBootOpenCms()) {
            initOpenCmsConfiguration();
            return;
        }
    }

    /**
     * Shuts OpenCms down after all test methods in the class have run.<p>
     */
    @AfterAll
    protected void openCmsTearDown() {

        if (m_cms != null) {
            removeOpenCms();
            m_cms = null;
        }
    }

    /**
     * Logs the start of a test method.<p>
     *
     * @param testInfo the test info
     */
    @BeforeEach
    void logTestStart(TestInfo testInfo) {

        prepareCurrentTest(testInfo);
        if (!shouldLogTestStartBeforeEach()) {
            return;
        }
        printTestStart(testInfo);
    }

    /**
     * Prepares the current test method name.<p>
     *
     * @param testInfo the test info
     */
    protected final void prepareCurrentTest(TestInfo testInfo) {

        m_currentTestName = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");
    }

    /**
     * Prints the standard test start header.<p>
     *
     * @param testInfo the test info
     */
    protected final void printTestStart(TestInfo testInfo) {

        String displayName = testInfo.getDisplayName();
        String logTestName = displayName.equals(m_currentTestName + "()") ? m_currentTestName : displayName;
        System.out.println();
        System.out.println();
        System.out.println(" +------------------------------------------------------------------------------");
        System.out.println(" | Running OpenCms test case:");
        System.out.println(" | " + getClass().getName() + "#" + logTestName);
        System.out.println(" +------------------------------------------------------------------------------");
        System.out.println();
        System.out.println();
    }

    /**
     * Initializes the OpenCms configuration for the current test class.<p>
     */
    protected void initOpenCmsConfiguration() {

        if (!shouldInitConfiguration() || m_configurationInitialized) {
            return;
        }
        OpenCmsTestEnvironment.initConfiguration(shouldBreakOnErrorAfterInitConfiguration());
        m_configurationInitialized = true;
    }

    /**
     * Logs the end of a test method.<p>
     */
    @AfterEach
    void logTestEnd() {

        // No-op for now, legacy runTest() only logs start.
    }

    /**
     * Prints a message to system out.<p>
     *
     * @param obj the message to print
     */
    protected void echo(Object obj) {

        OpenCmsTestEnvironment.echo(String.valueOf(obj));
    }

    protected void printExceptionWarning() {

        OpenCmsTestEnvironment.printExceptionWarning();
    }

    /**
     * Asserts that two XML documents are equal.<p>
     *
     * @param expected the expected document
     * @param actual the actual document
     */
    public static void assertXmlEquals(Document expected, Document actual) {

        assertXmlEquals(expected, actual, null);
    }

    /**
     * Asserts that two XML documents are equal.<p>
     *
     * @param expected the expected document
     * @param actual the actual document
     * @param message the message to output on failure
     */
    public static void assertXmlEquals(Document expected, Document actual, String message) {

        String errorMsg = OpenCmsTestEnvironment.compareXmlDocuments(expected, actual);
        if (errorMsg != null) {
            if (message != null) {
                Assertions.fail(message + " ==> " + errorMsg);
            } else {
                Assertions.fail(errorMsg);
            }
        }
    }

    public void storeResources(org.opencms.file.CmsObject cms, String resourceName) {

        m_testEnvironment.storeResources(cms, resourceName);
    }

    public void storeResources(org.opencms.file.CmsObject cms, String resourceName, boolean storeSubresources) {

        m_testEnvironment.storeResources(cms, resourceName, storeSubresources);
    }

    public void assertFilter(org.opencms.file.CmsObject cms, String resourceName, org.opencms.test.OpenCmsTestResourceFilter filter) throws org.opencms.main.CmsException {

        m_testEnvironment.assertFilter(cms, resourceName, filter);
    }

    public void assertFilter(
        org.opencms.file.CmsObject cms,
        org.opencms.file.CmsResource resource,
        org.opencms.test.OpenCmsTestResourceFilter filter) {

        m_testEnvironment.assertFilter(cms, resource, filter);
    }

    public void assertFilter(
        org.opencms.file.CmsObject cms,
        String resourceName1,
        String resourceName2,
        org.opencms.test.OpenCmsTestResourceFilter filter) {

        m_testEnvironment.assertFilter(cms, resourceName1, resourceName2, filter);
    }

    public void assertProject(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsProject project) {

        m_testEnvironment.assertProject(cms, resourceName, project);
    }

    public void assertAce(
        org.opencms.file.CmsObject cms,
        String resourceName,
        org.opencms.security.CmsAccessControlEntry ace) {

        m_testEnvironment.assertAce(cms, resourceName, ace);
    }

    public void assertAcl(
        org.opencms.file.CmsObject cms,
        String resourceName,
        org.opencms.util.CmsUUID principal,
        org.opencms.security.CmsPermissionSet permission) {

        m_testEnvironment.assertAcl(cms, resourceName, principal, permission);
    }

    public void assertAcl(
        org.opencms.file.CmsObject cms,
        String modifiedResource,
        String resourceName,
        org.opencms.util.CmsUUID principal,
        org.opencms.security.CmsPermissionSet permission) {

        m_testEnvironment.assertAcl(cms, modifiedResource, resourceName, principal, permission);
    }

    public void assertState(org.opencms.file.CmsObject cms, String resourceName, org.opencms.db.CmsResourceState state) {

        m_testEnvironment.assertState(cms, resourceName, state);
    }

    public org.opencms.db.CmsResourceState getPreCalculatedState(String resourceName) throws Exception {

        return m_testEnvironment.getPreCalculatedState(resourceName);
    }

    public void assertDateLastModifiedAfter(org.opencms.file.CmsObject cms, String resourceName, long dateLastModified) {

        m_testEnvironment.assertDateLastModifiedAfter(cms, resourceName, dateLastModified);
    }

    public void assertDateLastModified(org.opencms.file.CmsObject cms, String resourceName, long dateLastModified) {

        m_testEnvironment.assertDateLastModified(cms, resourceName, dateLastModified);
    }

    public void assertDateCreatedAfter(org.opencms.file.CmsObject cms, String resourceName, long dateCreated) {

        m_testEnvironment.assertDateCreatedAfter(cms, resourceName, dateCreated);
    }

    public void assertDateCreated(org.opencms.file.CmsObject cms, String resourceName, long dateCreated) {

        m_testEnvironment.assertDateCreated(cms, resourceName, dateCreated);
    }

    public void assertDateContent(org.opencms.file.CmsObject cms, String resourceName, long dateContent) {

        m_testEnvironment.assertDateContent(cms, resourceName, dateContent);
    }

    public void assertDateContentAfter(org.opencms.file.CmsObject cms, String resourceName, long dateContent) {

        m_testEnvironment.assertDateContentAfter(cms, resourceName, dateContent);
    }

    public void assertContent(org.opencms.file.CmsObject cms, String resourceName, byte[] content) {

        m_testEnvironment.assertContent(cms, resourceName, content);
    }

    public void assertIsFolder(org.opencms.file.CmsObject cms, String resourceName) {

        m_testEnvironment.assertIsFolder(cms, resourceName);
    }

    public void assertUserLastModified(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsUser user) {

        m_testEnvironment.assertUserLastModified(cms, resourceName, user);
    }

    public void assertUserCreated(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsUser user) {

        m_testEnvironment.assertUserCreated(cms, resourceName, user);
    }

    public void assertFlags(org.opencms.file.CmsObject cms, String resourceName, int flag) {

        m_testEnvironment.assertFlags(cms, resourceName, flag);
    }

    public void assertResourceType(org.opencms.file.CmsObject cms, String resourceName, int resourceType) {

        m_testEnvironment.assertResourceType(cms, resourceName, resourceType);
    }

    public void assertThrows(String message, OpenCmsTestEnvironment.CodeBlock block) {

        m_testEnvironment.assertThrows(message, block);
    }

    public void assertVersion(org.opencms.file.CmsObject cms, String resourceName, int version) {

        m_testEnvironment.assertVersion(cms, resourceName, version);
    }

    public void assertHistory(org.opencms.file.CmsObject cms, String resourceName, int versionCount) throws Exception {

        m_testEnvironment.assertHistory(cms, resourceName, versionCount);
    }

    public void assertHistoryForRestored(org.opencms.file.CmsObject cms, String resourceName, int versionCount)
    throws Exception {

        m_testEnvironment.assertHistoryForRestored(cms, resourceName, versionCount);
    }

    public void assertPropertyNew(org.opencms.file.CmsObject cms, String resourceName, java.util.List<org.opencms.file.CmsProperty> excludeList) {

        m_testEnvironment.assertPropertyNew(cms, resourceName, excludeList);
    }

    public void assertPropertyNew(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsProperty property) {

        m_testEnvironment.assertPropertyNew(cms, resourceName, property);
    }

    public void assertPropertyRemoved(org.opencms.file.CmsObject cms, String resourceName, java.util.List<org.opencms.file.CmsProperty> excludeList) {

        m_testEnvironment.assertPropertyRemoved(cms, resourceName, excludeList);
    }

    public void assertPropertyRemoved(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsProperty property) {

        m_testEnvironment.assertPropertyRemoved(cms, resourceName, property);
    }

    public void assertPropertyChanged(org.opencms.file.CmsObject cms, String resourceName, java.util.List<org.opencms.file.CmsProperty> excludeList) {

        m_testEnvironment.assertPropertyChanged(cms, resourceName, excludeList);
    }

    public void assertPropertyChanged(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsProperty property) {

        m_testEnvironment.assertPropertyChanged(cms, resourceName, property);
    }

    public void assertPropertydefinitionExist(
        org.opencms.file.CmsObject cms,
        org.opencms.file.CmsPropertyDefinition propertyDefinition) {

        m_testEnvironment.assertPropertydefinitionExist(cms, propertyDefinition);
    }

    public void assertPropertydefinitions(
        org.opencms.file.CmsObject cms,
        java.util.List propertyDefinitions,
        org.opencms.file.CmsPropertyDefinition propertyDefinition) {

        m_testEnvironment.assertPropertydefinitions(cms, propertyDefinitions, propertyDefinition);
    }


    public void assertContains(String content, String pattern) {

        m_testEnvironment.assertContains(content, pattern);
    }

    public void createStorage(String name) {

        m_testEnvironment.createStorage(name);
    }

    public void switchStorage(String name) throws org.opencms.main.CmsException {

        m_testEnvironment.switchStorage(name);
    }

    public void setMapping(String source, String target) {

        m_testEnvironment.setMapping(source, target);
    }

    public void resetMapping() {

        m_testEnvironment.resetMapping();
    }

    public int getCurrentResourceStorageSize() {

        return m_testEnvironment.m_currentResourceStrorage.size();
    }

    public void assertRelation(org.opencms.relations.CmsRelation expected, org.opencms.relations.CmsRelation actual) {

        m_testEnvironment.assertRelation(expected, actual);
    }

    public void assertLock(CmsObject cms, String resourceName) {

        m_testEnvironment.assertLock(cms, resourceName);
    }

    public void assertLock(CmsObject cms, String resourceName, org.opencms.lock.CmsLockType lockType) {

        m_testEnvironment.assertLock(cms, resourceName, lockType);
    }

    public void assertLock(CmsObject cms, String resourceName, org.opencms.lock.CmsLockType lockType, org.opencms.file.CmsUser user) {

        m_testEnvironment.assertLock(cms, resourceName, lockType, user);
    }

    public void assertModifiedInCurrentProject(CmsObject cms, String resourceName, boolean shouldHaveRedFlag) {

        m_testEnvironment.assertModifiedInCurrentProject(cms, resourceName, shouldHaveRedFlag);
    }

    public void assertPermissionString(
        CmsObject cms,
        String resourceName,
        org.opencms.file.CmsUser user,
        String expectedPermissionString) throws org.opencms.main.CmsException {

        m_testEnvironment.assertPermissionString(cms, resourceName, user, expectedPermissionString);
    }

    public void assertSiblingCount(CmsObject cms, String resourceName, int count) {

        m_testEnvironment.assertSiblingCount(cms, resourceName, count);
    }

    public void assertSiblingCountIncremented(CmsObject cms, String resourceName, int increment) {

        m_testEnvironment.assertSiblingCountIncremented(cms, resourceName, increment);
    }

    public void assertDateReleased(CmsObject cms, String resourceName, long dateReleased) {

        m_testEnvironment.assertDateReleased(cms, resourceName, dateReleased);
    }

    /**
     * Deletes a VFS resource, locking it first if necessary.<p>
     *
     * @param path the resource path
     * @throws org.opencms.main.CmsException if something goes wrong
     */
    protected void delete(String path) throws org.opencms.main.CmsException {

        CmsObject cms = getCmsObject();
        if (cms.existsResource(path)) {
            org.opencms.lock.CmsLock lock = cms.getLock(path);
            if (lock.isUnlocked() || !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                cms.lockResource(path);
            }
            cms.deleteResource(path, org.opencms.file.CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
    }

    /**
     * Deletes a file from the RFS.<p>
     *
     * @param absolutePath the absolute path to delete
     */
    protected void deleteFile(String absolutePath) {

        m_testEnvironment.deleteFile(absolutePath);
    }

    /**
     * Sets up the test database.<p>
     */
    protected void setupDatabase() {

        OpenCmsTestEnvironment.setupDatabase();
    }

    /**
     * Returns the configured database product key.<p>
     *
     * @return the configured database product key
     */
    protected String getDbProduct() {

        return OpenCmsTestEnvironment.getDbProduct();
    }

    /**
     * Returns a setup DB instance using the default database connection.<p>
     *
     * @return the setup DB instance
     */
    protected CmsSetupDb getSetupDbForDefaultConnection() {

        return OpenCmsTestEnvironment.getSetupDb(OpenCmsTestEnvironment.m_defaultConnection);
    }

    /**
     * Returns a setup DB instance using the setup database connection.<p>
     *
     * @return the setup DB instance
     */
    protected CmsSetupDb getSetupDbForSetupConnection() {

        return OpenCmsTestEnvironment.getSetupDb(OpenCmsTestEnvironment.m_setupConnection);
    }

    /**
     * Returns the replacer map for the default database connection.<p>
     *
     * @return the replacer map
     */
    protected java.util.Map<String, String> getDefaultConnectionReplacer() {

        return OpenCmsTestEnvironment.getReplacer(OpenCmsTestEnvironment.m_defaultConnection);
    }

    /**
     * Checks the setup DB instance for errors.<p>
     *
     * @param setupDb the setup DB instance
     */
    protected void checkErrors(CmsSetupDb setupDb) {

        OpenCmsTestEnvironment.checkErrors(setupDb);
    }

    /**
     * Removes the test database.<p>
     */
    protected void removeDatabase() {

        OpenCmsTestEnvironment.removeDatabase();
    }

    /**
     * Imports resources into the VFS.<p>
     *
     * @param cms the CMS context
     * @param importFile the import folder
     * @param targetPath the target path
     *
     * @throws org.opencms.main.CmsException if importing fails
     */
    protected void importResources(CmsObject cms, String importFile, String targetPath) throws org.opencms.main.CmsException {

        OpenCmsTestEnvironment.importResources(cms, importFile, targetPath);
    }

    /**
     * Imports a module from the test package path.<p>
     *
     * @param cms the CMS context
     * @param moduleName the module name
     *
     * @throws org.opencms.main.CmsException if importing fails
     */
    protected void importModule(CmsObject cms, String moduleName) throws org.opencms.main.CmsException {

        OpenCmsTestEnvironment.importModule(cms, moduleName);
    }

    /**
     * Imports a module from the test package path using a suffix and subfolder.<p>
     *
     * @param cms the CMS context
     * @param moduleName the module name
     * @param suffix the module file suffix
     * @param subfolder the module subfolder
     *
     * @throws org.opencms.main.CmsException if importing fails
     */
    protected void importModule(CmsObject cms, String moduleName, String suffix, String subfolder)
    throws org.opencms.main.CmsException {

        OpenCmsTestEnvironment.importModule(cms, moduleName, suffix, subfolder);
    }

    /**
     * Imports a test resource into the VFS.<p>
     *
     * @param cms the CMS context
     * @param rfsPath the resource path on the classpath
     * @param vfsPath the VFS target path
     * @param type the resource type id
     * @param properties the resource properties
     *
     * @return the imported resource
     *
     * @throws Exception if importing fails
     */
    protected org.opencms.file.CmsResource importTestResource(
        CmsObject cms,
        String rfsPath,
        String vfsPath,
        int type,
        java.util.List<org.opencms.file.CmsProperty> properties)
    throws Exception {

        return OpenCmsTestEnvironment.importTestResource(cms, rfsPath, vfsPath, type, properties);
    }

    /**
     * Tests if the given publish jobs are internally equal.<p>
     *
     * @param j1 first job to compare
     * @param j2 second job to compare
     * @param comparePublishLists if the publish lists should be compared, too
     * @param compareTime if the timestamps should be compared, too
     */
    protected void assertPublishJobEquals(
        org.opencms.publish.CmsPublishJobBase j1,
        org.opencms.publish.CmsPublishJobBase j2,
        boolean comparePublishLists,
        boolean compareTime) {

        m_testEnvironment.assertEquals(j1, j2, comparePublishLists, compareTime);
    }

    /**
     * Returns the test data path for the given file name.<p>
     *
     * @param filename the file name
     *
     * @return the test data path
     */
    protected String getTestDataPath(String filename) {

        return OpenCmsTestEnvironment.getTestDataPath(filename);
    }

    /**
     * Sets up a complete OpenCms instance with configuration from the config-ori folder,
     * creating the usual projects, and importing a default database.<p>
     *
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/"
     */
    public CmsObject setupOpenCms(String importFolder, String targetFolder) {

        return setupOpenCms(importFolder, targetFolder, null, null, null, true);
    }

    /**
     * Sets up a complete OpenCms instance with configuration from the config-ori folder,
     * creating the usual projects, and importing a default database.<p>
     *
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param publish flag to signalize if the publish script should be called
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/"
     */
    public CmsObject setupOpenCms(String importFolder, String targetFolder, boolean publish) {

        return setupOpenCms(importFolder, targetFolder, null, null, null, publish);
    }

    /**
     * Sets up a complete OpenCms instance with configuration from the config-ori folder,
     * creating the usual projects, and importing a default database.<p>
     *
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param specialConfigFolder the folder that contains the special configuration files for this setup
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/"
     */
    public CmsObject setupOpenCms(String importFolder, String targetFolder, String specialConfigFolder) {

        return setupOpenCms(importFolder, targetFolder, null, specialConfigFolder, null, true);
    }

    /**
     * Sets up a complete OpenCms instance, creating the usual projects,
     * and importing a default database.<p>
     *
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param configFolder the folder to copy the configuration files
     * @param publish publish only if set
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/"
     */
    public CmsObject setupOpenCms(
        String importFolder,
        String targetFolder,
        String configFolder,
        boolean publish) {

        return setupOpenCms(importFolder, targetFolder, configFolder, null, null, publish);
    }

    /**
     * Sets up a complete OpenCms instance with configuration from the config-ori folder,
     * creating the usual projects, and importing a default database.<p>
     *
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param specialConfigFolder the folder that contains the special configuration files for this setup
     * @param testName the name of the test class (for writing it to the console)
     *
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/"
     */
    public CmsObject setupOpenCms(
        String importFolder,
        String targetFolder,
        String specialConfigFolder,
        String testName) {

        return setupOpenCms(importFolder, targetFolder, null, specialConfigFolder, testName, true);
    }

    /**
     * Sets up a complete OpenCms instance, creating the usual projects,
     * and importing a default database.<p>
     *
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param configFolder the folder to copy the standard configuration files from
     * @param specialConfigFolder the folder that contains the special configuration fiiles for this setup
     * @param testName the name of the test class (for writing it to the console)
     * @param publish publish only if set
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/"
     */
    public CmsObject setupOpenCms(
        String importFolder,
        String targetFolder,
        String configFolder,
        String specialConfigFolder,
        String testName,
        boolean publish) {

        return setupOpenCms(
            importFolder,
            targetFolder,
            configFolder,
            specialConfigFolder,
            testName ,
            null,
            null,
            publish);
    }

    /**
     * Sets up a complete OpenCms instance, creating the usual projects,
     * and importing a default database.<p>
     *
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param configFolder the folder to copy the standard configuration files from
     * @param specialConfigFolder the folder that contains the special configuration fiiles for this setup
     * @param testName the name of the test class (for writing it to the console)
     * @param servletMapping The servlet mapping used by the OpenCms shell. Default: "/opencms/*".
     * @param defaultWebAppName The default webapp name assumed by the OpenCms shell. Default: "ROOT".
     * @param publish publish only if set
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/"
     */
    public CmsObject setupOpenCms(
        String importFolder,
        String targetFolder,
        String configFolder,
        String specialConfigFolder,
        String testName,
        String servletMapping,
        String defaultWebAppName,
        boolean publish) {

        initOpenCmsConfiguration();
        m_cms = OpenCmsTestEnvironment.setupOpenCms(
            importFolder,
            targetFolder,
            configFolder,
            specialConfigFolder,
            testName == null ? getClass().getName() : testName,
            servletMapping,
            defaultWebAppName,
            publish);
        return m_cms;
    }

    /**
     * Removes OpenCms for the current test.<p>
     */
    protected void removeOpenCms() {

        OpenCms.getPublishManager().waitWhileRunning();
        OpenCmsTestEnvironment.removeOpenCms(getClass().getName());
    }
}
