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
 * Basic regular expression validator for widgets of field type string.<p>
 *
 * @since 8.0.0
 */
public class CmsRegexValidator implements I_CmsValidator {

    /** If true, the validator always allows empty input. */
    private boolean m_alwaysAllowEmpty;

    /** The message to be displayed when the validation fails. */
    private String m_message;

    /** The regex used for validation. */
    private String m_regex;

    /**
     * Creates a new regex-based validator.
     *
     * The regular expression passed as a parameter is used to match complete strings, not parts of strings.
     * For example, a regex of "AAA" will only match the string "AAA", and not "BAAA".
     * If the regex starts with an exclamation mark ("!"), the match will be inverted, i.e. only strings that don't
     * match the rest of the regular expression will be interpreted as valid.
     *
     * @param regex a regular expression
     * @param message an error message
     * @param alwaysAllowEmpty if true, the validation will always allow the empty string
     */
    public CmsRegexValidator(String regex, String message, boolean alwaysAllowEmpty) {

        assert message != null;
        m_regex = regex;
        m_message = message;
        m_alwaysAllowEmpty = alwaysAllowEmpty;
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
    public void validate(I_CmsFormField field, I_CmsValidationController controller) {

        I_CmsFormWidget widget = field.getWidget();
        if (widget.getFieldType() == I_CmsFormWidget.FieldType.STRING) {
            String value = widget.getFormValueAsString();
            CmsValidationResult result;
            if (CmsStringUtil.isEmpty(value) && m_alwaysAllowEmpty) {
                result = CmsValidationResult.VALIDATION_OK;
            } else if (!matchRuleRegex(m_regex, value)) {
                result = new CmsValidationResult(m_message);
            } else {
                result = CmsValidationResult.VALIDATION_OK;
            }
            controller.provideValidationResult(field.getId(), result);
        }
    }
}
