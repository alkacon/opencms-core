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
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;

import java.util.HashMap;
import java.util.Map;

/**
 * A form validator which does nothing special and just validates form field values independently of each other.<p>
 *
 * @since 8.0.0
 */
public class CmsDefaultFormValidator implements I_CmsFormValidator {

    /**
     * @see org.opencms.gwt.I_CmsFormValidator#validate(org.opencms.file.CmsObject, java.util.Map, java.util.Map, java.lang.String)
     */
    public Map<String, CmsValidationResult> validate(
        CmsObject cms,
        Map<String, CmsValidationQuery> queries,
        Map<String, String> values,
        String config) throws Exception {

        Map<String, CmsValidationResult> result = new HashMap<String, CmsValidationResult>();
        for (Map.Entry<String, CmsValidationQuery> queryEntry : queries.entrySet()) {
            String fieldName = queryEntry.getKey();
            CmsValidationQuery query = queryEntry.getValue();
            I_CmsValidationService fieldValidator = CmsCoreService.instantiate(
                I_CmsValidationService.class,
                query.getValidatorId());
            CmsValidationResult fieldResult = fieldValidator.validate(cms, query.getValue(), query.getConfig());
            result.put(fieldName, fieldResult);
        }
        return result;
    }
}
