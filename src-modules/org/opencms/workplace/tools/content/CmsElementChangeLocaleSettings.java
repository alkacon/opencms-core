/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsElementChangeLocaleSettings.java,v $
 * Date   : $Date: 2005/07/29 15:38:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.content;

/**
 * Settings object that provides the settings to convert page locales from one locale to another.<p>
 * 
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.0.1 
 */
public class CmsElementChangeLocaleSettings {
    
    /** Flag indicating if resources in sub folders should be changed, too. */
    private boolean m_includeSubFolders;
    
    /** The new Locale to use for the page elements. */
    private String m_newLocale;
    
    /** The old Locale of the page elements. */
    private String m_oldLocale;
    
    /** The template for the pages that should be converted. */
    private String m_template;
    
    /** The VFS folder in OpenCms to start the conversion from. */
    private String m_vfsFolder;
    
    /**
     * Default constructor.<p>
     */
    public CmsElementChangeLocaleSettings() {

        super();
    }
    
    /**
     * Returns the new Locale to use for the page elements.<p>
     *
     * @return the new Locale to use for the page elements
     */
    public String getNewLocale() {

        return m_newLocale;
    }
    
    /**
     * Returns the old Locale of the page elements.<p>
     *
     * @return the old Locale of the page elements
     */
    public String getOldLocale() {

        return m_oldLocale;
    }
    
    /**
     * Returns the template for the pages that should be converted.<p>
     *
     * @return the template for the pages that should be converted
     */
    public String getTemplate() {

        return m_template;
    }
    
    /**
     * Returns the VFS folder in OpenCms to start the conversion from.<p>
     *
     * @return the VFS folder in OpenCms to start the conversion from
     */
    public String getVfsFolder() {

        return m_vfsFolder;
    }

    /**
     * Returns the flag indicating if resources in sub folders should be changed, too.<p>
     *
     * @return the flag indicating if resources in sub folders should be changed, too
     */
    public boolean isIncludeSubFolders() {

        return m_includeSubFolders;
    }
    
    /**
     * Sets the flag indicating if resources in sub folders should be changed, too.<p>
     *
     * @param includeSubFolders the flag indicating if resources in sub folders should be changed, too
     */
    public void setIncludeSubFolders(boolean includeSubFolders) {

        m_includeSubFolders = includeSubFolders;
    }
    
    /**
     * Sets the new Locale to use for the page elements.<p>
     *
     * @param newLocale the new Locale to use for the page elements
     */
    public void setNewLocale(String newLocale) {

        m_newLocale = newLocale;
    }
    
    /**
     * Sets the old Locale of the page elements.<p>
     *
     * @param oldLocale the old Locale of the page elements
     */
    public void setOldLocale(String oldLocale) {

        m_oldLocale = oldLocale;
    }
    
    /**
     * Sets the template for the pages that should be converted.<p>
     *
     * @param template the template for the pages that should be converted
     */
    public void setTemplate(String template) {

        m_template = template;
    }
    
    /**
     * Sets the VFS folder in OpenCms to start the conversion from.<p>
     *
     * @param vfsFolder the VFS folder in OpenCms to start the conversion from
     */
    public void setVfsFolder(String vfsFolder) {

       m_vfsFolder = vfsFolder;
    }

}
