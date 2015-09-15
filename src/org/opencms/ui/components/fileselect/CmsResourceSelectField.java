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

package org.opencms.ui.components.fileselect;

import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.workplace.CmsWorkplace;

import org.apache.commons.logging.Log;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * File select field.<p>
 */
public class CmsResourceSelectField extends HorizontalLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceSelectField.class);

    /** The text field containing the selected path. */
    private TextField m_textField = new TextField();

    /** The filter used for reading resources. */
    private CmsResourceFilter m_filter = CmsResourceFilter.DEFAULT;

    /**
     * Creates a new instance.<p>
     */
    public CmsResourceSelectField() {
        setSpacing(true);
        addComponent(m_textField);
        Button fileSelectButton = new Button("");
        fileSelectButton.addStyleName(OpenCmsTheme.BUTTON_UNPADDED);
        fileSelectButton.addStyleName(ValoTheme.BUTTON_LINK);
        ExternalResource folderRes = new ExternalResource(
            CmsWorkplace.getResourceUri(
                CmsWorkplace.RES_PATH_FILETYPES
                    + OpenCms.getWorkplaceManager().getExplorerTypeSetting("folder").getBigIcon()));
        fileSelectButton.setIcon(folderRes);

        addComponent(fileSelectButton);
        setExpandRatio(m_textField, 1f);
        m_textField.setWidth("100%");
        fileSelectButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openFileSelector();
            }
        });

    }

    /**
     * Gets the text field component.<p>
     *
     * @return the text field component
     */
    public TextField getTextField() {

        return m_textField;
    }

    /**
     * Gets the value.<p>
     *
     * @return the value
     */
    public String getValue() {

        return m_textField.getValue();
    }

    /**
     * Sets the filter to use for reading resources.<p>
     *
     * @param filter the new filter
     */
    public void setResourceFilter(CmsResourceFilter filter) {

        m_filter = filter;

    }

    /**
     * Sets the value.<p>
     *
     * @param value the new value
     */
    public void setValue(String value) {

        m_textField.setValue(value);
    }

    /**
     * Opens the file selector dialog.<p>
     */
    protected void openFileSelector() {

        Window window = new Window("Select file");
        window.setModal(true);
        CmsResourceSelectDialog fileSelect;

        try {
            fileSelect = new CmsResourceSelectDialog(m_filter);
            fileSelect.addPathSelectionHandler(true, new I_CmsSelectionHandler<String>() {

                public void onSelection(String selected) {

                    getTextField().setValue(selected);
                }
            });
            window.setContent(fileSelect);
            A_CmsUI.get().addWindow(window);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            // ignore
        }
    }

}
