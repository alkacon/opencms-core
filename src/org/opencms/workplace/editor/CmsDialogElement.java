/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDialogElement.java,v $
 * Date   : $Date: 2004/04/28 22:34:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editor;

import org.opencms.util.CmsStringSubstitution;

/**
 * Contains the setup information about a single dialog element.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.0
 */
public class CmsDialogElement implements Comparable {

    /** The (system) name of the element */
    private String m_name;

    /** The nice "display" name of the element */
    private String m_niceName;

    /** Indicates if the element is mandantory or not */
    private boolean m_mandantory;

    /** Indicates if the element is declared as template-element or not */
    private boolean m_templateElement;

    /** Indicates if the element is existing on the page or not */
    private boolean m_existing;

    /**
     * Creates a new dialog element.<p>
     * 
     * @param name the (system) name of the element
     * @param niceName the nice "display" name of the element
     * @param mandantory indicates if the element is mandantory
     * @param templateElement indicates if the element is defined as template-element
     * @param existing indicates if the element is existing on the xmlPage or not
     */
    public CmsDialogElement(String name, String niceName, boolean mandantory, boolean templateElement, boolean existing) {

        m_name = name;
        m_niceName = niceName;
        m_mandantory = mandantory;
        m_templateElement = templateElement;
        m_existing = existing;
    }

    /**
     * Returns the mandantory.<p>
     *
     * @return the mandantory
     */
    public boolean isMandantory() {

        return m_mandantory;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the niceName.<p>
     *
     * @return the niceName
     */
    public String getNiceName() {
        if (CmsStringSubstitution.isEmpty(m_niceName)) {
            // if the nice name is empty use the system name for display

            if (isExisting() && !isTemplateElement()) {
                // this element was not defined with the "template-elements" property
                return "* " + getName();
            } else {
                return getName();
            }
        }
        
        return m_niceName;
    }

    /**
     * Returns the templateElement.<p>
     *
     * @return the templateElement
     */
    public boolean isTemplateElement() {

        return m_templateElement;
    }
   
    /**
     * Returns the existing.<p>
     *
     * @return the existing
     */
    public boolean isExisting() {

        return m_existing;
    }
    
    /**
     * Sets the existing.<p>
     *
     * @param existing the existing to set
     */
    public void setExisting(boolean existing) {

        m_existing = existing;
    }    
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (!(o instanceof CmsDialogElement)) {
            return false;
        }
        CmsDialogElement element = (CmsDialogElement)o;
        if (m_name == null) {
            return element.m_name == null;
        } else {
            return m_name.equals(element.m_name);
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        if (m_name == null) {
            return 0;
        } else {
            return m_name.hashCode();
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {

        if (o == null) {
            return 0;
        }
        if (!(o instanceof CmsDialogElement)) {
            return 0;
        }
        CmsDialogElement element = (CmsDialogElement)o;
        if (m_name == null) {
            return (element.m_name == null)?0:-1;
        } else {
            return m_name.compareToIgnoreCase(element.m_name);
        }
    }
}