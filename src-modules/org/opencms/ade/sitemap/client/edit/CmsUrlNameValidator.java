/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsUrlNameValidator.java,v $
 * Date   : $Date: 2011/05/03 10:49:11 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.validation.I_CmsValidationController;
import org.opencms.gwt.client.validation.I_CmsValidator;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * Validator class for the URL name field in the sitemap entry editor.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsUrlNameValidator implements I_CmsValidator {

    /** The server-side validator class. */
    private static final String SERVER_VALIDATOR = "org.opencms.ade.sitemap.CmsUrlNameValidationService";

    /** The other url names which the URL name should not be equal to. */
    private List<String> m_otherUrlNames;

    /**
     * Creates a new URL name validator which checks that the translated URL name does not already exist
     * in a list of URL names.<p>
     * 
     * @param otherUrlNames the URL names which the URL name which is validated should not equal 
     */
    public CmsUrlNameValidator(List<String> otherUrlNames) {

        m_otherUrlNames = otherUrlNames;
    }

    /**
     * @see org.opencms.gwt.client.validation.I_CmsValidator#validate(org.opencms.gwt.client.ui.input.I_CmsFormField, org.opencms.gwt.client.validation.I_CmsValidationController)
     */
    public void validate(I_CmsFormField field, I_CmsValidationController controller) {

        String value = field.getWidget().getFormValueAsString();

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            String message = Messages.get().key(Messages.GUI_URLNAME_CANT_BE_EMPTY_0);
            controller.provideValidationResult(field.getId(), new CmsValidationResult(message));
            return;
        }
        controller.validateAsync(field.getId(), value, SERVER_VALIDATOR, CmsStringUtil.listAsString(
            m_otherUrlNames,
            "|"));
    }

}
