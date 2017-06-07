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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/**
 * Dialog used to change resource modification times.<p>
 */
public class CmsUndeleteDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUndeleteDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The OK  button. */
    private Button m_okButton;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsUndeleteDialog(I_CmsDialogContext context) {
        m_context = context;
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsUndeleteDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Undeletes the selected files
     *
     * @return the ids of the modified resources
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsUUID> undelete() throws CmsException {

        List<CmsUUID> modifiedResources = new ArrayList<CmsUUID>();
        CmsObject cms = m_context.getCms();
        for (CmsResource resource : m_context.getResources()) {
            CmsLockActionRecord actionRecord = null;
            try {
                actionRecord = CmsLockUtil.ensureLock(m_context.getCms(), resource);
                cms.undeleteResource(cms.getSitePath(resource), true);
                modifiedResources.add(resource.getStructureId());
            } finally {
                if ((actionRecord != null) && (actionRecord.getChange() == LockChange.locked)) {

                    try {
                        cms.unlockResource(resource);
                    } catch (CmsLockException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return modifiedResources;
    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(new ArrayList<CmsUUID>());
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        try {
            List<CmsUUID> modifiedResources = undelete();
            m_context.finish(modifiedResources);
        } catch (Exception e) {
            m_context.error(e);
        }
    }

}
