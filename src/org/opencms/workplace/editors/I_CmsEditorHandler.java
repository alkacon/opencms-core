/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/I_CmsEditorHandler.java,v $
 * Date   : $Date: 2005/06/26 12:23:30 $
 * Version: $Revision: 1.7 $
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

package org.opencms.workplace.editors;

import org.opencms.jsp.CmsJspActionElement;

/**
 * Provides a method for selecting an individual file editor.<p>
 * 
 * You can define the class of your own editor handler in the OpenCms XML configuration files.
 * changing the &lt;class&gt; subnode of the system node &lt;editorhandler&gt; to another value. 
 * The class you enter must implement this interface to obtain the URI of the displayed editor.<p>  
 * 
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsEditorHandler {

    /**
     * Returns the editor URI which will be used for the selected resource in the OpenCms VFS to the editor selector class.<p>
     * 
     * @param resource the selected resource
     * @param jsp the CmsJspActionElement
     * @return the absolute path to the editor that will be displayed
     */
    String getEditorUri(String resource, CmsJspActionElement jsp);

}
