package com.opencms.template;

import java.util.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class CmsDumpTemplate implements I_CmsDumpTemplate, I_CmsLogChannels {
    
    private I_CmsTemplateCache m_cache = null;

    
    public CmsDumpTemplate() {
    }
    

    public void setTemplateCache(I_CmsTemplateCache c) {
        // do nothing.
        // we don't include other templates so there is
        // nothing to cache.
    }
    
    public boolean isTemplateCacheSet() {
        return true;
    }
    
    public Object getKey(A_CmsObject cms, String templateFile, Hashtable parameter) {
        //return templateFile.getAbsolutePath();
        return templateFile;
    }
    
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        //String s = templateFile.getContents();
        return cms.readFile(templateFile).getContents();
    }
    
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        // ignore the templateSelector since we only dump the template
        return getContent(cms, templateFile, elementName, parameters);
    }
    
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return true;
    }    
    
    public boolean shouldReload(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return false;
    }
}
