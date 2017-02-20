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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Variations for resource of flex and image caches.<p>
 */
public class CmsVariationsList extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 269853411209495687L;

    /**Vaadin component.*/
    private Button m_cancel;

    /**Vaadin component*/
    private VerticalLayout m_panel;

    /**
     * Public constructor.<p>
     *
     * @param app instance of calling app.
     * @param state String holding information about Variation to show (from flexCache or ImageCache)
     */
    public CmsVariationsList(final CmsCacheAdminApp app, final String state) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        //Read out variations

        Iterator<String> variationsIterator;

        if (state.startsWith(CmsCacheAdminApp.PATH_VIEW_FLEX_VARIATIONS)) {
            //For FlexCache
            CmsFlexCache cache = OpenCms.getFlexCache();
            Set<String> variations = cache.getCachedVariations(app.getResourceFromState(state), A_CmsUI.getCmsObject());
            variationsIterator = variations.iterator();
        } else {
            //For image Cache
            CmsImageCacheHelper helper = new CmsImageCacheHelper(
                app.getImagePathFromState(state),
                A_CmsUI.getCmsObject(),
                true,
                false,
                false);
            List<String> variations = helper.getVariations(helper.getAllCachedImages().get(0).toString());
            variationsIterator = variations.iterator();
        }

        //Fill label

        Label label = new Label();
        label.setContentMode(ContentMode.HTML);
        label.setValue(getVariationsHTMLString(variationsIterator));

        m_panel.addComponent(label);

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -1921697074171843576L;

            public void buttonClick(ClickEvent event) {

                if (state.startsWith(CmsCacheAdminApp.PATH_VIEW_FLEX_VARIATIONS)) {
                    app.openSubView(CmsCacheAdminApp.PATH_VIEW_FLEX, true);
                } else {
                    app.openSubView(CmsCacheAdminApp.PATH_VIEW_IMAGE, true);
                }
            }
        });
    }

    /**
     * Creates HTML String from iterator containing Variations.<p>
     *
     * @param iterator with Variations
     * @return HTML String
     */
    private String getVariationsHTMLString(Iterator iterator) {

        String ret = "";
        while (iterator.hasNext()) {
            ret += iterator.next() + "<br/>";
        }
        return ret;
    }
}
