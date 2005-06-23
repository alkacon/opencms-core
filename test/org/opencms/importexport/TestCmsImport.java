/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/importexport/TestCmsImport.java,v $
 * Date   : $Date: 2005/06/23 14:27:27 $
 * Version: $Revision: 1.8 $
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

package org.opencms.importexport;

import junit.framework.TestCase;

/**
 * @param Please leave a comment here.
 *
 * @author Alexander Kandzior 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 6.0.0
 */
public class TestCmsImport extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsImport(String arg0) {

        super(arg0);
    }

    /**
     * Runs a test for the conversion of the digest encoding.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testConvertDigestEncoding() throws Throwable {

        A_CmsImport imp = new CmsImportVersion4();
        String result;

        // test 'password'
        result = imp.convertDigestEncoding("dfcd4cbbda27e5569d03a75e38024f19");
        assertEquals(result, "X03MO1qnZdYdgyfeuILPmQ==");

        // test 'admin'
        result = imp.convertDigestEncoding("a1a3afa9fad72527c309ca8eca009f43");
        assertEquals(result, "ISMvKXpXpadDiUoOSoAfww==");

        // test 'test'
        result = imp.convertDigestEncoding("890feb4dc6a153f34a5ece03a6a73476");
        assertEquals(result, "CY9rzUYh03PK3k6DJie09g==");

        // test '12345678901234567890'
        result = imp.convertDigestEncoding("7d0566ad1b6bc5c207f16ce8049832f1");
        assertEquals(result, "/YXmLZvrRUKHcexohBiycQ==");

        // test 'undnocheins'
        result = imp.convertDigestEncoding("7fd2de3ccff5567c2fc64fe744283452");
        assertEquals(result, "/1JevE911vyvRs9nxKi00g==");
    }

    /**
     * Runs a test for the import of content.<p>  
     */
    public void testSetDirectories() {

        String[] rules = {
            "s#/default/vfs/content/bodys/(.*)#/default/vfs/system/bodies/$1#",
            "s#/default/vfs/pics/system/(.*)#/default/vfs/system/workplace/resources/$1#",
            "s#/default/vfs/pics/(.*)#/default/vfs/system/galleries/pics/$1#",
            "s#/default/vfs/download/(.*)#/default/vfs/system/galleries/download/$1#",
            "s#/default/vfs/externallinks/(.*)#/default/vfs/system/galleries/externallinks/$1#",
            "s#/default/vfs/htmlgalleries/(.*)#/default/vfs/system/galleries/htmlgalleries/$1#",
            "s#/default/vfs/content/(.*)#/default/vfs/system/modules/org.opencms.default/$1#",
            "s#/default/vfs/moduledemos/(.*)#/default/vfs/system/moduledemos/$1#",
            "s#/default/vfs/system/workplace/config/language/(.*)#/default/vfs/system/workplace/locales/$1#",
            "s#/default/vfs/system/workplace/css/(.*)#/default/vfs/system/workplace/resources/$1#",
            "s#/default/vfs/system/workplace/templates/js/(.*)#/default/vfs/system/workplace/scripts/$1#"};

        String content, result;

        content = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
            + "<PAGE>\n"
            + "    <class>com.opencms.template.CmsXmlTemplate</class>\n"
            + "    <masterTemplate>/system/modules/org.opencms.frontend/templates/group_main</masterTemplate>\n"
            + "    <ELEMENTDEF name=\"body\">\n"
            + "        <CLASS>com.opencms.template.CmsXmlTemplate</CLASS>\n"
            + "        <TEMPLATE>/content/bodys/group/de/index.html</TEMPLATE>\n"
            + "    </ELEMENTDEF>\n"
            + "</PAGE>";

        result = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
            + "<PAGE>\n"
            + "    <class>com.opencms.template.CmsXmlTemplate</class>\n"
            + "    <masterTemplate>/system/modules/org.opencms.frontend/templates/group_main</masterTemplate>\n"
            + "    <ELEMENTDEF name=\"body\">\n"
            + "        <CLASS>com.opencms.template.CmsXmlTemplate</CLASS>\n"
            + "        <TEMPLATE>/system/bodies/group/de/index.html</TEMPLATE>\n"
            + "    </ELEMENTDEF>\n"
            + "</PAGE>";

        content = CmsImportVersion2.setDirectories(content, rules);
        assertEquals(content, result);

        content = ".hbackground {background:url(/open/cms/system/modules/li.castle.frontend/pics/bg_1.gif) no-repeat; background-color:#FFFFFF; }\n"
            + ".hibackground {background:url(/open/cms/system/modules/li.castle.frontend/pics/bg_1_cai_cpe.gif); no-repeat; background-color:#FFFFFF; }"
            + "<img src=\"{OpenCmsContext}/pics/test/\">\n"
            + "picDir=/system/modules/li.castle.frontend/pics/\n"
            + "<img alt=\"Slogan CPE\" src=\"]]><LINK><![CDATA[/pics/castle/slogan_cpe_de.gif]]></LINK><![CDATA[\">";

        result = ".hbackground {background:url(/open/cms/system/modules/li.castle.frontend/pics/bg_1.gif) no-repeat; background-color:#FFFFFF; }\n"
            + ".hibackground {background:url(/open/cms/system/modules/li.castle.frontend/pics/bg_1_cai_cpe.gif); no-repeat; background-color:#FFFFFF; }"
            + "<img src=\"{OpenCmsContext}/system/galleries/pics/test/\">\n"
            + "picDir=/system/modules/li.castle.frontend/pics/\n"
            + "<img alt=\"Slogan CPE\" src=\"]]><LINK><![CDATA[/system/galleries/pics/castle/slogan_cpe_de.gif]]></LINK><![CDATA[\">";

        content = CmsImportVersion2.setDirectories(content, rules);

        System.out.println(content);
        assertEquals(content, result);
    }
}
