/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsStaticExportProperties.java,v $
 * Date   : $Date: 2003/08/11 18:30:52 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;


/**
 * Provides a data structure to access the static 
 * export properties read from <code>opencms.properties</code>.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 */
public class CmsStaticExportProperties {

    /** Indicates if <code>true</code> is the default value for the property "export" */
    private boolean m_exportPropertyDefault;

    /** Indicates if links in the static export should be relative */
    private boolean m_exportRelativeLinks;

    /** Prefix to use for exported files */
    private String m_rfsPrefix;

    /** Indicates if the static export is enabled or diabled */
    private boolean m_staticExportEnabled;

    /** The path to where the static export will be written */
    private String m_staticExportPath;
    
    /** Prefix to use for internal OpenCms files */
    private String m_vfsPrefix;

    /**
     * Creates a new static export property object.<p>
     */
    public CmsStaticExportProperties() {
        m_exportRelativeLinks = false;
        m_staticExportEnabled = false;
        m_exportPropertyDefault = true;
    }

    /**
     * Returns the export path for the static export.<p>
     * 
     * @return the export path for the static export
     */
    public String getExportPath() {
        return m_staticExportPath;
    }

    /**
     * Returns true if the default value for the resource property "export" is true.<p>
     * 
     * @return true if the default value for the resource property "export" is true
     */
    public boolean getExportPropertyDefault() {
        return m_exportPropertyDefault;
    }
    
    /**
     * Returns the prefix for exported links in the "real" file system.<p>
     * 
     * @return the prefix for exported links in the "real" file system
     */ 
    public String getRfsPrefix() {
        return m_rfsPrefix;
    }

    /**
     * Returns the prefix for internal links in the vfs.<p>
     * 
     * @return the prefix for internal links in the vfs
     */
    public String getVfsPrefix() {
        return m_vfsPrefix;
    }

    /**
     * Returns true if the static export is enabled.<p>
     * 
     * @return true if the static export is enabled
     */
    public boolean isStaticExportEnabled() {
        return m_staticExportEnabled;
    }

    /**
     * Returns true if the links in the static export should be relative.<p>
     * 
     * @return true if the links in the static export should be relative
     */
    public boolean relativLinksInExport() {
        return m_exportRelativeLinks;
    }
    
    /**
     * Sets the path where the static export is written.<p>
     * 
     * @param path the path where the static export is written
     */
    public void setExportPath(String path) {
        m_staticExportPath = path;
        if (! m_staticExportPath.endsWith("/")) {
            m_staticExportPath += "/";
        }
    }
    
    /**
     * Sets the default for the "export" resource property, 
     * possible values are "true", "false" or "dynamic".<p>
     *  
     * @param value the default for the "export" resource property
     */
    public void setExportPropertyDefault(boolean value) {
        m_exportPropertyDefault = value;
    }
    
    /**
     * Controls if links in exported files are relative or absolute.<p>
     * 
     * @param value if true, links in exported files are relative
     */
    public void setExportRelativeLinks(boolean value) {
        m_exportRelativeLinks = value;
    }

    /**
     * Sets the prefix for exported links in the "real" file system.<p>
     * 
     * @param exportPrefix the prefix for exported links in the "real" file system
     */
    public void setRfsPrefix(String rfsPrefix) {
        m_rfsPrefix = rfsPrefix;
    }
    
    /**
     * Controls if the static export is enabled or not.<p>
     * 
     * @param value if true, the static export is enabled
     */
    public void setStaticExportEnabled(boolean value) {
        m_staticExportEnabled = value;
    }

    /**
     * Sets the prefix for internal links in the vfs.<p>
     * 
     * @param internPrefix the prefix for internal links in the vfs
     */
    public void setVfsPrefix(String vfsPrefix) {
        m_vfsPrefix = vfsPrefix;
    }
}