package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import java.util.*;


/**
 * Sample content definition for news articles.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
 */
 public class CmsXmlNewsContentDefinition extends A_CmsXmlContent {

    /**
     * Default constructor.
     */
    public CmsXmlNewsContentDefinition() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlNewsContentDefinition(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlNewsContentDefinition(A_CmsObject cms, String filename) throws CmsException {
        super();            
        init(cms, filename);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "NEWSARTICLE";
    }

    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms news article";
    }
            
    /**
     * Gets the content of the headline.
     * @return Headline
     */
    public String getNewsHeadline() {
        return getDataValue("HEADLINE");
    }
    
    /**
     * Gets the date of the article:
     * @return Date.
     */
    public String getNewsDate() {
        return getDataValue("DATE");
    }
    
    /**
     * Gets the article text.
     * @return Article text.
     */
    public String getNewsText() {
        return getDataValue("TEXT");
    }
    
    /**
     * Gets an enumeration of all articles in a folder.
     * @param cms A_CmsObject object for accessing system resources.
     * @param folder Name of the folder to scan for articles.
     */
    public static Enumeration getAllArticles(A_CmsObject cms, String folder) throws CmsException {
        Vector allFiles = null;
        try {
            allFiles = cms.getFilesInFolder(folder);
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlNewsContentDefinition] " + e);
            }
            allFiles = null;
        }
        if(allFiles == null) {
            String errorMessage = "Could not read news article files in folder " + folder;
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlNewsContentDefinition] " + errorMessage);
            }
            throw new CmsException(errorMessage);
        }
        Vector listFiles = new Vector();
        int numFiles = allFiles.size();
        for(int i=0; i<numFiles; i++) {
            CmsXmlNewsContentDefinition newsDoc = new CmsXmlNewsContentDefinition();
            CmsFile fileHeader = (CmsFile)allFiles.elementAt(i);
            CmsFile file = cms.readFile(fileHeader.getAbsolutePath());
            newsDoc.init(cms, file);
            listFiles.addElement(newsDoc);
        }
        return listFiles.elements();        
    }    
}
