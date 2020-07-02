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

import org.opencms.gwt.CmsCoreService;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsExtendedSiteSelector;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;

import org.apache.commons.logging.Log;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;

/**
 * The site select dialog.<p>
 */
public class CmsSiteSelectDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsSiteSelectDialog.class);

    /** The serial version id. */
    private static final long serialVersionUID = 4455901453008760434L;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The site select. */
    private CmsExtendedSiteSelector m_siteComboBox;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     */
    public CmsSiteSelectDialog(CmsEmbeddedDialogContext context) {

        m_context = context;
        setContent(initForm());
        m_cancelButton = createButtonCancel();
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });
        addButton(m_cancelButton);

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsSiteSelectDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Actually changes the site.
     *
     * @param context the dialog context
     * @param option the site selector option
     */
    public static void changeSite(CmsEmbeddedDialogContext context, CmsExtendedSiteSelector.SiteSelectorOption option) {

        if ((option.getPath() != null)
            || !CmsStringUtil.comparePaths(context.getCms().getRequestContext().getSiteRoot(), option.getSite())) {
            String siteRoot = option.getSite();
            A_CmsUI.get().changeSite(option.getSite());
            if (CmsStringUtil.comparePaths(siteRoot, "/") || OpenCms.getSiteManager().isSharedFolder(siteRoot)) {
                // switch to explorer view when selecting shared or root site

                Page.getCurrent().open(CmsCoreService.getFileExplorerLink(A_CmsUI.getCmsObject(), siteRoot), "_top");
                return;
            }
            context.finish(option);
        } else {
            context.finish(null, null);
        }
    }

    /**
     * Cancels the dialog action.<p>
     */
    void cancel() {

        m_context.finish(Collections.<CmsUUID> emptyList());
    }

    /**
     * Submits the dialog action.<p>
     */
    void submit() {

        CmsExtendedSiteSelector.SiteSelectorOption option = m_siteComboBox.getValue();
        I_CmsDialogContext context = m_context;
        changeSite((CmsEmbeddedDialogContext)context, option);
    }

    /**
     * Initializes the form component.<p>
     *
     * @return the form component
     */
    private FormLayout initForm() {

        FormLayout form = new FormLayout();
        form.setWidth("100%");
        m_siteComboBox = prepareSiteSelector(org.opencms.workplace.Messages.GUI_LABEL_SITE_0);
        m_siteComboBox.setValue(
            new CmsExtendedSiteSelector.SiteSelectorOption(
                A_CmsUI.getCmsObject().getRequestContext().getSiteRoot(),
                null,
                null));
        form.addComponent(m_siteComboBox);
        ValueChangeListener changeListener = new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                submit();
            }
        };
        m_siteComboBox.addValueChangeListener(evt -> submit());
        return form;
    }

    /**
     * Prepares a combo box.<p>
     *
     * @param captionKey the caption message key
     *
     * @return the combo box
     */
    private CmsExtendedSiteSelector prepareSiteSelector(String captionKey) {

        CmsExtendedSiteSelector result = new CmsExtendedSiteSelector();
        String caption = CmsVaadinUtils.getWpMessagesForCurrentLocale().key(captionKey);
        result.setCaption(caption);
        result.initOptions(m_context.getCms(), true);
        result.setPageLength(CmsExtendedSiteSelector.LONG_PAGE_LENGTH);
        result.setWidth("100%");
        result.setPlaceholder(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
        return result;
    }
}
