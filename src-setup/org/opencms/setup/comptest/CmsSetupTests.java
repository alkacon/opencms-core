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
import org.opencms.setup.CmsSetupDb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Runs various tests to give users infos about whether their system is compatible to OpenCms.<p>
 *
 * @since 6.0.0
 */
public class CmsSetupTests {

    /** Flag indicating tests where successful. */
    private boolean m_green;

    /** Flag indicating tests where not successful. */
    private boolean m_red;

    /** The test results. */
    private List<CmsSetupTestResult> m_testResults;

    /** Indicating there should be a warning. */
    private boolean m_yellow;

    /**
     * Creates a new setup test suite.<p>
     */
    public CmsSetupTests() {

        super();
    }

    /**
     * Returns a list of all available tests.<p>
     *
     * @return a list of all available tests
     */
    public List<I_CmsSetupTest> getAllTests() {

        List<I_CmsSetupTest> tests = new ArrayList<I_CmsSetupTest>();
        tests.add(new CmsSetupTestFolderPermissions());
        tests.add(new CmsSetupTestJdkVersion());
        tests.add(new CmsSetupTestJavaTempDir());
        tests.add(new CmsSetupTestOperatingSystem());
        tests.add(new CmsSetupTestServletContainer());
        tests.add(new CmsSetupTestSimapi());
        tests.add(new CmsSetupTestWarFileUnpacked());
        tests.add(new CmsSetupTestXercesVersion());
        tests.add(new CmsSetupTestXmlAPI());
        return tests;
    }

    /**
     * Returns the test results.<p>
     *
     * @return the test results
     */
    public List<CmsSetupTestResult> getTestResults() {

        return m_testResults;
    }

    /**
     * Returns true, if the conditions in all tests were fulfilled.<p>
     *
     * @return true, if the conditions in all tests were fulfilled
     */
    public boolean isGreen() {

        return m_green;
    }

    /**
     * Returns true if one of the tests found a violated condition.
     * It is assumed that it will be impossible to run OpenCms.<p>
     *
     * @return true if one of the tests violates a condition
     */
    public boolean isRed() {

        return m_red;
    }

    /**
     * Returns true if one of the tests found a questionable condition.
     * It is possible that OpenCms will not run.<p>
     *
     * @return true if one of the tests found a questionable condition
     */
    public boolean isYellow() {

        return m_yellow;
    }

    /**
     * Runs all tests.<p>
     *
     * @param setupBean the CmsSetup bean of the setup wizard
     */
    public void runTests(CmsSetupBean setupBean) {

        runTests(setupBean, null);
    }

    /**
     * Runs all tests.<p>
     *
     * @param setupBean the CmsSetup bean of the setup wizard
     * @param serverInfo optional server info, if not present the server info is retrieved from the bean
     */
    public void runTests(CmsSetupBean setupBean, String serverInfo) {

        boolean hasRed = false;
        boolean hasYellow = false;

        // reset everything back to an initial state
        m_testResults = new ArrayList<CmsSetupTestResult>();
        setGreen();

        Iterator<I_CmsSetupTest> it = getAllTests().iterator();
        while (it.hasNext()) {
            I_CmsSetupTest test = it.next();
            CmsSetupTestResult testResult = null;
            try {
                testResult = test.execute(setupBean);
                m_testResults.add(testResult);
            } catch (Throwable e) {
                testResult = new CmsSetupTestResult(test);
                testResult.setRed();
                testResult.setResult(I_CmsSetupTest.RESULT_FAILED);
                testResult.setHelp("Unable to test " + test.getName());
                testResult.setInfo(e.toString());
            }
        }

        // check whether a test found violated or questionable conditions
        for (int i = 0; i < m_testResults.size(); i++) {
            CmsSetupTestResult testResult = m_testResults.get(i);
            if (testResult.isRed()) {
                hasRed = true;
            } else if (testResult.isYellow()) {
                hasYellow = true;
            }
        }

        // set the global result of all tests
        if (hasRed) {
            setRed();
        } else if (!hasRed && hasYellow) {
            setYellow();
        } else {
            setGreen();
        }

        if (serverInfo == null) {
            // save the detected software component versions in a text file
            writeVersionInfo(
                setupBean.getServletConfig().getServletContext().getServerInfo(),
                System.getProperty("java.version"),
                setupBean.getWebAppRfsPath());
        } else {
            writeVersionInfo(serverInfo, System.getProperty("java.version"), setupBean.getWebAppRfsPath());
        }
    }

    /**
     * Sets if the conditions in all testes were fulfilled.<p>
     */
    protected void setGreen() {

        m_green = true;
        m_red = false;
        m_yellow = false;
    }

    /**
     * Sets if one of the tests found a violated condition.<p>
     */
    protected void setRed() {

        m_green = false;
        m_red = true;
        m_yellow = false;
    }

    /**
     * Sets if one of the tests found a questionable condition.<p>
     */
    protected void setYellow() {

        m_green = false;
        m_red = false;
        m_yellow = true;
    }

    /**
     * Writes the version info of the used servlet engine and the used JDK
     * to the version.txt.<p>
     *
     * @param thisEngine The servlet engine in use
     * @param usedJDK The JDK version in use
     * @param basePath the OpenCms base path
     */
    protected void writeVersionInfo(String thisEngine, String usedJDK, String basePath) {

        FileWriter fOut = null;
        PrintWriter dOut = null;
        String filename = basePath + CmsSetupDb.SETUP_FOLDER + "versions.txt";
        try {
            File file = new File(filename);
            if (file.exists()) {
                // new FileOutputStream of the existing file with parameter append=true
                fOut = new FileWriter(filename, true);
            } else {
                fOut = new FileWriter(file);
            }
            // write the content to the file in server filesystem
            dOut = new PrintWriter(fOut);
            dOut.println();
            dOut.println("############### currently used configuration ################");
            dOut.println(
                "Date:                "
                    + DateFormat.getDateTimeInstance().format(new java.util.Date(System.currentTimeMillis())));
            dOut.println("Used JDK:            " + usedJDK);
            dOut.println("Used Servlet Engine: " + thisEngine);
            dOut.close();
        } catch (IOException e) {
            // nothing we can do
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                // nothing we can do
            }
        }
    }

}
