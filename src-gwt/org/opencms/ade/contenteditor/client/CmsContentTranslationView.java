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

package org.opencms.ade.contenteditor.client;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroupWidget;
import org.opencms.gwt.shared.CmsGwtLog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public class CmsContentTranslationView extends Composite {

    /**
     * UiBinder interface for this dialog.<p>
     */
    interface I_UiBinder extends UiBinder<Panel, CmsContentTranslationView> {
        // empty uibinder interface
    }

    /** UiBinder instance for this dialog. */
    private static I_UiBinder uibinder = GWT.create(I_UiBinder.class);

    /** The action to execute after selecting the locale. */
    private Consumer<String> m_action;

    /** The selection widget for the target locale. */
    private CmsRadioButtonGroupWidget m_input;

    /** The popup in which this widget is displayed. */
    private CmsPopup m_popup;

    /** The label to display above the radio buttons. */
    @UiField
    protected Label m_label;

    /**
     * The container for the radio buttons.
     */
    @UiField
    protected FlowPanel m_radioButtonContainer;

    /** The buttons. */
    private List<CmsPushButton> m_buttons = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param locale the current locale
     * @param availableLocales the available locales
     * @param action the action to execute after selecting a locale
     */
    public CmsContentTranslationView(String locale, Map<String, String> availableLocales, Consumer<String> action) {

        m_action = action;
        Panel content = uibinder.createAndBindUi(this);
        initWidget(content);

        LinkedHashMap<String, String> selectableLocales = new LinkedHashMap<>(availableLocales);
        selectableLocales.remove(locale);

        CmsPushButton okButton = new CmsPushButton();
        okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        okButton.setText(Messages.get().key(Messages.GUI_OK_0));
        okButton.setUseMinWidth(true);
        m_label.setText(
            org.opencms.ade.contenteditor.client.Messages.get().key(
                org.opencms.ade.contenteditor.client.Messages.GUI_TRANSLATION_DIALOG_LOCALE_CHOICE_0));
        m_input = new CmsRadioButtonGroupWidget(selectableLocales);
        m_input.setFormValueAsString(selectableLocales.keySet().iterator().next());
        m_radioButtonContainer.add(m_input);
        okButton.addClickHandler(event -> {

            String formValue = m_input.getFormValueAsString();
            String localeSelected = formValue.trim();
            CmsGwtLog.log("ok clicked, locale=" + localeSelected); //$NON-NLS-1$
            m_popup.hide();
            m_action.accept(localeSelected);
        });

        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        cancelButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.setUseMinWidth(true);
        cancelButton.addClickHandler(event -> m_popup.hide());
        m_buttons.add(cancelButton);
        m_buttons.add(okButton);

    }

    /**
     * Shows the content translation dialog.
     *
     * @param locale the current locale
     * @param availableLocales the available locales
     * @param action the action to perform after locale selection
     */
    public static void showDialog(String locale, Map<String, String> availableLocales, Consumer<String> action) {

        CmsPopup popup = new CmsPopup(
            org.opencms.ade.contenteditor.client.Messages.get().key(
                org.opencms.ade.contenteditor.client.Messages.GUI_TRANSLATION_DIALOG_TITLE_0),
            CmsPopup.DEFAULT_WIDTH);
        CmsContentTranslationView view = new CmsContentTranslationView(locale, availableLocales, action);

        popup.setMainContent(view);
        popup.setModal(true);
        view.setPopup(popup);
        for (CmsPushButton button : view.getButtons()) {
            popup.addButton(button);
        }

        popup.center();
    }

    /**
     * Gets the buttons.
     *
     * @return the buttons
     */
    private List<CmsPushButton> getButtons() {

        return m_buttons;
    }

    /**
     * Sets the popup the widget is displayed in.
     *
     * @param popup the popup
     */
    private void setPopup(CmsPopup popup) {

        m_popup = popup;

    }

}
