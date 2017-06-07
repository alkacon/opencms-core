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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Map;

/**
 * Validation class which both translates a sitemap URL name and checks whether it already exists in a '|'-separated
 * list of URL names which is passed as a configuration parameter.<p>
 *
 * @since 8.0.0
 */
public class CmsUrlNameValidationService implements I_CmsValidationService {

    /**
     * @see org.opencms.gwt.I_CmsValidationService#validate(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public CmsValidationResult validate(CmsObject cms, String value, String config) {

        String name = cms.getRequestContext().getFileTranslator().translateResource(value);
        name = name.replace('/', '_');

        Map<String, String> configMap = CmsStringUtil.splitAsMap(config, "|", ":");
        String parentPath = configMap.get("parent");
        String id = configMap.get("id");
        try {
            CmsResource res = cms.readResource(CmsStringUtil.joinPaths(parentPath, name));
            // file already exists
            if (!CmsUUID.isValidUUID(id) || res.getStructureId().toString().equals(id)) {
                // no problem, it's the same resource
                return new CmsValidationResult(null, name);
            } else {
                // it's a different resource, so we fail
                return new CmsValidationResult(
                    org.opencms.gwt.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                        org.opencms.gwt.Messages.ERR_URL_NAME_ALREADY_EXISTS_1,
                        name));
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, the resource was not found
            return new CmsValidationResult(null, name);
        } catch (CmsException e) {
            throw new CmsRuntimeException(e.getMessageContainer());

        }
    }
}
