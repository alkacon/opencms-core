/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ugc;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.ugc.shared.CmsUgcException;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Optional;

import junit.framework.Test;

/**
 * Test cases for the org.opencms.editors.usergenerated package.
 */
public class TestFormSessionSecurityLimits extends OpenCmsTestCase {

    /**
     * Creates a new test instance.<p<
     *
     * @param name the test name
     */
    public TestFormSessionSecurityLimits(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestFormSessionSecurityLimits.class, "systemtest", "/");
    }

    /**
     * Tests that an error occurs when trying to upload without a configured upload folder.<p>
     *
     * @throws Exception -
     */
    public void testErrorNoUploadsAllowed() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        CmsResource rootFolder = cms.readResource("/");
        CmsGroup administrators = cms.readGroup("Administrators");
        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "plain",
            rootFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.<CmsResource> absent(),
            Optional.<Long> absent(),
            Optional.of(Integer.valueOf(100)),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.of(Arrays.asList(".jpg", ".PNG")));

        try {
            CmsUgcSessionSecurityUtil.checkCreateUpload(cms, config, "foo.png", 100L);
            fail("Exception should have been thrown!");
        } catch (CmsUgcException e) {
            // empty
        }

    }

    /**
     * Tests that errors are thrown when the configured upload / content limits in the configuration are violated.<p>
     *
     * @throws Exception -
     */
    public void testLimits() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");

        CmsResource uploadFolder = cms.createResource("/uploads", 0);
        CmsResource rootFolder = cms.readResource("/");
        CmsGroup administrators = cms.readGroup("Administrators");
        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "plain",
            rootFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.of(uploadFolder),
            Optional.of(Long.valueOf(10000)),
            Optional.of(Integer.valueOf(0)),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.of(Arrays.asList(".jpg", ".PNG")));
        try {
            CmsUgcSessionSecurityUtil.checkCreateUpload(cms, config, "foo.png", 50000);
            fail("Exception should have been thrown!");
        } catch (CmsUgcException e) {
            // empty
        }

        try {
            CmsUgcSessionSecurityUtil.checkCreateUpload(cms, config, "foo.png", 3);
        } catch (CmsUgcException e) {
            fail("Exception was thrown: " + e.getLocalizedMessage());
        }

        try {
            CmsUgcSessionSecurityUtil.checkCreateUpload(cms, config, "foo.doc", 100L);
            fail("Exception should have been thrown!");
        } catch (CmsUgcException e) {
            // empty

        }

        try {
            CmsUgcSessionSecurityUtil.checkCreateUpload(cms, config, "foo.JPG", 100L);
        } catch (CmsUgcException e) {
            fail("Exceptikon was thrown: " + e.getLocalizedMessage());
        }

        try {
            CmsUgcSessionSecurityUtil.checkCreateContent(cms, config);
            fail("Exception should have been thrown!");
        } catch (CmsUgcException e) {
            // empty

        }

        config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "plain",
            rootFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.of(uploadFolder),
            Optional.<Long> absent(),
            Optional.of(Integer.valueOf(100)),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.of(Arrays.asList(".jpg", ".PNG")));

        try {
            CmsUgcSessionSecurityUtil.checkCreateContent(cms, config);
        } catch (CmsUgcException e) {
            fail("Exception was thrown: " + e.getLocalizedMessage());
        }

    }

    /**
     * Tests that errors are not thrown if no limits are configured in the form configuration.<p>
     *
     * @throws Exception -
     */
    public void testNoLimits() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");

        CmsResource uploadFolder = cms.createResource("/uploads2", 0);
        CmsResource rootFolder = cms.readResource("/");
        CmsGroup administrators = cms.readGroup("Administrators");
        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "plain",
            rootFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.of(uploadFolder),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());

        try {
            CmsUgcSessionSecurityUtil.checkCreateUpload(cms, config, "foo.aasdfasdfasdf", 99999999L);
        } catch (CmsUgcException e) {
            fail("Exception was thrown: " + e);
        }
    }
}
