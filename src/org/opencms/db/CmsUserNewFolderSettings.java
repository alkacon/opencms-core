/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsUserNewFolderSettings.java,v $
 * Date   : $Date: 2006/09/26 15:10:04 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.main.OpenCms;

import java.io.Serializable;

/**
 * Bean for holding user specific settings for the create new folder dialogs.<p> 
 * 
 * Get access to the configuration data by: 
 * <code>OpenCms.getWorkplaceManager().getDefaultUserSettings().getNewResourceSettings().getNewFolderSettings()</code>.<p>
 * 
 * @author Achim Westermann  
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 6.2.3
 */
public class CmsUserNewFolderSettings extends CmsUserNewResourceSettings implements Serializable {

    /** Genereated serial version UID. */
    private static final long serialVersionUID = -4397564777269791908L;

    /** 
     * Boolean flag that controls if the "create index page" checkbox of the 
     * "create new folder" dialog is checked by default. 
     **/
    private Boolean m_createIndexPageChecked;

    /**
     * Default constructor that marks the internal property accessible by 
     * {@link #setEditPropsChecked(Boolean)} to be unconfigured. <p> 
     *
     */
    public CmsUserNewFolderSettings() {

        super();
        m_editPropsChecked = null;
    }

    /**
     * Returns if the "create index page" checkbox of the 
     * "create new folder" dialog is checked by default. 
     * 
     * @return if the "create index page" checkbox of the 
     *      "create new folder" dialog is checked by default 
     */
    public Boolean isCeateIndexPageChecked() {

        return m_createIndexPageChecked;
    }

    /**
     * Sets if the "create index page" checkbox of the 
     * "create new folder" dialog is checked by default.<p>
     * 
     * @param newFolderCreateIndexPage a <code>"boolean String"</code> controls if the "create index page" checkbox of the 
     *      "create new folder" dialog is checked by default. 
     */
    public void setCreateIndexPageChecked(Boolean newFolderCreateIndexPage) {

        m_createIndexPageChecked = newFolderCreateIndexPage;
    }

    /**
     * Overrridden for avoiding mixups.<p> 
     * 
     * @see org.opencms.db.CmsUserNewResourceSettings#getNewFolderSettings()
     */
    public CmsUserNewFolderSettings getNewFolderSettings() {

        return this;
    }

    /**
     * Overridden to implement the 
     * "if this is not set use the default for all new resource dialogs" - logic.<p>
     * 
     * @see org.opencms.db.CmsUserNewResourceSettings#isEditPropsChecked()
     */
    public Boolean isEditPropsChecked() {

        Boolean result = this.m_editPropsChecked;
        if (result == null) {
            if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
                // parent lookup
                result = OpenCms.getWorkplaceManager().getDefaultUserSettings().getNewResourceSettings().isEditPropsChecked();
            } else {
                // default value
                result = EDITPROPSCHECKED_DEFAULT;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.db.CmsUserNewResourceSettings#setNewFolderSettings(org.opencms.db.CmsUserNewFolderSettings)
     */
    public void setNewFolderSettings(CmsUserNewFolderSettings folderSettings) {

        m_editPropsChecked = folderSettings.isEditPropsChecked();
        m_createIndexPageChecked = folderSettings.isCeateIndexPageChecked();
    }
}
