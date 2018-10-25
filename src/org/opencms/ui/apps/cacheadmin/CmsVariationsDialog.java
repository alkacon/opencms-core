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

import org.opencms.flex.CmsFlexCache;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.cacheadmin.CmsCacheViewApp.Mode;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.Iterator;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the dialog to show variations of flex cache and image cache.<p>
 */
public class CmsVariationsDialog extends CmsBasicDialog {

    /**generated vaadin id.*/
    private static final long serialVersionUID = -7346908393288365974L;

    /**vaadin component.*/
    private Button m_cancelButton;

    /**vaadin component.*/
    private FormLayout m_layout;

    /**vaadin component.*/
    private VerticalLayout m_outerlayout;

    /**vaadin component.*/
    private Panel m_panel;

    /**
     * public constructor.<p>
     *
     * @param resource to show variations for.
     * @param cancel runnable
     * @param mode mode
     */
    public CmsVariationsDialog(String resource, final Runnable cancel, CmsCacheViewApp.Mode mode) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_cancelButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -4321889329235244258L;

            public void buttonClick(ClickEvent event) {

                cancel.run();
            }
        });

        Iterator<String> variationsIterator = null;

        if (Mode.FlexCache.equals(mode)) {
            //For FlexCache
            CmsFlexCache cache = OpenCms.getFlexCache();
            Set<String> variations = cache.getCachedVariations(resource, A_CmsUI.getCmsObject());
            variationsIterator = variations.iterator();

            m_panel.setSizeFull();

            m_layout.setHeight("100%");
            m_outerlayout.setHeight("100%");

            m_layout.addStyleName("v-scrollable");

            while (variationsIterator.hasNext()) {
                m_layout.addComponent(new Label(variationsIterator.next()));
            }
        } else {
            m_outerlayout.addComponent(new CmsImageVariationsTable(resource));
        }

    }
}
