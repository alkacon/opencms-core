/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/A_CmsElement.java,v $
* Date   : $Date: 2001/05/03 16:00:41 $
* Version: $Revision: 1.1 $
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
public abstract class A_CmsElement {

    /** The class-name of this element definition. */
    protected String m_className;

    /** The template-name of this element definition. */
    protected String m_templateName;

    /** The name of this element. */
    protected String m_elementName;

    /**
     * A Vector with definitions declared in this element.
     */
    protected Vector m_elementDefinitions;


    /** Hashtable for element variant cache */
    protected Hashtable m_variants;

    /**
     * Constructor for an element with the given class and template name.
     */
    protected void init(String className, String templateName, String elementName) {
        m_className = className;
        m_templateName = templateName;
        m_elementName = elementName;
        m_elementDefinitions = new Vector();
        m_variants = new Hashtable();
    }

    /**
     * A construcor which creates an element with the given element
     * definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param defs Vector with ElementDefinitions for this element.
     */
    protected void init(String className, String templateName, String elementName, Vector defs) {
        m_className = className;
        m_templateName = templateName;
        m_elementName = elementName;
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
        System.err.println("??? " + toString() + " adding variant " + variant + " with key: " + key);
        m_variants.put(key, variant);
    }

    /**
     * Returns a Vector with all ElementDefinitions
     * @returns a Vector with all ElementDefinitions.
     */
    public Vector getAllDefinitions() {
        return m_elementDefinitions;
    }

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

    public abstract byte[] getContent(CmsStaging staging, CmsObject cms, Hashtable parameters) throws CmsException;

    protected I_CmsTemplate getTemplateClass(CmsObject cms, String classname) throws CmsException {
        Object o = CmsTemplateClassManager.getClassInstance(cms, classname);

        // Check, if the loaded class really is a OpenCms template class.
        I_CmsTemplate cmsTemplate = (I_CmsTemplate)o;
        return cmsTemplate;
    }

    public String toString() {
        String part1 = getClass().getName();
        part1 = part1.substring(part1.lastIndexOf(".") + 1);

        String part2 = m_className.substring(m_className.lastIndexOf(".") + 1);
        String part3 = m_templateName.substring(m_templateName.lastIndexOf("/") + 1);

        return "[" + part1 + " (" + part2 + "/" + part3 + ")]";
    }

}