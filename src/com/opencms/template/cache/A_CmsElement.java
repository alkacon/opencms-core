/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/A_CmsElement.java,v $
* Date   : $Date: 2004/06/28 07:44:02 $
* Version: $Revision: 1.60 $
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

package com.opencms.template.cache;

import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsCacheDirectives;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsTemplateClassManager;
import com.opencms.template.I_CmsTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
public abstract class A_CmsElement {

    /** The class-name of this element definition. */
    protected String m_className;

    /** The template-name of this element definition. */
    protected String m_templateName;

    /** Cache directives of this element. */
    protected A_CmsCacheDirectives m_cacheDirectives;

    /** Last time this element was generated.(used for CacheDirectives timeout) */
    protected long m_timestamp = 0;

    /** All definitions declared in this element. */
    protected CmsElementDefinitionCollection m_elementDefinitions;

    /** LruCache for element variant cache */
    private CmsLruCache m_variants;

    /** indicates if this element may have a variant that has dependencies
     *  if such a element is deletet from elementcache the extern dependencies
     *  hashtable must be updated.
     */
    protected boolean m_hasDepVariants = false;

    /**
     * Initializer for an element with the given class and template name.
     */
    protected void init(String className, String templateName, A_CmsCacheDirectives cd, int variantCachesize) {
        m_className = className;
        m_templateName = templateName;
        m_cacheDirectives = cd;
        m_elementDefinitions = new CmsElementDefinitionCollection();
        m_variants = new CmsLruCache(variantCachesize);
    }

    /**
     * Initializer for building an element with the given element definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param cd Cache directives for this element
     * @param defs Vector with ElementDefinitions for this element.
     * @param variantCachesize The size of the variant cache.
     */
    protected void init(String className, String templateName, A_CmsCacheDirectives cd, CmsElementDefinitionCollection defs, int variantCachesize) {
        m_className = className;
        m_templateName = templateName;
        m_cacheDirectives = cd;
        m_elementDefinitions = defs;
        m_variants = new CmsLruCache(variantCachesize);
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
     * @return a CmsElementVariant of dependencies that must be deleted from extern store for this element
     */
    public Vector addVariant(Object key, CmsElementVariant variant) {
        if(OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Adding variant \"" + key + "\" to xml template cache");
        }
        if(key != null){
            CmsElementVariant old = (CmsElementVariant)m_variants.get(key);
            if ((old != null) && (old.size() == 0)){
                variant.addDependencies(old.getDependencies());
                variant.mergeNextTimeout(old.getNextTimeout());
            }
            return m_variants.put(key, variant);
        }
        return null;
    }

    public void removeVariant(Object key){
        if(OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info(toString() + " removing variant \"" + key + "\" from xml template cache");
        }
        if(key != null){
            m_variants.remove(key);
        }
    }

    /**
     * checks the read access.
     * @param cms The cms Object for reading groups.
     * @throws CmsException if no read access.
     */
    public void checkReadAccess(CmsObject cms) throws CmsException{
        return;
    }

    /**
     * Clears all variants. Used for TimeCritical elements.
     */
    public void clearVariantCache(){
        m_variants.clearCache();
        m_timestamp = System.currentTimeMillis();
    }

    /**
     *
     */
    public Vector getAllVariantKeys(){
        return m_variants.getAllKeys();
    }

    /**
     * Get a variant from the vatiant cache
     * @param key Key of the ElementVariant.
     * @return Cached CmsElementVariant object
     */
    public CmsElementVariant getVariant(Object key) {
        if (key == null){
            return null;
        }
        CmsElementVariant result = (CmsElementVariant)m_variants.get(key);
        if(result != null && result.size() == 0){
            result = null;
        }
        if(OpenCms.getLog(this).isInfoEnabled()) {
            if(result != null) {
                OpenCms.getLog(this).info(toString() + " getting variant \"" + key + "\" from cache. ");
            } else {
                OpenCms.getLog(this).info(toString() + " Variant \"" + key + "\" is not in element cache. ");
            }
        }
        return result;
    }

    /**
     * says if the extern dependenciescache has to be updated when this element
     * is deleted.
     */
    public boolean hasDependenciesVariants(){
        return m_hasDepVariants;
    }

    /**
     * indicates this element critical for delete.
     */
    public void thisElementHasDepVariants(){
        m_hasDepVariants = true;
    }

    /**
     * Returns a Vector with all ElementDefinitions
     * @return a Vector with all ElementDefinitions.
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
        return m_elementDefinitions.get(name);
    }

    /** Get cache directives for this element.
     *  @return cache directives.
     */
    public A_CmsCacheDirectives getCacheDirectives() {
        return m_cacheDirectives;
    }

    /**
     * checks the proxy public and the proxy private cache settings
     *  of this element and all subelements.
     *  @param cms the cms object.
     *  @param proxySettings The CacheDirectives to merge the own CacheDriectives with.
     *  @param parameters A Hashtable with the parameters.
     */
    public void checkProxySettings(CmsObject cms, CmsCacheDirectives proxySettings, Hashtable parameters) throws CmsException{
        // first our own cachedirectives are they set or not?
        if (!(m_cacheDirectives.userSetProxyPrivate() && m_cacheDirectives.userSetProxyPublic())){
            // we have to find out manually
            boolean proxyPublic = false;
            boolean proxyPrivate = false;
            boolean export = false;
            if(m_templateName == null){
                // no template given set everything to true
                proxyPublic = true;
                proxyPrivate = true;
                export = true;
            }else{
                try{
                    if (m_cacheDirectives.isInternalCacheable() && (!m_cacheDirectives.isUserPartOfKey())){
                        CmsResource templ = cms.readResource(m_templateName);
                        int accessflags = templ.getFlags() ;
                        if(!((accessflags & I_CmsConstants.C_ACCESS_INTERNAL_READ) > 0)){
                            // internal flag not set
                            proxyPrivate = true;
                            if((!m_cacheDirectives.isParameterPartOfKey()) && (!m_cacheDirectives.isTimeCritical())){
                                export = true;
                            }
                        }
                    }

                }catch(Exception e){
                    // do nothing, set everything to false and log the error
                    if(OpenCms.getLog(this).isWarnEnabled()) {
                        OpenCms.getLog(this).warn(toString()
                                    + " could not find out if the element is proxy cacheable", e);
                    }
                }
            }
            if(!m_cacheDirectives.userSetProxyPrivate()){
                ((CmsCacheDirectives)m_cacheDirectives).setProxyPrivateCacheable(proxyPrivate);
            }
            if(!m_cacheDirectives.userSetProxyPublic()){
                ((CmsCacheDirectives)m_cacheDirectives).setProxyPublicCacheable(proxyPublic);
            }
            if(!m_cacheDirectives.userSetExport()){
                ((CmsCacheDirectives)m_cacheDirectives).setExport(export);
            }
        }
        proxySettings.merge(m_cacheDirectives);
        // now for the subelements
        Enumeration elementNames = m_elementDefinitions.getAllElementNames();
        while(elementNames.hasMoreElements()){
            String name = (String)elementNames.nextElement();
            CmsElementDefinition currentDef = m_elementDefinitions.get(name);
            A_CmsElement currentEle = CmsXmlTemplateLoader.getElementCache().getElementLocator().get(
                                    cms, new CmsElementDescriptor(currentDef.getClassName(),
                                                            currentDef.getTemplateName()), parameters);
            currentEle.checkProxySettings(cms, proxySettings, parameters);
        }
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
     * @param methodparameter used only for methode elements. Contains the parameter for the methode.
     * @return Byte array with the processed content of this element.
     * @throws CmsException
     */
    public abstract byte[] getContent(CmsElementCache elementCache, CmsObject cms, CmsElementDefinitionCollection efDefs, String elementName, Hashtable parameters, String methodParameter) throws CmsException;

    /**
     * Get a template class from the template class manager.
     * @param cms CmsObject for accessing system resources.
     * @param classname Name of the requested class.
     * @throws CmsException if the loaded class is no OpenCms template class
     */
    protected I_CmsTemplate getTemplateClass(CmsObject cms, String classname) throws CmsException {
        Object o = CmsTemplateClassManager.getClassInstance(classname);
        // Check, if the loaded class really is a OpenCms template class.
        if(o instanceof I_CmsTemplate) {
            return (I_CmsTemplate)o;
        } else {
            throw new CmsException(classname + " is no OpenCms template class.", CmsException.C_XML_NO_TEMPLATE_CLASS);
        }
    }

    /**
     * Resolve given variant of this element and get content of all sub elements.<p>
     * 
     * @param cms CmsObject for accessing system resources
     * @param variant Variant that should be resolved
     * @param elementCache Entry point for element cache
     * @param elDefs Definitions for all subelements
     * @param parameters All parameters of this request
     * @return Byte array with processed element content
     * @throws CmsException if resolving fails.
     */
    public byte[] resolveVariant(CmsObject cms, CmsElementVariant variant, CmsElementCache elementCache, CmsElementDefinitionCollection elDefs, Hashtable parameters) throws CmsException {
        boolean resolveDebug = false;
        if(resolveDebug) System.err.println("= Start resolving variant " + variant);
        int len = variant.size();
        // Try to get the corresponding element using the element locator
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for(int i=0; i<len; i++) {
                // put the name of the element into the params
                if(resolveDebug) System.err.print("= Part " + i + " is a ");
                Object o = variant.get(i);
                // Decide what to do with the current part.
                // If it's a String or byte array, just print it out.
                // If it's a link to a sub element, get this element and call its getContent() method
                if(o instanceof String) {
                    if(resolveDebug) System.err.println("String");
                    baos.write(((String)o).getBytes(cms.getRequestContext().getEncoding()));
                } else if(o instanceof byte[]) {
                    if(resolveDebug) System.err.println("byte array");
                    baos.write((byte[])o);
                } else if(o instanceof CmsElementLink) {
                    if(resolveDebug) System.err.println("Element Link");

                    // we have to resolve the element link right NOW!
                    // Look for the element definition
                    String lookupName = ((CmsElementLink)o).getElementName();
                    if(resolveDebug) System.err.println("= Trying to resolve link \"" + lookupName +"\".");
                    CmsElementDefinition elDef = elDefs.get(lookupName);
                    if(elDef != null) {
                        // We have successfully found an element definition.
                        // first add the parameter from the elementdefinition to the parameters
                        elDef.joinParameters(parameters);
                        // put the name of the element into the params
                        parameters.put("_ELEMENT_", elDef.getName());
                        if(elDef.getTemplateName() != null){
                            parameters.put(elDef.getName() + "._TEMPLATE_", elDef.getTemplateName());
                        }
                        parameters.put(elDef.getName() + "._CLASS_", elDef.getClassName());
                        if(elDef.getTemplateSelector()!= null) {
                            parameters.put(elDef.getName() + "._TEMPLATESELECTOR_", elDef.getTemplateSelector());
                        } else {
                            parameters.put(elDef.getName() + "._TEMPLATESELECTOR_", "default");
                        }
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
                                buffer = subEl.getContent(elementCache, cms, elDefs, lookupName, parameters, null);
                            } catch(Exception e) {
                                // An error occured while getting the element's content.
                                // Do some error handling here.
                                if(I_CmsConstants.C_USER_TYPE_SYSTEMUSER == cms.getRequestContext().currentUser().getType()
                                    && !OpenCms.getDefaultUsers().getUserGuest().equals(cms.getRequestContext().currentUser().getName())){
                                    // a systemuser gets the real message.(except guests)
                                    errorMessage = e.toString();
                                }
                                subEl = null;
                                buffer = null;
                                if(e instanceof CmsException) {
                                    CmsException ce = (CmsException)e;
                                    if(ce instanceof CmsSecurityException) {
                                        if(OpenCms.getLog(this).isErrorEnabled()) {
                                            OpenCms.getLog(this).error("Access denied in element " + lookupName, ce);
                                        }
                                        throw ce;
                                    } else {
                                        // Any other CmsException. This may be critical
                                        if(OpenCms.getLog(this).isErrorEnabled()) {
                                            OpenCms.getLog(this).error("Error in element " + lookupName, e);
                                        }
                                    }
                                } else {
                                    // Any other Non-CmsException.
                                    if(OpenCms.getLog(this).isErrorEnabled()) {
                                        OpenCms.getLog(this).error("Non-CmsException in element " + lookupName, e);
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
                            if(OpenCms.getLog(this).isWarnEnabled()) {
                                OpenCms.getLog(this).warn("Cannot find Element object for \"" + lookupName +"\", ignoring this link");
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
                        if(OpenCms.getLog(this).isWarnEnabled()) {
                            OpenCms.getLog(this).warn("No element definition found for \"" + lookupName +"\", ignoring this link");
                        }
                        if(resolveDebug) {
                            System.err.println("= No element definition found for \"" + lookupName +"\". Ignoring this link. ");
                            System.err.println(elDefs.toString());
                        }
                    }
                }else if(o instanceof CmsMethodLink){
                    if(resolveDebug) System.err.println("Method Link");
                    // get the methodelement
                    String methodName = ((CmsMethodLink)o).getMethodeName();
                    String methodParameter = ((CmsMethodLink)o).getMethodParameter();
                    A_CmsElement metEle =elementCache.getElementLocator().get(cms,
                                        new CmsElementDescriptor(m_className + "." + methodName, "METHOD"),
                                        parameters);
                    byte[] buffer = null;
                    if(metEle != null){
                        try{
                            buffer = metEle.getContent(elementCache, cms, elDefs, null,parameters, methodParameter);
                        }catch(Exception e){
                            if(e instanceof CmsException) {
                                if(OpenCms.getLog(this).isErrorEnabled()) {
                                    OpenCms.getLog(this).error("Error in method " + methodName, e);
                                }
                            }else{
                                // Any other Non-CmsException.
                                if(OpenCms.getLog(this).isErrorEnabled()) {
                                    OpenCms.getLog(this).error("Non-CmsException in method " + methodName, e);
                                }
                            }
                        }
                    }else{
                        // The subelement object is null, i.e. the element could not be found.
                        // Do nothing but a little bit logging here.
                        if(resolveDebug) System.err.println("= Cannot find methodElemtn object for \"" + methodName +"\". Ignoring this link. ");
                        if(OpenCms.getLog(this).isWarnEnabled()) {
                            OpenCms.getLog(this).warn("Cannot find method Element object for \"" + methodName +"\", ignoring this link");
                        }
                    }
                    // If we have some results print them out.
                    if(buffer != null) {
                        baos.write(buffer);
                    }
                }
            }
            return baos.toByteArray();
        } catch(IOException e) {
            // Something went wrong while writing to the OutputStream
            if(OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("IOException while writing to XMl template OutputStream", e);
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
        String part3 = "";
        if(m_templateName != null){
            part3 = m_templateName.substring(m_templateName.lastIndexOf("/") + 1);
        }

        return "[" + part1 + " (" + part2 + "/" + part3 + ")]";
    }
}