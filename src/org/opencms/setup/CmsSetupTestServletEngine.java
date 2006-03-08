/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupTestServletEngine.java,v $
 * Date   : $Date: 2006/03/08 15:05:50 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup;

import javax.servlet.ServletConfig;

/**
 * Tests the servlet engine.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.8 
 */
public class CmsSetupTestServletEngine implements I_CmsSetupTest {

    /** The test name. */
    public static final String TEST_NAME = "Servlet Engine";

    /**
     * @see org.opencms.setup.I_CmsSetupTest#getName()
     */
    public String getName() {

        return TEST_NAME;
    }

    /**
     * @see org.opencms.setup.I_CmsSetupTest#run(org.opencms.setup.CmsSetupBean)
     */
    public CmsSetupTestResult run(CmsSetupBean setupBean) {

        CmsSetupTestResult testResult = new CmsSetupTestResult(this);

        String[] supportedEngines = {"Apache Tomcat/4.1", "Apache Tomcat/4.0", "Apache Tomcat/5.0", "Apache Tomcat/5.5"};

        String[] unsupportedEngines = {"Tomcat Web Server/3.2", "Tomcat Web Server/3.3", "Resin/2.0.b2"};

        String[] unsupportedServletEngineInfo = {
            "Tomcat 3.2 is no longer supported. Please use Tomcat 4.x instead.",
            "Tomcat 3.3 is no longer supported. Please use Tomcat 4.x instead.",
            "The OpenCms JSP integration does currently not work with Resin. Please use Tomcat 4.x instead."};

        ServletConfig config = setupBean.getServletConfig();
        String servletEngine = config.getServletContext().getServerInfo();
        boolean supportedServletEngine = hasSupportedServletEngine(servletEngine, supportedEngines);
        int unsupportedServletEngine = unsupportedServletEngine(servletEngine, unsupportedEngines);

        testResult.setResult(servletEngine);

        if (unsupportedServletEngine > -1) {
            testResult.setRed();
            testResult.setInfo(unsupportedServletEngineInfo[unsupportedServletEngine]);
            testResult.setHelp("This servlet engine does not work with OpenCms. Even though OpenCms is fully standards compliant, "
                + "the standard leaves some 'grey' (i.e. undefined) areas. "
                + "Please consider using another, supported engine.");
        } else if (!supportedServletEngine) {
            testResult.setYellow();
            testResult.setHelp("This servlet engine has not been tested with OpenCms. Please consider using another, supported engine.");
        } else {
            testResult.setGreen();
        }
        return testResult;
    }

    /** 
     * Checks if the used servlet engine is part of the servlet engines OpenCms supports.<p>
     * 
     * @param thisEngine The servlet engine in use
     * @param supportedEngines All known servlet engines OpenCms supports
     * 
     * @return true if this engine is supported, false if it was not found in the list
     */
    private boolean hasSupportedServletEngine(String thisEngine, String[] supportedEngines) {

        boolean supported = false;
        engineCheck: for (int i = 0; i < supportedEngines.length; i++) {
            if (thisEngine.indexOf(supportedEngines[i]) >= 0) {
                supported = true;
                break engineCheck;
            }
        }
        return supported;
    }

    /** 
     * Checks if the used servlet engine is part of the servlet engines OpenCms
     * does NOT support.<p>
     * 
     * @param thisEngine the servlet engine in use
     * @param unsupportedEngines all known servlet engines OpenCms does NOT support
     * @return the engine id or -1 if the engine is not supported
     */
    private int unsupportedServletEngine(String thisEngine, String[] unsupportedEngines) {

        for (int i = 0; i < unsupportedEngines.length; i++) {
            if (thisEngine.indexOf(unsupportedEngines[i]) >= 0) {
                return i;
            }
        }
        return -1;
    }
}
