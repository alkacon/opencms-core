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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The create new gallery folder dialog.<p>
 */
public class CmsCopyModelPageDialog extends A_CmsNewModelPageDialog {

    /** The callbackr. */
    private AsyncCallback<String> m_callback;

    /**
     * Constructor.<p>
     *
     * @param infoBean the list info bean to display in the dialog
     * @param isModelGroup if copying as model group
     * @param callback the callback to call with the title entered by the user
     */
    public CmsCopyModelPageDialog(CmsListInfoBean infoBean, boolean isModelGroup, AsyncCallback<String> callback) {

        super(
            isModelGroup
            ? Messages.get().key(Messages.GUI_COPY_AS_MODEL_GROUP_PAGE_DIALOG_TITLE_0)
            : Messages.get().key(Messages.GUI_COPY_MODEL_PAGE_DIALOG_TITLE_0),
            infoBean);
        m_callback = callback;
    }

    /**
     * Gets the description from the description text box.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_descriptionInput.getFormValueAsString();
    }

    /**
     * Creates the new gallery folder.<p>
     */
    @Override
    protected void onOk() {

        m_callback.onSuccess(m_titleInput.getFormValueAsString());
        hide();
    }

}
