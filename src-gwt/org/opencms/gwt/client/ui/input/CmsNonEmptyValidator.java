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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.validation.I_CmsValidationController;
import org.opencms.gwt.client.validation.I_CmsValidator;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsStringUtil;

/**
 * A validator that checks whether a field is not empty.<p>
 *
 * @since 8.0.0
 */
public class CmsNonEmptyValidator implements I_CmsValidator {

    /** The error message to display if the validation fails. */
    private String m_errorMessage;

    /**
     * Constructs a new validator with a given error message.<p>
     *
     * @param errorMessage the error message to use when the validated field is empty
     */
    public CmsNonEmptyValidator(String errorMessage) {

        m_errorMessage = errorMessage;
    }

    /**
     * @see org.opencms.gwt.client.validation.I_CmsValidator#validate(org.opencms.gwt.client.ui.input.I_CmsFormField, org.opencms.gwt.client.validation.I_CmsValidationController)
     */
    public void validate(I_CmsFormField field, I_CmsValidationController controller) {

        String value = field.getWidget().getFormValueAsString();
        CmsValidationResult result;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            result = new CmsValidationResult(m_errorMessage);
        } else {
            result = CmsValidationResult.VALIDATION_OK;
        }
        controller.provideValidationResult(field.getId(), result);
    }
}
