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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.shared.CmsDialogOptions;
import org.opencms.ade.containerpage.shared.CmsDialogOptions.Option;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog to show different options to select.<p>
 */
public class CmsOptionDialog extends CmsPopup {

    /** The radio button group. */
    CmsRadioButtonGroup m_buttonGroup;

    /**
     * Constructor.<p>
     *
     * @param caption the default dialog caption
     * @param options the available options
     * @param resourceInfo the resource info if available
     * @param onSelect the on select callback
     */
    public CmsOptionDialog(
        String caption,
        CmsDialogOptions options,
        CmsListInfoBean resourceInfo,
        final I_CmsSimpleCallback<String> onSelect) {

        super(CmsStringUtil.isNotEmptyOrWhitespaceOnly(options.getTitle()) ? options.getTitle() : caption);
        setModal(true);
        setGlassEnabled(true);
        FlowPanel panel = new FlowPanel();
        if (resourceInfo != null) {
            CmsListItemWidget resourceInfoWidget = new CmsListItemWidget(resourceInfo);
            panel.add(resourceInfoWidget);
        }
        VerticalPanel innerContent = new VerticalPanel();
        innerContent.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().border());
        innerContent.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        innerContent.getElement().getStyle().setProperty(
            "marginTop",
            I_CmsLayoutBundle.INSTANCE.constants().css().defaultSpace());
        innerContent.getElement().getStyle().setPadding(10, Unit.PX);
        innerContent.setWidth("100%");
        panel.add(innerContent);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(options.getInfo())) {
            HTML infoHTML = new HTML(options.getInfo());
            infoHTML.getElement().getStyle().setMarginBottom(10, Unit.PX);
            innerContent.add(infoHTML);
        }
        m_buttonGroup = new CmsRadioButtonGroup();
        boolean valueSet = false;
        for (Option option : options.getOptions()) {
            CmsRadioButton radioButton = new CmsRadioButton(option.getValue(), option.getLabel());
            radioButton.setGroup(m_buttonGroup);
            innerContent.add(radioButton);
            if (option.isDisabled()) {
                radioButton.setEnabled(false);
            } else if (!valueSet) {
                radioButton.setChecked(true);
                valueSet = true;
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(option.getDescription())) {
                radioButton.setTitle(option.getDescription());
            }

        }
        setMainContent(panel);
        CmsPushButton ok = new CmsPushButton();
        ok.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        ok.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                onSelect.execute(m_buttonGroup.getSelectedButton().getName());
                close();
            }
        });
        CmsPushButton cancel = new CmsPushButton();
        cancel.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        cancel.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                close();
            }
        });
        addButton(cancel);
        addButton(ok);
        addDialogClose(null);
    }

    /**
     * Closes the dialog.<p>
     */
    void close() {

        if (m_closeCommand != null) {
            m_closeCommand.execute();
        }
        hide();
    }
}
