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

import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.util.CmsMacroResolver;

import java.lang.reflect.Constructor;
import java.util.Locale;

import com.vaadin.ui.Component;

/**
 * Context menu item class for basic Vaadin dialogs.
 *
 * This class is highly likely to change.
 */
public class CmsDialogContextMenuItem extends A_CmsContextMenuItem {

    private Class<? extends Component> m_dialogClass;
    private String m_id;

    private int m_order;
    private int m_priority;
    private String m_title;

    public CmsDialogContextMenuItem(

        String id,
        String parentId,
        Class<? extends Component> dialogClass,
        String title,
        int order,
        int priority) {
        m_dialogClass = dialogClass;
        m_id = id;
        m_order = order;
        m_priority = priority;
        m_title = title;
    }

    public void executeAction(I_CmsDialogContext context) {

        try {
            Constructor<? extends Component> constructor = m_dialogClass.getConstructor(I_CmsDialogContext.class);
            Component component = constructor.newInstance(context);
            context.start(getTitle(OpenCms.getWorkplaceManager().getWorkplaceLocale(context.getCms())), component);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getClientAction() {

        return null;
    }

    public String getId() {

        return m_id;
    }

    public int getOrder() {

        return 0;
    }

    public String getParentId() {

        return null;
    }

    public int getPriority() {

        return 0;
    }

    public String getTitle(Locale locale) {

        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        return resolver.resolveMacros(m_title);
    }

    public boolean isLeafItem() {

        return true;
    }
}
