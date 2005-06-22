/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/Attic/CmsRootToolHandler.java,v $
 * Date   : $Date: 2005/06/22 10:38:24 $
 * Version: $Revision: 1.2 $
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

package org.opencms.workplace.tools;

import org.opencms.file.CmsObject;

/**
 * Default admin tool handler.<p>
 * 
 * Always enabled and visible.<p>
 * 
 * @author Michael Moossen  
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public class CmsRootToolHandler extends A_CmsToolHandler {
    
    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#setup(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean setup(CmsObject cms, String resourcePath) {

        setName("${key." + Messages.GUI_ADMIN_VIEW_ROOT_NAME_0+ "}");
        setHelpText("${key." + Messages.GUI_ADMIN_VIEW_ROOT_HELP_0+ "}");
        setDisabledHelpText(getHelpText());
        setIconPath("admin/images/deficon.png");
        setSmallIconPath(getIconPath());
        setPath("/");
        setGroup("");
        setPosition(1);   
        setLink(resourcePath);
        return true;
    }
    
    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return true;
    }
    
    
    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        return true;
    }
}
