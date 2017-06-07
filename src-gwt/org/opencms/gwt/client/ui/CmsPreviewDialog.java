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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Resource preview dialog.<p>
 */
public final class CmsPreviewDialog extends CmsPopup {

    /**
     * Preview provider interface.<p>
     */
    public interface I_PreviewInfoProvider {

        /**
         * Loads the preview information for the given locale.<p>
         *
         * @param locale the locale for which to load the preview
         * @param resultCallback the callback to call with the result
         */
        void loadPreviewForLocale(String locale, AsyncCallback<CmsPreviewInfo> resultCallback);
    }

    /** The dialog height. */
    private static final int DIALOG_HEIGHT = 900;

    /** The dialog preview content width. */
    private static final int DIALOG_PREVIEW_CONTENT_WIDTH = 642;

    /** The dialog width. */
    private static final int DIALOG_WIDTH = 1200;

    /** The select-box width. */
    private static final int SELECTBOX_WIDTH = 120;

    /** The truncation prefix. */
    private static final String TRUNCATION_PREFIX = "PREVIEW_DIALOG";

    /** The locale select box. */
    private CmsSelectBox m_localeSelect;

    /** The preview info provider. */
    private I_PreviewInfoProvider m_previewInfoProvider;

    /** The site path of the preview resource. */
    private String m_sitePath;

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
        catchNotifications();
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
     * Creates a new preview dialog instance.<p>
     *
     * @param previewInfo the preview information
     *
     * @return the new preview dialog instance
     */
    public static CmsPreviewDialog createPreviewDialog(CmsPreviewInfo previewInfo) {

        String caption = generateCaption(previewInfo);
        CmsPreviewDialog dialog = new CmsPreviewDialog(caption, DIALOG_WIDTH + 12);
        dialog.initContent(previewInfo);
        dialog.initLocales(previewInfo);
        return dialog;
    }

    /**
     * Shows the preview for the given resource.<p>
     *
     * @param previewInfo the resource preview info
     */
    public static void showPreviewForResource(CmsPreviewInfo previewInfo) {

        if (previewInfo.isNewWindowRequired()) {
            CmsDomUtil.openWindow(previewInfo.getPreviewUrl(), CmsDomUtil.Target.BLANK.getRepresentation(), "");
        } else {

            CmsPreviewDialog dialog = createPreviewDialog(previewInfo);
            dialog.center();
        }
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
                start(0, true);
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
                start(0, true);
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
     * Generates the dialog caption.<p>
     *
     * @param previewInfo the preview info
     *
     * @return the caption
     */
    private static String generateCaption(CmsPreviewInfo previewInfo) {

        return (CmsStringUtil.isNotEmptyOrWhitespaceOnly(previewInfo.getTitle()) ? previewInfo.getTitle() + " / " : "")
            + previewInfo.getSitePath();
    }

    /**
     * Sets the preview info provider.<p>
     *
     * @param provider the preview info provider instance
     */
    public void setPreviewInfoProvider(I_PreviewInfoProvider provider) {

        m_previewInfoProvider = provider;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    protected String getSitePath() {

        return m_sitePath;
    }

    /**
     * Loads preview for another locale.<p>
     *
     * @param locale the locale to load
     */
    protected void loadOtherLocale(final String locale) {

        CmsRpcAction<CmsPreviewInfo> previewAction = new CmsRpcAction<CmsPreviewInfo>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void execute() {

                if (m_previewInfoProvider != null) {
                    m_previewInfoProvider.loadPreviewForLocale(locale, this);
                } else {
                    CmsCoreProvider.getVfsService().getPreviewInfo(getSitePath(), locale, this);
                }
                start(0, true);
            }

            @Override
            protected void onResponse(CmsPreviewInfo result) {

                stop(false);
                updatePreviewContent(result);
            }
        };
        previewAction.execute();
    }

    /**
     * Updates the preview content of the dialog.<p>
     *
     * @param previewInfo the preview info
     */
    protected void updatePreviewContent(CmsPreviewInfo previewInfo) {

        setCaption(generateCaption(previewInfo));
        initContent(previewInfo);
        initLocales(previewInfo);
        center();
    }

    /**
     * Initializes the preview content.<p>
     *
     * @param previewInfo the preview info
     */
    private void initContent(CmsPreviewInfo previewInfo) {

        setSitePath(previewInfo.getSitePath());
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
            content.setHTML(
                "<iframe src=\"" + previewInfo.getPreviewUrl() + "\" style=\"height:100%; width:100%;\" />");
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
        setWidth(width + 12);
        setMainContent(content);
    }

    /**
     * Initializes the locale selector if needed.<p>
     *
     * @param previewInfo the preview info
     */
    private void initLocales(CmsPreviewInfo previewInfo) {

        if (m_localeSelect != null) {
            removeButton(m_localeSelect);
            m_localeSelect = null;
        }
        if (previewInfo.hasAdditionalLocales()) {
            m_localeSelect = new CmsSelectBox(previewInfo.getLocales());
            m_localeSelect.setFormValueAsString(previewInfo.getLocale());
            m_localeSelect.addValueChangeHandler(new ValueChangeHandler<String>() {

                public void onValueChange(ValueChangeEvent<String> event) {

                    loadOtherLocale(event.getValue());
                }
            });
            Style style = m_localeSelect.getElement().getStyle();
            style.setWidth(SELECTBOX_WIDTH, Unit.PX);
            style.setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
            style.setMargin(0, Unit.PX);
            m_localeSelect.truncate(TRUNCATION_PREFIX, SELECTBOX_WIDTH - 20);
            addButton(m_localeSelect);

        }
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    private void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }
}
