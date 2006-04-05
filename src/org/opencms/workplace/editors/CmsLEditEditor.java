/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/Attic/CmsLEditEditor.java,v $
 * Date   : $Date: 2005/06/23 11:11:54 $
 * Version: $Revision: 1.6 $
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
 * Creates the output for editing a resource.<p> 
 * 
 * This class extends the simple editor class and uses the same methods.
 * Be careful when changing anything in the CmsSimpleEditor class.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/editors/ledit/editor.jsp</li>
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLEditEditor extends CmsSimpleEditor {

    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS. */
    public static final String EDITOR_TYPE = "ledit";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsLEditEditor(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public final String getEditorResourceUri() {

        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";
    }

}
