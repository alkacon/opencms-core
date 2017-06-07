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

import org.opencms.i18n.CmsEncoder;
import org.opencms.setup.CmsSetupBean;

import java.io.ByteArrayInputStream;

import org.apache.xerces.impl.Version;
import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Test for the Xerces version.<p>
 *
 * @since 6.1.8
 */
public class CmsSetupTestXercesVersion implements I_CmsSetupTest {

    /** The test name. */
    public static final String TEST_NAME = "Xerces Version";

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#getName()
     */
    public String getName() {

        return TEST_NAME;
    }

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#execute(org.opencms.setup.CmsSetupBean)
     */
    public CmsSetupTestResult execute(CmsSetupBean setupBean) throws Exception {

        CmsSetupTestResult testResult = new CmsSetupTestResult(this);

        DOMParser parser = new DOMParser();
        String document = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<test>test</test>\n";
        parser.parse(new InputSource(new ByteArrayInputStream(document.getBytes(CmsEncoder.ENCODING_UTF_8))));
        Document doc = parser.getDocument();

        // Xerces 1 and 2 APIs are different, let's see what we have...
        String versionStr = null;
        int xercesVersion = 0;

        try {
            doc.getClass().getMethod("getXmlEncoding", new Class[] {}).invoke(doc, new Object[] {});
            versionStr = Version.getVersion();
            xercesVersion = 2;
        } catch (Throwable t) {
            // noop
        }
        if (versionStr == null) {
            try {
                doc.getClass().getMethod("getEncoding", new Class[] {}).invoke(doc, new Object[] {});
                versionStr = "Xerces version 1";
                xercesVersion = 1;
            } catch (Throwable t) {
                // noop
            }
        }

        switch (xercesVersion) {
            case 2:
                testResult.setResult(versionStr);
                testResult.setHelp(
                    "OpenCms requires Xerces version 2 to run. Usually this should be available as part of the servlet environment.");
                testResult.setGreen();
                break;
            case 1:
                testResult.setResult(versionStr);
                testResult.setRed();
                testResult.setInfo(
                    "OpenCms requires Xerces version 2 to run, your Xerces version is 1. "
                        + "Usually Xerces 2 should be installed by default as part of the servlet environment.");
                testResult.setHelp(testResult.getInfo());
                break;
            default:
                if (versionStr == null) {
                    versionStr = "Unknown version";
                }
                testResult.setResult(versionStr);
                testResult.setRed();
                testResult.setInfo(
                    "OpenCms requires Xerces version 2 to run. "
                        + "Usually Xerces 2 should be installed by default as part of the servlet environment.");
                testResult.setHelp(testResult.getInfo());
        }
        return testResult;
    }
}
