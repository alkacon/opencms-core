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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.shared.CmsSitemapInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A title element.<p>
 * 
 * @since 8.0.0
 */
public class CmsSitemapHeader extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsSitemapHeaderUiBinder extends UiBinder<Widget, CmsSitemapHeader> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsSitemapHeaderUiBinder uiBinder = GWT.create(I_CmsSitemapHeaderUiBinder.class);

    /** The title element. */
    @UiField
    protected HeadingElement m_title;

    /** The description element. */
    @UiField
    protected ParagraphElement m_description;

    /** The site host element. */
    @UiField
    protected SpanElement m_siteHost;

    /** The locale cell. */
    @UiField
    protected SpanElement m_locale;

    /**
     * Constructor.<p>
     * 
     * @param sitemapInfo the sitemap info to display 
     */
    public CmsSitemapHeader(CmsSitemapInfo sitemapInfo) {

        initWidget(uiBinder.createAndBindUi(this));
        m_title.setInnerText(sitemapInfo.getTitle());
        m_description.setInnerText(sitemapInfo.getDescription());
        m_siteHost.setInnerText(sitemapInfo.getSiteHost());
        m_locale.setInnerText("[" + sitemapInfo.getSiteLocale() + "]");
    }
}
