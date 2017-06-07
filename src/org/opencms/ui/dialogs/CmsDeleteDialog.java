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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
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
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

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
    
    /** Message if deleting resources is allowed or not */
    private Label m_deleteResource;
    
    /** Label for the links. */
    private Label m_linksLabel;

    /** The OK button. */
    private Button m_okButton;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsDeleteDialog(I_CmsDialogContext context) {
        m_context = context;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        displayResourceInfo(m_context.getResources());

        m_cancelButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(Button.ClickEvent event) {

                cancel();
            }
        });

        m_okButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(Button.ClickEvent event) {

                submit();
            }
        });

        CmsObject cms = A_CmsUI.getCmsObject();
        boolean canIgnoreBrokenLinks = OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
            || OpenCms.getRoleManager().hasRole(cms, CmsRole.VFS_MANAGER);
        try {
            Multimap<CmsResource, CmsResource> brokenLinks = getBrokenLinks(cms, m_context.getResources());
            m_deleteResource.setValue(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_DELETE_MULTI_CONFIRMATION_0));
            if (brokenLinks.isEmpty()) {
                m_linksLabel.setVisible(false);
                String noLinksBroken = CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.commons.Messages.GUI_DELETE_RELATIONS_NOT_BROKEN_0);
                m_resourceBox.addComponent(new Label(noLinksBroken));
            } else {
                if (!canIgnoreBrokenLinks) {
                    m_deleteResource.setValue(
                        CmsVaadinUtils.getMessageText(
                            org.opencms.workplace.commons.Messages.GUI_DELETE_RELATIONS_NOT_ALLOWED_0));
                    m_okButton.setVisible(false);
                }
                for (CmsResource source : brokenLinks.keySet()) {
                    m_resourceBox.addComponent(new CmsResourceInfo(source));
                    for (CmsResource target : brokenLinks.get(source)) {
                        m_resourceBox.addComponent(indent(new CmsResourceInfo(target)));
                    }

                }
            }
        } catch (CmsException e) {
            m_context.error(e);
            return;
        }

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
     * @return multimap of broken links, with sources as keys and targets as values
     *
     * @throws CmsException if something goes wrong
     */
    public Multimap<CmsResource, CmsResource> getBrokenLinks(CmsObject cms, List<CmsResource> selectedResources)
    throws CmsException {

        Set<CmsResource> descendants = Sets.newHashSet();
        for (CmsResource root : selectedResources) {
            descendants.add(root);
            if (root.isFolder()) {
                descendants.addAll(cms.readResources(cms.getSitePath(root), CmsResourceFilter.IGNORE_EXPIRATION));
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
                    CmsResource source1 = relation.getSource(cms, CmsResourceFilter.ALL);
                    if (!source1.getState().isDeleted()) {
                        result1.add(source1);
                    }
                }
            }
            List<CmsResource> linkSources = result1;
            for (CmsResource source : linkSources) {
                linkMap.put(source, resource);
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
     * Submits the dialog.<p>
     */
    void submit() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            List<CmsUUID> changedIds = Lists.newArrayList();
            for (CmsResource resource : m_context.getResources()) {
                changedIds.add(resource.getStructureId());
                CmsLockActionRecord lockRecord = CmsLockUtil.ensureLock(cms, resource);
                try {
                    cms.deleteResource(cms.getSitePath(resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
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
