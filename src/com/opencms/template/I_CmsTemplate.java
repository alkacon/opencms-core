package com.opencms.template;

import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;

import java.util.*;

/**
 * Common interface for OpenCms template classes.
 * Classes and interfaces for each customized template type
 * have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
 */
public interface I_CmsTemplate {
    /**
     * Gets the content of a given template file with the given parameters.
     * <P>
     * Parameters are stored in a hashtable and can derive from
     * <UL>
     * <LI>Template file of the parent template</LI>
     * <LI>Body file clicked by the user</LI>
     * <LI>URL parameters</LI>
     * </UL>
     * Paramter names must be in "elementName.parameterName" format.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
           throws CmsException;
    
    /**
     * Gets the content of a defined section in a given template file 
     * with the given parameters.
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector section that should be processed.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
            throws CmsException;
    
    /**
     * Set the instance of template cache that should be used to store 
     * cacheable results of the subtemplates.
     * If the template cache is not set, caching will be disabled.
     * @param c Template cache to be used.
     */
    public void setTemplateCache(I_CmsTemplateCache c);
    
    /**
     * Tests, if the template cache is setted.
     * @return <code>true</code> if setted, <code>false</code> otherwise.
     */
    public boolean isTemplateCacheSet();    
    
    /**
     * Gets the key that should be used to cache the results of
     * <EM>this</EM> template class. For simple template classes, e.g.
     * classes only dumping file contents and not using parameters,
     * the name of the template file may be adequate.
     * Other classes have to return a more complex key.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return key that can be used for caching
     */
    public Object getKey(A_CmsObject cms, String templateFile, Hashtable parameters);    
    
    /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * Complex classes that are able top include other subtemplates
     * have to check the cacheability of their subclasses here!
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters);

    /**
     * Indicates if a previous cached result should be reloaded.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <EM>true</EM> if reload is neccesary, <EM>false</EM> otherwise.
     */    
    public boolean shouldReload(A_CmsObject cms, String templateFile, Hashtable parameters);	
}