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

package org.opencms.ui.apps.searchindex;

import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.I_CmsCRUDApp;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.Iterator;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;

/**
 * Class for the dialog to show source information of a given index.<p>
 */
public class CmsSourceDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 4302977301857481351L;

    /**vaadin serial id.*/
    private Button m_cancelButton;

    /**vaadin serial id.*/
    private FormLayout m_layout;

    /**Manager app. */
    private I_CmsCRUDApp<I_CmsSearchIndex> m_manager;

    /**
     * public constructor.<p>
     * @param app calling app instance
     *
     * @param cancel runnable to be started when the dialog gets closed
     */
    public CmsSourceDialog(I_CmsCRUDApp<I_CmsSearchIndex> app, final Runnable cancel) {

        m_manager = app;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_cancelButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -4321889329235244258L;

            public void buttonClick(ClickEvent event) {

                cancel.run();
            }
        });
    }

    /**
     * Sets the search index to show information about.<p>
     *
     * @param searchindex to be displayed
     */
    public void setSource(String searchindex) {

        Label label = new Label();
        label.setContentMode(ContentMode.HTML);

        label.setValue(getSources(searchindex));

        m_layout.removeAllComponents();
        m_layout.addComponent(label);
    }

    /**
     * Fills details of the index source into the given item. <p>
     *
     * @param indexName name of index
     * @return String representation of information about given index
     *
     */
    private String getSources(String indexName) {

        StringBuffer html = new StringBuffer();
        // search for the corresponding A_CmsSearchIndex:
        I_CmsSearchIndex idx = m_manager.getElement(indexName);

        html.append("<ul>\n");
        // get the index sources (nice API)
        for (CmsSearchIndexSource idxSource : idx.getSources()) {
            html.append("  <li>\n").append("    ").append("name      : ").append(idxSource.getName()).append("\n");
            html.append("  </li>");

            html.append("  <li>\n").append("    ").append("indexer   : ").append(
                idxSource.getIndexerClassName()).append("\n");
            html.append("  </li>");

            html.append("  <li>\n").append("    ").append("resources : ").append("\n");
            html.append("    <ul>\n");
            List<String> resources = idxSource.getResourcesNames();
            Iterator<String> itResources = resources.iterator();
            while (itResources.hasNext()) {
                html.append("    <li>\n").append("      ").append(itResources.next()).append("\n");
                html.append("    </li>\n");
            }
            html.append("    </ul>\n");
            html.append("  </li>");

            html.append("  <li>\n").append("    ").append("doctypes : ").append("\n");
            html.append("    <ul>\n");
            resources = idxSource.getDocumentTypes();
            itResources = resources.iterator();
            while (itResources.hasNext()) {
                html.append("    <li>\n").append("      ").append(itResources.next()).append("\n");
                html.append("    </li>\n");
            }
            html.append("    </ul>\n");
            html.append("  </li>");
        }

        html.append("</ul>\n");
        return html.toString();
    }
}
