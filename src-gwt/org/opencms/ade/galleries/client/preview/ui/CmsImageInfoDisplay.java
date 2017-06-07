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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.gwt.client.ui.input.datebox.CmsDateConverter;
import org.opencms.gwt.shared.property.CmsClientProperty;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The widget to display the information of the selected image.<p>
 *
 * @since 8.0.
 */
public class CmsImageInfoDisplay extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsImageInfosTabUiBinder extends UiBinder<Widget, CmsImageInfoDisplay> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsImageInfosTabUiBinder m_uiBinder = GWT.create(I_CmsImageInfosTabUiBinder.class);

    /** The path label. */
    @UiField
    protected Label m_labelPath;

    /** The name label. */
    @UiField
    protected Label m_labelName;

    /** The title label. */
    @UiField
    protected Label m_labelTitle;

    /** The format label. */
    @UiField
    protected Label m_labelFormat;

    /** The type label. */
    @UiField
    protected Label m_labelType;

    /** The size label. */
    @UiField
    protected Label m_labelSize;

    /** The date last modified label. */
    @UiField
    protected Label m_labelModified;

    /** The path label. */
    @UiField
    protected Label m_displayPath;

    /** The name label. */
    @UiField
    protected Label m_displayName;

    /** The title label. */
    @UiField
    protected Label m_displayTitle;

    /** The format label. */
    @UiField
    protected Label m_displayFormat;

    /** The type label. */
    @UiField
    protected Label m_displayType;

    /** The size label. */
    @UiField
    protected Label m_displaySize;

    /** The date last modified label. */
    @UiField
    protected Label m_displayModified;

    /**
     * The constructor.<p>
    */
    public CmsImageInfoDisplay() {

        initWidget(m_uiBinder.createAndBindUi(this));

        m_labelPath.setText("Path:");
        m_labelName.setText("Name:");
        m_labelTitle.setText("Title:");
        m_labelFormat.setText("Format:");
        m_labelType.setText("Type:");
        m_labelSize.setText("Size:");
        m_labelModified.setText("Last modified:");
    }

    /**
     * Fills the contend data.<p>
     *
     * @param infoBean the image info bean
     */
    public void fillContent(CmsImageInfoBean infoBean) {

        m_displayPath.setText(infoBean.getResourcePath());
        m_displayName.setText(infoBean.getTitle());
        m_displayFormat.setText(infoBean.getWidth() + "x" + infoBean.getHeight());
        m_displayType.setText(infoBean.getResourceType());
        m_displaySize.setText(infoBean.getSize());
        m_displayTitle.setText(infoBean.getProperties().get(CmsClientProperty.PROPERTY_TITLE));
        m_displayModified.setText(CmsDateConverter.toString(infoBean.getLastModified()));
    }
}