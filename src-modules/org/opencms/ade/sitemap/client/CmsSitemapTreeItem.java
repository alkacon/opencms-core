/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapTreeItem.java,v $
 * Date   : $Date: 2010/11/18 15:32:56 $
 * Version: $Revision: 1.39 $
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

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapItemCss;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap entry tree item implementation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.39 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.tree.CmsLazyTreeItem
 * @see org.opencms.ade.sitemap.shared.CmsClientSitemapEntry
 */
public class CmsSitemapTreeItem extends CmsLazyTreeItem {

    /**
     * Enum for the type of status icon which should be displayed.<p>
     */
    public enum StatusIcon {
        /** export status icon. */
        export,
        /** no status icon. */
        none,
        /** secure status icon. */
        secure
    }

    /** The CSS bundle used by this widget. */
    private static final I_CmsSitemapItemCss CSS = I_CmsLayoutBundle.INSTANCE.sitemapItemCss();

    /** The current sitemap entry. */
    protected CmsClientSitemapEntry m_entry;

    /** The list item widget of this item. */
    private CmsListItemWidget m_listItemWidget;

    /** The original site path. */
    private String m_originalPath;

    /**
     * Default constructor.<p>
     * 
     * @param widget the widget to use
     * @param entry the sitemap entry
     * @param oriSitePath the original site path
     */
    public CmsSitemapTreeItem(CmsListItemWidget widget, CmsClientSitemapEntry entry, String oriSitePath) {

        super(widget);
        m_decoratedPanel.addDecorationBoxStyle(CSS.sitemapEntryDecoration());
        m_listItemWidget = widget;
        m_originalPath = oriSitePath;
        m_entry = entry;
        setId(getName(entry.getSitePath()));
        updateSitePath(entry.getSitePath());
        updateSitemapReferenceStatus(entry);
        setDropEnabled(!m_entry.getProperties().containsKey(CmsSitemapManager.Property.sitemap));
        widget.setTitleEditable(true);
        widget.setTitleEditHandler(new I_CmsTitleEditHandler() {

            /**
             * @see org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler#handleEdit(org.opencms.gwt.client.ui.input.CmsLabel, com.google.gwt.user.client.ui.TextBox)
             */
            public void handleEdit(CmsLabel titleLabel, TextBox box) {

                final String text = box.getText();
                box.removeFromParent();
                if (CmsStringUtil.isEmpty(text)) {
                    titleLabel.setVisible(true);
                    String dialogTitle = Messages.get().key(Messages.GUI_EDIT_TITLE_ERROR_DIALOG_TITLE_0);
                    String dialogText = Messages.get().key(Messages.GUI_TITLE_CANT_BE_EMPTY_0);
                    CmsAlertDialog alert = new CmsAlertDialog(dialogTitle, dialogText);
                    alert.center();
                    return;
                }
                String oldTitle = m_entry.getTitle();

                if (!oldTitle.equals(text)) {

                    if (m_entry.isNew()) {
                        CmsRpcAction<String> action = new CmsRpcAction<String>() {

                            /**
                             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                             */
                            @Override
                            public void execute() {

                                start(0, false);
                                CmsCoreProvider.getService().translateUrlName(text, this);
                            }

                            /**
                             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                             */
                            @Override
                            protected void onResponse(String result) {

                                stop(false);
                                String newUrlName = result;
                                if (!m_entry.isRoot()) {
                                    String parentPath = CmsResource.getParentFolder(m_entry.getSitePath());
                                    CmsClientSitemapEntry parent = CmsSitemapView.getInstance().getController().getEntry(
                                        parentPath);
                                    newUrlName = CmsSitemapController.ensureUniqueName(parent, result);
                                }
                                CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry(m_entry);
                                newEntry.setTitle(text);
                                newEntry.setName(newUrlName);
                                CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                                controller.editAndChangeName(
                                    m_entry,
                                    text,
                                    newUrlName,
                                    m_entry.getVfsPath(),
                                    m_entry.getProperties(),
                                    false);
                            }

                        };
                        action.execute();
                    } else {
                        CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                        controller.edit(m_entry, text, m_entry.getVfsPath(), m_entry.getProperties(), false);
                        //CmsSitemapView.getInstance().getController().addChange(edit, false);
                    }

                }

                titleLabel.setVisible(true);
            }
        });
    }

    /**
     * Given the path of a sitemap entry, this method returns the URL which should be displayed to the user.<p>
     * 
     * @param sitePath the site path of a sitemap entry 
     * 
     * @return the URL which should be displayed to the user
     */
    public String getDisplayedUrl(String sitePath) {

        String context = CmsCoreProvider.get().getContext();
        if (m_entry.getInheritedProperties() == null) {
            return CmsStringUtil.joinPaths(context, sitePath);
        }
        CmsComputedPropertyValue exportProp = m_entry.getInheritedProperties().get("export");
        if ((exportProp != null) && Boolean.parseBoolean(exportProp.getOwnValue())) {
            String exportName = CmsSitemapView.getInstance().getController().getData().getExportName();
            if (exportName == null) {
                exportName = CmsCoreProvider.get().getSiteRoot();
            }
            String rfsPrefix = CmsSitemapView.getInstance().getController().getData().getExportRfsPrefix();
            if (rfsPrefix != null) {
                return CmsStringUtil.joinPaths(rfsPrefix, exportName, sitePath);
            }
        }
        return CmsStringUtil.joinPaths(context, sitePath);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getDragHelper(I_CmsDropTarget)
     */
    @Override
    public Element getDragHelper(I_CmsDropTarget target) {

        m_listItemWidget.setBackground(Background.DEFAULT);
        return super.getDragHelper(target);
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
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_entry.getSitePath();
    }

    /**
     * Turns the highlighting for this item on or off.<p>
     * 
     * @param highlightOn if true, the highlighting is turned on, else off
     */
    public void highlight(boolean highlightOn) {

        if (highlightOn) {
            m_listItemWidget.getContentPanel().addStyleName(CSS.highlight());
        } else {
            m_listItemWidget.getContentPanel().removeStyleName(CSS.highlight());

        }
    }

    /**
     * Temporarily highlights an item.<p>
     * 
     * @param duration the duration for which  
     */
    public void highlightTemporarily(int duration) {

        int blinkInterval = 300;
        final int blinkCount = duration / blinkInterval;

        Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {

            private int m_counter;

            /**
             * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
             */
            public boolean execute() {

                boolean finish = m_counter > blinkCount;
                highlight((m_counter % 2 == 0) && !finish);
                m_counter += 1;
                return !finish;
            }
        }, blinkInterval);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    @Override
    public void onDragCancel() {

        removeStyleName(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().positionIndicator());
        super.onDragCancel();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDrop(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    @Override
    public void onDrop(I_CmsDropTarget target) {

        removeStyleName(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().positionIndicator());
        super.onDrop(target);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#onStartDrag(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    @Override
    public void onStartDrag(I_CmsDropTarget target) {

        setOpen(false);
        // transform the widget into a position indicator
        addStyleName(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().positionIndicator());
        CmsSitemapHoverbar hoverbar = getHoverbar();
        if (hoverbar != null) {
            hoverbar.hide();
        }
    }

    /**
     * Resets entry appearance.<p>
     */
    public void resetEntry() {

        updateEntry(m_entry);
    }

    /**
     * Sets the background color of the list item widget.<p>
     * 
     * If the background is <code>null</code>, the widget will be shown with its default style.<p>
     * 
     * @param background the background color to set
     */
    public void setBackgroundColor(Background background) {

        getListItemWidget().setBackground(background);
    }

    /**
     * Changes the status icon of the sitemap item.<p>
     * 
     * @param status the value representing the status icon 
     */
    public void setStatus(StatusIcon status) {

        switch (status) {
            case export:
                m_listItemWidget.setIcon(CSS.export());
                m_listItemWidget.setIconTitle(Messages.get().key(Messages.GUI_ICON_TITLE_EXPORT_0));
                break;
            case secure:
                m_listItemWidget.setIcon(CSS.secure());
                m_listItemWidget.setIconTitle(Messages.get().key(Messages.GUI_ICON_TITLE_SECURE_0));
                break;
            case none:
            default:
                m_listItemWidget.setIcon(CSS.normal());
                m_listItemWidget.setIconTitle(null);
                break;
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#toString()
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(m_entry.getSitePath()).append("\n");
        for (int i = 0; i < getChildCount(); i++) {
            CmsTreeItem child = getChild(i);
            if (child instanceof CmsLazyTreeItem.LoadingItem) {
                continue;
            }
            sb.append(child.toString());
        }
        return sb.toString();
    }

    /**
     * Updates the color of the sitemap tree item.<p>
     * 
     * @param entry the entry whose data should be used to update the color of the sitemap tree item.<p>
     */
    public void updateColor(CmsClientSitemapEntry entry) {

        if (entry.getProperties().containsKey("sitemap")) {
            return;
        }
        switch (entry.getEditStatus()) {
            case edited:
                setBackgroundColor(Background.RED);
                break;
            case created:
                setBackgroundColor(Background.BLUE);
                break;
            case normal:
            default:
                setBackgroundColor(Background.DEFAULT);
        }

    }

    /**
     * Refreshes the displayed data from the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        // since we keep a reference to the same entry we do not have to update it
        m_listItemWidget.setTitleLabel(entry.getTitle());
        m_listItemWidget.setAdditionalInfoValue(1, entry.getVfsPath());
        m_listItemWidget.updateTruncation();
        updateSitemapReferenceStatus(entry);
        updateColor(entry);
        setDropEnabled(!m_entry.getProperties().containsKey(CmsSitemapManager.Property.sitemap));
    }

    /**
     * Updates the site path using the current site entry's data.<p>
     */
    public void updateSitePath() {

        updateSitePath(m_entry.getSitePath());
    }

    /**
     * Updates the recursively the site path.<p>
     * 
     * @param sitePath the new site path to set
     */
    public void updateSitePath(String sitePath) {

        String newSubTitle = getDisplayedUrl(sitePath);

        m_listItemWidget.setSubtitleLabel(newSubTitle);
        String name = getName(sitePath);
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

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#getMoveHandle()
     */
    @Override
    protected I_CmsDragHandle getMoveHandle() {

        CmsSitemapHoverbar hoverbar = getHoverbar();
        if (hoverbar != null) {
            int count = hoverbar.getWidgetCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    Widget w = hoverbar.getWidget(i);
                    if (w instanceof I_CmsDragHandle) {
                        return (I_CmsDragHandle)w;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return the name of this item, which can differ from the entry name for root nodes.<p>
     * 
     * @param sitePath the sitemap entry's site path 
     * 
     * @return the name
     */
    protected String getName(String sitePath) {

        String name = CmsResource.getName(sitePath);
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;

    }

    /**
     * Changes the look of this widget if the entry passed as a parameter has a reference to a sub-sitemap.<p>
     * 
     * @param entry the entry which should be checked 
     */
    protected void updateSitemapReferenceStatus(CmsClientSitemapEntry entry) {

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(entry.getOwnProperty(CmsSitemapManager.Property.sitemap.name()))) {
            m_listItemWidget.setBackground(Background.YELLOW);
        } else {
            m_listItemWidget.setBackground(Background.DEFAULT);
        }
    }

    /**
     * Retrieves the hoverbar, can be <code>null</code> if not attached.<p>
     * 
     * @return the hoverbar, or <code>null</code> if not attached
     */
    private CmsSitemapHoverbar getHoverbar() {

        for (Widget w : getListItemWidget().getContentPanel()) {
            if (!(w instanceof CmsSitemapHoverbar)) {
                continue;
            }
            return (CmsSitemapHoverbar)w;
        }
        return null;
    }

}
