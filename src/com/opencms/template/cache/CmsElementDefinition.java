/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDefinition.java,v $
* Date   : $Date: 2005/02/18 14:23:16 $
* Version: $Revision: 1.10 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An instance of CmsElementDefinitions represents an "pointer" to other
 * elements. This pointers are stored in CmsElement's. An ElementDfinitions
 * stores information about an element, like the name, className, templateName
 * and parameters for the pointed element.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsElementDefinition implements Cloneable {

    /**
     * The name of this element.
     */
    private String m_name;

    /**
     * The class-name of this element definition.
     */
    private String m_className;

    /**
     * The template-name of this element definition.
     */
    private String m_templateName;

    /**
     * The template-selector of this element definition.
     */
    private String m_templateSelector;

    /**
     * The parameters of this element.
     */
    private Hashtable m_elements;

    /**
     * The constructor with name, classname and templateName. This fits for some
     * default - needs.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param templateName the name of the template for this element-definition.
     */
    public CmsElementDefinition(String name, String className,
        String templateName) {
        m_name = name;
        m_className = className;
        m_templateName = templateName;
    }

    /**
     * The constructor without any parameters. This fits for some needs.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param templateName the name of the template.
     * @param templateSelector the name of the template selector.
     */
    public CmsElementDefinition(String name, String className,
        String templateName, String templateSelector) {
        this(name, className, templateName);
        m_templateSelector = templateSelector;
    }

    /**
     * The complete constructor.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param templateName the name of the template.
     * @param templateSelector the name of the template selector.
     * @param elements a hashtable with parameters.
     */
    public CmsElementDefinition(String name, String className,
        String templateName, String templateSelector, Hashtable elements) {
        this(name, className, templateName, templateSelector);
        m_elements = elements;
    }

    /**
     * Join constructor.
     * @param primary Original object 1 with primary rights
     * @param secondary Original object 2 with primary rights
     */
    public CmsElementDefinition(CmsElementDefinition primary, CmsElementDefinition secondary) {

        m_elements = new Hashtable();

        if(primary != null) {
            m_name = primary.m_name;
            m_className = primary.m_className;
            m_templateName = primary.m_templateName;
            m_templateSelector = primary.m_templateSelector;
        }

        if(secondary != null) {
            if(m_name == null) {
                m_name = secondary.m_name;
            }

            if(m_className == null) {
                m_className = secondary.m_className;
            }

            if(m_templateName == null) {
                m_templateName = secondary.m_templateName;
            }

            if(m_templateSelector == null) {
                m_templateSelector = secondary.m_templateSelector;
            }

            m_elements.putAll(secondary.m_elements);
        }

        // Join parameters
        if(primary != null) {
            m_elements.putAll(primary.m_elements);
        }
    }

    /**
     * Get an element descriptor for looking up the
     * corresponding element of this definition using the element locator
     * @return Element descriptor for this definition
     */
    public CmsElementDescriptor getDescriptor() {
        return new CmsElementDescriptor(m_className, m_templateName);
    }

    /**
     * Get the name of the element defined.
     * @return Name of the element.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Get the templateSelector of the element defined.
     * @return templateSelector of the element.
     */
    public String getTemplateSelector() {
        return m_templateSelector;
    }

    /**
     * Get the class name for the element defined.
     * @return Class name for the element.
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Get the template name for the element defined.
     * @return Template name for the element.
     */
    public String getTemplateName() {
        return m_templateName;
    }

    /**
     * This method adds the parameter from the elementDefinition to the
     * requestParameters.
     *
     * @param The hashtable with the requestparameters.
     */
    public void joinParameters(Hashtable parameter){
        Enumeration enu = m_elements.keys();
        while(enu.hasMoreElements()){
            String key = (String)enu.nextElement();
            String value = (String)m_elements.get(key);
            key = m_name + "." + key;
            parameter.put(key, value);
        }
    }

    /**
     * Get a string representation of this definition.
     * @return String representation.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("DEF: " + m_name + " ");

        String part1 = m_className==null?"":m_className.substring(m_className.lastIndexOf(".") + 1);
        String part2 = m_templateName==null?"":m_templateName.substring(m_templateName.lastIndexOf("/") + 1);
        String part3 = m_templateSelector==null?"":m_templateSelector.substring(m_templateSelector.lastIndexOf("/") + 1);

        result.append(part1 + "/" + part2 + "/" + part3 + " (");

        if(m_elements != null) {
            Enumeration params = m_elements.keys();
            while(params.hasMoreElements()) {
                String name = (String)params.nextElement();
                String value = (String)m_elements.get(name);
                result.append(name + "=" + value);
                if(params.hasMoreElements()) result.append(", ");
            }
            result.append(")");
        }
        return result.toString();
    }
}