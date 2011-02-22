/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapTreeItem.java,v $
 * Date   : $Date: 2011/02/22 09:46:09 $
 * Version: $Revision: 1.57 $
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
import org.opencms.ade.sitemap.client.control.CmsSitemapController.ReloadMode;
import org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapItemCss;
import org.opencms.ade.sitemap.shared.CmsClientLock;
import org.opencms.ade.sitemap.shared.CmsClientProperty;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsPropertyModification;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler;
import org.opencms.gwt.client.ui.CmsListItemWidgetUtil;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsLabel.I_TitleGenerator;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.PageIcon;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap entry tree item implementation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.57 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.tree.CmsLazyTreeItem
 * @see org.opencms.ade.sitemap.shared.CmsClientSitemapEntry
 */
public class CmsSitemapTreeItem extends CmsLazyTreeItem {

    /**
     * Label generator for the detail page info label.<p>
     */
    protected class DetailPageLabelTitleGenerator implements I_TitleGenerator {

        /** The title to use for the detail page label.*/
        private String m_detailPageTitle;

        /**
         * @see org.opencms.gwt.client.ui.input.CmsLabel.I_TitleGenerator#getTitle(java.lang.String)
         */
        public String getTitle(String originalText) {

            return m_detailPageTitle;
        }

        /** 
         * Sets the title to use for the detail page label. 
         * 
         * @param detailPageTitle the title to use 
         */
        public void setDetailPageTitle(String detailPageTitle) {

            m_detailPageTitle = detailPageTitle;
        }
    }

    /** 
     * Class which contains detail page info messages for a sitemap entry.<p>
     */
    class DetailPageMessages {

        /** The message to display on the sitemap entry. */
        private String m_message;

        /** The title to display on the label which displays the message. */
        private String m_title;

        /**
         * Constructs a new detail page message bean.<p>
         * 
         * @param message the message to display on the sitemap entry
         * @param title  the title to display on the label which displays the message
         */
        public DetailPageMessages(String message, String title) {

            super();
            m_message = message;
            m_title = title;
        }

        /**
         * Returns the message to display on the sitemap entry.<p>
         * 
         * @return the message to display on the sitemap entry
         */
        public String getMessage() {

            return m_message;
        }

        /**
         * Returns the title to display on the label which displays the message.
         * 
         * @return  the title to display on the label which displays the message.
         */
        public String getTitle() {

            return m_title;
        }

    }

    /** The CSS bundle used by this widget. */
    private static final I_CmsSitemapItemCss CSS = I_CmsLayoutBundle.INSTANCE.sitemapItemCss();

    /** A map of sitemap tree items by entry id. */
    private static Map<CmsUUID, CmsSitemapTreeItem> m_itemsById = new HashMap<CmsUUID, CmsSitemapTreeItem>();

    /** The current sitemap entry. */
    protected CmsClientSitemapEntry m_entry;

    /** The detail page label title generator. */
    private DetailPageLabelTitleGenerator m_detailPageLabelTitleGenerator;

    private CmsStyleVariable m_inNavigationStyle;

    private HTML m_lockIcon;

    private CmsStyleVariable m_openerForNonNavigationStyle;

    /** The page icon. */
    private PageIcon m_pageIcon;

    /**
     * Default constructor.<p>
     * 
     * @param widget the widget to use
     * @param entry the sitemap entry
     */
    public CmsSitemapTreeItem(CmsListItemWidget widget, CmsClientSitemapEntry entry) {

        super(widget, false);
        m_decoratedPanel.addDecorationBoxStyle(CSS.sitemapEntryDecoration());
        m_listItemWidget = widget;
        m_detailPageLabelTitleGenerator = new DetailPageLabelTitleGenerator();
        m_listItemWidget.getSubTitleSuffix().setTitleGenerator(m_detailPageLabelTitleGenerator);
        m_entry = entry;
        m_inNavigationStyle = new CmsStyleVariable(this);
        m_openerForNonNavigationStyle = new CmsStyleVariable(m_opener);
        m_listItemWidget.addTitleStyleName(CSS.itemTitle());
        m_listItemWidget.setFixedIconClasses(CmsIconUtil.getResourceIconClasses(
            entry.getResourceTypeName(),
            entry.getSitePath(),
            false));
        updateInNavigation(entry);
        m_itemsById.put(entry.getId(), this);
        setId(getName(entry.getSitePath()));
        updateSitePath(entry.getSitePath());
        updateSitemapReferenceStatus(entry);
        updateDetailPageStatus();
        setLockIcon(entry.getLock());
        if (!entry.isFolderType()) {
            hideOpeners();
        }
        setDropEnabled(m_entry.isFolderType() && !m_entry.hasForeignFolderLock());
        m_listItemWidget.setTitleEditHandler(new I_CmsTitleEditHandler() {

            /**
             * @see org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler#handleEdit(org.opencms.gwt.client.ui.input.CmsLabel, com.google.gwt.user.client.ui.TextBox)
             */
            public void handleEdit(CmsLabel titleLabel, TextBox box) {

                final String newTitle = box.getText();
                box.removeFromParent();
                if (CmsStringUtil.isEmpty(newTitle)) {
                    titleLabel.setVisible(true);
                    String dialogTitle = Messages.get().key(Messages.GUI_EDIT_TITLE_ERROR_DIALOG_TITLE_0);
                    String dialogText = Messages.get().key(Messages.GUI_TITLE_CANT_BE_EMPTY_0);
                    CmsAlertDialog alert = new CmsAlertDialog(dialogTitle, dialogText);
                    alert.center();
                    return;
                }
                String oldTitle = m_entry.getTitle();

                if (!oldTitle.equals(newTitle)) {
                    CmsPropertyModification propMod = new CmsPropertyModification(
                        m_entry.getId(),
                        CmsClientProperty.PROPERTY_NAVTEXT,
                        newTitle,
                        true);
                    final List<CmsPropertyModification> propChanges = Collections.<CmsPropertyModification> singletonList(propMod);

                    if (m_entry.isNew()) {
                        CmsRpcAction<String> action = new CmsRpcAction<String>() {

                            /**
                             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                             */
                            @Override
                            public void execute() {

                                start(0, false);
                                CmsCoreProvider.getService().translateUrlName(newTitle, this);
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
                                newEntry.setName(newUrlName);
                                CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                                controller.editAndChangeName(
                                    m_entry,
                                    newUrlName,
                                    m_entry.getVfsPath(),
                                    propChanges,
                                    false,
                                    ReloadMode.none);
                            }

                        };
                        action.execute();
                    } else {
                        CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                        controller.edit(m_entry, m_entry.getVfsPath(), propChanges, false);
                    }

                }

                titleLabel.setVisible(true);
            }
        });
    }

    /** 
     * Looks up a sitemap tree item by entry id.<p>
     * 
     * @param id the sitemap entry id 
     * @return the corresponding sitemap tree item, or null if there is none 
     */
    public static CmsSitemapTreeItem getItemById(CmsUUID id) {

        return m_itemsById.get(id);
    }

    /**
     * Returns the current page info of this sitemap as {@link CmsListInfoBean}.<p>
     * 
     * @return the current page info of this sitemap as {@link CmsListInfoBean}
     */
    public CmsListInfoBean getCurrentPageInfo() {

        CmsListInfoBean info = new CmsListInfoBean();

        info.setPageIcon(m_pageIcon);
        info.setTitle(m_entry.getTitle());
        info.setSubTitle(getDisplayedUrl(m_entry.getSitePath()));

        Map<String, String> addInfo = new LinkedHashMap<String, String>();
        addInfo.put(Messages.get().key(Messages.GUI_NAME_0), m_entry.getName());
        addInfo.put(Messages.get().key(Messages.GUI_VFS_PATH_0), m_entry.getVfsPath());
        info.setAdditionalInfo(addInfo);

        return info;
    }

    /**
     * Returns the detail page data which should be displayed for a sitemap entry.<p>
     * 
     * @param entryId the sitemap entry id
     *  
     * @return a detail page message bean for that sitemap entry
     */
    public DetailPageMessages getDetailPageMessages(CmsUUID entryId) {

        CmsDetailPageTable detailPageTable = CmsSitemapView.getInstance().getController().getDetailPageTable();
        String type;
        String text = "";
        String suffixTitle = null;
        switch (detailPageTable.getStatus(entryId)) {
            case firstDetailPage:
                type = detailPageTable.get(entryId).getType();
                suffixTitle = Messages.get().key(Messages.GUI_MAIN_DETAIL_PAGE_TITLE_1, getNiceTypeName(type));
                text = "(*" + type + ")";
                break;
            case otherDetailPage:
                type = detailPageTable.get(entryId).getType();
                suffixTitle = Messages.get().key(Messages.GUI_DETAIL_PAGE_TITLE_1, getNiceTypeName(type));
                text = "(" + type + ")";
                break;
            case noDetailPage:
            default:
                text = null;
                break;
        }
        return new DetailPageMessages(text, suffixTitle);

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
        if (m_entry.isLeafType() && sitePath.endsWith("/")) {
            sitePath = sitePath.substring(0, sitePath.length() - 1);
        }
        CmsSitemapController controller = CmsSitemapView.getInstance().getController();
        String exportProp = controller.getEffectiveProperty(m_entry, "export");
        if ("true".equals(exportProp)) {
            String exportName = m_entry.getExportName();
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
     * @see org.opencms.gwt.client.ui.tree.CmsTreeItem#getPath()
     */
    @Override
    public String getPath() {

        return getSitePath();
    }

    /**
     * Returns the sitemap entry.<p>
     * 
     * @return the sitemap entry
     */
    public CmsClientSitemapEntry getSitemapEntry() {

        return m_entry;
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
     * Sets the lock icon.<p>
     * 
     * @param lock the current item lock
     */
    public void setLockIcon(CmsClientLock lock) {

        if (m_lockIcon == null) {
            m_lockIcon = new HTML();
            m_listItemWidget.getContentPanel().add(m_lockIcon);
        }
        if ((lock == null) || lock.getLockType().isUnlocked()) {
            m_lockIcon.setStyleName(CSS.lockIcon());
        } else {
            if (lock.isOwnedByUser()) {
                switch (lock.getLockType()) {
                    case EXCLUSIVE:
                    case INHERITED:
                    case TEMPORARY:
                        m_lockIcon.setStyleName(CSS.lockIcon() + " " + CSS.lockOpen());
                        break;
                    case SHARED_EXCLUSIVE:
                    case SHARED_INHERITED:
                        m_lockIcon.setStyleName(CSS.lockIcon() + " " + CSS.lockSharedOpen());
                        break;
                    default:
                        // remove for all other cases
                        m_lockIcon.setStyleName(CSS.lockIcon());
                }
            } else {
                switch (lock.getLockType()) {
                    case EXCLUSIVE:
                    case INHERITED:
                    case TEMPORARY:
                        m_lockIcon.setStyleName(CSS.lockIcon() + " " + CSS.lockClosed());
                        break;
                    case SHARED_EXCLUSIVE:
                    case SHARED_INHERITED:
                        m_lockIcon.setStyleName(CSS.lockIcon() + " " + CSS.lockSharedClosed());
                        break;
                    default:
                        // remove for all other cases
                        m_lockIcon.setStyleName(CSS.lockIcon());
                }
            }
            // TODO: localization
            m_lockIcon.setTitle("Lock owned by " + lock.getLockOwner());
        }
    }

    /**
     * Sets the icon.<p>
     *
     * @param icon the icon to set
     */
    public void setPageIcon(PageIcon icon) {

        m_pageIcon = icon;
        CmsListItemWidgetUtil.setPageIcon(m_listItemWidget, icon);
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

    }

    /**
     * Updates the detail page description.<p>
     */
    public void updateDetailPageStatus() {

        DetailPageMessages detailPageMessages = getDetailPageMessages(m_entry.getId());
        m_detailPageLabelTitleGenerator.setDetailPageTitle(detailPageMessages.getTitle());
        m_listItemWidget.updateTruncation();
        CmsLabel label = m_listItemWidget.getSubTitleSuffix();
        label.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().subtitleSuffix());
        m_listItemWidget.setSubtitleSuffixText(detailPageMessages.getMessage());
    }

    /**
     * Refreshes the displayed data from the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        // since we keep a reference to the same entry we do not have to update it
        m_listItemWidget.setTitleLabel(entry.getTitle());
        String shownPath = entry.getVfsPath();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(shownPath)) {
            shownPath = "-";
        }
        m_listItemWidget.setAdditionalInfoValue(1, shownPath);
        updateSitemapReferenceStatus(entry);
        updateColor(entry);
        updateSitePath();
        updateDetailPageStatus();
        setLockIcon(entry.getLock());
        updateInNavigation(entry);
        getListItemWidget().setFixedIconClasses(
            CmsIconUtil.getResourceIconClasses(entry.getResourceTypeName(), entry.getSitePath(), false));
        setDropEnabled(m_entry.isFolderType() && !m_entry.hasForeignFolderLock());
    }

    /**
     * Updates the in navigation properties of the displayed entry.<p>
     * 
     * @param entry the sitemap entry 
     */
    public void updateInNavigation(CmsClientSitemapEntry entry) {

        if (entry.isInNavigation()) {
            m_inNavigationStyle.setValue(null);
            m_listItemWidget.setTitleEditable(true);
        } else {
            m_inNavigationStyle.setValue(CSS.notInNavigationEntry());
            m_listItemWidget.setTitleEditable(false);
        }
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

        //        CmsDetailPageTable detailPageTable = CmsSitemapView.getInstance().getController().getDetailPageTable();
        //        String type;
        //        String suffix = "";
        //        switch (detailPageTable.getStatus(m_entry.getId())) {
        //            case firstDetailPage:
        //                type = detailPageTable.get(m_entry.getId()).getType();
        //                suffix = "&nbsp;&nbsp;" + wrapBold("(*" + type + ")", "Default detail page for " + type);
        //                break;
        //            case otherDetailPage:
        //                type = detailPageTable.get(m_entry.getId()).getType();
        //                suffix = "&nbsp;&nbsp;" + wrapBold("(" + type + ")", "Detail page for " + type);
        //                break;
        //            case noDetailPage:
        //            default:
        //                suffix = "";
        //                break;
        //        }
        //        newSubTitle = newSubTitle + suffix;

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
     * Helper method for adding the marker widget.<p>
     * 
     * @param text the text for the marker widget 
     * 
     * @return the new marker widget 
     */
    protected Widget addMarker(String text) {

        Label label = new Label(text);
        label.addStyleName(CSS.marker());
        m_listItemWidget.addButton(label);
        return label;
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
     * Returns the nice type name for a resource type.<p>
     * 
     * @param type the type identifier 
     * 
     * @return the nice type name 
     */
    protected String getNiceTypeName(String type) {

        // TODO: return nice type name by getting information from the sitemap controller 
        return type;
    }

    /**
     * @see org.opencms.gwt.client.ui.tree.CmsLazyTreeItem#onChangeChildren()
     */
    @Override
    protected void onChangeChildren() {

        super.onChangeChildren();
        if (m_openerForNonNavigationStyle == null) {
            // happens when initializing
            return;
        }
        Iterator<Widget> childIt = m_children.iterator();
        while (childIt.hasNext()) {
            Widget childWidget = childIt.next();
            if (childWidget instanceof CmsSitemapTreeItem) {
                CmsSitemapTreeItem treeItem = (CmsSitemapTreeItem)childWidget;
                if (treeItem.getSitemapEntry().isInNavigation()) {
                    m_openerForNonNavigationStyle.setValue(null);
                    return;
                }
            }
        }
        m_openerForNonNavigationStyle.setValue(CSS.notInNavigationEntry());
    }

    /**
     * Changes the look of this widget if the entry passed as a parameter has a reference to a sub-sitemap.<p>
     * 
     * @param entry the entry which should be checked 
     */
    protected void updateSitemapReferenceStatus(CmsClientSitemapEntry entry) {

        if (entry.isSubSitemapType()) {
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
