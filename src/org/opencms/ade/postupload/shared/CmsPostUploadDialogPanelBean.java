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

package org.opencms.ade.postupload.shared;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean representing the state of a single resource to be edited in the upload property dialog.<p>
 */
public class CmsPostUploadDialogPanelBean implements IsSerializable {

    /** The page info for displaying the CmsListItemWidget. */
    private CmsListInfoBean m_infoBean;

    /** The first image preview info text. */
    private String m_previewInfo1;

    /** The second preview info text. */
    private String m_previewInfo2;

    /** The image preview URL. */
    private String m_previewLink;

    /** The properties of the resource. */
    private Map<String, CmsClientProperty> m_properties;

    /** The property definitions for the resource type of the resource. */
    private Map<String, CmsXmlContentProperty> m_propertyDefinitions;

    /** The structure id of the resource. */
    private CmsUUID m_structureId;

    /** The warning to display. */
    private String m_warning;

    /**
     * Creates a new instance.<p>
     */
    public CmsPostUploadDialogPanelBean() {

    }

    /**
     * Creates a new instance.<p>
     *
     * @param structureId the structure id of the resource
     * @param infoBean the list info bean
     */
    public CmsPostUploadDialogPanelBean(CmsUUID structureId, CmsListInfoBean infoBean) {

        m_structureId = structureId;
        m_infoBean = infoBean;

    }

    /**
     * Gets the info bean for the resource.<p>
     *
     * @return the info bean for the resource
     */
    public CmsListInfoBean getInfoBean() {

        return m_infoBean;
    }

    /**
     * Gets the first preview info text to display.
     *
     * @return a preview info text
     */
    public String getPreviewInfo1() {

        return m_previewInfo1;
    }

    /**
     * Gets the second preview info text to display.
     *
     * @return a preview info text
     */
    public String getPreviewInfo2() {

        return m_previewInfo2;
    }

    /**
     * Gets the image preview URL.
     *
     * @return the image preview URL, if it exists, and null otherwise
     */
    public String getPreviewLink() {

        return m_previewLink;
    }

    /**
     * Gets the properties for the resource.<p>
     *
     * @return the map of properties by property name
     */
    public Map<String, CmsClientProperty> getProperties() {

        return m_properties;
    }

    /**
     * Gets the property definitions for the type of the resource.<p>
     *
     * @return the map of property definitions by property names
     */
    public Map<String, CmsXmlContentProperty> getPropertyDefinitions() {

        return m_propertyDefinitions;
    }

    /**
     * Gets the structure id of the resource.<p>
     *
     * @return the user id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the warning to display.
     *
     * @return the warning to display
     */
    public String getWarning() {

        return m_warning;
    }

    /**
     * Sets the list info bean for the resource.<p>
     *
     * @param info the list info bean for the resource
     */
    public void setInfoBean(CmsListInfoBean info) {

        m_infoBean = info;
    }

    /**
     * Sets the first preview info text.
     *
     * @param text the preview info text
     *
     */
    public void setPreviewInfo1(String text) {

        this.m_previewInfo1 = text;
    }

    /**
     * Sets the second preview info text.
     *
     * @param text the preview info text
     */
    public void setPreviewInfo2(String text) {

        this.m_previewInfo2 = text;
    }

    /**
     * Sets the image preview URL.
     *
     * @param previewLink the image preview URL
     */
    public void setPreviewLink(String previewLink) {

        m_previewLink = previewLink;
    }

    /**
     * Sets the properties for the resource.<p>
     *
     * @param properties the properties for the resource
     */
    public void setProperties(Map<String, CmsClientProperty> properties) {

        m_properties = properties;

    }

    /**
     * Sets the property definitions for the type of the resource.<p>
     *
     * @param propertyDefinitions the map of property definitions
     */
    public void setPropertyDefinitions(Map<String, CmsXmlContentProperty> propertyDefinitions) {

        m_propertyDefinitions = propertyDefinitions;
    }

    /**
     * Sets the structure id of the resource.<p>
     *
     * @param structureId the structure id of the resource
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the warning to display.
     *
     * @param warning the warning to display
     */
    public void setWarning(String warning) {

        m_warning = warning;
    }

}
