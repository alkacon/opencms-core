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

import org.dom4j.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

/**
 * Jupiter base class: boots OpenCms once per test class via
 * {@link OpenCmsTestEnvironment#setupOpenCms(String, String)} and tears it down via
 * {@link OpenCmsTestEnvironment#removeOpenCms(String)}.<p>
 *
 * Subclasses override {@link #getImportFolder()} and {@link #getTargetFolder()}
 * if they need a different fixture than {@code simpletest} at site root {@code /}.<p>
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class OpenCmsJupiterTestCase {

    public static final String C_AUML_LOWER = "\u00e4";
    public static final String C_AUML_UPPER = "\u00c4";
    public static final String C_EURO = "\u20ac";
    public static final String C_OUML_LOWER = "\u00f6";
    public static final String C_OUML_UPPER = "\u00d6";
    public static final String C_SHARP_S = "\u00df";
    public static final String C_UUML_LOWER = "\u00fc";
    public static final String C_UUML_UPPER = "\u00dc";




    /** The OpenCms context. */
    private CmsObject m_cms;

    /** The current test method name. */
    private String m_currentTestName;

    /** The OpenCms test environment. */
    protected final OpenCmsTestEnvironment m_testEnvironment = new OpenCmsTestEnvironment();

    /**
     * Default constructor.<p>
     */
    protected OpenCmsJupiterTestCase() {

    }

    /**
     * Returns the import fixture folder under {@code test/data/imports/}.<p>
     * Subclasses can override this to use a different fixture.
     *
     * @return import fixture folder name
     */
    protected String getImportFolder() {

        return "simpletest";
    }

    /**
     * Returns the VFS target folder for the import.<p>
     * Subclasses can override this to import into a different folder.
     *
     * @return VFS target folder
     */
    protected String getTargetFolder() {

        return "/";
    }

    /**
     * Returns the configuration folder.<p>
     * Subclasses can override this to use a different standard configuration folder.
     *
     * @return the configuration folder
     */
    protected String getConfigFolder() {

        return null;
    }

    /**
     * Returns the special configuration folder.<p>
     * Subclasses can override this to use a special configuration folder.
     *
     * @return the special configuration folder
     */
    protected String getSpecialConfigFolder() {

        return null;
    }

    /**
     * Returns the servlet mapping.<p>
     * Subclasses can override this.
     *
     * @return the servlet mapping
     */
    protected String getServletMapping() {

        return null;
    }

    /**
     * Returns the default web application name.<p>
     * Subclasses can override this.
     *
     * @return the default web application name
     */
    protected String getDefaultWebAppName() {

        return null;
    }

    /**
     * Returns the current test method name.<p>
     *
     * @return the current test method name
     */
    protected String getName() {

        return m_currentTestName;
    }

    /**
     * Returns whether to run the publish script during setup.<p>
     * Subclasses can override this.
     *
     * @return true to publish, false otherwise
     */
    protected boolean getPublish() {

        return true;
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
     * Boots OpenCms once before any test method in the class runs.<p>
     */
    @BeforeAll
    void setUpOpenCms() {

        if (shouldInitConfiguration()) {
            OpenCmsTestEnvironment.initConfiguration(shouldBreakOnErrorAfterInitConfiguration());
        }
        if (!shouldBootOpenCms()) {
            return;
        }
        m_cms = OpenCmsTestEnvironment.setupOpenCms(
            getImportFolder(),
            getTargetFolder(),
            getConfigFolder(),
            getSpecialConfigFolder(),
            getClass().getName(),
            getServletMapping(),
            getDefaultWebAppName(),
            getPublish());
    }

    /**
     * Shuts OpenCms down after all test methods in the class have run.<p>
     */
    @AfterAll
    void tearDownOpenCms() {

        if (!shouldBootOpenCms()) {
            return;
        }
        OpenCmsTestEnvironment.removeOpenCms(getClass().getName());
    }

    /**
     * Logs the start of a test method.<p>
     *
     * @param testInfo the test info
     */
    @BeforeEach
    void logTestStart(TestInfo testInfo) {

        m_currentTestName = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");
        System.out.println();
        System.out.println();
        System.out.println(" +------------------------------------------------------------------------------");
        System.out.println(" | Running OpenCms test case:");
        System.out.println(" | " + getClass().getName() + "#" + m_currentTestName);
        System.out.println(" +------------------------------------------------------------------------------");
        System.out.println();
        System.out.println();
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

    public void assertFilter(org.opencms.file.CmsObject cms, String resourceName, org.opencms.test.OpenCmsTestResourceFilter filter) throws org.opencms.main.CmsException {

        m_testEnvironment.assertFilter(cms, resourceName, filter);
    }

    public void assertProject(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsProject project) {

        m_testEnvironment.assertProject(cms, resourceName, project);
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

    public void assertUserLastModified(org.opencms.file.CmsObject cms, String resourceName, org.opencms.file.CmsUser user) {

        m_testEnvironment.assertUserLastModified(cms, resourceName, user);
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


    public void assertContains(String content, String pattern) {

        m_testEnvironment.assertContains(content, pattern);
    }

    public void createStorage(String name) {

        m_testEnvironment.createStorage(name);
    }

    public void switchStorage(String name) throws org.opencms.main.CmsException {

        m_testEnvironment.switchStorage(name);
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
     * Sets up the test database.<p>
     */
    protected void setupDatabase() {

        OpenCmsTestEnvironment.setupDatabase();
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
     * Sets up OpenCms for the current test.<p>
     *
     * @param importFolder the import folder
     * @param targetFolder the target folder
     *
     * @return the initialized CMS object
     */
    protected CmsObject setupOpenCms(String importFolder, String targetFolder) {

        return OpenCmsTestEnvironment.setupOpenCms(importFolder, targetFolder, null, null, getClass().getName(), true);
    }

    /**
     * Removes OpenCms for the current test.<p>
     */
    protected void removeOpenCms() {

        OpenCmsTestEnvironment.removeOpenCms(getClass().getName());
    }
}
