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

package org.opencms.ugc;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Optional;

/**
 * The configuration for 'user generated content' forms.<p>
 */
public class CmsUgcConfiguration {

    /** The user to user for VFS operations caused by guests who submit the XML content form. */
    private Optional<CmsUser> m_userForGuests;

    /** An id that should uniquely identify the configuration. */
    private CmsUUID m_id;

    /** The manager group for the project in which VFS operations should be performed. */
    private CmsGroup m_projectGroup;

    /** The optional wait interval for the queue. */
    private Optional<Long> m_queueInterval;

    /** The optional maximum queue length. */
    private Optional<Integer> m_maxQueueLength;

    /** The name pattern for XML contents. */
    private String m_namePattern;

    /** The parent folder in which contents should be created. */
    private CmsResource m_contentParentFolder;

    /** The optional parent folder in which uploaded files should be created. */
    private Optional<CmsResource> m_uploadParentFolder;

    /** The maximum upload size (optional). */
    private Optional<Long> m_maxUploadSize;

    /** The maximum content number (optional). */
    private Optional<Integer> m_maxContentNumber;

    /** Flag which determines whether contents should automatically be published. */
    private boolean m_isAutoPublish;

    /** The valid file name extensions. */
    private Optional<List<String>> m_validExtensions;

    /** The locale in which to save the content. */
    private Locale m_locale;

    /** The resource type for new XML contents. */
    private String m_resourceType;

    /** The path of the configuration. */
    private String m_path;

    /**
     * Creates a new form configuration.<p>
     *
     * @param id the id for the form configuration
     * @param userForGuests the user to use for VFS operations caused by guests who submit the XML content form
     * @param projectGroup the group to be used as the manager group for projects based on this configuration
     * @param resourceType the resource type for new XML contents
     * @param contentParentFolder the parent folder for XML contents
     * @param namePattern the name pattern for XML contents
     * @param locale the locale to use
     * @param uploadParent the parent folder for uploads
     * @param maxUploadSize the maximum upload file size
     * @param maxContents the maximum number of XML contents
     * @param queueTimeout the wait time for the queue
     * @param maxQueueLength the maximum queue length
     * @param autoPublish enables/disables automatic publishing
     * @param validExtensions the list of valid extensions
     */
    public CmsUgcConfiguration(
        CmsUUID id,
        Optional<CmsUser> userForGuests,
        CmsGroup projectGroup,
        String resourceType,
        CmsResource contentParentFolder,
        String namePattern,
        Locale locale,
        Optional<CmsResource> uploadParent,
        Optional<Long> maxUploadSize,
        Optional<Integer> maxContents,
        Optional<Long> queueTimeout,
        Optional<Integer> maxQueueLength,
        boolean autoPublish,
        Optional<List<String>> validExtensions) {

        m_id = id;
        m_userForGuests = userForGuests;
        m_projectGroup = projectGroup;
        m_resourceType = resourceType;
        m_contentParentFolder = contentParentFolder;
        m_namePattern = namePattern;
        m_locale = locale;
        m_uploadParentFolder = uploadParent;
        m_maxUploadSize = maxUploadSize;
        m_maxContentNumber = maxContents;
        m_queueInterval = queueTimeout;
        m_maxQueueLength = maxQueueLength;
        m_isAutoPublish = autoPublish;
        m_validExtensions = validExtensions;

    }

    /**
     * Returns the folder for XML contents.<p>
     *
     * @return the folder for XML contents
     */
    public CmsResource getContentParentFolder() {

        return m_contentParentFolder;
    }

    /**
     * Gets the id.<p>
     *
     * The id is a UUID that should uniquely identify this configuration.<p>
     *
     * @return the id for this configuration
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the maximum number of XML contents.<p>
     *
     * @return the maximum number of XML contents
     */
    public Optional<Integer> getMaxContentNumber() {

        return m_maxContentNumber;
    }

    /**
     * Returns the maximum queue length.<p>
     *
     * @return the maximum queue length
     */
    public Optional<Integer> getMaxQueueLength() {

        return m_maxQueueLength;
    }

    /**
     * Returns the maximum upload size.<p>
     *
     * @return the maximum upload size
     */
    public Optional<Long> getMaxUploadSize() {

        return m_maxUploadSize;
    }

    /**
     * Returns the name pattern for XML contents.<p>
     *
     * @return the name pattern for XML contents
     */
    public String getNamePattern() {

        return m_namePattern;
    }

    /**
     * Gets the path of the configuration.<p>
     *
     * @return the path of the configuration
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the group which should be used as the manager groups for projects based on this configuration.<p>
     *
     * @return the project manager group for this configuration
     */
    public CmsGroup getProjectGroup() {

        return m_projectGroup;
    }

    /**
     * Returns the wait time for acquiring sessions for the same configuration.<p>
     *
     * @return the wait time
     */
    public Optional<Long> getQueueInterval() {

        return m_queueInterval;
    }

    /**
     * Returns the resource type for XML contents.<p>
     *
     * @return the resource type for XML contents
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the folder for uploads.<p>
     *
     * @return the folder for uploads
     */
    public Optional<CmsResource> getUploadParentFolder() {

        return m_uploadParentFolder;
    }

    /**
     * Returns the user which should be used for VFS operations when guests submit the XML content form.<p>
     *
     * @return the  user to use for VFS operations instead of the guest user
     */
    public Optional<CmsUser> getUserForGuests() {

        return m_userForGuests;
    }

    /**
     * Returns the list of valid extensions for uploads.<p>
     *
     * @return the list of valid extensions for uploads
     */
    public Optional<List<String>> getValidExtensions() {

        return m_validExtensions;
    }

    /**
     * Returns true if XML contents should automatically be published.<p>
     *
     * @return true if XML contents should automatically be published
     */
    public boolean isAutoPublish() {

        return m_isAutoPublish;
    }

    /**
     * Checks if a queue is needed for creating sessions for this configuration.<p>
     *
     * @return true if a queue is needed for this configuration
     */
    public boolean needsQueue() {

        return m_maxQueueLength.isPresent() || m_queueInterval.isPresent();
    }

    /**
     * Sets the path.<p>
     *
     * @param path the path of the configuration.<p>
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
