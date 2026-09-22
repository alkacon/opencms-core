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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsUser;
import org.opencms.main.OpenCmsCore;
import org.opencms.security.CmsDefaultPasswordHandler;
import org.opencms.security.CmsDefaultValidationHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests CSV user import parsing.<p>
 */
public class TestCmsCsvImportUtils extends TestCase {

    /** Default password for parser tests. */
    private static final String DEFAULT_PASSWORD = "generated";

    /**
     * Sets a private field value.<p>
     *
     * @param target the target object
     * @param fieldName the field name
     * @param value the field value
     *
     * @throws Exception if setting the field fails
     */
    private static void setField(Object target, String fieldName, Object value) throws Exception {

        Field field = OpenCmsCore.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Tests empty user name rejection.<p>
     */
    public void testEmptyUserName() {

        IllegalArgumentException exception = expectIllegalArgument("name;firstname;lastname\n;Ana;López", true);

        assertEquals("CSV line 2 does not contain a user name.", exception.getMessage());
    }

    /**
     * Tests missing name column rejection.<p>
     */
    public void testMissingNameColumn() {

        IllegalArgumentException exception = expectIllegalArgument(
            "firstname;lastname;email\nAna;López;ana@example.com",
            true);

        assertEquals("The mandatory CSV column 'name' is missing.", exception.getMessage());
    }

    /**
     * Tests token normalization.<p>
     */
    public void testNormalizeToken() {

        assertNull(CmsCsvImportUtils.normalizeToken(null));
        assertEquals("name", CmsCsvImportUtils.normalizeToken("\ufeffname"));
        assertEquals("name", CmsCsvImportUtils.normalizeToken("\"name\""));
        assertEquals("name", CmsCsvImportUtils.normalizeToken("\ufeff\"name\""));
    }

    /**
     * Tests password import handling.<p>
     */
    public void testPasswordImportHandling() {

        CmsUser importedPassword = readOne("name;password\nuser1;csvPassword", true);
        CmsUser defaultPassword = readOne("name;password\nuser1;csvPassword", false);

        assertEquals("csvPassword", importedPassword.getPassword());
        assertEquals(DEFAULT_PASSWORD, defaultPassword.getPassword());
    }

    /**
     * Tests additional CSV columns.<p>
     */
    public void testReadAdditionalColumn() {

        CmsUser user = readOne("name;firstname;customField\nuser1;Ana;customValue");

        assertEquals("Ana", user.getFirstname());
        assertEquals("customValue", user.getAdditionalInfo("customField"));
    }

    /**
     * Tests UTF-8 CSV with BOM and unquoted headers.<p>
     */
    public void testReadBomWithoutQuotedHeader() {

        CmsUser user = readOne("\ufeffname;firstname;lastname;email;password\nuser1;Ana;López;ana@example.com;");

        assertEquals("user1", user.getName());
        assertEquals("Ana", user.getFirstname());
    }

    /**
     * Tests UTF-8 CSV with BOM and quoted headers.<p>
     */
    public void testReadBomWithQuotedHeader() {

        CmsUser user = readOne(
            "\ufeff\"name\";\"firstname\";\"lastname\";\"email\";\"password\"\nuser1;Ana;López;ana@example.com;");

        assertEquals("user1", user.getName());
        assertEquals("Ana", user.getFirstname());
    }

    /**
     * Tests incomplete CSV rows.<p>
     */
    public void testReadIncompleteRow() {

        CmsUser user = readOne("name;firstname;lastname;email\nuser1;Ana");

        assertEquals("user1", user.getName());
        assertEquals("Ana", user.getFirstname());
        assertEquals("", user.getLastname());
        assertEquals("", user.getEmail());
    }

    /**
     * Tests UTF-8 CSV without BOM.<p>
     */
    public void testReadUtf8WithoutBom() {

        CmsUser user = readOne("name;firstname;lastname;email;password\nuser1;Ana;López;ana@example.com;");

        assertEquals("user1", user.getName());
        assertEquals("Ana", user.getFirstname());
        assertEquals("López", user.getLastname());
        assertEquals("ana@example.com", user.getEmail());
        assertEquals(DEFAULT_PASSWORD, user.getPassword());
    }

    /**
     * Tests CSV without password column.<p>
     */
    public void testReadWithoutPasswordColumn() {

        CmsUser user = readOne("name;firstname;lastname;email\nuser1;Ana;López;ana@example.com");

        assertEquals(DEFAULT_PASSWORD, user.getPassword());
    }

    /**
     * Installs the minimal OpenCms handlers needed by CmsUser setters.<p>
     *
     * @throws Exception if handler setup fails
     */
    @Override
    protected void setUp() throws Exception {

        super.setUp();
        Method getInstance = OpenCmsCore.class.getDeclaredMethod("getInstance");
        getInstance.setAccessible(true);
        Object core = getInstance.invoke(null);
        setField(core, "m_validationHandler", new CmsDefaultValidationHandler());
        setField(core, "m_passwordHandler", new CmsDefaultPasswordHandler());
    }

    /**
     * Expects CSV parsing to fail with an illegal argument exception.<p>
     *
     * @param csv the CSV text
     * @param keepPasswordIfPossible true if CSV passwords should be imported
     *
     * @return the thrown exception
     */
    private IllegalArgumentException expectIllegalArgument(String csv, boolean keepPasswordIfPossible) {

        try {
            readUsers(csv, keepPasswordIfPossible);
            fail("Expected IllegalArgumentException.");
            return null;
        } catch (IllegalArgumentException e) {
            return e;
        }
    }

    /**
     * Reads one user from CSV.<p>
     *
     * @param csv the CSV text
     *
     * @return the parsed user
     */
    private CmsUser readOne(String csv) {

        return readOne(csv, true);
    }

    /**
     * Reads one user from CSV.<p>
     *
     * @param csv the CSV text
     * @param keepPasswordIfPossible true if CSV passwords should be imported
     *
     * @return the parsed user
     */
    private CmsUser readOne(String csv, boolean keepPasswordIfPossible) {

        List<CmsUser> users = readUsers(csv, keepPasswordIfPossible);
        assertEquals(1, users.size());
        return users.get(0);
    }

    /**
     * Reads users from CSV.<p>
     *
     * @param csv the CSV text
     * @param keepPasswordIfPossible true if CSV passwords should be imported
     *
     * @return the parsed users
     */
    private List<CmsUser> readUsers(String csv, boolean keepPasswordIfPossible) {

        return CmsCsvImportUtils.readUsers(
            csv.getBytes(StandardCharsets.UTF_8),
            DEFAULT_PASSWORD,
            keepPasswordIfPossible);
    }
}
