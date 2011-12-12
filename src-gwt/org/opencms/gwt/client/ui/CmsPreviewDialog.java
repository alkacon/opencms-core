/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Resource preview dialog.<p>
 */
public class CmsPreviewDialog extends CmsPopup {

    /** The dialog width. */
    private static final int DIALOG_WIDTH = 1200;

    /** The dialog preview content width. */
    private static final int DIALOG_PREVIEW_CONTENT_WIDTH = 642;

    /** The dialog height. */
    private static final int DIALOG_HEIGHT = 900;

    /**
     * Constructor.<p>
     * 
     * @param caption the dialog caption 
     * @param width the dialog width
     */
    private CmsPreviewDialog(String caption, int width) {

        super(caption, width);
        setGlassEnabled(true);
        setPositionFixed();
        CmsPushButton closeButton = new CmsPushButton();
        closeButton.setText(Messages.get().key(Messages.GUI_CLOSE_0));
        closeButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                hide();

            }
        });
        addButton(closeButton);
        addDialogClose(null);
    }

    /**
     * Shows the preview for the given resource.<p>
     * 
     * @param structureId the resource structure id
     */
    public static void showPreviewForResource(final CmsUUID structureId) {

        CmsRpcAction<CmsPreviewInfo> previewAction = new CmsRpcAction<CmsPreviewInfo>() {

            @Override
            public void execute() {

                CmsCoreProvider.getVfsService().getPreviewInfo(structureId, CmsCoreProvider.get().getLocale(), this);
                this.start(0, true);
            }

            @Override
            protected void onResponse(CmsPreviewInfo result) {

                stop(false);
                showPreviewForResource(result);
            }
        };
        previewAction.execute();
    }

    /**
     * Shows the preview for the given resource.<p>
     * 
     * @param sitePath the resource site path
     */
    public static void showPreviewForResource(final String sitePath) {

        CmsRpcAction<CmsPreviewInfo> previewAction = new CmsRpcAction<CmsPreviewInfo>() {

            @Override
            public void execute() {

                CmsCoreProvider.getVfsService().getPreviewInfo(sitePath, CmsCoreProvider.get().getLocale(), this);
                this.start(0, true);
            }

            @Override
            protected void onResponse(CmsPreviewInfo result) {

                stop(false);
                showPreviewForResource(result);
            }
        };
        previewAction.execute();
    }

    /**
     * Shows the preview for the given resource.<p>
     * 
     * @param previewInfo the resource preview info
     */
    public static void showPreviewForResource(CmsPreviewInfo previewInfo) {

        if (previewInfo.isNewWindowRequired()) {
            Window.open(previewInfo.getPreviewUrl(), CmsDomUtil.Target.BLANK.getRepresentation(), "");
        } else {

            HTML content = new HTML();
            Style style = content.getElement().getStyle();
            int height = DIALOG_HEIGHT;
            int width = DIALOG_WIDTH;
            if (previewInfo.hasPreviewContent()) {
                content.setHTML(previewInfo.getPreviewContent());
                style.setOverflow(Overflow.AUTO);
                // try to measure dimensions
                width = DIALOG_PREVIEW_CONTENT_WIDTH;
                style.setWidth(width, Unit.PX);
                RootPanel.get().add(content);
                height = content.getOffsetHeight();
            } else {
                content.setHTML("<iframe src=\""
                    + previewInfo.getPreviewUrl()
                    + "\" style=\"height:100%; width:100%;\" />");
            }
            if (previewInfo.hasDimensions()) {
                height = previewInfo.getHeight();
                width = previewInfo.getWidth();
            }
            // check if window is big enough for requested dimensions
            int availableHeight = Window.getClientHeight() - 200;
            if (height > availableHeight) {
                height = availableHeight;
            }
            int availableWidth = Window.getClientWidth() - 50;
            if (width > availableWidth) {
                width = availableWidth;
            }
            style.setHeight(height, Unit.PX);
            style.setWidth(width, Unit.PX);
            String caption = (CmsStringUtil.isNotEmptyOrWhitespaceOnly(previewInfo.getTitle()) ? previewInfo.getTitle()
                + " / " : "")
                + previewInfo.getSitePath();

            CmsPreviewDialog dialog = new CmsPreviewDialog(caption, width + 12);
            dialog.setMainContent(content);
            dialog.center();
        }
    }
}
