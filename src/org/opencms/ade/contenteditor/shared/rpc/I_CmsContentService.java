/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.shared.rpc;

import com.alkacon.acacia.shared.Entity;
import com.alkacon.acacia.shared.ValidationResult;
import com.alkacon.acacia.shared.rpc.I_ContentService;

import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * The content editor service interface.<p>
 */
public interface I_CmsContentService extends I_ContentService {

    /** The content definition dictionary name. */
    String DICT_CONTENT_DEFINITION = "com_alkacon_acacia_shared_ContentDefinition";

    /** The back-link parameter. */
    String PARAM_BACKLINK = "backlink";

    /**
     * Loads the content definition for a given entity.<p>
     * 
     * @param entityId the entity id/URI
     * 
     * @return the content definition
     * 
     * @throws Exception if something goes wrong processing the request
     */
    CmsContentDefinition loadDefinition(String entityId) throws Exception;

    /**
     * Loads the content definition for a given entity.<p>
     * 
     * @param entityId the entity id/URI
     * @param newLink the new link
     * @param modelFileId  the optional model file id
     * @param editContext the container page currently being edited (may be null)
     * 
     * @return the content definition
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContentDefinition loadDefinition(String entityId, String newLink, CmsUUID modelFileId, String editContext)
    throws CmsRpcException;

    /**
     * Loads new entity definition.<p>
     * This will load the entity representation of a new locale node.<p>
     * 
     * @param entityId the entity id/URI
     * 
     * @return the content definition
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContentDefinition loadNewDefinition(String entityId) throws CmsRpcException;

    /**
     * Returns the content definition of the resource requested through parameter 'resource'.<p>
     * 
     * @return the content definition
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsContentDefinition prefetch() throws CmsRpcException;

    /**
     * Saves and deletes the given entities. Returns a validation result in case of invalid entities.<p>
     * 
     * @param changedEntities the changed entities
     * @param deletedEntities the entity id's to delete
     * @param clearOnSuccess  <code>true</code> to unlock resource after saving
     * 
     * @return the validation result in case of invalid entities
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    ValidationResult saveAndDeleteEntities(
        List<Entity> changedEntities,
        List<String> deletedEntities,
        boolean clearOnSuccess) throws CmsRpcException;
}
