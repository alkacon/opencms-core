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
import com.alkacon.acacia.shared.rpc.I_ContentServiceAsync;

import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The content editor asynchronous service interface.<p>
 */
public interface I_CmsContentServiceAsync extends I_ContentServiceAsync {

    /**
     * Loads the content definition for a given type.<p>
     * 
     * @param entityId the entity id/URI
     * @param callback the asynchronous callback
     */
    void loadDefinition(String entityId, AsyncCallback<CmsContentDefinition> callback);

    /**
     * Loads the content definition for a given type creating a new resource according to the new link and model file parameter.<p>
     * 
     * @param entityId the entity id/URI
     * @param newLink the new link
     * @param modelFileId  the optional model file id
     * @param editContext the container page currently being edited (may be null)
     * 
     * @param mode the content creation mode
     * @param postCreateHandler the post-create handler class name 
     * @param callback the asynchronous callback
     */
    void loadDefinition(String entityId, String newLink, CmsUUID modelFileId,

    String editContext,

    String mode, String postCreateHandler,

    AsyncCallback<CmsContentDefinition> callback);

    /**
     * Loads new entity definition.<p>
     * This will load the entity representation of a new locale node.<p>
     * 
     * @param entityId the entity id
     * @param lastLocale the last edited locale
     * @param skipPaths the paths to skip during locale synchronization
     * @param editedEntities the edited entities
     * @param newLocale states if a new locale should be generated
     * @param callback the asynchronous callback
     */
    void loadOtherLocale(
        String entityId,
        String lastLocale,
        Collection<String> skipPaths,
        Map<String, Entity> editedEntities,
        boolean newLocale,
        AsyncCallback<CmsContentDefinition> callback);

    /**
     * Returns the content definition of the resource requested through parameter 'resource'.<p>
     * 
     * @param callback the callback 
     */
    void prefetch(AsyncCallback<CmsContentDefinition> callback);

    /**
     * Saves and deletes the given entities. Returns a validation result in case of invalid entities.<p>
     * 
     * @param changedEntities the changed entities
     * @param deletedEntities the entity id's to delete
     * @param skipPaths the paths to skip during locale synchronization
     * @param lastEditedLocale the last edited locale
     * @param clearOnSuccess  <code>true</code> to unlock resource after saving
     * @param callback the asynchronous callback
     */
    void saveAndDeleteEntities(
        List<Entity> changedEntities,
        List<String> deletedEntities,
        Collection<String> skipPaths,
        String lastEditedLocale,
        boolean clearOnSuccess,
        AsyncCallback<ValidationResult> callback);

}
