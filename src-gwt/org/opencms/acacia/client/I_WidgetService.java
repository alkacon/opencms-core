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

import org.opencms.acacia.client.widgets.I_EditWidget;
import org.opencms.acacia.client.widgets.I_FormEditWidget;
import org.opencms.acacia.shared.AttributeConfiguration;
import org.opencms.acacia.shared.Type;

import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * Provides widget renderer for entity attributes.<p>
 */
public interface I_WidgetService {

    /**
     * Adds the given path to the list of paths where child attributes have change their order.<p>
     * 
     * @param attributePath the attribute path
     */
    void addChangedOrderPath(String attributePath);

    /**
     * Adds all configurations.<p>
     * 
     * @param configurations the configurations to add
     */
    void addConfigurations(Map<String, AttributeConfiguration> configurations);

    /** 
     * Adds a renderer.<p>
     * 
     * @param renderer the renderer to add 
     */
    void addRenderer(I_EntityRenderer renderer);

    /**
     * Registers the given widget factory with the service.<p>
     * 
     * @param widgetName the widget name
     * @param widgetFactory the widget factory
     */
    void addWidgetFactory(String widgetName, I_WidgetFactory widgetFactory);

    /**
     * Returns the attribute form editing widget.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the attribute widget
     */
    I_FormEditWidget getAttributeFormWidget(String attributeName);

    /**
     * Returns the attribute help information.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the attribute help information
     */
    String getAttributeHelp(String attributeName);

    /**
     * Returns the attribute inline editing widget wrapping the given DOM element.<p>
     * 
     * @param attributeName the attribute name
     * @param element the DOM element to wrap
     * 
     * @return the attribute widget
     */
    I_EditWidget getAttributeInlineWidget(String attributeName, Element element);

    /**
     * Returns the label for the given attribute.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the attribute label
     */
    String getAttributeLabel(String attributeName);

    /**
     * Returns the default attribute value.<p>
     * 
     * @param attributeName the attribute name
     * @param simpleValuePath the value path
     * 
     * @return the default value
     */
    String getDefaultAttributeValue(String attributeName, String simpleValuePath);

    /**
     * Returns the renderer for the given attribute.<p>
     * 
     * @param attributeName the name of the attribute
     * @param attributeType the type of the attribute
     * 
     * @return the renderer
     */
    I_EntityRenderer getRendererForAttribute(String attributeName, Type attributeType);

    /**
     * Returns the renderer for the given entity type.<p>
     * 
     * @param entityType the type
     * 
     * @return the renderer
     */
    I_EntityRenderer getRendererForType(Type entityType);

    /**
     * Returns the if the attribute widget should be displayed in compact view.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return <code>true</code> if the attribute widget should be displayed in compact view
     */
    boolean isDisplayCompact(String attributeName);

    /**
     * Returns if the attribute widget should be displayed in single line view.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return <code>true</code> if the attribute widget should be displayed in single line view
     */
    boolean isDisplaySingleLine(String attributeName);

    /**
     * Returns if the given attribute should be visible in the editor.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return <code>true</code> if the given attribute should be visible in the editor
     */
    boolean isVisible(String attributeName);

    /**
     * Registers a complex widget attribute which should be handled by a special renderer.<p>
     * 
     * @param attrName the attribute name 
     * @param renderer the renderer to register for the attribute 
     * @param configuration the renderer configuration 
     */
    void registerComplexWidgetAttribute(String attrName, String renderer, String configuration);

    /**
     * Sets the default renderer.<p>
     * 
     * @param renderer the default renderer 
     */
    void setDefaultRenderer(I_EntityRenderer renderer);

    /**
     * Sets the widget factories.<p>
     * 
     * @param widgetFactories the widget factories
     */
    void setWidgetFactories(Map<String, I_WidgetFactory> widgetFactories);

    /**
     * Returns true if the value which this widget is being used for should be disabled if it is unfocused and the last remaining attribute value for a given attribute.<p>
     * 
     * The main use case is disabling empty text input fields.<p>
     * 
     * @param widget the widget to check
     *  
     * @return true if the value should be disabled 
     */
    boolean shouldRemoveLastValueAfterUnfocus(I_EditWidget widget);
}
