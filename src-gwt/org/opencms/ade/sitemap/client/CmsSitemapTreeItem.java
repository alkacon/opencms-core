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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapItemCss;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsLabel.I_TitleGenerator;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsAdditionalInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.LockIcon;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap entry tree item implementation.<p>
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

    /** The CSS bundle used by this widget. */
    private static final I_CmsSitemapItemCss CSS = I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss();

    /** A map of sitemap tree items by entry id. */
    private static Map<CmsUUID, CmsSitemapTreeItem> m_itemsById = new HashMap<CmsUUID, CmsSitemapTreeItem>();

    /** The current sitemap entry id. */
    protected CmsUUID m_entryId;

    /** The detail page label title generator. */
    private DetailPageLabelTitleGenerator m_detailPageLabelTitleGenerator;

    /** Style variable for to toggle in navigation style. */
    private CmsStyleVariable m_inNavigationStyle;

    /** Style variable for opener. */
    private CmsStyleVariable m_openerForNonNavigationStyle;

    /**
     * Default constructor.<p>
     * 
     * @param entry the sitemap entry
     */
    public CmsSitemapTreeItem(CmsClientSitemapEntry entry) {

        super(generateItemWidget(entry), false);
        m_entryId = entry.getId();
        m_decoratedPanel.addDecorationBoxStyle(CSS.sitemapEntryDecoration());
        m_detailPageLabelTitleGenerator = new DetailPageLabelTitleGenerator();
        getListItemWidget().getSubTitleSuffix().setTitleGenerator(m_detailPageLabelTitleGenerator);
        getListItemWidget().addOpenHandler(new OpenHandler<CmsListItemWidget>() {

            public void onOpen(OpenEvent<CmsListItemWidget> event) {

                CmsSitemapView.getInstance().getController().updateSingleEntry(m_entryId);
            }
        });
        getListItemWidget().addIconClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                CmsClientSitemapEntry sitemapEntry = controller.getEntryById(m_entryId);
                if (sitemapEntry != null) {
                    if (sitemapEntry.isSubSitemapType()) {
                        controller.openSiteMap(sitemapEntry.getSitePath());
                    } else {
                        controller.leaveEditor(sitemapEntry.getSitePath());
                    }
                }
            }
        });
        m_inNavigationStyle = new CmsStyleVariable(this);
        m_openerForNonNavigationStyle = new CmsStyleVariable(m_opener);
        getListItemWidget().addTitleStyleName(CSS.itemTitle());
        updateInNavigation(entry);
        m_itemsById.put(m_entryId, this);
        setId(getName(entry.getSitePath()));
        updateSitePath(entry.getSitePath());
        updateDetailPageStatus();
        updateLock(entry);
        if (!entry.isFolderType()) {
            hideOpeners();
        }
        setDropEnabled(entry.isFolderType() && !entry.hasForeignFolderLock());
        getListItemWidget().setTitleEditHandler(new I_CmsTitleEditHandler() {

            /**
             * @see org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler#handleEdit(org.opencms.gwt.client.ui.input.CmsLabel, com.google.gwt.user.client.ui.TextBox)
             */
            public void handleEdit(CmsLabel titleLabel, TextBox box) {

                CmsClientSitemapEntry editEntry = getSitemapEntry();
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
                String oldTitle = editEntry.getTitle();
                if (!oldTitle.equals(newTitle)) {
                    CmsPropertyModification propMod = new CmsPropertyModification(
                        editEntry.getId(),
                        CmsClientProperty.PROPERTY_NAVTEXT,
                        newTitle,
                        true);
                    final List<CmsPropertyModification> propChanges = new ArrayList<CmsPropertyModification>();
                    propChanges.add(propMod);
                    CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                    if (editEntry.isNew() && !editEntry.isRoot()) {
                        String urlName = controller.ensureUniqueName(
                            CmsResource.getParentFolder(editEntry.getSitePath()),
                            newTitle);
                        if (oldTitle.equals(editEntry.getPropertyValue(CmsClientProperty.PROPERTY_TITLE))) {
                            CmsPropertyModification titleMod = new CmsPropertyModification(
                                editEntry.getId(),
                                CmsClientProperty.PROPERTY_TITLE,
                                newTitle,
                                true);
                            propChanges.add(titleMod);
                        }
                        controller.editAndChangeName(editEntry, urlName, propChanges, true, CmsReloadMode.none);
                    } else {
                        controller.edit(editEntry, propChanges, CmsReloadMode.none);
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
     * Generates the list item widget for the tree item.<p>
     * 
     * @param entry the sitemap entry
     * 
     * @return the list item widget
     */
    private static CmsListItemWidget generateItemWidget(CmsClientSitemapEntry entry) {

        CmsListInfoBean infoBean = getInfoBean(entry);
        final CmsListItemWidget itemWidget = new CmsListItemWidget(infoBean);
        itemWidget.setIcon(CmsSitemapView.getInstance().getIconForEntry(entry));
        itemWidget.setIconTitle(entry.isSubSitemapType()
        ? Messages.get().key(Messages.GUI_HOVERBAR_GOTO_SUB_0)
        : Messages.get().key(Messages.GUI_HOVERBAR_GOTO_0));
        setExpiredStyle(entry, itemWidget);
        return itemWidget;
    }

    /**
     * Returns the list info bean for the given entry.<p>
     * 
     * @param entry the sitemap entry
     * 
     * @return the list info bean
     */
    private static CmsListInfoBean getInfoBean(CmsClientSitemapEntry entry) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        // showing the resource type icon of the default file in navigation mode
        infoBean.setResourceType(CmsStringUtil.isNotEmptyOrWhitespaceOnly(entry.getDefaultFileType())
        ? entry.getDefaultFileType()
        : entry.getResourceTypeName());
        infoBean.setResourceState(entry.getResourceState());

        List<CmsAdditionalInfoBean> infos = new ArrayList<CmsAdditionalInfoBean>();
        infos.add(new CmsAdditionalInfoBean(Messages.get().key(Messages.GUI_NAME_0), entry.getName(), null));
        CmsClientProperty titleProperty = entry.getOwnProperties().get(CmsClientProperty.PROPERTY_TITLE);
        if ((titleProperty != null) && !titleProperty.isEmpty()) {
            infos.add(new CmsAdditionalInfoBean(
                Messages.get().key(Messages.GUI_TITLE_PROPERTY_0),
                titleProperty.getEffectiveValue(),
                null));
        }
        String shownPath = entry.getVfsPath();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(shownPath)) {
            shownPath = "-";
        }
        infos.add(new CmsAdditionalInfoBean(Messages.get().key(Messages.GUI_VFS_PATH_0), shownPath, null));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(entry.getDateReleased())) {
            infos.add(new CmsAdditionalInfoBean(
                Messages.get().key(Messages.GUI_LABEL_DATE_RELEASED_0),
                entry.getDateReleased(),
                null));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(entry.getDateExpired())) {
            infos.add(new CmsAdditionalInfoBean(
                Messages.get().key(Messages.GUI_LABEL_DATE_EXPIRED_0),
                entry.getDateExpired(),
                null));
        }
        if (entry.getEntryType() == EntryType.redirect) {
            infos.add(new CmsAdditionalInfoBean(
                Messages.get().key(Messages.GUI_LABEL_TARGET_0),
                entry.getRedirectTarget(),
                null));
        }
        infoBean.setAdditionalInfo(infos);
        return infoBean;
    }

    /**
     * Sets the expired item style if appropriate.<p>
     * 
     * @param entry the sitemap entry
     * @param itemWidget the item widget
     */
    private static void setExpiredStyle(CmsClientSitemapEntry entry, CmsListItemWidget itemWidget) {

        if (!entry.isResleasedAndNotExpired()) {
            itemWidget.getContentPanel().addStyleName(
                I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().expiredOrNotReleased());
        } else {
            itemWidget.getContentPanel().removeStyleName(
                I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().expiredOrNotReleased());
        }
    }

    /**
     * Given the path of a sitemap entry, this method returns the URL which should be displayed to the user.<p>
     * 
     * @param sitePath the site path of a sitemap entry 
     * 
     * @return the URL which should be displayed to the user
     */
    public String getDisplayedUrl(String sitePath) {

        if (getSitemapEntry().isLeafType() && sitePath.endsWith("/")) {
            sitePath = sitePath.substring(0, sitePath.length() - 1);
        }
        CmsSitemapController controller = CmsSitemapView.getInstance().getController();
        String exportProp = controller.getEffectiveProperty(getSitemapEntry(), "export");
        if ("true".equals(exportProp)) {
            String exportName = getSitemapEntry().getExportName();
            if (exportName == null) {
                exportName = CmsCoreProvider.get().getSiteRoot();
            }
            String rfsPrefix = CmsSitemapView.getInstance().getController().getData().getExportRfsPrefix();
            if (rfsPrefix != null) {
                return CmsStringUtil.joinPaths(rfsPrefix, exportName, sitePath);
            }
        }
        return CmsCoreProvider.get().link(sitePath);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getDragHelper(I_CmsDropTarget)
     */
    @Override
    public Element getDragHelper(I_CmsDropTarget target) {

        Element helper = super.getDragHelper(target);
        // ensure the proper CSS context for the drag helper
        m_provisionalParent.addClassName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().navMode());
        return helper;
    }

    /**
     * Returns the entry id.<p>
     * 
     * @return the entry id
     */
    public CmsUUID getEntryId() {

        return m_entryId;
    }

    /**
     * @see org.opencms.gwt.client.ui.tree.CmsTreeItem#getPath()
     */
    @Override
    public String getPath() {

        String result = getSitePath();
        // ensure that the path of a folder ends with a '/'
        if (getSitemapEntry().isFolderType() && !result.endsWith("/")) {
            result += "/";
        }
        return result;
    }

    /**
     * Returns the sitemap entry.<p>
     * 
     * @return the sitemap entry
     */
    public CmsClientSitemapEntry getSitemapEntry() {

        return CmsSitemapView.getInstance().getController().getEntryById(m_entryId);
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return getSitemapEntry().getSitePath();
    }

    /**
     * Turns the highlighting for this item on or off.<p>
     * 
     * @param highlightOn if true, the highlighting is turned on, else off
     */
    public void highlight(boolean highlightOn) {

        if (highlightOn) {
            setBackgroundColor(Background.YELLOW);
        } else {
            setBackgroundColor(Background.DEFAULT);
        }
    }

    /**
     * Temporarily highlights an item.<p>
     * 
     * @param duration the duration for which  
     * @param background the background to color to set when finished
     */
    public void highlightTemporarily(int duration, final Background background) {

        int blinkInterval = 300;
        final int blinkCount = duration / blinkInterval;

        Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {

            private int m_counter;

            /**
             * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
             */
            public boolean execute() {

                boolean finish = m_counter > blinkCount;
                highlight(((m_counter % 2) == 0) && !finish);
                m_counter += 1;
                if (finish) {
                    setBackgroundColor(background);
                }
                return !finish;
            }
        }, blinkInterval);
    }

    /**
     * @see org.opencms.gwt.client.ui.tree.CmsTreeItem#isDropEnabled()
     */
    @Override
    public boolean isDropEnabled() {

        return getSitemapEntry().isInNavigation() && getSitemapEntry().isFolderType() && super.isDropEnabled();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    @Override
    public void onDragCancel() {

        removeStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().positionIndicator());
        super.onDragCancel();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDrop(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    @Override
    public void onDrop(I_CmsDropTarget target) {

        removeStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().positionIndicator());
        super.onDrop(target);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#onStartDrag(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    @Override
    public void onStartDrag(I_CmsDropTarget target) {

        setOpen(false);
        // transform the widget into a position indicator
        addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().positionIndicator());
        CmsSitemapHoverbar hoverbar = getHoverbar();
        if (hoverbar != null) {
            hoverbar.hide();
        }
    }

    /**
     * Resets entry appearance.<p>
     */
    public void resetEntry() {

        updateEntry(getSitemapEntry());
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
     * Sets the icon.<p>
     *
     * @param icon the icon to set
     */
    public void setStateIcon(StateIcon icon) {

        getListItemWidget().setStateIcon(icon);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#toString()
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(getSitemapEntry().getSitePath()).append("\n");
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
     * Updates the detail page description.<p>
     */
    public void updateDetailPageStatus() {

        CmsDetailPageTable detailPageTable = CmsSitemapView.getInstance().getController().getDetailPageTable();
        String type;
        String text = null;
        String suffixTitle = null;
        switch (detailPageTable.getStatus(m_entryId)) {
            case firstDetailPage:
                type = detailPageTable.get(m_entryId).getDisplayType();
                suffixTitle = Messages.get().key(Messages.GUI_MAIN_DETAIL_PAGE_TITLE_1, type);
                text = "(*" + type + ")";
                break;
            case otherDetailPage:
                type = detailPageTable.get(m_entryId).getDisplayType();
                suffixTitle = Messages.get().key(Messages.GUI_DETAIL_PAGE_TITLE_1, type);
                text = "(" + type + ")";
                break;
            case noDetailPage:
            default:
        }
        m_detailPageLabelTitleGenerator.setDetailPageTitle(suffixTitle);
        getListItemWidget().updateTruncation();
        CmsLabel label = getListItemWidget().getSubTitleSuffix();
        label.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().subtitleSuffix());
        getListItemWidget().setSubtitleSuffixText(text);
    }

    /**
     * Updates the sitemap editor mode.<p>
     */
    public void updateEditorMode() {

        getListItemWidget().setIcon(CmsSitemapView.getInstance().getIconForEntry(getSitemapEntry()));
        for (Widget child : m_children) {
            if (child instanceof CmsSitemapTreeItem) {
                ((CmsSitemapTreeItem)child).updateEditorMode();
            }
        }
    }

    /**
     * Refreshes the displayed data from the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        getListItemWidget().setTitleLabel(entry.getTitle());
        getListItemWidget().reInitAdditionalInfo(getInfoBean(entry));
        updateSitePath(entry.getSitePath());
        updateDetailPageStatus();
        updateLock(entry);
        updateInNavigation(entry);
        getListItemWidget().setIcon(CmsSitemapView.getInstance().getIconForEntry(entry));
        setExpiredStyle(entry, getListItemWidget());
        setDropEnabled(getSitemapEntry().isFolderType() && !getSitemapEntry().hasForeignFolderLock());
        if (entry.isSubSitemapType() || entry.isLeafType()) {
            hideOpeners();
        } else {
            showOpeners();
        }
        getListItemWidget().updateTruncation();
    }

    /**
     * Updates the in navigation properties of the displayed entry.<p>
     * 
     * @param entry the sitemap entry 
     */
    public void updateInNavigation(CmsClientSitemapEntry entry) {

        if (entry.isInNavigation()) {
            m_inNavigationStyle.setValue(null);
            getListItemWidget().setTitleEditable(true);
        } else {
            m_inNavigationStyle.setValue(CSS.notInNavigationEntry());
            getListItemWidget().setTitleEditable(false);
        }
    }

    /**
     * Updates the site path using the current site entry's data.<p>
     */
    public void updateSitePath() {

        updateSitePath(getSitemapEntry().getSitePath());
    }

    /**
     * Updates the recursively the site path.<p>
     * 
     * @param sitePath the new site path to set
     */
    public void updateSitePath(String sitePath) {

        String newSubTitle = getDisplayedUrl(sitePath);

        getListItemWidget().setSubtitleLabel(newSubTitle);
        String name = getName(sitePath);
        setId(name);
        getListItemWidget().setAdditionalInfoValue(1, name);
        if (getLoadState() == LoadState.LOADED) {
            for (int i = 0; i < getChildCount(); i++) {
                CmsSitemapTreeItem item = (CmsSitemapTreeItem)getChild(i);
                if (item.getSitemapEntry() != null) {
                    String path = CmsStringUtil.joinPaths(sitePath, CmsResource.getName(item.getSitePath()));
                    item.updateSitePath(path);
                }
            }
        }
        getListItemWidget().updateTruncation();

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
        getListItemWidget().addButton(label);
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

    /**
     * Updates the lock icon according to the entry information.<p>
     * 
     * @param entry the entry
     */
    private void updateLock(CmsClientSitemapEntry entry) {

        LockIcon icon = LockIcon.NONE;
        String iconTitle = null;
        if (entry.hasBlockingLockedChildren()) {
            icon = LockIcon.CLOSED;
            iconTitle = Messages.get().key(Messages.GUI_BLOCKING_LOCKED_CHILDREN_0);
        }
        if (!entry.getLock().isOwnedByUser()) {
            switch (entry.getLock().getLockType()) {
                case EXCLUSIVE:
                case INHERITED:
                case TEMPORARY:
                    icon = LockIcon.CLOSED;
                    break;
                case SHARED_EXCLUSIVE:
                case SHARED_INHERITED:
                    icon = LockIcon.SHARED_CLOSED;
                    break;
                default:
            }
        } else {
            switch (entry.getLock().getLockType()) {
                case EXCLUSIVE:
                case INHERITED:
                case TEMPORARY:
                    icon = LockIcon.OPEN;
                    break;
                case SHARED_EXCLUSIVE:
                case SHARED_INHERITED:
                    icon = LockIcon.SHARED_OPEN;
                    break;
                default:
            }
        }
        if (entry.getLock().getLockOwner() != null) {
            iconTitle = org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_LOCK_OWNED_BY_1,
                entry.getLock().getLockOwner());
        }

        getListItemWidget().setLockIcon(icon, iconTitle);
    }
}
