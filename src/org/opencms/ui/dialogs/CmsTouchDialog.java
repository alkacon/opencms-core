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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
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
import org.opencms.ui.components.CmsDateField;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;

/**
 * Dialog used to change resource modification times.<p>
 */
public class CmsTouchDialog extends CmsBasicDialog {

    /** Logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTouchDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The date selection field. */
    private CmsDateField m_dateField;

    /** Check box to enable/disable modification of children. */
    private CheckBox m_modifySubresourcesField;

    /** The OK  button. */
    private Button m_okButton;

    /** Checkbox to enable/disable rewriting of contents. */
    private CheckBox m_rewriteContentField;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsTouchDialog(I_CmsDialogContext context) {
        m_context = context;
        boolean hasFolders = false;

        for (CmsResource resource : context.getResources()) {
            if (resource.isFolder()) {
                hasFolders = true;
                break;
            }
        }

        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        m_modifySubresourcesField.setVisible(hasFolders);

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
        m_dateField.setValue(new Date());
        displayResourceInfo(m_context.getResources());
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsTouchDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Touches the selected files.<p>
     *
     * @throws CmsException if something goes wrong
     */
    protected void touchFiles() throws CmsException {

        Date touchDate = m_dateField.getValue();
        boolean validDate = touchDate != null;
        long touchTime = touchDate != null ? touchDate.getTime() : 0;
        boolean recursive = m_modifySubresourcesField.getValue().booleanValue();
        boolean rewriteContent = m_rewriteContentField.getValue().booleanValue();
        List<CmsUUID> changedIds = Lists.newArrayList();
        for (CmsResource resource : m_context.getResources()) {
            CmsLockActionRecord actionRecord = null;
            try {
                actionRecord = CmsLockUtil.ensureLock(m_context.getCms(), resource);
                touchSingleResource(
                    m_context.getCms().getSitePath(resource),
                    touchTime,
                    recursive,
                    validDate,
                    rewriteContent);
                changedIds.add(resource.getStructureId());
            } finally {
                if ((actionRecord != null) && (actionRecord.getChange() == LockChange.locked)) {
                    try {
                        m_context.getCms().unlockResource(resource);
                    } catch (CmsLockException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }

            }

        }
        m_context.finish(changedIds);

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
            touchFiles();
        } catch (Exception e) {
            m_context.error(e);
        }
    }

    /**
     * Rewrites the content of the given file.<p>
     *
     * @param resource the resource to rewrite the content for
     *
     * @throws CmsException if something goes wrong
     */
    private void hardTouch(CmsResource resource) throws CmsException {

        CmsFile file = m_context.getCms().readFile(resource);
        CmsObject cms = OpenCms.initCmsObject(m_context.getCms());
        cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
        file.setContents(file.getContents());
        cms.writeFile(file);
    }

    /**
     * Performs a touch operation for a single resource.<p>
     *
     * @param resourceName the resource name of the resource to touch
     * @param timeStamp the new time stamp
     * @param recursive the flag if the touch operation is recursive
     * @param correctDate the flag if the new time stamp is a correct date
     * @param touchContent if the content has to be rewritten
     *
     * @throws CmsException if touching the resource fails
     */
    private void touchSingleResource(
        String resourceName,
        long timeStamp,
        boolean recursive,
        boolean correctDate,
        boolean touchContent)
    throws CmsException {

        CmsObject cms = m_context.getCms();
        CmsResource sourceRes = cms.readResource(resourceName, CmsResourceFilter.ALL);
        if (!correctDate) {
            // no date value entered, use current resource modification date
            timeStamp = sourceRes.getDateLastModified();
        }
        cms.setDateLastModified(resourceName, timeStamp, recursive);

        if (touchContent) {
            if (sourceRes.isFile()) {
                hardTouch(sourceRes);
            } else if (recursive) {
                Iterator<CmsResource> it = cms.readResources(resourceName, CmsResourceFilter.ALL, true).iterator();
                while (it.hasNext()) {
                    CmsResource subRes = it.next();
                    if (subRes.isFile()) {
                        hardTouch(subRes);
                    }
                }
            }
        }
    }
}
