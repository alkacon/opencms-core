/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsMacroResolver.java,v $
 * Date   : $Date: 2005/04/02 07:32:10 $
 * Version: $Revision: 1.4 $
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
 
package org.opencms.util;

import junit.framework.TestCase;

/** 
 * Test cases for {@link org.opencms.util.CmsMacroResolver}.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com}
 * @version $Revision: 1.4 $
 */
public class TestCmsMacroResolver extends TestCase {
      
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsMacroResolver(String arg0) {
        super(arg0);
    }
    
    /**
     * Tests the macro resolver main functions.<p>
     */
    public void testResolveMacros() {
           
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro("test", "REPLACED");
        
        String content, result;
        
        content = "<<This is a prefix >>${test}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED<<This is a suffix>>", result);
        
        content = "${test}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a suffix>>", result);        
        
        content = "<<This is a prefix >>${test}";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED", result);
        
        content = "<<This is a prefix >>$<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$<<This is a suffix>>", result);            

        content = "$<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("$<<This is a suffix>>", result);        
        
        content = "${<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("${<<This is a suffix>>", result);  
        
        content = "<<This is a prefix >>$";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$", result);
        
        content = "<<This is a prefix >>${";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${", result);
        
        content = "<<This is a prefix >>${}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);  
        
        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);
        
        content = "<<This is a prefix >>${test<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${test<<This is a suffix>>", result);
        
        content = "<<This is a prefix >>${";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${", result);
        
        content = "<<This is a prefix >>${${}";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${", result);
        
        content = "${$<<This $}{ is$}$ a {{pr${{${efix >>${${}";
        result  = resolver.resolveMacros(content);
        assertEquals("${$<<This $}{ is$}$ a {{pr${{${efix >>${", result);
        
        // test for unknown macros       
        
        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);
                
        content = "<<This is a prefix >>${unknown}";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>", result);
        
        content = "${unknown}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a suffix>>", result);   
        
        content = "${test}<<This is a prefix >>${test}${unknown}${test}<<This is a suffix>>${test}";
        result  = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACEDREPLACED<<This is a suffix>>REPLACED", result);   
        
        // set the "keep unknown macros" flag
        resolver.setKeepEmptyMacros(true);        
        
        content = "<<This is a prefix >>${${}";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${${}", result);
        
        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}<<This is a suffix>>", result);
        
        content = "<<This is a prefix >>${unknown}";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}", result);
        
        content = "${unknown}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("${unknown}<<This is a suffix>>", result);   
        
        content = "${test}<<This is a prefix >>${test}${unknown}${test}<<This is a suffix>>${test}";
        result  = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACED${unknown}REPLACED<<This is a suffix>>REPLACED", result);                
    }       
    
    
    /**
     * Tests a minimal interface implementation.<p>
     */
    public void testResolverInterface() {
    
        I_CmsMacroResolver resolver = new I_CmsMacroResolver() {
            public String getMacroValue(String key) {
                if ("test".equals(key)) {
                    return "REPLACED";
                } else {
                    return null;
                }
            }

            public boolean isKeepEmptyMacros() {
                return true;
            }

            public String resolveMacros(String input) {
                return CmsMacroResolver.resolveMacros(input, this);
            }
        };
        
        String content, result;
        
        content = null;
        result  = resolver.resolveMacros(null);
        assertEquals(null, result);   
        
        content = "";
        result  = resolver.resolveMacros(content);
        assertEquals("", result);           
        
        content = "${test}";
        result  = resolver.resolveMacros(content);
        assertEquals("REPLACED", result);    
        
        content = "<<This is a prefix >>${test}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED<<This is a suffix>>", result);
        
        content = "${test}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a suffix>>", result);        
        
        content = "<<This is a prefix >>${test}";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED", result);
        
        content = "<<This is a prefix >>$<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$<<This is a suffix>>", result);            

        content = "$<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("$<<This is a suffix>>", result);        
        
        content = "<<This is a prefix >>$";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$", result);       
        
        content = "<<This is a prefix >>${}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${}<<This is a suffix>>", result);  
        
        content = "<<This is a prefix >>${test<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${test<<This is a suffix>>", result);
        
        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}<<This is a suffix>>", result);
        
        content = "${test}<<This is a prefix >>${test}${unknown}${test}<<This is a suffix>>${test}";
        result  = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACED${unknown}REPLACED<<This is a suffix>>REPLACED", result);  
        
        content = "<<This is a prefix >>${unknown}";
        result  = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}", result);
        
        content = "${unknown}<<This is a suffix>>";
        result  = resolver.resolveMacros(content);
        assertEquals("${unknown}<<This is a suffix>>", result);   
        
        content = "Uncle Scrooge owns many $$$$";
        result  = resolver.resolveMacros(content);
        assertEquals(content, result);
        
        content = "$$$$ is what uncle Scrooge owns";
        result  = resolver.resolveMacros(content);
        assertEquals(content, result);
        
        content = "$$$$ is $ what $ uncle $$$ Scrooge $ owns $$$";
        result  = resolver.resolveMacros(content);
        assertEquals(content, result);        
        
        content = "$${test}$}${test} is ${ what ${test}{$} uncle ${${test} Scrooge $ owns ${${test}}";
        result  = resolver.resolveMacros(content);
        assertEquals("$REPLACED$}REPLACED is ${ what REPLACED{$} uncle ${REPLACED Scrooge $ owns ${REPLACED}", result);   
    }
}
