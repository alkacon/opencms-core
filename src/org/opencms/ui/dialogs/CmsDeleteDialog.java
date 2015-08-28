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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CmsDeleteDialog extends CmsBasicDialog {

    private VerticalLayout m_resourceBox;
    private Label m_linksLabel;
    private Button m_okButton;
    private Button m_cancelButton;

    public CmsDeleteDialog(final I_CmsDialogContext context) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        List<CmsResource> resources = context.getResources();
        boolean showDeleteSiblings = false;
        displayResourceInfo(context.getResources());

        final CmsObject cms = A_CmsUI.getCmsObject();

        m_cancelButton.addClickListener(new ClickListener() {

            public void buttonClick(Button.ClickEvent event) {

                context.finish(new ArrayList<CmsUUID>());
            }
        });

        m_okButton.addClickListener(new ClickListener() {

            public void buttonClick(Button.ClickEvent event) {

                try {
                    List<CmsUUID> changedIds = Lists.newArrayList();
                    for (CmsResource resource : context.getResources()) {
                        changedIds.add(resource.getStructureId());
                        CmsLockActionRecord lockRecord = CmsLockUtil.ensureLock(cms, resource);
                        try {
                            cms.deleteResource(cms.getSitePath(resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
                        } finally {
                            if (lockRecord.getChange().equals(LockChange.locked)) {
                                try {
                                    cms.unlockResource(resource);
                                } catch (CmsVfsResourceNotFoundException e) {
                                    System.out.println(e);
                                }
                            }
                        }
                    }
                    context.finish(changedIds);
                } catch (Exception e) {
                    context.error(e);
                }

            }
        });

        boolean canIgnoreBrokenLinks = OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
            || OpenCms.getRoleManager().hasRole(cms, CmsRole.VFS_MANAGER);
        try {
            List<CmsResource> brokenLinkSources = getBrokenLinkSources(cms, context.getResources());
            if (brokenLinkSources.isEmpty()) {
                m_linksLabel.setVisible(false);
                m_resourceBox.addComponent(new Label("*No links will be broken!"));
            } else {
                if (!canIgnoreBrokenLinks) {
                    m_okButton.setVisible(false);
                }
                for (CmsResource source : brokenLinkSources) {
                    m_resourceBox.addComponent(new CmsResourceInfo(source));
                }
            }
        } catch (CmsException e) {
            context.error(e);
            return;
        }

    }

    public List<CmsResource> getBrokenLinkSources(CmsObject cms, List<CmsResource> selectedResources)
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

        List<CmsResource> result = Lists.newArrayList();
        for (CmsResource key : linkMap.keySet()) {
            result.add(key);
        }
        return result;
    }

}
