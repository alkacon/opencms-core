/*
 * T
his library is part of OpenCms -
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

package org.opencms.ui.apps.modules;

import org.opencms.file.CmsObject;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.Window;

/**
 * The dialog for selecting a site when exporting / deleting a module without a module site.<p>
 */
public class CmsSiteSelectDialog extends CmsBasicDialog {

    /**
     * Callback for the code using the dialog.<p>
     */
    interface I_Callback {

        /**
         * Called when the user cancels the site select dialog.<p>
         */
        void onCancel();

        /**
         * Called when the user selects a site.<p>
         *
         * @param site the selected site root
         */
        void onSiteSelect(String site);
    }

    /** The property id for the caption. */
    private static final String CAPTION_PROP = "caption";

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The callback to call when the dialog finishes. */
    private I_Callback m_callback;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The OK button. */
    private Button m_okButton;

    /** The site selector. */
    private ComboBox m_siteSelector;

    /**
     * Creates a new instance.<p>
     */
    public CmsSiteSelectDialog() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsObject cms = A_CmsUI.getCmsObject();
        IndexedContainer container = CmsVaadinUtils.getAvailableSitesContainer(cms, CAPTION_PROP);

        m_siteSelector.setContainerDataSource(container);
        m_siteSelector.setItemCaptionPropertyId(CAPTION_PROP);
        m_siteSelector.setNullSelectionAllowed(false);
        m_siteSelector.setValue(cms.getRequestContext().getSiteRoot());
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                m_callback.onSiteSelect(getSite());
            }
        });
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                m_callback.onCancel();
            }
        });
    }

    /**
     * Opens the site selection dialog in a window.<p>
     *
     * @param callback the callback to call when the dialog finishes
     * @param windowCaption the window caption
     */
    public static void openDialogInWindow(final I_Callback callback, String windowCaption) {

        final Window window = CmsBasicDialog.prepareWindow();
        window.setCaption(windowCaption);
        CmsSiteSelectDialog dialog = new CmsSiteSelectDialog();
        window.setContent(dialog);
        dialog.setCallback(new I_Callback() {

            public void onCancel() {

                window.close();
                callback.onCancel();

            }

            public void onSiteSelect(String site) {

                window.close();
                callback.onSiteSelect(site);
            }
        });
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Gets the selected site.<p>
     *
     * @return the selected site
     */
    public String getSite() {

        return (String)(m_siteSelector.getValue());
    }

    /**
     * Sets the callback that should be called when the dialog finishes.<p<
     *
     * @param callback the callback to call when the dialog finishes
     */
    public void setCallback(I_Callback callback) {

        m_callback = callback;
    }

}
