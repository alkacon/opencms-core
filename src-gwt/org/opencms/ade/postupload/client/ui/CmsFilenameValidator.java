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

package org.opencms.ade.postupload.client.ui;

import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.validation.I_CmsValidationController;
import org.opencms.gwt.client.validation.I_CmsValidator;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

/**
 * Validates modified file names for uploaded files.
 */
public class CmsFilenameValidator implements I_CmsValidator {

    /** The property definition for the file name. */
    private CmsXmlContentProperty m_propDef;

    /** The structure id of the uploaded file. */
    private CmsUUID m_structureId;

    /**
     * Creates a new instance.
     *
     * @param structureId the structure id of the uploaded file
     * @param propDef the property definition for the file name
     */
    public CmsFilenameValidator(CmsUUID structureId, CmsXmlContentProperty propDef) {

        m_propDef = propDef;
        m_structureId = structureId;

    }

    /**
     * Matches a string against a regex, and inverts the match if the regex starts with a '!'.<p>
     *
     * @param regex the regular expression
     * @param value the string to be matched
     *
     * @return true if the validation succeeded
     */
    private static boolean matchRuleRegex(String regex, String value) {

        if (value == null) {
            value = "";
        }

        if (regex == null) {
            return true;
        }
        if ((regex.length() > 0) && (regex.charAt(0) == '!')) {
            return !value.matches(regex.substring(1));
        } else {
            return value.matches(regex);
        }
    }

    /**
     * @see org.opencms.gwt.client.validation.I_CmsValidator#validate(org.opencms.gwt.client.ui.input.I_CmsFormField, org.opencms.gwt.client.validation.I_CmsValidationController)
     */
    @Override
    public void validate(I_CmsFormField field, I_CmsValidationController controller) {

        if (!matchRuleRegex(m_propDef.getRuleRegex(), field.getWidget().getFormValueAsString())) {
            controller.provideValidationResult(field.getId(), new CmsValidationResult(m_propDef.getError()));
        } else {
            controller.validateAsync(
                field.getId(),
                field.getWidget().getFormValueAsString(),
                "org.opencms.ade.postupload.CmsServerFilenameValidator",
                "" + m_structureId);
        }

    }

}
