/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsEditorBase.java,v $
 * Date   : $Date: 2011/03/23 14:51:43 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsUserSettings;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;

import javax.servlet.http.HttpSession;

/**
 * Base class for all editors that turns of time warp deletion inherited from 
 * <code>{@link org.opencms.workplace.CmsWorkplace}</code>.<p> 
 *
 * @author  Achim Westermann
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsEditorBase extends CmsDialog {

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditorBase(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initTimeWarp(org.opencms.db.CmsUserSettings, javax.servlet.http.HttpSession)
     */
    @Override
    protected void initTimeWarp(CmsUserSettings settings, HttpSession session) {

        // overridden to avoid deletion of the configured time warp: 
        // this is triggered by editors and in auto time warping a direct edit 
        // must not delete a potential auto warped request time 
    }
}