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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * class for the input dialog to search for cached images.<p>
 */
public class CmsImageCacheInput extends VerticalLayout {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1021439352252805506L;

    /**vaadin component. */
    private TextField m_searchString;

    /**vaadin component. */
    private Button m_okButton;

    /**
     * public constructor.<p>
     *
     * @param table to be updated after user input
     */
    public CmsImageCacheInput(final CmsImageCacheTable table) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        String siteRoot = A_CmsUI.getCmsObject().getRequestContext().getSiteRoot();
        if (!siteRoot.endsWith("/")) {
            siteRoot += "/";
        }
        siteRoot += "*";
        m_searchString.setValue(siteRoot);

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -2309066076096393602L;

            public void buttonClick(ClickEvent event) {

                table.load(getSearchPattern());
            }
        });
    }

    /**
     * Reads the search field out.<p>
     *
     * @return search pattern
     */
    protected String getSearchPattern() {

        return m_searchString.getValue();
    }
}
