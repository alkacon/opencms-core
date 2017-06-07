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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.gwt.shared.CmsReturnLinkInfo;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

/**
 * The toolbar button for jumping to the last visited container page.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarGoBackButton extends CmsPushButton {

    /**
     * Constructor.<p>
     *
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller
     */
    public CmsToolbarGoBackButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        setImageClass(I_CmsButton.ButtonData.BACK.getIconClass());
        setTitle(I_CmsButton.ButtonData.BACK.getTitle());
        setButtonStyle(ButtonStyle.FONT_ICON, null);
        setSize(Size.big);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                toolbar.onButtonActivation(CmsToolbarGoBackButton.this);
                CmsToolbarGoBackButton.this.clearHoverState();
                setDown(false);
                setEnabled(false);
                goBack(controller.getData().getReturnCode());
            }
        });
    }

    /**
     * Opens the publish dialog without changes check.<p>
     *
     * @param returnCode the return code previously passed to the sitemap editor
     */
    public static void goBack(final String returnCode) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(returnCode)) {
            CmsRpcAction<CmsReturnLinkInfo> goBackAction = new CmsRpcAction<CmsReturnLinkInfo>() {

                @Override
                public void execute() {

                    start(300, false);
                    CmsCoreProvider.getService().getLinkForReturnCode(returnCode, this);
                }

                @Override
                protected void onResponse(CmsReturnLinkInfo result) {

                    stop(false);
                    if (result.getStatus() == CmsReturnLinkInfo.Status.ok) {
                        Window.Location.assign(result.getLink());
                    } else if (result.getStatus() == CmsReturnLinkInfo.Status.notfound) {
                        CmsMessages msg = org.opencms.ade.sitemap.client.Messages.get();
                        String title = msg.key(
                            org.opencms.ade.sitemap.client.Messages.GUI_RETURN_PAGE_NOT_FOUND_TITLE_0);
                        String content = msg.key(
                            org.opencms.ade.sitemap.client.Messages.GUI_RETURN_PAGE_NOT_FOUND_TEXT_0);
                        CmsAlertDialog alert = new CmsAlertDialog(title, content);
                        alert.center();
                    }
                }
            };

            goBackAction.execute();
        } else {
            CmsSitemapController controller = CmsSitemapView.getInstance().getController();
            CmsClientSitemapEntry root = controller.getData().getRoot();
            String newPath = root.getSitePath();
            controller.leaveEditor(newPath);
        }
    }

}
