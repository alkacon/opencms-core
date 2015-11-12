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

package org.opencms.ui.components;

import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.Locale;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

/**
 * Resource info box.<p>
 */
public class CmsResourceInfo extends Panel {

    /** The serial version id. */
    private static final long serialVersionUID = -1715926038770100307L;

    /** The sub title label. */
    private Label m_bottomText;

    /** The button label. */
    private Label m_buttonLabel;

    /** The resource icon. */
    private CmsResourceIcon m_icon;

    /** The main container. */
    private CssLayout m_mainContainer;

    /** The title label. */
    private Label m_topText;

    /**
     * Constructor.<p>
     *
     * @param resource the resource
     */
    public CmsResourceInfo(CmsResource resource) {
        this();
        Locale locale = A_CmsUI.get().getLocale();
        CmsResourceUtil resUtil = new CmsResourceUtil(A_CmsUI.getCmsObject(), resource);
        resUtil.setAbbrevLength(100);
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        m_topText.setValue(resUtil.getGalleryTitle(locale));
        m_bottomText.setValue(resUtil.getPath());
        m_icon.initContent(
            resUtil,
            CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getBigIconIfAvailable()),
            resource.getState());
        m_buttonLabel.setVisible(false);
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
        m_icon.initContent(null, iconPath, null);

    }

    /**
     * Constructor.<p>
     */
    private CmsResourceInfo() {

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
    }

    /**
     * Gets the button label.<p>
     *
     * @return the button label
     */
    public Label getButtonLabel() {

        return m_buttonLabel;
    }

    /**
     * Gets the main container.<p>
     *
     * @return the main container
     */
    public CssLayout getMainContainer() {

        return m_mainContainer;
    }
}
