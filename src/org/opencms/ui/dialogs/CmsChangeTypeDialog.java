/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.ui.I_CmsDialogContext;

import java.util.Arrays;

import org.apache.commons.logging.Log;

/**
 * Dialog for changing the resource type.<p>
 */
public class CmsChangeTypeDialog extends CmsNewDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChangeTypeDialog.class);

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
     *
     * @see org.opencms.ui.dialogs.CmsNewDialog#getSubtitle(org.opencms.ade.galleries.shared.CmsResourceTypeBean, boolean)
     */
    @Override
    protected String getSubtitle(CmsResourceTypeBean type, boolean useDefault) {

        return "";
    }

    /**
     * @see org.opencms.ui.dialogs.CmsNewDialog#getTypeHelper()
     */
    @Override
    protected CmsAddDialogTypeHelper getTypeHelper() {

        return new CmsAddDialogTypeHelper() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected boolean exclude(CmsResourceTypeBean type) {

                String typeName = type.getType();
                try {
                    boolean isFolder = m_dialogContext.getResources().get(0).isFolder();
                    return OpenCms.getResourceManager().getResourceType(typeName).isFolder() != isFolder;
                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                    return false;
                }
            }
        };
    }

}
