package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;

/**
 * Interface for OpenCms XML template classes.
 * <P>
 * All methods extending the functionality of the common
 * template class interface to the special behaviour
 * of XML template classes may be defined here.
 * <P>
 * Primarily, this interface is important for the launcher, 
 * NOT for the template engine.
 * The CmsXmlLauncher can launch all templates that
 * implement the I_CmsXmlTemplate interface. 
 * <P>
 * For subtemplates, the I_CmsTemplate interface is
 * all that must be implemented, so you can load all kind
 * of templates (eg. type I_CmsDumpTemplate) as subtemplate.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
 */
public interface I_CmsXmlTemplate extends I_CmsTemplate {
    
    /**
     * Handles any occurence of an "ELEMENT" tag.
     * <P>
     * Every XML template class should use CmsXmlTemplateFile as
     * the interface to the XML file. Since CmsXmlTemplateFile is
     * an extension of A_CmsXmlContent by the additional tag
     * "ELEMENT" this user method ist mandatory.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object templateElement(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException;
}
