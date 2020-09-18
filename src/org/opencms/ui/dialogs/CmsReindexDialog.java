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
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.CmsReindexDialogAction;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;

/**
 * Dialog used to change resource modification times.<p>
 */
public class CmsReindexDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsReindexDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The OK  button. */
    private Button m_okButton;

    /** Label with info text. */
    private Label m_infoText;

    /** The checkbox, telling if related should be reindexed as well. */
    private CheckBox m_reindexRelated;

    /** Flag, indicating if we are in the online project. */
    private boolean m_isOnline;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsReindexDialog(I_CmsDialogContext context) {

        m_context = context;
        m_isOnline = context.getCms().getRequestContext().getCurrentProject().isOnlineProject();
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        String indexType = CmsVaadinUtils.getMessageText(
            m_isOnline
            ? org.opencms.workplace.commons.Messages.GUI_REINDEX_INDEX_TYPE_ONLINE_0
            : org.opencms.workplace.commons.Messages.GUI_REINDEX_INDEX_TYPE_OFFLINE_0);
        m_infoText.setValue(
            CmsVaadinUtils.getMessageText(
                org.opencms.workplace.commons.Messages.GUI_REINDEX_CONFIRMATION_1,
                indexType));
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                reindex();
                m_context.finish(new ArrayList<CmsUUID>());

            }
        });
        displayResourceInfo(m_context.getResources());

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsReindexDialog.this.cancel();
            }

            @Override
            protected void ok() {

                reindex();
            }
        });
    }

    /**
     * Triggers reindexing.<p>
     */
    protected void reindex() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Map<String, Object> eventData = new HashMap<>(3);
        if (!m_isOnline) {
            eventData.put(I_CmsEventListener.KEY_PROJECTID, cms.getRequestContext().getCurrentProject().getId());
        }
        eventData.put(I_CmsEventListener.KEY_RESOURCES, m_context.getResources());
        eventData.put(
            I_CmsEventListener.KEY_REPORT,
            new CmsLogReport(CmsLocaleManager.getDefaultLocale(), CmsReindexDialogAction.class));
        eventData.put(I_CmsEventListener.KEY_USER_ID, cms.getRequestContext().getCurrentUser().getId());
        Boolean reindexRelated = m_reindexRelated.getValue();
        eventData.put(I_CmsEventListener.KEY_REINDEX_RELATED, reindexRelated);
        CmsEvent reindexEvent = new CmsEvent(
            m_isOnline ? I_CmsEventListener.EVENT_REINDEX_ONLINE : I_CmsEventListener.EVENT_REINDEX_OFFLINE,
            eventData);
        OpenCms.fireCmsEvent(reindexEvent);

    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(new ArrayList<CmsUUID>());
    }
}
