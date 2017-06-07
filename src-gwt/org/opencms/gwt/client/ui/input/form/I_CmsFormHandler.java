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

package org.opencms.gwt.client.ui.input.form;

/**
 * The interface for objects which should be notified when a {@link CmsForm} is successfully submitted.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsFormHandler {

    /**
     * Returns true if properties are currently being submitted.<p>
     *
     * @return true if properties are being submitted
     */
    boolean isSubmitting();

    /**
     * This method is called when the validation triggered by an attempt to submit the form has finished.<p>
     *
     * @param form the form
     * @param ok the validation result
     */
    void onSubmitValidationResult(CmsForm form, boolean ok);

    /**
     * This method is called when the normal validation triggered by changing fields has finished.<p>
     *
     * @param form the form
     * @param ok the validation result
     */
    void onValidationResult(CmsForm form, boolean ok);

}
