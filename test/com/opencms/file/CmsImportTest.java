/*
 * Created on 28.02.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.opencms.file;

import junit.framework.TestCase;

/**
 * @author thomas
 */
public class CmsImportTest extends TestCase {

    /**
     * Constructor for CmsImportTest.
     * @param arg0
     */
    public CmsImportTest(String arg0) {
        super(arg0);
    }

    public void testSetDirectories() {
        String content =         
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<PAGE>\n" +
            "    <class>com.opencms.template.CmsXmlTemplate</class>\n" +
            "    <masterTemplate>/system/modules/de.alkacon.opencms.modules.lgt.frontend/templates/lgt_group_main</masterTemplate>\n" +
            "    <ELEMENTDEF name=\"body\">\n" +
            "        <CLASS>com.opencms.template.CmsXmlTemplate</CLASS>\n" +
            "        <TEMPLATE>/content/bodys/group/de/index.html</TEMPLATE>\n" +
            "    </ELEMENTDEF>\n" +
            "</PAGE>";
        
        String result =         
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<PAGE>\n" +
            "    <class>com.opencms.template.CmsXmlTemplate</class>\n" +
            "    <masterTemplate>/system/modules/de.alkacon.opencms.modules.lgt.frontend/templates/lgt_group_main</masterTemplate>\n" +
            "    <ELEMENTDEF name=\"body\">\n" +
            "        <CLASS>com.opencms.template.CmsXmlTemplate</CLASS>\n" +
            "        <TEMPLATE>/system/bodies/group/de/index.html</TEMPLATE>\n" +
            "    </ELEMENTDEF>\n" +
            "</PAGE>";        
        
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

        content = CmsImport.setDirectories(content, rules);        
        assertEquals(content, result);        
    }

}
