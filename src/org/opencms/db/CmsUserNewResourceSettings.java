/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsUserNewResourceSettings.java,v $
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

import org.opencms.workplace.explorer.CmsNewResource;

import java.io.Serializable;

/**
 * Base Bean for holding user specific settings for the create new resource dialogs.<p>
 * 
 * The inheritance tree of this abstract base bean is exactly as in the dialog 
 * implementations in package <code>org.opencms.workplace.explorer</code>. This class is 
 * the "configuration counterpart" of {@link CmsNewResource}.<p> 
 * 
 * 
 * 
 * @author Achim Westermann  
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 6.2.3
 */
public class CmsUserNewResourceSettings implements Serializable {

    /** Default setting for edit properties of new resource checkedbox state. */
    public static Boolean EDITPROPSCHECKED_DEFAULT = Boolean.TRUE;

    /** Generated <code>serial version UID</code>. */
    private static final long serialVersionUID = -6623229633227716230L;

    /** 
     * Boolean flag that controls if the "edit properties of the new resource" checkbox of the 
     * "create new resource" dialog is checked by default. Defaults to true. 
     **/
    protected Boolean m_editPropsChecked;

    /** The new resource settings for folders. */
    private CmsUserNewFolderSettings m_newFolderSettings;

    /**
     * Default constructor.<p>
     *
     */
    public CmsUserNewResourceSettings() {

        m_editPropsChecked = EDITPROPSCHECKED_DEFAULT;
    }

    /**
     * Returns the new resource settings for folders.<p> 
     * 
     * @return the new resource settings for folders.
     */

    public CmsUserNewFolderSettings getNewFolderSettings() {

        return m_newFolderSettings;
    }

    /**
     * Returns if the "edit properties of the new resource" checkbox of the 
     * "create new resource" dialog is checked by default.<p> 
     * 
     * @return if the "edit properties of the new resource" checkbox of the 
     *      "create new resource" dialog is checked by default 
     */
    public Boolean isEditPropsChecked() {

        return m_editPropsChecked;
    }

    /**
     * Sets whether the edit properties of the new resource" checkbox of the 
     * "create new resource" dialog is checked by default.<p>
     * 
     * @param newResourceEditPropsChecked controls if "edit properties of the new resource" checkbox of the 
     *      "create new resource" dialog is checked by default 
     */
    public void setEditPropsChecked(Boolean newResourceEditPropsChecked) {

        m_editPropsChecked = newResourceEditPropsChecked;
    }

    /**
     * Sets the new resource settings for folders.<p>
     * 
     * @param folderSettings the new resource settings for folders.
     */
    public void setNewFolderSettings(CmsUserNewFolderSettings folderSettings) {

        m_newFolderSettings = folderSettings;
    }

}
