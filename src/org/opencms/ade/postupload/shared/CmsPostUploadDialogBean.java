/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.postupload.shared;

import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Runtime data bean for prefetching.<p>
 *
 * @since 8.0.0
 */
public class CmsPostUploadDialogBean implements IsSerializable {

    /** Name of the used js variable. */
    public static final String DICT_NAME = "postupload_dialog";

    /**
     * A map of the resources for which the properties should be edited, with the structure ids as keys and the resource
     * paths as values.
     */
    private Map<CmsUUID, String> m_resources = new LinkedHashMap<CmsUUID, String>();

    /** Ids of resources for which validation is required. */
    private Set<CmsUUID> m_idsWithRequiredValidation = new HashSet<>();

    /** Flag which controls whether the property configurations should be used. */
    private boolean m_useConfiguration;

    /** Flag to control if configured basic properties should be shown. */
    private boolean m_addBasicProperties;

    /** True if there  was an image among the uploaded resources. */
    private boolean m_hasImage;

    /**
     * Default constructor for serialization.<p>
     */
    public CmsPostUploadDialogBean() {

        // default constructor for serialization
    }

    /**
     * Creates a new instance.<p>
     *
     * @param resources the map of resources for which the properties should be uploaded
     * @param idsWithRequiredValidation structurei ids of resources for which validation is required
     * @param hasImage true if there is an image among the resources
     */
    public CmsPostUploadDialogBean(
        Map<CmsUUID, String> resources,
        Set<CmsUUID> idsWithRequiredValidation,
        boolean hasImage) {

        m_resources.putAll(resources);
        m_idsWithRequiredValidation = idsWithRequiredValidation;
        m_hasImage = hasImage;
    }

    /**
     * Gets the structure ids of resources for which validation is required.
     *
     * @return the structure ids of resources for which validation is required
     */
    public Set<CmsUUID> getIdsWithRequiredValidation() {

        return m_idsWithRequiredValidation;
    }

    /**
     * Returns the list of resource paths.<p>
     *
     * @return the list of resource paths
     */
    public Map<CmsUUID, String> getResources() {

        return m_resources;
    }

    /**
     * Checks if there was an image among the uploaded resources.
     *
     * @return  true if an image was uploaded
     */
    public boolean hasImage() {

        return m_hasImage;
    }

    /**
     * Returns true if the basic properties configured for the sitemap should be shown.
     * @return true if the basic properties configured for the sitemap should be shown.
     */
    public boolean isAddBasicProperties() {

        return m_addBasicProperties;
    }

    /**
     * Returns true if the property configurations should be used.<p>
     *
     * @return true if the property configurations should be used
     */
    public boolean isUsePropertyConfiguration() {

        return m_useConfiguration;

    }

    /**
     * Set a flag, indicating if basic properties as configured in the sitemap are merged into the
     * properties shown on file upload.
     *
     * @param addBasicProperties flag, indicating if basic properties as configured in the sitemap should be added
     */
    public void setAddBasicProperties(final boolean addBasicProperties) {

        m_addBasicProperties = addBasicProperties;
    }

    /**
     * Sets the map of resources for which the properties should be uploaded.<p>
     *
     * @param resources the map of resources for which the properties should be uploaded
     */
    public void setResources(Map<CmsUUID, String> resources) {

        m_resources = resources;
    }

    /**
     * Enables/disables use of the property configuration.<p>
     *
     * @param useConfiguration true if the property configuration should be used
     */
    public void setUsePropertyConfiguration(boolean useConfiguration) {

        m_useConfiguration = useConfiguration;
    }

}
