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

import org.opencms.gwt.shared.CmsValidationResult;

/**
 * This is the interface which an {@link I_CmsValidator} object uses to either synchronously report the result of a validation
 * or to request an asynchronous validation from the server.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsValidationController {

    /**
     * Reports the result of a synchronous validation.<p>
     *
     * @param field the field name
     * @param result the validation result
     */
    void provideValidationResult(String field, CmsValidationResult result);

    /**
     * Requests an server-side validation to be performed later.<p>
     *
     * @param field the field name
     * @param value the value of the field
     * @param validator the server-side validator class name
     * @param config the configuration string for the server-side validator
     */
    void validateAsync(String field, String value, String validator, String config);
}
