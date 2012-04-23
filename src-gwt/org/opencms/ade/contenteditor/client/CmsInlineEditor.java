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
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.user.client.ui.Widget;

/**
 * The in-line content editor.<p>
 */
public class CmsInlineEditor {

    /** The in-line editor instance. */
    private static CmsInlineEditor INSTANCE;

    /** The editor base. */
    protected CmsEditorBase m_editor;

    /** The edit tool-bar. */
    protected CmsToolbar m_toolbar;

    /** The form editing base panel. */
    private FlowPanel m_basePanel;

    /** The cancel button. */
    private CmsPushButton m_cancelButton;

    /** The id of the edited entity. */
    private String m_entityId;

    /** The current content locale. */
    private String m_locale;

    /** The locale select box. */
    private CmsSelectBox m_localeSelect;

    /** The on close call back. */
    private Command m_onClose;

    /** The open form button. */
    private CmsPushButton m_openFormButton;

    /** The save button. */
    private CmsPushButton m_saveButton;

    /**
     * Constructor.<p>
     */
    public CmsInlineEditor() {

        I_LayoutBundle.INSTANCE.form().ensureInjected();
        I_CmsContentServiceAsync service = GWT.create(I_CmsContentService.class);
        String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsContentService.gwt");
        ((ServiceDefTarget)service).setServiceEntryPoint(serviceUrl);
        m_editor = new CmsEditorBase(service);
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
    public void openContentEditorDialog(final String locale, String elementId, final Command onClose) {

        final String entityId = "http://opencms.org/resources/" + elementId;
        m_editor.loadDefinition(entityId, locale, new I_CmsSimpleCallback<CmsContentDefinition>() {

            public void execute(CmsContentDefinition contentDefinition) {

                openForm(entityId, locale, onClose);

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
    public void renderInlineEditor(final String locale, final ComplexPanel panel, final Command onClose) {

        final String entityId = panel.getElement().getAttribute("about");

        m_editor.loadDefinition(entityId, locale, new I_CmsSimpleCallback<CmsContentDefinition>() {

            public void execute(CmsContentDefinition contentDefinition) {

                initForm(entityId, locale, panel, onClose);
            }
        });
    }

    /**
     * Cancels the editing process.<p>
     */
    void cancelEdit() {

        m_onClose.execute();
        m_editor.destroyFrom();
        closeEditor();
    }

    /**
     * Initializes the in-line form.<p>
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param panel the element panel
     * @param onClose the command to execute on close
     */
    void initForm(final String entityId, final String locale, ComplexPanel panel, final Command onClose) {

        m_entityId = entityId;
        m_locale = locale;
        m_onClose = onClose;
        m_editor.renderInlineEntity(entityId, panel.getElement());
        initToolbar();

        m_editor.addEntityChangeHandler(entityId, new ValueChangeHandler<I_Entity>() {

            public void onValueChange(ValueChangeEvent<I_Entity> event) {

                for (Widget button : m_toolbar.getAll()) {
                    ((CmsPushButton)button).enable();
                }
            }
        });
    }

    /**
     * Opens the form based editor.<p>
     */
    void openForm() {

        I_CmsLayoutBundle.INSTANCE.editorCss().ensureInjected();
        m_openFormButton.getElement().getStyle().setDisplay(Display.NONE);
        m_basePanel = new FlowPanel();
        m_basePanel.addStyleName(I_CmsLayoutBundle.INSTANCE.editorCss().basePanel());
        CmsScrollPanel content = GWT.create(CmsScrollPanel.class);
        content.getElement().getStyle().setProperty("maxHeight", Window.getClientHeight() - 100, Unit.PX);
        content.addStyleName(I_CmsLayoutBundle.INSTANCE.editorCss().contentPanel());
        content.addStyleName(I_LayoutBundle.INSTANCE.form().formParent());
        m_basePanel.add(content);
        // insert base panel before the tool bar too keep the tool bar visible 
        RootPanel.get().insert(m_basePanel, RootPanel.get().getWidgetIndex(m_toolbar));
        m_editor.renderEntityForm(m_entityId, content);
    }

    /**
     * Opens the form based editor.<p>
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param onClose the on close command
     */
    void openForm(final String entityId, final String locale, final Command onClose) {

        m_entityId = entityId;
        m_locale = locale;
        m_onClose = onClose;
        initToolbar();
        openForm();

    }

    /**
     * Saves the content and closes the editor.<p> 
     */
    void save() {

        m_editor.saveEntity(m_entityId, m_locale, true, m_onClose);
        closeEditor();
    }

    /**
     * Closes the editor.<p>
     */
    private void closeEditor() {

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
     * Generates the button bar displayed beneath the editable fields.<p>
     */
    private void initToolbar() {

        m_toolbar = new CmsToolbar();
        m_saveButton = createButton("Save", I_CmsButton.ButtonData.SAVE.getIconClass());
        m_saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                save();
            }
        });
        m_saveButton.disable("Nothing changed yet");
        m_toolbar.addLeft(m_saveButton);

        m_cancelButton = createButton("Cancel", I_CmsButton.ButtonData.RESET.getIconClass());
        m_cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                cancelEdit();

            }
        });
        m_toolbar.addRight(m_cancelButton);

        m_openFormButton = createButton("Open form", I_CmsButton.ButtonData.EDIT.getIconClass());
        m_openFormButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                openForm();

            }
        });
        m_toolbar.addLeft(m_openFormButton);
        RootPanel.get().add(m_toolbar);
        m_toolbar.getElement().getStyle().setDisplay(Display.BLOCK);
    }
}
