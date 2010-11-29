/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsBinaryPreviewContent.java,v $
 * Date   : $Date: 2010/11/29 07:52:34 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsDateTimeUtil;
import org.opencms.gwt.client.util.CmsDateTimeUtil.Format;
import org.opencms.gwt.shared.CmsIconUtil;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * Widget to display resource informations within the resource preview.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsBinaryPreviewContent extends Composite {

    /**
     * The ui-binder interface.<p>
     */
    public interface I_CmsPreviewContentUiBinder extends UiBinder<HTMLPanel, CmsBinaryPreviewContent> {
        // GWT interface, nothing to do
    }

    /**
     * The style interface.<p>
     */
    interface I_CmsPreviewContentStyle extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String description();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String field();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String icon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String label();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String panel();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String title();
    }

    private static I_CmsPreviewContentUiBinder uiBinder = GWT.create(I_CmsPreviewContentUiBinder.class);

    /** The date last modified field. */
    @UiField
    protected InlineLabel m_dateMod;

    /** The date last modified label. */
    @UiField
    protected CmsLabel m_dateModLabel;

    /** The description field. */
    @UiField
    protected Label m_description;

    /** The icon. */
    @UiField
    protected InlineLabel m_icon;

    /** The path field. */
    @UiField
    protected InlineLabel m_path;

    /** The path label. */
    @UiField
    protected CmsLabel m_pathLabel;

    /** The size field. */
    @UiField
    protected InlineLabel m_size;

    /** The size label. */
    @UiField
    protected CmsLabel m_sizeLabel;

    /** The css for this widget. */
    @UiField
    protected I_CmsPreviewContentStyle m_style;

    /** The title field. */
    @UiField
    protected InlineLabel m_title;

    /**
     * Constructor.<p>
     */
    public CmsBinaryPreviewContent() {

        initWidget(uiBinder.createAndBindUi(this));
        m_sizeLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_SIZE_0) + ":");
        m_pathLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_PATH_0) + ":");
        m_dateModLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_DATEMODIFIED_0) + ":");
    }

    /**
     * Constructor.<p>
     * 
     * @param info the resource info to display
     */
    public CmsBinaryPreviewContent(CmsResourceInfoBean info) {

        this();
        setInfo(info);
    }

    /**
     * Sets the last modification date.<p>
     * 
     * @param date the last modification date
     */
    public void setDateModified(Date date) {

        if (date != null) {
            m_dateMod.setText(CmsDateTimeUtil.getDate(date, Format.MEDIUM));
        } else {
            m_dateMod.setText("");
        }
    }

    /**
     * Sets the description.<p>
     * 
     * @param description the description
     */
    public void setDescription(String description) {

        m_description.setText(description);
    }

    /**
     * Sets the resource icon css class.<p>
     * 
     * @param iconClass the resource icon css class
     */
    public void setIconClass(String iconClass) {

        m_icon.setStyleName(m_style.icon());
        m_icon.addStyleName(iconClass);
    }

    /**
     * Sets the resource info and all displayed fields.<p>
     * 
     * @param info the resource info
     */
    public void setInfo(CmsResourceInfoBean info) {

        setIconClass(CmsIconUtil.getResourceIconClasses(info.getResourceType(), info.getResourcePath(), false));
        setTitle(info.getTitle());
        setDescription(info.getDescription());
        setDateModified(info.getLastModified());
        setPath(info.getResourcePath());
        setSize(info.getSize());
    }

    /**
     * Sets the path.<p>
     * 
     * @param path the path
     */
    public void setPath(String path) {

        m_path.setText(path);
    }

    /**
     * Sets the size.<p>
     * 
     * @param size the size
     */
    public void setSize(String size) {

        m_size.setText(size);
    }

    /**
     * Sets the title.<p>
     * 
     * @param title the title
     * 
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {

        super.setTitle(title);
        m_title.setText(title);
    }
}
