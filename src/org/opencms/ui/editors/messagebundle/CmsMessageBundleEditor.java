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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsUIServlet;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsHasShortcutActions;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.editors.I_CmsEditor;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorModel.ConfigurableMessages;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorModel.KeyChangeResult;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.BundleType;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EditMode;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EntryChangeEvent;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_EntryChangeListener;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_ItemDeletionListener;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_OptionListener;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.ItemDeletionEvent;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.TableProperty;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.TranslateTableFieldFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.tepi.filtertable.FilterTable;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Focusable;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Controller for the VAADIN UI of the Message Bundle Editor.
 */
@Theme("opencms")
public class CmsMessageBundleEditor
implements I_CmsEditor, I_CmsWindowCloseListener, ViewChangeListener, I_EntryChangeListener, I_ItemDeletionListener,
I_OptionListener, I_CmsHasShortcutActions {

    /** Used to implement {@link java.io.Serializable}. */
    private static final long serialVersionUID = 5366955716462191580L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMessageBundleEditor.class);

    /** Save shortcut. */
    private static final Action ACTION_SAVE = new ShortcutAction(
        "Ctrl+S",
        ShortcutAction.KeyCode.S,
        new int[] {ShortcutAction.ModifierKey.CTRL});

    /** Save & Exit shortcut. */
    private static final Action ACTION_SAVE_AND_EXIT = new ShortcutAction(
        "Ctrl+Shift+S",
        ShortcutAction.KeyCode.S,
        new int[] {ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.SHIFT});

    /** Exit shortcut. */
    private static final Action ACTION_EXIT = new ShortcutAction(
        "Ctrl+X",
        ShortcutAction.KeyCode.X,
        new int[] {ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.SHIFT});

    /** The bundle editor shortcuts. */
    Map<Action, Runnable> m_shortcutActions;

    /** Messages used by the GUI. */
    CmsMessages m_messages;

    /** Configurable Messages. */
    ConfigurableMessages m_configurableMessages;

    /** The field factories for the different modes. */
    private final Map<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableFieldFactory> m_fieldFactories = new HashMap<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableFieldFactory>(
        2);
    /** The cell style generators for the different modes. */
    private final Map<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator> m_styleGenerators = new HashMap<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator>(
        2);

    /** Flag, indicating if leaving the editor is confirmed. */
    boolean m_leaving;

    /** The context of the UI. */
    I_CmsAppUIContext m_context;

    /** The model behind the UI. */
    CmsMessageBundleEditorModel m_model;

    /** CmsObject for read / write actions. */
    CmsObject m_cms;

    /** The resource that was opened with the editor. */
    CmsResource m_resource;

    /** The table component that is shown. */
    FilterTable m_table;

    /** The save button. */
    Button m_saveBtn;
    /** The save and exit button. */
    Button m_saveExitBtn;

    /** The options column, optionally shown in the table. */
    CmsMessageBundleEditorTypes.OptionColumnGenerator m_optionsColumn;

    /** The place where to go when the editor is closed. */
    private String m_backLink;

    /** The options view of the editor. */
    private CmsMessageBundleEditorOptions m_options;

    /**
     * Default constructor.
     */
    public CmsMessageBundleEditor() {
        m_shortcutActions = new HashMap<Action, Runnable>();
        m_shortcutActions.put(ACTION_SAVE, new Runnable() {

            public void run() {

                saveAction();
            }
        });
        m_shortcutActions.put(ACTION_SAVE_AND_EXIT, new Runnable() {

            public void run() {

                saveAction();
                closeAction();
            }
        });
        m_shortcutActions.put(ACTION_EXIT, new Runnable() {

            public void run() {

                closeAction();
            }
        });

    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        // do nothing

    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(final ViewChangeEvent event) {

        if (!m_leaving && m_model.hasChanges()) {
            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EDITOR_CLOSE_CAPTION_0),
                CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EDITOR_CLOSE_TEXT_0),
                new Runnable() {

                    public void run() {

                        m_leaving = true;
                        event.getNavigator().navigateTo(event.getViewName());
                    }
                });
            return false;
        }

        cleanUpAction();
        return true;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#getPriority()
     */
    public int getPriority() {

        return 200;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasShortcutActions#getShortcutActions()
     */
    public Map<Action, Runnable> getShortcutActions() {

        return m_shortcutActions;
    }

    /**
     * @see org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_OptionListener#handleAddKey(java.lang.String)
     */
    public boolean handleAddKey(final String newKey) {

        Map<Object, Object> filters = getFilters();
        m_table.clearFilters();
        boolean canAdd = !keyAlreadyExists(newKey);
        if (canAdd) {
            Object copyEntryId = m_table.addItem();
            Item copyEntry = m_table.getItem(copyEntryId);
            copyEntry.getItemProperty(TableProperty.KEY).setValue(newKey);
        }
        setFilters(filters);
        return canAdd;
    }

    /**
     * @see org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_EntryChangeListener#handleEntryChange(org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EntryChangeEvent)
     */
    public void handleEntryChange(EntryChangeEvent event) {

        try {
            m_model.lockOnChange(event.getPropertyId());

            if (event.getPropertyId().equals(TableProperty.KEY)) {
                KeyChangeResult result = m_model.handleKeyChange(event, true);
                String captionKey = null;
                String descriptionKey = null;
                switch (result) {
                    case SUCCESS:
                        handleChange(event.getPropertyId());
                        return;
                    case FAILED_DUPLICATED_KEY:
                        captionKey = Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_KEY_ALREADY_EXISTS_CAPTION_0;
                        descriptionKey = Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_KEY_ALREADY_EXISTS_DESCRIPTION_0;
                        break;
                    case FAILED_FOR_OTHER_LANGUAGE:
                        captionKey = Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_KEY_RENAMING_FAILED_CAPTION_0;
                        descriptionKey = Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_KEY_RENAMING_FAILED_DESCRIPTION_0;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                CmsMessageBundleEditorTypes.showWarning(m_messages.key(captionKey), m_messages.key(descriptionKey));
                event.getSource().focus();
            }
            handleChange(event.getPropertyId());

        } catch (CmsException e) {
            // TODO: Localize
            LOG.debug(e);
            CmsMessageBundleEditorTypes.showWarning(
                m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_EDITING_NOT_POSSIBLE_0),
                m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_EDITING_NOT_POSSIBLE_DESCRIPTION_0));
            event.getSource().setValue(event.getOldValue());
        }
    }

    /**
     * @see org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_ItemDeletionListener#handleItemDeletion(org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.ItemDeletionEvent)
     */
    public boolean handleItemDeletion(ItemDeletionEvent e) {

        Item it = m_table.getItem(e.getItemId());
        Property<?> keyProp = it.getItemProperty(TableProperty.KEY);
        String key = (String)keyProp.getValue();
        if (m_model.handleKeyDeletion(key)) {
            return true;
        }
        CmsMessageBundleEditorTypes.showWarning(
            m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_REMOVE_ENTRY_FAILED_CAPTION_0),
            m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_REMOVE_ENTRY_FAILED_DESCRIPTION_0));
        return false;

    }

    /**
     * @see org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_OptionListener#handleLanguageChange(java.util.Locale)
     */
    public void handleLanguageChange(final Locale locale) {

        if (!locale.equals(m_model.getLocale())) {
            Object sortProperty = m_table.getSortContainerPropertyId();
            boolean isAcending = m_table.isSortAscending();
            Map<Object, Object> filters = getFilters();
            m_table.clearFilters();
            if (m_model.setLocale(locale)) {
                m_options.setEditedFilePath(m_model.getEditedFilePath());
                m_table.sort(new Object[] {sortProperty}, new boolean[] {isAcending});
            } else {
                String caption = m_messages.key(
                    Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_LOCALE_SWITCHING_FAILED_CAPTION_0);
                String description = m_messages.key(
                    Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_LOCALE_SWITCHING_FAILED_DESCRIPTION_0);
                Notification warning = new Notification(caption, description, Type.WARNING_MESSAGE, true);
                warning.setDelayMsec(-1);
                warning.show(UI.getCurrent().getPage());
                m_options.setLanguage(m_model.getLocale());
            }
            setFilters(filters);
            m_table.select(m_table.getCurrentPageFirstItemId());
        }
    }

    /**
     * @see org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.I_OptionListener#handleModeChange(org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EditMode)
     */
    public void handleModeChange(final EditMode mode) {

        setEditMode(mode);

    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#initUI(org.opencms.ui.apps.I_CmsAppUIContext, org.opencms.file.CmsResource, java.lang.String)
     */
    public void initUI(I_CmsAppUIContext context, CmsResource resource, String backLink) {

        m_cms = ((CmsUIServlet)VaadinServlet.getCurrent()).getCmsObject();
        m_messages = Messages.get().getBundle(UI.getCurrent().getLocale());
        m_resource = resource;
        m_backLink = backLink;
        m_context = context;

        try {
            m_model = new CmsMessageBundleEditorModel(m_cms, m_resource);
            m_options = new CmsMessageBundleEditorOptions(
                m_model.getLocales(),
                m_model.getLocale(),
                m_model.getEditMode(),
                this);
            m_options.setEditedFilePath(m_model.getEditedFilePath());
            m_configurableMessages = m_model.getConfigurableMessages(m_messages, UI.getCurrent().getLocale());

            fillToolBar(context);
            context.showInfoArea(false);

            Component main = createMainComponent();

            initFieldFactories();
            initStyleGenerators();

            m_table.setTableFieldFactory(m_fieldFactories.get(m_model.getEditMode()));
            m_table.setCellStyleGenerator(m_styleGenerators.get(m_model.getEditMode()));

            m_optionsColumn.registerItemDeletionListener(this);

            adjustVisibleColumns();

            context.setAppContent(main);

            adjustFocus();

            if (m_model.getSwitchedLocaleOnOpening()) {
                CmsMessageBundleEditorTypes.showWarning(
                    m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_SWITCHED_LOCALE_CAPTION_0),
                    m_messages.key(Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_SWITCHED_LOCALE_DESCRIPTION_0));
            }

        } catch (IOException | CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_LOADING_RESOURCES_0), e);
            Notification.show(m_messages.key(Messages.ERR_LOADING_RESOURCES_0), Type.ERROR_MESSAGE);
            closeAction();
        }

    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#matchesResource(org.opencms.file.CmsResource, boolean)
     */
    public boolean matchesResource(CmsResource resource, boolean plainText) {

        if (plainText) {
            return false;
        }

        String resourceTypeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        if (CmsMessageBundleEditorTypes.BundleType.toBundleType(resourceTypeName) != null) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#newInstance()
     */
    public I_CmsEditor newInstance() {

        return new CmsMessageBundleEditor();
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        cleanUpAction();

    }

    /**
     * Unlocks all resources. Call when closing the editor.
     */
    void closeAction() {

        CmsEditor.openBackLink(m_backLink);
    }

    /**
     * Returns the currently set filters in a map column -> filter.
     *
     * @return the currently set filters in a map column -> filter.
     */
    Map<Object, Object> getFilters() {

        Map<Object, Object> result = new HashMap<Object, Object>(4);
        result.put(TableProperty.KEY, m_table.getFilterFieldValue(TableProperty.KEY));
        result.put(TableProperty.DEFAULT, m_table.getFilterFieldValue(TableProperty.DEFAULT));
        result.put(TableProperty.DESCRIPTION, m_table.getFilterFieldValue(TableProperty.DESCRIPTION));
        result.put(TableProperty.TRANSLATION, m_table.getFilterFieldValue(TableProperty.TRANSLATION));
        return result;
    }

    /**
     * Publish the changes.
     */
    void publishAction() {

        //save first
        saveAction();

        //publish
        m_model.publish();

    }

    /**
     * Save the changes.
     */
    void saveAction() {

        Map<Object, Object> filters = getFilters();
        m_table.clearFilters();

        try {

            m_model.save();
            disableSaveButtons();

        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_SAVING_CHANGES_0), e);
            CmsErrorDialog.showErrorDialog(m_messages.key(Messages.ERR_SAVING_CHANGES_0), e);
        }

        setFilters(filters);

    }

    /**
     * Set the edit mode.
     * @param newMode the edit mode to set.
     * @return Flag, indicating if mode switching was successful.
     */
    boolean setEditMode(CmsMessageBundleEditorTypes.EditMode newMode) {

        CmsMessageBundleEditorTypes.EditMode oldMode = m_model.getEditMode();
        boolean success = false;
        if (!newMode.equals(oldMode)) {
            Map<Object, Object> filters = getFilters();
            m_table.clearFilters();
            if (m_model.setEditMode(newMode)) {
                m_table.setTableFieldFactory(m_fieldFactories.get(newMode));
                m_table.setCellStyleGenerator(m_styleGenerators.get(newMode));
                adjustOptionsColumn(oldMode, newMode);
                m_options.updateShownOptions(m_model.hasMasterMode(), m_model.canAddKeys());
                m_options.setEditMode(newMode);
                success = true;
            } else {
                Notification.show(m_messages.key(Messages.ERR_MODE_CHANGE_NOT_POSSIBLE_0), Type.ERROR_MESSAGE);

            }
            setFilters(filters);
            adjustFocus();
        }
        return success;

    }

    /**
     * Sets the provided filters.
     * @param filters a map "column id -> filter".
     */
    void setFilters(Map<Object, Object> filters) {

        for (Object column : filters.keySet()) {
            Object filterValue = filters.get(column);
            if ((filterValue != null) && !filterValue.toString().isEmpty() && !m_table.isColumnCollapsed(column)) {
                m_table.setFilterFieldValue(column, filterValue);
            }
        }
    }

    /**
     * Sets the focus to the first editable field of the table.
     * If entries can be added, it is set to the first field of the "Add entries" row.
     */
    private void adjustFocus() {

        // Put the focus on the "Filter key" field first
        ((Focusable)m_table.getFilterField(TableProperty.KEY)).focus();
    }

    /**
     * Show or hide the options column dependent on the provided edit mode.
     * @param oldMode the old edit mode
     * @param newMode the edit mode for which the options column's visibility should be adjusted.
     */
    private void adjustOptionsColumn(
        CmsMessageBundleEditorTypes.EditMode oldMode,
        CmsMessageBundleEditorTypes.EditMode newMode) {

        if (m_model.isShowOptionsColumn(oldMode) != m_model.isShowOptionsColumn(newMode)) {
            m_table.removeGeneratedColumn(TableProperty.OPTIONS);
            if (m_model.isShowOptionsColumn(newMode)) {
                // Don't know why exactly setting the filter field invisible is necessary here,
                // it should be already set invisible - but apparently not setting it invisible again
                // will result in the field being visible.
                m_table.setFilterFieldVisible(TableProperty.OPTIONS, false);
                m_table.addGeneratedColumn(TableProperty.OPTIONS, m_optionsColumn);
            }
        }
    }

    /**
     * Adjust the visible columns.
     */
    private void adjustVisibleColumns() {

        if (m_table.isColumnCollapsingAllowed()) {
            if ((m_model.hasDefaultValues()) || m_model.getBundleType().equals(BundleType.DESCRIPTOR)) {
                m_table.setColumnCollapsed(TableProperty.DEFAULT, false);
            } else {
                m_table.setColumnCollapsed(TableProperty.DEFAULT, true);
            }

            if (((m_model.getEditMode().equals(EditMode.MASTER) || m_model.hasDescriptionValues()))
                || m_model.getBundleType().equals(BundleType.DESCRIPTOR)) {
                m_table.setColumnCollapsed(TableProperty.DESCRIPTION, false);
            } else {
                m_table.setColumnCollapsed(TableProperty.DESCRIPTION, true);
            }
        }
    }

    /**
     * Unlock all edited resources.
     */
    private void cleanUpAction() {

        try {
            m_model.deleteDescriptorIfNecessary();
        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_DELETING_DESCRIPTOR_0), e);
        }
        // unlock resource
        try {
            m_model.unlock();
        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_UNLOCKING_RESOURCES_0), e);
        }

    }

    /**
     * Returns a button component. On click, it triggers adding a bundle descriptor.
     * @return a button for adding a descriptor to a bundle.
     */
    @SuppressWarnings("serial")
    private Component createAddDescriptorButton() {

        Button addDescriptorButton = CmsToolBar.createButton(
            FontOpenCms.COPY_LOCALE,
            m_messages.key(Messages.GUI_ADD_DESCRIPTOR_0));

        addDescriptorButton.setDisableOnClick(true);

        addDescriptorButton.addClickListener(new ClickListener() {

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                Map<Object, Object> filters = getFilters();
                m_table.clearFilters();
                if (!m_model.addDescriptor()) {
                    CmsVaadinUtils.showAlert(
                        m_messages.key(Messages.ERR_BUNDLE_DESCRIPTOR_CREATION_FAILED_0),
                        m_messages.key(Messages.ERR_BUNDLE_DESCRIPTOR_CREATION_FAILED_DESCRIPTION_0),
                        null);
                } else {
                    IndexedContainer newContainer = null;
                    try {
                        newContainer = m_model.getContainerForCurrentLocale();
                        m_table.setContainerDataSource(newContainer);
                        initFieldFactories();
                        initStyleGenerators();
                        setEditMode(EditMode.MASTER);
                        m_table.setColumnCollapsingAllowed(true);
                        adjustVisibleColumns();
                        m_options.updateShownOptions(m_model.hasMasterMode(), m_model.canAddKeys());
                        m_options.setEditMode(m_model.getEditMode());
                    } catch (IOException | CmsException e) {
                        // Can never appear here, since container is created by addDescriptor already.
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                setFilters(filters);
            }
        });
        return addDescriptorButton;
    }

    /**
     * Create the close button UI Component.
     * @return the close button.
     */
    @SuppressWarnings("serial")
    private Component createCloseButton() {

        Button closeBtn = CmsToolBar.createButton(
            FontOpenCms.CIRCLE_INV_CANCEL,
            m_messages.key(Messages.GUI_BUTTON_CANCEL_0));
        closeBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                closeAction();
            }

        });
        return closeBtn;
    }

    /**
     * Creates the button for converting an XML bundle in a property bundle.
     * @return the created button.
     */
    private Component createConvertToPropertyBundleButton() {

        Button addDescriptorButton = CmsToolBar.createButton(
            FontOpenCms.SETTINGS,
            m_messages.key(Messages.GUI_CONVERT_TO_PROPERTY_BUNDLE_0));

        addDescriptorButton.setDisableOnClick(true);

        addDescriptorButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                try {
                    m_model.saveAsPropertyBundle();
                    Notification.show("Conversion successful.");
                } catch (CmsException | IOException e) {
                    CmsVaadinUtils.showAlert("Conversion failed", e.getLocalizedMessage(), null);
                }
            }
        });
        addDescriptorButton.setDisableOnClick(true);
        return addDescriptorButton;
    }

    /**
     * Creates the main component of the editor with all sub-components.
     * @return the completely filled main component of the editor.
     * @throws IOException thrown if setting the table's content data source fails.
     * @throws CmsException thrown if setting the table's content data source fails.
     */
    private Component createMainComponent() throws IOException, CmsException {

        VerticalLayout mainComponent = new VerticalLayout();
        mainComponent.setSizeFull();
        mainComponent.addStyleName("o-message-bundle-editor");
        m_table = createTable();
        Panel navigator = new Panel();
        navigator.setSizeFull();
        navigator.setContent(m_table);
        navigator.addActionHandler(new CmsMessageBundleEditorTypes.TableKeyboardHandler(m_table));
        navigator.addStyleName("v-panel-borderless");

        mainComponent.addComponent(m_options.getOptionsComponent());
        mainComponent.addComponent(navigator);
        mainComponent.setExpandRatio(navigator, 1f);
        m_options.updateShownOptions(m_model.hasMasterMode(), m_model.canAddKeys());
        return mainComponent;
    }

    /** Creates the save button UI Component.
     * @return the save button.
     */
    @SuppressWarnings("serial")
    private Component createPublishButton() {

        Button publishBtn = CmsToolBar.createButton(FontOpenCms.PUBLISH, m_messages.key(Messages.GUI_BUTTON_PUBLISH_0));
        publishBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                publishAction();
            }

        });
        return publishBtn;
    }

    /** Creates the save button UI Component.
     * @return the save button.
     */
    @SuppressWarnings("serial")
    private Button createSaveButton() {

        Button saveBtn = CmsToolBar.createButton(FontOpenCms.SAVE, m_messages.key(Messages.GUI_BUTTON_SAVE_0));
        saveBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                saveAction();
            }

        });
        saveBtn.setEnabled(false);
        return saveBtn;
    }

    /** Creates the save and exit button UI Component.
     * @return the save and exit button.
     */
    @SuppressWarnings("serial")
    private Button createSaveExitButton() {

        Button saveExitBtn = CmsToolBar.createButton(
            FontOpenCms.SAVE_EXIT,
            m_messages.key(Messages.GUI_BUTTON_SAVE_AND_EXIT_0));
        saveExitBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                saveAction();
                closeAction();

            }
        });
        saveExitBtn.setEnabled(false);
        return saveExitBtn;
    }

    /** Creates the (filled) table UI component.
     * @return the (filled) table
     * @throws IOException thrown if reading the properties file fails.
     * @throws CmsException thrown if some read action for getting the table contentFilter fails.
     */
    private FilterTable createTable() throws IOException, CmsException {

        final FilterTable table = new FilterTable();
        table.setSizeFull();

        table.setContainerDataSource(m_model.getContainerForCurrentLocale());

        table.setColumnHeader(TableProperty.KEY, m_configurableMessages.getColumnHeader(TableProperty.KEY));
        table.setColumnCollapsible(TableProperty.KEY, false);

        table.setColumnHeader(TableProperty.DEFAULT, m_configurableMessages.getColumnHeader(TableProperty.DEFAULT));
        table.setColumnCollapsible(TableProperty.DEFAULT, true);

        table.setColumnHeader(
            TableProperty.DESCRIPTION,
            m_configurableMessages.getColumnHeader(TableProperty.DESCRIPTION));
        table.setColumnCollapsible(TableProperty.DESCRIPTION, true);

        table.setColumnHeader(
            TableProperty.TRANSLATION,
            m_configurableMessages.getColumnHeader(TableProperty.TRANSLATION));
        table.setColumnCollapsible(TableProperty.TRANSLATION, false);

        table.setColumnHeader(TableProperty.OPTIONS, m_configurableMessages.getColumnHeader(TableProperty.OPTIONS));
        table.setFilterDecorator(new CmsMessageBundleEditorFilterDecorator());

        table.setFilterBarVisible(true);
        table.setColumnCollapsible(TableProperty.OPTIONS, false);

        table.setSortEnabled(true);
        table.setEditable(true);

        table.setSelectable(true);
        table.setImmediate(true);
        table.setMultiSelect(false);

        table.setColumnCollapsingAllowed(m_model.hasDescriptor());

        table.setColumnReorderingAllowed(false);

        m_optionsColumn = generateOptionsColumn(table);

        if (m_model.isShowOptionsColumn(m_model.getEditMode())) {
            table.setFilterFieldVisible(TableProperty.OPTIONS, false);
            table.addGeneratedColumn(TableProperty.OPTIONS, m_optionsColumn);
        }
        table.setColumnWidth(TableProperty.OPTIONS, CmsMessageBundleEditorTypes.OPTION_COLUMN_WIDTH);
        table.setColumnExpandRatio(TableProperty.KEY, 1f);
        table.setColumnExpandRatio(TableProperty.DESCRIPTION, 1f);
        table.setColumnExpandRatio(TableProperty.DEFAULT, 1f);
        table.setColumnExpandRatio(TableProperty.TRANSLATION, 1f);

        table.setPageLength(30);
        table.setCacheRate(1);
        table.sort(new Object[] {TableProperty.KEY}, new boolean[] {true});
        table.addContextClickListener(new ContextClickListener() {

            private static final long serialVersionUID = 1L;

            public void contextClick(ContextClickEvent event) {

                Object itemId = m_table.getValue();
                CmsContextMenu contextMenu = m_model.getContextMenuForItem(itemId);
                if (null != contextMenu) {
                    contextMenu.setAsContextMenuOf(m_table);
                    contextMenu.setOpenAutomatically(false);
                    contextMenu.open(event.getClientX(), event.getClientY());
                }
            }
        });
        table.setNullSelectionAllowed(false);
        table.select(table.getCurrentPageFirstItemId());
        return table;
    }

    /**
     * Disable the save buttons, e.g., after saving.
     */
    private void disableSaveButtons() {

        if (m_saveBtn.isEnabled()) {
            m_saveBtn.setEnabled(false);
            m_saveExitBtn.setEnabled(false);
        }

    }

    /** Adds Editor specific UI components to the toolbar.
     * @param context The context that provides access to the toolbar.
     */
    private void fillToolBar(final I_CmsAppUIContext context) {

        context.setAppTitle(m_messages.key(Messages.GUI_APP_TITLE_0));

        // create components
        Component publishBtn = createPublishButton();
        m_saveBtn = createSaveButton();
        m_saveExitBtn = createSaveExitButton();
        Component closeBtn = createCloseButton();

        context.enableDefaultToolbarButtons(false);
        context.addToolbarButtonRight(closeBtn);
        context.addToolbarButton(publishBtn);
        context.addToolbarButton(m_saveExitBtn);
        context.addToolbarButton(m_saveBtn);

        Component addDescriptorBtn = createAddDescriptorButton();
        if (m_model.hasDescriptor() || m_model.getBundleType().equals(BundleType.DESCRIPTOR)) {
            addDescriptorBtn.setEnabled(false);
        }
        context.addToolbarButton(addDescriptorBtn);
        if (m_model.getBundleType().equals(BundleType.XML)) {
            Component convertToPropertyBundleBtn = createConvertToPropertyBundleButton();
            context.addToolbarButton(convertToPropertyBundleBtn);
        }
    }

    /** Generates the options column for the table.
     * @param table table instance passed to the option column generator
     * @return the options column
     */
    private CmsMessageBundleEditorTypes.OptionColumnGenerator generateOptionsColumn(CustomTable table) {

        return new CmsMessageBundleEditorTypes.OptionColumnGenerator(table);
    }

    /**
     * Handle a value change.
     * @param propertyId the column in which the value has changed.
     */
    private void handleChange(Object propertyId) {

        if (!m_saveBtn.isEnabled()) {
            m_saveBtn.setEnabled(true);
            m_saveExitBtn.setEnabled(true);
        }
        m_model.handleChange(propertyId);

    }

    /**
     * Initialize the field factories for the messages table.
     */
    private void initFieldFactories() {

        if (m_model.hasMasterMode()) {
            TranslateTableFieldFactory masterFieldFactory = new CmsMessageBundleEditorTypes.TranslateTableFieldFactory(
                m_table,
                m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.MASTER));
            masterFieldFactory.registerKeyChangeListener(this);
            m_fieldFactories.put(CmsMessageBundleEditorTypes.EditMode.MASTER, masterFieldFactory);
        }
        TranslateTableFieldFactory defaultFieldFactory = new CmsMessageBundleEditorTypes.TranslateTableFieldFactory(
            m_table,
            m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.DEFAULT));
        defaultFieldFactory.registerKeyChangeListener(this);
        m_fieldFactories.put(CmsMessageBundleEditorTypes.EditMode.DEFAULT, defaultFieldFactory);

    }

    /**
     * Initialize the style generators for the messages table.
     */
    private void initStyleGenerators() {

        if (m_model.hasMasterMode()) {
            m_styleGenerators.put(
                CmsMessageBundleEditorTypes.EditMode.MASTER,
                new CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator(
                    m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.MASTER)));
        }
        m_styleGenerators.put(
            CmsMessageBundleEditorTypes.EditMode.DEFAULT,
            new CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator(
                m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.DEFAULT)));

    }

    /**
     * Checks if a key already exists.
     * @param newKey the key to check for.
     * @return <code>true</code> if the key already exists, <code>false</code> otherwise.
     */
    private boolean keyAlreadyExists(String newKey) {

        Collection<?> itemIds = m_table.getItemIds();
        for (Object itemId : itemIds) {
            if (m_table.getItem(itemId).getItemProperty(TableProperty.KEY).getValue().equals(newKey)) {
                return true;
            }
        }
        return false;
    }

}
