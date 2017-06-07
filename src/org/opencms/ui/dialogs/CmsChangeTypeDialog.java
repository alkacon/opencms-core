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

import org.opencms.ade.containerpage.CmsAddDialogTypeHelper;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.Arrays;

import org.apache.commons.logging.Log;

import com.vaadin.server.FontAwesome;

/**
 * Dialog for changing the resource type.<p>
 */
public class CmsChangeTypeDialog extends CmsNewDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChangeTypeDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     *
     * @throws CmsException if something goes wrong
     */
    public CmsChangeTypeDialog(I_CmsDialogContext context)
    throws CmsException {
        super(A_CmsUI.getCmsObject().readParentFolder(context.getResources().get(0).getStructureId()), context);
        displayResourceInfo(context.getResources());
        m_defaultLocationCheckbox.setVisible(false);
    }

    /**
     * @see org.opencms.ui.dialogs.CmsNewDialog#handleSelection(org.opencms.ade.galleries.shared.CmsResourceTypeBean)
     */
    @Override
    public void handleSelection(CmsResourceTypeBean typeBean) {

        CmsResource changeRes = m_dialogContext.getResources().get(0);
        CmsObject cms = A_CmsUI.getCmsObject();
        CmsLockActionRecord lockRecord = null;
        try {
            lockRecord = CmsLockUtil.ensureLock(cms, changeRes);
            cms.chtype(cms.getSitePath(changeRes), OpenCms.getResourceManager().getResourceType(typeBean.getType()));
            m_dialogContext.finish(Arrays.asList(changeRes.getStructureId()));
        } catch (CmsException e) {
            m_dialogContext.error(e);
        } finally {
            if ((lockRecord != null) && (lockRecord.getChange() == LockChange.locked)) {
                try {
                    cms.unlockResource(changeRes);
                    m_dialogContext.finish(Arrays.asList(changeRes.getStructureId()));
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage());
                }
            }
        }

    }

    /**
     * @see org.opencms.ui.dialogs.CmsNewDialog#createTypeHelper()
     */
    @Override
    protected CmsAddDialogTypeHelper createTypeHelper() {

        return new CmsAddDialogTypeHelper() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected boolean exclude(CmsResourceTypeBean type) {

                boolean sameType = OpenCms.getResourceManager().matchResourceType(
                    type.getType(),
                    m_dialogContext.getResources().get(0).getTypeId());
                if (sameType) {
                    return true;
                }

                String typeName = type.getType();
                try {
                    boolean isFolder = m_dialogContext.getResources().get(0).isFolder();
                    boolean identicalTypeGroup = OpenCms.getResourceManager().getResourceType(
                        typeName).isFolder() == isFolder;
                    return !identicalTypeGroup;
                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                    return false;
                }
            }
        };
    }

    /**
     * @see org.opencms.ui.dialogs.CmsNewDialog#getActionIconHtml()
     */
    @Override
    protected String getActionIconHtml() {

        return FontAwesome.CHECK.getHtml();
    }

    /**
     * @see org.opencms.ui.dialogs.CmsNewDialog#getLabelClass()
     */
    @Override
    protected String getLabelClass() {

        return "o-checkIcon";
    }

    /**
     *
     * @see org.opencms.ui.dialogs.CmsNewDialog#getSubtitle(org.opencms.ade.galleries.shared.CmsResourceTypeBean, boolean)
     */
    @Override
    protected String getSubtitle(CmsResourceTypeBean type, boolean useDefault) {

        CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getType());
        if (explorerType != null) {
            String explorerInfo = explorerType.getInfo();
            if (explorerInfo != null) {
                return CmsVaadinUtils.getMessageText(explorerInfo);
            }
        }
        return "";

    }

}
