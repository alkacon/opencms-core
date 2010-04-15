/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapEditor.java,v $
 * Date   : $Date: 2010/04/15 10:08:06 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.ade.sitemap.client.ui.CmsPage;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.ade.sitemap.client.util.CmsSitemapProvider;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsHeader;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarButton;
import org.opencms.gwt.client.ui.CmsToolbarPlaceHolder;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapEditor extends A_CmsEntryPoint {

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();

        I_CmsLayoutBundle.INSTANCE.rootCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.pageCss().ensureInjected();
        I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().ensureInjected();

        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.rootCss().root());

        CmsToolbar toolBar = new CmsToolbar();
        toolBar.addLeft(new CmsToolbarButton(CmsToolbarButton.ButtonData.SAVE));
        toolBar.addLeft(new CmsToolbarButton(
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSubsitemap(),
            Messages.get().key(Messages.GUI_TOOLBAR_SUBSITEMAP_0)));
        toolBar.addLeft(new CmsToolbarButton(CmsToolbarButton.ButtonData.ADD));
        toolBar.addLeft(new CmsToolbarButton(CmsToolbarButton.ButtonData.NEW));
        toolBar.addLeft(new CmsToolbarButton(CmsToolbarButton.ButtonData.CLIPBOARD));
        final CmsToolbarButton publishButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.PUBLISH);
        publishButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                // triggering a mouse-out event, as it won't be fired once the dialog has opened (the dialog will capture all events)
                CmsDomUtil.ensureMouseOut(publishButton.getElement());
                CmsPublishDialog.showPublishDialog(new CloseHandler<PopupPanel>() {

                    /**
                     * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
                     */
                    public void onClose(CloseEvent<PopupPanel> event2) {

                        publishButton.setDown(false);
                    }
                });
            }
        });
        toolBar.addRight(publishButton);
        toolBar.addRight(new CmsToolbarButton(CmsToolbarButton.ButtonData.RESET));
        RootPanel.get().add(toolBar);

        RootPanel.get().add(new CmsToolbarPlaceHolder());
        CmsHeader title = new CmsHeader(
            Messages.get().key(Messages.GUI_EDITOR_TITLE_1, ""),
            CmsSitemapProvider.get().getUri());
        title.addStyleName(I_CmsLayoutBundle.INSTANCE.rootCss().pageCenter());
        RootPanel.get().add(title);

        final CmsPage page = new CmsPage();
        RootPanel.get().add(page);

        final Label loadingLabel = new Label(Messages.get().key(Messages.GUI_LOADING_0));
        page.add(loadingLabel);

        CmsRpcAction<CmsClientSitemapEntry> getRootAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500);
                CmsSitemapProvider.getService().getRoot(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry root) {

                page.remove(loadingLabel);

                CmsLazyTree<CmsSitemapTreeItem> tree = new CmsLazyTree<CmsSitemapTreeItem>(
                    new A_CmsLazyOpenHandler<CmsSitemapTreeItem>() {

                        /**
                         * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
                         */
                        public void load(final CmsSitemapTreeItem target) {

                            CmsRpcAction<CmsClientSitemapEntry[]> getChildrenAction = new CmsRpcAction<CmsClientSitemapEntry[]>() {

                                /**
                                * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                                */
                                @Override
                                public void execute() {

                                    // Make the call to the sitemap service
                                    start(1000);
                                    CmsSitemapProvider.getService().getChildren(target.getEntry().getSitePath(), this);
                                }

                                /**
                                * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                                */
                                @Override
                                public void onResponse(CmsClientSitemapEntry[] result) {

                                    target.clearChildren();
                                    for (CmsClientSitemapEntry entry : result) {
                                        target.addChild(entry);
                                    }
                                    target.onFinishLoading();
                                    stop();
                                }
                            };
                            getChildrenAction.execute();
                        }

                    });
                tree.addItem(new CmsSitemapTreeItem(root));
                page.add(tree);
                stop();
            }

        };
        getRootAction.execute();
    }
}
