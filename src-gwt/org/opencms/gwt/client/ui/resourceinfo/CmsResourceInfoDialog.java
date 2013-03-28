/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.resourceinfo;

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog for displaying resource information.<p>
 */
public class CmsResourceInfoDialog extends CmsPopup {

    /**
     * Creates the dialog for the given resource information.<p>
     * 
     * @param statusBean the resource information to bean 
     */
    public CmsResourceInfoDialog(final CmsResourceStatusBean statusBean) {

        super();
        setModal(true);
        setGlassEnabled(true);
        addDialogClose(null);
        setWidth(520);
        int height = 400;
        setHeight(height);
        removePadding();

        CmsMessages messages = Messages.get();
        CmsScrollPanel panel = GWT.create(CmsScrollPanel.class);
        panel.getElement().getStyle().setProperty("maxHeight", height + "px");
        CmsResourceInfoView infoView = new CmsResourceInfoView(statusBean);
        panel.add(infoView);
        CmsTabbedPanel<Widget> tabPanel = new CmsTabbedPanel<Widget>();
        tabPanel.add(panel, messages.key(Messages.GUI_RESOURCE_INFO_TAB_ATTRIBUTES_0));

        final CmsResourceUsageView usage = new CmsResourceUsageView(statusBean);
        tabPanel.add(usage, messages.key(Messages.GUI_RESOURCE_INFO_TAB_USAGE_0));
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                if (1 == event.getSelectedItem().intValue()) {
                    usage.onSelect();
                }

            }
        });
        setMainContent(tabPanel);
    }

    /**
     * Loads the resource information for a resource and displays it in a dialog.<p>   
     * 
     * @param structureId the structure id of the resource for which the resource info should be loaded 
     */
    public static void load(final CmsUUID structureId) {

        CmsRpcAction<CmsResourceStatusBean> action = new CmsRpcAction<CmsResourceStatusBean>() {

            @Override
            public void execute() {

                start(200, false);
                CmsCoreProvider.getVfsService().getResourceStatus(structureId, CmsCoreProvider.get().getLocale(), this);
            }

            @Override
            protected void onResponse(CmsResourceStatusBean result) {

                stop(false);
                CmsResourceInfoDialog dialog = new CmsResourceInfoDialog(result);
                dialog.centerHorizontally(150);
            }
        };
        action.execute();
    }
}
