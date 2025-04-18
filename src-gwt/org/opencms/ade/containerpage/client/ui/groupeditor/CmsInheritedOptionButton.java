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

package org.opencms.ade.containerpage.client.ui.groupeditor;

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * The selection option.<p>
 *
 * @since 8.5.0
 */
public class CmsInheritedOptionButton extends CmsPushButton implements I_CmsGroupEditorOption {

    /** The element widget. */
    private CmsContainerPageElementPanel m_elementWidget;

    /**
     * The constructor.<p>
     *
     * @param elementWidget the associated element widget
     * @param editor the editor instance
     */
    public CmsInheritedOptionButton(CmsContainerPageElementPanel elementWidget, CmsInheritanceContainerEditor editor) {

        super();
        setImageClass(I_CmsButton.ButtonData.INHERITED.getSmallIconClass());
        setButtonStyle(ButtonStyle.FONT_ICON, null);

        String path = elementWidget.getInheritanceInfo().getPath();
        String title = I_CmsButton.ButtonData.INHERITED.getTitle();
        if (path != null) {
            title = Messages.get().key(
                Messages.GUI_TITLE_INHERITED_FROM_1,
                elementWidget.getInheritanceInfo().getPath());
        }
        setTitle(title);
        m_elementWidget = elementWidget;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.I_CmsGroupEditorOption#checkVisibility()
     */
    public boolean checkVisibility() {

        return !m_elementWidget.getInheritanceInfo().isNew();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.I_CmsGroupEditorOption#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public void onClick(ClickEvent event) {

        // nothing to do
    }
}
