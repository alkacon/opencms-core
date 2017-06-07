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

package org.opencms.ui.editors.messagebundle;

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EditMode;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_OptionListener;

import java.util.Collection;
import java.util.Locale;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/** View of the message bundle editor options, i.e., language/mode switcher, file name display and "Add key" option. */
public class CmsMessageBundleEditorOptions {

    /** Messages used by the GUI. */
    CmsMessages m_messages;

    /** Grid with all options (2x2). */
    private GridLayout m_optionsComponent;
    /** The upper left component in the options grid (containing the language/mode switches and the label for the file path). */
    private HorizontalLayout m_upperLeftComponent;
    /** The lower left component in the options grid (containing the "Add key" label). */
    private Component m_lowerLeftComponent;
    /** The lower right component in the options grid (containing the "Add key" input field and the "Add key" button). */
    private Component m_lowerRightComponent;
    /** The select box for choosing the currently edited language. */
    private ComboBox m_languageSelect;
    /** The component that contains the language switch. */
    private Component m_languageSwitch;
    /** The component that contains the mode switch. */
    private Component m_modeSwitch;
    /** The select box for the current edit mode. */
    private ComboBox m_modeSelect;
    /** The component with the label of the "File"-Display. */
    private Component m_filePathLabel;
    /** The input field that displays the path of the currently edited file. */
    private TextField m_filePathField;
    /** The "Add key" input field. */
    TextField m_addKeyInput;
    /** A flag, indicating if the mode switch should be shown. */
    private boolean m_showModeSwitch;
    /** A flag, indicating if the "Add key" row should be shown. */
    private boolean m_showAddKeyOption;
    /** The listener for option changes. */
    I_OptionListener m_listener;

    /**
     * Default constructor.
     * @param locales the locales shown in the language switch.
     * @param currentLocale the currently edited locale.
     * @param currentMode the current edit mode.
     * @param optionListener the option listener.
     */
    public CmsMessageBundleEditorOptions(
        final Collection<Locale> locales,
        final Locale currentLocale,
        final EditMode currentMode,
        final I_OptionListener optionListener) {

        m_messages = Messages.get().getBundle(UI.getCurrent().getLocale());
        m_listener = optionListener;
        initLanguageSwitch(locales, currentLocale);
        initModeSwitch(currentMode);
        initFilePathLabel();
        initUpperLeftComponent();
        initLowerLeftComponent();
        initLowerRightComponent();

        initOptionsComponent();
    }

    /**
     * Puts focus on the "Add key" input field, iff it is shown.
     * @return <code>true</code> if the focus has been set, <code>false</code> otherwise.
     */
    public boolean focusAddKey() {

        if (m_showAddKeyOption) {
            m_addKeyInput.focus();
        }
        return m_showAddKeyOption;
    }

    /**
     * Returns the options component.
     * @return the options component.
     */
    public Component getOptionsComponent() {

        return m_optionsComponent;
    }

    /**
     * Sets the path of the edited file in the corresponding display.
     * @param editedFilePath path of the edited file to set.
     */
    public void setEditedFilePath(final String editedFilePath) {

        m_filePathField.setReadOnly(false);
        m_filePathField.setValue(editedFilePath);
        m_filePathField.setReadOnly(true);

    }

    /**
     * Set the edit mode.
     * @param mode the edit mode to set.
     */
    public void setEditMode(final EditMode mode) {

        if (!m_modeSelect.getValue().equals(mode)) {
            m_modeSelect.setValue(mode);
        }
    }

    /**
     * Update which options are shown.
     * @param showModeSwitch flag, indicating if the mode switch should be shown.
     * @param showAddKeyOption flag, indicating if the "Add key" row should be shown.
     */
    public void updateShownOptions(boolean showModeSwitch, boolean showAddKeyOption) {

        if (showModeSwitch != m_showModeSwitch) {
            m_upperLeftComponent.removeAllComponents();
            m_upperLeftComponent.addComponent(m_languageSwitch);
            if (showModeSwitch) {
                m_upperLeftComponent.addComponent(m_modeSwitch);
            }
            m_upperLeftComponent.addComponent(m_filePathLabel);
            m_showModeSwitch = showModeSwitch;
        }
        if (showAddKeyOption != m_showAddKeyOption) {
            if (showAddKeyOption) {
                m_optionsComponent.addComponent(m_lowerLeftComponent, 0, 1);
                m_optionsComponent.addComponent(m_lowerRightComponent, 1, 1);
            } else {
                m_optionsComponent.removeComponent(0, 1);
                m_optionsComponent.removeComponent(1, 1);
            }
            m_showAddKeyOption = showAddKeyOption;
        }
    }

    /**
     * Handles adding a key. Calls the registered listener and wraps it's method in some GUI adjustments.
     */
    void handleAddKey() {

        String key = m_addKeyInput.getValue();
        if (m_listener.handleAddKey(key)) {
            Notification.show(
                key.isEmpty()
                ? m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_EMPTY_KEY_SUCCESSFULLY_ADDED_0)
                : m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_KEY_SUCCESSFULLY_ADDED_1, key));
        } else {
            CmsMessageBundleEditorTypes.showWarning(
                m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_KEY_ALREADEY_EXISTS_CAPTION_0),
                m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_KEY_ALREADEY_EXISTS_DESCRIPTION_1, key));

        }
        m_addKeyInput.focus();
        m_addKeyInput.selectAll();
    }

    /**
     * Sets the currently edited locale.
     * @param locale the locale to set.
     */
    void setLanguage(final Locale locale) {

        if (!m_languageSelect.getValue().equals(locale)) {
            m_languageSelect.setValue(locale);
        }
    }

    /**
     * Creates the "Add key" button.
     * @return the "Add key" button.
     */
    private Component createAddKeyButton() {

        // the "+" button
        Button addKeyButton = new Button();
        addKeyButton.addStyleName("icon-only");
        addKeyButton.addStyleName("borderless-colored");
        addKeyButton.setDescription(m_messages.key(Messages.GUI_ADD_KEY_0));
        addKeyButton.setIcon(FontOpenCms.CIRCLE_PLUS, m_messages.key(Messages.GUI_ADD_KEY_0));
        addKeyButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                handleAddKey();

            }
        });
        return addKeyButton;
    }

    /**
     * Creates the upper right component of the options grid.
     * Creation includes the initialization of {@link #m_filePathField}.
     *
     * @return the upper right component in the options grid.
     */
    private Component createUpperRightComponent() {

        HorizontalLayout upperRight = new HorizontalLayout();
        upperRight.setSizeFull();

        FormLayout fileNameDisplay = new FormLayout();
        fileNameDisplay.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        fileNameDisplay.setSizeFull();

        m_filePathField = new TextField();
        m_filePathField.setWidth("100%");
        m_filePathField.setEnabled(true);
        m_filePathField.setReadOnly(true);

        fileNameDisplay.addComponent(m_filePathField);
        fileNameDisplay.setSpacing(true);

        FormLayout filePathDisplay = new FormLayout();
        filePathDisplay.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        filePathDisplay.setSizeFull();
        filePathDisplay.addComponent(m_filePathField);
        filePathDisplay.setSpacing(true);

        upperRight.addComponent(filePathDisplay);
        upperRight.setExpandRatio(filePathDisplay, 2f);

        HorizontalLayout placeHolder = new HorizontalLayout();
        placeHolder.setWidth(CmsMessageBundleEditorTypes.OPTION_COLUMN_WIDTH_PX);
        upperRight.addComponent(placeHolder);

        return upperRight;
    }

    /**
     * Initializes the input field for new keys {@link #m_addKeyInput}.
     */
    private void initAddKeyInput() {

        //the input field for the key
        m_addKeyInput = new TextField();
        m_addKeyInput.setWidth("100%");
        m_addKeyInput.setInputPrompt(m_messages.key(Messages.GUI_INPUT_PROMPT_ADD_KEY_0));
        final ShortcutListener shortCutListener = new ShortcutListener("Add key via ENTER", KeyCode.ENTER, null) {

            private static final long serialVersionUID = 1L;

            @Override
            public void handleAction(Object sender, Object target) {

                handleAddKey();

            }

        };
        m_addKeyInput.addFocusListener(new FocusListener() {

            private static final long serialVersionUID = 1L;

            public void focus(FocusEvent event) {

                m_addKeyInput.addShortcutListener(shortCutListener);

            }
        });
        m_addKeyInput.addBlurListener(new BlurListener() {

            private static final long serialVersionUID = 1L;

            public void blur(BlurEvent event) {

                m_addKeyInput.removeShortcutListener(shortCutListener);

            }
        });

    }

    /**
     * Initializes the label for the file path display {@link #m_filePathLabel}.
     */
    private void initFilePathLabel() {

        m_filePathLabel = new TextField();
        m_filePathLabel.setWidth("100%");
        m_filePathLabel.setEnabled(true);
        m_filePathLabel.setReadOnly(true);
        m_filePathLabel = new Label(m_messages.key(Messages.GUI_FILENAME_LABEL_0));

    }

    /**
     * Initializes the language switcher UI Component {@link #m_languageSwitch}, including {@link #m_languageSelect}.
     * @param locales the locales that can be selected.
     * @param current the currently selected locale.
     */
    private void initLanguageSwitch(Collection<Locale> locales, Locale current) {

        FormLayout languages = new FormLayout();
        languages.setHeight("100%");
        languages.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        ComboBox languageSelect = new ComboBox();
        languageSelect.setCaption(m_messages.key(Messages.GUI_LANGUAGE_SWITCHER_LABEL_0));
        languageSelect.setNullSelectionAllowed(false);

        // set Locales
        for (Locale locale : locales) {
            languageSelect.addItem(locale);
            String caption = locale.getDisplayName(UI.getCurrent().getLocale());
            if (CmsLocaleManager.getDefaultLocale().equals(locale)) {
                caption += " ("
                    + Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_DEFAULT_LOCALE_0)
                    + ")";
            }
            languageSelect.setItemCaption(locale, caption);
        }
        languageSelect.setValue(current);
        languageSelect.setNewItemsAllowed(false);
        languageSelect.setTextInputAllowed(false);
        languageSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                m_listener.handleLanguageChange((Locale)event.getProperty().getValue());

            }
        });

        if (locales.size() == 1) {
            languageSelect.setEnabled(false);
        }
        languages.addComponent(languageSelect);
        m_languageSwitch = languages;
    }

    /**
     * Initializes the lower left component {@link #m_lowerLeftComponent} with the correctly placed "Add key"-label.
     */
    private void initLowerLeftComponent() {

        HorizontalLayout placeHolderLowerLeft = new HorizontalLayout();
        placeHolderLowerLeft.setWidth("100%");
        Label newKeyLabel = new Label(m_messages.key(Messages.GUI_CAPTION_ADD_KEY_0));
        newKeyLabel.setWidthUndefined();
        HorizontalLayout lowerLeft = new HorizontalLayout(placeHolderLowerLeft, newKeyLabel);
        lowerLeft.setWidth("100%");
        lowerLeft.setExpandRatio(placeHolderLowerLeft, 1f);
        m_lowerLeftComponent = lowerLeft;

    }

    /**
     * Initializes the lower right component {@link #m_lowerRightComponent}, with all its components, i.e.,
     * the "Add key" input field {@link #m_addKeyInput} and the "Add key" button.
     */
    private void initLowerRightComponent() {

        initAddKeyInput();

        Component addKeyButton = createAddKeyButton();
        HorizontalLayout addKeyWrapper = new HorizontalLayout(addKeyButton);
        addKeyWrapper.setComponentAlignment(addKeyButton, Alignment.MIDDLE_CENTER);
        addKeyWrapper.setHeight("100%");
        addKeyWrapper.setWidth(CmsMessageBundleEditorTypes.OPTION_COLUMN_WIDTH_PX);

        FormLayout inputForm = new FormLayout(m_addKeyInput);
        inputForm.setWidth("100%");
        HorizontalLayout lowerRight = new HorizontalLayout();
        lowerRight.setWidth("100%");
        lowerRight.addComponent(inputForm);
        lowerRight.addComponent(addKeyWrapper);
        lowerRight.setExpandRatio(inputForm, 1f);
        m_lowerRightComponent = lowerRight;

    }

    /**
     * Initializes the mode switcher.
     * @param current the current edit mode
     */
    private void initModeSwitch(final EditMode current) {

        FormLayout modes = new FormLayout();
        modes.setHeight("100%");
        modes.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        m_modeSelect = new ComboBox();
        m_modeSelect.setCaption(m_messages.key(Messages.GUI_VIEW_SWITCHER_LABEL_0));

        // add Modes
        m_modeSelect.addItem(CmsMessageBundleEditorTypes.EditMode.DEFAULT);
        m_modeSelect.setItemCaption(
            CmsMessageBundleEditorTypes.EditMode.DEFAULT,
            m_messages.key(Messages.GUI_VIEW_SWITCHER_EDITMODE_DEFAULT_0));
        m_modeSelect.addItem(CmsMessageBundleEditorTypes.EditMode.MASTER);
        m_modeSelect.setItemCaption(
            CmsMessageBundleEditorTypes.EditMode.MASTER,
            m_messages.key(Messages.GUI_VIEW_SWITCHER_EDITMODE_MASTER_0));

        // set current mode as selected
        m_modeSelect.setValue(current);

        m_modeSelect.setNewItemsAllowed(false);
        m_modeSelect.setTextInputAllowed(false);
        m_modeSelect.setNullSelectionAllowed(false);

        m_modeSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                m_listener.handleModeChange((EditMode)event.getProperty().getValue());

            }
        });

        modes.addComponent(m_modeSelect);
        m_modeSwitch = modes;
    }

    /**
     * Creates the complete options component.
     * It's a grid with two rows and two columns, styled like:
     *
     *                                              ||
     *   [language switch] [mode switch] File-Label || [file path display]
     *   -------------------------------------------||----------------------------------
     *                                New-Key-Label || [new key input] [add key button]
     *                                              ||
     *
     *   NOTE: The second row is not filled with components on initialization, what means keys can not be added
     *         Filling is done via {@link #updateShownOptions(boolean, boolean)}
     */
    private void initOptionsComponent() {

        // create and layout the component
        m_optionsComponent = new GridLayout(2, 2);
        m_optionsComponent.setHideEmptyRowsAndColumns(true);
        m_optionsComponent.setDefaultComponentAlignment(Alignment.MIDDLE_RIGHT);
        m_optionsComponent.setWidth("100%");
        m_optionsComponent.setColumnExpandRatio(1, 1f);
        m_optionsComponent.setStyleName("v-options");

        // add the components
        m_optionsComponent.addComponent(m_upperLeftComponent, 0, 0);

        Component upperRight = createUpperRightComponent();
        m_optionsComponent.addComponent(upperRight, 1, 0);
    }

    /**
     * Initializes the upper left component. Does not show the mode switch.
     */
    private void initUpperLeftComponent() {

        m_upperLeftComponent = new HorizontalLayout();
        m_upperLeftComponent.setHeight("100%");
        m_upperLeftComponent.setSpacing(true);
        m_upperLeftComponent.setDefaultComponentAlignment(Alignment.MIDDLE_RIGHT);
        m_upperLeftComponent.addComponent(m_languageSwitch);
        m_upperLeftComponent.addComponent(m_filePathLabel);

    }

}
