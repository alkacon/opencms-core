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

import com.alkacon.vie.shared.I_Entity;

import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsPushButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The in-line content editor.<p>
 */
public class CmsInlineEditor {

    /** The in-line editor instance. */
    private static CmsInlineEditor INSTANCE;

    /** The editor base. */
    protected CmsEditorBase m_editor;

    /**
     * Constructor.<p>
     */
    public CmsInlineEditor() {

        I_CmsContentServiceAsync service = GWT.create(I_CmsContentService.class);
        String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsContentService.gwt");
        ((ServiceDefTarget)service).setServiceEntryPoint(serviceUrl);
        m_editor = new CmsEditorBase(service, true);
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
     * Renders the in-line editor for the given element.<p>
     *  
     * @param locale the content locale
     * @param panel the element panel
     * @param onClose the command to execute on close
     * @param openForm the command to use to open the form based editor
     */
    public void renderInlineEditor(
        final String locale,
        final ComplexPanel panel,
        final Command onClose,
        final Command openForm) {

        final String entityId = panel.getElement().getAttribute("about");

        m_editor.loadContentDefinition(entityId, locale, new Command() {

            public void execute() {

                initForm(entityId, locale, panel, onClose, openForm);
            }
        });
    }

    /**
     * Generates the button bar displayed beneath the editable fields.<p>
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param onClose the on close command
     * @param openForm the command to open the form based editor
     * 
     * @return the button bar
     */
    protected FlowPanel generateButtonBar(
        final String entityId,
        final String locale,
        final Command onClose,
        final Command openForm) {

        FlowPanel buttonBar = new FlowPanel();
        CmsPushButton saveButton = new CmsPushButton();
        saveButton.setText("Save");
        saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_editor.saveEntity(entityId, locale, true, onClose);
            }
        });
        saveButton.disable("Nothing changed yet");
        saveButton.getElement().getStyle().setMarginRight(5, Unit.PX);
        buttonBar.add(saveButton);

        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setText("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                onClose.execute();
                m_editor.clearVie();
            }
        });
        cancelButton.getElement().getStyle().setMarginRight(5, Unit.PX);
        buttonBar.add(cancelButton);

        CmsPushButton formButton = new CmsPushButton();
        formButton.setText("Open form");
        formButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                openForm.execute();
                onClose.execute();
                m_editor.clearVie();
            }
        });
        buttonBar.add(formButton);
        return buttonBar;
    }

    /**
     * Initializes the in-line form.<p>
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param panel the element panel
     * @param onClose the command to execute on close
     * @param openForm the command to use to open the form based editor
     */
    protected void initForm(
        final String entityId,
        final String locale,
        ComplexPanel panel,
        final Command onClose,
        final Command openForm) {

        m_editor.renderEntity(entityId, panel.getElement());
        final FlowPanel buttonBar = generateButtonBar(entityId, locale, onClose, openForm);
        final CmsPushButton saveButton = new CmsPushButton();
        saveButton.setText("Save");
        saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_editor.saveEntity(entityId, locale, true, onClose);

            }
        });
        saveButton.disable("Nothing changed yet");
        panel.add(buttonBar);
        m_editor.addEntityChangeHandler(entityId, new ValueChangeHandler<I_Entity>() {

            public void onValueChange(ValueChangeEvent<I_Entity> event) {

                for (Widget button : buttonBar) {
                    ((CmsPushButton)button).enable();
                }
            }
        });
    }
}
