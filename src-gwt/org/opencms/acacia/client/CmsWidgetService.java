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

import org.opencms.acacia.client.widgets.CmsFormWidgetWrapper;
import org.opencms.acacia.client.widgets.CmsStringWidget;
import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.client.widgets.I_CmsFormEditWidget;
import org.opencms.acacia.shared.CmsAttributeConfiguration;
import org.opencms.acacia.shared.CmsContentDefinition;
import org.opencms.acacia.shared.CmsType;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * Service providing form widget renderer for entity attributes.<p>
 */
public class CmsWidgetService implements I_CmsWidgetService {

    /** The attribute configurations. */
    private Map<String, CmsAttributeConfiguration> m_attributeConfigurations;

    /** The in-line renderer. */
    private I_CmsEntityRenderer m_defaultRenderer;

    /** Renderers by attribute. */
    private Map<String, I_CmsEntityRenderer> m_rendererByAttribute = new HashMap<String, I_CmsEntityRenderer>();

    /** Map of renderer by type name. */
    private Map<String, I_CmsEntityRenderer> m_rendererByType;

    /** Map of renderers by name. */
    private Map<String, I_CmsEntityRenderer> m_renderers = new HashMap<String, I_CmsEntityRenderer>();

    /** The registered widget factories. */
    private Map<String, I_CmsWidgetFactory> m_widgetFactories;

    /**
     * Constructor.<p>
     */
    public CmsWidgetService() {

        m_rendererByType = new HashMap<String, I_CmsEntityRenderer>();
        m_widgetFactories = new HashMap<String, I_CmsWidgetFactory>();
        m_attributeConfigurations = new HashMap<String, CmsAttributeConfiguration>();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#addChangedOrderPath(java.lang.String)
     */
    public void addChangedOrderPath(String valuePath) {

        // not implemented
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#addConfigurations(java.util.Map)
     */
    public void addConfigurations(Map<String, CmsAttributeConfiguration> configurations) {

        m_attributeConfigurations.putAll(configurations);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#addRenderer(org.opencms.acacia.client.I_CmsEntityRenderer)
     */
    public void addRenderer(I_CmsEntityRenderer renderer) {

        m_renderers.put(renderer.getName(), renderer);
    }

    /**
     * Adds a renderer for the given type.<p>
     *
     * @param typeName the type name
     * @param renderer the renderer
     */
    public void addRenderer(String typeName, I_CmsEntityRenderer renderer) {

        m_rendererByType.put(typeName, renderer);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#addWidgetFactory(java.lang.String, org.opencms.acacia.client.I_CmsWidgetFactory)
     */
    public void addWidgetFactory(String widgetName, I_CmsWidgetFactory widgetFactory) {

        m_widgetFactories.put(widgetName, widgetFactory);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#getAttributeFormWidget(java.lang.String)
     */
    public I_CmsFormEditWidget getAttributeFormWidget(String attributeName) {

        if (m_attributeConfigurations != null) {
            CmsAttributeConfiguration config = m_attributeConfigurations.get(attributeName);
            if (config != null) {
                I_CmsWidgetFactory factory = m_widgetFactories.get(config.getWidgetName());
                if (factory != null) {
                    return factory.createFormWidget(config.getWidgetConfig());
                }
            }
        }
        // no configuration or widget factory found, return default string widget
        return new CmsFormWidgetWrapper(new CmsStringWidget());
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#getAttributeHelp(java.lang.String)
     */
    public String getAttributeHelp(String attributeName) {

        if (m_attributeConfigurations != null) {
            CmsAttributeConfiguration config = m_attributeConfigurations.get(attributeName);
            if (config != null) {
                return config.getHelp();
            }
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#getAttributeInlineWidget(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public I_CmsEditWidget getAttributeInlineWidget(String attributeName, Element element) {

        if (m_attributeConfigurations != null) {
            CmsAttributeConfiguration config = m_attributeConfigurations.get(attributeName);
            if (config != null) {
                I_CmsWidgetFactory factory = m_widgetFactories.get(config.getWidgetName());
                if (factory != null) {
                    return factory.createInlineWidget(config.getWidgetConfig(), element);
                }
            }
        }
        // no widget configured
        return null;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#getAttributeLabel(java.lang.String)
     */
    public String getAttributeLabel(String attributeName) {

        if (m_attributeConfigurations != null) {
            CmsAttributeConfiguration config = m_attributeConfigurations.get(attributeName);
            if (config != null) {
                return config.getLabel();
            }
        }
        return attributeName;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#getDefaultAttributeValue(java.lang.String, java.lang.String)
     */
    public String getDefaultAttributeValue(String attributeName, String simpleValuePath) {

        CmsAttributeConfiguration config = m_attributeConfigurations.get(attributeName);
        return (config != null) && (config.getDefaultValue() != null) ? config.getDefaultValue() : "";
    }

    /**
     * Gets the renderer instance for a specific attribute.<p>
     *
     * @param attributeName the attribute for which we want the renderer
     *
     * @return the renderer instance
     */
    public I_CmsEntityRenderer getRendererForAttribute(String attributeName) {

        I_CmsEntityRenderer result = m_rendererByAttribute.get(attributeName);
        if (result == null) {
            return m_defaultRenderer;
        }
        return result;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#getRendererForAttribute(java.lang.String, org.opencms.acacia.shared.CmsType)
     */
    public I_CmsEntityRenderer getRendererForAttribute(String attributeName, CmsType attributeType) {

        return getRendererForAttribute(attributeName);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#getRendererForType(org.opencms.acacia.shared.CmsType)
     */
    public I_CmsEntityRenderer getRendererForType(CmsType entityType) {

        if (m_rendererByType.containsKey(entityType.getId())) {
            return m_rendererByType.get(entityType.getId());
        }
        return m_defaultRenderer;
    }

    /**
     * Initializes the widget service with the given content definition.<p>
     *
     * @param definition the content definition
     */
    public void init(CmsContentDefinition definition) {

        m_attributeConfigurations = definition.getConfigurations();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#isDisplayCompact(java.lang.String)
     */
    public boolean isDisplayCompact(String attributeName) {

        if (m_attributeConfigurations != null) {
            CmsAttributeConfiguration config = m_attributeConfigurations.get(attributeName);
            if (config != null) {
                return config.isDisplayColumn();
            }
        }
        return false;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#isDisplaySingleLine(java.lang.String)
     */
    public boolean isDisplaySingleLine(String attributeName) {

        if (m_attributeConfigurations != null) {
            CmsAttributeConfiguration config = m_attributeConfigurations.get(attributeName);
            if (config != null) {
                return config.isDisplaySingleLine();
            }
        }
        return false;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#isVisible(java.lang.String)
     */
    public boolean isVisible(String attributeName) {

        boolean result = true;
        if (m_attributeConfigurations.containsKey(attributeName)) {
            result = m_attributeConfigurations.get(attributeName).isVisible();
        }
        return result;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#registerComplexWidgetAttribute(java.lang.String, java.lang.String, java.lang.String)
     */
    public void registerComplexWidgetAttribute(String attrName, String rendererName, String configuration) {

        I_CmsEntityRenderer renderer = m_renderers.get(rendererName);
        if (renderer != null) {
            renderer = renderer.configure(configuration);
            m_rendererByAttribute.put(attrName, renderer);
        } else {
            log("Invalid entity renderer: " + rendererName);
        }
    }

    /**
     * Adds the default complex type renderer.<p>
     *
     * @param renderer the renderer
     */
    public void setDefaultRenderer(I_CmsEntityRenderer renderer) {

        m_defaultRenderer = renderer;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#setWidgetFactories(java.util.Map)
     */
    public void setWidgetFactories(Map<String, I_CmsWidgetFactory> widgetFactories) {

        m_widgetFactories = widgetFactories;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetService#shouldRemoveLastValueAfterUnfocus(org.opencms.acacia.client.widgets.I_CmsEditWidget)
     */
    public boolean shouldRemoveLastValueAfterUnfocus(I_CmsEditWidget widget) {

        return false;
    }

    /**
     * Log method for debugging.<p>
     *
     * @param message the message to log
     */
    private native void log(String message) /*-{
                                            if ($wnd.console) {
                                            $wnd.console.log(message);
                                            }
                                            }-*/;

}
