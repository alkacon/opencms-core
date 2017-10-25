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

package org.opencms.ui.actions;

import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.publishqueue.CmsPublishQueueConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * The publish queue dialog action.<p>
 */
public class CmsPublishQueueDialogAction extends A_CmsToolbarAction implements I_CmsADEAction {

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        context.navigateTo(CmsPublishQueueConfiguration.APP_ID);
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getCommandClassName()
     */
    public String getCommandClassName() {

        return "org.opencms.gwt.client.ui.contextmenu.CmsEmbeddedAction";
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return CmsPublishQueueConfiguration.APP_ID;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getJspPath()
     */
    public String getJspPath() {

        return null;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getParams()
     */
    public Map<String, String> getParams() {

        Map<String, String> params = new HashMap<String, String>();
        params.put(CmsGwtConstants.ACTION_PARAM_DIALOG_ID, this.getClass().getName());
        return params;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#isAdeSupported()
     */
    public boolean isAdeSupported() {

        return true;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitleKey()
     */
    @Override
    protected String getTitleKey() {

        return org.opencms.ui.Messages.GUI_EXPLORER_CONTEXT_PUBLISH_QUEUE_0;
    }
}
