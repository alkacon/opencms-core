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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsResultItemBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Image info element.<p>
 */
public class CmsImageInfo extends Composite {

    /** The ui-binder interface. */
    interface I_CmsImageInfoUiBinder extends UiBinder<HTMLPanel, CmsImageInfo> {
        // nothing to do
    }

    /** The ui-binder instance. */
    private static I_CmsImageInfoUiBinder uiBinder = GWT.create(I_CmsImageInfoUiBinder.class);

    /** The description field. */
    @UiField
    protected TableCellElement m_description;

    /** The dimension field. */
    @UiField
    protected TableCellElement m_dimension;

    /** The description label. */
    @UiField
    protected TableCellElement m_labelDescription;

    /** The dimension label. */
    @UiField
    protected TableCellElement m_labelDimension;

    /** The last changed label. */
    @UiField
    protected TableCellElement m_labelLastChanged;

    /** The user last modified label. */
    @UiField
    protected TableCellElement m_labelUserLastModified;

    /** The last changed field. */
    @UiField
    protected TableCellElement m_lastChanged;

    /** The title field. */
    @UiField
    protected SpanElement m_title;

    /** The user last modified field. */
    @UiField
    protected TableCellElement m_userLastModified;

    /**
     * Constructor.<p>
     *
     * @param info the resource info bean
     * @param dimensions the image dimensions
     */
    public CmsImageInfo(CmsResultItemBean info, String dimensions) {

        initWidget(uiBinder.createAndBindUi(this));
        m_labelDescription.setInnerText(Messages.get().key(Messages.GUI_IMAGE_INFO_DESCRIPTION_0));
        m_labelDimension.setInnerText(Messages.get().key(Messages.GUI_IMAGE_INFO_DIMENSION_0));
        m_labelLastChanged.setInnerText(Messages.get().key(Messages.GUI_IMAGE_INFO_DATE_LAST_CHANGED_0));
        m_labelUserLastModified.setInnerText(Messages.get().key(Messages.GUI_IMAGE_INFO_LAST_CHANGED_BY_0));
        m_description.setInnerText(info.getDescription());
        m_description.setTitle(info.getDescription());
        m_dimension.setInnerText(dimensions);
        m_dimension.setTitle(dimensions);
        m_userLastModified.setInnerText(info.getUserLastModified());
        m_userLastModified.setTitle(info.getUserLastModified());
        m_lastChanged.setInnerText(info.getDateLastModified());
        m_lastChanged.setTitle(info.getDateLastModified());
        m_title.setInnerText(info.getTitle());
        m_title.setTitle(info.getTitle());
    }
}
