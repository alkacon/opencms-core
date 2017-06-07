/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.acacia.client.widgets.complex;

import org.opencms.acacia.client.CmsAttributeHandler;
import org.opencms.acacia.client.I_CmsAttributeHandler;
import org.opencms.acacia.client.I_CmsEntityRenderer;
import org.opencms.acacia.client.I_CmsInlineFormParent;
import org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler;
import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.shared.CmsDataViewConstants;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Special renderer for DataView widgets.<p>
 */
public class CmsDataViewWidgetRenderer implements I_CmsEntityRenderer {

    /** The entity CSS class. */
    public static final String ENTITY_CLASS = I_CmsLayoutBundle.INSTANCE.form().entity();

    /** The attribute label CSS class. */
    public static final String LABEL_CLASS = I_CmsLayoutBundle.INSTANCE.form().label();

    /** The widget holder CSS class. */
    public static final String WIDGET_HOLDER_CLASS = I_CmsLayoutBundle.INSTANCE.form().widgetHolder();

    /** The configuration string. */
    private String m_configuration;

    /**
     * Default constructor.<p>
     */
    public CmsDataViewWidgetRenderer() {

    }

    /**
     * Creates a new configured instance.<p>
     *
     * @param configuration the configuration string
     */
    public CmsDataViewWidgetRenderer(String configuration) {

        m_configuration = configuration;
        if (m_configuration == null) {
            m_configuration = "";
        }
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#configure(java.lang.String)
     */
    public CmsDataViewWidgetRenderer configure(String configuration) {

        return new CmsDataViewWidgetRenderer(configuration);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#getName()
     */
    public String getName() {

        return CmsDataViewConstants.RENDERER_ID;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderAttributeValue(org.opencms.acacia.shared.CmsEntity, org.opencms.acacia.client.CmsAttributeHandler, int, com.google.gwt.user.client.ui.Panel)
     */
    public void renderAttributeValue(
        CmsEntity parentEntity,
        CmsAttributeHandler attributeHandler,
        int attributeIndex,
        Panel context) {

        // ignore

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

        return null;

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
        CmsDataViewValueAccessor accessor = new CmsDataViewValueAccessor(entity, parentHandler, attributeIndex);
        context.add(new CmsDataViewClientWidget(accessor, m_configuration));
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderInline(org.opencms.acacia.shared.CmsEntity, org.opencms.acacia.client.I_CmsInlineFormParent, org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler, org.opencms.acacia.client.I_CmsAttributeHandler, int)
     */
    public void renderInline(
        CmsEntity entity,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex) {

        // ignore
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderInline(org.opencms.acacia.shared.CmsEntity, java.lang.String, org.opencms.acacia.client.I_CmsInlineFormParent, org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler, org.opencms.acacia.client.I_CmsAttributeHandler, int, int, int)
     */
    public void renderInline(
        CmsEntity parentEntity,
        String attributeName,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex,
        int minOccurrence,
        int maxOccurrence) {

        // ignore
    }

    /**
     * Throws an error indicating that a method is not supported.<p>
     */
    private void notSupported() {

        throw new UnsupportedOperationException("method not supported by this renderer!");
    }

}
