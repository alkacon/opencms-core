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

package org.opencms.ui.favorites;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.editablegroup.CmsEditableGroupButtons;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;

/**
 * Resource info box.<p>
 */
public class CmsFavInfo extends CustomLayout implements I_CmsEditableGroupRow {

    /** Button container location id. */
    private static final String BUTTON_CONTAINER = "buttonContainer";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFavInfo.class);

    /** The serial version id. */
    private static final long serialVersionUID = -1715926038770100307L;

    /** The sub title label. */
    private Label m_bottomText = new Label();

    /** The button label. */
    private Label m_buttonLabel = new Label();

    /**
     * The buttons for changing the position.
     */
    private CmsEditableGroupButtons m_buttons;

    /** The favorite entry. */
    private CmsFavoriteEntry m_entry;

    /** The resource icon. */
    private CmsResourceIcon m_icon = new CmsResourceIcon();

    /** The project label. */
    private Label m_projectLabel = new Label();

    /** The resource. */
    private CmsResource m_resource;

    /** The site label. */
    private Label m_siteLabel = new Label();

    /** The title label. */
    private Label m_topText = new Label();

    /**
     * Constructor.<p>
     *
     * @param entry the favorite entry whose data to display
     */
    public CmsFavInfo(CmsFavoriteEntry entry) {

        super();
        try {
            initTemplateContentsFromInputStream(CmsVaadinUtils.readCustomLayout(CmsFavInfo.class, "favinfo.html"));
            addComponent(m_topText, "topLabel");
            addComponent(m_bottomText, "bottomLabel");
            addComponent(m_icon, "icon");
            addComponent(m_buttonLabel, "buttonContainer");
            addComponent(m_projectLabel, "projectLabel");
            addComponent(m_siteLabel, "siteLabel");

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        m_entry = entry;
        addStyleName("o-pointer");
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
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow#getButtonBar()
     */
    public CmsEditableGroupButtons getButtonBar() {

        return m_buttons;
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
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow#getComponent()
     */
    public Component getComponent() {

        return this;
    }

    /**
     * Gets the favorite entry.
     *
     * @return the favorite entry
     */
    public CmsFavoriteEntry getEntry() {

        return m_entry;
    }

    /**
     * Gets the project label.
     *
     * @return the project label
        // TODO Auto-generated method stub
        return null;

     */
    public Label getProjectLabel() {

        return m_projectLabel;

    }

    /**
     * Gets the resource.
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
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
     * Gets the site label.
     *
     * @return the site label
     */
    public Label getSiteLabel() {

        return m_siteLabel;
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
     * Sets the buttons.
     *
     * @param buttons the buttons
     */
    public void setButtons(CmsEditableGroupButtons buttons) {

        m_buttons = buttons;
        setButtonWidget(m_buttons);
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
     * Sets the bookmark entry.
     *
     * @param entry the bookmark entry
     */
    public void setEntry(CmsFavoriteEntry entry) {

        m_entry = entry;
    }

    /**
     * Sets the resource
     *
     * @param resource the resource to set
     */
    public void setResource(CmsResource resource) {

        m_resource = resource;

    }

}
