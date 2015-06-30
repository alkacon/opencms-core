/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
 * Abstract handler superclass for forms which have their own dialog.<p>
 */
public class CmsDialogFormHandler implements I_CmsFormHandler {

    /** The form dialog. */
    protected CmsFormDialog m_dialog;

    /** The form submit handler. */
    protected I_CmsFormSubmitHandler m_submitHandler;

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
     */
    public void onSubmitValidationResult(CmsForm form, boolean ok) {

        if (ok) {
            m_dialog.hide();
            form.handleSubmit(m_submitHandler);
        } else {
            m_dialog.setOkButtonEnabled(form.noFieldsInvalid());
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
     */
    public void onValidationResult(CmsForm form, boolean ok) {

        m_dialog.setOkButtonEnabled(ok);
    }

    /**
     * Sets the dialog.<p>
     *
     * @param dialog the form dialog
     */
    public void setDialog(CmsFormDialog dialog) {

        m_dialog = dialog;
    }

    /**
     * Sets the form submit handler.<p>
     *
     * @param submitHandler the new form submit handler
     */
    public void setSubmitHandler(I_CmsFormSubmitHandler submitHandler) {

        m_submitHandler = submitHandler;
    }
}
