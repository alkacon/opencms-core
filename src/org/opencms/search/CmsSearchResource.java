/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.search;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.util.Date;
import java.util.List;

/**
 * A resource implementation that combines the Solr document together with a OpenCms VFS resource.<p>
 * 
 * @since 8.5.0
 */
public class CmsSearchResource extends CmsResource {

    /** A generated serial version UID. */
    private static final long serialVersionUID = -323110688331457211L;

    /** The Solr document. */
    private I_CmsSearchDocument m_doc;

    /**
     * Constructor, creates a new file Object from the given resource with 
     * an empty byte array as file content, if the resource does not
     * implement a file.<p>
     * 
     * @param resource the base resource object to create a file from
     * @param doc the search document
     */
    public CmsSearchResource(CmsResource resource, I_CmsSearchDocument doc) {

        this(
            resource.getStructureId(),
            resource.getResourceId(),
            resource.getRootPath(),
            resource.getTypeId(),
            resource.getFlags(),
            resource.getProjectLastModified(),
            resource.getState(),
            resource.getDateCreated(),
            resource.getUserCreated(),
            resource.getDateLastModified(),
            resource.getUserLastModified(),
            resource.getDateReleased(),
            resource.getDateExpired(),
            resource.getSiblingCount(),
            resource.getLength(),
            resource.getDateContent(),
            resource.getVersion(),
            doc);
    }

    /**
     * Constructor, creates a new file object.<p>
     * 
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param path the filename of this resource
     * @param type the type of this resource
     * @param flags the flags of this resource
     * @param projectId the project id this resource was last modified in
     * @param state the state of this resource
     * @param dateCreated the creation date of this resource
     * @param userCreated the id of the user who created this resource
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     * @param linkCount the count of all siblings of this resource 
     * @param length the size of the file content of this resource
     * @param dateContent the date of the last modification of the content of this resource 
     * @param version the version number of this resource   
     * @param doc the search document
     */
    public CmsSearchResource(
        CmsUUID structureId,
        CmsUUID resourceId,
        String path,
        int type,
        int flags,
        CmsUUID projectId,
        CmsResourceState state,
        long dateCreated,
        CmsUUID userCreated,
        long dateLastModified,
        CmsUUID userLastModified,
        long dateReleased,
        long dateExpired,
        int linkCount,
        int length,
        long dateContent,
        int version,
        I_CmsSearchDocument doc) {

        super(
            structureId,
            resourceId,
            path,
            type,
            false,
            flags,
            projectId,
            state,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            linkCount,
            length,
            dateContent,
            version);

        m_doc = doc;
    }

    /**
     * Delegator.<p>
     * 
     * {@link I_CmsSearchDocument#getFieldValueAsDate(String)}
     * 
     * @param fieldName the field name to get the value for
     * 
     * @return the value
     */
    public Date getDateField(String fieldName) {

        return m_doc.getFieldValueAsDate(fieldName);
    }

    /**
     * Returns the document.<p>
     * 
     * @return the document
     */
    public I_CmsSearchDocument getDocument() {

        return m_doc;
    }

    /**
     * Delegator.<p>
     * 
     * {@link I_CmsSearchDocument#getFieldValueAsString(String)}
     * 
     * @param fieldName the field name to get the value for
     * 
     * @return the value
     */
    public String getField(String fieldName) {

        return m_doc.getFieldValueAsString(fieldName);
    }

    /**
     * Delegator.<p>
     * 
     * {@link I_CmsSearchDocument#getFieldValueAsString(String)}
     * 
     * @param fieldName the field name to get the value for
     * 
     * @return the value
     */
    public List<String> getMultivaluedField(String fieldName) {

        return m_doc.getMultivaluedFieldAsStringList(fieldName);
    }

    /**
     * Delegator.<p>
     * 
     * {@link I_CmsSearchDocument#getScore()}
     * 
     * Returns the score of this document.<p>
     * 
     * @param maxScore the maximum score of this search
     * 
     * @return the score
     */
    public int getScore(float maxScore) {

        return Math.round((m_doc.getScore() / maxScore) * 100f);
    }
}
