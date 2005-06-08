/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/CmsModuleDependency.java,v $
 * Date   : $Date: 2005/06/08 10:46:48 $
 * Version: $Revision: 1.4 $
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

package org.opencms.module;

/**
 * Describes an OpenCms module dependency.<p>
 * 
 * Module dependencies are checked if a module is imported or deleted.
 * If a module A requires certain resources (like Java classes) 
 * from another module B, a should be made dependend on B.<p>
 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3.6
 */
public class CmsModuleDependency implements Comparable {

    /** The hash code of the module dependency. */
    private int m_hashCode;

    /** The name of the module dependency. */
    private String m_name;

    /** The (minimum) version of the module dependency. */
    private CmsModuleVersion m_version;

    /**
     * Generates a new, empty module dependency.<p>
     * 
     */
    public CmsModuleDependency() {

        super();
        m_name = new String();
        m_version = new CmsModuleVersion("0");

        // pre - calculate the hash code
        m_hashCode = m_name.concat(m_version.toString()).hashCode();
    }

    /**
     * Generates a new module dependency.<p>
     * 
     * @param moduleName the name of the module dependency
     * @param minVersion the minimum version of the dependency
     */
    public CmsModuleDependency(String moduleName, CmsModuleVersion minVersion) {

        super();
        m_name = moduleName;
        m_version = minVersion;

        // pre - calculate the hash code
        m_hashCode = m_name.concat(m_version.toString()).hashCode();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == null) {
            return 0;
        }

        if (!(obj instanceof CmsModuleDependency)) {
            return 0;
        }

        CmsModuleDependency other = (CmsModuleDependency)obj;

        if (!m_name.equals(other.m_name)) {
            // not same name means no dependency
            return 0;
        }

        // same name: result depends on version numbers
        return m_version.compareTo(other.m_version);
    }

    /**
     * Checks if this module depedency depends on another given module dependency.<p>
     * 
     * @param other the other dependency to check against
     * @return true if this module depedency depends on the given module dependency
     */
    public boolean dependesOn(CmsModuleDependency other) {

        if (!m_name.equals(other.m_name)) {
            // not same name means no dependency
            return false;
        }

        // same name: result depends on version numbers
        return (m_version.compareTo(other.m_version) <= 0);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CmsModuleDependency)) {
            return false;
        }

        CmsModuleDependency other = (CmsModuleDependency)obj;

        return m_name.equals(other.m_name) && m_version.equals(other.m_version);
    }

    /**
     * Returns the name of the module dependency.<p>
     *
     * @return the name of the module dependency
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the minimum version of the dependency.<p>
     *
     * @return the minimum version of the dependency
     */
    public CmsModuleVersion getVersion() {

        return m_version;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_hashCode;
    }

    /** Sets the name of a module dependency.<p>
     * 
     * @param value the name of a module dependency
     */
    public void setName(String value) {

        m_name = value;
    }

    /** Sets the version of a module dependency.<p>
     * 
     * @param value the version of a module dependency
     */
    public void setVersion(CmsModuleVersion value) {

        m_version = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return "[" + getClass().getName() + ", name: " + m_name + ", version: " + m_version + "]";
    }
}