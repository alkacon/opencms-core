/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsErrorWidget.java,v $
 * Date   : $Date: 2010/03/09 09:03:53 $
 * Version: $Revision: 1.2 $
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

package org.opencms.gwt.client.ui.input;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Helper class for displaying errors.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsErrorWidget extends Composite {

    /** The CSS bundle for this widget. */
    private static I_CmsInputCss CSS = I_CmsLayoutBundle.INSTANCE.inputCss();

    /** The internal label that displays the error message. */
    private Label m_label;

    /**
     * Creates a new instance.<p>
     */
    public CmsErrorWidget() {

        m_label = createErrorLabel();
        initWidget(m_label);
        setText(null);
    }

    /**
     * Creates the a new error label.<p>
     * 
     * @return a label with the appropriate style for  an error label
     */
    private static Label createErrorLabel() {

        Label label = new Label();
        label.addStyleName(CSS.error());
        return label;
    }

    /**
     * Sets the text for the error message.<p>
     * 
     * If the text parameter is null, the error message will be hidden.
     * @param text
     */
    public void setText(String text) {

        if (text == null) {
            m_label.setVisible(false);
        } else {
            m_label.setText(text);
            m_label.setVisible(true);
        }
    }
}
