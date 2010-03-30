/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultListItem.java,v $
 * Date   : $Date: 2010/03/30 14:08:37 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.I_CmsItemId;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the specific list item for the result list.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public class CmsResultListItem extends CmsSimpleListItem implements ClickHandler, I_CmsItemId {

    /** The resource path to identify the specific resource. */
    private String m_resourcePath;

    /** The resource type name of the resource. */
    private String m_resourceType;

    /** The reference to the gallery info bean. */
    private CmsGalleryInfoBean m_infoBean;

    /**
     * The list item constructor.
     * 
     * @param infoBean the reference to the gallery info bean 
     * @param content the content of the category list item
     */
    public CmsResultListItem(CmsGalleryInfoBean infoBean, Widget... content) {

        super(content);
        for (Widget i : content) {
            if (i instanceof CmsCheckBox) {
                ((CmsCheckBox)i).addClickHandler(this);
            }
        }
        setInfoBean(infoBean);
    }

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public void onClick(ClickEvent event) {

        Object sender = event.getSource();
        // TODO: improve the instance test 
        // if check box is selected
        if (sender instanceof ToggleButton) {
            // open the preview
        }
    }

    /**
     * Returns the resource path as the unique id of the resource.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#getId()
     */
    public String getId() {

        return m_resourcePath;
    }

    /**
     * Sets the resource path as a unique id for this resource.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#setId(java.lang.String)
     */
    public void setId(String id) {

        m_resourcePath = id;

    }

    /**
     * Sets the resource type name.<p>
     *
     * @param resourceType the resource type name to set
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Sets the gallery info bean.<p>
     *
     * @param infoBean the gallery info bean
     */
    public void setInfoBean(CmsGalleryInfoBean infoBean) {

        m_infoBean = infoBean;
    }

    /**
     * Returns the gallery info bean.<p>
     *
     * @return the infoBean
     */
    public CmsGalleryInfoBean getInfoBean() {

        return m_infoBean;
    }
}