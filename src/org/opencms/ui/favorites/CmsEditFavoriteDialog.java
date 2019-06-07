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

package org.opencms.ui.favorites;

import org.opencms.json.JSONException;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

/**
 * Dialog for editing bookmark title.
 */
public class CmsEditFavoriteDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditFavoriteDialog.class);

    /** The callback to call after the title is changed. */
    private Consumer<CmsFavoriteEntry> m_callback;

    /** The cancel button. */
    private Button m_cancelButton;

    /** Boolean flag indicating whether the text field has changed. */
    private boolean m_changed;

    /** The favorite entry. */
    private CmsFavoriteEntry m_entry;

    /** The OK button. */
    private Button m_okButton;

    /** The title field. */
    private TextField m_title;

    /**
     * Creates a new instance.
     *
     * @param info the info widget
     * @param callback the callback
     */
    public CmsEditFavoriteDialog(CmsFavInfo info, Consumer<CmsFavoriteEntry> callback) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsFavoriteEntry entry = info.getEntry();
        m_callback = callback;
        m_okButton.addClickListener(evt -> onClickOk());
        String customTitle = entry.getCustomTitle();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(customTitle)) {
            customTitle = info.getTopLine().getValue();
        }
        m_entry = entry;
        m_title.setValue(customTitle);
        if (info.getResource() != null) {
            displayResourceInfo(Collections.singletonList(info.getResource()));
        }
        m_cancelButton.addClickListener(evt -> {
            CmsVaadinUtils.closeWindow(CmsEditFavoriteDialog.this);
        });
        m_title.addValueChangeListener(evt -> {
            m_changed = true;
        });
    }

    /**
     * Handler for the OK button.
     */
    protected void onClickOk() {

        if (!m_changed) {
            CmsVaadinUtils.closeWindow(CmsEditFavoriteDialog.this);
            return;
        }
        String value = m_title.getValue();
        CmsFavoriteEntry result;
        try {
            result = new CmsFavoriteEntry(m_entry.toJson());
            result.setCustomTitle(value);
            m_callback.accept(result);
            CmsVaadinUtils.closeWindow(CmsEditFavoriteDialog.this);

        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

}
