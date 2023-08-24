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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.util.CmsJspElFunctions;
import org.opencms.main.CmsLog;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsResourceIcon.IconMode;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.TextField;
import com.vaadin.v7.ui.Label;

/**
 * Class representing a resource info box.<p>
 */
public class CmsResourceInfo extends CustomLayout {

    /** Button container location id. */
    private static final String BUTTON_CONTAINER = "buttonContainer";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceInfo.class);

    /** The serial version id. */
    private static final long serialVersionUID = -1715926038770100307L;

    /** The sub title label. */
    private Label m_bottomText = new Label();

    /** The button label. */
    private Label m_buttonLabel = new Label();

    /** The resource icon. */
    private CmsResourceIcon m_icon = new CmsResourceIcon();

    /** The label on top. */
    private Label m_topText = new Label();

    /** The input on top. */
    private TextField m_topInput = new TextField();

    /**
     * Constructor.<p>
     */
    public CmsResourceInfo() {

        super();
        try {
            initTemplateContentsFromInputStream(
                CmsVaadinUtils.readCustomLayout(CmsResourceInfo.class, "resourceinfo.html"));
            addComponent(m_topText, "topLabel");
            addComponent(m_bottomText, "bottomLabel");
            addComponent(m_icon, "icon");
            addComponent(m_buttonLabel, "buttonContainer");
            addComponent(m_topInput, "topInput");
            m_topInput.setVisible(false);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Constructor.<p>
     *
     * @param resource the resource
     */
    public CmsResourceInfo(CmsResource resource) {

        this(resource, true);
    }

    /**
     * Constructor.<p>
     *
     * @param resource the resource
     * @param useState true if the resource state should be displayed
     */
    public CmsResourceInfo(CmsResource resource, boolean useState) {

        this();
        Locale locale = A_CmsUI.get().getLocale();
        CmsResourceUtil resourceUtil = new CmsResourceUtil(A_CmsUI.getCmsObject(), resource);
        resourceUtil.setAbbrevLength(100);
        String galleryTitle = resourceUtil.getGalleryTitle(locale);
        m_topText.setValue(galleryTitle);
        m_topInput.setValue(galleryTitle);
        m_bottomText.setValue(resourceUtil.getPath());
        if (!resourceUtil.isReleasedAndNotExpired()) {
            m_topText.addStyleName("o-expired");
            m_topInput.addStyleName("o-expired");
            m_bottomText.addStyleName("o-expired");
        }
        m_icon.initContent(resourceUtil, useState ? resource.getState() : null, true, true);

    }

    /**
     * Constructor.<p>
     *
     * @param top the title
     * @param bottom the sub title
     * @param iconResource the icon resource path
     */
    public CmsResourceInfo(String top, String bottom, Resource iconResource) {

        this();
        m_topText.setValue(top);
        m_topInput.setValue(top);
        m_bottomText.setValue(CmsJspElFunctions.stripHtml(bottom));
        m_icon.initContent(null, iconResource, null, false, true);
    }

    /**
     * Constructor.<p>
     *
     * @param top the title
     * @param bottom the sub title
     * @param iconPath the icon resource path
     */
    public CmsResourceInfo(String top, String bottom, String iconPath) {

        this(top, bottom, new ExternalResource(iconPath));
    }

    /**
     * Creates a resource info widget for a resource that looks like the sitemap entry for that resource.<p>
     *
     * @param resource the resource
     * @param baseSite the base site
     *
     * @return the resource info widget
     */
    public static CmsResourceInfo createSitemapResourceInfo(CmsResource resource, CmsSite baseSite) {

        String title = resource.getName();
        String path = resource.getRootPath();

        CmsResourceInfo info = new CmsResourceInfo();
        CmsResourceUtil resUtil = new CmsResourceUtil(A_CmsUI.getCmsObject(), resource);

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            Map<String, CmsProperty> props = CmsProperty.toObjectMap(cms.readPropertyObjects(resource, false));
            CmsProperty navtextProp = props.get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            CmsProperty titleProp = props.get(CmsPropertyDefinition.PROPERTY_TITLE);

            if ((navtextProp != null) && (navtextProp.getValue() != null)) {
                title = navtextProp.getValue();
            } else if ((titleProp != null) && (titleProp.getValue() != null)) {
                title = titleProp.getValue();
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        info.getTopLine().setValue(title);
        if (baseSite != null) {
            String siteRoot = baseSite.getSiteRoot();
            if (path.startsWith(siteRoot)) {
                path = path.substring(siteRoot.length());
                path = CmsStringUtil.joinPaths("/", path);
            }
        }
        info.getBottomLine().setValue(path);
        Resource icon = CmsResourceIcon.getSitemapResourceIcon(
            A_CmsUI.getCmsObject(),
            resUtil.getResource(),
            IconMode.localeCompare);
        info.getResourceIcon().initContent(resUtil, icon, null, true, false);
        return info;
    }

    /**
     *
     */
    public void decorateTopInput() {

        m_topText.setVisible(false);
        m_topInput.setVisible(true);
    }

    /**
     *
     */
    public void decorateTopLabel() {

        m_topText.setVisible(true);
        m_topInput.setVisible(false);
    }

    /**
     * Gets the bottom label.<p>
     *
     * @return the bottom label
     */
    public Label getBottomLine() {

        return m_bottomText;
    }

    /**
     * Gets the button label.<p>
     *
     * @return the button label
     */
    public Component getButtonWidget() {

        return getComponent("buttonContainer");
    }

    /**
     * Gets the resource icon.<p>
     *
     * @return the resource icon
     */
    public CmsResourceIcon getResourceIcon() {

        return m_icon;
    }

    /**
     * Returns the editable text on top.<p>
     *
     * @return the editable text on top
     */
    public TextField getTopInput() {

        return m_topInput;
    }

    /**
     * Gets the top label.<p>
     *
     * @return the top label
     */
    public Label getTopLine() {

        return m_topText;
    }

    /**
     * Replaces the button component.<p>
     *
     * @param button the new button component
     */
    public void setButtonWidget(Component button) {

        addComponent(button, BUTTON_CONTAINER);
    }

    /**
     * Replaces the text of the top label and top input.
     *
     * @param text the text
     */
    public void setTopLineText(String text) {

        m_topText.setValue(text);
        m_topInput.setValue(text);
    }

}
