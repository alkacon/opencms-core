package com.opencms.template;

import java.util.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Template class for dumping files to the output without further 
 * interpreting or processing.
 * This can be used for plain text files or files containing graphics.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
 */
public class CmsDumpTemplate implements I_CmsDumpTemplate, I_CmsLogChannels {
    
    /** 
     * Template cache is not used here since we don't include
     * any subtemplates.
     */
    private static I_CmsTemplateCache m_cache = null;

    public CmsDumpTemplate() {
    }
    
    /** 
     * Template cache is not used here since we don't include
     * any subtemplates <em>(not implemented)</em>.
     */
    public void setTemplateCache(I_CmsTemplateCache c) {
        // do nothing.
    }
    
    /** 
     * Any results of this class are cacheable since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean isTemplateCacheSet() {
        return true;
    }
    
    /**
     * Gets the key that should be used to cache the results of
     * this template class. 
     * <P>
     * Since this class is quite simple it's okay to return
     * just the name of the template file here.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return key that can be used for caching
     */
    public Object getKey(A_CmsObject cms, String templateFile, Hashtable parameter) {
        //return templateFile.getAbsolutePath();
        return templateFile;
    }
    
    /**
     * Gets the content of a given template file.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException 
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        //String s = templateFile.getContents();
        return cms.readFile(templateFile).getContents();
    }
    
    /**
     * Gets the content of a given template file.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @param templateSelector <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException 
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        // ignore the templateSelector since we only dump the template
        return getContent(cms, templateFile, elementName, parameters);
    }
    
    /** 
     * Template cache is not used here since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return true;
    }    
    
    /** 
     * Template cache is not used here since we don't include
     * any subtemplates. So we can always return <code>false</code> here.
     * @return <code>false</code>
     */
    public boolean shouldReload(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return false;
    }
}
