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
 * Tests the servlet container.<p>
 *
 * @since 6.1.8
 */
public class CmsSetupTestServletContainer implements I_CmsSetupTest {

    /** The test name. */
    public static final String TEST_NAME = "Servlet Container";

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#execute(org.opencms.setup.CmsSetupBean)
     */
    public CmsSetupTestResult execute(CmsSetupBean setupBean) {

        CmsSetupTestResult testResult = new CmsSetupTestResult(this);

        String[][] supportedContainers = {
            {"Apache Tomcat/4.1", null},
            {"Apache Tomcat/5", null},
            {"Apache Tomcat/6", null},
            {"Apache Tomcat/7", null},
            {"Apache Tomcat/8", null},
            {"WebLogic Server 9", null},
            {
                "Resin/3",
                "Please be sure that during the Setup Wizard, the web application auto-redeployment feature is deactivated. One way to achieve this, is to set the '<code>dependency-check-interval</code>' option in your <code>resin.conf</code> configuration file to <code>-1</code> or something big like <code>2000s</code>."},
            {
                "IBM WebSphere Application Server/6",
                "The only limitation found so far, is that when using the <code>sendRedirect</code> method you have always to use an absolute path."},
            {"Sun GlassFish Enterprise Server v2.1", null},
            {
                "GlassFish/v3",
                "GlassFish/v3 is not a stable release and subject to major changes. Please prefer a stable release."},
            {"JBoss Web/2.1.3.GA", null}};

        String[][] unsupportedContainers = {
            {"Tomcat Web Server/3", "Tomcat 3.x is no longer supported. Please use at least Tomcat 4.1 instead."},
            {"Apache Tomcat/4.0", "Tomcat 4.0.x is no longer supported. Please use at least Tomcat 4.1 instead."},
            {"Resin/2", "The OpenCms JSP integration does not work with Resin 2.x. Please use Resin 3 instead."},
            {
                "IBM WebSphere Application Server/5",
                "OpenCms has problems with the way Websphere handles the <code>sendRedirect</code> method. Please use at least WebSphere 6 instead."}};

        String servletContainer = setupBean.getServletConfig().getServletContext().getServerInfo();
        testResult.setResult(servletContainer);

        int supportedServletContainer = hasSupportedServletContainer(servletContainer, supportedContainers);
        int unsupportedServletContainer = unsupportedServletContainer(servletContainer, unsupportedContainers);

        if (unsupportedServletContainer > -1) {
            testResult.setRed();
            testResult.setInfo(unsupportedContainers[unsupportedServletContainer][1]);
            testResult.setHelp(
                "This servlet container does not work with OpenCms. Even though OpenCms is fully standards compliant, "
                    + "the standard leaves some 'grey' (i.e. undefined) areas. "
                    + "Please consider using another, supported servlet container.");
        } else if (supportedServletContainer < 0) {
            testResult.setYellow();
            testResult.setHelp(
                "This servlet container has not been tested with OpenCms. Please consider using another, supported servlet container.");
        } else if (supportedContainers[supportedServletContainer][1] != null) {
            // set additional info for supported servlet containers
            testResult.setInfo(supportedContainers[supportedServletContainer][1]);
        } else {
            testResult.setGreen();
        }
        return testResult;
    }

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#getName()
     */
    public String getName() {

        return TEST_NAME;
    }

    /**
     * Checks if the used servlet container is part of the servlet containers OpenCms supports.<p>
     *
     * @param thisContainer The servlet container in use
     * @param supportedContainers All known servlet containers OpenCms supports
     *
     * @return true if this container is supported, false if it was not found in the list
     */
    private int hasSupportedServletContainer(String thisContainer, String[][] supportedContainers) {

        for (int i = 0; i < supportedContainers.length; i++) {
            if (thisContainer.indexOf(supportedContainers[i][0]) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the used servlet container is part of the servlet containers OpenCms
     * does NOT support.<p>
     *
     * @param thisContainer the servlet container in use
     * @param unsupportedContainers all known servlet containers OpenCms does NOT support
     *
     * @return the container id or -1 if the container is not supported
     */
    private int unsupportedServletContainer(String thisContainer, String[][] unsupportedContainers) {

        for (int i = 0; i < unsupportedContainers.length; i++) {
            if (thisContainer.indexOf(unsupportedContainers[i][0]) >= 0) {
                return i;
            }
        }
        return -1;
    }
}
