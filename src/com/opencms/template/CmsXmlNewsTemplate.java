package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Template class for displaying news articles of the content type
 * <CODE>CmsXmlNewsContentDefinition</CODE>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
 * @see com.opencms.template.CmsXmlNewsContentDefinition
 */
public class CmsXmlNewsTemplate extends CmsXmlTemplate implements I_CmsLogChannels {
    
    /** 
     * Any results of this class are cacheable since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean isCacheable() {
        return true;
    }
        
    /**
     * Gets the content of a given template file.
     * <P>
     * While processing the template file the user methods
     * <code>newsList</code> and <code>article</code> will be used
     * to display data of the content type "news article".
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException 
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        String selector = "";
        
        String read = (String)parameters.get(elementName + ".read");        
        if(read != null && ! "".equals(read)) {
            selector = "read";
        }
            
        byte[] h = getContent(cms, templateFile, elementName, parameters, selector);
        return h;
    }
        
    /**
     * Gets out a list of all available news articles.
     * The news folder is selected by the <code>elem1.newsfolder</code> parameter.<BR>
     * <P>
     * Called by the template file using <code>&lt;METHOD name="newsList"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return List of all articles.
     */
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
 
    /**
     * Gets a selected article with its headline and date.<BR>
     * The article is selected by the <code>elem1.read</code> parameter.<BR>
     * The news folder is selected by the <code>elem1.newsfolder</code> parameter.<BR>
     * <P>
     * Called by the template file using <code>&lt;METHOD name="article"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return Selected Article.
     */
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
    
    /**
     * <EM>Not implemented</EM>. Always returns <CODE>false</CODE>.
     * @return <CODE>false</CODE>
     */
    public boolean shouldReload() {
        return false;
    }
}
