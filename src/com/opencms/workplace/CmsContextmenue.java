/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsContextmenue.java,v $
 * Date   : $Date: 2000/02/19 17:05:41 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class for building workplace icons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ICON&gt;</code>.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.5 $ $Date: 2000/02/19 17:05:41 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsContextmenue extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {

    /** Storage for contextmenue */
    private Hashtable m_storage= new Hashtable();
    
     /**
     * Handling of the special workplace <CODE>&lt;Contextmenue&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Contextmenue can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;Contextmenue /&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ICON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        // Read Contextmenue parameters
        String name = n.getAttribute("name");
		String output;
        
		// create the result
		StringBuffer result = new StringBuffer();
		
        // check if this contextmenu is already cached
        output=(String)m_storage.get(name);
        if (output== null) {    
            // Get list definition and language values
            CmsXmlWpTemplateFile context = getContextmenueDefinitions(cms);
		
		    // set the name (id) of the contextmenu
		    context.setXmlData("name", name);
		    result.append(context.getProcessedXmlDataValue("CONTEXTHEAD", callingObject, parameters));
		
    		NodeList nl = n.getChildNodes();
	    	// get each childnode
		    for(int i = 0; i < nl.getLength(); i++) {
    			Node actualNode = nl.item(i);
	    		if( actualNode.getNodeType() != Node.TEXT_NODE ) {
		    		Element e = (Element) actualNode;
			    	// this is not a text node, process it
    				if(e.getTagName().toLowerCase().equals("contextspacer")) {
	    				// append a spacer
		    			result.append(context.getProcessedXmlDataValue("CONTEXTSPACER", callingObject, parameters));
			    	} else if(e.getTagName().toLowerCase().equals("contextentry")){
    					// append a entry
	    				context.setXmlData("name", lang.getLanguageValue(e.getAttribute("name")));
		    			context.setXmlData("href", e.getAttribute("href"));
			    		result.append(context.getProcessedXmlDataValue("CONTEXTENTRY", callingObject, parameters));
    				} else if(e.getTagName().toLowerCase().equals("contextdisabled")){
	    				// append a entry
			    		context.setXmlData("name", lang.getLanguageValue(e.getAttribute("name")));
		    			result.append(context.getProcessedXmlDataValue("CONTEXTDISABLED", callingObject, parameters));
    				}
	    		}
            }
		
		    result.append(context.getProcessedXmlDataValue("CONTEXTFOOT", callingObject, parameters));
            output=result.toString();
            m_storage.put(name,output);            
        }
        
		// rerun the result
		return output;
    }
}
