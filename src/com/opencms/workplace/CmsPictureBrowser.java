package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms picture browser.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/02/03 09:38:38 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsPictureBrowser extends CmsWorkplaceDefault {

    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }    

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }

        A_CmsRequestContext reqCont = cms.getRequestContext();
        String folder = (String)parameters.get(C_PARA_FOLDER);
        String pageText = (String)parameters.get(C_PARA_PAGE);
        String filter = (String)parameters.get(C_PARA_FILTER);
                
        // Check if the user requested a special folder
        if(folder == null || "".equals(folder)) {
            folder = getConfigFile(cms).getPicturePath();
            parameters.put(C_PARA_FOLDER, folder);
        }

        // Check if the user requested a special page
        if(pageText == null ||"".equals(pageText))  {
            pageText = "1";
            parameters.put(C_PARA_PAGE, pageText);
        }
        
        // Check if the user requested a filter
        if(filter == null || "".equals(filter)) {
            filter = "*";
            parameters.put(C_PARA_FILTER, filter);
        }
        
        // Compute the maximum page number
        int maxpage = ((cms.getFilesInFolder(folder).size()-1)/C_PICBROWSER_MAXIMAGES)+1;
                        
        // Now load the template file and set the appropriate datablocks
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        xmlTemplateDocument.setXmlData(C_PARA_FOLDER, folder);
        xmlTemplateDocument.setXmlData(C_PARA_PAGE, pageText);
        xmlTemplateDocument.setXmlData(C_PARA_FILTER, filter);
        xmlTemplateDocument.setXmlData(C_PARA_MAXPAGE, "" + maxpage);
        
        // Start the processing        
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }                    


    /**
     * User method to generate an URL for the pics folder.
     * <P>
     * All pictures should reside in the docroot of the webserver for
     * performance reasons. This folder can be mounted into the OpenCms system to 
     * make it accessible for the OpenCms explorer.
     * <P>
     * The path to the docroot can be set in the workplace ini.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @exception CmsException
     */    
    public Object pictureList(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {

        Hashtable parameters = (Hashtable)userObj;
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)doc;

        StringBuffer result = new StringBuffer();

        String folder = (String)parameters.get(C_PARA_FOLDER);
        String pageText = (String)parameters.get(C_PARA_PAGE);
        String filter = (String)parameters.get(C_PARA_FILTER);

        // Get all pictures in the given folder using the cms object
        Vector allPics = cms.getFilesInFolder(folder);
        int numPics = allPics.size();
        
        // Get limits for the requested page
        int page = new Integer(pageText).intValue();
        int from = (page-1) * C_PICBROWSER_MAXIMAGES;
        int to = ((from+C_PICBROWSER_MAXIMAGES)>numPics)?numPics:(from+C_PICBROWSER_MAXIMAGES);
        
        String picsUrl = getConfigFile(cms).getPictureUrl();
        
        // Generate the picture list
        for(int i=from; i<to; i++) {
            CmsFile file = (CmsFile)allPics.elementAt(i);
            String filename = file.getName();
            if(inFilter(filename, filter) && isImage(filename)) {
                result.append(xmlTemplateDocument.getProcessedXmlDataValue("picstartseq", this, userObj));
                result.append(picsUrl + file.getName());
                result.append(xmlTemplateDocument.getProcessedXmlDataValue("picendseq", this, userObj));
                result.append(xmlTemplateDocument.getProcessedXmlDataValue("textstartseq", this, userObj));
                result.append(file.getLength() + " Bytes\n");
                result.append(xmlTemplateDocument.getProcessedXmlDataValue("textendseq", this, userObj));
            }
        }
        return result.toString();
    }

    /**
     * Checks, if the given filename matches the filter.
     * @param filename filename to be checked.
     * @param filter filter to be checked.
     * @return <code>true</code> if the filename matches the filter, <code>false</code> otherwise.
     */
    private boolean inFilter(String filename, String filter) {
        if("*".equals(filter) || (filename.indexOf(filter) != -1)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Checks, if the given filename ends with a typical picture suffix.
     * @param filename filename to be checked.
     * @return <code>true</code> if the is an image file, <code>false</code> otherwise.
     */
    private boolean isImage(String filename) {
        if(filename.toLowerCase().endsWith("gif") 
                || filename.toLowerCase().endsWith("jpg") 
                || filename.toLowerCase().endsWith("jpeg")) {
            return true;
        } else {
            return false;
        }
    }        
}
