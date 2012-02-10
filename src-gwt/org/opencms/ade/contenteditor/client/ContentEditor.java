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

import com.alkacon.acacia.client.ComplexTypeRenderer;
import com.alkacon.acacia.client.I_EntityRenderer;
import com.alkacon.acacia.client.I_WidgetFactory;
import com.alkacon.acacia.client.WidgetService;
import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.StringWidget;
import com.alkacon.acacia.client.widgets.TinyMCEWidget;
import com.alkacon.acacia.shared.ContentDefinition;
import com.alkacon.vie.client.Entity;
import com.alkacon.vie.client.I_Vie;
import com.alkacon.vie.client.Vie;
import com.alkacon.vie.shared.I_Entity;

import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Content editor entry point.<p>
 */
public class ContentEditor implements EntryPoint {

    /** The content service instance. */
    private static I_CmsContentServiceAsync SERVICE;

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    public void onModuleLoad() {

        I_LayoutBundle.INSTANCE.style().ensureInjected();

        I_Vie vie = Vie.getInstance();
        ContentDefinition definition = null;
        try {
            definition = (ContentDefinition)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                getService(),
                I_CmsContentService.DICT_CONTENT_DEFINITION);
        } catch (SerializationException e) {
            RootPanel.get().add(new Label(e.getMessage()));
            return;
        }

        I_Entity entity = WidgetService.register(vie, definition);

        WidgetService service = new WidgetService();
        service.init(definition);
        service.registerWidgetFactory("string", new I_WidgetFactory() {

            public I_EditWidget createWidget(String configuration) {

                return new StringWidget();
            }
        });
        service.registerWidgetFactory("tinymce", new I_WidgetFactory() {

            public I_EditWidget createWidget(String configuration) {

                return new TinyMCEWidget(null);
            }
        });

        I_EntityRenderer inlineRenderer = new ComplexTypeRenderer(service, vie);
        service.setDefaultComplexRenderer(inlineRenderer);
        service.setDefaultSimpleRenderer(inlineRenderer);
        I_EntityRenderer renderer = service.getRendererForType(vie.getType(entity.getTypeName()));
        renderer.render(entity, RootPanel.getBodyElement());

        ((Entity)entity).addValueChangeHandler(new ValueChangeHandler<I_Entity>() {

            public void onValueChange(ValueChangeEvent<I_Entity> event) {

                RootPanel.get().getElement().getStyle().setBackgroundColor("#77f3f3");
                RootPanel.get().add(new Label(event.getValue().toJSON()));
            }
        });

    }

    /**
     * Returns the content service instance.<p>
     * 
     * @return the content service
     */
    protected I_CmsContentServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsContentService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsContentService.gwt");
            ((ServiceDefTarget)SERVICE).setServiceEntryPoint(serviceUrl);
        }
        return SERVICE;
    }

}
