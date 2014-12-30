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

package org.opencms.acacia.shared.rpc;

import org.opencms.acacia.shared.CmsContentDefinition;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityHtml;
import org.opencms.acacia.shared.CmsValidationResult;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The content service used to load and persist entity and type information.<p>
 * 
 * Implement this on the server side.<p>
 */
public interface I_CmsContentService extends RemoteService {

    /**
     * Loads the content definition for a given entity.<p>
     * 
     * @param entityId the entity id/URI
     * 
     * @return the content type definition
     * 
     * @throws Exception if something goes wrong processing the request
     */
    CmsContentDefinition loadContentDefinition(String entityId) throws Exception;

    /**
     * Saves the given entities and returns a validation result in case of invalid entities.<p>
     * Invalid entities will not be saved.<p>
     * 
     * @param entities the entities to save
     * 
     * @return the validation result in case of invalid entities
     * 
     * @throws Exception if something goes wrong processing the request
     */
    CmsValidationResult saveEntities(List<CmsEntity> entities) throws Exception;

    /**
     * Saves the given entity and returns a validation result in case of invalid entities.<p>
     * Invalid entities will not be saved.<p>
     * 
     * @param entity the entity to save
     * 
     * @return the validation result in case of invalid entities
     * 
     * @throws Exception if something goes wrong processing the request
     */
    CmsValidationResult saveEntity(CmsEntity entity) throws Exception;

    /**
     * Retrieves the updated entity HTML representation.<p>
     * The entity data will be validated but not persisted on the server.<p>
     * 
     * @param entity the entity
     * @param contextUri the context URI
     * @param htmlContextInfo information about the HTML context
     * 
     * @return the HTML representation including the validation result
     * 
     * @throws Exception if something goes wrong processing the request
     */
    CmsEntityHtml updateEntityHtml(CmsEntity entity, String contextUri, String htmlContextInfo) throws Exception;

    /**
     * Validates the given entities and returns maps of error and warning messages in case of invalid attributes.<p>
     * 
     * @param changedEntities the entities to validate
     * 
     * @return the validation result
     * 
     * @throws Exception if something goes wrong processing the request
     */
    CmsValidationResult validateEntities(List<CmsEntity> changedEntities) throws Exception;
}
