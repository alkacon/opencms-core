/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlLanguageFile.java,v $
* Date   : $Date: 2003/01/20 23:53:00 $
* Version: $Revision: 1.37 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.workplace;

/**
 * Content definition for language files.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.37 $ $Date: 2003/01/20 23:53:00 $
 */
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.I_CmsXmlContent;
import com.opencms.template.I_CmsXmlParser;

import java.io.OutputStream;
import java.io.Writer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CmsXmlLanguageFile implements I_CmsXmlContent{
	
	private CmsXmlLanguageFileContent m_languageFile;
	
    /**
     * Default constructor.
     */
    public CmsXmlLanguageFile() throws CmsException {
        m_languageFile = new CmsXmlLanguageFileContent();
    }
    
    /**
     * Default constructor.
     */
    public CmsXmlLanguageFile(CmsXmlLanguageFileContent langFile) throws CmsException {
        m_languageFile = langFile;
    }
    
    /**
     * Constructor for creating a new language file object containing the content
     * of the corresponding system language file for the actual user.
     * <P>
     * The position of the language file will be looked up in workplace.ini.
     * The selected language of the current user can be searched in the user object.
     *
     * @param cms CmsObject object for accessing system resources.
     */    
	public CmsXmlLanguageFile(CmsObject cms) throws CmsException{
		String curLanguage = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
		m_languageFile = (CmsXmlLanguageFileContent)(CmsXmlWpTemplateFile.getLangFileFromCache(curLanguage));
		if(m_languageFile == null){
			m_languageFile = new CmsXmlLanguageFileContent(cms);
		}
	}

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */
    public CmsXmlLanguageFile(CmsObject cms, CmsFile file) throws CmsException {
        m_languageFile = new CmsXmlLanguageFileContent(cms, file);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */
    public CmsXmlLanguageFile(CmsObject cms, String filename) throws CmsException {
        m_languageFile = new CmsXmlLanguageFileContent(cms, filename);
    }
    
	//
	// The methods from the interface
	//
    /**
     * Creates a clone of this object.
     * @return cloned object
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException{
    	return m_languageFile.clone();
    }
    
    /**
     * Gets the absolute filename of the XML file represented by this content class
     * @return Absolute filename
     */
    public String getAbsoluteFilename(){
    	return m_languageFile.getAbsoluteFilename();
    }
    
    /**
     * Gets a short filename (without path) of the XML file represented by this content class
     * of the template file.
     * @return filename
     */
    public String getFilename(){
    	return m_languageFile.getFilename();
    }
    
    /**
     * Prints the XML parsed content to a String
     * @return String with XML content
     */
    public String getXmlText(){
    	return m_languageFile.getXmlText();
    }
    
    /**
     * Prints the XML parsed content of this template file
     * to the given Writer.
     * 
     * @param out Writer to print to.
     */
    public void getXmlText(Writer out){
    	m_languageFile.getXmlText(out);
    }
    
    /**
     * Prints the XML parsed content of the given Node and
     * its subnodes to the given Writer.
     * 
     * @param out Writer to print to.
     * @param n Node that should be printed.
     */
    public void getXmlText(Writer out, Node n){
    	m_languageFile.getXmlText(out,n);
    }
    
    /**
     * Prints the XML parsed content of a given node and 
     * its subnodes to a String
     * @param n Node that should be printed.
     * @return String with XML content
     */
    public String getXmlText(Node n){
    	return m_languageFile.getXmlText(n);
    }
    
    /**
     * Initialize the XML content class.
     * Load and parse the content of the given CmsFile object.
     * @param cms CmsObject Object for accessing resources.
     * @param file CmsFile object of the file to be loaded and parsed.
     * @throws CmsException
     */
    public void init(CmsObject cms, CmsFile file) throws CmsException{
    	m_languageFile.init(cms, file);
    }
    
    /**
     * Initialize the XML content class.
     * Load and parse the file given by filename,
     * @param cms CmsObject Object for accessing resources.
     * @param filename Filename of the file to be loaded.
     * @throws CmsException
     */
    public void init(CmsObject cms, String filename) throws CmsException{
    	m_languageFile.init(cms, filename);
    }
    
    /**
     * Initialize the class with the given parsed XML DOM document.
     * @param cms CmsObject Object for accessing system resources.
     * @param document DOM document object containing the parsed XML file.
     * @param filename OpenCms filename of the XML file.
     * @throws CmsException
     */
    public void init(CmsObject cms, Document content, String filename) throws CmsException{
    	m_languageFile.init(cms, content, filename);
    }
    
    /**
     * Parses the given file and stores it in the internal list of included files and
     * appends the relevant data structures of the new file to its own structures.
     * 
     * @param include Filename of the XML file to be included
     * @throws CmsException
     */
    public A_CmsXmlContent readIncludeFile(String filename) throws CmsException{
    	return m_languageFile.readIncludeFile(filename);
    }
    

	/**
	 * Method returns content encoding attached to this language.
 	 * @param cms
	 * @return String
	 */
	public String getEncoding() {
	   String result = null;
	   try {
		   result = getLanguageValue(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING);
	   } catch (CmsException e) {;}
	   if ((result != null) && result.startsWith("?") && result.endsWith("?")) {
		   return null;
	   }
	   return result;
	}


    /**
     * Writes the XML document back to the OpenCms system. 
     * @throws CmsException  
     */
    public void write() throws CmsException{
    	m_languageFile.write();
    }

	//
	// Methods from the Abstract XmlContent class
	//
	/**
     * Deletes all files from the file cache.
     */
    public static void clearFileCache() {
    	CmsXmlLanguageFileContent.clearFileCache();
    }

    /**
     * Deletes the file represented by the given A_CmsXmlContent from
     * the file cache.
     * @param doc A_CmsXmlContent representing the XML file to be deleted.
     */
    public static void clearFileCache(A_CmsXmlContent doc) {
    	CmsXmlLanguageFileContent.clearFileCache(doc);
    }

    /**
     * Deletes the file with the given key from the file cache.
     * If no such file exists nothing happens.
     * @param key Key of the template file to be removed from the cache.
     */
    public static void clearFileCache(String key) {
    	CmsXmlLanguageFileContent.clearFileCache(key);	
    }

    /**
     * Gets the currently used XML Parser.
     * @return currently used parser.
     */
    public static I_CmsXmlParser getXmlParser() {
        return CmsXmlLanguageFileContent.getXmlParser();
    }
    
    /**
     * Create a new CmsFile object containing an empty XML file of the
     * current content type.
     * The String returned by <code>getXmlDocumentTagName()</code>
     * will be used to build the XML document element.
     * @param cms Current cms object used for accessing system resources.
     * @param filename Name of the file to be created.
     * @param documentType Document type of the new file.
     * @throws CmsException if no absolute filename is given or write access failed.
     */
    public void createNewFile(CmsObject cms, String filename, String documentType) throws CmsException {
    	m_languageFile.createNewFile(cms, filename, documentType);
    }
    
    //[added by Gridnine AB, 2002-06-17]    
    public void getXmlText(OutputStream out) {
    	m_languageFile.getXmlText(out);
    }
    
    //[added by Gridnine AB, 2002-06-17]
    public void getXmlText(OutputStream out, Node n) {
    	m_languageFile.getXmlText(out, n);
    }    

    /**
     * Read the datablocks of the given content file and include them
     * into the own Hashtable of datablocks.
     *
     * @param include completely initialized A_CmsXmlObject to be included
     * @throws CmsExeption
     */
    public void readIncludeFile(A_CmsXmlContent include) throws CmsException {
    	m_languageFile.readIncludeFile(include);
    }

    /**
     * Registers the given tag to be "known" by the system.
     * So this tag will not be handled by the default method of processNode.
     * Under normal circumstances this feature will only be used for
     * the XML document tag.
     * @param tagname Tag name to register.
     */
    public void registerTag(String tagname) {
    	m_languageFile.registerTag(tagname);
    }

    /**
     * Registeres a tagname together with a corresponding method for processing
     * with processNode. Tags can be registered for two different runs of the processNode
     * method. This can be selected by the runSelector.
     * <P>
     * C_REGISTER_FIRST_RUN registeres the given tag for the first
     * run of processNode, just after parsing a XML document. The basic functionality
     * of this class uses this run to scan for INCLUDE and DATA tags.
     * <P>
     * C_REGISTER_MAIN_RUN registeres the given tag for the main run of processNode.
     * This will be initiated by getProcessedData(), processDocument() or any
     * PROCESS tag.
     *
     * @param tagname Tag name to register.
     * @param c Class containing the handling method.
     * @param methodName Name of the method that should handle a occurance of tag "tagname".
     * @param runSelector see description above.
     */
    public void registerTag(String tagname, Class c, String methodName, int runSelector) {
    	m_languageFile.registerTag(tagname, c, methodName, runSelector);
    }  

    /**
     * Deletes this object from the internal XML file cache
     */
    public void removeFromFileCache() {
    	m_languageFile.removeFromFileCache();	
    }

    /**
     * Creates a datablock element by parsing the data string
     * and stores this block into the datablock-hashtable.
     *
     * @param tag Key for this datablock.
     * @param data String to be put in the datablock.
     */
    public void setParsedData(String tag, String data) throws CmsException{
    	m_languageFile.setParsedData(tag,data);	
    }                                      	

    /**
     * Gets a string representation of this object.
     * @return String representation of this object.
     */
    public String toString() {
    	return m_languageFile.toString();
    }
    
	//
	// The methods of the LanguageFile class
	// 
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return m_languageFile.getContentDescription();
    }

    /**
     * Get the code of the language the user prefers.
     * This language will be taken from the user's start settings.
     * If the user hasn't configured a language yet, if will be
     * taken from the "Accept-Language" header of the request.
     * Finally, there is a fallback value (English), if no preferred
     * language can be found or none of the preferred languages exists.
     *
     * @param cms CmsObject for accessing system resources.
     * @return Code of the preferred language (e.g. "en" or "de")
     */
    public static String getCurrentUserLanguage(CmsObject cms) throws CmsException {
    	return CmsXmlLanguageFileContent.getCurrentUserLanguage(cms);
    }

    /**
     * Overridden internal method for getting datablocks.
     * This method first checkes, if the requested value exists.
     * Otherwise it throws an exception of the type C_XML_TAG_MISSING.
     *
     * @param tag requested datablock.
     * @return Value of the datablock.
     * @throws CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getDataValue(String tag) throws CmsException {
    	return m_languageFile.getDataValue(tag);	
    }
        
    /**
     * Gets the language value vor the requested tag.
     * @param tag requested tag.
     * @return Language value.
     */
    public String getLanguageValue(String tag) throws CmsException {
    	return m_languageFile.getLanguageValue(tag);
    }

    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return m_languageFile.getXmlDocumentTagName();
    }
    
    /**
     * Checks if there exists a language value vor the requested tag.
     * @param tag requested tag.
     * @return Language value.
     */
    public boolean hasLanguageValue(String tag) {
    	return m_languageFile.hasLanguageValue(tag);
    }
    
    /**
     * Sets the class specific language section.
     * When requesting a language value this section will be
     * checked first, before looking up the global value.
     * @param section class specific language section.
     */
    public void setClassSpecificLangSection(String section) {
    	m_languageFile.setClassSpecificLangSection(section);
    }
}
