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

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;

/**
 * Resource info box.<p>
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

    /** The title label. */
    private Label m_topText = new Label();

    /**
     * Constructor.<p>
     */
    public CmsResourceInfo() {
        super();
        try {
            initTemplateContentsFromInputStream(CmsVaadinUtils.readCustomLayout(getClass(), "resourceinfo.html"));
            addComponent(m_topText, "topLabel");
            addComponent(m_bottomText, "bottomLabel");
            addComponent(m_icon, "icon");
            addComponent(m_buttonLabel, "buttonContainer");
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
        CmsResourceUtil resUtil = new CmsResourceUtil(A_CmsUI.getCmsObject(), resource);
        resUtil.setAbbrevLength(100);
        m_topText.setValue(resUtil.getGalleryTitle(locale));
        m_bottomText.setValue(resUtil.getPath());

        m_icon.initContent(resUtil, useState ? resource.getState() : null, true, true);

    }

    /**
     * Constructor.<p>
     *
     * @param top the title
     * @param bottom the sub title
     * @param iconPath the icon resource path
     */
    public CmsResourceInfo(String top, String bottom, String iconPath) {

        this();
        m_topText.setValue(top);
        m_bottomText.setValue(bottom);
        m_icon.initContent(null, iconPath, null, false, true);

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
        String icon = CmsResourceIcon.getSitemapResourceIcon(
            A_CmsUI.getCmsObject(),
            resUtil.getResource(),
            IconMode.localeCompare);
        info.getResourceIcon().initContent(resUtil, icon, null, true, false);
        return info;
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

}
