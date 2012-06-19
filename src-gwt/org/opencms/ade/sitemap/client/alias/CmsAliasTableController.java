/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.alias;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationReply;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasInitialFetchResult;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.Window;

/**
 * This is the controller class for the alias editor which is responsible for reacting to edit operations on the client 
 * by triggering the corresponding validation or save actions on the server.<p>
 */
public class CmsAliasTableController {

    /** The set of ids of deleted rows. */
    protected Set<CmsUUID> m_deletedIds = new HashSet<CmsUUID>();

    /** The view. */
    protected CmsAliasView m_view;

    /** The URL from which the list of aliases can be downloaded. */
    String m_downloadUrl;

    /** The initial data loaded from the server. */
    List<CmsAliasTableRow> m_initialData;

    /** The site root. */
    String m_siteRoot;

    /**
     * This method is called when the user deletes a set of rows.<p>
     * 
     * @param rowsToDelete the list of rows which should be deleted 
     */
    public void deleteRows(List<CmsAliasTableRow> rowsToDelete) {

        List<CmsAliasTableRow> liveData = m_view.getLiveData();
        for (CmsAliasTableRow row : liveData) {
            CmsUUID structureId = row.getStructureId();
            if (structureId != null) {
                m_deletedIds.add(row.getStructureId());
            }
        }
        liveData.removeAll(rowsToDelete);
    }

    /**
     * Triggers the download of the current aliases.<p>
     */
    public void download() {

        Window.open(m_downloadUrl + "?site=" + m_siteRoot, "_blank", "");
    }

    /**
     * This method is called after the mode of an alias has been edited.<p>
     * 
     * @param row the edited row
     * @param mode the new alias mode 
     */
    public void editAliasMode(CmsAliasTableRow row, CmsAliasMode mode) {

        row.setMode(mode);
        row.setEdited(true);
    }

    /**
     * This method is called after the alias path of an alias has been edited.<p>
     * 
     * @param row the edited row
     * @param path the new alias path 
     */
    public void editAliasPath(CmsAliasTableRow row, String path) {

        row.editAliasPath(path);
        row.setEdited(true);
        validate();

    }

    /**
     * This method is called when the user wants to add a new alias entry.<p>
     * 
     * @param aliasPath the alias path
     * @param resourcePath the resource site path 
     * @param mode the alias mode 
     */
    public void editNewAlias(String aliasPath, String resourcePath, CmsAliasMode mode) {

        CmsAliasTableRow row = new CmsAliasTableRow();
        row.setEdited(true);
        row.setAliasPath(aliasPath);
        row.setResourcePath(resourcePath);
        row.setMode(mode);
        validateNew(row);
    }

    /**
     * This method is called when the user has edited the resource path of an alias.<p>
     * 
     * @param row the alias the table row 
     * @param path the new path 
     */
    public void editResourcePath(CmsAliasTableRow row, String path) {

        row.setEdited(true);
        row.editResourcePath(path);
        validate();
    }

    /**
     * Loads the initial data from the server.<p>
     * 
     * @param afterLoad the action that should be executed after loading 
     */
    public void load(final Runnable afterLoad) {

        CmsRpcAction<CmsAliasInitialFetchResult> action = new CmsRpcAction<CmsAliasInitialFetchResult>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                CmsSitemapView.getInstance().getController().getService().getAliasTable(this);
                start(200, false);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            public void onResponse(CmsAliasInitialFetchResult aliasTable) {

                stop(false);
                m_downloadUrl = aliasTable.getDownloadUrl();
                m_initialData = aliasTable.getRows();
                m_siteRoot = CmsCoreProvider.get().getSiteRoot();
                List<CmsAliasTableRow> copiedData = copyData(m_initialData);
                m_view.setData(copiedData);
                if (afterLoad != null) {
                    afterLoad.run();
                }
            }
        };
        action.execute();
    }

    /**
     * This method is called when the user wants to save the aliases.<p>
     */
    public void save() {

        CmsRpcAction<CmsAliasEditValidationReply> action = new CmsRpcAction<CmsAliasEditValidationReply>() {

            @Override
            public void execute() {

                start(200, false);

                CmsAliasSaveValidationRequest saveRequest = new CmsAliasSaveValidationRequest();
                saveRequest.setSiteRoot(m_siteRoot);
                List<CmsAliasTableRow> rows = m_view.getLiveData();
                saveRequest.setEditedData(rows);
                saveRequest.getDeletedIds().addAll(m_deletedIds);
                saveRequest.setOriginalData(m_initialData);
                CmsSitemapView.getInstance().getController().getService().saveAliases(saveRequest, this);
            }

            @Override
            public void onResponse(CmsAliasEditValidationReply result) {

                stop(false);
                if (result == null) {
                    m_view.close();
                } else if (result.hasErrors()) {
                    m_view.update(result.getChangedRows());
                }
            }
        };
        action.execute();
    }

    /**
     * Sets the alias editor view.<p>
     * 
     * @param view the view object 
     */
    public void setView(CmsAliasView view) {

        m_view = view;
    }

    /**
     * Triggers server-side validatiom of the alias table.<p>
     */
    protected void validate() {

        CmsRpcAction<CmsAliasEditValidationReply> action = new CmsRpcAction<CmsAliasEditValidationReply>() {

            @Override
            public void execute() {

                CmsAliasEditValidationRequest validationRequest = new CmsAliasEditValidationRequest();

                List<CmsAliasTableRow> rows = m_view.getLiveData();
                validationRequest.setEditedData(rows);
                validationRequest.setOriginalData(m_initialData);
                CmsSitemapView.getInstance().getController().getService().validateAliases(validationRequest, this);
            }

            @Override
            public void onResponse(CmsAliasEditValidationReply result) {

                stop(false);
                List<CmsAliasTableRow> changedRows = result.getChangedRows();
                m_view.update(changedRows);

            }

        };
        action.execute();
    }

    /**
     * Triggers server-side validation of the alias table and of a new entry which should be added to it.<p>
     * 
     * @param newEntry the new entry 
     */
    protected void validateNew(final CmsAliasTableRow newEntry) {

        CmsRpcAction<CmsAliasEditValidationReply> action = new CmsRpcAction<CmsAliasEditValidationReply>() {

            @Override
            public void execute() {

                start(200, false);
                CmsAliasEditValidationRequest validationRequest = new CmsAliasEditValidationRequest();
                List<CmsAliasTableRow> rows = m_view.getLiveData();
                validationRequest.setEditedData(rows);
                validationRequest.setNewEntry(newEntry);
                validationRequest.setOriginalData(m_initialData);
                CmsSitemapView.getInstance().getController().getService().validateAliases(validationRequest, this);
            }

            @Override
            public void onResponse(CmsAliasEditValidationReply result) {

                stop(false);
                List<CmsAliasTableRow> tableRows = result.getChangedRows();
                CmsAliasTableRow validatedNewEntry = result.getValidatedNewEntry();
                if (validatedNewEntry.hasErrors()) {
                    m_view.setNewAliasPathError(validatedNewEntry.getAliasError());
                    m_view.setNewAliasResourceError(validatedNewEntry.getPathError());
                } else {
                    m_view.clearNew();
                    tableRows.add(validatedNewEntry);
                }
                m_view.update(tableRows);
            }
        };
        action.execute();
    }

    /**
     * Copies a list of rows.<p>
     * 
     * @param data the original data 
     * 
     * @return the copied data 
     */
    List<CmsAliasTableRow> copyData(List<CmsAliasTableRow> data) {

        List<CmsAliasTableRow> result = new ArrayList<CmsAliasTableRow>();
        for (CmsAliasTableRow row : data) {
            CmsAliasTableRow copiedRow = row.copy();
            result.add(copiedRow);
        }
        return result;
    }

}
