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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * A title element.<p>
 * 
 * @since 8.0.0
 */
public class CmsHeader extends Widget implements HasText {

    private CmsLabel m_label;

    /**
     * Constructor.<p>
     */
    public CmsHeader() {

        this("");
    }

    /**
     * Constructor.<p>
     * 
     * @param text the text to set
     */
    public CmsHeader(String text) {

        this(text, CmsDomUtil.Tag.h1);
    }

    /**
     * Constructor.<p>
     * 
     * @param text the text to set
     * @param tag the tag to use 
     */
    public CmsHeader(String text, CmsDomUtil.Tag tag) {

        setElement(DOM.createElement(tag.name()));
        String className = I_CmsLayoutBundle.INSTANCE.headerCss().h1();
        if (tag == CmsDomUtil.Tag.h2) {
            className = I_CmsLayoutBundle.INSTANCE.headerCss().h2();
        } else if (tag == CmsDomUtil.Tag.h3) {
            className = I_CmsLayoutBundle.INSTANCE.headerCss().h3();
        } else if (tag == CmsDomUtil.Tag.h4) {
            className = I_CmsLayoutBundle.INSTANCE.headerCss().h4();
        } else if (tag != CmsDomUtil.Tag.h1) {
            // prevent use of any other tags
            assert false;
        }
        getElement().addClassName(className);
        setText(text);
    }

    /**
     * Constructor with label.<p>
     * 
     * @param text the text to set
     * @param label the label text to use 
     */
    public CmsHeader(String text, String label) {

        this(text, CmsDomUtil.Tag.h3);
        m_label = new CmsLabel(label);
        Element element = m_label.getElement();
        element.getStyle().setDisplay(Style.Display.INLINE);
        getElement().appendChild(element);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    public String getText() {

        return getElement().getInnerText();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    public void setText(String text) {

        getElement().setInnerText(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#doAttachChildren()
     */
    @Override
    protected void doAttachChildren() {

        if (m_label == null) {
            return;
        }
        m_label.onAttach();
    }
}
