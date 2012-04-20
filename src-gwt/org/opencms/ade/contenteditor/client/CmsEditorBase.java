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

import com.alkacon.acacia.client.EditorBase;
import com.alkacon.acacia.client.I_WidgetFactory;
import com.alkacon.acacia.client.widgets.HTMLWidget;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.StringWidget;
import com.alkacon.acacia.shared.ContentDefinition;
import com.alkacon.vie.shared.I_Entity;

import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Command;

/**
 * The content editor base.<p>
 */
public class CmsEditorBase extends EditorBase {

    /**
     * Constructor.<p>
     * 
     * @param service the content service 
     */
    public CmsEditorBase(I_CmsContentServiceAsync service) {

        super(service);
        Map<String, I_WidgetFactory> widgetFactories = new HashMap<String, I_WidgetFactory>();
        widgetFactories.put("org.opencms.widgets.CmsInputWidget", new I_WidgetFactory() {

            public I_EditWidget createWidget(String configuration) {

                I_EditWidget widget = new StringWidget();
                widget.setConfiguration(configuration);
                return widget;
            }
        });
        widgetFactories.put("org.opencms.widgets.CmsHtmlWidget", new I_WidgetFactory() {

            public I_EditWidget createWidget(String configuration) {

                I_EditWidget widget = new HTMLWidget();
                widget.setConfiguration(configuration);
                return widget;
            }
        });
        getWidgetService().setWidgetFactories(widgetFactories);
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     * 
     * @param entityId the entity id
     * @param locale the content locale
     * @param callback the callback
     */
    @Override
    public void loadContentDefinition(final String entityId, final String locale, final Command callback) {

        CmsRpcAction<ContentDefinition> action = new CmsRpcAction<ContentDefinition>() {

            @Override
            public void execute() {

                getService().loadContentDefinition(entityId, locale, this);
            }

            @Override
            protected void onResponse(ContentDefinition result) {

                registerContentDefinition(result);
                callback.execute();
            }
        };
        action.execute();
    }

    /**
     * Saves the given entity.<p>
     * 
     * @param entity the entity
     * @param locale the content locale
     * @param clearOnSuccess <code>true</code> to clear all entities from VIE on success
     * @param callback the callback executed on success
     */
    @Override
    public void saveEntity(
        final I_Entity entity,
        final String locale,
        final boolean clearOnSuccess,
        final Command callback) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(0, true);
                getService().saveEntity(com.alkacon.acacia.shared.Entity.serializeEntity(entity), locale, this);

            }

            @Override
            protected void onResponse(Void result) {

                callback.execute();
                if (clearOnSuccess) {
                    destroyFrom();
                }
                stop(true);
            }
        };
        action.execute();
    }
}
