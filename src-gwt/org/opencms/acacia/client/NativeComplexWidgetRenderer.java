/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client;

import org.opencms.acacia.client.css.I_LayoutBundle;
import org.opencms.acacia.client.entity.I_Vie;
import org.opencms.acacia.client.entity.Vie;
import org.opencms.acacia.shared.ContentDefinition;
import org.opencms.acacia.shared.Entity;
import org.opencms.acacia.shared.EntityAttribute;
import org.opencms.acacia.shared.TabInfo;
import org.opencms.gwt.client.ui.CmsTabbedPanel;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Renderer which delegates the rendering of an entity to native Javascript.
 * 
 * This renderer will interpret its configuration string as a JSON object (which we will call 'config').
 * To render an entity, it will take the name of a function from config.render and then call the function
 * with the entity to render, the parent element, a VIE wrapper, and the configuration object as parameters. 
 */
public class NativeComplexWidgetRenderer implements I_EntityRenderer {

    /** The entity CSS class. */
    public static final String ENTITY_CLASS = I_LayoutBundle.INSTANCE.form().entity();

    /** The attribute label CSS class. */
    public static final String LABEL_CLASS = I_LayoutBundle.INSTANCE.form().label();

    /** The widget holder CSS class. */
    public static final String WIDGET_HOLDER_CLASS = I_LayoutBundle.INSTANCE.form().widgetHolder();

    /** The configuration string. */
    private String m_configuration;

    /** The parsed JSON value from the configuration string. */
    private JSONObject m_jsonConfig;

    /** The native renderer instance. */
    private JavaScriptObject m_nativeInstance;

    /** 
     * Default constructor.<p>
     */
    public NativeComplexWidgetRenderer() {

    }

    /**
     * Creates a new configured instance.<p>
     * 
     * @param configuration the configuration string 
     */
    public NativeComplexWidgetRenderer(String configuration) {

        m_configuration = configuration;
        m_jsonConfig = JSONParser.parseLenient(configuration).isObject();
    }

    /**
     * @see org.opencms.acacia.client.I_EntityRenderer#configure(java.lang.String)
     */
    public NativeComplexWidgetRenderer configure(String configuration) {

        return new NativeComplexWidgetRenderer(configuration);
    }

    /**
     * @see org.opencms.acacia.client.I_EntityRenderer#getName()
     */
    public String getName() {

        return ContentDefinition.NATIVE_RENDERER;
    }

    /**
     * @see org.opencms.acacia.client.I_EntityRenderer#renderAttributeValue(org.opencms.acacia.shared.Entity, org.opencms.acacia.client.AttributeHandler, int, com.google.gwt.user.client.ui.Panel)
     */
    public void renderAttributeValue(
        Entity parentEntity,
        AttributeHandler attributeHandler,
        int attributeIndex,
        Panel context) {

        notSupported();
    }

    /**
     * @see org.opencms.acacia.client.I_EntityRenderer#renderForm(org.opencms.acacia.shared.Entity, java.util.List, com.google.gwt.user.client.ui.Panel, org.opencms.acacia.client.I_AttributeHandler, int)
     */
    public CmsTabbedPanel<FlowPanel> renderForm(
        Entity entity,
        List<TabInfo> tabInfos,
        Panel context,
        I_AttributeHandler parentHandler,
        int attributeIndex) {

        throw new UnsupportedOperationException("Custom renderer does not support tabs!");

    }

    /**
     * @see org.opencms.acacia.client.I_EntityRenderer#renderForm(org.opencms.acacia.shared.Entity, com.google.gwt.user.client.ui.Panel, org.opencms.acacia.client.I_AttributeHandler, int)
     */
    public void renderForm(
        final Entity entity,
        Panel context,
        final I_AttributeHandler parentHandler,
        final int attributeIndex) {

        context.addStyleName(ENTITY_CLASS);
        context.getElement().setAttribute("typeof", entity.getTypeName());
        context.getElement().setAttribute("about", entity.getId());
        String initFunction = ContentDefinition.FUNCTION_RENDER_FORM;
        renderNative(
            getNativeInstance(),
            initFunction,
            context.getElement(),
            entity,
            Vie.getInstance(),
            m_jsonConfig.isObject().getJavaScriptObject());
    }

    /**
     * @see org.opencms.acacia.client.I_EntityRenderer#renderInline(org.opencms.acacia.shared.Entity, org.opencms.acacia.client.I_InlineFormParent, org.opencms.acacia.client.I_InlineHtmlUpdateHandler)
     */
    public void renderInline(Entity entity, I_InlineFormParent formParent, I_InlineHtmlUpdateHandler updateHandler) {

        notSupported();
    }

    /**
     * @see org.opencms.acacia.client.I_EntityRenderer#renderInline(org.opencms.acacia.shared.Entity, java.lang.String, org.opencms.acacia.client.I_InlineFormParent, org.opencms.acacia.client.I_InlineHtmlUpdateHandler, int, int)
     */
    public void renderInline(
        Entity parentEntity,
        String attributeName,
        I_InlineFormParent formParent,
        I_InlineHtmlUpdateHandler updateHandler,
        int minOccurrence,
        int maxOccurrence) {

        EntityAttribute attribute = parentEntity.getAttribute(attributeName);
        String renderInline = ContentDefinition.FUNCTION_RENDER_INLINE;
        if (attribute != null) {
            List<Entity> values = attribute.getComplexValues();
            List<Element> elements = Vie.getInstance().getAttributeElements(
                parentEntity,
                attributeName,
                formParent.getElement());
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                if (i < values.size()) {
                    Entity value = values.get(i);
                    renderNative(
                        getNativeInstance(),
                        renderInline,
                        element,
                        value,
                        Vie.getInstance(),
                        m_jsonConfig.getJavaScriptObject());
                }
            }
        }
    }

    /** 
     * Creates the native renderer instance.<p>
     * 
     * @param initCall the name of the native function which creates the native renderer instance  
     * 
     * @return the native renderer instance 
     */
    protected native JavaScriptObject createNativeInstance(String initCall) /*-{
                                                                            if ($wnd[initCall]) {
                                                                            return $wnd[initCall]();
                                                                            } else {
                                                                            throw ("No init function found: " + initCall);
                                                                            }
                                                                            }-*/;

    /** 
     * Gets the native renderer instance.<p>
     * 
     * @return the native renderer instance 
     */
    protected JavaScriptObject getNativeInstance() {

        if (m_nativeInstance == null) {
            m_nativeInstance = createNativeInstance(m_jsonConfig.get(ContentDefinition.PARAM_INIT_CALL).isString().stringValue());
        }
        return m_nativeInstance;
    }

    /**
     * Calls the native render function.<p>
     * 
     * @param nativeRenderer the native renderer instance 
     * @param renderFunction the name of the render function 
     * @param element the element in which to render the entity 
     * @param entity the entity to render 
     * @param vie the VIE wrapper to use 
     * @param config the configuration 
     */
    protected native void renderNative(
        JavaScriptObject nativeRenderer,
        String renderFunction,
        com.google.gwt.dom.client.Element element,
        Entity entity,
        I_Vie vie,
        JavaScriptObject config) /*-{
                                 var entityWrapper = new $wnd.acacia.EntityWrapper();
                                 entityWrapper.setEntity(entity);
                                 var vieWrapper = new $wnd.acacia.VieWrapper();
                                 if (nativeRenderer && nativeRenderer[renderFunction]) {
                                 nativeRenderer[renderFunction](element, entityWrapper, vieWrapper,
                                 config);
                                 } else if ($wnd.console) {
                                 $wnd.console.log("Rendering function not found: " + renderFunction);
                                 }
                                 }-*/;

    /** 
     * Throws an error indicating that a method is not supported.<p>
     */
    private void notSupported() {

        throw new UnsupportedOperationException("method not supported by this renderer!");
    }
}
