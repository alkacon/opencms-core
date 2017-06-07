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

package org.opencms.workplace.tools.git.ui;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/** Dialog that prints the result of the execution of a git action. */
public class CmsGitActionResultPanel extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Label showing the result message. */
    private Label m_messageLabel;
    /** Label showing the log output. */
    private Label m_logLabel;

    /**
     * Creates a dialog window with the git action result.
     * @param message the message written at top.
     * @param log the log output
     * @param isError flag, indicating if an error occurred.
     * @param buttons the buttons to show at the bottom of the dialog.
     */
    public CmsGitActionResultPanel(String message, String log, boolean isError, List<Button> buttons) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_logLabel.setContentMode(ContentMode.PREFORMATTED);
        if (isError) {
            m_messageLabel.addStyleName(OpenCmsTheme.LABEL_ERROR);
        }
        m_messageLabel.setValue(message);
        m_logLabel.setValue(log);
        for (Button button : buttons) {
            addButton(button);
        }
        m_messageLabel.addStyleName(ValoTheme.LABEL_H2);
    }
}
