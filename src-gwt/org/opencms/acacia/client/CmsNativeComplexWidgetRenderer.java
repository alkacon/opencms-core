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

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.entity.CmsEntityBackend;
import org.opencms.acacia.client.entity.I_CmsEntityBackend;
import org.opencms.acacia.shared.CmsContentDefinition;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.gwt.client.ui.CmsTabbedPanel;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * CmsRenderer which delegates the rendering of an entity to native Javascript.
 * 
 * This renderer will interpret its configuration string as a JSON object (which we will call 'config').
 * To render an entity, it will take the name of a function from config.render and then call the function
 * with the entity to render, the parent element, a VIE wrapper, and the configuration object as parameters. 
 */
public class CmsNativeComplexWidgetRenderer implements I_CmsEntityRenderer {

    /** The entity CSS class. */
    public static final String ENTITY_CLASS = I_CmsLayoutBundle.INSTANCE.form().entity();

    /** The attribute label CSS class. */
    public static final String LABEL_CLASS = I_CmsLayoutBundle.INSTANCE.form().label();

    /** The widget holder CSS class. */
    public static final String WIDGET_HOLDER_CLASS = I_CmsLayoutBundle.INSTANCE.form().widgetHolder();

    /** The configuration string. */
    private String m_configuration;

    /** The parsed JSON value from the configuration string. */
    private JSONObject m_jsonConfig;

    /** The native renderer instance. */
    private JavaScriptObject m_nativeInstance;

    /** 
     * Default constructor.<p>
     */
    public CmsNativeComplexWidgetRenderer() {

    }

    /**
     * Creates a new configured instance.<p>
     * 
     * @param configuration the configuration string 
     */
    public CmsNativeComplexWidgetRenderer(String configuration) {

        m_configuration = configuration;
        m_jsonConfig = JSONParser.parseLenient(configuration).isObject();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#configure(java.lang.String)
     */
    public CmsNativeComplexWidgetRenderer configure(String configuration) {

        return new CmsNativeComplexWidgetRenderer(configuration);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#getName()
     */
    public String getName() {

        return CmsContentDefinition.NATIVE_RENDERER;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderAttributeValue(org.opencms.acacia.shared.CmsEntity, org.opencms.acacia.client.CmsAttributeHandler, int, com.google.gwt.user.client.ui.Panel)
     */
    public void renderAttributeValue(
        CmsEntity parentEntity,
        CmsAttributeHandler attributeHandler,
        int attributeIndex,
        Panel context) {

        notSupported();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderForm(org.opencms.acacia.shared.CmsEntity, java.util.List, com.google.gwt.user.client.ui.Panel, org.opencms.acacia.client.I_CmsAttributeHandler, int)
     */
    public CmsTabbedPanel<FlowPanel> renderForm(
        CmsEntity entity,
        List<CmsTabInfo> tabInfos,
        Panel context,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex) {

        throw new UnsupportedOperationException("Custom renderer does not support tabs!");

    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderForm(org.opencms.acacia.shared.CmsEntity, com.google.gwt.user.client.ui.Panel, org.opencms.acacia.client.I_CmsAttributeHandler, int)
     */
    public void renderForm(
        final CmsEntity entity,
        Panel context,
        final I_CmsAttributeHandler parentHandler,
        final int attributeIndex) {

        context.addStyleName(ENTITY_CLASS);
        context.getElement().setAttribute("typeof", entity.getTypeName());
        context.getElement().setAttribute("about", entity.getId());
        String initFunction = CmsContentDefinition.FUNCTION_RENDER_FORM;
        renderNative(
            getNativeInstance(),
            initFunction,
            context.getElement(),
            entity,
            CmsEntityBackend.getInstance(),
            m_jsonConfig.isObject().getJavaScriptObject());
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderInline(org.opencms.acacia.shared.CmsEntity, org.opencms.acacia.client.I_CmsInlineFormParent, org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler)
     */
    public void renderInline(
        CmsEntity entity,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler) {

        notSupported();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderInline(org.opencms.acacia.shared.CmsEntity, java.lang.String, org.opencms.acacia.client.I_CmsInlineFormParent, org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler, int, int)
     */
    public void renderInline(
        CmsEntity parentEntity,
        String attributeName,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler,
        int minOccurrence,
        int maxOccurrence) {

        CmsEntityAttribute attribute = parentEntity.getAttribute(attributeName);
        String renderInline = CmsContentDefinition.FUNCTION_RENDER_INLINE;
        if (attribute != null) {
            List<CmsEntity> values = attribute.getComplexValues();
            List<Element> elements = CmsEntityBackend.getInstance().getAttributeElements(
                parentEntity,
                attributeName,
                formParent.getElement());
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                if (i < values.size()) {
                    CmsEntity value = values.get(i);
                    renderNative(
                        getNativeInstance(),
                        renderInline,
                        element,
                        value,
                        CmsEntityBackend.getInstance(),
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
            m_nativeInstance = createNativeInstance(m_jsonConfig.get(CmsContentDefinition.PARAM_INIT_CALL).isString().stringValue());
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
        CmsEntity entity,
        I_CmsEntityBackend vie,
        JavaScriptObject config) /*-{
                                 var entityWrapper = new $wnd.acacia.CmsEntityWrapper();
                                 entityWrapper.setEntity(entity);
                                 var vieWrapper = new $wnd.acacia.CmsEntityBackendWrapper();
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
