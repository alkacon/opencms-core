/*
 * File   :
 * Date   : 
 * Version: 
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package org.opencms.modules.htmlimport;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypePage;
import com.opencms.util.Encoder;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import source.org.apache.java.util.ExtendedProperties;


/**
 * This class implements the HTML->OpenCms Template converter for OpenCms 5.x.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public class HtmlImport {
    public static final String C_DOWNLOADGALLERIES = I_CmsWpConstants.C_VFS_GALLERY_DOWNLOAD;
    
    // defintition of required constants  
    public static final String C_IMAGEGALLERIES = I_CmsWpConstants.C_VFS_GALLERY_PICS;
    public static final String C_LINKGALLERIES= I_CmsWpConstants.C_VFS_GALLERY_EXTERNALLINKS;    
    
    /** filename of the meta.properties file */
    public static final String C_META_PROPERTIES = "meta.properties";
    
    /** reference to the import thread */
    private static HtmlImportThread m_htmlImportThread;
    
    /** the report for the output */
    private static StringBuffer m_report;
    
    /** the base URL for link modification */
    private URL m_baseUrl;
        
    /** the CmsObject to use */
    private CmsObject m_cms;
    
    /** the destination directory in the OpenCms VFS */
    private String m_destinationDir;
    
    /** he download gallery name */
    private String m_downloadGallery;
    
    /** the encoding used for all imported files */
    private String m_encoding;
    
    /** hashtable of all file extensions in OpenCms */
    private Hashtable m_extensions;
    
    /**
     * Storage for external links, it is filled by the HtmlConverter each time a 
     * new external link is found
     */
    private HashSet m_externalLinks;
    
    /** 
     * the file index contains all resourcenames in the real file system and their renamed ones
     * in the OpenCms VFS
     */
    private HashMap m_fileIndex;
    
    /** the HTML converter to parse and modifiy the content */
    private HtmlConverter m_htmlConverter;
    
    /** the image gallery name */
    private String m_imageGallery;
    
    /**
     * Storage for image alt tags, it is filled by the HtmlConverter each time a 
     * new image is found
     */
    private HashMap m_imageInfo;
    
    /** the input directory in the "real" file system  */
    private String m_inputDir;
    
    /** the external link gallery name */
    private String m_linkGallery;
    
    /** the template use for all pages*/
    private String m_template;
    
    /**
     * Constructor, creates a new HtmlImport.<p>
     * 
     * @param cms the current CmsObject
     * @param inputDir the input directory in the "real" file system 
     * @param destinationDir the destination directory in the OpenCms VFS
     * @param imageGallery the image gallery name
     * @param linkGallery the external link gallery name
     * @param downloadGallery the download gallery name
     * @param template the template use for all pages
     * @param encoding encoding used for importing all pages
     * @param xmlMode flag to use the XML or HMTL import mode
     * @throws CmsException if something goes wrong
     */
    public HtmlImport(CmsObject cms,
                       String inputDir,
                       String destinationDir,
                       String imageGallery,
                       String linkGallery,
                       String downloadGallery,
                       String template,
                       String encoding,
                       boolean xmlMode) throws CmsException {
                           
        // store all member variables
        m_cms = cms;
        m_inputDir=inputDir;
        m_destinationDir = destinationDir;     
        m_imageGallery = C_IMAGEGALLERIES + imageGallery+"/";
        m_linkGallery = C_LINKGALLERIES + linkGallery+"/";
        m_downloadGallery = C_DOWNLOADGALLERIES + downloadGallery+"/";
        m_template = template;    
        m_encoding=encoding;
     
        
        // create all other required member objects
        m_fileIndex = new HashMap();          
        m_externalLinks = new HashSet();
        m_imageInfo = new HashMap();
        m_extensions = cms.readFileExtensions();
        m_htmlConverter = new HtmlConverter(this, xmlMode);            
        m_baseUrl = null;
        try {
            m_baseUrl = new URL("file://");
        } catch (MalformedURLException e) {
            // this won't happen
        }
                     
    }
        
       
    /**
     * Gets the output of the HtmlImportThread.<p>
     * 
     * @return log output of the import threat
     */
    public static String getThreadOutput(){
      String output="";
     // check if we have a thread
      if (m_htmlImportThread!= null) {
          // is it still alive?
          if (m_htmlImportThread.isAlive()) {
              output=m_htmlImportThread.getReportUpdate();
          } else {
              output=new String(m_report);
          }
      }
      return output;      
    }
        
    /**
     * Substitutes searchString in content with replaceItem.<p>
     * 
     * @param content the content which is scanned
     * @param searchString the String which is searched in content
     * @param replaceItem the new String which replaces searchString
     * @return String the substituted String
     */
    public static String substitute(String content, String searchString, String replaceItem) {
        // high performance implementation to avoid regular expression overhead
        int findLength;
        if (content == null) {
            return null;
        }
        int stringLength = content.length();
        if (searchString == null || (findLength = searchString.length()) == 0) {
            return content;
        }
        if (replaceItem == null) {
            replaceItem = "";
        }
        int replaceLength = replaceItem.length();
        int length;
        if (findLength == replaceLength) {
            length = stringLength;
        } else {
            int count;
            int start;
            int end;
            count = 0;
            start = 0;
            while ((end = content.indexOf(searchString, start)) != -1) {
                count++;
                start = end + findLength;
            }
            if (count == 0) {
                return content;
            }
            length = stringLength - (count * (findLength - replaceLength));
        }
        int start = 0;
        int end = content.indexOf(searchString, start);
        if (end == -1) {
            return content;
        }
        StringBuffer sb = new StringBuffer(length);
        while (end != -1) {
            sb.append(content.substring(start, end));
            sb.append(replaceItem);
            start = end + findLength;
            end = content.indexOf(searchString, start);
        }
        end = stringLength;
        sb.append(content.substring(start, end));
        return sb.toString();
    }
    
    /**
     * Checks if the HtmlImportThread is still alive.<p>
     * 
     * @return true or false
     */
    public static boolean threadAlive() {
        boolean alive=false;
        if (m_htmlImportThread!= null) {
            alive=m_htmlImportThread.isAlive();
        }
        return alive;
    }

    /**
     * Builds an index of all files to be imported and determines their new names in the OpenCms.<p>
     * @param startfolder the folder to start with
     */
    private void buildIndex(String startfolder) {
        File folder= new File(startfolder);
        // get all subresources
        File[] subresources = folder.listFiles();
        // now loop through all subresources and add them to the index list
        for (int i=0; i<subresources.length; i++) {
            String relativeFSName = subresources[i].getAbsolutePath().substring(m_inputDir.length()+1);
            String absoluteVFSName = getVfsName(relativeFSName, subresources[i].getName(), subresources[i].isFile());        
            m_report.append("Create Index for "+subresources[i].getAbsolutePath() +" -> "+absoluteVFSName+"<br>");
                    
            m_fileIndex.put(subresources[i].getAbsolutePath().replace('\\', '/'), absoluteVFSName);
            // if the subresource is a folder, get all subresources of it as well
            if (subresources[i].isDirectory()) {
                buildIndex(subresources[i].getAbsolutePath());
            } 
        }        
    }



    /**
     * Tests if all given input parameters for the HTML Import are valid, i.e. that all the 
     * given folders do exist. <p>
     * 
     * @throws CmsParameterValidationException if some parameters are not valid
     */
    public void checkParameters() throws CmsParameterValidationException {
        // error string, will store the any errors
        String error="";                  
        // check the input directory
        File inputDir=new File(m_inputDir);      
        if (!inputDir.exists() || inputDir.isFile()) {
            // the input directory is not valid, so mark this as an error
            error += "[Input Directory not found=" + m_inputDir+"]";
        }       
        // check the destination directory
        try {
            m_cms.readFolder(m_destinationDir);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist, so mark this as an error
            error += "[Destination Folder not found=" + m_destinationDir+"]";
        }      
        // check the image gallery
        try {
            m_cms.readFolder(m_imageGallery);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist, so mark this as an error
            error += "[Image Gallery not found=" + m_imageGallery+"]";
        }  
        // check the link gallery
        try {
            m_cms.readFolder(m_linkGallery);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist, so mark this as an error
            error += "[Link Gallery not found=" + m_linkGallery+"]";
        } 
        // check the download gallery
        try {
            m_cms.readFolder(m_downloadGallery);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist, so mark this as an error
            error += "[Download Gallery not found=" + m_downloadGallery+"]";
        } 
        // check the template
        try {
            m_cms.readFileHeader(m_template);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist, so mark this as an error
            error += "[Template not found=" + m_template+"]";
        }    
        
        // if there were any errors collected, throw a CmsParameterValidationException
        if (error.length()>0) {
           throw new CmsParameterValidationException(error);           
        }
    }     

    
    /**
     * Clear all used indices and lists.<p>
     * 
     * This should only be done when the import has been done. 
     */
    private void clear() {
        m_fileIndex = null;
        m_externalLinks = null;
        m_imageInfo = null;
    }
    
    
    /**
     * Copies all  HTML files to the VFS.<p>
     * 
     * @param startfolder startfolder the folder to start with
     * @throws CmsException if something goes wrong
     */
    private void copyHtmlFiles(String startfolder) throws CmsException {
        try {
        File folder= new File(startfolder);
        // get all subresources
        File[] subresources = folder.listFiles();
        // now loop through all subresources 
        for (int i=0; i<subresources.length; i++) {
            // if the subresource is a folder, get all subresources of it as well          
            if (subresources[i].isDirectory()) {
                // first, create the folder in the VFS    
                Hashtable properties= new Hashtable();
                createFolder(subresources[i].getAbsolutePath(), i, properties);     
                // now process all rescources inside of the folder
                copyHtmlFiles(subresources[i].getAbsolutePath());                
            } else {
                // create a new file in the VFS      
                String vfsFileName = (String)m_fileIndex.get(subresources[i].getAbsolutePath().replace('\\', '/')); 
                // check if this is an Html file, do only import and parse those
                String type=getFileType(vfsFileName);
                if (type.equals("plain")) {                                 
                    Hashtable properties= new Hashtable();
                    // the subresource is a file, so start the parsing process
                     String content=parseHtmlFile(subresources[i], properties);
                    // create the file in the VFS
                    createFile(subresources[i].getAbsolutePath(), i, content, properties);
                }
            }
        }
        } catch (CmsException e) {
            m_report.append(e);
            e.printStackTrace();
        }
    }
    
    

    /**
     * Copies all files except HTML files to the VFS.<p>
     * 
     * @param startfolder startfolder the folder to start with
     * @throws CmsException if something goes wrong
     */
    private void copyOtherFiles(String startfolder) throws CmsException {
        File folder= new File(startfolder);
        // get all subresources
        File[] subresources = folder.listFiles();
        // now loop through all subresources 
        for (int i=0; i<subresources.length; i++) {
            // if the subresource is a folder, get all subresources of it as well
            if (subresources[i].isDirectory()) {
                copyOtherFiles(subresources[i].getAbsolutePath());                
            } else {
                // do not import the "meta.properties" file
                if (!subresources[i].getName().equals(C_META_PROPERTIES)) {
                    // create a new file in the VFS      
                    String vfsFileName = (String)m_fileIndex.get(subresources[i].getAbsolutePath().replace('\\', '/'));    
                    // get the file type of the FS file
                    String type=getFileType(vfsFileName);
    
                    if (!type.equals("plain")) {
                    
                        m_report.append("import "+ subresources[i] +" -> "+vfsFileName+"<br>");
                         // get the content of the FS file
                        byte[] content = getFileBytes(subresources[i]);
                        // get the filename from the fileIndex list
                       
                        // check if there are some image infos stored for this resource
                        HashMap properties=new HashMap();
                        String altText=(String)m_imageInfo.get(subresources[i].getAbsolutePath().replace('\\', '/'));                
                        // add them to the title and description property
                        if (altText!=null) {
                            properties.put(I_CmsConstants.C_PROPERTY_DESCRIPTION, altText);
                            properties.put(I_CmsConstants.C_PROPERTY_TITLE, altText);
                        }
                        // create the file
                        m_cms.createResource(vfsFileName, type, properties, content, null);
                    }  
                }
            }         
        }   
    }
    

    /**
     * Creates all external links, which were found during the HTML-page processing.<p>
     * 
     */
    private void createExternalLinks() {
        // loop through all links
        Iterator i=m_externalLinks.iterator();
        while (i.hasNext()) {
            String linkUrl = (String)i.next();
            String filename = linkUrl.substring(linkUrl.indexOf("://")+3, linkUrl.length());
            filename = m_cms.getRequestContext().getFileTranslator().translateResource(filename.replace('/', '-'));            
 
            m_report.append("Creating External link to "+linkUrl+ " -> "+filename+"<br>");
           
            HashMap properties=new HashMap();
            properties.put("Title", "Link to "+linkUrl);
            try {
                m_cms.createResource(m_linkGallery, filename, "link", properties, linkUrl.getBytes());
            } catch (CmsException e) {               
                // do nothing here, an exception will be thrown if this link already exisits
            }
        }
    }



    /**
     * Creates a file in the VFS.<p>
     * 
     * @param filename the complete filename in the real file system
     * @param position the default nav pos of this folder
     * @param content the html content of the file
     * @param properties the file properties
     */
    private void createFile(String filename, int position, String content, Hashtable properties) {
     
        String vfsFileName= (String)m_fileIndex.get(filename.replace('\\', '/'));
 
        m_report.append("Creating file "+filename+ " -> "+vfsFileName+"<br>");

        
        if (vfsFileName!= null) {                     
            try {
               // check if we have to set the navpos property.
                if (properties.get(I_CmsConstants.C_PROPERTY_NAVPOS)==null) {
                    // set the position in the folder as navpos
                    // we have to add one to the postion, since it is counted from 0
                    properties.put(I_CmsConstants.C_PROPERTY_NAVPOS, (position+1)+"");
                }  
               CmsResource newFile=((CmsResourceTypePage)m_cms.getResourceType("page")).createResourceForTemplate(m_cms, vfsFileName, new Hashtable(), content.getBytes(), m_template);

               // now write all properties as well
               Enumeration enu = properties.keys();
               while (enu.hasMoreElements()) {
                   // get property and value
                   String property=(String)enu.nextElement();
                   String propertyvalue=(String) properties.get(property);
                   //try to write the property
                   try {
                       m_cms.writeProperty(newFile.getAbsolutePath(), property, propertyvalue);
                   } catch (CmsException e1) {
                       // the propertydefinition did not exist, so create it first
                       m_cms.createPropertydefinition(property, "page");
                       // now write the propertydefintion again
                       m_cms.writeProperty(newFile.getAbsolutePath(), property, propertyvalue);                        
                   }
               }

            } catch (CmsException e) {
                m_report.append(e);
                e.printStackTrace();
             
          }
                   
        }
    }


    /**
     * Creates a folder in the VFS.<p>
     * 
     * @param foldername the complete foldername in the real file system
     * @param position the default nav pos of this folder
     * @param properties the file properties
     */
    private void createFolder(String foldername, int position, Hashtable properties) {
      
        String vfsFolderName= (String)m_fileIndex.get(foldername.replace('\\', '/'));

        m_report.append("Creating folder "+foldername+ " -> "+vfsFolderName+"<br>");
       
        if (vfsFolderName!= null) {              
            String path = vfsFolderName.substring(0, vfsFolderName.substring(0, vfsFolderName.length()-1).lastIndexOf("/"));
            String folder = vfsFolderName.substring(path.length(), vfsFolderName.length());
            try {
                // try to find a meta.properties file in the folder
                String propertyFileName=foldername+File.separator+C_META_PROPERTIES;
         
                ExtendedProperties propertyFile = new ExtendedProperties(); 
                try {                   
                    propertyFile.load(new FileInputStream(new File(propertyFileName)));
                } catch (Exception e1) {
                    // do nothing if the propertyfile could not be loaded since it is not required
                    // that such s file does exist
                }
                // now copy all values from the propertyfile to the already found properties of the
                // new folder in OpenCms
                Enumeration enu = propertyFile.keys();
                while (enu.hasMoreElements()) {
                       // get property and value
                       try {
                           String property=(String)enu.nextElement();
                           String propertyvalue=(String) propertyFile.get(property);
                           // copy to the properties of the OpenCms folder
                           properties.put(property, propertyvalue);
                       } catch (Exception e2) {
                           // just skip this property if it could ne be read.
                       }
                }
                                
                // check if we have to set the navpos property.
                if (properties.get(I_CmsConstants.C_PROPERTY_NAVPOS)==null) {
                    // set the position in the folder as navpos
                    // we have to add one to the postion, since it is counted from 0
                    properties.put(I_CmsConstants.C_PROPERTY_NAVPOS, (position+1)+"");
                }
                // check if we have to set the navpos property.
                if (properties.get(I_CmsConstants.C_PROPERTY_NAVTEXT)==null) {
                    // set the foldername in the folder as navtext
                    String navtext=folder.substring(1, 2).toUpperCase()+folder.substring(2, folder.length()-1);
                    properties.put(I_CmsConstants.C_PROPERTY_NAVTEXT, navtext);
                }
                CmsResource newFolder;
                // try to read the folder, it its there we must not create it again
                try {
                    newFolder = m_cms.readFolder(path+folder);
                } catch (CmsException e1) {
                    // the folder was not there, so create it
                    newFolder = m_cms.createResource(path, folder, "folder");
                }
                // now write all properties as well
                enu = properties.keys();
                while (enu.hasMoreElements()) {
                    // get property and value
                    String property=(String)enu.nextElement();
                    String propertyvalue=(String) properties.get(property);
                    //try to write the property
                    try {
                        m_cms.writeProperty(newFolder.getAbsolutePath(), property, propertyvalue);
                    } catch (CmsException e1) {
                        // the propertydefinition did not exist, so create it first
                        m_cms.createPropertydefinition(property, "folder");
                        // now write the propertydefintion again
                        m_cms.writeProperty(newFolder.getAbsolutePath(), property, propertyvalue);                        
                    }
                }                
            } catch (CmsException e) {
              m_report.append(e);  
              e.printStackTrace();            
            }
                   
        }
    }

    /**
     * Starts the HtmlImportThread.<p>
     *  
     * @throws CmsException if something goes wrong
     */
    public void doImport() throws CmsException {
        m_htmlImportThread = new HtmlImportThread(m_cms,this);
        m_htmlImportThread.start();         
    }
    
    
    /**
     * Calculates an absolute uri from a relative "uri" and the given absolute "baseUri".<p> 
     * 
     * If "uri" is already absolute, it is returned unchanged.
     * This method also returns "uri" unchanged if it is not well-formed.<p>
     *    
     * @param relativeUri the relative uri to calculate an absolute uri for
     * @param baseUri the base uri, this must be an absolute uri
     * @return an absolute uri calculated from "uri" and "baseUri"
     */
    public  String getAbsoluteUri(String relativeUri, String baseUri) {         
        if ((relativeUri == null) || (relativeUri.charAt(0) == '/') || (relativeUri.startsWith("#"))) {
            return relativeUri;
        }
        try {            
            URL url = new URL(new URL(m_baseUrl, "file://"+baseUri), relativeUri);
            if (url.getQuery() == null) {                       
                return url.getHost()+":"+url.getPath();             
            } else {
                return url.getHost()+":"+url.getPath() + "?" + url.getQuery();
            }
        } catch (MalformedURLException e) {           
            return relativeUri;
        }
    }

    /**
     * Returns a byte array containing the content of server FS file.<p>
     *
     * @param file the name of the file to read
     * @return bytes[] the content of the file
     * @throws CmsException if something goes wrong
     */
    private byte[] getFileBytes(File file) throws CmsException {
        byte[] buffer = null;
        
        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            buffer = new byte[size];
            while (charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            return buffer;
        } catch (IOException e) {
            throw new CmsException(e.getMessage());
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) { }
        }
    }


    /**
     * Gets the OpenCms file type of a real filesystem file. <p>
     * 
     * This is made by checking the extension.
     * 
     * @param filename the name of the file in the real filesystem  
     * @return the name of the OpenCms file type
     */
    private String getFileType(String filename) {
        String extension="";
        if (filename.indexOf(".")>0) {
            extension=filename.substring((filename.lastIndexOf(".")+1));
        }
        String filetype = (String)m_extensions.get(extension.toLowerCase());
        if (filetype == null) {
            filetype="binary";
        }
        return filetype;
    }
        
    /**
     * Gets a valid VfsName form a given name in the real filesystem.<p>
     * 
     * This name will ater be used for all link translations during the HTML-parsing process.
     * @param relativeName the name in the real fielsystem, relative to the start folder
     * @param name the name of the file
     * @param isFile flag to indicate that the resource is a file
     * @return a valid name in the VFS
     */
    private String getVfsName(String relativeName, String name, boolean isFile) {
        // first translate all fileseperators to the valid "/" in OpenCms
        String vfsName=relativeName.replace('\\', '/');
        // the resource is a file
        if (isFile) {
            // we must check if it might be copied into a gallery. this can be done by checking the
            // file extension
            String filetype=getFileType(name);

            // depending on the filetype, the resource must be moved into a speical folder in 
            // OpenCms:
            // images -> move into image gallery
            // binary -> move into download gallery
            // plain -> move into destination folder
            // other -> move into download gallery
            if (filetype.equals("image")) {
                // move to image gallery
                // as the image gallery is "flat", we must use the file name and not the complete
                // relative name
                vfsName=m_imageGallery+name;
            } else if (filetype.equals("plain")) {
                // move to destination folder
                vfsName=m_destinationDir+relativeName;                
            } else {
                // everything else will be moved to the download gallery.
                // as the download gallery is "flat", we must use the file name and not the complete
                // relative name
                vfsName=m_downloadGallery+name;
            }
            // now we have the filename in the VFS. its possible that a file with the same name
             // is already existing, in this case, we have to adjust the filename.        
             return validateFilename(vfsName);
        } else {
            // folders are always moved to the destination folder
            vfsName=m_destinationDir+vfsName+"/";  
            return vfsName;
        }
    }


    /**
     * Reads the content of an Html file from the real file system and parses it for link
     * transformation.<p>
     * @param  file the filein the real file system
     * @param properties the file properties
     * @return the modified Html code of the file
     * @throws CmsException if something goes wrong
     */
    private String parseHtmlFile(File file, Hashtable properties) throws CmsException {
        String parsedHtml="";
        try {
          
            byte[] content = getFileBytes(file);

            // use the correct encoding to get the string from the file bytes
            String contentString = new String(content, m_encoding);
            // escape the string to remove all special chars
            contentString = Encoder.escapeNonAscii(contentString);
            // we must substitute all occurences of "&#", otherwiese tidy would remove them
            contentString=substitute(contentString, "&#", "{subst}");
            // parse the content                  
            parsedHtml= m_htmlConverter.convertHTML(file.getAbsolutePath(), contentString, properties);
            // resubstidute the converted HTML code
            parsedHtml=substitute(parsedHtml, "{subst}", "&#");
            
        } catch (Exception e) {
            throw new CmsException(e.getMessage());
        }
        return parsedHtml;
    }
    
    
    /**
      * Imports all resources from the real filesystem, stores them into the correct locations
      * in the OpenCms VFS and modifies all links. This method is called form the JSP to start the
      * import process.<p>
      * @throws CmsException if something goes wrong
      */
     public void startImport(StringBuffer report) throws CmsException {
         try {
             m_report=report;
             // first build the index of all resources
             buildIndex(m_inputDir);
             // copy and parse all html files first. during the copy process we will collect all 
             // required data for downloads and images
             copyHtmlFiles(m_inputDir);
             // now copy the other files
             copyOtherFiles(m_inputDir);
             // finally create all the external links    
             createExternalLinks();
         } catch (Exception e) {
             m_report.append(e);
             e.printStackTrace();
         } finally {
             // clear memory
             clear();
         }
         
     }

    /**
     * Add a new external link to the storage of external links.<p>
     * 
     * All links in this storage are later used to create entries in the external link gallery.
     * @param externalLink link to an external resource
     */
    public void storeExternalLink(String externalLink) {
        m_externalLinks.add(externalLink);
    }

    /**
     * Add a new image info to the storage of image infos.<p>
     * 
     * The image infoes are later used to set the description properties of the images.
     * @param image the name of the image
     * @param altText the alt-text of the image
     */
    public void storeImageInfo(String image, String altText) {       
        m_imageInfo.put(image, altText);
    }

    
    
    /**
     * Translated a link into the real filesystem to its new location in the OpenCms VFS.<p>
     * 
     * This is needed by the HtmlConverter to get the correct links for link translation.
     * @param link link to the reafl filesystem
     * @return string containing absulute link into the OpenCms VFS
     */
    public String translateLink(String link) {        
        String translatedLink;
        translatedLink=(String)m_fileIndex.get(link.replace('\\', '/'));
        if (translatedLink==null) {
            translatedLink=link;
        }
        return translatedLink;
    }

    
    /**
     * Validates a fielname for OpenCms.<p>
     * 
     * This method checks if there are any illegal characters in the fielname and modifies them
     * if nescessary. In addition it ensures that no dublicate filenames are created.
     * 
     * @param filename the filename to validate
     * @return a validated and unique filename in OpenCms
     */
    private String validateFilename(String filename) {
        // check if this resource name does already exist
        // if so add a postfix to the name
       
        int postfix = 1;
        boolean found = true;
        String validFilename=filename.toLowerCase();      

        while (found) {
            try {
              //  get the translated name, this one only contains valid chars in OpenCms       
                validFilename =  m_cms.getRequestContext().getFileTranslator().translateResource(validFilename);
      
                // try to read the file.....
                found = true;
                // first try to read it form the fileIndex of already processed files
                if (!m_fileIndex.containsValue(validFilename.replace('\\', '/'))) {                   
                    found = false;                              
                }
                if (!found) {  
                    found=true;   
                    // there was no entry in the fileIndex, so try to read from the VFS         
                    m_cms.readFileHeader(validFilename);                   
                }
                // ....it's there, so add a postfix and try again
                String path = filename.substring(0, filename.lastIndexOf("/") + 1);              
                String name = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                validFilename = path;
                if (name.lastIndexOf(".") > 0) {
                    validFilename += name.substring(0, name.lastIndexOf("."));
                } else {
                    validFilename += name;
                }
                validFilename += "_" + postfix;
                if (name.lastIndexOf(".") > 0) {
                    validFilename += name.substring(name.lastIndexOf("."), name.length());
                }
                postfix++;                
            } catch (CmsException e) {
                // the file does not exist, so we can use this filename                               
                found = false;
            }
        }               
        return validFilename;
    }
    
}
