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
import com.alkacon.acacia.shared.ContentDefinition;
import com.alkacon.vie.shared.I_Entity;

import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsPushButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Content editor entry point.<p>
 */
public class CmsContentEditor extends A_CmsEntryPoint {

    /** The save disabled message. */
    private static final String DISABLE_SAVE = "Nothing to save yet.";

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        I_LayoutBundle.INSTANCE.form().ensureInjected();
        I_CmsContentServiceAsync service = GWT.create(I_CmsContentService.class);
        String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsContentService.gwt");
        ((ServiceDefTarget)service).setServiceEntryPoint(serviceUrl);
        final CmsEditorBase editor = new CmsEditorBase(service);
        ContentDefinition definition = null;
        try {
            definition = (ContentDefinition)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                editor.getService(),
                I_CmsContentService.DICT_CONTENT_DEFINITION);
        } catch (SerializationException e) {
            RootPanel.get().add(new Label(e.getMessage()));
            return;
        }

        editor.registerContentDefinition(definition);
        final String entityId = definition.getEntityId();

        final String locale = definition.getLocale();
        FlowPanel panel = new FlowPanel();
        RootPanel.get().add(panel);
        final CmsPushButton saveButton = new CmsPushButton();
        saveButton.setText("Save");
        saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                editor.saveEntity(entityId, locale, new Command() {

                    public void execute() {

                        saveButton.disable(DISABLE_SAVE);
                        closeEditor();
                    }
                });
            }
        });
        saveButton.disable(DISABLE_SAVE);
        panel.add(saveButton);
        Element parent = DOM.createDiv();
        RootPanel.getBodyElement().appendChild(parent);
        editor.renderEntity(entityId, parent, false);
        editor.addEntityChangeHandler(entityId, new ValueChangeHandler<I_Entity>() {

            public void onValueChange(ValueChangeEvent<I_Entity> event) {

                saveButton.enable();
            }
        });
    }

    /**
     * Closes the editor.<p>
     */
    protected native void closeEditor() /*-{
        if ($wnd.top.cms_ade_closeEditorDialog) {
            $wnd.top.cms_ade_closeEditorDialog();
        } else {
            var backlink = $wnd[@org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService::PARAM_BACKLINK];
            if (backlink) {
                $wnd.location.href = backlink;
            }
        }
    }-*/;
}
