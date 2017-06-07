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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsFadeAnimation;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Helper class for displaying errors.<p>
 *
 * @since 8.0.0
 */
public class CmsErrorWidget extends Composite {

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
        label.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        label.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().error());
        return label;
    }

    /**
     * Returns <code>true</code> if a error message is set.<p>
     *
     * @return <code>true</code> if a error message is set
     */
    public boolean hasError() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_label.getText());
    }

    /**
     * Sets the text for the error message.<p>
     *
     * If the text parameter is null, the error message will be hidden.
     * @param text
     */
    public void setText(final String text) {

        setErrorVisible(false);
        m_label.setText(text);
    }

    /**
     * Hides the error.<p>
     */
    protected void hideError() {

        if (hasError()) {
            CmsFadeAnimation.fadeOut(m_label.getElement(), new Command() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    setErrorVisible(false);
                }
            }, 300);
        }
    }

    /**
     * Sets the visibility of the error label.<p>
     *
     * @param visible <code>true</code> if the error should be visible
     */
    protected void setErrorVisible(boolean visible) {

        m_label.setVisible(visible);
    }

    /**
     * Shows the error.<p>
     */
    protected void showError() {

        if (hasError()) {
            m_label.getElement().getStyle().clearDisplay();
            CmsFadeAnimation.fadeIn(m_label.getElement(), new Command() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    // noop
                }
            }, 300);
        }
    }
}
