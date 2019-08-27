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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsLabel.I_TitleGenerator;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsToolTipHandler;
import org.opencms.gwt.shared.CmsAdditionalInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;

/**
 * The result list item widget.<p>
 *
 * Enabling the image tile view.<p>
 *
 * @since 8.0.0
 */
public class CmsResultItemWidget extends CmsListItemWidget {

    /**
     * Widget containing the image(s) for a result list item..<p>
     */
    public class ImageTile extends HTML implements I_CmsDragHandle {

        /** The draggable result list item. */
        private I_CmsDraggable m_draggable;

        /**
         * Creates a new instance with the given content.<p>
         *
         * @param content the HTML content
         */
        public ImageTile(String content) {

            super(content);
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDragHandle#getDraggable()
         */
        public I_CmsDraggable getDraggable() {

            return m_draggable;
        }

        /**
         * Sets the draggable widget.<p>
         *
         * @param draggable the draggable widget
         */
        public void setDraggable(I_CmsDraggable draggable) {

            m_draggable = draggable;
        }

    }

    /** Standard image tile scale parameter. */
    private static final String IMAGE_SCALE_PARAM = "?__scale=t:1,c:ffffff,r:0";

    /** Tile view flag. */
    private boolean m_hasTileView;

    /** The image tile. */
    private ImageTile m_imageTile;

    /** The tool tip handler. */
    private CmsToolTipHandler m_tooltipHandler;

    /**
     * Constructor.<p>
     *
     * @param infoBean the resource info bean
     * @param showPath <code>true</code> to show the resource path in sub title
     */
    public CmsResultItemWidget(CmsResultItemBean infoBean, boolean showPath) {

        super(infoBean);
        if (showPath) {
            setSubtitleLabel(infoBean.getPath());
            setSubtitleTitle(infoBean.getSubTitle());
        } else {
            setSubtitleTitle(infoBean.getPath());
        }

        // if resourceType=="image" prepare for tile view
        if (CmsResultsTab.isImagelikeType(infoBean.getType())) {
            m_hasTileView = true;
            // add tile view marker css classes
            String src = infoBean.getViewLink();
            if (src == null) {
                src = CmsCoreProvider.get().link(infoBean.getPath());
            }
            String timeParam = "&time=" + System.currentTimeMillis();
            // insert tile view image div
            ImageTile imageTile = new ImageTile("<img src=\"" + src + getBigImageScaleParam()
            // add time stamp to override browser image caching
                + timeParam
                + "\" class=\""
                + I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().bigImage()
                + "\" />"
                // using a second image tag for the small thumbnail variant
                + "<img src=\""
                + src
                + getSmallImageScaleParam(infoBean)
                // add time stamp to override browser image caching
                + timeParam
                + "\" class=\""
                + I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().smallImage()
                + "\" />"
                + "<div class='"
                + I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().expiredImageOverlay()
                + "' />");
            imageTile.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().imageTile());
            if (CmsClientStringUtil.checkIsPathOrLinkToSvg(infoBean.getPath())) {
                imageTile.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().svg());
            }
            m_imageTile = imageTile;
            m_tooltipHandler = new CmsToolTipHandler(imageTile, generateTooltipHtml(infoBean));
            m_contentPanel.insert(imageTile, 0);
        }

    }

    private static Element appendDom(Element parent, String name) {

        Element child = DOM.createElement(name);
        parent.appendChild(child);
        return child;
    }

    /**
     * Gets the image tile.<p>
     *
     * @return the image tile
     */
    public ImageTile getImageTile() {

        return m_imageTile;
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
     * Initializes the title attribute of the subtitle line.<p>
     *
     * @param subtitleTitle the value to set
     */
    public void setSubtitleTitle(final String subtitleTitle) {

        m_subtitle.setTitle(subtitleTitle);
        m_subtitle.setTitleGenerator(new I_TitleGenerator() {

            public String getTitle(String originalText) {

                return subtitleTitle;
            }
        });
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

        Element root = DOM.createElement("div");
        appendDom(appendDom(root, "p"), "b").setInnerText(CmsClientStringUtil.shortenString(infoBean.getTitle(), 70));
        if (infoBean.hasAdditionalInfo()) {
            for (CmsAdditionalInfoBean additionalInfo : infoBean.getAdditionalInfo()) {
                appendDom(root, "p").setInnerText(
                    additionalInfo.getName()
                        + ":\u00a0"
                        + CmsClientStringUtil.shortenString(additionalInfo.getValue(), 45));
            }
        }
        return root.getInnerHTML();
    }

    /**
     * Returns the scale parameter for big thumbnail images.<p>
     *
     * @return the scale parameter
     */
    private String getBigImageScaleParam() {

        return IMAGE_SCALE_PARAM
            + ",w:"
            + I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().bigImageWidth()
            + ",h:"
            + I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().bigImageHeight();
    }

    /**
     * Returns the scale parameter for small thumbnail images.<p>
     *
     * @param infoBean the resource info
     *
     * @return the scale parameter
     */
    private String getSmallImageScaleParam(CmsResultItemBean infoBean) {

        String result = null;
        if (infoBean.getDimension() != null) {
            String[] sizes = infoBean.getDimension().split("x");
            try {
                int width = Integer.parseInt(sizes[0].trim());
                int height = Integer.parseInt(sizes[1].trim());
                // only use the small image dimensions in case of dimensions smaller than the big thumbnail
                if ((I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().bigImageWidth() > width)
                    || (I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().bigImageHeight() > height)) {
                    result = IMAGE_SCALE_PARAM
                        + ",w:"
                        + I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().smallImageWidth()
                        + ",h:"
                        + I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().smallImageHeight();
                }
            } catch (Exception e) {
                // failed parsing the dimensions, will use big image
            }
        }
        if (result == null) {
            result = getBigImageScaleParam();
        }
        return result;
    }
}
