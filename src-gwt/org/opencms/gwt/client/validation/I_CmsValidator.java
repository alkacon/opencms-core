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

package org.opencms.gwt.client.validation;

import org.opencms.gwt.client.ui.input.I_CmsFormField;

/**
 * This interface is used to tell an object that it should either validate a form
 * field or request an asynchronous validation from a {@link I_CmsValidationController}.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsValidator {

    /**
     * If this method is called, the object should either validate the form field and report the result to the
     * validation controller, or request asynchronous validation of the field from the validation controller.<p>
     *
     * @param field the form field to be validated
     * @param controller the validation controller
     */
    void validate(I_CmsFormField field, I_CmsValidationController controller);
}
