/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDefinition.java,v $
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
import com.opencms.file.*;

/**
 * An instance of CmsElementDefinitions represents an "pointer" to other
 * elements. This pointers are stored in CmsElement's. An ElementDfinitions
 * stores information about an element, like the name, className, templateName
 * and parameters for the pointed element.
 *
 * @author: Andreas Schouten
 */
public class CmsElementDefinition {

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

}