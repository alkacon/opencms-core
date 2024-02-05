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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Vaadin Layout with Buttons to clear the different types of cache.<p>
 */
public class CmsFlushCache extends Panel {

    /**
     * Interface for the dialogs.<p>
     */
    public interface I_CloseableDialog {

        /**
         * Sets the runnable which should be run to close window.
         *
         * @param closeRunnable runnable
         *  */
        void setCloseRunnable(Runnable closeRunnable);

        /**
         * Sets the runnable which should be run after ok action.<p>
         *
         * @param okRunnable runnable
         */
        void setOkRunnable(Runnable okRunnable);
    }

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsFlushCache.class.getName());

    /**Width of buttons.*/
    private static final String BUTTON_WIDTH = "250px";

    /**Vaadin serial id.*/
    private static final long serialVersionUID = -8868998646787654217L;

    /**Icon for clean flex cache.*/
    private VerticalLayout m_flushes;

    /**
     * public constructor.<p>
     */
    public CmsFlushCache() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_flushes.setSpacing(true);
        m_flushes.setMargin(true);
        setWidthUndefined();
        Component c = getButtonLayout(3, new Runnable() {

            public void run() {

                // nothing to do

            }

        });
        m_flushes.addComponent(c);

    }

    /**
     * Creates the button layout.<p>
     *
     * @param size number ob buttons per line
     * @param clickedRunnable click runnable
     * @return VerticalLayout
     */
    protected static VerticalLayout getButtonLayout(int size, final Runnable clickedRunnable) {

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        Button clean_flex = getFlushButton(
            Messages.GUI_CACHE_FLEXCACHE_CLEAN_ADMIN_TOOL_NAME_0,
            Messages.GUI_CACHE_FLEXCACHE_CLEAN_ADMIN_TOOL_HELP_0,
            new CmsFlexCacheCleanDialog(),
            clickedRunnable);

        Button clean_image = getFlushButton(
            Messages.GUI_CACHE_IMAGECACHE_CLEAN_ADMIN_TOOL_NAME_0,
            Messages.GUI_CACHE_IMAGECACHE_CLEAN_ADMIN_TOOL_HELP_0,
            new CmsImageCacheCleanDialog(),
            clickedRunnable);

        Button clean_core = getFlushButton(
            Messages.GUI_CACHE_CORECACHE_CLEAN_ADMIN_TOOL_NAME_0,
            Messages.GUI_CACHE_CORECACHE_CLEAN_ADMIN_TOOL_HELP_0,
            new CmsConfirmSimpleFlushDialog(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CORECACHE_CLEAN_ADMIN_TOOL_CONF_0)),
            new Runnable() {

                public void run() {

                    OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, null));
                    clickedRunnable.run();
                }
            });

        Button clean_repo = getFlushButton(
            Messages.GUI_CACHE_JSP_REPOSITORY_ADMIN_TOOL_NAME_0,
            Messages.GUI_CACHE_JSP_REPOSITORY_ADMIN_TOOL_HELP_0,
            new CmsConfirmSimpleFlushDialog(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JSP_REPOSITORY_ADMIN_TOOL_CONF_0)),
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
                                Integer.valueOf(CmsFlexCache.CLEAR_ENTRIES))));
                    clickedRunnable.run();
                }
            });

        Button reini = getFlushButton(
            Messages.GUI_CACHE_REINI_TOOL_NAME_0,
            Messages.GUI_CACHE_REINI_TOOL_NAME_HELP_0,
            new CmsConfirmSimpleFlushDialog(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_REINI_TOOL_CONF_0)),
            new Runnable() {

                public void run() {

                    try {
                        // re-initialize the workplace
                        OpenCms.getWorkplaceManager().initialize(A_CmsUI.getCmsObject());
                        // fire "clear caches" event to reload all cached resource bundles
                        OpenCms.fireCmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>());
                        clickedRunnable.run();
                    } catch (CmsException e) {
                        LOG.error("Unable to reinitialize workspace", e);
                    }
                }
            });

        List<Component> elements = new ArrayList<Component>();
        elements.add(clean_core);
        elements.add(clean_flex);
        elements.add(clean_image);
        elements.add(reini);
        elements.add(clean_repo);

        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        for (int i = 0; i < elements.size(); i++) {

            if (size > 0) {
                row.addComponent(elements.get(i));

                if (((i % (size - 1)) == 0) & (i > 0)) {
                    layout.addComponent(row);
                    row = new HorizontalLayout();
                    row.setSpacing(true);
                }

            } else {
                layout.addComponent(elements.get(i));
            }
        }

        return layout;
    }

    /**
     * Creates the button which opens the flush dialogs.<p>
     *
     * @return Button
     */
    protected static Button getFlushToolButton() {

        Button res = new Button(FontOpenCms.CACHE);
        res.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        res.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        res.addClickListener(new ClickListener() {

            /**vaadin serial id. */
            private static final long serialVersionUID = 7110311791006484514L;

            public void buttonClick(ClickEvent event) {

                Window window = CmsBasicDialog.prepareWindow(DialogWidth.content);

                window.setContent(new CmsFlushButtonHolderDialog(window));
                A_CmsUI.get().addWindow(window);
            }

        });
        return res;
    }

    /**
     * Creates Button to flush cashes.<p>
     *
     * @param captionMessage caption
     * @param descriptionMessage description
     * @param dialog layout to be shown
     * @param okRun runnable called by clicking button
     * @return button with click listener
     */
    private static Button getFlushButton(
        String captionMessage,
        String descriptionMessage,
        final I_CloseableDialog dialog,
        final Runnable okRun) {

        Button ret = new Button();
        ret.setWidth(BUTTON_WIDTH);
        ret.addStyleName("v-button");
        ret.addStyleName("o-button-blue");
        ret.setCaption(CmsVaadinUtils.getMessageText(captionMessage));
        ret.setDescription(CmsVaadinUtils.getMessageText(descriptionMessage));

        ret.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -4513263981209222571L;

            public void buttonClick(ClickEvent event) {

                final Window window = CmsBasicDialog.prepareWindow();
                dialog.setCloseRunnable(new Runnable() {

                    public void run() {

                        window.close();
                    }
                });
                dialog.setOkRunnable(okRun);
                window.setContent((Component)dialog);
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_CONFIRM_0));
                UI.getCurrent().addWindow(window);
            }
        });
        return ret;
    }
}
