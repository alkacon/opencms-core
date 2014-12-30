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

import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.gwt.client.ui.CmsTabbedPanel;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Renders an entity into a widget.<p>
 */
public interface I_CmsEntityRenderer {

    /**
     * Returns a copy of this renderer which has been configured with the given configuration string.<p>
     * 
     * @param configuration the configuration string 
     * 
     * @return the configured copy of the renderer 
     */
    I_CmsEntityRenderer configure(String configuration);

    /**
     * Gets the name of the renderer (should be unique for each renderer class).<p>
     * 
     * @return the renderer name 
     */
    String getName();

    /**
     * Renders a single attribute value. Used for inline editing to show a fragment of the form.<p>
     * 
     * @param parentEntity the parent entity
     * @param attributeHandler the attribute handler
     * @param attributeIndex the value index
     * @param context the parent widget
     */
    void renderAttributeValue(
        CmsEntity parentEntity,
        CmsAttributeHandler attributeHandler,
        int attributeIndex,
        Panel context);

    /**
     * Renders the given entity into a form with tabs.<p>
     * 
     * @param entity the entity to render
     * @param tabInfos the tab infos
     * @param context the context widget panel
     * @param parentHandler the parent attribute handler
     * @param attributeIndex the attribute index
     * 
     * @return the tabbed panel 
     */
    CmsTabbedPanel<FlowPanel> renderForm(
        CmsEntity entity,
        List<CmsTabInfo> tabInfos,
        Panel context,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex);

    /**
     * Renders the given entity into a form.<p>
     * 
     * @param entity the entity to render
     * @param context the context widget panel
     * @param parentHandler the parent attribute handler
     * @param attributeIndex the attribute index
     */
    void renderForm(CmsEntity entity, Panel context, I_CmsAttributeHandler parentHandler, int attributeIndex);

    /**
     * Injects editing widgets into the given DOM context to enable editing of the given entity.<p>
     * 
     * @param entity the entity to render
     * @param formParent formParent the form parent widget
     * @param updateHandler handles updates on the HTML required  due to entity data changes
     * @param parentHandler the parent attribute handler
     * @param attributeIndex the attribute index
     */
    void renderInline(
        CmsEntity entity,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex);

    /**
     * Injects editing widgets into the given DOM context to enable editing of the given entity attribute.<p>
     * 
     * @param parentEntity the parent entity 
     * @param attributeName the attribute name
     * @param formParent the form parent widget
     * @param updateHandler handles updates on the HTML required  due to entity data changes
     * @param parentHandler the parent attribute handler
     * @param attributeIndex the attribute index
     * @param minOccurrence the minimum occurrence of this attribute
     * @param maxOccurrence the maximum occurrence of this attribute
     */
    void renderInline(
        CmsEntity parentEntity,
        String attributeName,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex,
        int minOccurrence,
        int maxOccurrence);
}
