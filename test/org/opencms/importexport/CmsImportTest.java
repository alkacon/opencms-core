/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/importexport/Attic/CmsImportTest.java,v $
 * Date   : $Date: 2004/06/25 16:37:26 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.importexport;

import junit.framework.TestCase;

/**
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.0
 */
public class CmsImportTest extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public CmsImportTest(String arg0) {
        super(arg0);
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
            "s#/default/vfs/system/workplace/templates/js/(.*)#/default/vfs/system/workplace/scripts/$1#"
        };
        
        String content, result;
        
        content =         
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
            + "<PAGE>\n"
            + "    <class>com.opencms.template.CmsXmlTemplate</class>\n"
            + "    <masterTemplate>/system/modules/de.alkacon.opencms.modules.lgt.frontend/templates/lgt_group_main</masterTemplate>\n"
            + "    <ELEMENTDEF name=\"body\">\n"
            + "        <CLASS>com.opencms.template.CmsXmlTemplate</CLASS>\n"
            + "        <TEMPLATE>/content/bodys/group/de/index.html</TEMPLATE>\n"
            + "    </ELEMENTDEF>\n"
            + "</PAGE>";
        
        result =         
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
            + "<PAGE>\n"
            + "    <class>com.opencms.template.CmsXmlTemplate</class>\n"
            + "    <masterTemplate>/system/modules/de.alkacon.opencms.modules.lgt.frontend/templates/lgt_group_main</masterTemplate>\n"
            + "    <ELEMENTDEF name=\"body\">\n"
            + "        <CLASS>com.opencms.template.CmsXmlTemplate</CLASS>\n"
            + "        <TEMPLATE>/system/bodies/group/de/index.html</TEMPLATE>\n"
            + "    </ELEMENTDEF>\n"
            + "</PAGE>";        
        
        content = CmsImportVersion2.setDirectories(content, rules);        
        assertEquals(content, result);        
            
        content =      
            ".hbackground {background:url(/lgt/cms/system/modules/li.castle.frontend/pics/bg_1.gif) no-repeat; background-color:#FFFFFF; }\n"
            + ".hibackground {background:url(/lgt/cms/system/modules/li.castle.frontend/pics/bg_1_cai_cpe.gif); no-repeat; background-color:#FFFFFF; }"        
            + "<img src=\"{OpenCmsContext}/pics/test/\">\n"
            + "picDir=/system/modules/li.castle.frontend/pics/\n"
            + "<img alt=\"Slogan CPE\" src=\"]]><LINK><![CDATA[/pics/castle/slogan_cpe_de.gif]]></LINK><![CDATA[\">";
        
        result =         
            ".hbackground {background:url(/lgt/cms/system/modules/li.castle.frontend/pics/bg_1.gif) no-repeat; background-color:#FFFFFF; }\n"
            + ".hibackground {background:url(/lgt/cms/system/modules/li.castle.frontend/pics/bg_1_cai_cpe.gif); no-repeat; background-color:#FFFFFF; }"        
            + "<img src=\"{OpenCmsContext}/system/galleries/pics/test/\">\n"
            + "picDir=/system/modules/li.castle.frontend/pics/\n"
            + "<img alt=\"Slogan CPE\" src=\"]]><LINK><![CDATA[/system/galleries/pics/castle/slogan_cpe_de.gif]]></LINK><![CDATA[\">";           
        
        content = CmsImportVersion2.setDirectories(content, rules);      
        
        System.out.println(content);  
        assertEquals(content, result);           
    }

}
