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

import org.opencms.acacia.shared.ContentDefinition;
import org.opencms.acacia.shared.Entity;
import org.opencms.acacia.shared.EntityHtml;
import org.opencms.acacia.shared.ValidationResult;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The content service used to load and persist entity and type information.<p>
 * 
 * Use this asynchronous interface on the client side.<p>
 */
public interface I_ContentServiceAsync {

    /**
     * Loads the content definition for a given type.<p>
     * 
     * @param entityId the entity id/URI
     * @param callback the asynchronous callback
     */
    void loadContentDefinition(String entityId, AsyncCallback<ContentDefinition> callback);

    /**
     * Saves the given entities and returns a validation result in case of invalid entities.<p>
     * 
     * @param entities the entities to save
     * @param callback the asynchronous callback
     */
    void saveEntities(List<Entity> entities, AsyncCallback<ValidationResult> callback);

    /**
     * Saves the given entity and returns a validation result in case of invalid entities.<p>
     * 
     * @param entity the entity to save
     * @param callback the asynchronous callback
     */
    void saveEntity(Entity entity, AsyncCallback<ValidationResult> callback);

    /**
     * Retrieves the updated entity HTML representation.<p>
     * The entity data will be validated but not persisted on the server.<p>
     * 
     * @param entity the entity
     * @param contextUri the context URI
     * @param htmlContextInfo information about the HTML context
     * @param callback the asynchronous callback
     */
    void updateEntityHtml(Entity entity, String contextUri, String htmlContextInfo, AsyncCallback<EntityHtml> callback);

    /**
     * Validates the given entities and returns maps of error and warning messages in case of invalid attributes.<p>
     * 
     * @param changedEntities the entities to validate
     * @param callback the asynchronous callback
     */
    void validateEntities(List<Entity> changedEntities, AsyncCallback<ValidationResult> callback);
}
