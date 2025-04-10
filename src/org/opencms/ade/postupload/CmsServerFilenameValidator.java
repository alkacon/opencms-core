/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.postupload;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.I_CmsValidationService;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Checks whether an uploaded file's modified name is already used by another file.
 */
public class CmsServerFilenameValidator implements I_CmsValidationService {

    private static final Log LOG = CmsLog.getLog(CmsServerFilenameValidator.class);

    /**
     * @see org.opencms.gwt.I_CmsValidationService#validate(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public CmsValidationResult validate(CmsObject cms, String value, String config) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsUUID structureId = new CmsUUID(config);
        try {
            CmsResource parentFolder = cms.readParentFolder(structureId);
            String pathToCheck = CmsStringUtil.joinPaths(cms.getSitePath(parentFolder), value);
            boolean otherFileExists = false;
            try {
                CmsResource res = cms.readResource(pathToCheck, CmsResourceFilter.ALL);
                otherFileExists = !structureId.equals(res.getStructureId());
            } catch (CmsPermissionViolationException e) {
                otherFileExists = true;
            } catch (Exception e) {
                otherFileExists = false;
            }
            if (otherFileExists) {
                return new CmsValidationResult(
                    Messages.get().getBundle(locale).key(Messages.GUI_POSTUPLOAD_FILE_EXISTS_0));
            } else {
                return CmsValidationResult.VALIDATION_OK;
            }
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
            return new CmsValidationResult(e.getLocalizedMessage());
        }
    }

}
