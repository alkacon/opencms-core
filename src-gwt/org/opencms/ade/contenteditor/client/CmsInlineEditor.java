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

package org.opencms.ade.contenteditor.client;

import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.vie.shared.I_Entity;

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsInfoHeader;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The in-line content editor.<p>
 */
public final class CmsInlineEditor {

    /** The in-line editor instance. */
    private static CmsInlineEditor INSTANCE;

    /** The editor base. */
    protected CmsEditorBase m_editor;

    /** The edit tool-bar. */
    protected CmsToolbar m_toolbar;

    /** The available locales. */
    private Map<String, String> m_availableLocales;

    /** The form editing base panel. */
    private FlowPanel m_basePanel;

    /** The cancel button. */
    private CmsPushButton m_cancelButton;

    /** The id's of the changed entities. */
    private Set<String> m_changedEntityIds;

    /** The locales present within the edited content. */
    private Set<String> m_contentLocales;

    /** The copy locale button. */
    private CmsPushButton m_copyLocaleButton;

    /** The entities to delete. */
    private Set<String> m_deletedEntities;

    /** The delete locale button. */
    private CmsPushButton m_deleteLocaleButton;

    /** The id of the edited entity. */
    private String m_entityId;

    /** The current content locale. */
    private String m_locale;

    /** The locale select label. */
    private CmsLabel m_localeLabel;

    /** The locale select box. */
    private CmsSelectBox m_localeSelect;

    /** The on close call back. */
    private Command m_onClose;

    /** The open form button. */
    private CmsPushButton m_openFormButton;

    /** The registered entity id's. */
    private Set<String> m_registeredEntities;

    /** The resource type name. */
    private String m_resourceTypeName;

    /** The save button. */
    private CmsPushButton m_saveButton;

    /** The resource site path. */
    private String m_sitePath;

    /** The resource title. */
    private String m_title;

    /**
     * Constructor.<p>
     */
    private CmsInlineEditor() {

        I_LayoutBundle.INSTANCE.form().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.editorCss().ensureInjected();
        I_CmsContentServiceAsync service = GWT.create(I_CmsContentService.class);
        String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsContentService.gwt");
        ((ServiceDefTarget)service).setServiceEntryPoint(serviceUrl);
        m_editor = new CmsEditorBase(service);
        m_changedEntityIds = new HashSet<String>();
        m_registeredEntities = new HashSet<String>();
        m_availableLocales = new HashMap<String, String>();
        m_contentLocales = new HashSet<String>();
        m_deletedEntities = new HashSet<String>();
    }

    /**
     * Returns the in-line editor instance.<p>
     * 
     * @return the in-line editor instance
     */
    public static CmsInlineEditor getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsInlineEditor();
        }
        return INSTANCE;
    }

    /**
     * Opens the content editor dialog.<p>
     * 
     * @param locale the content locale
     * @param elementId the element id
     * @param onClose the command executed on dialog close
     */
    public void openFormEditor(final String locale, String elementId, final Command onClose) {

        m_entityId = CmsContentDefinition.uuidToEntityId(new CmsUUID(elementId), locale);
        m_locale = locale;
        m_onClose = onClose;
        m_editor.loadDefinition(m_entityId, new I_CmsSimpleCallback<CmsContentDefinition>() {

            public void execute(CmsContentDefinition contentDefinition) {

                initEditor(contentDefinition, null, false);
            }
        });
    }

    /**
     * Renders the in-line editor for the given element.<p>
     *  
     * @param locale the content locale
     * @param panel the element panel
     * @param onClose the command to execute on close
     */
    public void openInlineEditor(final String locale, final ComplexPanel panel, final Command onClose) {

        m_entityId = panel.getElement().getAttribute("about");
        m_locale = locale;
        m_onClose = onClose;
        m_editor.loadDefinition(m_entityId, new I_CmsSimpleCallback<CmsContentDefinition>() {

            public void execute(CmsContentDefinition contentDefinition) {

                initEditor(contentDefinition, panel, true);
            }
        });
    }

    /**
     * Cancels the editing process.<p>
     */
    void cancelEdit() {

        m_onClose.execute();
        m_editor.destroyFrom(true);
        clearEditor();
    }

    /**
     * Copies the current entity values to the given locales.<p>
     * 
     * @param targetLocales the target locales
     */
    void copyLocales(Set<String> targetLocales) {

        for (String targetLocale : targetLocales) {
            String targetId = getIdForLocale(targetLocale);
            if (!m_entityId.equals(targetId)) {
                if (m_registeredEntities.contains(targetId)) {
                    m_editor.unregistereEntity(targetId);
                }
                m_editor.registerClonedEntity(m_entityId, targetId);
                m_registeredEntities.add(targetId);
                m_changedEntityIds.add(targetId);
                m_contentLocales.add(targetLocale);
                m_deletedEntities.remove(targetId);
            }
        }
        initLocaleSelect();
    }

    /**
     * Deletes the current locale.<p>
     */
    void deleteCurrentLocale() {

        // there has to remain at least one content locale
        if (m_contentLocales.size() > 1) {
            String deletedLocale = m_locale;
            m_contentLocales.remove(deletedLocale);
            m_registeredEntities.remove(m_entityId);
            m_changedEntityIds.remove(m_entityId);
            m_deletedEntities.add(m_entityId);
            m_editor.unregistereEntity(m_entityId);
            m_saveButton.enable();
            String nextLocale = null;
            if (m_registeredEntities.isEmpty()) {
                nextLocale = m_contentLocales.iterator().next();
            } else {
                nextLocale = CmsContentDefinition.getLocaleFromId(m_registeredEntities.iterator().next());
            }
            switchLocale(nextLocale);
        }
    }

    /**
     * Initializes the editor.<p>
     * 
     * @param contentDefinition the content definition
     * @param panel the associated content panel, needed for inline editing only
     * @param inline <code>true</code> to render the editor for inline editing
     */
    void initEditor(CmsContentDefinition contentDefinition, ComplexPanel panel, boolean inline) {

        setContentDefinition(contentDefinition);
        initToolbar();
        if (inline && (panel != null)) {
            m_editor.renderInlineEntity(m_entityId, panel.getElement());
        } else {
            initFormPanel();
            renderFormContent();
        }
    }

    /**
     * Opens the form based editor.<p>
     */
    void initFormPanel() {

        m_openFormButton.getElement().getStyle().setDisplay(Display.NONE);
        m_basePanel = new FlowPanel();
        // insert base panel before the tool bar too keep the tool bar visible 
        RootPanel.get().insert(m_basePanel, RootPanel.get().getWidgetIndex(m_toolbar));
    }

    /**
     * Opens the copy locale dialog.<p>
     */
    void openCopyLocaleDialog() {

        CmsCopyLocaleDialog dialog = new CmsCopyLocaleDialog(m_availableLocales, m_contentLocales, m_locale, this);
        dialog.center();
    }

    /**
     * Renders the form content.<p>
     */
    void renderFormContent() {

        initLocaleSelect();
        CmsInfoHeader header = new CmsInfoHeader(
            m_title,
            null,
            m_sitePath,
            m_locale,
            CmsIconUtil.getResourceIconClasses(m_resourceTypeName, m_sitePath, false));
        m_basePanel.add(header);
        m_basePanel.addStyleName(I_CmsLayoutBundle.INSTANCE.editorCss().basePanel());
        CmsScrollPanel content = GWT.create(CmsScrollPanel.class);
        content.addStyleName(I_CmsLayoutBundle.INSTANCE.editorCss().contentPanel());
        content.addStyleName(I_LayoutBundle.INSTANCE.form().formParent());
        m_basePanel.add(content);
        content.getElement().getStyle().setProperty(
            "maxHeight",
            Window.getClientHeight() - (100 + header.getOffsetHeight()),
            Unit.PX);
        m_editor.renderEntityForm(m_entityId, content);
    }

    /**
     * Saves the content and closes the editor.<p> 
     */
    void save() {

        m_editor.saveAndDeleteEntities(m_changedEntityIds, m_deletedEntities, true, m_onClose);
        clearEditor();
    }

    /**
     * Sets the has changed flag and enables the save button.<p>
     */
    void setChanged() {

        m_saveButton.enable();
        m_changedEntityIds.add(m_entityId);
        m_deletedEntities.remove(m_entityId);
    }

    /**
     * Sets the content definition.<p>
     * 
     * @param definition the content definition
     */
    void setContentDefinition(CmsContentDefinition definition) {

        m_availableLocales.putAll(definition.getAvailableLocales());
        m_contentLocales.addAll(definition.getContentLocales());
        m_title = definition.getTitle();
        m_sitePath = definition.getSitePath();
        m_resourceTypeName = definition.getResourceType();
        m_registeredEntities.add(definition.getEntityId());
        m_editor.addEntityChangeHandler(definition.getEntityId(), new ValueChangeHandler<I_Entity>() {

            public void onValueChange(ValueChangeEvent<I_Entity> event) {

                setChanged();
            }
        });
    }

    /**
     * Switches to the selected locale. Will save changes first.<p>
     * 
     * @param locale the locale to switch to
     */
    void switchLocale(final String locale) {

        m_locale = locale;
        m_basePanel.clear();
        m_editor.destroyFrom(false);
        m_entityId = getIdForLocale(locale);
        // if the content does not contain the requested locale yet, a new node will be created 
        final boolean addedNewLocale = !m_contentLocales.contains(locale);
        if (!m_registeredEntities.contains(m_entityId)) {
            I_CmsSimpleCallback<CmsContentDefinition> callback = new I_CmsSimpleCallback<CmsContentDefinition>() {

                public void execute(CmsContentDefinition contentDefinition) {

                    setContentDefinition(contentDefinition);
                    renderFormContent();
                    if (addedNewLocale) {
                        setChanged();
                    }
                }
            };
            if (addedNewLocale) {
                m_editor.loadNewDefinition(m_entityId, callback);
            } else {
                m_editor.loadDefinition(m_entityId, callback);
            }
        } else {
            renderFormContent();
        }
    }

    /**
     * Closes the editor.<p>
     */
    private void clearEditor() {

        m_toolbar.removeFromParent();
        m_toolbar = null;
        m_cancelButton = null;
        m_localeSelect = null;
        m_openFormButton = null;
        m_saveButton = null;
        m_entityId = null;
        m_onClose = null;
        m_locale = null;
        if (m_basePanel != null) {
            m_basePanel.removeFromParent();
            m_basePanel = null;
        }
        m_changedEntityIds.clear();
        m_registeredEntities.clear();
        m_availableLocales.clear();
        m_contentLocales.clear();
        m_deletedEntities.clear();
        m_title = null;
        m_sitePath = null;
        m_resourceTypeName = null;
    }

    /**
     * Creates a push button for the edit tool-bar.<p>
     * 
     * @param title the button title
     * @param imageClass the image class
     * 
     * @return the button
     */
    private CmsPushButton createButton(String title, String imageClass) {

        CmsPushButton result = new CmsPushButton();
        result.setTitle(title);
        result.setImageClass(imageClass);
        result.setButtonStyle(ButtonStyle.IMAGE, null);
        result.setSize(Size.big);
        return result;
    }

    /**
     * Returns the entity id for the given locale.<p>
     * 
     * @param locale the locale
     * 
     * @return the entity id
     */
    private String getIdForLocale(String locale) {

        return CmsContentDefinition.uuidToEntityId(CmsContentDefinition.entityIdToUuid(m_entityId), locale);
    }

    /**
     * Initializes the locale selector.<p>
     */
    private void initLocaleSelect() {

        if (m_localeSelect != null) {
            m_localeSelect.removeFromParent();
        }
        if (m_deleteLocaleButton == null) {
            m_deleteLocaleButton = createButton(
                Messages.get().key(Messages.GUI_TOOLBAR_DELETE_LOCALE_0),
                I_CmsButton.ButtonData.DELETE.getIconClass());
            m_deleteLocaleButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    deleteCurrentLocale();
                }
            });
        } else {
            m_deleteLocaleButton.removeFromParent();
        }
        if (m_copyLocaleButton == null) {
            m_copyLocaleButton = createButton(
                Messages.get().key(Messages.GUI_TOOLBAR_COPY_LOCALE_0),
                I_CmsButton.ButtonData.ADD.getIconClass());
            m_copyLocaleButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    openCopyLocaleDialog();
                }
            });
        } else {
            m_copyLocaleButton.removeFromParent();
        }
        if (m_localeLabel == null) {
            m_localeLabel = new CmsLabel();
            m_localeLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().inlineBlock());
            m_localeLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textBig());
            m_localeLabel.setText(Messages.get().key(Messages.GUI_TOOLBAR_LANGUAGE_0));
            m_toolbar.addLeft(m_localeLabel);
        }
        Map<String, String> selectOptions = new HashMap<String, String>();
        for (Entry<String, String> localeEntry : m_availableLocales.entrySet()) {
            if (m_contentLocales.contains(localeEntry.getKey())) {
                selectOptions.put(localeEntry.getKey(), localeEntry.getValue());
            } else {
                selectOptions.put(localeEntry.getKey(), localeEntry.getValue() + " [-]");
            }
        }
        m_localeSelect = new CmsSelectBox(selectOptions);
        m_localeSelect.setFormValueAsString(m_locale);
        m_localeSelect.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().inlineBlock());
        m_localeSelect.getElement().getStyle().setWidth(100, Unit.PX);
        m_localeSelect.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        m_localeSelect.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                switchLocale(event.getValue());
            }
        });
        m_toolbar.addLeft(m_localeSelect);
        if (m_contentLocales.size() > 1) {
            m_toolbar.addLeft(m_deleteLocaleButton);
        }
        if (m_availableLocales.size() > 1) {
            m_toolbar.addLeft(m_copyLocaleButton);
        }
    }

    /**
     * Generates the button bar displayed beneath the editable fields.<p>
     */
    private void initToolbar() {

        m_toolbar = new CmsToolbar();
        m_saveButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_SAVE_0),
            I_CmsButton.ButtonData.SAVE.getIconClass());
        m_saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                save();
            }
        });
        m_saveButton.disable(Messages.get().key(Messages.GUI_TOOLBAR_NOTHING_CHANGED_0));
        m_toolbar.addLeft(m_saveButton);
        m_cancelButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_RESET_0),
            I_CmsButton.ButtonData.RESET.getIconClass());
        m_cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                cancelEdit();

            }
        });
        m_toolbar.addRight(m_cancelButton);

        m_openFormButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_OPEN_FORM_0),
            I_CmsButton.ButtonData.EDIT.getIconClass());
        m_openFormButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                initFormPanel();
                renderFormContent();
            }
        });
        m_toolbar.addLeft(m_openFormButton);
        RootPanel.get().add(m_toolbar);
    }
}
