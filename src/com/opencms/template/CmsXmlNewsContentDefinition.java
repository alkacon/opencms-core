package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import java.util.*;

public class CmsXmlNewsContentDefinition extends A_CmsXmlContent {

    public CmsXmlNewsContentDefinition() throws CmsException {
        super();
    }
    
    public CmsXmlNewsContentDefinition(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }

    public CmsXmlNewsContentDefinition(A_CmsObject cms, String filename) throws CmsException {
        super();            
        init(cms, filename);
    }
    
    public String getXmlDocumentTagName() {
        return "NEWSARTICLE";
    }

    public String getContentDescription() {
        return "OpenCms news article";
    }
            
    public String getNewsHeadline() {
        return getDataValue("HEADLINE");
    }
    
    public String getNewsDate() {
        return getDataValue("DATE");
    }
    
    public String getNewsText() {
        return getDataValue("TEXT");
    }
    
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
