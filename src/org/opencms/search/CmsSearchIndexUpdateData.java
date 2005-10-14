/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndexUpdateData.java,v $
 * Date   : $Date: 2005/10/14 10:22:38 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsPublishedResource;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of resources for the incremental update of a search index.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.1
 */
public class CmsSearchIndexUpdateData {

    /** The indexer of this update collection. */
    private I_CmsIndexer m_indexer;

    /** List of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be deleted. */
    private List m_resourcesToDelete;

    /** List of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be updated. */
    private List m_resourcesToUpdate;

    /** The search index source of this update collection. */
    private CmsSearchIndexSource m_source;

    /**
     * Creates a new instance of an update collection.<p>
     * 
     * @param source the search index source of this update collection
     * @param indexer the indexer of this update collection
     */
    public CmsSearchIndexUpdateData(CmsSearchIndexSource source, I_CmsIndexer indexer) {

        m_source = source;
        m_indexer = indexer;
        m_resourcesToDelete = new ArrayList();
        m_resourcesToUpdate = new ArrayList();
    }

    /**
     * Adds the given resource to the resources that must be deleted from the search index.<p>
     * 
     * @param resource the resource to add
     */
    public void addResourceToDelete(CmsPublishedResource resource) {

        m_resourcesToDelete.add(resource);
    }

    /**
     * Adds the given resource to the resources that must be updated in the search index.<p>
     * 
     * @param resource the resource to add
     */
    public void addResourceToUpdate(CmsPublishedResource resource) {

        m_resourcesToUpdate.add(resource);
    }

    /**
     * Returns the indexer of this update collection.<p>
     * 
     * @return the indexer of this update collection
     */
    public I_CmsIndexer getIndexer() {

        return m_indexer;
    }

    /**
     * Returns the list of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be deleted.<p>
     * 
     * @return the list of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be deleted
     */
    public List getResourcesToDelete() {

        return m_resourcesToDelete;
    }

    /**
     * Returns the list of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be updated.<p>
     * 
     * @return the list of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be updated
     */
    public List getResourcesToUpdate() {

        return m_resourcesToUpdate;
    }

    /**
     * Returns the search index source of this update collection.<p>
     * 
     * @return the search index source of this update collection
     */
    public CmsSearchIndexSource getSource() {

        return m_source;
    }

    /**
     * Returns <code>true</code> if this collections contains resources to delete.<p>
     * 
     * @return <code>true</code> if this collections contains resources to delete
     */
    public boolean hasResourcesToDelete() {

        return !m_resourcesToDelete.isEmpty();
    }

    /**
     * Returns <code>true</code> if this collections contains resources to update.<p>
     * 
     * @return <code>true</code> if this collections contains resources to update
     */
    public boolean hasResourceToUpdate() {

        return !m_resourcesToUpdate.isEmpty();
    }

    /**
     * Returns <code>true</code> if this collections contains no resources to update or delete.<p>
     * 
     * @return <code>true</code> if this collections contains no resources to update or delete
     */
    public boolean isEmpty() {

        return m_resourcesToDelete.isEmpty() && m_resourcesToUpdate.isEmpty();
    }
}