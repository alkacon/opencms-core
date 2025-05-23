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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.cache;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ui.apps.cacheadmin.CmsImageCacheHelper;
import org.opencms.util.CmsFileUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The image cache overview dialog.<p>
 *
 * @since 7.0.5
 */
public class CmsImageCacheOverviewDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "image.stats";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Image Caches current size. */
    private String m_curSize;

    /** Image Caches keys. */
    private String m_keys;

    /** Image Caches variations. */
    private String m_variations;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsImageCacheOverviewDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsImageCacheOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited group to the db.<p>
     */
    @Override
    public void actionCommit() {

        // no saving needed
        setCommitErrors(new ArrayList());
    }

    /**
     * Returns the current size.<p>
     *
     * @return the current size
     */
    public String getCurSize() {

        return m_curSize;
    }

    /**
     * Returns the number of keys.<p>
     *
     * @return the number of keys
     */
    public String getKeys() {

        return m_keys;
    }

    /**
     * Returns the number of variations.<p>
     *
     * @return the number of variations
     */
    public String getVariations() {

        return m_variations;
    }

    /**
     * Sets the current size.<p>
     *
     * @param curSize the current size to set
     */
    public void setCurSize(String curSize) {

        m_curSize = curSize;
    }

    /**
     * Sets the number of keys.<p>
     *
     * @param keys the number of keys to set
     */
    public void setKeys(String keys) {

        m_keys = keys;
    }

    /**
     * Sets the number of variations.<p>
     *
     * @param variations the number of variations to set
     */
    public void setVariations(String variations) {

        m_variations = variations;
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

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_IMAGECACHE_LABEL_STATS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_IMAGECACHE_LABEL_MEMORY_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(2, 2));
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

        // initialize the cache object to use for the dialog
        initCacheObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        addWidget(new CmsWidgetDialogParameter(this, "keys", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "variations", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "curSize", PAGES[0], new CmsDisplayWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the cache object.<p>
     */
    protected void initCacheObject() {

        CmsImageCacheHelper helper = new CmsImageCacheHelper(getCms(), false, false, true);
        setKeys("" + helper.getFilesCount());
        setVariations("" + helper.getVariationsCount());
        setCurSize(CmsFileUtil.formatFilesize(helper.getVariationsSize(), getLocale()));
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
        setOnlineHelpUriCustom("/cache/flex/");
    }
}
