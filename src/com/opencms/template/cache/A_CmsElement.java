/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/A_CmsElement.java,v $
* Date   : $Date: 2001/05/17 14:10:31 $
* Version: $Revision: 1.8 $
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
package com.opencms.template.cache;

import java.util.*;
import java.io.*;
import com.opencms.boot.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;

/**
 * An instance of A_CmsElement represents an requestable Element in the OpenCms
 * element cache area. It contains all informations to generate the content of this
 * element. It also stores the variants of once generated content to speed up
 * performance.
 *
 * It may point to other depending elements. Theses elements are called to generate
 * their content on generation-time.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 */
public abstract class A_CmsElement implements com.opencms.boot.I_CmsLogChannels {

    /** Set to <code>true</true> for additional debug output */
    private boolean C_DEBUG = false;

    /** The class-name of this element definition. */
    protected String m_className;

    /** The template-name of this element definition. */
    protected String m_templateName;

    /** Cache directives of this element. */
    private CmsCacheDirectives m_cacheDirectives;

    /** Last time this element was generated.(used for CacheDirectives timeout) */
    protected long m_timestamp = 0;

    /** All definitions declared in this element. */
    protected CmsElementDefinitionCollection m_elementDefinitions;

    /** Hashtable for element variant cache */
    private Hashtable m_variants;

    /**
     * Initializer for an element with the given class and template name.
     */
    protected void init(String className, String templateName, CmsCacheDirectives cd) {
        m_className = className;
        m_templateName = templateName;
        m_cacheDirectives = cd;
        m_elementDefinitions = new CmsElementDefinitionCollection();
        m_variants = new Hashtable();
    }

    /**
     * Initializer for building an element with the given element definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param cd Cache directives for this element
     * @param defs Vector with ElementDefinitions for this element.
     */
    protected void init(String className, String templateName, CmsCacheDirectives cd, CmsElementDefinitionCollection defs) {
        m_className = className;
        m_templateName = templateName;
        m_cacheDirectives = cd;
        m_elementDefinitions = defs;
        m_variants = new Hashtable();
    }

    /**
     * Adds a single definition to this element.
     * @param def - the ElementDefinition to add.
     */
    public void addDefinition(CmsElementDefinition def) {
        m_elementDefinitions.add(def);
    }

    /**
     * Adds a single variant to this element.
     * @param def - the ElementVariant to add.
     */
    public void addVariant(Object key, CmsElementVariant variant) {
        if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_ELEMENTCACHE, toString() + " adding variant \"" + key + "\" to cache. ");
        }
        m_variants.put(key, variant);
    }

    /**
     * Clears all variants. Used for TimeCritical elements.
     */
    public void clearVariantCache(){
        m_variants.clear();
        m_timestamp = System.currentTimeMillis();
    }

    /**
     * Get a variant from the vatiant cache
     * @param key Key of the ElementVariant.
     * @return Cached CmsElementVariant object
     */
    public CmsElementVariant getVariant(Object key) {
        CmsElementVariant result = (CmsElementVariant)m_variants.get(key);
        if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            if(result != null) {
                A_OpenCms.log(C_OPENCMS_ELEMENTCACHE, toString() + " getting variant \"" + key + "\" from cache. ");
            } else {
                A_OpenCms.log(C_OPENCMS_ELEMENTCACHE, toString() + " Variant \"" + key + "\" is not in element cache. ");
            }
        }
        return (CmsElementVariant)m_variants.get(key);
    }

    /**
     * Returns a Vector with all ElementDefinitions
     * @returns a Vector with all ElementDefinitions.
     */
    public CmsElementDefinitionCollection getAllDefinitions() {
        return m_elementDefinitions;
    }

    /**
     * Get the element definition for the sub-element with the given name
     * @param name Name of the sub-element that should be looked up
     * @return Element definition of <em>name</em>
     */
    public CmsElementDefinition getElementDefinition(String name) {
        return (CmsElementDefinition)m_elementDefinitions.get(name);
    }

    /** Get cache directives for this element.
     *  @return cache directives.
     */
    public CmsCacheDirectives getCacheDirectives() {
        return m_cacheDirectives;
    }

    /**
     * Abstract method for getting the content of this element.
     * Element classes may have different implementations for getting
     * the contents. Common tasks of all implementations are checking
     * the variant cache and creating the variant if required.
     * @param elementCache Entry point for the element cache
     * @param cms CmsObject for accessing system resources
     * @param elDefs Definitions of this element's subelements
     * @param parameters All parameters of this request
     * @return Byte array with the processed content of this element.
     * @exception CmsException
     */
    public abstract byte[] getContent(CmsElementCache elementCache, CmsObject cms, CmsElementDefinitionCollection efDefs, String elementName, Hashtable parameters) throws CmsException;

    /**
     * Get a template class from the template class manager.
     * @param cms CmsObject for accessing system resources.
     * @param classname Name of the requested class.
     * @exception CmsException if the loaded class is no OpenCms template class
     */
    protected I_CmsTemplate getTemplateClass(CmsObject cms, String classname) throws CmsException {
        Object o = CmsTemplateClassManager.getClassInstance(cms, classname);
        // Check, if the loaded class really is a OpenCms template class.
        if(o instanceof I_CmsTemplate) {
            return (I_CmsTemplate)o;
        } else {
            throw new CmsException(classname + " is no OpenCms template class.", CmsException.C_XML_NO_TEMPLATE_CLASS);
        }
    }

    /**
     * Resolve given variant of this element and get content of all sub elements.
     * @param cms CmsObject for accessing system resources
     * @param variant Variant that should be resolved
     * @param elementCache Entry point for element cache
     * @param elDefs Definitions for all subelements
     * @param elementName Current name of the subelement during resolving
     * @param parameters All parameters of this request
     * @return Byte array with processed element content
     * @exception CmsException if resolving fails.
     */
    public byte[] resolveVariant(CmsObject cms, CmsElementVariant variant, CmsElementCache elementCache, CmsElementDefinitionCollection elDefs, String elementName, Hashtable parameters) throws CmsException {
        boolean resolveDebug = false;
        if(resolveDebug) System.err.println("= Start resolving variant " + variant);
        int len = variant.size();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for(int i=0; i<len; i++) {
                if(resolveDebug) System.err.print("= Part " + i + " is a ");
                Object o = variant.get(i);
                // Decide what to do with the current part.
                // If it's a String or byte array, just print it out.
                // If it's a link to a sub element, get this element and call its getContent() method
                if(o instanceof String) {
                    if(resolveDebug) System.err.println("String");
                    baos.write(((String)o).getBytes());
                } else if(o instanceof byte[]) {
                    if(resolveDebug) System.err.println("byte array");
                    baos.write((byte[])o);
                } else if(o instanceof CmsElementLink) {
                    if(resolveDebug) System.err.println("Link");

                    // we have to resolve the element link right NOW!
                    // Look for the element definition
                    String lookupName = ((CmsElementLink)o).getElementName();
                    if(resolveDebug) System.err.println("= Trying to resolve link \"" + lookupName +"\".");
                    CmsElementDefinition elDef = elDefs.get(lookupName);
                    if(elDef != null) {
                        // We have successfully found an element definition.
                        // Try to get the corresponding element using the element locator
                        A_CmsElement subEl = elementCache.getElementLocator().get(cms, elDef.getDescriptor(), parameters);
                        if(resolveDebug) System.err.println("= Element defintion for \"" + lookupName +"\" says: ");
                        if(resolveDebug) System.err.println("= -> Class    : " + elDef.getClassName());
                        if(resolveDebug) System.err.println("= -> Template : " + elDef.getTemplateName());
                        String errorMessage = "";
                        if(subEl != null) {
                            // An element could be found. Very fine.
                            // So we can go on and call its getContent() method
                            if(resolveDebug) System.err.println("= Element object found for \"" + lookupName +"\". Calling getContent on this object. ");
                            byte[] buffer = null;
                            try {
                                buffer = subEl.getContent(elementCache, cms, elDefs, elementName, parameters);
                            } catch(Exception e) {
                                // An error occured while getting the element's content.
                                // Do some error handling here.
                                subEl = null;
                                buffer = null;
                                if(e instanceof CmsException) {
                                    CmsException ce = (CmsException)e;
                                    if(ce.getType() == ce.C_ACCESS_DENIED) {
                                        // This was an access denied exception.
                                        // If we are streaming, we have to catch it and print an error message
                                        // If we are not streaming, we can throw it again and force an authorization request
                                        if(cms.getRequestContext().isStreaming()) {
                                            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                                A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Access denied in element " + lookupName);
                                                A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Streaming is active, so authentication box cannot be requested.");
                                            }
                                            errorMessage = "Access denied";
                                        } else {
                                            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                                A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Access denied in element " + lookupName);
                                            }
                                            throw ce;
                                        }
                                    } else {
                                        // Any other CmsException. This may be critical
                                        if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                            A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Error in element " + lookupName);
                                            A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + e);
                                        }
                                    }
                                } else {
                                    // Any other Non-CmsException.
                                    if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                        A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Non-CmsException in element " + lookupName);
                                        A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + e);
                                    }
                                }
                            }
                            // If we have some results print them out.
                            if(buffer != null) {
                                baos.write(buffer);
                            }
                        } else {
                            // The subelement object is null, i.e. the element could not be found.
                            // Do nothing but a little bit logging here.
                            if(resolveDebug) System.err.println("= Cannot find Element object for \"" + lookupName +"\". Ignoring this link. ");
                            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                A_OpenCms.log(C_OPENCMS_ELEMENTCACHE, toString() + " Cannot find Element object for \"" + lookupName +"\". Ignoring this link. ");
                            }
                        }

                        // If the element could not be generated properly, print a little error
                        // message instead of the element's results.
                        if(subEl == null) {
                            baos.write(("[" + lookupName + "] ??? ").getBytes());
                            baos.write(errorMessage.getBytes());
                        }
                    } else {
                        // No element definition could be found.
                        // Do some logging only and ignore this element
                        baos.write(("[" + lookupName + "] Element not defined.").getBytes());
                        if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(C_OPENCMS_ELEMENTCACHE, toString() + " No element definition found for \"" + lookupName +"\". Ignoring this link. ");
                        }
                        if(resolveDebug) {
                            System.err.println("= No element definition found for \"" + lookupName +"\". Ignoring this link. ");
                            System.err.println(elDefs.toString());
                        }
                    }
                }
            }
            return baos.toByteArray();
        } catch(IOException e) {
            // Something went wrong while writing to the OutputStream
            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Critical: IOException while writing to OutputStream. ");
            }
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
        }
    }

    /**
     * Get a string representation of this element.
     * @return String representation.
     */
    public String toString() {
        String part1 = getClass().getName();
        part1 = part1.substring(part1.lastIndexOf(".") + 1);

        String part2 = m_className.substring(m_className.lastIndexOf(".") + 1);
        String part3 = m_templateName.substring(m_templateName.lastIndexOf("/") + 1);

        return "[" + part1 + " (" + part2 + "/" + part3 + ")]";
    }
}