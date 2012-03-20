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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsToolTipHandler;
import org.opencms.gwt.shared.CmsAdditionalInfoBean;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.ui.HTML;

/**
 * The result list item widget.<p>
 * 
 * Enabling the image tile view.<p>
 * 
 * @since 8.0.0
 */
public class CmsResultItemWidget extends CmsListItemWidget {

    /** Standard image tile scale parameter. */
    private static final String IMAGE_SCALE_PARAM = "?__scale=w:142,h:100,t:1,c:white,r:2";

    /** The image resource type name. */
    private static final String IMAGE_TYPE = "image";

    /** Tile view flag. */
    private boolean m_hasTileView;

    private CmsToolTipHandler m_tooltipHandler;

    /**
     * Constructor.<p>
     * 
     * @param infoBean the resource info bean
     */
    public CmsResultItemWidget(CmsResultItemBean infoBean) {

        super(infoBean);
        setIcon(CmsIconUtil.getResourceIconClasses(infoBean.getType(), infoBean.getPath(), false));

        // if resourceType=="image" prepare for tile view
        if (IMAGE_TYPE.equals(infoBean.getType())) {
            m_hasTileView = true;
            // add tile view marker css classes

            // insert tile view image div
            HTML imageTile = new HTML("<img src=\""
                + CmsCoreProvider.get().link(infoBean.getPath())
                + IMAGE_SCALE_PARAM
                + "\" />");
            imageTile.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().imageTile());
            m_tooltipHandler = new CmsToolTipHandler(imageTile, generateTooltipHtml(infoBean));
            m_contentPanel.insert(imageTile, 0);
        }

    }

    /**
     * Indicates wther there is a tile view available for this widget.<p>
     * 
     * @return <code>true</code> if a tiled view is available
     */
    public boolean hasTileView() {

        return m_hasTileView;
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onDetach()
     */
    @Override
    protected void onDetach() {

        if (m_tooltipHandler != null) {
            m_tooltipHandler.clearShowing();
        }
        super.onDetach();
    }

    /**
     * Generates the HTML for the item tool-tip.<p>
     * 
     * @param infoBean the item info
     * 
     * @return the generated HTML
     */
    private String generateTooltipHtml(CmsListInfoBean infoBean) {

        StringBuffer result = new StringBuffer();
        result.append("<p><b>").append(CmsClientStringUtil.shortenString(infoBean.getTitle(), 70)).append("</b></p>");
        if (infoBean.hasAdditionalInfo()) {
            for (CmsAdditionalInfoBean additionalInfo : infoBean.getAdditionalInfo()) {
                result.append("<p>").append(additionalInfo.getName()).append(":&nbsp;");
                // shorten the value to max 45 characters
                result.append(CmsClientStringUtil.shortenString(additionalInfo.getValue(), 45)).append("</p>");
            }
        }
        return result.toString();
    }
}
