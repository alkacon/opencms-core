package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class CmsXmlNewsTemplate extends CmsXmlTemplate implements I_CmsLogChannels {
    
    /**
     * Are the results of this template class cacheable? 
     */    
    public boolean isCacheable() {
        return true;
    }
        
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        String selector = "";
        
        String read = (String)parameters.get(elementName + ".read");        
        if(read != null && ! "".equals(read)) {
            selector = "read";
        }
            
        byte[] h = getContent(cms, templateFile, elementName, parameters, selector);
        return h;
    }
        
    public String newsList(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        String newsFolder = (String)parameters.get("elem1.newsfolder");
        if(newsFolder == null || "".equals(newsFolder)) {
            String errorMessage = "No parameter \"NEWSFOLDER\" defined in " + doc.getAbsoluteFilename();
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlNewsTemplate] " + errorMessage);
            }            
            throw new CmsException(errorMessage);
        }
        
        Enumeration en = CmsXmlNewsContentDefinition.getAllArticles(cms, newsFolder);
       String result = "";
        while(en.hasMoreElements()) {
            Object o = en.nextElement();
            CmsXmlNewsContentDefinition doc2 = (CmsXmlNewsContentDefinition)o;
            result = result + "\n    <LI>" + doc2.getNewsHeadline() + "</LI>";
        }                        
        return result;
    }    
 
    public String article(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        
        Hashtable parameters = (Hashtable)userObj;
        String result = null;
        
        String read = (String)parameters.get("elem1.read");
        String folder = (String)parameters.get("elem1.newsfolder");
        if((read != null) && (folder != null) && (! "".equals(read)) && (! "".equals(folder))) {
            CmsXmlNewsContentDefinition doc2 = new CmsXmlNewsContentDefinition(cms, folder + read);
            result = doc2.getNewsHeadline() + "<P>" 
                     + doc2.getNewsText() + "<P>"
                     + doc2.getNewsDate() + "<P>";
            return result;
        } else {
            throw new CmsException("Cannot Read Article");
        }
    }        
    
    public boolean shouldReload() {
        return false;
    }
}
