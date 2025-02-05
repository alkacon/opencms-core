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

package org.opencms.ade.contenteditor.client;

import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsValidationDetailsWidget;
import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The confirm save dialog, shown when there are validation errors or warnings.<p>
 */
public class CmsConfirmSaveDialog extends CmsPopup {

    /**
     * Constructor.<p>
     *
     * @param issues the validation issues
     * @param isWarning flag, indicating if the issues are warnings (or errors)
     * @param hideLocale flag, indicating if the issues should be presented without locale.
     * @param okCallback callback for confirming the save action.
     */
    public CmsConfirmSaveDialog(
        Map<String, List<String>> issues,
        boolean isWarning,
        boolean hideLocale,
        final I_CmsSimpleCallback<?> okCallback) {

        super(Messages.get().key(Messages.GUI_DIALOG_VALIDATION_TITLE_0));
        FlowPanel main = new FlowPanel();
        CmsValidationDetailsWidget widget = new CmsValidationDetailsWidget();
        widget.setWidth("100%");
        widget.setMessageHtml(
            Messages.get().key(
                isWarning
                ? Messages.GUI_DIALOG_VALIDATION_WARNING_MESSAGE_0
                : Messages.GUI_DIALOG_VALIDATION_ERROR_MESSAGE_0));
        String issuesHtml = createIssuesHtml(issues, hideLocale);
        widget.setIssuesHtml(issuesHtml);
        main.add(widget);
        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setText(
            Messages.get().key(
                isWarning
                ? Messages.GUI_DIALOG_VALIDATION_WARNING_BUTTON_CANCEL_0
                : Messages.GUI_DIALOG_VALIDATION_ERROR_BUTTON_CLOSE_0));
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                hide();
            }
        });
        cancelButton.setUseMinWidth(true);
        setMainContent(main);
        addButton(cancelButton);
        if (isWarning) {
            CmsPushButton saveButton = new CmsPushButton();
            saveButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
            saveButton.setUseMinWidth(true);
            saveButton.setText(Messages.get().key(Messages.GUI_DIALOG_VALIDATION_WARNING_BUTTON_SAVE_0));
            saveButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    hide();
                    okCallback.execute(null);
                }
            });
            addButton(saveButton);
        } else {
            widget.setIcon(FontOpenCms.ERROR, I_CmsConstantsBundle.INSTANCE.css().colorError());
        }
        setGlassEnabled(true);
    }

    /**
     * Create the issues HTML to add to the validation details widget.
     * @param issues the issues to display.
     * @param hideLocale flag, indicating if the issues should be presented without locale.
     * @return the issues HTML to add to the validation details widget.
     */
    private String createIssuesHtml(Map<String, List<String>> issues, boolean hideLocale) {

        String result = "";
        if (hideLocale && (issues.size() == 1)) {
            List<String> pps = issues.values().iterator().next();
            for (String p : pps) {
                result += "<li>" + p + "</li>";
            }
        } else {
            for (Map.Entry<String, List<String>> e : issues.entrySet()) {
                result += "<li><em>" + e.getKey() + ":</em><ul>";
                for (String p : e.getValue()) {
                    result += "<li>" + p + "</li>";
                }
                result += "</ul></li>";
            }
        }
        return result;
    }
}
