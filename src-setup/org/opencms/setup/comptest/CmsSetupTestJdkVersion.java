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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.comptest;

import org.opencms.setup.CmsSetupBean;

/**
 * Tests the version of the JDK.<p>
 *
 * @since 6.1.8
 */
public class CmsSetupTestJdkVersion implements I_CmsSetupTest {

    /** The test name. */
    public static final String TEST_NAME = "JDK Version";

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#getName()
     */
    public String getName() {

        return TEST_NAME;
    }

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#execute(org.opencms.setup.CmsSetupBean)
     */
    public CmsSetupTestResult execute(CmsSetupBean setupBean) {

        CmsSetupTestResult testResult = new CmsSetupTestResult(this);

        String requiredJDK = "1.4.0";
        String JDKVersion = System.getProperty("java.version");

        testResult.setResult(JDKVersion);

        boolean supportedJDK = compareJDKVersions(JDKVersion, requiredJDK);

        if (!supportedJDK) {
            testResult.setRed();
            testResult.setHelp(
                "OpenCms requires at least Java version " + requiredJDK + " to run. Please update your JDK");
        } else {
            testResult.setGreen();
        }
        return testResult;
    }

    /**
     * Checks if the used JDK is a higher version than the required JDK.<p>
     *
     * @param usedJDK The JDK version in use
     * @param requiredJDK The required JDK version
     *
     * @return true if used JDK version is equal or higher than required JDK version, false otherwise
     */
    private boolean compareJDKVersions(String usedJDK, String requiredJDK) {

        int compare = usedJDK.compareTo(requiredJDK);
        return (!(compare < 0));
    }
}
