/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsDialogElement.java,v $
 * Date   : $Date: 2005/06/22 10:38:25 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors;

import org.opencms.util.CmsStringUtil;

/**
 * Contains the setup information about a single dialog element.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.6 $
 * 
 * @since 5.3.0
 */
public class CmsDialogElement implements Comparable {

    /** Indicates if the element is existing on the page or not. */
    private boolean m_existing;

    /** Indicates if the element is mandantory or not. */
    private boolean m_mandantory;

    /** The (system) name of the element. */
    private String m_name;

    /** The nice "display" name of the element. */
    private String m_niceName;

    /** Indicates if the element is declared as template-element or not. */
    private boolean m_templateElement;

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
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsDialogElement) {
            CmsDialogElement element = (CmsDialogElement)obj;
            if (m_name == null) {
                return (element.m_name == null) ? 0 : -1;
            } else {
                return m_name.compareToIgnoreCase(element.m_name);
            }
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CmsDialogElement)) {
            return false;
        }
        CmsDialogElement other = (CmsDialogElement)obj;
        if (m_name == null) {
            return other.m_name == null;
        } else {
            if (other.m_name == null) {
                return false;
            }
            String name1 = m_name;
            String name2 = other.m_name;
            if (name1.endsWith("[0]")) {
                name1 = name1.substring(0, name1.length() - 3);
            }
            if (name2.endsWith("[0]")) {
                name2 = name2.substring(0, name2.length() - 3);
            }
            return name1.equals(name2);
        }
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

        if (CmsStringUtil.isEmpty(m_niceName)) {
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
     * Returns the existing.<p>
     *
     * @return the existing
     */
    public boolean isExisting() {

        return m_existing;
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
     * Returns true if the element is defined by the template,
     * false if the element is just contained in the xml page code.<p>
     *
     * @return true if the element is defined by the template
     */
    public boolean isTemplateElement() {

        return m_templateElement;
    }

    /**
     * Sets the existing.<p>
     *
     * @param existing the existing to set
     */
    public void setExisting(boolean existing) {

        m_existing = existing;
    }
}