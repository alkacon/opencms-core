/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsContainerElementBean.java,v $
 * Date   : $Date: 2011/05/20 13:47:00 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * One element of a container in a container page.<p>
 * 
 * @author Michael Moossen
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 8.0
 */
public class CmsContainerElementBean {

    /** Flag indicating if a new element should be created replacing the given one on first edit of a container-page. */
    private final boolean m_createNew;

    /** The client ADE editor hash. */
    private final transient String m_editorHash;

    /** The element's structure id. */
    private final CmsUUID m_elementId;

    /** The formatter's structure id. */
    private final CmsUUID m_formatterId;

    /** The configured properties. */
    private final Map<String, String> m_individualSettings;

    /** The settings of this element containing also default values. */
    private transient Map<String, String> m_settings;

    /** The resource of this element. */
    private transient CmsResource m_resource;

    /** The element site path, only set while rendering. */
    private String m_sitePath;

    /**
     * Creates a new container page element bean.<p> 
     *  
     * @param elementId the element's structure id
     * @param formatterId the formatter's structure id, could be <code>null</code>
     * @param individualSettings the element settings as a map of name/value pairs
     * @param createNew <code>true</code> if a new element should be created replacing the given one on first edit of a container-page
     **/
    public CmsContainerElementBean(
        CmsUUID elementId,
        CmsUUID formatterId,
        Map<String, String> individualSettings,
        boolean createNew) {

        m_elementId = elementId;
        m_formatterId = formatterId;
        Map<String, String> newSettings = (individualSettings == null
        ? new HashMap<String, String>()
        : individualSettings);
        m_individualSettings = Collections.unmodifiableMap(newSettings);
        String clientId = m_elementId.toString();
        if (!m_individualSettings.isEmpty()) {
            int hash = m_individualSettings.toString().hashCode();
            clientId += CmsADEManager.CLIENT_ID_SEPERATOR + hash;
        }
        m_editorHash = clientId;
        m_createNew = createNew;
    }

    /**
     * Returns the ADE client editor has value.<p>
     * 
     * @return the ADE client editor has value
     */
    public String editorHash() {

        return m_editorHash;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsContainerElementBean)) {
            return false;
        }
        return editorHash().equals(((CmsContainerElementBean)obj).editorHash());
    }

    /**
     * Returns the structure id of the formatter of this element.<p>
     *
     * @return the structure id of the formatter of this element
     */
    public CmsUUID getFormatterId() {

        return m_formatterId;
    }

    /**
     * Returns the structure id of the resource of this element.<p>
     *
     * @return the structure id of the resource of this element
     */
    public CmsUUID getId() {

        return m_elementId;
    }

    /**
     * Returns the settings of this element.<p>
     * 
     * @return the settings of this element
     */
    public Map<String, String> getIndividualSettings() {

        return m_individualSettings;
    }

    /**
     * Returns the resource of this element.<p>
     * 
     * It is required to call {@link #initResource(CmsObject)} before this method can be used.<p>
     * 
     * @return the resource of this element
     * 
     * @see #initResource(CmsObject)
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the site path of the resource of this element.<p>
     * 
     * It is required to call {@link #initResource(CmsObject)} before this method can be used.<p>
     * 
     * @return the site path of the resource of this element
     * 
     * @see #initResource(CmsObject)
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_editorHash.hashCode();
    }

    /**
     * Initializes the resource and the site path of this element.<p>
     * 
     * @param cms the CMS context 
     * 
     * @throws CmsException if something goes wrong reading the element resource
     */
    public void initResource(CmsObject cms) throws CmsException {

        if (m_resource == null) {
            m_resource = cms.readResource(getId());
        }
        if (m_settings == null) {
            m_settings = CmsXmlContentPropertyHelper.mergeDefaults(cms, m_resource, m_individualSettings);
        }
        // redo on every init call to ensure sitepath is calculated for current site
        m_sitePath = cms.getSitePath(m_resource);
    }

    /**
     * Returns if a new element should be created replacing the given one on first edit of a container-page.<p>
     * 
     * @return <code>true</code> if a new element should be created replacing the given one on first edit of a container-page
     */
    public boolean isCreateNew() {

        return m_createNew;
    }

    /**
     * Tests whether this element refers to a group container.<p>
     * 
     * @param cms the CmsObject used for VFS operations
     *  
     * @return true if the container element refers to a group container
     * 
     * @throws CmsException if something goes wrong 
     */
    public boolean isGroupContainer(CmsObject cms) throws CmsException {

        CmsResource resource = cms.readResource(m_elementId);
        return resource.getTypeId() == CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_ID;
    }

    /**
     * Returns the element settings including default values for settings not set.<p>
     * Will return <code>null</code> if the element bean has not been initialized with {@link #initResource(org.opencms.file.CmsObject)}.<p>
     * 
     * @return the element settings
     */
    public Map<String, String> getSettings() {

        return m_settings;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return editorHash();
    }
}