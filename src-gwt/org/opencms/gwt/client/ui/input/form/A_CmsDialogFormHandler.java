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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class A_CmsDialogFormHandler implements I_CmsFormHandler {

    protected CmsFormDialog m_dialog;
    protected CmsForm m_form;

    public void onSubmitForm(Map<String, String> values, Set<String> editedFields) {

    }

    public void onSubmitValidationResult(boolean ok) {

        if (ok) {
            m_dialog.closeDialog();
            Map<String, String> values = m_form.collectValues();
            Set<String> editedFields = new HashSet<String>(m_form.getEditedFields());
            editedFields.retainAll(values.keySet());
            onSubmitForm(values, editedFields);
        } else {
            m_dialog.setOkButtonEnabled(m_form.noFieldsInvalid());
        }
    }

    public void onValidationResult(boolean ok) {

        m_dialog.setOkButtonEnabled(ok);
    }

    public void setDialog(CmsFormDialog dialog) {

        m_dialog = dialog;
    }

    public void setForm(CmsForm form) {

        m_form = form;
    }

}
