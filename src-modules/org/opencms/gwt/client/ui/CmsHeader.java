/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsHeader.java,v $
 * Date   : $Date: 2010/03/15 14:52:25 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * A title element.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsHeader extends Widget implements HasText {

    /**
     * The supported tags.<p>
     */
    public enum Tag {
        /** h1. */
        H1(I_CmsLayoutBundle.INSTANCE.headerCss().h1()),

        /** h2. */
        H2(I_CmsLayoutBundle.INSTANCE.headerCss().h2()),

        /** h3. */
        H3(I_CmsLayoutBundle.INSTANCE.headerCss().h3()),

        /** h4. */
        H4(I_CmsLayoutBundle.INSTANCE.headerCss().h4()),

        /** h5. */
        H5(I_CmsLayoutBundle.INSTANCE.headerCss().h5()),

        /** h6. */
        H6(I_CmsLayoutBundle.INSTANCE.headerCss().h6());

        /** The class name. */
        private final String m_className;

        /**
         * Constructor.<p>
         * 
         * @param className the class name
         */
        private Tag(String className) {

            m_className = className;
        }

        /**
         * Returns the class name.<p>
         *
         * @return the class name
         */
        public String className() {

            return m_className;
        }
    }

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

        this(text, Tag.H1);
    }

    /**
     * Constructor.<p>
     * 
     * @param text the text to set
     * @param tag the tag to use 
     */
    public CmsHeader(String text, Tag tag) {

        setElement(DOM.createElement(tag.name()));
        getElement().addClassName(tag.className());
        setText(text);
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
}
