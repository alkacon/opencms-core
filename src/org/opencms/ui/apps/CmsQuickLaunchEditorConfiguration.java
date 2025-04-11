/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps;

import org.opencms.ui.CmsCssIcon;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

/**
 * Editor for the user quick launch configuration.<p>
 */
public class CmsQuickLaunchEditorConfiguration extends A_CmsWorkplaceAppConfiguration {

    /**
     * The quick launch editor app.<p>
     */
    protected static class QuickLaunchEditorApp extends A_CmsWorkplaceApp implements ViewChangeListener {

        /** The serial version id. */
        private static final long serialVersionUID = 5187855022780289047L;

        /** The editor component. */
        private CmsQuickLaunchEditor editor;

        /**
         * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
         */
        public boolean beforeViewChange(ViewChangeEvent event) {

            editor.saveToUser();
            return true;
        }

        /**
         * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
         */
        @Override
        protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

            return null;
        }

        /**
         * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
         */
        @Override
        protected Component getComponentForState(String state) {

            if (editor == null) {
                editor = new CmsQuickLaunchEditor();
                editor.resetAppIcons();
            }
            return editor;
        }

        /**
         * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
         */
        @Override
        protected List<NavEntry> getSubNavEntries(String state) {

            return null;
        }
    }

    /** The app id. */
    public static String APP_ID = "quicklaunch_editor";

    /** The app icon resource (size 32x32). */
    public static final CmsCssIcon ICON = new CmsCssIcon("oc-icon-32-quicklaunch-editor");

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppInstance()
     */
    public I_CmsWorkplaceApp getAppInstance() {

        return new QuickLaunchEditorApp();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getHelpText(java.util.Locale)
     */
    @Override
    public String getHelpText(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_QUICK_LAUNCH_EDITOR_HELP_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    public Resource getIcon() {

        return ICON;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getId()
     */
    public String getId() {

        return APP_ID;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getName(java.util.Locale)
     */
    @Override
    public String getName(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_QUICK_LAUNCH_EDITOR_TITLE_0);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getOrder()
     */
    @Override
    public int getOrder() {

        return 30;
    }
}
