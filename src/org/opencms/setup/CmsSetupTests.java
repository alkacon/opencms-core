/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupTests.java,v $
 * Date   : $Date: 2004/02/20 13:25:20 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.setup;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.jsp.PageContext;

/**
 * Runs various tests to give users infos about whether their system is compatible to OpenCms.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2004/02/20 13:25:20 $
 * @since 5.3
 */
public class CmsSetupTests extends Object implements Serializable, Cloneable {

    private transient CmsSetup m_setupBean;
    private boolean m_red;
    private boolean m_yellow;
    private boolean m_green;
    private transient PageContext m_pageContext;
    private List m_testResults;

    /**
     * Creates a new setup test suite.<p>
     */
    public CmsSetupTests() {
        super();
    }

    /**
     * Runs all tests.<p>
     * 
     * @param pageContext the page context of the JSP page
     * @param setupBean the CmsSetup bean of the setup wizard
     */
    public void runTests(PageContext pageContext, CmsSetup setupBean) {
        Method method = null;
        String methodName = null;
        boolean hasRed = false;
        boolean hasYellow = false;
        
        // reset everything back to an initial state
        m_pageContext = pageContext;
        m_setupBean = setupBean;
        m_testResults = (List) new ArrayList();        
        setGreen();

        try {
            // execute all available tests
            Method[] methods = getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                method = methods[i];
                methodName = method.getName();
                if (method != null && methodName.startsWith("test")) {
                    method.invoke(this, new Object[0]);
                }
            }
        } catch (IllegalAccessException e) {
            System.out.println("[" + getClass().getName() + "] error executing test method: " + methodName + ". Method object enforces Java language access control and the underlying method is inaccessible.");
        } catch (IllegalArgumentException e) {
            System.out.println("[" + getClass().getName() + "] error executing test method: " + methodName + ". The method is an instance method and the specified object argument is not an instance of the class or interface declaring the underlying method (or of a subclass or implementor thereof); if the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion.");
        } catch (InvocationTargetException e) {
            System.out.println("[" + getClass().getName() + "] error executing test method: " + methodName + ". The underlying method throws an exception.");
        } catch (NullPointerException e) {
            System.out.println("[" + getClass().getName() + "] error executing test method: " + methodName + ". The specified object is null and the method is an instance method.");
        } catch (ExceptionInInitializerError e) {
            System.out.println("[" + getClass().getName() + "] error executing test method: " + methodName + ". The initialization provoked by this method fails.");
        } catch (Exception e) {
            System.out.println("[" + getClass().getName() + "] error executing test method: " + methodName + ". " + e.toString());
        }
        
        // check whether a test found violated or questionable conditions
        for (int i = 0; i < m_testResults.size(); i++) {
            CmsSetupTestResult testResult = (CmsSetupTestResult) m_testResults.get(i);
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

        // save the detected software component versions in a text file
        CmsSetupUtils.writeVersionInfo(m_pageContext.getServletConfig().getServletContext().getServerInfo(), System.getProperty("java.version"), m_pageContext.getServletConfig().getServletContext().getRealPath("/"));
    }

    /**
     * Tests the version of the JDK.<p>
     */
    public void testJdkVersion() {
        CmsSetupTestResult testResult = new CmsSetupTestResult();

        try {
            String requiredJDK = "1.4.0";
            String JDKVersion = System.getProperty("java.version");
            boolean supportedJDK = CmsSetupUtils.compareJDKVersions(JDKVersion, requiredJDK);

            testResult.setName("JDK version");
            testResult.setResult(JDKVersion);

            if (!supportedJDK) {
                testResult.setRed();
            } else {
                testResult.setGreen();
            }
        } catch (Exception e) {
            testResult.setRed();
            testResult.setName("<p>Unable to test JDK version!</p>");
            testResult.setInfo(e.toString());
        } finally {
            m_testResults.add(testResult);
        }
    }

    /**
     * Tests the servlet engine.<p>
     */
    public void testServletEngine() {
        CmsSetupTestResult testResult = new CmsSetupTestResult();

        try {
            String[] supportedEngines = { 
                    "Apache Tomcat/4.1", 
                    "Apache Tomcat/4.0", 
                    "Apache Tomcat/5.0" 
            };

            String[] unsupportedEngines = { 
                    "Tomcat Web Server/3.2", 
                    "Tomcat Web Server/3.3", 
                    "Resin/2.0.b2" 
            };

            String[] unsupportedServletEngineInfo = { 
                    "Tomcat 3.2 is no longer supported. Please use Tomcat 4.x instead.", 
                    "Tomcat 3.3 is no longer supported. Please use Tomcat 4.x instead.", 
                    "The OpenCms JSP integration does currently not work with Resin. Please use Tomcat 4.x instead." 
            };

            ServletConfig config = m_pageContext.getServletConfig();
            String servletEngine = config.getServletContext().getServerInfo();
            boolean supportedServletEngine = CmsSetupUtils.supportedServletEngine(servletEngine, supportedEngines);
            int unsupportedServletEngine = CmsSetupUtils.unsupportedServletEngine(servletEngine, unsupportedEngines);

            testResult.setName("Servlet engine");
            testResult.setResult(servletEngine);

            if (unsupportedServletEngine > -1) {
                testResult.setRed();
                testResult.setInfo("<p>" + unsupportedServletEngineInfo[unsupportedServletEngine] + "</p>");
            } else if (!supportedServletEngine) {
                testResult.setYellow();
            } else {
                testResult.setGreen();
            }
        } catch (Exception e) {
            testResult.setRed();
            testResult.setName("<p>Unable to test servlet engine!</p>");
            testResult.setInfo(e.toString());
        } finally {
            m_testResults.add(testResult);
        }
    }

    /**
     * Tests the operating system.<p>
     */
    public void testOperatingSystem() {
        CmsSetupTestResult testResult = new CmsSetupTestResult();

        try {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");

            testResult.setName("Operating system");
            testResult.setResult(osName + " " + osVersion);
            
            // there is still no handling to test the operating system
            testResult.setGreen();
        } catch (Exception e) {
            testResult.setRed();
            testResult.setName("<p>Unable to test the operating system!</p>");
            testResult.setInfo(e.toString());
        } finally {
            m_testResults.add(testResult);
        }
    }

    /**
     * Returns true, if the conditions in all testes were fulfilled.<p>
     * 
     * @return true, if the conditions in all testes were fulfilled
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
     * Sets if the conditions in all testes were fulfilled.<p>
     */
    protected void setGreen() {
        m_green = true;
        m_red = false;
        m_yellow = false;
    }

    /**
     * Sets if one of the tests found a violated condition
     */
    protected void setRed() {
        m_green = false;
        m_red = true;
        m_yellow = false;
    }

    /**
     * Sets if one of the tests found a questionable condition
     */
    protected void setYellow() {
        m_green = false;
        m_red = false;
        m_yellow = true;
    }

    /**
     * Returns the CmsSetup bean of the setup wizard.<p>
     * 
     * @return the CmsSetup bean of the setup wizard
     */
    public CmsSetup getSetupBean() {
        return m_setupBean;
    }

    /**
     * Returns the test results.<p>
     * 
     * @return the test results
     */
    public List getTestResults() {
        return m_testResults;
    }

}
