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
import org.opencms.ui.components.CmsBasicDialog;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class CmsVariationsDialog extends CmsBasicDialog {

    protected static int MODE_FLEX = 0;

    protected static int MODE_IMAGE = 1;

    private Button m_cancelButton;

    private Panel m_panel;

    private FormLayout m_layout;

    public CmsVariationsDialog(String resource, final Runnable cancel, CmsCacheAdminApp app, int mode) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_cancelButton.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {

                cancel.run();
            }
        });

        Iterator<String> variationsIterator;

        if (mode == MODE_FLEX) {
            //For FlexCache
            CmsFlexCache cache = OpenCms.getFlexCache();
            Set<String> variations = cache.getCachedVariations(resource, A_CmsUI.getCmsObject());
            variationsIterator = variations.iterator();
        } else {
            //For image Cache
            CmsImageCacheHelper helper = new CmsImageCacheHelper(resource, A_CmsUI.getCmsObject(), true, false, false);
            List<String> variations = helper.getVariations(helper.getAllCachedImages().get(0).toString());
            variationsIterator = variations.iterator();
        }
        m_panel.setSizeFull();

        m_layout.setHeight("700px");

        m_layout.addStyleName("v-scrollable");

        while (variationsIterator.hasNext()) {
            m_layout.addComponent(new Label(variationsIterator.next()));
        }

    }

}
