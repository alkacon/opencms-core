/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupTests.java,v $
 * Date   : $Date: 2004/02/22 19:14:26 $
 * Version: $Revision: 1.5 $
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.jsp.PageContext;

import org.apache.xerces.impl.Version;
import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Runs various tests to give users infos about whether their system is compatible to OpenCms.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.5 $ $Date: 2004/02/22 19:14:26 $
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
        writeVersionInfo(m_pageContext.getServletConfig().getServletContext().getServerInfo(), System.getProperty("java.version"), m_pageContext.getServletConfig().getServletContext().getRealPath("/"));
    }

    /**
     * Tests the version of the JDK.<p>
     */
    public void testJdkVersion() {
        CmsSetupTestResult testResult = new CmsSetupTestResult();

        try {
            String requiredJDK = "1.4.0";
            String JDKVersion = System.getProperty("java.version");
            boolean supportedJDK = compareJDKVersions(JDKVersion, requiredJDK);

            testResult.setName("JDK version");
            testResult.setResult(JDKVersion);

            if (!supportedJDK) {
                testResult.setRed();
                testResult.setHelp("OpenCms requires at least JDK version " + requiredJDK + " to run. Please update your JDK");
            } else {
                testResult.setGreen();
            }
        } catch (Exception e) {
            testResult.setRed();
            testResult.setResult("Unable to test JDK version!");
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
            boolean supportedServletEngine = hasSupportedServletEngine(servletEngine, supportedEngines);
            int unsupportedServletEngine = unsupportedServletEngine(servletEngine, unsupportedEngines);

            testResult.setName("Servlet engine");
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
        } catch (Exception e) {
            testResult.setRed();
            testResult.setResult("Unable to test servlet engine!");
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
            testResult.setHelp("No help available.");
            
            // there is still no handling to test the operating system
            testResult.setGreen();
        } catch (Exception e) {
            testResult.setRed();
            testResult.setResult("Unable to test the operating system!");
            testResult.setInfo(e.toString());
        } finally {
            m_testResults.add(testResult);
        }
    }
    
    /**
     * Test for the Xerces version.<p>
     */
    public void testXercesVersion() {
        CmsSetupTestResult testResult = new CmsSetupTestResult();
        testResult.setName("XML Parser");

        try {
            DOMParser parser = new DOMParser();
            String document = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<test>test</test>\n";
            parser.parse(new InputSource(new ByteArrayInputStream(document.getBytes("UTF-8"))));
            Document doc = parser.getDocument();

            // Xerces 1 and 2 APIs are different, let's see what we have...
            String versionStr = null;
            
            try {
                doc.getClass().getMethod("getXmlEncoding", new Class[] {}).invoke(doc, new Object[] {});
                versionStr = Version.getVersion();
            } catch (Throwable t) {
                // noop
            }
            if (versionStr == null) {
                try {
                    doc.getClass().getMethod("getEncoding", new Class[] {}).invoke(doc, new Object[] {});
                    versionStr = "Xerces version 1";
                } catch (Throwable t) {
                    // noop
                }
            }

            testResult.setResult(versionStr);
            testResult.setHelp("OpenCms requires Xerces version 1 or Xerces version 2 to run. Usually these should be available as part of the servlet environment.");
            testResult.setGreen();
        } catch (Exception e) {
            testResult.setResult("Unable to test the XML parser!");
            testResult.setInfo(e.toString());
            testResult.setRed();
        } finally {
            m_testResults.add(testResult);
        }
    }
    
    /**
     * Tests if the OpenCms WAR file is unpacked.<p>
     */
    public void testWarFileUnpacked() {
        CmsSetupTestResult testResult = new CmsSetupTestResult();

        try {
            testResult.setName("Unpacked WAR file");
            
            File file = new File(m_pageContext.getServletConfig().getServletContext().getRealPath("/") + "WEB-INF" + File.separator + "config" + File.separator + "opencms.properties");
            if (file.exists() && file.canRead() && file.canWrite()) {
                testResult.setGreen();
                testResult.setResult("yes");
            } else {
                testResult.setRed();
                testResult.setInfo("OpenCms cannot be installed unless the OpenCms WAR file is unpacked! "
                                    + "Please check the settings of your servlet container or unpack the WAR file manually.");
                testResult.setHelp(testResult.getInfo());
                testResult.setResult("WAR file NOT unpacked");
            }
        } catch (Exception e) {
            testResult.setRed();
            testResult.setResult("Unable to test if the OpenCms WAR file is unpacked!");
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

    /**
     * Checks if the used JDK is a higher version than the required JDK
     * 
     * @param usedJDK The JDK version in use
     * @param requiredJDK The required JDK version
     * @return true if used JDK version is equal or higher than required JDK version, false otherwise
     */
    protected boolean compareJDKVersions(String usedJDK, String requiredJDK) {
        int compare = usedJDK.compareTo(requiredJDK);
        return (!(compare < 0));
    }

    /** 
     * Checks if the used servlet engine is part of the servlet engines OpenCms
     * does NOT support<p>
     * 
     * @param thisEngine the servlet engine in use
     * @param unsupportedEngines all known servlet engines OpenCms does NOT support
     * @return the engine id or -1 if the engine is not supported
     */
    protected int unsupportedServletEngine(String thisEngine, String[] unsupportedEngines) {
        for (int i = 0; i < unsupportedEngines.length; i++) {
            if (thisEngine.indexOf(unsupportedEngines[i]) >= 0) {
                return i;
            }
        }
        return -1;
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
        String filename = basePath + CmsSetupDb.C_SETUP_FOLDER + "versions.txt";
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
            dOut.println("Date:                " + DateFormat.getDateTimeInstance().format(new java.util.Date(System.currentTimeMillis())));
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

    /** 
     * Checks if the used servlet engine is part of the servlet engines OpenCms supports
     * 
     * @param thisEngine The servlet engine in use
     * @param supportedEngines All known servlet engines OpenCms supports
     * @return true if this engine is supported, false if it was not found in the list
     */
    protected boolean hasSupportedServletEngine(String thisEngine, String[] supportedEngines) {
        boolean supported = false;
        engineCheck : for (int i = 0; i < supportedEngines.length; i++) {
            if (thisEngine.indexOf(supportedEngines[i]) >= 0) {
                supported = true;
                break engineCheck;
            }
        }
        return supported;
    }

}
