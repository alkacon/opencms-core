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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsWorkplaceLinkMode;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.Window;

/**
 * Provides a method to open the workplace.<p>
 *
 * @since 8.0.0
 */
public final class CmsShowWorkplace implements I_CmsHasContextMenuCommand {

    /**
     * Hidden utility class constructor.<p>
     */
    private CmsShowWorkplace() {

        // nothing to do
    }

    /**
     * Returns the context menu command according to
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     *
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new I_CmsContextMenuCommand() {

            public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

                openWorkplace(structureId, false);
            }

            public A_CmsContextMenuItem getItemWidget(
                CmsUUID structureId,
                I_CmsContextMenuHandler handler,
                CmsContextMenuEntryBean bean) {

                return null;
            }

            public boolean hasItemWidget() {

                return false;
            }
        };
    }

    /**
     * Opens the workplace.<p>
     *
     * @param structureId the structure id of the resource for which the workplace should be opened
     * @param classic if true, opens the old workplace, else the new workplace
     */
    public static void openWorkplace(final CmsUUID structureId, final boolean classic) {

        CmsRpcAction<String> callback = new CmsRpcAction<String>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getService().getWorkplaceLink(structureId, CmsWorkplaceLinkMode.auto, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(String result) {

                stop(false);
                Window.Location.assign(result);
            }
        };
        callback.execute();
    }
}
