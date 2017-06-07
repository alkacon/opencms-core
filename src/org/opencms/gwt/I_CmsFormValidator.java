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

import java.util.Map;

/**
 * Interface for handling the server-side validation of a whole form.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsFormValidator {

    /**
     * Validates the form and returns the validation result.<p>
     *
     * Implementations of this interface may or may not use the validators in the validation queries passed as
     * an argument.<p>
     *
     * @param cms the CMS context
     * @param queries the validation queries for the form fields, indexed by form field key
     * @param values the form field values
     * @param config the configuration for the form validator
     *
     * @return a map of the validation results, indexed by form field key
     *
     * @throws Exception if something goes wrong
     */
    Map<String, CmsValidationResult> validate(
        CmsObject cms,
        Map<String, CmsValidationQuery> queries,
        Map<String, String> values,
        String config) throws Exception;

}
