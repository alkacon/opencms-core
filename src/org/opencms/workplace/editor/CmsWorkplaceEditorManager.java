/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsWorkplaceEditorManager.java,v $
 * Date   : $Date: 2004/02/04 15:48:16 $
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

import com.opencms.file.CmsObject;

/**
 * The editor manager collects information about all editors in OpenCms.<p>
 * 
 * This class provides methods and constants to select the right editor according to:
 * <ul>
 * <li>the user preferences</li>
 * <li>the users current browser</li>
 * <li>the resource type</li>
 * <li>the editor rankings</li>
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.1
 */
public final class CmsWorkplaceEditorManager {
    
    /**
     * Creates a new editor manager.<p>
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     */
    public CmsWorkplaceEditorManager(CmsObject cms) {
        // empty constructor
        cms.toString();
    }
    
}
