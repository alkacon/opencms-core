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

import com.alkacon.acacia.shared.rpc.I_ContentService;

import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.gwt.CmsRpcException;

/**
 * The content editor service interface.<p>
 */
public interface I_CmsContentService extends I_ContentService {

    /** The content definition dictionary name. */
    String DICT_CONTENT_DEFINITION = "com_alkacon_acacia_shared_ContentDefinition";

    /** The back-link parameter. */
    String PARAM_BACKLINK = "backlink";

    /**
     * Returns the content definition of the resource requested through parameter 'resource'.<p>
     * 
     * @return the content definition
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsContentDefinition prefetch() throws CmsRpcException;

    /**
     * Loads the content definition for a given entity.<p>
     * 
     * @param entityId the entity id/URI
     * @param locale the entity content locale
     * 
     * @return the content type definition
     * 
     * @throws Exception if something goes wrong processing the request
     */
    CmsContentDefinition loadDefinition(String entityId, String locale) throws Exception;

}
