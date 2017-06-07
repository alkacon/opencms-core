/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
 * This tool handler disables the tool if in online project.<p>
 *
 * @since 6.0.0
 */
public class CmsOfflineToolHandler extends CmsDefaultToolHandler {

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#getDisabledHelpText()
     */
    @Override
    public String getDisabledHelpText() {

        if (super.getDisabledHelpText().equals(DEFAULT_DISABLED_HELPTEXT)) {
            return "${key." + Messages.GUI_TOOLS_DISABLED_ONLINE_HELP_0 + "}";
        }
        return super.getDisabledHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    @Override
    public boolean isEnabled(CmsObject cms) {

        return !cms.getRequestContext().getCurrentProject().isOnlineProject();
    }
}
