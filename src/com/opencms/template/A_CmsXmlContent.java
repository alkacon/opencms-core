/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/A_CmsXmlContent.java,v $
 * Date   : $Date: 2000/06/25 11:40:24 $
 * Version: $Revision: 1.29 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.template;

import java.io.*;
import java.util.*;

import java.lang.reflect.*;
import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.launcher.*;

// import com.sun.xml.*;
// import com.sun.xml.tree.*;
                  
//import org.apache.xerces.*;
//import org.apache.xerces.dom.*;
//import org.apache.xerces.parsers.*;

/** 
 * Abstract class for OpenCms files with XML content.
 * <P>
 * This class implements basic functionality for OpenCms XML files.
 * For each XML file content type (e.g. XML template files, XML 
 * control files, XML news article files, ...) a customized
 * class extending this abstract class has to be implemented.
 * <P>
 * The functionality of this class is:
 * <UL>
 * <LI>control the XML parser</LI>
 * <LI>recognize and handle special XML tags used in OpenCms environment</LI>
 * <LI>cache parsed documents, so that that they can be re-used</LI>
 * <LI>provide methods to access XML data</LI>
 * </UL>
 * <P>
 * After creating a new instance of the children of this class it has to be 
 * initialized by calling the init method.
 * <P>
 * While initializing the content of the given file will be read
 * and parsed with the XML parser. After this, the parsed 
 * document will be scanned for INCLUDE tags and for DATA tags.
 * DATA tags will be stored in an internal Hashtable an can 
 * easily be accessed by the getData methods or by a PROCESS tag.
 * <P>
 * Extending classes have to implement the abstract methods 
 * getXmlDocumentTagName() and getContentDescription().
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.29 $ $Date: 2000/06/25 11:40:24 $
 */
public abstract class A_CmsXmlContent implements I_CmsXmlContent, I_CmsLogChannels { 
    
    /** parameter types for XML node handling methods. */
    public static final Class[] C_PARAMTYPES_HANDLING_METHODS 
            = new Class[] {Element.class, Object.class, Object.class};
    
    /** parameter types for user methods called by METHOD tags */
    public static final Class[] C_PARAMTYPES_USER_METHODS
            = new Class[] {CmsObject.class, String.class, A_CmsXmlContent.class, Object.class};
    
	/** The classname of the super XML content class */
	public static final String C_MINIMUM_CLASSNAME = "com.opencms.template.A_CmsXmlContent";

	/** Constant pathname, where to find templates */
	public static final String C_TEMPLATEPATH = "/system/workplace/templates/";

	/** Constant extension of the template-files. */
	public static final String C_TEMPLATE_EXTENSION = "";
    
    /** Error message for bad <code>&lt;PROCESS&gt;</code> tags */
    public static final String C_ERR_NODATABLOCK = "? UNKNOWN DATABLOCK ";
    
    /** CmsObject Object for accessing resources */
    protected CmsObject m_cms;
        
    /** All XML tags known by this class. */
    protected Vector m_knownTags = new Vector();
    
    /**
     * This Hashtable contains some XML tags as keys
     * and the corresponding methods as values.
     * Used to pass to processNode() to read in
     * include files and scan for datablocks.
     */
    protected Hashtable m_firstRunTags = new Hashtable();
    
    /**
     * This Hashtable contains some XML tags as keys
     * and the corresponding methods as values.
     * Used to pass to processNode() before generating
     * HTML output.
     */
    protected Hashtable m_mainProcessTags = new Hashtable();

    /** constant for registering handling tags */
    protected final static int C_REGISTER_FIRST_RUN = 1;
    
    /** constant for registering handling tags */
    protected final static int C_REGISTER_MAIN_RUN = 2;
    
    /** Boolean for additional debug output control */
    private static final boolean C_DEBUG = false;

    /** DOM representaion of the template content. */
    private Document m_content;

    /** Filename this template was read from */
    private String m_filename;
  
	/** All datablocks in DOM format */
    private Hashtable m_blocks = new Hashtable();                                                        
                                                                                                          
    /** Reference All included A_CmsXmlContents */
    private Vector m_includedTemplates = new Vector();

    /** Cache for parsed documents */
    static private Hashtable m_filecache = new Hashtable();

 	/** XML parser */   
    private static I_CmsXmlParser parser = new CmsXmlXercesParser();
    // private static I_CmsXmlParser parser = new CmsXmlProjectXParser();

	/** Constructor for creating a new instance of this class */    
    public A_CmsXmlContent() {
        registerAllTags();
    }
                 
    /**
     * Initialize the XML content class.
     * Load and parse the content of the given CmsFile object.
     * <P>
     * If a previously cached parsed content exists, it will be re-used.
     * <P>
     * If no absolute file name ist given, 
     * template files will be searched a hierachical order using
     * <code>lookupAbsoluteFilename</code>.
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param file CmsFile object of the file to be loaded and parsed.
     * @exception CmsException
     * @see #lookupAbsoluteFilename
     */
    public void init(CmsObject cms, String filename) throws CmsException {
        if(! filename.startsWith("/")) {
            // this is no absolute filename. 
            filename = lookupAbsoluteFilename(cms, filename, this);
        }
        String currentProject = cms.getRequestContext().currentProject().getName();
        Document parsedContent = null;

        m_cms = cms;
        m_filename = filename;
        parsedContent = loadCachedDocument(filename);
        if(parsedContent == null) {   
            CmsFile file = cms.readFile(filename);
            parsedContent = parse(new String(file.getContents()));                
            m_filecache.put(currentProject + ":" + filename, parsedContent.cloneNode(true));
        } else {
            // File was found in cache.
            // We have to read the file header to check access rights.
            cms.readFileHeader(filename);
        }
        init(cms, parsedContent, filename);                                                     
    }
            
    /**
     * Initialize the XML content class.
     * Load and parse the content of the given CmsFile object.
     * @param cms CmsObject Object for accessing resources.
     * @param file CmsFile object of the file to be loaded and parsed.
     * @exception CmsException
     */    
    public void init(CmsObject cms, CmsFile file) throws CmsException {
        String filename = file.getAbsolutePath();
        String currentProject = cms.getRequestContext().currentProject().getName();
        Document parsedContent = null;

        m_cms = cms;
        m_filename = filename;            

        parsedContent = loadCachedDocument(filename);
        if(parsedContent == null) {            
            byte[] fileContent = file.getContents();
            if(fileContent == null || "".equals(fileContent)) {
                // The file content is empty. Possibly the file object is only
                // a file header. Re-read the file object and try again
                file = cms.readFile(filename);
                fileContent = file.getContents();
            }
            if(fileContent == null || "".equals(fileContent)) {
                // The file content is still emtpy.
                // Start with an empty XML document.
                try {
                    parsedContent = getXmlParser().createEmptyDocument(getXmlDocumentTagName());
                } catch(Exception e) {
                    throwException("Could not initialize now XML document " + filename + ". " + e, CmsException.C_XML_PARSING_ERROR );
                }
            } else {
                parsedContent = parse(new String(file.getContents()));                
            }
            m_filecache.put(currentProject + ":" + filename, parsedContent.cloneNode(true));
        }    
        init(cms, parsedContent, filename);
    }                               

    /**
     * Initialize the class with the given parsed XML DOM document.
     * @param cms CmsObject Object for accessing system resources.
     * @param document DOM document object containing the parsed XML file.
     * @param filename OpenCms filename of the XML file.
     * @exception CmsException
     */
    public void init(CmsObject cms, Document content, String filename) throws CmsException {
        m_cms = cms;
        m_content = content;
        m_filename = filename;
        
        // First check the document tag. Is this the right document type?
        Element docRootElement = m_content.getDocumentElement();
        String docRootElementName = docRootElement.getNodeName().toLowerCase();
        if(! docRootElementName.equals(getXmlDocumentTagName().toLowerCase())) {
            // Hey! This is a wrong XML document!
            // We will throw an execption and the document away :-)
            removeFromFileCache();
            m_content = null;
            String errorMessage = "XML document " + getAbsoluteFilename() + " is not of the expected type. This document is \"" 
                    + docRootElementName + "\", but it should be \"" + getXmlDocumentTagName() + "\" (" + getContentDescription() + ").";
            throwException(errorMessage, CmsException.C_XML_WRONG_CONTENT_TYPE);
        }     

        // OK. Document tag is fine. Now get the DATA tags and collect them
        // in a Hashtable (still in DOM representation!)        
        try {
            processNode(m_content, m_firstRunTags,
                    A_CmsXmlContent.class.getDeclaredMethod("handleDataTag", C_PARAMTYPES_HANDLING_METHODS),
                    null, null);
        } catch(CmsException e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "Error while scanning for DATA and INCLUDE tags in file " + getAbsoluteFilename() + ".");
            }
            throw e;
        } catch(NoSuchMethodException e2) {
            String errorMessage = "XML tag process method \"handleDataTag\" could not be found";
            throwException(errorMessage, CmsException.C_XML_NO_PROCESS_METHOD);            
        }
    }
    
    /**
     * Create a new CmsFile object containing an empty XML file of the
     * current content type.
     * The String returned by <code>getXmlDocumentTagName()</code>
     * will be used to build the XML document element.
     * @param cms Current cms object used for accessing system resources.
     * @param filename Name of the file to be created.
     * @param documentType Document type of the new file.
     * @exception CmsException if no absolute filename is given or write access failed.
     */
    public void createNewFile(CmsObject cms, String filename, String documentType) throws CmsException {
        if(! filename.startsWith("/")) {
            // this is no absolute filename. 
            this.throwException("Cannot create new file. Bad name.", CmsException.C_BAD_NAME);
        }
        
        int slashIndex = filename.lastIndexOf("/") + 1;
        String folder = filename.substring(0, slashIndex);
        String file = filename.substring(slashIndex);
        cms.createFile(folder, file, "".getBytes(), documentType);
        cms.lockResource(filename);
        m_cms = cms;
        m_filename = filename;
        try {
            m_content =parser.createEmptyDocument(getXmlDocumentTagName());
        } catch(Exception e) {
            e.printStackTrace();
            throwException("Cannot create empty XML document for file " + m_filename + ". ", CmsException.C_XML_PARSING_ERROR);
        }
        
        write();
    }        
    
    /**
     * Used by the init method to search a template file if only a filename 
     * is given instead of a CmsFile Object. 
     * Previously cached documents will be considered.
     * <P>
     * Template files will be searched in the following hierachical order:
     * <UL>
     * <LI> (template path)/(full classname).TemplateName.(Extension) </LI>
     * <LI> (template path)/(parent class).TemplateName.(Extension) </LI>
     * <LI> ... </LI>
     * <LI> (template path)/TemplateName.(Extension) </LI>
     * </UL>
     * <P>
     * The starting classname is determined by the class of the given <code>requestingObject</class>
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param filename Template file name to be loaded.
     * @param requestingObject Object whose class hierarchy should be used for resolving file names.
     * @return absolute path of the filename.
     * @exception CmsException
     */
    public static String lookupAbsoluteFilename(CmsObject cms, String filename, Object requestingObject) throws CmsException {
        Class actualClass = requestingObject.getClass();
        CmsResource retValue = null;
		String completeFilename = null;

        // we use this Vector for storing all tried filenames.
        // so we can give detailled error messages if the 
        // template file was not found.
        Vector checkedFilenames = new Vector();
        
        if(filename.startsWith("/")) {
            completeFilename = filename;
        } else {
        // Now start the loop to search 
        while(retValue == null) {
		    completeFilename = C_TEMPLATEPATH + actualClass.getName() + "." + filename + C_TEMPLATE_EXTENSION;            
		    checkedFilenames.addElement(completeFilename);
            retValue = null;
            try {
                retValue = cms.readFileHeader(completeFilename);
            } catch(Exception e) {
                retValue=null;
            }
            //retValue = readTemplateFile(cms, completeFilename);
            actualClass = actualClass.getSuperclass();
            if(actualClass.getName().equals(C_MINIMUM_CLASSNAME)){ 
			    if(retValue == null) {
                    // last chance to get the filename
                    completeFilename = C_TEMPLATEPATH + filename + C_TEMPLATE_EXTENSION;
                    checkedFilenames.addElement(completeFilename);
                    try {
                        retValue = cms.readFileHeader(completeFilename);
                    } catch(Exception e) {
                        retValue=null;
                    }
                    break;
                }
            }
        }
        if(retValue == null) {
            Enumeration checkedEnum = checkedFilenames.elements();
            if(A_OpenCms.isLogging()) {
                while(checkedEnum.hasMoreElements()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, "[A_CmsXmlContent] checked: " + (String)checkedEnum.nextElement());
                }
            }
            throw new CmsException("Cannot find template file for request \"" + filename + "\". ", CmsException.C_NOT_FOUND);
        }
        }
        return completeFilename;
    }
    
    /**
     * Parses the given file and stores it in the internal list of included files and
     * appends the relevant data structures of the new file to its own structures.
     * 
     * @param include Filename of the XML file to be included
     * @exception CmsException
     */    
    public A_CmsXmlContent readIncludeFile(String filename) throws CmsException {

        A_CmsXmlContent include = null;
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "Including File: " + filename);       
        }

        try {
            include = (A_CmsXmlContent)getClass().newInstance();
            include.init(m_cms, filename);
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "while include file: " + e);       
            }
        }

        readIncludeFile(include);
        return include;
    }

    /**
     * Read the datablocks of the given content file and include them
     * into the own Hashtable of datablocks.
     * 
     * @param include completely initialized A_CmsXmlObject to be included
     * @exception CmsExeption
     */    
    public void readIncludeFile(A_CmsXmlContent include) throws CmsException {
        m_includedTemplates.addElement(include);
        m_blocks = concatData(m_blocks, include.getAllData());
    }

	/**
	 * Prints the XML parsed content of this template file
	 * to the given Writer.
	 * 
	 * @param out Writer to print to.
	 */   
    public void getXmlText(Writer out) {
        parser.getXmlText(m_content, out);
    }

  	/**
	 * Prints the XML parsed content of the given Node and
	 * its subnodes to the given Writer.
	 * 
	 * @param out Writer to print to.
	 * @param n Node that should be printed.
	 */   
    public void getXmlText(Writer out, Node n) {
        Document tempDoc = (Document)m_content.cloneNode(false);
        tempDoc.appendChild(parser.importNode(tempDoc, n));
        parser.getXmlText(tempDoc, out);
    }
    
    /**
     * Prints the XML parsed content to a String
     * @return String with XML content
     */
    public String getXmlText() {
        StringWriter writer = new StringWriter();
        getXmlText(writer);
        return writer.toString();
    }
    
    /**
     * Prints the XML parsed content of a given node and 
     * its subnodes to a String
	 * @param n Node that should be printed.
     * @return String with XML content
     */
    public String getXmlText(Node n) {
        StringWriter writer = new StringWriter();
        getXmlText(writer, n);
        return writer.toString();
    }
    
    /**
     * Gets the absolute filename of the XML file represented by this content class
     * @return Absolute filename
     */
    public String getAbsoluteFilename() {
        return m_filename;
    }

    /**
     * Gets a short filename (without path) of the XML file represented by this content class
     * of the template file.
     * @return filename
     */    
    public String getFilename() {
        return m_filename.substring(m_filename.lastIndexOf("/")+1);
    }            

    /**
     * Writes the XML document back to the OpenCms system. 
     * @exception CmsException  
     */
    public void write() throws CmsException {
        // Get the XML content as String
        StringWriter writer = new StringWriter();
        getXmlText(writer);
        byte[] xmlContent = writer.toString().getBytes();
        
        // Get the CmsFile object to write to
        String filename = getAbsoluteFilename();                
        CmsFile file = m_cms.readFile(filename);
        
        // Set the new content and write the file
        file.setContents(xmlContent);
        m_cms.writeFile(file);        
        
        // update the internal parsed content cache with the new file data.
        String currentProject = m_cms.getRequestContext().currentProject().getName();
        m_filecache.put(currentProject + ":" + filename, m_content.cloneNode(true));        
    }

    /**
     * Deletes this object from the internal XML file cache
     */
    public void removeFromFileCache() {
        String currentProject = m_cms.getRequestContext().currentProject().getName();
        m_filecache.remove(currentProject + ":" + getAbsoluteFilename());
    }
    
    /**
     * Deletes all files from the file cache.
     */
    public static void clearFileCache() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CACHE, "[A_CmsXmlContent] clearing XML file cache.");
        }
        m_filecache.clear();        
    }

    /**
     * Deletes the file with the given key from the file cache.
     * If no such file exists nothing happens.
     * @param key Key of the template file to be removed from the cache.
     */
    public static void clearFileCache(String key) {
        m_filecache.remove(key);
    }

    /**
     * Deletes the file represented by the given A_CmsXmlContent from
     * the file cache.
     * @param doc A_CmsXmlContent representing the XML file to be deleted.
     */
    public static void clearFileCache(A_CmsXmlContent doc) {
        if(doc != null) {
            String currentProject = doc.m_cms.getRequestContext().currentUser().getName();
            m_filecache.remove(currentProject + ":" + doc.getAbsoluteFilename());
        }
    }

    /**
     * Gets the currently used XML Parser.
     * @return currently used parser.
     */
    public static I_CmsXmlParser getXmlParser() {
          return parser;
    }

    /**
     * This method should be implemented by every extending class.
     * It returns the name of the XML document tag to scan for.
     * @return name of the XML document tag.
     */
    abstract public String getXmlDocumentTagName();

    /**
     * This method should be implemented by every extending class.
     * It returns a short description of the content definition type
     * (e.g. "OpenCms news article").
     * @return content description.
     */
    abstract public String getContentDescription();                        

    /**
     * Creates a clone of this object.
     * @return cloned object.
     * @exception CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        try {
            A_CmsXmlContent newDoc = (A_CmsXmlContent)getClass().newInstance();
            newDoc.init(m_cms, (Document)m_content.cloneNode(true), m_filename);               
            return newDoc;    
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Error while trying to clone object.");
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + e);              
            }               
            throw new CloneNotSupportedException(e.toString());           
        }       
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
        Hashtable selectedRun = null;
        switch(runSelector) {
        case C_REGISTER_FIRST_RUN:
            selectedRun = m_firstRunTags;
            break;
        case C_REGISTER_MAIN_RUN:
            selectedRun = m_mainProcessTags;
            break;
        }

        try {
            selectedRun.put(tagname.toLowerCase(), c.getDeclaredMethod(methodName, C_PARAMTYPES_HANDLING_METHODS));
        } catch(Exception e) {
            System.err.println(e);
        }        
        registerTag(tagname);
    }

    /**
     * Registers the given tag to be "known" by the system. 
     * So this tag will not be handled by the default method of processNode.
     * Under normal circumstances this feature will only be used for 
     * the XML document tag.
     * @param tagname Tag name to register.
     */
    public void registerTag(String tagname) {
        if(!(m_knownTags.contains(tagname.toLowerCase()))) {
            m_knownTags.addElement(tagname.toLowerCase());
        }
    }

	/**
	 * Gets a string representation of this object.
	 * @return String representation of this object.
	 */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[XML file]: ");
        output.append(getFilename());
        output.append(", content type: ");
        output.append(getContentDescription());
        return output.toString();
    }
    
    /**
     * Main processing funtion for the whole XML document.
     * 
     * @see #processNode
     * @param keys Hashtable with XML tags to look for and corresponding methods.
     * @param defaultMethod Method to be called if the tag is unknown.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @exception CmsException
     */
    protected void processDocument(Hashtable keys, Method defaultMethod, Object callingObject, Object userObj) 
            throws CmsException {
        processNode(m_content.getDocumentElement(), keys, defaultMethod, callingObject, userObj);
    }

    /**
     * Universal main processing function for parsed XML templates.
     * The given node is processed by a tree walk. 
     * <P>
     * Every XML tag will be looked up in the Hashtable "keys".
     * If a corresponding entry is found, the tag will be handled
     * by the corresponding function returned from the Hashtable.
     * <P>
     * If an unknown tag is detected the method defaultMethod is called 
     * instead. Is defaultMethod == null nothing will be done with unknown tags.
     * <P>
     * The invoked handling methods are allowed to return null or objects 
     * of the type String, Node, Integer or byte[].
     * If the return value is null, nothing happens. In all other cases
     * the handled node in the tree will be replaced by a new node.
     * The value of this new node depends on the type of the returned value.
     *
     * @param n Node with its subnodes to process
     * @param keys Hashtable with XML tags to look for and corresponding methods.
     * @param defaultMethod Method to be called if the tag is unknown.
     * @param callingObject Reference to the Object that requested the node processing.
     * @param userObj Customizable user object that will be passed to handling and user methods.
     * @exception CmsException
     */    
    protected void processNode(Node n, Hashtable keys, Method defaultMethod,
                                   Object callingObject, Object userObj) throws CmsException {
        
        // Node currently processed
        Node child = null;
        
        // Name of the currently processed child
        String childName = null;

        // Node nextchild needed for the walk through the tree
        Node nextchild = null;
        
        // List of new Nodes the current node should be replaced with
        NodeList newnodes = null;
    
        // single new Node from newnodes
        Node insert = null;
                
        // tag processing method to be called for the current Node
        Method callMethod = null;
        
        // Object returned by the tag processing methods
        Object methodResult = null;
        
        // only start if there is something to process
        if(n != null && n.hasChildNodes()) {            
            child = n.getFirstChild();    
            
            while(child != null) {
                childName = child.getNodeName().toLowerCase();
                // Get the next node in the tree first                
                nextchild = treeWalker(child);
    
                // Only look for element nodes
                // all other nodes are not very interesting
                if(child.getNodeType()==Node.ELEMENT_NODE) {
                    newnodes = null;
                    callMethod = null;
                    if(keys.containsKey(childName)) {
                        // name of this element found in keys Hashtable
                        callMethod = (Method)keys.get(childName);
                    } else if (!m_knownTags.contains(childName)){
                        // name was not found
                        // and even name is not known as tag
                        callMethod = defaultMethod;
                    }
                    if(callMethod != null) {
                        methodResult = null;
                        try {
                            if(C_DEBUG && A_OpenCms.isLogging()) {
                                A_OpenCms.log(C_OPENCMS_DEBUG, "<" + childName + "> tag found. Value: " + child.getNodeValue());
                                A_OpenCms.log(C_OPENCMS_DEBUG, "Tag will be handled by method [" + callMethod.getName() + "]. Invoking method NOW.");
                            }
                            // now invoke the tag processing method.
                            methodResult = callMethod.invoke(this, new Object[] {child, callingObject, userObj});
                        } catch (Exception e) {
                            if(e instanceof InvocationTargetException) {
                                Throwable thrown = ((InvocationTargetException)e).getTargetException();
                                // if the method has thrown a cms exception then
                                // throw it again
                                if(thrown instanceof CmsException) {
                                    throw (CmsException)thrown;
                                } else { 
                                    thrown.printStackTrace();
                                    throwException("processNode received an exception while handling XML tag \"" 
                                            + childName + "\" by \"" + callMethod.getName() + "\" for file " 
                                            + getFilename() + ": " + e, CmsException.C_XML_PROCESS_ERROR);
                                }
                            } else {
                                throwException("processNode could not invoke the XML tag handling method " 
                                        + callMethod.getName() + "\" for file " + getFilename() + ": " 
                                        + e, CmsException.C_XML_PROCESS_ERROR);
                            }
                        }
    
                        // Inspect the type of the method return value
                        // Currently NodeList, String and Integer are
                        // recognized. All other types will be ignored.
                        if(methodResult == null) {
                            newnodes = null;
                        } else if(methodResult instanceof NodeList) {
                            newnodes = (NodeList)methodResult;
                        } else if(methodResult instanceof String) {
                            newnodes = stringToNodeList((String)methodResult);
                        } else if(methodResult instanceof Integer) {
                            newnodes = stringToNodeList(((Integer)methodResult).toString());
                        } else if(methodResult instanceof byte[]) {
                            newnodes = stringToNodeList(new String((byte[])methodResult));
                        } else {
                            // Type not recognized.
                            if(A_OpenCms.isLogging()) {
                                A_OpenCms.log(C_OPENCMS_CRITICAL, "Return type of method " + callMethod.getName()
                                           + " not recognized. Cannot insert value.");
                            }
                            newnodes = null;                                            
                        }
                        
                        // the list of nodes to be inserted could be printed out here. 
                        // uncomment the following to activate this feature.
                        // printNodeList(newnodes);
                        
                        if(newnodes != null) {
                            // the called method returned a valid result.
                            // we have do remove the old element from the tree
                            // and replace it by the new nodes.
                            // WARNING! Do not remove any subchilds from the old
                            // element. There could be links to the subchilds
                            // in our Hashtables (e.g. for datablocks).
                            // Only remove the child itself from the tree!
    
                            int numNewChilds = newnodes.getLength();
                            if(numNewChilds > 0) {
                                // there are new childs.
                                // so we can replace the old element                                                        
                                for(int j=0; j<numNewChilds; j++) {
                                    //insert = parser.importNode(m_content, newnodes.item(j));
                                    insert = parser.importNode(child.getOwnerDocument(), newnodes.item(j));
                                    if(j==0) {
                                        nextchild = insert;             
                                    }
                                    //A_OpenCms.log(c_OPENCMS_DEBUG, "trying to add node " + newnodes.item(j));
                                    child.getParentNode().insertBefore(insert, child);
                                    //A_OpenCms.log(c_OPENCMS_DEBUG, "Node " + newnodes.item(j) + " added.");
                                }
                            } else {
                                // the list of the new childs is empty.
                                // so we have to re-calculate the next node 
                                // in the tree since the old nextchild will be deleted                                
                                // been deleted.
                                nextchild = treeWalkerWidth(child);                                
                            }
                            // now delete the old child and get the next one.
                            child.getParentNode().removeChild(child);                              
                        }
                    }
                } 
                child = nextchild;
            }
        }
    }

    /**
     * Gets the XML parsed content of this template file as a DOM document.
     * <P>
     * <em>WARNING: The returned value is the original DOM document, not a clone.
     * Any changes will take effect to the behaviour of this class.
     * Especially datablocks are concerned by this!</em>
     * 
     * @return the content of this template file.
     */
    protected Document getXmlDocument() {
        return m_content;
    }

    /**
     * Checks if this Template owns a datablock with the given key.
     * @param key Datablock key to be checked.
     * @return true if a datablock is found, false otherwise.
     */
    protected boolean hasData(String key) {
        return m_blocks.containsKey(key.toLowerCase());
    }    
                    
	/**
	 * Gets a complete datablock from the datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Complete DOM element of the datablock for the given key 
	 * or null if no datablock is found for this key.
	 */
	protected Element getData(String tag) throws CmsException {
        Object result = m_blocks.get(tag.toLowerCase());
        if(result == null) {
            String errorMessage = "Unknown Datablock " + tag + " requested.";
            throwException(errorMessage, CmsException.C_XML_UNKNOWN_DATA);
        } else if(!(result instanceof Element)) {
            String errorMessage = "Unexpected object returned as datablock. Requested Tagname: " 
                    + tag + ". Returned object: " + result.getClass().getName() + ".";
            throwException(errorMessage, CmsException.C_XML_CORRUPT_INTERNAL_STRUCTURE);
        }
	    return (Element)m_blocks.get(tag.toLowerCase());
    }

	/**
	 * Gets the text and CDATA content of a datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Datablock content for the given key or null if no datablock
	 * is found for this key.
	 */
    protected String getDataValue(String tag) throws CmsException {
        Element dataElement = getData(tag);
        return getTagValue(dataElement);
    }    

    
  	/**
	 * Gets a processed datablock from the datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	protected Element getProcessedData(String tag) throws CmsException {
        return getProcessedData(tag, null, null);
    }
   
  	/**
	 * Gets a processed datablock from the datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
    protected Element getProcessedData(String tag, Object callingObject) throws CmsException {
        return getProcessedData(tag, callingObject, null);
    }
    
  	/**
	 * Gets a processed datablock from the datablock hashtable.
	 * <P>
	 * The userObj Object is passed to all called user methods.
	 * By using this, the initiating class can pass customized data to its methods.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @param userObj any object that should be passed to user methods
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	protected Element getProcessedData(String tag, Object callingObject, Object userObj) 
            throws CmsException {
        Element dBlock = (Element)getData(tag).cloneNode(true);
        processNode(dBlock, m_mainProcessTags, null, callingObject, userObj);
        return dBlock;
	}

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	protected String getProcessedDataValue(String tag) throws CmsException {
        Element data = getProcessedData(tag);
        return getTagValue(data);
    }

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
    protected String getProcessedDataValue(String tag, Object callingObject) throws CmsException {
        Element data = getProcessedData(tag, callingObject);
        return getTagValue(data);
    }

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * <P>
	 * The userObj Object is passed to all called user methods.
	 * By using this, the initiating class can pass customized data to its methods.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @param userObj any object that should be passed to user methods
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	protected String getProcessedDataValue(String tag, Object callingObject, Object userObj) 
            throws CmsException {
        Element data = getProcessedData(tag, callingObject, userObj);
        return getTagValue(data);
	}

     
    /**
	 * Gets all datablocks (the datablock hashtable).
	 * @return Hashtable with all datablocks.
	 */
	protected Hashtable getAllData() {
		return m_blocks;
	}
  
   /**
    * Fast method to replace a datablock.
    * <P>
    * <b>USE WITH CARE!</b>
    * <P>
    * Using this method only if
    * <ul>
    * <li>The tag name is given in lowercase</li>
    * <li>The datablock already exists (it may be empty)</li>
    * <li>Neither tag nor data are <code>null</code></li>
    * <li>You are sure, there will occure no errors</li>
    * </ul>
    * 
    * @param tag Key for this datablock.
    * @param data String to be put in the datablock.
    */
    protected void fastSetData(String tag, String data) {   
		// fastSetData could have been called with an upper case argument
		tag=tag.toLowerCase();
		Element originalBlock = (Element)(m_blocks.get(tag)); 
		while(originalBlock.hasChildNodes()) { 
			originalBlock.removeChild(originalBlock.getFirstChild());
		} 
		originalBlock.appendChild(m_content.createTextNode(data));  
    }
    
   /**
    * Creates a datablock consisting of a single TextNode containing 
    * data and stores this block into the datablock-hashtable.
    * 
    * @param tag Key for this datablock.
    * @param data String to be put in the datablock.
    */
    protected void setData(String tag, String data) { 
        // create new XML Element to store the data
        //Element newElement = m_content.createElement("DATA");
        String attribute = tag;
        int dotIndex = tag.lastIndexOf(".");
        if(dotIndex != -1) {
            attribute = attribute.substring(dotIndex + 1);
        }                    
        //newElement.setAttribute("name", attribute);
        Element newElement = m_content.createElement(attribute);
        
        if(data == null || "".equals(data)) {
            // empty string or null are given.
            // put an empty datablock without any text nodes.
            setData(tag, newElement);    
        } else {
            // Fine. String is not empty.
            // So we can add a new text node containig the string data.
            // Leading spaces are removed before creating the text node.
            newElement.appendChild(m_content.createTextNode(data.trim()));
            setData(tag, newElement);    
        }
    }

    /**
     * Stores a given datablock element in the datablock hashtable.
     * 
     * @param tag Key for this datablock.
     * @param data DOM element node for this datablock.
     */
    protected void setData(String tag, Element data) {        
        // If we got a null data, give this request to setData(Strig, String)
        // to create a new text node.
        if(data==null) {
            setData(tag, "");
        } else {        
            // Now we can be sure to have a correct Element
            tag = tag.toLowerCase();
           
            Element newElement = (Element)data.cloneNode(true);        
    
            if(C_DEBUG && A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "Putting datablock " + tag + " into internal Hashtable.");
            }
                   
            if (! (m_blocks.containsKey(tag))) {
                // This is a brand new datablock. It can be inserted directly.
                //m_blocks.put(tag, newElement);
                insertNewDatablock(tag, newElement);
            } else {        
                // datablock existed before, so the childs of the old 
                // one can be replaced.
                if(C_DEBUG && A_OpenCms.isLogging()) {
                   A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "Datablock existed before. Replacing.");               
                }
           
                // Look up the old datablock and remove all its childs.
                Element originalBlock = (Element)(m_blocks.get(tag));
                while(originalBlock.hasChildNodes()) {
                    originalBlock.removeChild(originalBlock.getFirstChild());
                }
               
                // And now add all childs of the new node
                NodeList newNodes = data.getChildNodes();
                int len = newNodes.getLength();
                for(int i=0; i<len; i++) {
                    Node newElement2 = (Node)newNodes.item(i).cloneNode(true);
                    originalBlock.appendChild(parser.importNode(originalBlock.getOwnerDocument(), newElement2));
                }            
            }
        } 
    }
        
    /**
     * Remove a datablock from the internal hashtable and
     * from the XML document
     * @param tag Key of the datablock to delete.
     */    
    protected void removeData(String tag) {
        Element e = (Element)m_blocks.get(tag.toLowerCase());
        if(e != null) {
            m_blocks.remove(tag.toLowerCase());
            Element parent = (Element)e.getParentNode();
            if(parent != null) {
                parent.removeChild(e);
            }
        }
    }
    
    /**
     * Calls a user method in the object callingObject.
     * Every user method has to user the parameter types defined in 
     * C_PARAMTYPES_USER_METHODS to be recognized by this method.
     * 
     * @see #C_PARAMTYPES_USER_METHODS
     * @param methodName Name of the method to be called.
     * @param parameter Additional parameter passed to the method.
     * @param callingObject Reference to the object containing the called method.
     * @param userObj Customizable user object that will be passed through to the user method.
     * @exception CmsException
     */    
    protected Object callUserMethod(String methodName, String parameter, Object callingObject, Object userObj) 
            throws CmsException {
        Object[] params = new Object[] {m_cms, parameter, this, userObj};
        Object result = null;
        
        // Check if the user selected a object where to look for the user method.
        if(callingObject == null) {
            throwException("You are trying to call the user method \"" + methodName + "\" without giving an object containing this method. "
                    + "Please select a callingObject in your getProcessedData or getProcessedDataValue call.", CmsException.C_XML_NO_USER_METHOD);
        }
        
        // OK. We have a calling object. Now try to invoke the method
        try {
            // try to invoke the method 'methodName'
            result = getUserMethod(methodName, callingObject).invoke(callingObject, params);
        } catch(NoSuchMethodException exc) {
           throwException("User method " + methodName + " was not found in class "
                         + callingObject.getClass().getName() + ".", CmsException.C_XML_NO_USER_METHOD);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                throwException("User method " + methodName + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("User method " + methodName + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
        if((result != null) && (! (result instanceof String || result instanceof Integer 
                || result instanceof NodeList || result instanceof byte[]))) {
            throwException("User method " + methodName + " in class " + callingObject.getClass().getName() 
                    + " returned an unsupported Object: " + result.getClass().getName(), CmsException.C_XML_PROCESS_ERROR);
        }
        return(result);
    }
    
    /**
     * Reads all text or CDATA values from the given XML element, 
     * e.g. <code>&lt;ELEMENT&gt;foo blah &lt;![CDATA[&lt;H1&gt;Hello&lt;/H1&gt;]]&gt;&lt;/ELEMENT&gt;</code>.
     * 
     * @param n Element that should be read out.
     * @return Concatenated string of all text and CDATA nodes or <code>null</code> 
     * if no nodes were found.
     */
    protected String getTagValue(Element n) {
        StringBuffer result = new StringBuffer();
        if(n != null) {
            NodeList childNodes = n.getChildNodes();
            Node child = null;
            int numchilds = childNodes.getLength();
    
            if(childNodes != null) {
                for(int i=0; i<numchilds; i++) {
                    child = childNodes.item(i);
                    if(child.getNodeType() == n.TEXT_NODE || child.getNodeType() == n.CDATA_SECTION_NODE) {
                        result.append(child.getNodeValue());
                    }
                }
            }
        }
        return result.toString();
    }    

    /**
     * Help method to print nice classnames in error messages
     * @return class name in [ClassName] format
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }

    /**
     * Help method to walk through the DOM document tree.
     * First it will be looked for children of the given node.
     * If there are no children, the siblings and the siblings of our parents
     * are examined. This will be done by calling treeWalkerWidth.
     * @param n Node representing the actual position in the tree
     * @return next node
     */
    protected Node treeWalker(Node n) {
        Node nextnode = null;
        
        if(n.hasChildNodes()) {
            // child has child notes itself
            // process these first in the next loop
            nextnode = n.getFirstChild();
        } else {
            // child has no subchild.
            // so we take the next sibling
            nextnode = treeWalkerWidth(n);
        }    
        return nextnode;
    }        

    /**
     * Help method to walk through the DOM document tree by a
     * width-first-order.
     * @param n Node representing the actual position in the tree
     * @return next node
     */
    protected Node treeWalkerWidth(Node n) {
        Node nextnode = null;
        Node parent = null;
        
            nextnode = n.getNextSibling();
            parent = n.getParentNode();
            while(nextnode == null && parent != null) {
                // child has sibling
                // last chance: we take our parent's sibling
                // (or our grandparent's sibling...)
                nextnode = parent.getNextSibling();
                parent = parent.getParentNode();
            }
        return nextnode;
    }        
    
    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @exception CmsException
     */
    protected void throwException(String errorMessage) throws CmsException {
        throwException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the given type.
     * @param errorMessage String with the error message to be printed.
     * @param type Type of the exception to be thrown.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, int type) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
        }        
        throw new CmsException(errorMessage, type);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @param e Original exception.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, Exception e) throws CmsException {
        throwException(errorMessage, e, CmsException.C_UNKNOWN_EXCEPTION);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @param e Original exception.
     * @param type Type of the exception to be thrown.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, Exception e, int type) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
        }        
        if(e instanceof CmsException) {
            throw (CmsException)e;
        } else {
            throw new CmsException(errorMessage, type, e);
        }
    }
    
    /**
	 * Starts the XML parser with the content of the given CmsFile object.
	 * After parsing the document it is scanned for INCLUDE and DATA tags
	 * by calling processNode with m_firstRunParameters.
	 * 
	 * @param content String to be parsed
     * @return Parsed DOM document.
     * @see #processNode
     * @see #firstRunParameters
	 */
    protected Document parse(String content) throws CmsException {
        Document parsedDoc = null;
        StringReader reader = new StringReader(content);
        
        // First parse the String for XML Tags and
        // get a DOM representation of the document
        try {
            parsedDoc = parser.parse(reader);
        } catch(Exception e) {
            // Error while parsing the document.
            // there ist nothing to do, we cannot go on.
            // throws exception.
            String errorMessage = "Cannot parse XML file \"" + getAbsoluteFilename() + "\". " + e;
            throwException(errorMessage, CmsException.C_XML_PARSING_ERROR);
        }
        if(parsedDoc == null) {
            String errorMessage = "Unknown error. Parsed DOM document is null.";
            throwException(errorMessage, CmsException.C_XML_PARSING_ERROR);
        }        

        parsedDoc.getDocumentElement().normalize();
        
        // Delete all unnecessary text nodes from the tree.
        // These nodes could cause errors when serializing this document otherwise
        Node loop = parsedDoc.getDocumentElement();
        while(loop != null) {
            Node next = treeWalker(loop);
            if(loop.getNodeType() == loop.TEXT_NODE) {
                Node leftSibling = loop.getPreviousSibling();
                Node rightSibling = loop.getNextSibling();
                if(leftSibling == null || rightSibling == null ||
                        (leftSibling.getNodeType() == loop.ELEMENT_NODE && rightSibling.getNodeType() == loop.ELEMENT_NODE)) {
                    if("".equals(loop.getNodeValue().trim())) {
                        loop.getParentNode().removeChild(loop);
                    }              
                }
            }
            loop = next;
        }
                
        return parsedDoc;
    }

    /**
     * Internal method registering all special tags relevant for the basic functionality of
     * this abstract class.
     * <P>
     * OpenCms special tags are:
     * <UL>
     * <LI><CODE>INCLUDE: </CODE> used to include other XML files</LI>
     * <LI><CODE>DATA: </CODE> used to define a datablock that can be handled
     * by getData or processed by getProcessedData or <code>PROCESS</CODE></LI>
     * <LI><CODE>PROCESS: </CODE> used to insert earlier or external defined datablocks</LI>
     * <LI><CODE>METHOD: </CODE> used to call customized methods in the initiating user object</LI>
     * </UL>
     * All unknown tags will be treated as a shortcut for <code>&lt;DATA name="..."&gt;</code>.
     */

    private void registerAllTags() {
        // register tags for scanning "INCLUDE" and "DATA"
        registerTag("INCLUDE", A_CmsXmlContent.class, "handleIncludeTag", C_REGISTER_FIRST_RUN);
        registerTag("DATA", A_CmsXmlContent.class, "handleDataTag", C_REGISTER_FIRST_RUN);
    
        // register tags for preparing HTML output
        registerTag("METHOD", A_CmsXmlContent.class, "handleMethodTag", C_REGISTER_MAIN_RUN);
        registerTag("PROCESS", A_CmsXmlContent.class, "handleProcessTag", C_REGISTER_MAIN_RUN);
        registerTag("INCLUDE", A_CmsXmlContent.class, "replaceTagByComment", C_REGISTER_MAIN_RUN);
        registerTag("DATA", A_CmsXmlContent.class, "replaceTagByComment", C_REGISTER_MAIN_RUN);   
        
        registerTag(getXmlDocumentTagName());
    }
    
    /**
     * Handling of "INCLUDE" tags.
     * @param n XML element containing the <code>&lt;INCLUDE&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     */

    private Object handleIncludeTag(Element n, Object callingObject, Object userObj) throws CmsException {
        A_CmsXmlContent include = null;
        
        String tagcontent = getTagValue(n);
        include = readIncludeFile(tagcontent);
        return include.getXmlDocument().getDocumentElement().getChildNodes();
    }

    /**
     * Handling of "DATA" tags and unknown tags.
     * A reference to each data tag ist stored in an internal hashtable with
     * the name of the datablock as key.
     * Nested datablocks are stored with names like outername.innername
     * 
     * @param n XML element containing the <code>&lt;DATA&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     */
    private void handleDataTag(Element n, Object callingObject, Object userObj) { 
        String blockname;
        String bestFit = null;
        String parentname = null;
        Node parent = n.getParentNode();
    
        while(parent != null && parent.getNodeType() == Node.ELEMENT_NODE ) {
            // check if this datablock is part of a datablock
            // hierarchy like 'language.de.btn_yes'
            // look for the best fitting hierarchy name part, too
            if(parent.getNodeName().equals("DATA")) {           
                blockname = ((Element)parent).getAttribute("name");
            } else {
                blockname = parent.getNodeName();
                String secondName = ((Element)parent).getAttribute("name");
                if(!"".equals(secondName)) {
                    blockname = blockname + "." + secondName;
                }
            }
            
            blockname = blockname.toLowerCase();
            
            if(parentname == null) {
                parentname = blockname;
            } else {
                parentname = blockname + "." + parentname;
            }
            if(m_blocks.containsKey(parentname)) {
                bestFit = parentname;
            }
            parent = parent.getParentNode();
        }
    
        // bestFit now contains the best fitting name part
        // next, look for the tag name (the part behind the last ".")
        if(n.getNodeName().equals("DATA")) {
            blockname = n.getAttribute("name");
        } else {
            blockname = n.getNodeName();
            String secondName = n.getAttribute("name");
            if(!"".equals(secondName)) {
                blockname = blockname + "." + secondName;
            }
        }
        
        blockname = blockname.toLowerCase();
    
        // now we can build the complete datablock name
        if(bestFit != null) {            
            blockname = bestFit + "." + blockname;
        }
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "Reading datablock " + blockname);
        }
        
        // finally we cat put the new datablock into the hashtable
        m_blocks.put(blockname, n);
        //return null;
    }
    
    /**
     * Handling of the "PROCESS" tags.
     * Looks up the requested datablocks in the internal hashtable and
     * returns its subnodes.
     * 
     * @param n XML element containing the <code>&lt;PROCESS&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     */
    private Object handleProcessTag(Element n, Object callingObject, Object userObj) { 
        String blockname = getTagValue(n).toLowerCase();
        Element datablock = null;
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "handleProcessTag() started. Request for datablock \"" + blockname + "\".");
        }
    
        datablock = (Element)((Element)m_blocks.get(blockname));
        if(datablock == null) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Requested datablock  \"" + blockname + "\" not found!");
            }
            return C_ERR_NODATABLOCK + blockname;
        } else {
            return datablock.getChildNodes();
         }
    }

    /**
     * Handling of the "METHOD name=..." tags.
     * Name attribute and value of the element are read and the user method
     * 'name' is invoked with the element value as parameter.
     *
     * @param n XML element containing the <code>&lt;METHOD&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @return Object returned by the user method 
     * @exception CmsException
     */
    private Object handleMethodTag(Element n, Object callingObject, Object userObj) throws CmsException {         
        processNode(n, m_mainProcessTags, null, callingObject, userObj);
        String tagcontent = getTagValue(n);
        String method = n.getAttribute("name");
        
        Object result = null;
        try {
            result = callUserMethod(method, tagcontent, callingObject, userObj);         
        } catch(Throwable e1) {
            if(e1 instanceof CmsException) {
                throw (CmsException)e1;
            } else {
                throwException("handleMethodTag() received an exception from callUserMethod() while calling \"" 
                        + method + "\" requested by class " + callingObject.getClass().getName() + ": " + e1);
            }
        }      
        return result;
    }

    /**
     * Generates a XML comment.
     * It's used to replace no longer needed DOM elements by a short XML comment
     * 
     * @param n XML element containing the tag to be replaced <em>(unused)</em>.
     * @param callingObject Reference to the object requesting the node processing <em>(unused)</em>.
     * @param userObj Customizable user object that will be passed through to handling and user methods <em>(unused)</em>.
     * @return the generated XML comment.
     */
    private NodeList replaceTagByComment(Element n, Object callingObject, Object userObj) {
        Element tempNode = (Element)n.cloneNode(false);
        while(tempNode.hasChildNodes()) {
            tempNode.removeChild(tempNode.getFirstChild());
        }   
        tempNode.appendChild(m_content.createComment("removed " + n.getNodeName()));
        return tempNode.getChildNodes();
    }

    /**
     * Internal method for creating a new datablock.
     * <P>
     * This method is called by setData() if a new, not existing
     * datablock must be created.
     * <P>
     * <B>Functionality:</B> If a non-hierarchical datablock is given,
     * it is inserted at the end of the DOM document.
     * If a hierarchical datablock is given, all possible parent
     * names are checked in a backward oriented order. If a
     * datablock with a name that equals a part of the hierarchy is
     * found, the new datablock will be created as a (sub)child
     * of this datablock. 
     * 
     * @param tag Key for this datablock.
     * @param data DOM element node for this datablock.
     */
    private void insertNewDatablock(String tag, Element data) {
        // First check, if this is an extended datablock
        // in <NAME1 name="name2>... format, that has to be inserted
        // as name1.name2
                             
        String nameAttr = data.getAttribute("name");
        String workTag = null;
        if((!data.getNodeName().toLowerCase().equals("data")) && 
                nameAttr != null && (!"".equals(nameAttr))) {
            // this is an extended datablock
            workTag = tag.substring(0, tag.lastIndexOf("."));
        } else {
            workTag = tag;
        }

        // Import the node for later inserting
        Element importedNode = (Element)parser.importNode(m_content, data);
        
        // Check, if this is a simple datablock without hierarchy.
        if(workTag.indexOf(".") == -1) {
            // Fine. We can insert the new Datablock at the of the document
            m_content.getDocumentElement().appendChild(importedNode);
            m_blocks.put(tag, importedNode);
        } else {
            // This is a hierachical datablock tag. We have to search for
            // an appropriate place to insert first.
            boolean found = false;
            String match = "." + workTag;
            int dotIndex = match.lastIndexOf(".");
            Vector newBlocks = new Vector();
            
            while((!found) && (dotIndex > 1)) {
                match = match.substring(0, dotIndex);
                if(hasData(match.substring(1))) {
                    found = true;
                } else {
                    dotIndex = match.lastIndexOf(".");
                    newBlocks.addElement(match.substring(dotIndex + 1));
                }
            }

            // newBlocks now contains a (backward oriented) list
            // of all datablocks that have to be created, before
            // the new datablock named "tag" can be inserted.
            String datablockPrefix = "";
            
            if(found) {
                datablockPrefix = match.substring(1) + ".";
            }            
            
            // number of new elements to be created
            int numNewBlocks = newBlocks.size();
            
            // used to create the required new elements
            Element newElem = null;

            // Contains the last existing Element in the hierarchy.
            Element lastElem = null;
            
            // now create the new elements backwards
            for(int i=numNewBlocks-1; i>=0; i--) {
                newElem = m_content.createElement("DATA");
                newElem.setAttribute("name", (String)newBlocks.elementAt(i));
                m_blocks.put(datablockPrefix + (String)newBlocks.elementAt(i), newElem);
                if(lastElem != null) {
                    lastElem.appendChild(newElem);
                } else {
                    lastElem = newElem;
                }
            }                
            
            // Now all required parent datablocks are created.
            // Finally the given datablock can be inserted.
            if(lastElem != null) {
                lastElem.appendChild(importedNode);
            } else {
                lastElem = importedNode;
            }
            m_blocks.put(datablockPrefix + tag, importedNode);
            
            // lastElem now contains the hierarchical tree of all DATA tags to be
            // inserted.
            // If we have found an existing part of the hierarchy, get
            // this part and append the tree. If no part was found, append the
            // tree at the end of the document.
            if(found) {
                Element parent = (Element)m_blocks.get(match.substring(1));
                parent.appendChild(lastElem);
            } else {
                m_content.getDocumentElement().appendChild(lastElem);
            }                
        }
    }    
    
    /**
     * Reloads a previously cached parsed content.
     * 
     * @param filename Absolute pathname of the file to look for.
     * @return DOM parsed document or null if the cached content was not found.
     */    
    private Document loadCachedDocument(String filename) { 
        Document cachedDoc = null;
        String currentProject = m_cms.getRequestContext().currentProject().getName();
        Document lookup = (Document)m_filecache.get(currentProject + ":" + filename);
        if(lookup != null) {
            try {
                cachedDoc = lookup.cloneNode(true).getOwnerDocument();
            } catch (Exception e) {
                lookup=null;
                cachedDoc = null;
            }
        }
        if(C_DEBUG && cachedDoc != null && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "Re-used previously parsed XML file " + getFilename() + ".");
        }        
        return cachedDoc;        
    }
           
    /**
     * Looks up a user defined method requested by a "METHOD" tag.
     * The method is searched in the Object callingObject.
     * @param methodName Name of the user method
     * @param callingObject Object that requested the processing of the XML document
     * @return user method
     * @exception NoSuchMethodException
     */    
   	private Method getUserMethod(String methodName, Object callingObject)
		throws NoSuchMethodException {
		if(methodName == null || "".equals(methodName)) {
			// no valid user method name
			throw(new NoSuchMethodException("method name is null or empty"));
		}		
        return callingObject.getClass().getMethod(methodName, C_PARAMTYPES_USER_METHODS);
	}

    /**
     * Utility method for converting a String to a NodeList containing
     * a single TextNode. 
     * @param s String to convert
     * @return NodeList containing a TextNode with s
     */
    private NodeList stringToNodeList(String s) {
        Element tempNode = m_content.createElement("TEMP");
        Text text = m_content.createTextNode(s);
        tempNode.appendChild(text);
        return tempNode.getChildNodes();
    }
    
    /**
     * Utility method for putting a single Node to a new NodeList
     * consisting only of this Node.
     * @param n Node to put in NodeList
     * @return NodeList containing copy of the Node n
     */
    private NodeList nodeToNodeList(Node n) {
        A_OpenCms.log(C_OPENCMS_DEBUG, "nodeToNodeList called with node " + n);
        Element tempNode = m_content.createElement("TEMP");
        tempNode.appendChild(n.cloneNode(true));
        return tempNode.getChildNodes();
    }
        
    /**
     * Concats two datablock hashtables and returns the resulting one.
     * 
     * @param data1 First datablock hashtable.
     * @param data2 Second datablock hashtable.
     * @return Concatenated data.
     */
    private Hashtable concatData(Hashtable data1, Hashtable data2) {
        Hashtable retValue = (Hashtable) data1.clone();
        Enumeration keys = data2.keys();
        Object key;
		
        while(keys.hasMoreElements()) {
            key = keys.nextElement();
            retValue.put(key, data2.get(key));      
        }   
        return retValue;	
    }
	
    /**
	 * Internal method for debugging purposes.
	 * Dumpes the content of a given NodeList to the logfile.
	 * 
	 * @param l NodeList to dump.
	 */
    private void dumpNodeList(NodeList l) {
        if(A_OpenCms.isLogging()) {
            if(l == null) {
                A_OpenCms.log(C_OPENCMS_DEBUG, "******* NODE LIST IS NULL ********");
            } else {       
                int len = l.getLength();
                A_OpenCms.log(C_OPENCMS_DEBUG, "******** DUMP OF NODELIST ********");
                A_OpenCms.log(C_OPENCMS_DEBUG, "* LEN: " + len);
                for(int i=0; i<len; i++) {
                    A_OpenCms.log(C_OPENCMS_DEBUG, "*" + l.item(i));
                }
                A_OpenCms.log(C_OPENCMS_DEBUG, "**********************************");
            }
        }
    }                                 
        
	/**
	 * Internal method for debugging purposes.
	 * Dumps the content of the datablock hashtable to the logfile.
	 */
    private void dumpDatablocks() {
        if(A_OpenCms.isLogging()) {
            Enumeration hashKeys = m_blocks.keys();
            String key = null;
            Element node = null;
        
            A_OpenCms.log(C_OPENCMS_DEBUG, "******** DUMP OF DATABLOCK HASHTABLE *********");
            while(hashKeys.hasMoreElements()) {
                key = (String)hashKeys.nextElement();
                node = (Element)m_blocks.get(key);
                A_OpenCms.log(C_OPENCMS_DEBUG, "* " + key + " --> " + node.getNodeName());
            }
            A_OpenCms.log(C_OPENCMS_DEBUG, "**********************************************");
        }
    }    
}