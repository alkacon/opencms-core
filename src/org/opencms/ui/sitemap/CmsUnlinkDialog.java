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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleGroupService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Dialog to confirm detaching a resource from a locale group.<p>
 */
public class CmsUnlinkDialog extends CmsBasicDialog {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUnlinkDialog.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The cancel button. */
    protected Button m_cancelButton;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** The locale comparison context. */
    protected I_CmsLocaleCompareContext m_localeContext;

    /** The label with the confirmation message. */
    protected Label m_messageLabel;

    /** The OK button. */
    protected Button m_okButton;

    /** The other resource.*/
    protected CmsResource m_otherResource;

    /** The container for the resource boxes. */
    protected HorizontalLayout m_resourceBoxContainer;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     * @param otherResource the other resource
     */
    public CmsUnlinkDialog(I_CmsDialogContext context, CmsResource otherResource) {
        super();
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_context = context;
        m_otherResource = otherResource;
        CmsResource leftResource = m_context.getResources().get(0);
        try {
            if (leftResource.isFolder()) {
                CmsResource defaultFile = context.getCms().readDefaultFile(
                    leftResource,
                    CmsResourceFilter.IGNORE_EXPIRATION);
                if (defaultFile != null) {
                    leftResource = defaultFile;
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        CmsResource rightResource = m_otherResource;
        CmsResourceInfo left = new CmsResourceInfo(leftResource);
        CmsResourceInfo right = new CmsResourceInfo(rightResource);
        Locale leftLocale = OpenCms.getLocaleManager().getDefaultLocale(context.getCms(), leftResource);
        Locale rightLocale = OpenCms.getLocaleManager().getDefaultLocale(context.getCms(), rightResource);

        left.getTopLine().setValue("[" + leftLocale.toString().toUpperCase() + "] " + left.getTopLine().getValue());
        right.getTopLine().setValue("[" + rightLocale.toString().toUpperCase() + "] " + right.getTopLine().getValue());

        m_resourceBoxContainer.addComponent(left);
        m_resourceBoxContainer.addComponent(right);

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickOk();
            }
        });

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickCancel();
            }
        });

    }

    /**
     * Called when the Cancel button is clicked.<p>
     */
    protected void onClickCancel() {

        m_context.finish(new ArrayList<CmsUUID>());

    }

    /**
     * Called when the OK button is clicked.<p>
     */
    protected void onClickOk() {

        CmsResource res1 = m_context.getResources().get(0);
        CmsResource res2 = m_otherResource;
        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            CmsLocaleGroupService groupService = cms.getLocaleGroupService();
            groupService.detachLocaleGroup(res1, res2);
            m_context.finish(Arrays.asList(m_context.getResources().get(0).getStructureId()));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            m_context.error(e);
        }

    }

}
