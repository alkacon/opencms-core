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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.cache;

import org.opencms.flex.CmsFlexCache;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsRadioSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The flex cache clear dialog.<p>
 *
 * @since 7.0.5
 */
public class CmsFlexCacheClearDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "flex.clear";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Clean up mode constant. */
    private static final String MODE_ALL = "all";

    /** Clean up mode constant. */
    private static final String MODE_VARIATIONS = "variations";

    /** Widget value. */
    private String m_mode;

    /** Widget value. */
    private boolean m_offline;

    /** Widget value. */
    private boolean m_online;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsFlexCacheClearDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsFlexCacheClearDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Sends a clear caches event.<p>
     *
     * @throws JspException if something goes wrong
     */
    public void actionClearCaches() throws JspException {

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, null));

        setAction(CmsDialog.ACTION_CANCEL);
        actionCloseDialog();
    }

    /**
     * Commits the edited group to the db.<p>
     */
    @Override
    public void actionCommit() {

        try {
            int action = -1;
            if (isOnline() && isOffline()) {
                if (getMode().equals(MODE_ALL)) {
                    action = CmsFlexCache.CLEAR_ALL;
                } else {
                    action = CmsFlexCache.CLEAR_ENTRIES;
                }
            } else if (isOnline()) {
                if (getMode().equals(MODE_ALL)) {
                    action = CmsFlexCache.CLEAR_ONLINE_ALL;
                } else {
                    action = CmsFlexCache.CLEAR_ONLINE_ENTRIES;
                }
            } else if (isOffline()) {
                if (getMode().equals(MODE_ALL)) {
                    action = CmsFlexCache.CLEAR_OFFLINE_ALL;
                } else {
                    action = CmsFlexCache.CLEAR_OFFLINE_ENTRIES;
                }
            } else {
                if (getMode().equals(MODE_ALL)) {
                    action = CmsFlexCache.CLEAR_ALL;
                } else {
                    action = CmsFlexCache.CLEAR_ENTRIES;
                }
            }
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                    Collections.<String, Object> singletonMap(CmsFlexCache.CACHE_ACTION, Integer.valueOf(action))));
        } catch (Exception e) {
            setCommitErrors(Collections.singletonList((Throwable)e));
        }
    }

    /**
     * Purges the jsp repository.<p>
     *
     * @throws JspException if something goes wrong
     */
    public void actionPurgeJspRepository() throws JspException {

        OpenCms.fireCmsEvent(
            new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, Collections.<String, Object> emptyMap()));
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                Collections.<String, Object> singletonMap("action", Integer.valueOf(CmsFlexCache.CLEAR_ENTRIES))));

        setAction(CmsDialog.ACTION_CANCEL);
        actionCloseDialog();
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public String getMode() {

        return m_mode;
    }

    /**
     * Returns the offline.<p>
     *
     * @return the offline
     */
    public boolean isOffline() {

        return m_offline;
    }

    /**
     * Returns the online.<p>
     *
     * @return the online
     */
    public boolean isOnline() {

        return m_online;
    }

    /**
     * Sets the mode.<p>
     *
     * @param mode the mode to set
     */
    public void setMode(String mode) {

        m_mode = mode;
    }

    /**
     * Sets the offline.<p>
     *
     * @param offline the offline to set
     */
    public void setOffline(boolean offline) {

        m_offline = offline;
    }

    /**
     * Sets the online.<p>
     *
     * @param online the online to set
     */
    public void setOnline(boolean online) {

        m_online = online;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        int n = 1;

        // initialize the cache object to use for the dialog
        CmsFlexController controller = (CmsFlexController)getJsp().getRequest().getAttribute(
            CmsFlexController.ATTRIBUTE_NAME);
        CmsFlexCache cache = controller.getCmsCache();

        // widgets to display
        if (cache.cacheOffline()) {
            n = 2;
        }
        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_FLEXCACHE_LABEL_CLEAN_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, n));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close widget table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defaultActionHtmlEnd()
     */
    @Override
    protected String defaultActionHtmlEnd() {

        return "";
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        setKeyPrefix(KEY_PREFIX);

        // initialize the cache object to use for the dialog
        CmsFlexController controller = (CmsFlexController)getJsp().getRequest().getAttribute(
            CmsFlexController.ATTRIBUTE_NAME);
        CmsFlexCache cache = controller.getCmsCache();

        setOffline(true);
        setOnline(true);
        setMode(MODE_ALL);

        // widgets to display
        if (cache.cacheOffline()) {
            addWidget(new CmsWidgetDialogParameter(this, "offline", PAGES[0], new CmsCheckboxWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(this, "online", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "mode", PAGES[0], new CmsRadioSelectWidget(getModes())));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Overridden to set the online help path for this dialog.<p>
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    @Override
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        super.initWorkplaceMembers(jsp);
        setOnlineHelpUriCustom("/cache/flex/clean/");
    }

    /**
     * Returns a list with the possible modes for the clean action.<p>
     *
     * @return a list with the possible modes for the clean action
     */
    private List getModes() {

        ArrayList ret = new ArrayList();

        ret.add(
            new CmsSelectWidgetOption(
                MODE_VARIATIONS,
                getMode().equals(MODE_VARIATIONS),
                key(Messages.GUI_FLEXCACHE_CLEAN_MODE_VARIATIONS_0)));
        ret.add(
            new CmsSelectWidgetOption(
                MODE_ALL,
                getMode().equals(MODE_ALL),
                key(Messages.GUI_FLEXCACHE_CLEAN_MODE_ALL_0)));

        return ret;
    }
}
