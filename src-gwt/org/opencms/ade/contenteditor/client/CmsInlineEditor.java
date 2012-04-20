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

import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
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

    /** the edit tool-bar. */
    protected CmsToolbar m_toolbar;

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

        final String id = "http://opencms.org/resources/" + elementId;
        m_editor.loadContentDefinition(id, locale, new Command() {

            public void execute() {

                openForm(id, locale, onClose);

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

        m_editor.loadContentDefinition(entityId, locale, new Command() {

            public void execute() {

                initForm(entityId, locale, panel, onClose);
            }
        });
    }

    /**
     * Generates the button bar displayed beneath the editable fields.<p>
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param onClose the on close command
     * 
     * @return the button bar
     */
    protected CmsToolbar generateButtonBar(final String entityId, final String locale, final Command onClose) {

        CmsToolbar toolbar = new CmsToolbar();
        CmsPushButton saveButton = createButton("Save", I_CmsButton.ButtonData.SAVE.getIconClass());
        saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_editor.saveEntity(entityId, locale, true, onClose);
                m_toolbar.removeFromParent();
            }
        });
        saveButton.disable("Nothing changed yet");
        toolbar.addLeft(saveButton);

        CmsPushButton cancelButton = createButton("Cancel", I_CmsButton.ButtonData.RESET.getIconClass());
        cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                onClose.execute();
                m_editor.destroyFrom();
                m_toolbar.removeFromParent();
            }
        });
        toolbar.addRight(cancelButton);

        CmsPushButton formButton = createButton("Open form", I_CmsButton.ButtonData.EDIT.getIconClass());
        formButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_toolbar.removeFromParent();
                openForm(entityId, locale, onClose);

            }
        });
        toolbar.addLeft(formButton);
        return toolbar;
    }

    /**
     * Initializes the in-line form.<p>
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param panel the element panel
     * @param onClose the command to execute on close
     */
    protected void initForm(final String entityId, final String locale, ComplexPanel panel, final Command onClose) {

        m_editor.renderInlineEntity(entityId, panel.getElement());
        m_toolbar = generateButtonBar(entityId, locale, onClose);
        RootPanel.get().add(m_toolbar);
        m_toolbar.getElement().getStyle().setDisplay(Display.BLOCK);
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
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param onClose the on close command
     */
    protected void openForm(final String entityId, final String locale, final Command onClose) {

        final CmsPopup popup = new CmsPopup("Editor");
        popup.setSpecialBackgroundClass(I_LayoutBundle.INSTANCE.form().formParent());
        popup.setGlassEnabled(true);
        popup.addDialogClose(new Command() {

            public void execute() {

                onClose.execute();
                m_editor.destroyFrom();
            }
        });
        popup.setWidth(600);
        final CmsPushButton saveButton = new CmsPushButton();
        saveButton.setText("Save");
        saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_editor.saveEntity(entityId, locale, true, onClose);
                m_editor.destroyFrom();
                popup.hide();
            }
        });

        final CmsPushButton closeButton = new CmsPushButton();
        closeButton.setText("Cancel");
        closeButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                onClose.execute();
                m_editor.destroyFrom();
                popup.hide();
            }
        });

        popup.addButton(closeButton);
        popup.addButton(saveButton);
        FlowPanel content = new FlowPanel();
        content.getElement().getStyle().setProperty("maxHeight", popup.getAvailableHeight(0), Unit.PX);
        content.getElement().getStyle().setOverflow(Overflow.AUTO);
        content.getElement().getStyle().setPosition(Position.RELATIVE);
        popup.add(content);
        popup.centerHorizontally(50);
        m_editor.renderEntityForm(entityId, content);
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
}
