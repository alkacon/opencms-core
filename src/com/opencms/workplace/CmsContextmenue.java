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
 * @version $Revision: 1.1 $ $Date: 2000/02/08 15:45:03 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsContextmenue extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {
            
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
		
		// create the result
		StringBuffer result = new StringBuffer();
		
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
				} else {
					// append a entry
					context.setXmlData("name", lang.getLanguageValue(e.getAttribute("name")));
					context.setXmlData("href", e.getAttribute("href"));
					result.append(context.getProcessedXmlDataValue("CONTEXTENTRY", callingObject, parameters));
				}
			}
		}
		
		
		result.append(context.getProcessedXmlDataValue("CONTEXTFOOT", callingObject, parameters));

		// retur the result
		return result.toString();
    }
}
