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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.seo.CmsSeoOptionsDialog;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The context menu command to open the SEO dialog from the container page editor.<p>
 */
public class CmsOpenSeoDialog implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /**
     * Returns the context menu command according to 
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     * 
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsOpenSeoDialog();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler contextMenuHandler,
        final CmsContextMenuEntryBean bean) {

        if (contextMenuHandler.ensureLockOnResource(structureId)) {

            CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

                @Override
                public void execute() {

                    start(0, true);
                    CmsCoreProvider.getVfsService().loadPropertyData(structureId, this);
                }

                @Override
                protected void onResponse(final CmsPropertiesBean propertyData) {

                    stop(false);
                    CmsRpcAction<CmsListInfoBean> infoAction = new CmsRpcAction<CmsListInfoBean>() {

                        @Override
                        public void execute() {

                            start(200, true);
                            CmsCoreProvider.getVfsService().getPageInfo(structureId, this);
                        }

                        @Override
                        protected void onResponse(final CmsListInfoBean listInfoBean) {

                            stop(false);
                            CmsSeoOptionsDialog.loadAliases(structureId, new AsyncCallback<List<CmsAliasBean>>() {

                                public void onFailure(Throwable caught) {

                                    // do nothing
                                }

                                public void onSuccess(final List<CmsAliasBean> aliases) {

                                    CmsSimplePropertyEditorHandler handler = new CmsSimplePropertyEditorHandler(
                                        contextMenuHandler);
                                    handler.setPropertiesBean(propertyData);
                                    CmsSeoOptionsDialog dialog = new CmsSeoOptionsDialog(
                                        structureId,
                                        listInfoBean,
                                        aliases,
                                        propertyData.getPropertyDefinitions(),
                                        handler);
                                    dialog.centerHorizontally(50);
                                    dialog.catchNotifications();
                                    dialog.center();
                                }
                            });
                        }
                    };
                    infoAction.execute();

                }
            };
            action.execute();
        }
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

}
