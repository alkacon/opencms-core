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

package org.opencms.gwt.client.property;

import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.validation.I_CmsValidationController;
import org.opencms.gwt.client.validation.I_CmsValidator;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * Validator class for the URL name field in the property editor.<p>
 *
 * @since 8.0.0
 */
public class CmsUrlNameValidator implements I_CmsValidator {

    /** The server-side validator class. */
    private static final String SERVER_VALIDATOR = "org.opencms.gwt.CmsUrlNameValidationService";

    /** The path of the parent folder of the edited resource. */
    private String m_parentPath;

    /** The structure id of the edited resource. */
    private CmsUUID m_structureId;

    /**
     * Creates a new URL name validator.<p>
     *
     * @param parentPath the parent path of the resource for which the URL name is being validated
     * @param id the id of the resource whose URL name is being validated
     */
    public CmsUrlNameValidator(String parentPath, CmsUUID id) {

        m_parentPath = parentPath;
        m_structureId = id;
    }

    /**
     * @see org.opencms.gwt.client.validation.I_CmsValidator#validate(org.opencms.gwt.client.ui.input.I_CmsFormField, org.opencms.gwt.client.validation.I_CmsValidationController)
     */
    public void validate(I_CmsFormField field, I_CmsValidationController controller) {

        String value = field.getWidget().getFormValueAsString();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            String message = org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_URLNAME_CANT_BE_EMPTY_0);
            controller.provideValidationResult(field.getId(), new CmsValidationResult(message));
            return;
        }
        controller.validateAsync(
            field.getId(),
            value,
            SERVER_VALIDATOR,
            "parent:" + m_parentPath + "|id:" + m_structureId);
    }

}
