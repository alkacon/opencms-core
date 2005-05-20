/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/template/Attic/A_CmsXmlContent.java,v $
* Date   : $Date: 2005/05/20 12:10:17 $
* Version: $Revision: 1.5 $
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

package com.opencms.template;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.legacy.CmsLegacyException;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
 * @version $Revision: 1.5 $ $Date: 2005/05/20 12:10:17 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public abstract class A_CmsXmlContent implements I_CmsXmlContent {

    /** parameter types for XML node handling methods. */
    public static final Class[] C_PARAMTYPES_HANDLING_METHODS = new Class[] {Element.class, Object.class, Object.class};

    /** parameter types for user methods called by METHOD tags. */
    public static final Class[] C_PARAMTYPES_USER_METHODS = new Class[] {CmsObject.class, String.class, A_CmsXmlContent.class, Object.class};

    /** The classname of the super XML content class. */
    public static final String C_MINIMUM_CLASSNAME = "com.opencms.template.A_CmsXmlContent";

    /** Constant extension of the template-files. */
    public static final String C_TEMPLATE_EXTENSION = "";

    /** Error message for bad <code>&lt;PROCESS&gt;</code> tags. */
    public static final String C_ERR_NODATABLOCK = "? UNKNOWN DATABLOCK ";

    /** CmsObject Object for accessing resources. */
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

    /** Constant for registering handling tags. */
    protected static final int C_REGISTER_FIRST_RUN = 1;

    /** Constant for registering handling tags. */
    protected static final int C_REGISTER_MAIN_RUN = 2;

    /** Boolean for additional debug output control. */
    private static final boolean C_DEBUG = false;

    /** DOM representaion of the template content. */
    private Document m_content;

    /** Filename this template was read from. */
    private String m_filename;

    /** All datablocks in DOM format. */
    private Hashtable m_blocks = new Hashtable();

    /** Reference all included A_CmsXmlContents. */
    private Vector m_includedTemplates = new Vector();

    /** Cache for parsed documents. */
    private static Hashtable m_filecache = new Hashtable();

    /** XML parser. */
    private static I_CmsXmlParser m_parser = new CmsXmlXercesParser();

    private String m_newEncoding;

    /** Constructor for creating a new instance of this class. */
    public A_CmsXmlContent() {
        registerAllTags();
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
     * @param resolveMethods If true the methodtags will be resolved even if they have own CacheDirectives.
     * @return cutomizable user object
     * @throws CmsException if something goes wrong
     */
    protected Object callUserMethod(String methodName, String parameter, Object callingObject, Object userObj, boolean resolveMethods) throws CmsException {
        Object[] params = new Object[] {m_cms, parameter, this, userObj};
        Object result = null;

        // Check if the user selected a object where to look for the user method.
        if (callingObject == null) {
            throwException("You are trying to call the user method \"" + methodName + "\" without giving an object containing this method. " + "Please select a callingObject in your getProcessedData or getProcessedDataValue call.", CmsLegacyException.C_XML_NO_USER_METHOD);
        }

        // check if the method has cachedirectives, if so we just return null
        // this way the methode tag stays in the Element and can be handled like
        // an normal element. We do this only if elementCache is active.
        if (CmsXmlTemplateLoader.isElementCacheEnabled() && !resolveMethods) {
            try {
                if (callingObject.getClass().getMethod("getMethodCacheDirectives", new Class[] {CmsObject.class, String.class}).invoke(callingObject, new Object[] {m_cms, methodName}) != null) {
                    return null;
                }
            } catch (NoSuchMethodException e) {
                throwException("Method getMethodeCacheDirectives was not found in class " + callingObject.getClass().getName() + ".", CmsLegacyException.C_XML_NO_USER_METHOD);
            } catch (InvocationTargetException targetEx) {

                // the method could be invoked, but throwed a exception
                // itself. Get this exception and throw it again.
                Throwable e = targetEx.getTargetException();
                if (!(e instanceof CmsException)) {
                    // Only print an error if this is NO CmsException
                    throwException("Method getMethodeCacheDirectives throwed an exception. " + e, CmsLegacyException.C_UNKNOWN_EXCEPTION);
                } else {
                    // This is a CmsException Error printing should be done previously.
                    throw (CmsException) e;
                }
            } catch (Exception exc2) {
                throwException("Method getMethodeCacheDirectives was found but could not be invoked. " + exc2, CmsLegacyException.C_XML_NO_USER_METHOD);
            }
        }

        // OK. We have a calling object. Now try to invoke the method
        try {
            // try to invoke the method 'methodName'
            result = getUserMethod(methodName, callingObject).invoke(callingObject, params);
        } catch (NoSuchMethodException exc) {
            throwException("User method " + methodName + " was not found in class " + callingObject.getClass().getName() + ".", CmsLegacyException.C_XML_NO_USER_METHOD);
        } catch (InvocationTargetException targetEx) {

            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.
            Throwable e = targetEx.getTargetException();
            if (!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                throwException("User method " + methodName + " throwed an exception. " + e, CmsLegacyException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException) e;
            }
        } catch (Exception exc2) {
            throwException("User method " + methodName + " was found but could not be invoked. " + exc2, CmsLegacyException.C_XML_NO_USER_METHOD);
        }
        if ((result != null) && (!(result instanceof String || result instanceof CmsProcessedString || result instanceof Integer || result instanceof NodeList || result instanceof byte[]))) {
            throwException("User method " + methodName + " in class " + callingObject.getClass().getName() + " returned an unsupported Object: " + result.getClass().getName(), CmsLegacyException.C_XML_PROCESS_ERROR);
        }
        return (result);
    }

    /**
     * Deletes all files from the file cache.
     */
    public static void clearFileCache() {
        if (OpenCms.getLog(A_CmsXmlContent.class).isInfoEnabled()) {
            OpenCms.getLog(A_CmsXmlContent.class).info("Clearing XML file cache.");
        }
        m_filecache.clear();
    }

    /**
     * Deletes the file represented by the given A_CmsXmlContent from
     * the file cache.
     * @param doc A_CmsXmlContent representing the XML file to be deleted.
     */
    public static void clearFileCache(A_CmsXmlContent doc) {
        if (doc != null) {
            String currentProject = doc.m_cms.getRequestContext().currentProject().getName();
            String filename = doc.m_cms.getRequestContext().addSiteRoot(doc.getAbsoluteFilename());
            m_filecache.remove(currentProject + ":" + filename);
        }
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
     * Creates a clone of this object.
     * @return cloned object.
     * @throws CloneNotSupportedException if an error occurs while cloning
     */
    public Object clone() throws CloneNotSupportedException {
        try {
            A_CmsXmlContent newDoc = (A_CmsXmlContent) getClass().newInstance();
            newDoc.init(m_cms, (Document) m_content.cloneNode(true), m_filename);
            return newDoc;
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error while trying to clone object " + this.getClass().getName());
            }
            throw new CloneNotSupportedException(e.toString());
        }
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
        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            retValue.put(key, data2.get(key));
        }
        return retValue;
    }

    /**
     * Create a new CmsFile object containing an empty XML file of the
     * current content type.
     * 
     * The String returned by <code>getXmlDocumentTagName()</code>
     * will be used to build the XML document element.
     * 
     * @param cms Current cms object used for accessing system resources.
     * @param filename Name of the file to be created.
     * @param documentType Document type of the new file.
     * @throws CmsException if no absolute filename is given or write access failed.
     */
    public void createNewFile(CmsObject cms, String filename, String documentType) throws CmsException {
        if (!filename.startsWith("/")) {

            // this is no absolute filename.
            this.throwException("Cannot create new file. Bad name.", CmsLegacyException.C_BAD_NAME);
        }
        int pos = filename.lastIndexOf("/") + 1;
        String folder = filename.substring(0, pos);
        int type = OpenCms.getResourceManager().getResourceType(documentType).getTypeId();
        cms.createResource(folder + filename, type);
        cms.lockResource(filename);
        m_cms = cms;
        m_filename = filename;
        try {
            m_content = m_parser.createEmptyDocument(getXmlDocumentTagName());
        } catch (Exception e) {
            throwException("Cannot create empty XML document for file " + m_filename + ". ", CmsLegacyException.C_XML_PARSING_ERROR);
        }
        write();
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
        tag = tag.toLowerCase();
        Element originalBlock = (Element) (m_blocks.get(tag));
        while (originalBlock.hasChildNodes()) {
            originalBlock.removeChild(originalBlock.getFirstChild());
        }
        originalBlock.appendChild(m_content.createTextNode(data));
    }

    /**
     * Gets the absolute filename of the XML file represented by this content class.<p>
     * 
     * @return Absolute filename
     */
    public String getAbsoluteFilename() {
        return m_filename;
    }

    /**
     * Gets all datablocks (the datablock hashtable).
     * @return Hashtable with all datablocks.
     */
    protected Hashtable getAllData() {
        return m_blocks;
    }

    /**
     * This method should be implemented by every extending class.
     * It returns a short description of the content definition type
     * (e.g. "OpenCms news article").
     * @return content description.
     */
    public abstract String getContentDescription();

    /**
     * Gets a complete datablock from the datablock hashtable.
     *
     * @param tag Key for the datablocks hashtable.
     * @return Complete DOM element of the datablock for the given key or null if no datablock is found for this key.
     * @throws CmsException if something goes wrong
     */
    protected Element getData(String tag) throws CmsException {
        Object result = m_blocks.get(tag.toLowerCase());
        if (result == null) {
            String errorMessage = "Unknown Datablock " + tag + " requested.";
            throwException(errorMessage, CmsLegacyException.C_XML_UNKNOWN_DATA);
        } else {
            if (!(result instanceof Element)) {
                String errorMessage = "Unexpected object returned as datablock. Requested Tagname: " + tag + ". Returned object: " + result.getClass().getName() + ".";
                throwException(errorMessage, CmsLegacyException.C_XML_CORRUPT_INTERNAL_STRUCTURE);
            }
        }
        return (Element) m_blocks.get(tag.toLowerCase());
    }

    /**
     * Gets the text and CDATA content of a datablock from the
     * datablock hashtable.
     *
     * @param tag Key for the datablocks hashtable.
     * @return Datablock content for the given key or null if no datablock is found for this key.
     * @throws CmsException if something goes wrong
     */
    protected String getDataValue(String tag) throws CmsException {
        Element dataElement = getData(tag);
        return getTagValue(dataElement);
    }

    /**
     * Gets a short filename (without path) of the XML file represented by this content class
     * of the template file.
     * @return filename
     */
    public String getFilename() {
        return m_filename.substring(m_filename.lastIndexOf("/") + 1);
    }

    /**
     * Gets a processed datablock from the datablock hashtable.
     *
     * @param tag Key for the datablocks hashtable.
     * @return Processed datablock for the given key.
     * @throws CmsException if something goes wrong
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
     * @throws CmsException if something goes wrong
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
     * @throws CmsException if something goes wrong
     */
    protected Element getProcessedData(String tag, Object callingObject, Object userObj) throws CmsException {
        Element dBlock = (Element) getData(tag).cloneNode(true);
        processNode(dBlock, m_mainProcessTags, null, callingObject, userObj);
        return dBlock;
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
     * @param stream OutputStream that may be used for directly streaming the results or null.
     * @return Processed datablock for the given key.
     * @throws CmsException if something goes wrong
     */
    protected Element getProcessedData(String tag, Object callingObject, Object userObj, OutputStream stream) throws CmsException {
        Element dBlock = (Element) getData(tag).cloneNode(true);
        processNode(dBlock, m_mainProcessTags, null, callingObject, userObj, stream);
        return dBlock;
    }

    /**
     * Gets the text and CDATA content of a processed datablock from the
     * datablock hashtable.
     *
     * @param tag Key for the datablocks hashtable.
     * @return Processed datablock for the given key.
     * @throws CmsException if something goes wrong
     */
    protected String getProcessedDataValue(String tag) throws CmsException {
        return getProcessedDataValue(tag, null, null, null);
    }

    /**
     * Gets the text and CDATA content of a processed datablock from the
     * datablock hashtable.
     *
     * @param tag Key for the datablocks hashtable.
     * @param callingObject Object that should be used to look up user methods.
     * @return Processed datablock for the given key.
     * @throws CmsException if something goes wrong
     */
    protected String getProcessedDataValue(String tag, Object callingObject) throws CmsException {
        return getProcessedDataValue(tag, callingObject, null, null);
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
     * @throws CmsException if something goes wrong
     */
    protected String getProcessedDataValue(String tag, Object callingObject, Object userObj) throws CmsException {
        return getProcessedDataValue(tag, callingObject, userObj, null);
    }

    /**
     * Gets the text and CDATA content of a processed datablock from the
     * datablock hashtable. An eventually given output stream is user for streaming
     * the generated result directly to the response output stream while processing.
     * <P>
     * The userObj Object is passed to all called user methods.
     * By using this, the initiating class can pass customized data to its methods.
     *
     * @param tag Key for the datablocks hashtable.
     * @param callingObject Object that should be used to look up user methods.
     * @param userObj any object that should be passed to user methods
     * @param stream OutputStream that may be used for directly streaming the results or null.
     * @return Processed datablock for the given key.
     * @throws CmsException if something goes wrong
     */
    protected String getProcessedDataValue(String tag, Object callingObject, Object userObj, OutputStream stream) throws CmsException {
        // we cant cache the methods here, so we use the other way
        registerTag("METHOD", A_CmsXmlContent.class, "handleMethodTagForSure", C_REGISTER_MAIN_RUN);
        Element data = getProcessedData(tag, callingObject, userObj, stream);
        registerTag("METHOD", A_CmsXmlContent.class, "handleMethodTag", C_REGISTER_MAIN_RUN);
        return getTagValue(data);
    }

    /**
     * Reads all text or CDATA values from the given XML element,
     * e.g. <code>&lt;ELEMENT&gt;foo blah &lt;![CDATA[&lt;H1&gt;Hello&lt;/H1&gt;]]&gt;&lt;/ELEMENT&gt;</code>.
     *
     * @param n Element that should be read out.
     * @return concatenated string of all text and CDATA nodes or <code>null</code>
     * if no nodes were found.
     */
    protected String getTagValue(Element n) {
        StringBuffer result = new StringBuffer();
        if (n != null) {
            NodeList childNodes = n.getChildNodes();
            Node child = null;
            if (childNodes != null) {
                int numchilds = childNodes.getLength();
                for (int i = 0; i < numchilds; i++) {
                    child = childNodes.item(i);
                    String nodeValue = child.getNodeValue();

                    if (nodeValue != null) {
                        //if(child.getNodeType() == n.TEXT_NODE || child.getNodeType() == n.CDATA_SECTION_NODE) {
                        if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                            //result.append(child.getNodeValue());
                            result.append(nodeValue);
                        } else {
                            if (child.getNodeType() == Node.TEXT_NODE) {
                                //String s = child.getNodeValue().trim();
                                nodeValue = nodeValue.trim();
                                //if(!"".equals(s)) {
                                if (!"".equals(nodeValue)) {
                                    //result.append(child.getNodeValue());
                                    result.append(nodeValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Looks up a user defined method requested by a "METHOD" tag.
     * The method is searched in the Object callingObject.
     * @param methodName Name of the user method
     * @param callingObject Object that requested the processing of the XML document
     * @return user method
     * @throws NoSuchMethodException
     */
    private Method getUserMethod(String methodName, Object callingObject) throws NoSuchMethodException {
        if (methodName == null || "".equals(methodName)) {

            // no valid user method name
            throw (new NoSuchMethodException("method name is null or empty"));
        }
        return callingObject.getClass().getMethod(methodName, C_PARAMTYPES_USER_METHODS);
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
     * This method should be implemented by every extending class.
     * It returns the name of the XML document tag to scan for.
     * @return name of the XML document tag.
     */
    public abstract String getXmlDocumentTagName();

    /**
     * Gets the currently used XML Parser.
     * @return currently used parser.
     */
    public static I_CmsXmlParser getXmlParser() {
        return m_parser;
    }

    /**
     * Prints the XML parsed content to a String.<p>
     * 
     * @return String with XML content
     */
    public String getXmlText() {
        StringWriter writer = new StringWriter();
        getXmlText(writer);
        return writer.toString();
    }

    /**
     * Prints the XML parsed content of this template file
     * to the given Writer.
     *
     * @param out Writer to print to.
     */
    public void getXmlText(Writer out) {
        m_parser.getXmlText(m_content, out);
    }

    /**
     * Prints the XML parsed content of the given Node and
     * its subnodes to the given Writer.<p>
     *
     * @param out Writer to print to.
     * @param n Node that should be printed.
     */
    public void getXmlText(Writer out, Node n) {
        Document tempDoc = (Document) m_content.cloneNode(false);
        tempDoc.appendChild(m_parser.importNode(tempDoc, n));
        m_parser.getXmlText(tempDoc, out);
    }
    
    /**
     * Prints the XML parsed content of the given Node and
     * its subnodes to the given Writer.<p>
     *
     * @param out Stream to print to
     */
    public void getXmlText(OutputStream out) {
        m_parser.getXmlText(m_content, out, m_newEncoding);
    }

    /**
     * Prints the XML parsed content of the given Node and
     * its subnodes to the given Writer.<p>
     *
     * @param out Stream to print to
     * @param n Node that should be printed.
     */
    public void getXmlText(OutputStream out, Node n) {
        Document tempDoc = (Document) m_content.cloneNode(false);
        tempDoc.appendChild(m_parser.importNode(tempDoc, n));
        m_parser.getXmlText(tempDoc, out, m_newEncoding);
    }

    /**
     * Prints the XML parsed content of a given node and
     * its subnodes to a String.<p>
     * 
     * @param n Node that should be printed.
     * @return String with XML content
     */
    public String getXmlText(Node n) {
        StringWriter writer = new StringWriter();
        getXmlText(writer, n);
        return writer.toString();
    }

    /**
     * This method is just a hack so that the Eclise IDE will not show the methods listed here 
     * as warnings when the "unused private methods" option is selected, 
     * since they are called only using reclection API.
     * Do not use this method. 
     * 
     * @throws CmsException if something goes wrong
     */
    protected void callAllUncalledMethodsSoThatEclipseDoesntComplainAboutThem() throws CmsException {
        this.handleDataTag(null, null, null);
        this.handleIncludeTag(null, null, null);
        this.handleLinkTag(null, null, null);
        this.handleMethodTag(null, null, null);
        this.handleMethodTag(null, null, null);
        this.handleMethodTagForSure(null, null, null);
        this.handleProcessTag(null, null, null);
        this.replaceTagByComment(null, null, null);
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
        while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {

            // check if this datablock is part of a datablock

            // hierarchy like 'language.de.btn_yes'

            // look for the best fitting hierarchy name part, too
            if (parent.getNodeName().equals("DATA")) {
                blockname = ((Element) parent).getAttribute("name");
            } else {
                blockname = parent.getNodeName();
                String secondName = ((Element) parent).getAttribute("name");
                if (!"".equals(secondName)) {
                    blockname = blockname + "." + secondName;
                }
            }
            blockname = blockname.toLowerCase();
            if (parentname == null) {
                parentname = blockname;
            } else {
                parentname = blockname + "." + parentname;
            }
            if (m_blocks.containsKey(parentname)) {
                bestFit = parentname;
            }
            parent = parent.getParentNode();
        }

        // bestFit now contains the best fitting name part

        // next, look for the tag name (the part behind the last ".")
        if (n.getNodeName().equals("DATA")) {
            blockname = n.getAttribute("name");
        } else {
            blockname = n.getNodeName();
            String secondName = n.getAttribute("name");
            if (!"".equals(secondName)) {
                blockname = blockname + "." + secondName;
            }
        }
        blockname = blockname.toLowerCase();

        // now we can build the complete datablock name
        if (bestFit != null) {
            blockname = bestFit + "." + blockname;
        }
        if (OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Reading datablock " + blockname);
        }

        // finally we cat put the new datablock into the hashtable
        m_blocks.put(blockname, n);

        //return null;
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
     * Handling of "LINK" tags.
     * @param n XML element containing the <code>&lt;LINK&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     */
    private Object handleLinkTag(Element n, Object callingObject, Object userObj) throws CmsException {
        // get the string and call the getLinkSubstitution method
        Element dBlock = (Element) n.cloneNode(true);
        processNode(dBlock, m_mainProcessTags, null, callingObject, userObj, null);
        String link = getTagValue(dBlock);
        return OpenCms.getLinkManager().substituteLink(m_cms, link);
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
     * @throws CmsException
     */
    private Object handleMethodTag(Element n, Object callingObject, Object userObj) throws CmsException {
        processNode(n, m_mainProcessTags, null, callingObject, userObj);
        String tagcontent = getTagValue(n);
        String method = n.getAttribute("name");
        Object result = null;
        try {
            result = callUserMethod(method, tagcontent, callingObject, userObj, false);
        } catch (Throwable e1) {
            if (e1 instanceof CmsException) {
                throw (CmsException) e1;
            } else {
                throwException("handleMethodTag() received an exception from callUserMethod() while calling \"" + method + "\" requested by class " + callingObject.getClass().getName() + ": " + e1);
            }
        }
        return result;
    }

    /**
     * Handling of the "METHOD name=..." tags.
     * In contrast to the method handleMethodTag this method resolves
     * every method even if it has it own CacheDirectives. It is used only for
     * getProcessedDataValue.
     * Name attribute and value of the element are read and the user method
     * 'name' is invoked with the element value as parameter.
     *
     * @param n XML element containing the <code>&lt;METHOD&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @return Object returned by the user method
     * @throws CmsException
     */
    private Object handleMethodTagForSure(Element n, Object callingObject, Object userObj) throws CmsException {
        processNode(n, m_mainProcessTags, null, callingObject, userObj);
        String tagcontent = getTagValue(n);
        String method = n.getAttribute("name");
        Object result = null;
        try {
            result = callUserMethod(method, tagcontent, callingObject, userObj, true);
        } catch (Throwable e1) {
            if (e1 instanceof CmsException) {
                throw (CmsException) e1;
            } else {
                throwException("handleMethodTagForSure() received an exception from callUserMethod() while calling \"" + method + "\" requested by class " + callingObject.getClass().getName() + ": " + e1);
            }
        }
        return result;
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
        if (OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Request for datablock \"" + blockname + "\"");
        }
        datablock = ((Element)m_blocks.get(blockname));
        if (datablock == null) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                String logUri = "";
                try {
                    logUri = " RequestUri is " + m_cms.getRequestContext().getFolderUri() + m_cms.getRequestContext().getUri() + ".";
                } catch (Exception e) {
                    // noop    
                }
                OpenCms.getLog(this).error("Requested datablock  \"" + blockname + "\" not found in " + m_filename + " - " + logUri);
            }
            return C_ERR_NODATABLOCK + blockname;
        } else {
            return datablock.getChildNodes();
        }
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
     * Initialize the XML content class.
     * Load and parse the content of the given CmsFile object.
     * @param cms CmsObject Object for accessing resources.
     * @param file CmsFile object of the file to be loaded and parsed.
     * @throws CmsException if something goes wrong
     */
    public void init(CmsObject cms, CmsFile file) throws CmsException {
        String filename = cms.getSitePath(file);
        String currentProject = cms.getRequestContext().currentProject().getName();
        Document parsedContent = null;
        m_cms = cms;
        m_filename = filename;
        parsedContent = loadCachedDocument(cms.getRequestContext().addSiteRoot(filename));
        if (parsedContent == null) {
            byte[] fileContent = file.getContents();
            if (fileContent == null || fileContent.length <= 1) {
                // The file content is empty. Possibly the file object is only
                // a file header. Re-read the file object and try again
                file = cms.readFile(filename);
                fileContent = file.getContents();
            }
            if (fileContent == null || fileContent.length <= 1) {
                // The file content is still emtpy.
                // Start with an empty XML document.
                try {
                    parsedContent = getXmlParser().createEmptyDocument(getXmlDocumentTagName());
                } catch (Exception e) {
                    throwException("Could not initialize now XML document " + filename + ". " + e, CmsLegacyException.C_XML_PARSING_ERROR);
                }
            } else {
                parsedContent = parse(fileContent);
            }
            m_filecache.put(currentProject + ":" + cms.getRequestContext().addSiteRoot(filename), parsedContent.cloneNode(true));
            fileContent = null;
        }
        init(cms, parsedContent, filename);
    }

    /**
     * Initialize the XML content class.
     * Load and parse the content of the given String.
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param filename file name to use when storing the parsed XML in the cache
     * @param content XMl content to parse
     * @throws CmsException if something goes wrong
     */
    public void init(CmsObject cms, String filename, String content) throws CmsException {
        m_cms = cms;
        m_filename = filename + I_CmsConstants.C_XML_CONTROL_FILE_SUFFIX;  
        init(cms, parse(content.getBytes()), filename);
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
     * @param filename CmsFile name of the file to be loaded and parsed.
     * @throws CmsException if something goes wrong
     */
    public void init(CmsObject cms, String filename) throws CmsException {

        if (!filename.startsWith("/")) {
            throw new CmsLegacyException("A relative path has entered the A_CmsXmlContent class. filename=" + filename + "");
        }
        String currentProject = cms.getRequestContext().currentProject().getName();
        Document parsedContent = null;
        m_cms = cms;
        m_filename = filename;
        parsedContent = loadCachedDocument(cms.getRequestContext().addSiteRoot(filename));
        if (parsedContent == null) {
            CmsFile file = cms.readFile(filename);
            
            parsedContent = parse(file.getContents());
            m_filecache.put(currentProject + ":" + cms.getRequestContext().addSiteRoot(filename), parsedContent.cloneNode(true));
        } else {

            // File was found in cache.
            // We have to read the file header to check access rights.
            cms.readResource(filename);
        }

        if (C_PRINTNODES) {
            if (filename.indexOf(I_CmsWpConstants.C_VFS_DIR_LOCALES) != -1) {
                System.err.println("\n" + filename);
                this.printNode(parsedContent, 0, "");
            }
        }

        init(cms, parsedContent, filename);
    }

    /** Flag to enable / disable printNode() method. */
    private static final boolean C_PRINTNODES = false;

    /**
     * Prints all nodes of a XML locale file in depth first order split by "."
     * to STDOUT.<p>
     * 
     * This method is useful for backward compatibility: you can copy
     * the output of this method (which is written to $TOMCAT_HOME/logs/catalina.
     * out) to build Java resource bundles. This method is for internal use
     * only, should be deactivated on a production system!<p>
     * 
     * Activate this method by setting the value of C_PRINTNODES to true;
     * 
     * @param node the current node in the XML document that is examined
     * @param depth the current depth in the XML tree
     * @param path the current path of the XML nodes, eg. node1.node2.node3...
     */
    private void printNode(Node node, int depth, String path) {

        if (C_PRINTNODES) {
            // Char array for the printNode() method 
            final String badChars = "\n";

            // Char array for the printNode() method
            final String[] goodChars = {"\\n"};

            int nodeType = node.getNodeType();

            if (nodeType == Node.ELEMENT_NODE) {
                String nodeName = node.getNodeName();

                if (!"".equals(nodeName)) {
                    if (depth > 2) {
                        path += ".";
                    }
                    if (depth > 1) {
                        path += nodeName;
                    }
                }
            } else if (nodeType == Node.TEXT_NODE) {
                String nodeValue = node.getNodeValue();

                if (!"".equals(nodeValue)) {
                    int nodeValueLength = nodeValue.length();
                    String nodeValueNoBadChars = "";

                    for (int i = 0; i < nodeValueLength; i++) {
                        int index = 0;

                        if ((index = badChars.indexOf(nodeValue.charAt(i))) != -1) {
                            nodeValueNoBadChars += goodChars[index];
                        } else {
                            nodeValueNoBadChars += nodeValue.charAt(i);
                        }
                    }

                    if (node.getPreviousSibling() == null) {
                        System.out.print(path + "=");
                    }

                    System.out.print(nodeValueNoBadChars);

                    if (node.getNextSibling() == null) {
                        System.out.print("\n");
                    }
                }
            } else if (nodeType == Node.CDATA_SECTION_NODE) {
                CDATASection cdata = (CDATASection) node;
                String nodeValue = cdata.getData();

                if (!"".equals(nodeValue)) {
                    int nodeValueLength = nodeValue.length();
                    String nodeValueNoBadChars = "";

                    for (int i = 0; i < nodeValueLength; i++) {
                        int index = 0;

                        if ((index = badChars.indexOf(nodeValue.charAt(i))) != -1) {
                            nodeValueNoBadChars += goodChars[index];
                        }    else {
                            nodeValueNoBadChars += nodeValue.charAt(i);
                        }
                    }

                    if (node.getPreviousSibling() == null) {
                        System.out.print(path + "=");
                    }

                    System.out.print(nodeValueNoBadChars);

                    if (node.getNextSibling() == null) {
                        System.out.print("\n");
                    }
                }
            }

            NodeList nodeChildren = node.getChildNodes();
            if (nodeChildren != null) {
                for (int i = 0; i < nodeChildren.getLength(); i++) {
                    printNode(nodeChildren.item(i), depth + 1, path);
                }
            }
        }
    }

    /**
     * Initialize the class with the given parsed XML DOM document.
     * @param cms CmsObject Object for accessing system resources.
     * @param content DOM document object containing the parsed XML file.
     * @param filename OpenCms filename of the XML file.
     * @throws CmsException if something goes wrong
     */
    public void init(CmsObject cms, Document content, String filename) throws CmsException {
        m_cms = cms;
        m_content = content;
        m_filename = filename;

        // First check the document tag. Is this the right document type?
        Element docRootElement = m_content.getDocumentElement();
        String docRootElementName = docRootElement.getNodeName().toLowerCase();
        if (!docRootElementName.equals(getXmlDocumentTagName().toLowerCase())) {

            // Hey! This is a wrong XML document!

            // We will throw an execption and the document away :-)
            removeFromFileCache();
            m_content = null;
            String errorMessage = "XML document " + getAbsoluteFilename() + " is not of the expected type. This document is \"" + docRootElementName + "\", but it should be \"" + getXmlDocumentTagName() + "\" (" + getContentDescription() + ").";
            throwException(errorMessage, CmsLegacyException.C_XML_WRONG_CONTENT_TYPE);
        }

        // OK. Document tag is fine. Now get the DATA tags and collect them

        // in a Hashtable (still in DOM representation!)
        try {
            processNode(m_content, m_firstRunTags, A_CmsXmlContent.class.getDeclaredMethod("handleDataTag", C_PARAMTYPES_HANDLING_METHODS), null, null);
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Error while scanning for DATA and INCLUDE tags in file " + getAbsoluteFilename(), e);
            }
            throw e;
        } catch (NoSuchMethodException e2) {
            String errorMessage = "XML tag process method \"handleDataTag\" could not be found";
            throwException(errorMessage, CmsLegacyException.C_XML_NO_PROCESS_METHOD);
        }
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
        if ((!data.getNodeName().toLowerCase().equals("data")) && nameAttr != null && (!"".equals(nameAttr))) {
            // this is an extended datablock
            workTag = tag.substring(0, tag.lastIndexOf("."));
        } else {
            workTag = tag;
        }
        // Import the node for later inserting
        Element importedNode = (Element) m_parser.importNode(m_content, data);

        // Check, if this is a simple datablock without hierarchy.
        if (workTag.indexOf(".") == -1) {
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
            while ((!found) && (dotIndex > 1)) {
                match = match.substring(0, dotIndex);
                if (hasData(match.substring(1))) {
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
            if (found) {
                datablockPrefix = match.substring(1) + ".";
            }
            // number of new elements to be created
            int numNewBlocks = newBlocks.size();
            // used to create the required new elements
            Element newElem = null;
            // Contains the last existing Element in the hierarchy.
            Element lastElem = null;
            // now create the new elements backwards
            for (int i = numNewBlocks - 1; i >= 0; i--) {
                newElem = m_content.createElement("DATA");
                newElem.setAttribute("name", (String) newBlocks.elementAt(i));
                m_blocks.put(datablockPrefix + (String) newBlocks.elementAt(i), newElem);
                if (lastElem != null) {
                    lastElem.appendChild(newElem);
                } else {
                    lastElem = newElem;
                }
            }
            // Now all required parent datablocks are created.
            // Finally the given datablock can be inserted.
            if (lastElem != null) {
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
            if (found) {
                Element parent = (Element) m_blocks.get(match.substring(1));
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
        Document lookup = (Document) m_filecache.get(currentProject + ":" + filename);
        if (lookup != null) {
            try {
                //cachedDoc = lookup.cloneNode(true).getOwnerDocument();
                cachedDoc = (Document) lookup.cloneNode(true);
            } catch (Exception e) {
                lookup = null;
                cachedDoc = null;
            }
        }
        if (OpenCms.getLog(this).isDebugEnabled() && cachedDoc != null) {
            OpenCms.getLog(this).debug("Reused previously parsed XML file " + getFilename());
        }
        return cachedDoc;
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
        Element tempNode = (Element) n.cloneNode(false);
        while (tempNode.hasChildNodes()) {
            tempNode.removeChild(tempNode.getFirstChild());
        }
        tempNode.appendChild(m_content.createComment("removed " + n.getNodeName()));
        return tempNode.getChildNodes();
    }
    
    /**
     * Starts the XML parser with the content of the given CmsFile object.
     * After parsing the document it is scanned for INCLUDE and DATA tags
     * by calling processNode with m_firstRunParameters.
     *
     * @param content byte array to be parsed
     * @return parsed DOM document
     * @throws CmsException if something goes wrong
     */
    protected Document parse(byte[] content) throws CmsException {
        return parse(new ByteArrayInputStream(content));
    }

    /**
     * Starts the XML parser with the content of the given CmsFile object.
     * After parsing the document it is scanned for INCLUDE and DATA tags
     * by calling processNode with m_firstRunParameters.
     *
     * @param content String to be parsed
     * @return Parsed DOM document.
     * @throws CmsException if something goes wrong
     */
    protected Document parse(InputStream content) throws CmsException {
        Document parsedDoc = null;

        // First parse the String for XML Tags and
        // get a DOM representation of the document
        try {
            parsedDoc = m_parser.parse(content);
        } catch (Exception e) {
            // Error while parsing the document.
            // there ist nothing to do, we cannot go on.
            String errorMessage = "Cannot parse XML file \"" + getAbsoluteFilename() + "\". " + e;
            throwException(errorMessage, CmsLegacyException.C_XML_PARSING_ERROR);
        }
        if (parsedDoc == null) {
            String errorMessage = "Unknown error. Parsed DOM document is null.";
            throwException(errorMessage, CmsLegacyException.C_XML_PARSING_ERROR);
        }

        // Try to normalize the XML document.
        // We should not call the normalize() method in the usual way
        // here, since the DOM interface changed at this point between
        // Level 1 and Level 2.
        // It's better to lookup the normalize() method first using reflection
        // API and call it then. So we will get the appropriate method for the
        // currently used DOM level and avoid NoClassDefFound exceptions.
        try {
            Class elementClass = Class.forName("org.w3c.dom.Element");
            Method normalizeMethod = elementClass.getMethod("normalize", new Class[] {});
            normalizeMethod.invoke(parsedDoc.getDocumentElement(), new Object[] {});
        } catch (Exception e) {
            // The workaround using reflection API failed.
            // We have to throw an exception.
            throwException("Normalizing the XML document failed. Possibly you are using concurrent versions of " + "the XML parser with different DOM levels. ", e, CmsLegacyException.C_XML_PARSING_ERROR);
        }
        // Delete all unnecessary text nodes from the tree.
        // These nodes could cause errors when serializing this document otherwise
        Node loop = parsedDoc.getDocumentElement();
        while (loop != null) {
            Node next = treeWalker(parsedDoc.getDocumentElement(), loop);
            if (loop.getNodeType() == Node.TEXT_NODE) {
                Node leftSibling = loop.getPreviousSibling();
                Node rightSibling = loop.getNextSibling();
                if (leftSibling == null || rightSibling == null || (leftSibling.getNodeType() == Node.ELEMENT_NODE && rightSibling.getNodeType() == Node.ELEMENT_NODE)) {
                    if ("".equals(loop.getNodeValue().trim())) {
                        loop.getParentNode().removeChild(loop);
                    }
                }
            }
            loop = next;
        }
        return parsedDoc;
    }

    /**
     * Main processing funtion for the whole XML document.
     *
     * @param keys Hashtable with XML tags to look for and corresponding methods.
     * @param defaultMethod Method to be called if the tag is unknown.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @throws CmsException if something goes wrong
     */
    protected void processDocument(Hashtable keys, Method defaultMethod, Object callingObject, Object userObj) throws CmsException {
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
     * @throws CmsException if something goes wrong
     */
    protected void processNode(Node n, Hashtable keys, Method defaultMethod, Object callingObject, Object userObj) throws CmsException {
        processNode(n, keys, defaultMethod, callingObject, userObj, null);
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
     * @param stream the output stream
     * @throws CmsException if something goes wrong
     */
    protected void processNode(Node n, Hashtable keys, Method defaultMethod, Object callingObject, Object userObj, OutputStream stream) throws CmsException {

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

        // Used for streaming mode. Indicates, if the replaced results for the current node are already written to the stream.
        boolean newnodesAreAlreadyProcessed = false;

        // We should remember the starting node for walking through the tree.
        Node startingNode = n;

        // only start if there is something to process
        if (n != null && n.hasChildNodes()) {
            child = n.getFirstChild();
            while (child != null) {
                childName = child.getNodeName().toLowerCase();

                // Get the next node in the tree first
                nextchild = treeWalker(startingNode, child);

                // Only look for element nodes

                // all other nodes are not very interesting
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    newnodes = null;
                    callMethod = null;
                    newnodesAreAlreadyProcessed = false;
                    if (keys.containsKey(childName)) {

                        // name of this element found in keys Hashtable
                        callMethod = (Method) keys.get(childName);
                    } else {
                        if (!m_knownTags.contains(childName)) {

                            // name was not found
                            // and even name is not known as tag
                            callMethod = defaultMethod;
                        }
                    }
                    if (callMethod != null) {
                        methodResult = null;
                        try {
                            if (C_DEBUG && OpenCms.getLog(this).isDebugEnabled()) {
                                OpenCms.getLog(this).debug("<" + childName + "> tag found. Value: " + child.getNodeValue());
                                OpenCms.getLog(this).debug("Tag will be handled by method [" + callMethod.getName() + "]. Invoking method NOW.");
                            }

                            // now invoke the tag processing method.
                            methodResult = callMethod.invoke(this, new Object[] {child, callingObject, userObj});
                        } catch (Exception e) {
                            if (e instanceof InvocationTargetException) {
                                Throwable thrown = ((InvocationTargetException) e).getTargetException();

                                // if the method has thrown a cms exception then
                                // throw it again
                                if (thrown instanceof CmsException) {
                                    throw (CmsException) thrown;
                                } else {
                                    throwException("processNode received an exception while handling XML tag \"" + childName + "\" by \"" + callMethod.getName() + "\" for file " + getFilename() + ": " + e, CmsLegacyException.C_XML_PROCESS_ERROR);
                                }
                            } else {
                                throwException("processNode could not invoke the XML tag handling method " + callMethod.getName() + "\" for file " + getFilename() + ": " + e, CmsLegacyException.C_XML_PROCESS_ERROR);
                            }
                        }

                        // Inspect the type of the method return value
                        // Currently NodeList, String and Integer are
                        // recognized. All other types will be ignored.
                        if (methodResult == null) {
                            newnodes = null;
                        } else {
                            if (methodResult instanceof NodeList) {
                                newnodes = (NodeList) methodResult;
                            } else {
                                if (methodResult instanceof String) {
                                    newnodes = stringToNodeList((String) methodResult);
                                } else {
                                    if (methodResult instanceof CmsProcessedString) {
                                        newnodes = stringToNodeList(((CmsProcessedString) methodResult).toString());
                                        newnodesAreAlreadyProcessed = true;
                                    } else {
                                        if (methodResult instanceof Integer) {
                                            newnodes = stringToNodeList(((Integer) methodResult).toString());
                                        } else {
                                            if (methodResult instanceof byte[]) {
                                                newnodes = stringToNodeList(new String((byte[]) methodResult));
                                            } else {

                                                // Type not recognized.
                                                if (OpenCms.getLog(this).isErrorEnabled()) {
                                                    OpenCms.getLog(this).error("Return type of method " + callMethod.getName() + " not recognized, can not insert value");
                                                }
                                                newnodes = null;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // the list of nodes to be inserted could be printed out here.
                        // uncomment the following to activate this feature.
                        // printNodeList(newnodes);
                        if (newnodes != null) {
                            // the called method returned a valid result.
                            // we have do remove the old element from the tree
                            // and replace it by the new nodes.
                            // WARNING! Do not remove any subchilds from the old
                            // element. There could be links to the subchilds
                            // in our Hashtables (e.g. for datablocks).
                            // Only remove the child itself from the tree!
                            int numNewChilds = newnodes.getLength();
                            if (numNewChilds > 0) {

                                // there are new childs.
                                // so we can replace the old element
                                for (int j = 0; j < numNewChilds; j++) {

                                    //insert = parser.importNode(m_content, newnodes.item(j));
                                    insert = m_parser.importNode(child.getOwnerDocument(), newnodes.item(j));
                                    if (j == 0 && !newnodesAreAlreadyProcessed) {
                                        nextchild = insert;
                                    }

                                    //A_OpenCms.log(c_OPENCMS_DEBUG, "trying to add node " + newnodes.item(j));
                                    child.getParentNode().insertBefore(insert, child);

                                    //A_OpenCms.log(c_OPENCMS_DEBUG, "Node " + newnodes.item(j) + " added.");
                                }

                                if (newnodesAreAlreadyProcessed) {
                                    // We just have inserted new nodes that were processed prviously.
                                    // So we hav to recalculate the next child.
                                    nextchild = treeWalkerWidth(startingNode, child);
                                }

                            } else {

                                // the list of the new childs is empty.
                                // so we have to re-calculate the next node
                                // in the tree since the old nextchild will be deleted
                                // been deleted.
                                nextchild = treeWalkerWidth(startingNode, child);
                            }

                            // now delete the old child and get the next one.
                            child.getParentNode().removeChild(child);
                        }
                    }
                } else if (stream != null) {
                    /* We are in HTTP streaming mode.
                    So we can put the content of the current node directly into
                    the output stream. */
                    String streamResults = null;
                    if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                        streamResults = child.getNodeValue();
                    } else {
                        if (child.getNodeType() == Node.TEXT_NODE) {
                            String s = child.getNodeValue().trim();
                            if (!"".equals(s)) {
                                streamResults = child.getNodeValue();
                            }
                        }
                    }
                    if (streamResults != null) {
                        try {
                            stream.write(streamResults.getBytes(m_cms.getRequestContext().getEncoding()));
                        } catch (Exception e) {
                            throw new CmsLegacyException(CmsLegacyException.C_UNKNOWN_EXCEPTION, e);
                        }
                    }
                }
                child = nextchild;
            }
        }
    }

    /**
     * Read the datablocks of the given content file and include them
     * into the own Hashtable of datablocks.
     *
     * @param include completely initialized A_CmsXmlObject to be included
     * @throws CmsException if something goes wrong
     */
    public void readIncludeFile(A_CmsXmlContent include) throws CmsException {
        m_includedTemplates.addElement(include);
        m_blocks = concatData(m_blocks, include.getAllData());
    }

    /**
     * Parses the given file and stores it in the internal list of included files and
     * appends the relevant data structures of the new file to its own structures.
     *
     * @param filename file name of the XML file to be included
     * @return file with xml conten
     * @throws CmsException if something goes wrong
     */
    public A_CmsXmlContent readIncludeFile(String filename) throws CmsException {
        A_CmsXmlContent include = null;
        if (OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Including file: " + filename);
        }
        try {
            include = (A_CmsXmlContent) getClass().newInstance();
            include.init(m_cms, filename);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error include file: " + filename, e);
            }
        }
        readIncludeFile(include);
        return include;
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
        registerTag("LINK", A_CmsXmlContent.class, "handleLinkTag", C_REGISTER_MAIN_RUN);
        registerTag("INCLUDE", A_CmsXmlContent.class, "replaceTagByComment", C_REGISTER_MAIN_RUN);
        registerTag("DATA", A_CmsXmlContent.class, "replaceTagByComment", C_REGISTER_MAIN_RUN);
        registerTag(getXmlDocumentTagName());
    }

    /**
     * Registers the given tag to be "known" by the system.
     * So this tag will not be handled by the default method of processNode.
     * Under normal circumstances this feature will only be used for
     * the XML document tag.
     * @param tagname Tag name to register.
     */
    public void registerTag(String tagname) {
        if (!(m_knownTags.contains(tagname.toLowerCase()))) {
            m_knownTags.addElement(tagname.toLowerCase());
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
        switch (runSelector) {
            case C_REGISTER_FIRST_RUN :
                selectedRun = m_firstRunTags;
                break;

            case C_REGISTER_MAIN_RUN :
            default:
                selectedRun = m_mainProcessTags;
                break;
        }
        try {
            selectedRun.put(tagname.toLowerCase(), c.getDeclaredMethod(methodName, C_PARAMTYPES_HANDLING_METHODS));
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Exception in register tag ", e);
            }
        }
        registerTag(tagname);
    }

    /**
     * Remove a datablock from the internal hashtable and
     * from the XML document.<p>
     * @param tag Key of the datablock to delete.
     */
    protected void removeData(String tag) {
        Element e = (Element) m_blocks.get(tag.toLowerCase());
        if (e != null) {
            m_blocks.remove(tag.toLowerCase());
            Element parent = (Element) e.getParentNode();
            if (parent != null) {
                parent.removeChild(e);
            }
        }
    }

    /**
     * Deletes this object from the internal XML file cache.<p>
     */
    public void removeFromFileCache() {
        String currentProject = m_cms.getRequestContext().currentProject().getName();
        m_filecache.remove(currentProject + ":" + m_cms.getRequestContext().addSiteRoot(getAbsoluteFilename()));
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
        String attribute = tag;
        int dotIndex = tag.lastIndexOf(".");
        if (dotIndex != -1) {
            attribute = attribute.substring(dotIndex + 1);
        }
        Element newElement = m_content.createElement(attribute);
        if (data == null || "".equals(data)) {
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
        if (data == null) {
            setData(tag, "");
        } else {
            // Now we can be sure to have a correct Element
            tag = tag.toLowerCase();
            Element newElement = (Element) data.cloneNode(true);
            if (OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
                OpenCms.getLog(this).debug("Putting datablock " + tag + " into internal Hashtable");
            }
            if (!(m_blocks.containsKey(tag))) {
                // This is a brand new datablock. It can be inserted directly.
                //m_blocks.put(tag, newElement);
                insertNewDatablock(tag, newElement);
            } else {
                // datablock existed before, so the childs of the old
                // one can be replaced.
                if (OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
                    OpenCms.getLog(this).debug("Datablock existed before, replacing");
                }
                // Look up the old datablock and remove all its childs.
                Element originalBlock = (Element) (m_blocks.get(tag));
                while (originalBlock.hasChildNodes()) {
                    originalBlock.removeChild(originalBlock.getFirstChild());
                }
                // And now add all childs of the new node
                NodeList newNodes = data.getChildNodes();
                int len = newNodes.getLength();
                for (int i = 0; i < len; i++) {
                    Node newElement2 = newNodes.item(i).cloneNode(true);
                    originalBlock.appendChild(m_parser.importNode(originalBlock.getOwnerDocument(), newElement2));
                }
            }
        }
    }

    /**
     * Creates a datablock element by parsing the data string
     * and stores this block into the datablock-hashtable.
     *
     * @param tag Key for this datablock.
     * @param data String to be put in the datablock.
     * @throws CmsException if something goes wrong
     */
    public void setParsedData(String tag, String data) throws CmsException {

        StringBuffer tempXmlString = new StringBuffer();
        tempXmlString.append("<?xml version=\"1.0\"?>\n");
        tempXmlString.append("<" + getXmlDocumentTagName() + ">");
        tempXmlString.append("<" + tag + ">\n");
        tempXmlString.append("<![CDATA[");
        tempXmlString.append(data);
        tempXmlString.append("]]>");
        tempXmlString.append("</" + tag + ">\n");
        tempXmlString.append("</" + getXmlDocumentTagName() + ">\n");
        StringReader parserReader = new StringReader(tempXmlString.toString());
        Document tempDoc = null;
        try {
            tempDoc = m_parser.parse(parserReader);
        } catch (Exception e) {
            throwException("PARSING ERROR! " + e.toString(), CmsLegacyException.C_XML_PARSING_ERROR);
        }
        Element templateNode = (Element) tempDoc.getDocumentElement().getFirstChild();
        setData(tag, templateNode);
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
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @throws CmsException if something goes wrong
     */
    protected void throwException(String errorMessage) throws CmsException {
        throwException(errorMessage, CmsLegacyException.C_UNKNOWN_EXCEPTION);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a
     * CmsException of the given type.
     * @param errorMessage String with the error message to be printed.
     * @param type Type of the exception to be thrown.
     * @throws CmsLegacyException if something goes wrong
     */
    protected void throwException(String errorMessage, int type) throws CmsLegacyException {
        if (OpenCms.getLog(this).isErrorEnabled()) {
            OpenCms.getLog(this).error(errorMessage);
        }
        throw new CmsLegacyException(errorMessage, type);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @param e Original exception.
     * @throws CmsException if something goes wrong
     */
    protected void throwException(String errorMessage, Exception e) throws CmsException {
        throwException(errorMessage, e, CmsLegacyException.C_UNKNOWN_EXCEPTION);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @param e Original exception.
     * @param type Type of the exception to be thrown.
     * @throws CmsException if something goes wrong
     */
    protected void throwException(String errorMessage, Exception e, int type) throws CmsException {
        if (OpenCms.getLog(this).isErrorEnabled()) {
            OpenCms.getLog(this).error(errorMessage, e);
        }
        if (e instanceof CmsException) {
            throw (CmsException) e;
        } else {
            throw new CmsLegacyException(errorMessage, type, e);
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
     * Help method to walk through the DOM document tree.
     * First it will be looked for children of the given node.
     * If there are no children, the siblings and the siblings of our parents
     * are examined. This will be done by calling treeWalkerWidth.
     * @param root the root node
     * @param n Node representing the actual position in the tree
     * @return next node
     */
    protected Node treeWalker(Node root, Node n) {
        Node nextnode = null;
        if (n.hasChildNodes()) {
            // child has child notes itself
            // process these first in the next loop
            nextnode = n.getFirstChild();
        } else {
            // child has no subchild.
            // so we take the next sibling
            nextnode = treeWalkerWidth(root, n);
        }
        return nextnode;
    }

    /**
     * Help method to walk through the DOM document tree by a
     * width-first-order.
     * @param root the root node
     * @param n Node representing the actual position in the tree
     * @return next node
     */
    protected Node treeWalkerWidth(Node root, Node n) {
        if (n == root) {
            return null;
        }
        Node nextnode = null;
        Node parent = null;
        nextnode = n.getNextSibling();
        parent = n.getParentNode();
        while (nextnode == null && parent != null && parent != root) {

            // child has sibling
            // last chance: we take our parent's sibling
            // (or our grandparent's sibling...)
            nextnode = parent.getNextSibling();
            parent = parent.getParentNode();
        }
        return nextnode;
    }

    /**
     * Writes the XML document back to the OpenCms system.
     * @throws CmsException if something goes wrong
     */
    public void write() throws CmsException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        getXmlText(os);
        byte[] xmlContent = os.toByteArray();

        // Get the CmsFile object to write to
        String filename = getAbsoluteFilename();
        CmsFile file = m_cms.readFile(filename);

        // Set the new content and write the file
        file.setContents(xmlContent);
        m_cms.writeFile(file);
        xmlContent = null;

        // update the internal parsed content cache with the new file data.
        String currentProject = m_cms.getRequestContext().currentProject().getName();
        m_filecache.put(currentProject + ":" + m_cms.getRequestContext().addSiteRoot(filename), m_content.cloneNode(true));
    }

    /**
     * Returns current XML document encoding.
     * @return String encoding of XML document
     */
    public String getEncoding() {
        return m_parser.getOriginalEncoding(m_content);
    }

    /**
     * Sets new encoding for XML document.
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        m_newEncoding = encoding;
    }

}
