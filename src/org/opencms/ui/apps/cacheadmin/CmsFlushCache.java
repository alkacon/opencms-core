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
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsDefaultAppButtonProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.Collections;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Vaadin Layout with Buttons to clear the different types of cache.<p>
 */
public class CmsFlushCache extends VerticalLayout {

    /**Icon for clean flex cache.*/
    private final static String ICON_CLEAN_CORE = "apps/cacheAdmin/core.png";

    /**Icon for clean flex cache.*/
    private final static String ICON_CLEAN_FLEX = "apps/cacheAdmin/flexcache_clean.png";

    /**Icon for clean flex cache.*/
    private final static String ICON_CLEAN_IMAGE = "apps/cacheAdmin/images_flush.png";

    /**Icon for clean flex cache.*/
    private final static String ICON_CLEAN_REPOSITORY = "apps/cacheAdmin/jsp_repository.png";

    /**Vaadin serial id.*/
    private static final long serialVersionUID = -8868998646787654217L;

    /**Icon for clean flex cache.*/
    private HorizontalLayout m_flushes;

    /**
     * public constructor.<p>
     */
    public CmsFlushCache() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        Button clean_flex = getCleanFlexButton();

        Button clean_image = getCleanImageButton();

        Button clean_core = getCleanCoreButton();

        Button clean_repo = getCleanRepoButton();

        m_flushes.addComponent(clean_flex);
        m_flushes.addComponent(clean_image);
        m_flushes.addComponent(clean_core);
        m_flushes.addComponent(clean_repo);
    }

    /**
     * Creates Button for the Clean Core function.<p>
     *
     * @return vaadin button
     */
    private Button getCleanCoreButton() {

        Button ret = CmsDefaultAppButtonProvider.createIconButton(
            CmsVaadinUtils.getMessageText(Messages.GUI_CORECACHE_CLEAN_ADMIN_TOOL_NAME_0),
            CmsVaadinUtils.getMessageText(Messages.GUI_CORECACHE_CLEAN_ADMIN_TOOL_HELP_0),
            new ExternalResource(OpenCmsTheme.getImageLink(ICON_CLEAN_CORE)));

        ret.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -4387739753282016853L;

            public void buttonClick(ClickEvent event) {

                final Window window = CmsBasicDialog.prepareWindow();
                CmsConfirmSimpleFlushDialog flushDialog = new CmsConfirmSimpleFlushDialog(
                    CmsVaadinUtils.getMessageText(Messages.GUI_CORECACHE_CLEAN_ADMIN_TOOL_CONF_0),
                    new Runnable() {

                        public void run() {

                            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, null));
                            window.close();
                        }
                    },
                    new Runnable() {

                        public void run() {

                            window.close();
                        }
                    });
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_CONFIRM_0));
                window.setContent(flushDialog);
                UI.getCurrent().addWindow(window);
            }
        });

        return ret;
    }

    /**
     * Creates Button for the clean FlexCache function.<p>
     *
     * @return a vaadin button
     */
    private Button getCleanFlexButton() {

        Button ret = CmsDefaultAppButtonProvider.createIconButton(
            CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_CLEAN_ADMIN_TOOL_NAME_0),
            CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_CLEAN_ADMIN_TOOL_HELP_0),
            new ExternalResource(OpenCmsTheme.getImageLink(ICON_CLEAN_FLEX)));
        ret.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 5045744580680900240L;

            public void buttonClick(ClickEvent event) {

                final Window window = CmsBasicDialog.prepareWindow();
                CmsFlexCacheCleanDialog flushDialog = new CmsFlexCacheCleanDialog(window);
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_CONFIRM_0));
                window.setContent(flushDialog);
                UI.getCurrent().addWindow(window);
            }
        });
        return ret;
    }

    /**
     * Creates a Button for the clean Image cache function.<p>
     *
     * @return a vaadin button
     */
    private Button getCleanImageButton() {

        Button ret = CmsDefaultAppButtonProvider.createIconButton(
            CmsVaadinUtils.getMessageText(Messages.GUI_IMAGECACHE_CLEAN_ADMIN_TOOL_NAME_0),
            CmsVaadinUtils.getMessageText(Messages.GUI_IMAGECACHE_CLEAN_ADMIN_TOOL_HELP_0),
            new ExternalResource(OpenCmsTheme.getImageLink(ICON_CLEAN_IMAGE)));

        ret.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -4513263981209222571L;

            public void buttonClick(ClickEvent event) {

                Window window = CmsBasicDialog.prepareWindow();
                CmsImageCacheCleanDialog dialog = new CmsImageCacheCleanDialog(window);
                window.setContent(dialog);
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_CONFIRM_0));
                UI.getCurrent().addWindow(window);
            }
        });

        return ret;
    }

    /**
     * Creates a Button for the clean repository function.<p>
     *
     * @return a vaadin button
     */
    private Button getCleanRepoButton() {

        Button ret = CmsDefaultAppButtonProvider.createIconButton(
            CmsVaadinUtils.getMessageText(Messages.GUI_JSP_REPOSITORY_ADMIN_TOOL_NAME_0),
            CmsVaadinUtils.getMessageText(Messages.GUI_JSP_REPOSITORY_ADMIN_TOOL_HELP_0),
            new ExternalResource(OpenCmsTheme.getImageLink(ICON_CLEAN_REPOSITORY)));
        ret.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -4513263981209222571L;

            public void buttonClick(ClickEvent event) {

                final Window window = CmsBasicDialog.prepareWindow();
                CmsConfirmSimpleFlushDialog flushDialog = new CmsConfirmSimpleFlushDialog(
                    CmsVaadinUtils.getMessageText(Messages.GUI_JSP_REPOSITORY_ADMIN_TOOL_CONF_0),
                    new Runnable() {

                        public void run() {

                            OpenCms.fireCmsEvent(
                                new CmsEvent(
                                    I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY,
                                    Collections.<String, Object> emptyMap()));
                            OpenCms.fireCmsEvent(
                                new CmsEvent(
                                    I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                                    Collections.<String, Object> singletonMap(
                                        "action",
                                        new Integer(CmsFlexCache.CLEAR_ENTRIES))));
                            window.close();
                        }
                    },
                    new Runnable() {

                        public void run() {

                            window.close();
                        }
                    });
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_CONFIRM_0));
                window.setContent(flushDialog);
                UI.getCurrent().addWindow(window);
            }
        });
        return ret;
    }
}
