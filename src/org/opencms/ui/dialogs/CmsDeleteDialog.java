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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsGwtContextMenuButton;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.shared.rpc.I_CmsGwtContextMenuServerRpc;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.commons.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog for deleting resources.<p>
 */
public class CmsDeleteDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsDeleteDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Box for displaying resource widgets. */
    private VerticalLayout m_resourceBox;

    // private AbstractComponent m_container;

    /** Message if deleting resources is allowed or not. */
    private Label m_deleteResource;

    /** Label for the links. */
    private Label m_linksLabel;

    /** The OK button. */
    private Button m_okButton;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The delete siblings check box group. */
    private OptionGroup m_deleteSiblings;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsDeleteDialog(I_CmsDialogContext context) {

        m_context = context;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_deleteSiblings.addItem(CmsResource.DELETE_PRESERVE_SIBLINGS);
        m_deleteSiblings.setItemCaption(
            CmsResource.DELETE_PRESERVE_SIBLINGS,
            CmsVaadinUtils.getMessageText(Messages.GUI_DELETE_PRESERVE_SIBLINGS_0));
        m_deleteSiblings.addItem(CmsResource.DELETE_REMOVE_SIBLINGS);
        m_deleteSiblings.setItemCaption(
            CmsResource.DELETE_REMOVE_SIBLINGS,
            CmsVaadinUtils.getMessageText(Messages.GUI_DELETE_ALL_SIBLINGS_0));
        m_deleteSiblings.setValue(CmsResource.DELETE_PRESERVE_SIBLINGS);
        m_deleteSiblings.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                displayBrokenLinks();
            }
        });
        m_deleteSiblings.setVisible(hasSiblings());
        displayResourceInfo(m_context.getResources());

        m_cancelButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(Button.ClickEvent event) {

                cancel();
            }
        });

        m_okButton.addStyleName(OpenCmsTheme.BUTTON_RED);

        m_okButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(Button.ClickEvent event) {

                submit();
            }
        });

        displayBrokenLinks();

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsDeleteDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Gets the broken links.<p>
     *
     * @param cms the CMS context
     * @param selectedResources the selected resources
     * @param includeSiblings <code>true</code> if siblings would be deleted too
     *
     * @return multimap of broken links, with sources as keys and targets as values
     *
     * @throws CmsException if something goes wrong
     */
    public static Multimap<CmsResource, CmsResource> getBrokenLinks(
        CmsObject cms,
        List<CmsResource> selectedResources,
        boolean includeSiblings)
    throws CmsException {

        return getBrokenLinks(cms, selectedResources, includeSiblings, false);

    }

    /**
     * Gets the broken links.<p>
     *
     * @param cms the CMS context
     * @param selectedResources the selected resources
     * @param includeSiblings <code>true</code> if siblings would be deleted too
     * @param reverse <code>true</code> if the resulting map should be reverted
     *
     * @return multimap of broken links, with sources as keys and targets as values
     *
     * @throws CmsException if something goes wrong
     */
    public static Multimap<CmsResource, CmsResource> getBrokenLinks(
        CmsObject cms,
        List<CmsResource> selectedResources,
        boolean includeSiblings,
        boolean reverse)
    throws CmsException {

        Set<CmsResource> descendants = new HashSet<CmsResource>();
        for (CmsResource root : selectedResources) {
            descendants.add(root);
            if (root.isFolder()) {
                descendants.addAll(cms.readResources(cms.getSitePath(root), CmsResourceFilter.IGNORE_EXPIRATION));
            }
        }

        if (includeSiblings) {
            // add siblings
            for (CmsResource res : new HashSet<CmsResource>(descendants)) {
                if (res.isFile()) {
                    descendants.addAll(cms.readSiblings(res, CmsResourceFilter.IGNORE_EXPIRATION));
                }
            }
        }
        HashSet<CmsUUID> deleteIds = new HashSet<CmsUUID>();
        for (CmsResource deleteRes : descendants) {
            deleteIds.add(deleteRes.getStructureId());
        }
        Multimap<CmsResource, CmsResource> linkMap = HashMultimap.create();
        for (CmsResource resource : descendants) {
            List<CmsRelation> relations = cms.getRelationsForResource(resource, CmsRelationFilter.SOURCES);
            List<CmsResource> result1 = new ArrayList<CmsResource>();
            for (CmsRelation relation : relations) {
                // only add related resources that are not going to be deleted
                if (!deleteIds.contains(relation.getSourceId())) {
                    try {
                        CmsResource source1 = relation.getSource(cms, CmsResourceFilter.ALL);
                        if (!source1.getState().isDeleted()) {
                            result1.add(source1);
                        }
                    } catch (Exception e) {
                        LOG.warn(
                            "Couldn't find relation source while checking the following relation: "
                                + relation.toString(),
                            e);

                    }
                }
            }
            List<CmsResource> linkSources = result1;
            for (CmsResource source : linkSources) {
                if (reverse) {
                    linkMap.put(resource, source);
                } else {
                    linkMap.put(source, resource);
                }
            }
        }
        return linkMap;

    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(new ArrayList<CmsUUID>());
    }

    /**
     * Displays the broken links.<p>
     */
    void displayBrokenLinks() {

        I_CmsGwtContextMenuServerRpc rpc = new I_CmsGwtContextMenuServerRpc() {

            public void refresh(String id) {

                if (id != null) {
                    m_context.finish(Arrays.asList(new CmsUUID(id)));
                } else {
                    m_context.finish(Collections.emptyList());
                }
            }
        };
        CmsObject cms = A_CmsUI.getCmsObject();
        m_resourceBox.removeAllComponents();
        m_resourceBox.addStyleName("o-broken-links");
        m_deleteResource.setVisible(false);
        m_okButton.setVisible(true);
        boolean canIgnoreBrokenLinks = OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
            || OpenCms.getRoleManager().hasRole(cms, CmsRole.VFS_MANAGER);
        try {
            Multimap<CmsResource, CmsResource> brokenLinks = getBrokenLinks(
                cms,
                m_context.getResources(),
                CmsResource.DELETE_REMOVE_SIBLINGS.equals(m_deleteSiblings.getValue()));
            if (brokenLinks.isEmpty()) {
                m_linksLabel.setVisible(false);
                String noLinksBroken = CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.commons.Messages.GUI_DELETE_RELATIONS_NOT_BROKEN_0);
                m_resourceBox.addComponent(new Label(noLinksBroken));
            } else {
                if (!canIgnoreBrokenLinks) {
                    m_deleteResource.setVisible(true);
                    m_deleteResource.setValue(
                        CmsVaadinUtils.getMessageText(
                            org.opencms.workplace.commons.Messages.GUI_DELETE_RELATIONS_NOT_ALLOWED_0));
                    m_okButton.setVisible(false);
                }
                for (CmsResource source : brokenLinks.keySet()) {
                    CmsResourceInfo parentInfo = new CmsResourceInfo(source);
                    CmsGwtContextMenuButton contextMenu = new CmsGwtContextMenuButton(source.getStructureId(), rpc);
                    contextMenu.addStyleName("o-gwt-contextmenu-button-margin");
                    parentInfo.setButtonWidget(contextMenu);
                    m_resourceBox.addComponent(parentInfo);
                    for (CmsResource target : brokenLinks.get(source)) {
                        CmsResourceInfo childInfo = new CmsResourceInfo(target);
                        childInfo.addStyleName("o-deleted");
                        m_resourceBox.addComponent(indent(childInfo));
                    }

                }
            }
        } catch (CmsException e) {
            m_context.error(e);
            return;
        }
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            List<CmsUUID> changedIds = Lists.newArrayList();
            CmsResourceDeleteMode mode = (CmsResourceDeleteMode)m_deleteSiblings.getValue();
            for (CmsResource resource : m_context.getResources()) {
                if (resource.getState().isDeleted()) {
                    continue;
                }
                changedIds.add(resource.getStructureId());
                CmsLockActionRecord lockRecord = CmsLockUtil.ensureLock(cms, resource);
                try {
                    cms.deleteResource(cms.getSitePath(resource), mode);
                } finally {
                    if (lockRecord.getChange().equals(LockChange.locked)) {
                        if (!resource.getState().isNew()) {
                            try {
                                cms.unlockResource(resource);
                            } catch (CmsVfsResourceNotFoundException e) {
                                LOG.warn(e.getLocalizedMessage(), e);
                            } catch (CmsLockException e) {
                                LOG.warn(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
            }
            m_context.finish(changedIds);
        } catch (Exception e) {
            m_context.error(e);
        }
    }

    /**
     * Checks whether the selected resources have siblings.<p>
     *
     * @return whether the selected resources have siblings
     */
    private boolean hasSiblings() {

        for (CmsResource res : m_context.getResources()) {
            if (res.getSiblingCount() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indents a resources box.<p>
     *
     * @param resourceInfo the resource box
     *
     * @return an indented resource box
     */
    private Component indent(CmsResourceInfo resourceInfo) {

        boolean simple = false;

        if (simple) {
            return resourceInfo;

        } else {
            HorizontalLayout hl = new HorizontalLayout();
            Label label = new Label("");
            label.setWidth("35px");
            hl.addComponent(label);
            hl.addComponent(resourceInfo);
            hl.setExpandRatio(resourceInfo, 1.0f);
            hl.setWidth("100%");
            return hl;
        }
    }

}
