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

package org.opencms.ui.contextmenu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.lang.reflect.Constructor;
import java.util.List;

import com.vaadin.ui.Component;

/**
 * Context menu item class for basic Vaadin dialogs.
 *
 * This class is highly likely to change.
 */
public class CmsDialogContextMenuItem extends A_CmsContextMenuItem {

    private Class<? extends Component> m_dialogClass;

    private I_CmsHasMenuItemVisibility m_visibilityCheck;

    public CmsDialogContextMenuItem(

        String id,
        String parentId,
        Class<? extends Component> dialogClass,
        String title,
        int order,
        int priority,
        I_CmsHasMenuItemVisibility visibilityCheck) {
        super(id, parentId, title, order, priority);
        m_dialogClass = dialogClass;
        m_visibilityCheck = visibilityCheck;
    }

    public void executeAction(I_CmsDialogContext context) {

        try {
            Constructor<? extends Component> constructor = m_dialogClass.getConstructor(I_CmsDialogContext.class);
            Component component = constructor.newInstance(context);
            context.start(CmsVaadinUtils.localizeString(getTitle()), component);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getClientAction() {

        return null;
    }

    @Override
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return m_visibilityCheck.getVisibility(cms, resources);

    }

    public boolean isLeafItem() {

        return true;
    }

}
