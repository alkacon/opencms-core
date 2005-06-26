/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsDefaultToolHandler.java,v $
 * Date   : $Date: 2005/06/26 13:30:31 $
 * Version: $Revision: 1.14 $
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

package org.opencms.workplace.tools;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import java.util.Iterator;
import java.util.List;

/**
 * Default admin tool handler.<p>
 * 
 * Always enabled and visible.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDefaultToolHandler extends A_CmsToolHandler {

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        // at least one sub tool should be enabled
        CmsToolManager toolManager = OpenCms.getWorkplaceManager().getToolManager();
        List subTools = toolManager.getToolsForPath(getPath(), false);
        Iterator itSubTools = subTools.iterator();
        while (itSubTools.hasNext()) {
            String subToolPath = (String)itSubTools.next();
            CmsTool subTool = toolManager.resolveAdminTool(subToolPath);
            if (subTool.getHandler().isEnabled(cms)) {
                return true;
            }
        }
        return !getLink().equals(CmsToolManager.C_VIEW_JSPPAGE_LOCATION);
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        // at least one sub tool should be visible
        CmsToolManager toolManager = OpenCms.getWorkplaceManager().getToolManager();
        List subTools = toolManager.getToolsForPath(getPath(), false);
        Iterator itSubTools = subTools.iterator();
        while (itSubTools.hasNext()) {
            String subToolPath = (String)itSubTools.next();
            CmsTool subTool = toolManager.resolveAdminTool(subToolPath);
            if (subTool.getHandler().isVisible(cms)) {
                return true;
            }
        }
        return false;
    }
}
