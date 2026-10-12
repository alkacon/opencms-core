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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.shared.CmsQuotaProjection;
import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Reusable advisory storage budget warning; hidden below the server's warning threshold.<p>
 */
public class CmsQuotaWarning extends Composite {

    /**
     * UI binder.<p>
     */
    interface I_CmsQuotaWarningUiBinder extends UiBinder<Widget, CmsQuotaWarning> {}

    /**
     * Local styles.<p>
     */
    interface I_Style extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the exceeded state style
         */
        String exceeded();
    }

    /** UI binder instance. */
    private static final I_CmsQuotaWarningUiBinder UI_BINDER = GWT.create(I_CmsQuotaWarningUiBinder.class);

    /** Existing message widget, with a warning icon and plain text. */
    @UiField
    CmsMessageWidget m_message;

    /** Bar background. */
    @UiField
    FlowPanel m_bar;

    /** Used fraction of the bar. */
    @UiField
    FlowPanel m_fill;

    /** Local styles. */
    @UiField
    I_Style style;

    /**
     * Creates an initially hidden warning.<p>
     */
    public CmsQuotaWarning() {

        initWidget(UI_BINDER.createAndBindUi(this));
        getElement().setAttribute("role", "status");
        getElement().setAttribute("aria-live", "polite");
        m_bar.getElement().setAttribute("role", "progressbar");
        m_bar.getElement().setAttribute("aria-label", Messages.get().key(Messages.GUI_QUOTA_LABEL_0));
        m_bar.getElement().setAttribute("aria-valuemin", "0");
        m_bar.getElement().setAttribute("aria-valuemax", "100");
        setVisible(false);
    }

    /**
     * Formats utilization as a whole percentage, truncating the fractional part.<p>
     *
     * @param percentage the utilization
     * @return utilization without decimal places
     */
    private static String formatPercentage(double percentage) {

        return NumberFormat.getFormat("#,##0").format(Math.floor(percentage));
    }

    /**
     * Uses the same decimal units as the tenant UI: whole B/KB and up to one decimal for MB/GB.<p>
     *
     * @param bytes the remaining bytes
     * @return the localized size
     */
    private static String formatSize(long bytes) {

        if (bytes >= 1000000000L) {
            return NumberFormat.getFormat("#,##0.#").format(bytes / 1000000000.0) + " GB";
        } else if (bytes >= 1000000L) {
            return NumberFormat.getFormat("#,##0.#").format(bytes / 1000000.0) + " MB";
        } else if (bytes >= 1000L) {
            return NumberFormat.getFormat("#,##0").format(bytes / 1000.0) + " KB";
        } else {
            return NumberFormat.getFormat("#,##0").format(bytes) + " B";
        }
    }

    /**
     * Displays the gallery-specific warning for an exhausted storage budget.<p>
     *
     * @param status the current budget snapshot
     */
    public void setGalleryStatus(CmsQuotaStatus status) {

        if ((status == null) || (status.getState() != State.EXCEEDED)) {
            setVisible(false);
            return;
        }
        showStatus(status, Messages.get().key(Messages.GUI_QUOTA_GALLERY_EXCEEDED_0));
    }

    /**
     * Displays only orange/red states. Unknown and unlimited budgets remain hidden.<p>
     *
     * @param status the server-calculated snapshot, or null if unavailable
     */
    public void setStatus(CmsQuotaStatus status) {

        showStatus(status, null);
    }

    /**
     * Shows the selected upload's effect, distinguishing existing usage from the projection.<p>
     *
     * @param current the stored usage
     * @param selectedBytes the bytes selected for upload
     * @param selectedFiles the number of selected files
     */
    public void setUpload(CmsQuotaStatus current, long selectedBytes, int selectedFiles) {

        CmsQuotaProjection projection = new CmsQuotaProjection(current, selectedBytes);
        if (projection.isAlreadyFull()) {
            showStatus(
                current,
                Messages.get().key(
                    Messages.GUI_QUOTA_UPLOAD_ALREADY_FULL_1,
                    formatPercentage(current.getPercentage())));
        } else if (selectedFiles == 0) {
            setStatus(current);
        } else {
            CmsQuotaStatus projected = projection.getProjectedStatus();
            if ((projected == null) || (projected.getQuotaBytes() <= 0)) {
                setVisible(false);
                return;
            }
            String percentage = formatPercentage(projected.getPercentage());
            String message;
            if (projection.isUploadBlocked()) {
                message = Messages.get().key(
                    selectedFiles == 1 ? Messages.GUI_QUOTA_UPLOAD_SINGLE_2 : Messages.GUI_QUOTA_UPLOAD_MULTIPLE_2,
                    formatSize(Math.max(0, current.getQuotaBytes() - current.getUsedBytes())),
                    percentage);
            } else if (projected.getState() == State.EXCEEDED) {
                message = Messages.get().key(Messages.GUI_QUOTA_UPLOAD_FULL_0);
            } else {
                message = Messages.get().key(
                    Messages.GUI_QUOTA_UPLOAD_WARNING_2,
                    formatSize(Math.max(0, projected.getQuotaBytes() - projected.getUsedBytes())),
                    percentage);
            }
            showStatus(projected, message);
        }
    }

    /**
     * Renders a warning using an optional message for the current dialog.<p>
     *
     * @param status the usage to visualize
     * @param uploadMessage the message to display, or null for the default usage message
     */
    private void showStatus(CmsQuotaStatus status, String uploadMessage) {

        if ((status == null)
            || ((status.getState() != State.WARNING) && (status.getState() != State.EXCEEDED))
            || (status.getQuotaBytes() <= 0)
            || (status.getUsedBytes() < 0)) {
            setVisible(false);
            return;
        }
        boolean exceeded = status.getState() == State.EXCEEDED;
        double percentage = status.getPercentage();
        String percentageText = formatPercentage(percentage);
        String message = exceeded
        ? Messages.get().key(Messages.GUI_QUOTA_EXCEEDED_1, percentageText)
        : Messages.get().key(
            Messages.GUI_QUOTA_WARNING_2,
            formatSize(Math.max(0, status.getQuotaBytes() - status.getUsedBytes())),
            percentageText);
        if (uploadMessage != null) {
            message = uploadMessage;
        }
        setStyleName(style.exceeded(), exceeded);
        m_message.setMessageText(message);
        m_message.setIcon(
            FontOpenCms.WARNING,
            exceeded
            ? I_CmsConstantsBundle.INSTANCE.css().notificationErrorBorder()
            : I_CmsConstantsBundle.INSTANCE.css().colorWarning());
        double filled = Math.max(0, Math.min(100, percentage));
        m_fill.getElement().getStyle().setWidth(filled, Unit.PCT);
        m_bar.getElement().setAttribute("aria-valuenow", String.valueOf(filled));
        m_bar.getElement().setAttribute("aria-valuetext", message);
        setVisible(true);
    }
}
