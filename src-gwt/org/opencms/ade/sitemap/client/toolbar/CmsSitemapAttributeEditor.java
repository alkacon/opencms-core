/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.attributes.CmsAttributesDialog;
import org.opencms.ade.sitemap.shared.CmsSitemapAttributeData;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.I_CmsDisableable;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.contextmenu.A_CmsContextMenuItem;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.util.CmsUUID;

/**
 * Context menu entry for the sitemap attribute editor.
 */
public class CmsSitemapAttributeEditor
implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand, I_CmsDisableable {

    /**
     * Returns the context menu command according to
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     *
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsSitemapAttributeEditor();
    }

    /**
     * Gets the service to use for validating/saving aliases.<p>
     *
     * @return the service used for validating/saving aliases
     */
    protected static I_CmsSitemapServiceAsync getService() {

        return CmsSitemapView.getInstance().getController().getService();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

        CmsRpcAction<CmsSitemapAttributeData> action = new CmsRpcAction<CmsSitemapAttributeData>() {

            @Override
            public void execute() {

                start(0, true);
                CmsUUID rootId = CmsSitemapView.getInstance().getController().getData().getRoot().getId();
                getService().editAttributeData(rootId, this);
            }

            @Override
            protected void onResponse(CmsSitemapAttributeData result) {

                stop(false);
                CmsAttributesDialog dialog = new CmsAttributesDialog(result);
                dialog.centerHorizontally(50);

            }
        };
        action.execute();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#getItemWidget(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public A_CmsContextMenuItem getItemWidget(
        CmsUUID structureId,
        I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#hasItemWidget()
     */
    public boolean hasItemWidget() {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsDisableable#isDisabled()
     */
    public boolean isDisabled() {

        return false;
    }

}
