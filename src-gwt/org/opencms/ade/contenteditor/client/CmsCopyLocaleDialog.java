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

import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsMessageWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The copy locale dialog.<p>
 */
public class CmsCopyLocaleDialog extends CmsPopup {

    /** Locale checkbox value change handler. */
    private class CopyLocaleChangeHandler implements ValueChangeHandler<Boolean> {

        /** The locale. */
        private String m_copyLocale;

        /**
         * The copy locale checkbox change handler.<p>
         *
         * @param locale the locale to copy
         */
        CopyLocaleChangeHandler(String locale) {

            m_copyLocale = locale;
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<Boolean> event) {

            if (event.getValue().booleanValue()) {
                m_targetLocales.add(m_copyLocale);
                m_okButton.enable();
            } else {
                m_targetLocales.remove(m_copyLocale);
                if (m_targetLocales.isEmpty()) {
                    m_okButton.disable(Messages.get().key(Messages.GUI_LOCALE_DIALOG_NO_LANGUAGE_SELECTED_0));
                }
            }
        }
    }

    /** The ok button. */
    CmsPushButton m_okButton;

    /** The synchronize locale independent fields button. */
    CmsPushButton m_synchronizeLocaleButton;

    /** The locales to copy the current locale values to. */
    Set<String> m_targetLocales;

    /**
     * Constructor.<p>
     *
     * @param availableLocales the available locales
     * @param contentLocales the present content locales
     * @param currentLocale the current content locale
     * @param hasSync indicates the dialog requires the synchronize locale independent fields button
     * @param editor the editor instance
     */
    public CmsCopyLocaleDialog(
        Map<String, String> availableLocales,
        Set<String> contentLocales,
        String currentLocale,
        boolean hasSync,
        final CmsContentEditor editor) {

        super(Messages.get().key(Messages.GUI_LOCALE_DIALOG_TITLE_0));
        FlowPanel main = new FlowPanel();
        CmsMessageWidget message = new CmsMessageWidget();
        message.setMessageText(
            Messages.get().key(Messages.GUI_LOCALE_DIALOG_DESCRIPTION_1, availableLocales.get(currentLocale)));
        main.add(message);
        CmsFieldSet fieldset = new CmsFieldSet();
        m_targetLocales = new HashSet<String>();
        for (Entry<String, String> availableLocaleEntry : availableLocales.entrySet()) {
            if (!availableLocaleEntry.getKey().equals(currentLocale)) {
                String label = availableLocaleEntry.getValue();
                if (!contentLocales.contains(availableLocaleEntry.getKey())) {
                    label += " [-]";
                }
                CmsCheckBox checkBox = new CmsCheckBox(label);
                checkBox.addValueChangeHandler(new CopyLocaleChangeHandler(availableLocaleEntry.getKey()));
                fieldset.addContent(checkBox);
            }
        }
        m_okButton = new CmsPushButton();
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_okButton.setUseMinWidth(true);
        m_okButton.setText(Messages.get().key(Messages.GUI_LOCALE_DIALOG_OK_0));
        m_okButton.disable(Messages.get().key(Messages.GUI_LOCALE_DIALOG_NO_LANGUAGE_SELECTED_0));
        m_okButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                hide();
                editor.copyLocales(m_targetLocales);
            }
        });
        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setText(Messages.get().key(Messages.GUI_LOCALE_DIALOG_CANCEL_0));
        cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                hide();
            }
        });
        cancelButton.setUseMinWidth(true);
        main.add(fieldset);
        setMainContent(main);
        addButton(cancelButton);
        addButton(m_okButton);
        if (hasSync) {
            m_synchronizeLocaleButton = new CmsPushButton();
            m_synchronizeLocaleButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.GREEN);
            m_synchronizeLocaleButton.setUseMinWidth(true);
            m_synchronizeLocaleButton.setText(Messages.get().key(Messages.GUI_LOCALE_DIALOG_SYNCHRONIZE_0));
            m_synchronizeLocaleButton.setTitle(Messages.get().key(Messages.GUI_LOCALE_DIALOG_SYNCHRONIZE_TITLE_0));
            m_synchronizeLocaleButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    hide();
                    editor.synchronizeCurrentLocale();
                }
            });
            addButton(m_synchronizeLocaleButton);
        }
        setGlassEnabled(true);
    }
}
