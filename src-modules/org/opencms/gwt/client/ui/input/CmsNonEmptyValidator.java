/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsNonEmptyValidator.java,v $
 * Date   : $Date: 2010/05/06 09:51:37 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

/**
 * A validator that checks whether a field is not empty.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
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
     * @see org.opencms.gwt.client.ui.input.I_CmsValidator#validate(org.opencms.gwt.client.ui.input.I_CmsFormField)
     */
    public boolean validate(I_CmsFormField field) {

        String value = field.getWidget().getFormValueAsString();
        if ((value == null) || value.trim().equals("")) {
            field.getWidget().setErrorMessage(m_errorMessage);
            return false;
        } else {
            field.getWidget().setErrorMessage(null);
            return true;
        }
    }
}
