/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.cacheadmin;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Dialog for the flush actions.<p>
 */
public class CmsFlushButtonHolderDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -223664443814758803L;

    /**vaadin component. */
    private Button m_cancelButton;

    /**
     * public constructor.<p>
     *
     * @param window window
     */
    public CmsFlushButtonHolderDialog(final Window window) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        VerticalLayout layout = CmsFlushCache.getButtonLayout(0, new Runnable() {

            public void run() {

                window.close();
                A_CmsUI.get().reload();
            }
        });
        layout.addStyleName("o-center");
        setContent(layout);

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 2203061285642153560L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }

        });

        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_0));
    }
}
