/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/A_CmsElement.java,v $
* Date   : $Date: 2001/05/07 16:22:56 $
* Version: $Revision: 1.3 $
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
 * An instance of CmsElement represents an requestable Element in the OpenCms
 * staging-area. It contains all informations to generate the content of this
 * element. It also stores the variants of once generated content to speed up
 * performance.
 *
 * It points to other depending elements. Theses elements are called to generate
 * their content on generation-time.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 */
public abstract class A_CmsElement implements com.opencms.boot.I_CmsLogChannels {

    /** Set to <code>true</true> for additional debug output */
    private boolean C_DEBUG = true;

    /** The class-name of this element definition. */
    protected String m_className;

    /** The template-name of this element definition. */
    protected String m_templateName;

    /** The name of this element. */
    protected String m_elementName;

    /** Cache directives of this element. */
    private CmsCacheDirectives m_cacheDirectives;

    /**
     * A Vector with definitions declared in this element.
     */
    protected Vector m_elementDefinitions;


    /** Hashtable for element variant cache */
    private Hashtable m_variants;

    /**
     * Constructor for an element with the given class and template name.
     */
    protected void init(String className, String templateName, CmsCacheDirectives cd) {
        m_className = className;
        m_templateName = templateName;
        m_cacheDirectives = cd;
        m_elementDefinitions = new Vector();
        m_variants = new Hashtable();
    }

    /**
     * A construcor which creates an element with the given element
     * definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param cd Cache directives for this element
     * @param defs Vector with ElementDefinitions for this element.
     */
    protected void init(String className, String templateName, CmsCacheDirectives cd, Vector defs) {
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
        if(C_DEBUG && CmsBase.isLogging()) {
            CmsBase.log(C_OPENCMS_STAGING, toString() + " adding variant \"" + key + "\" to cache. ");
        }
        m_variants.put(key, variant);
    }

    /**
     * Get a variant from the vatiant cache
     * @param key Key of the ElementVariant.
     * @return Cached CmsElementVariant object
     */
    public CmsElementVariant getVariant(Object key) {
        CmsElementVariant result = (CmsElementVariant)m_variants.get(key);
        if(C_DEBUG && CmsBase.isLogging()) {
            if(result != null) {
                CmsBase.log(C_OPENCMS_STAGING, toString() + " getting variant \"" + key + "\" from cache. ");
            } else {
                CmsBase.log(C_OPENCMS_STAGING, toString() + " Variant \"" + key + "\" is not in element cache. ");
            }
        }
        return (CmsElementVariant)m_variants.get(key);
    }

    /**
     * Returns a Vector with all ElementDefinitions
     * @returns a Vector with all ElementDefinitions.
     */
    public Vector getAllDefinitions() {
        return m_elementDefinitions;
    }

    /**
     * Get the element definition for the sub-element with the given name
     * @param name Name of the sub-element that should be looked up
     * @return Element definition of <em>name</em>
     */
    public CmsElementDefinition getElementDefinition(String name) {
        CmsElementDefinition result = null;
        int numDefs = m_elementDefinitions.size();
        for(int i = 0; i < numDefs; i++) {
            CmsElementDefinition loop = (CmsElementDefinition)m_elementDefinitions.elementAt(i);
            String elName = loop.getName();
            if(elName.equals(name)) {
                result = loop;
            }
        }
        return result;
    }

    public CmsCacheDirectives getCacheDirectives() {
        return m_cacheDirectives;
    }
    public abstract byte[] getContent(CmsStaging staging, CmsObject cms, Hashtable parameters) throws CmsException;

    protected I_CmsTemplate getTemplateClass(CmsObject cms, String classname) throws CmsException {
        Object o = CmsTemplateClassManager.getClassInstance(cms, classname);

        // Check, if the loaded class really is a OpenCms template class.
        I_CmsTemplate cmsTemplate = (I_CmsTemplate)o;
        return cmsTemplate;
    }


    public byte[] resolveVariant(CmsObject cms, CmsElementVariant variant, CmsStaging staging, Hashtable parameters) {
        if(C_DEBUG) System.err.println("= Start resolving variant " + variant);
        int len = variant.size();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for(int i=0; i<len; i++) {
                if(C_DEBUG) System.err.print("= Part " + i + " is a ");
                Object o = variant.get(i);
                if(o instanceof String) {
                    if(C_DEBUG) System.err.println("String");
                    baos.write(((String)o).getBytes());
                } else if(o instanceof byte[]) {
                    if(C_DEBUG) System.err.println("byte array");
                    baos.write((byte[])o);
                } else if(o instanceof CmsElementLink) {
                    if(C_DEBUG) System.err.println("Link");
                    // we have to resolve the element link right NOW!
                    String lookupName = ((CmsElementLink)o).getElementName();
                    if(C_DEBUG) System.err.println("= Trying to resolve link \"" + lookupName +"\".");
                    CmsElementDefinition elDef = getElementDefinition(lookupName);
                    if(elDef != null) {
                        A_CmsElement subEl = staging.getElementLocator().get(cms, elDef.getDescriptor(), parameters);
                        if(C_DEBUG) System.err.println("= Element defintion for \"" + lookupName +"\" says: ");
                        if(C_DEBUG) System.err.println("= -> Class    : " + elDef.getClassName());
                        if(C_DEBUG) System.err.println("= -> Template : " + elDef.getTemplateName());
                        if(subEl != null) {
                            if(C_DEBUG) System.err.println("= Element object found for \"" + lookupName +"\". Calling getContent on this object. ");
                            byte[] buffer = subEl.getContent(staging, cms, parameters);
                            if(buffer != null) {
                                baos.write(buffer);
                            }
                        } else {
                            baos.write(("[" + lookupName + "] ???").getBytes());
                            if(C_DEBUG) System.err.println("= Cannot find Element object for \"" + lookupName +"\". Ignoring this link. ");
                        }
                    } else {
                        if(C_DEBUG) System.err.println("= No element definition found for \"" + lookupName +"\". Ignoring this link. ");
                    }
                }
            }
        } catch(Exception e) {
            System.err.println("Error while resolving element variant");
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public String toString() {
        String part1 = getClass().getName();
        part1 = part1.substring(part1.lastIndexOf(".") + 1);

        String part2 = m_className.substring(m_className.lastIndexOf(".") + 1);
        String part3 = m_templateName.substring(m_templateName.lastIndexOf("/") + 1);

        return "[" + part1 + " (" + part2 + "/" + part3 + ")]";
    }

}