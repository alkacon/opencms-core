/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/staticexport/TestCmsStaticExportManager.java,v $
 * Date   : $Date: 2005/04/29 16:02:25 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import java.util.regex.Pattern;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the XML page that require a running OpenCms system.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 5.9.1
 */
public class TestCmsStaticExportManager extends OpenCmsTestCase {

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsStaticExportManager.class.getName());

        suite.addTest(new TestCmsStaticExportManager("testExportLinkGeneration"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }

        };

        return wrapper;
    }

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsStaticExportManager(String arg0) {

        super(arg0);
    }

    
    /**
     * Tests the link generation for files that are to be exported.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testExportLinkGeneration() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing the link generation for exported files without parameters");
        
        // fist setup a little test scenario with the imported data
        String typesUri = "/types";
        String jspUri = "/types/jsp.jsp";
        CmsProperty exportProp = new CmsProperty(I_CmsConstants.C_PROPERTY_EXPORT, "true", null);
        cms.lockResource(typesUri);
        cms.writePropertyObject(typesUri, exportProp);
        cms.unlockResource(typesUri);        
        cms.lockResource(jspUri);
        cms.writePropertyObject(jspUri, exportProp);        
        cms.unlockResource(jspUri);        
        // publish the changes
        cms.publishProject();
        
        CmsStaticExportManager exportManager = OpenCms.getStaticExportManager();
        
        
        echo("Testing basic export name generating functions for a JSP");
        
        // check JSP without parameters
        String jspRfsName = exportManager.getRfsName(cms, jspUri);
        System.out.println("JSP RFS name/1: " + jspRfsName);
        String expected = exportManager.getRfsPrefix() + cms.getRequestContext().getSiteRoot() + jspUri + ".html";
        assertEquals(expected, jspRfsName);
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
        
        // check JSP WITH parameters
        jspRfsName = exportManager.getRfsName(cms, jspUri, "?a=b&c=d");
        System.out.println("JSP RFS name/2: " + jspRfsName);        
        Pattern pattern = Pattern.compile("^"
            + CmsStringUtil.escapePattern(exportManager.getRfsPrefix()
                + cms.getRequestContext().getSiteRoot()
                + jspUri
                + ".html"
                + "_")
            + "\\d*"
            + "\\.html$");
        assertTrue(pattern.matcher(jspRfsName).matches());
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
        // assert the last result again - test for potential cache issues
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
        
        
        
        echo("Testing export name generating functions for a JSP with 'exportname' property set");
        
        // set "exportname" property to JSP
        cms.lockResource(typesUri);
        cms.writePropertyObject(typesUri, new CmsProperty(I_CmsConstants.C_PROPERTY_EXPORTNAME, "myfolder", null));
        cms.unlockResource(typesUri);
        // publish the changes
        cms.publishProject(); 
        
        String exportNameJspUri = "/myfolder/jsp.jsp";
        
        // check JSP without parameters
        jspRfsName = exportManager.getRfsName(cms, jspUri);
        System.out.println("JSP RFS name/3: " + jspRfsName);
        expected = exportManager.getRfsPrefix() + exportNameJspUri + ".html";
        assertEquals(expected, jspRfsName);
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
        
        // check JSP WITH parameters
        jspRfsName = exportManager.getRfsName(cms, jspUri, "?a=b&c=d");
        System.out.println("JSP RFS name/4: " + jspRfsName);        
        pattern = Pattern.compile("^"
            + CmsStringUtil.escapePattern(exportManager.getRfsPrefix()
                + exportNameJspUri
                + ".html"
                + "_")
            + "\\d*"
            + "\\.html$");
        assertTrue(pattern.matcher(jspRfsName).matches());
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
        
        
        echo("Testing export name generating functions for a JSP with 'exportname' property AND 'exportsuffix' property set");

        // set "exportsuffix" property to JSP
        cms.lockResource(jspUri);
        cms.writePropertyObject(jspUri, new CmsProperty(I_CmsConstants.C_PROPERTY_EXPORTSUFFIX, ".txt", null));
        cms.unlockResource(jspUri);
        // publish the changes
        cms.publishProject(); 
        
        // check JSP without parameters
        jspRfsName = exportManager.getRfsName(cms, jspUri);
        System.out.println("JSP RFS name/5: " + jspRfsName);
        expected = exportManager.getRfsPrefix() + exportNameJspUri + ".txt";
        assertEquals(expected, jspRfsName);
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));        
        
        // check JSP WITH parameters
        jspRfsName = exportManager.getRfsName(cms, jspUri, "?a=b&c=d");
        System.out.println("JSP RFS name/6: " + jspRfsName);        
        pattern = Pattern.compile("^"
            + CmsStringUtil.escapePattern(exportManager.getRfsPrefix()
                + exportNameJspUri
                + ".txt"
                + "_")
            + "\\d*"
            + "\\.txt$");
        assertTrue(pattern.matcher(jspRfsName).matches());
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
        
        
        echo("Testing export name generating functions for a JSP with only 'exportsuffix' property set");
        
        // remove "exportname" property from JSP
        cms.lockResource(typesUri);
        cms.writePropertyObject(typesUri, new CmsProperty(
            I_CmsConstants.C_PROPERTY_EXPORTNAME,
            CmsProperty.C_DELETE_VALUE,
            CmsProperty.C_DELETE_VALUE));
        cms.unlockResource(typesUri);
        cms.lockResource(jspUri);        
        cms.writePropertyObject(jspUri, new CmsProperty(I_CmsConstants.C_PROPERTY_EXPORTSUFFIX, ".pdf", null));
        cms.unlockResource(jspUri);        
        // publish the changes
        cms.publishProject(); 
        
        // check JSP without parameters
        jspRfsName = exportManager.getRfsName(cms, jspUri);
        System.out.println("JSP RFS name/7: " + jspRfsName);
        expected = exportManager.getRfsPrefix() + cms.getRequestContext().getSiteRoot() + jspUri + ".pdf";
        assertEquals(expected, jspRfsName);
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));    
        
        // check JSP WITH parameters
        jspRfsName = exportManager.getRfsName(cms, jspUri, "?a=b&c=d");
        System.out.println("JSP RFS name/8: " + jspRfsName);        
        pattern = Pattern.compile("^"
            + CmsStringUtil.escapePattern(exportManager.getRfsPrefix()
                + cms.getRequestContext().getSiteRoot()
                + jspUri
                + ".pdf"
                + "_")
            + "\\d*"
            + "\\.pdf");
        assertTrue(pattern.matcher(jspRfsName).matches());
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
        // assert the last result again - test for potential cache issues
        assertEquals(jspUri, exportManager.getVfsName(cms, jspRfsName));
    }    
}
