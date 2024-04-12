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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Header info widget.<p>
 */
public class CmsInfoHeader extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsInfoHeaderUiBinder extends UiBinder<HTMLPanel, CmsInfoHeader> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsInfoHeaderUiBinder uiBinder = GWT.create(I_CmsInfoHeaderUiBinder.class);

    /** The description element. */
    @UiField
    protected ParagraphElement m_description;

    /** The locale cell. */
    @UiField
    protected SpanElement m_locale;

    /** The site host element. */
    @UiField
    protected SpanElement m_siteHost;

    /** The title element. */
    @UiField
    protected HeadingElement m_title;

    /** The type icon element. */
    @UiField
    protected DivElement m_typeIcon;

    @UiField
    protected DivElement m_additionalWidgets;

    /** The button bar. */
    @UiField
    protected Element m_buttonBar;

    /** The main panel. */
    private HTMLPanel m_main;

    /**
     * Constructor.<p>
     *
     * @param title the title
     * @param description the description
     * @param path the path
     * @param locale the locale
     * @param typeIcon the type icon CSS class
     */
    public CmsInfoHeader(String title, String description, String path, String locale, String typeIcon) {

        m_main = uiBinder.createAndBindUi(this);
        initWidget(m_main);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
            title = Messages.get().key(Messages.GUI_NO_TITLE_0);
        }
        m_title.setInnerText(title);
        m_description.setInnerText(description);
        m_siteHost.setInnerText(path);
        m_locale.setInnerText("[" + locale + "]");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(typeIcon)) {
            m_typeIcon.addClassName(typeIcon);
            m_typeIcon.getStyle().setWidth(24, Unit.PX);
            m_typeIcon.getStyle().setHeight(24, Unit.PX);
        } else {
            m_typeIcon.removeFromParent();
        }
    }

    /**
     * Adds a button to the top right of the info header.<p>
     *
     * @param button the button to add
     */
    public void addButtonTopRight(Widget button) {

        m_main.add(button, m_buttonBar);
    }

    public void addWidget(Widget widget) {
        m_main.add(widget, m_additionalWidgets);
    }
}
