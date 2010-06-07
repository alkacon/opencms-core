/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapTreeItem.java,v $
 * Date   : $Date: 2010/06/07 14:27:01 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarMoveButton;
import org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapItemCss;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.ui.CmsDnDListHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.tree.CmsDnDLazyTreeItem;
import org.opencms.gwt.client.ui.tree.CmsDnDTreeItem;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem.LoadState;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap entry tree item implementation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.tree.CmsLazyTreeItem
 * @see org.opencms.ade.sitemap.shared.CmsClientSitemapEntry
 */
public class CmsSitemapTreeItem extends CmsDnDLazyTreeItem {

    /** The CSS bundle used by this widget. */
    private static final I_CmsSitemapItemCss CSS = I_CmsLayoutBundle.INSTANCE.sitemapItemCss();

    /** The list item widget of this item. */
    private CmsListItemWidget m_listItemWidget;

    /** The original site path. */
    private String m_originalPath;

    /** The current site path. */
    private String m_sitePath;

    /**
     * Default constructor.<p>
     * 
     * @param widget the widget to use
     * @param sitePath the site path
     */
    public CmsSitemapTreeItem(CmsListItemWidget widget, String sitePath) {

        super(widget);
        String name = CmsResource.getName(sitePath);
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        setId(name);
        m_decoratedPanel.addDecorationBoxStyle(CSS.sitemapEntryDecoration());
        m_listItemWidget = widget;
        m_originalPath = sitePath;
        m_sitePath = sitePath;
    }

    /**
     * Returns the original site path, in case this entry has been moved or renamed.<p>
     *
     * @return the original site path
     */
    public String getOriginalPath() {

        return m_originalPath;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListItem#enableDnD(org.opencms.gwt.client.ui.CmsDnDListHandler)
     */
    @Override
    public void enableDnD(CmsDnDListHandler handler) {

        m_dndEnabled = true;
        handler.registerMouseHandler(this);
        m_children.setDnDHandler(handler);
        m_children.setDnDEnabled(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListItem#disableDnD()
     */
    @Override
    public void disableDnD() {

        m_children.setDnDEnabled(false);
        m_dndEnabled = false;
        removeDndMouseHandlers();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListItem#isHandleEvent(com.google.gwt.dom.client.NativeEvent)
     */
    @Override
    public boolean isHandleEvent(NativeEvent event) {

        if (!m_dndEnabled) {
            return false;
        }
        for (Widget w : getListItemWidget().getContentPanel()) {
            if (!(w instanceof CmsSitemapHoverbar)) {
                continue;
            }
            for (Widget b : (CmsSitemapHoverbar)w) {
                if (!(b instanceof CmsHoverbarMoveButton)) {
                    continue;
                }
                if (!((CmsHoverbarMoveButton)b).isEnabled()) {
                    return false;
                }
                EventTarget target = event.getEventTarget();
                if (com.google.gwt.dom.client.Element.is(target)) {
                    return b.getElement().isOrHasChild(com.google.gwt.dom.client.Element.as(target));
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#toString()
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(m_sitePath).append("\n");
        for (int i = 0; i < getChildCount(); i++) {
            CmsDnDTreeItem child = getChild(i);
            sb.append(child.toString());
        }
        return sb.toString();
    }

    /**
     * Refreshes the displayed data from the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        m_listItemWidget.setTitleLabel(entry.getTitle());
        m_listItemWidget.setAdditionalInfoValue(1, entry.getVfsPath());
        m_listItemWidget.updateTruncation();
    }

    /**
     * Updates the recursively the site path.<p>
     * 
     * @param sitePath the new site path to set
     */
    public void updateSitePath(String sitePath) {

        if (m_sitePath.equals(sitePath)) {
            // nothing to do
            return;
        }
        m_sitePath = sitePath;
        m_listItemWidget.setSubtitleLabel(sitePath);
        String name = CmsResource.getName(sitePath);
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        setId(name);
        m_listItemWidget.setAdditionalInfoValue(0, name);
        if (getLoadState() == LoadState.LOADED) {
            for (int i = 0; i < getChildCount(); i++) {
                CmsSitemapTreeItem item = (CmsSitemapTreeItem)getChild(i);
                item.updateSitePath(sitePath + CmsResource.getName(item.getSitePath()));
            }
        }
        m_listItemWidget.updateTruncation();
    }
}
