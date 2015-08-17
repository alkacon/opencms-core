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

package org.opencms.ade.sitemap;

import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.PARAM_IMPORTFILE;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.PARAM_SEPARATOR;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.PARAM_SITEROOT;

import org.opencms.db.CmsAlias;
import org.opencms.db.CmsAliasManager;
import org.opencms.db.CmsRewriteAlias;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationReply;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasImportResult;
import org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.collect.Sets;

/**
 * Helper class used by a service to edit or import aliases for a whole site.<p>
 */
public class CmsAliasBulkEditHelper {

    /** The current CMS context. */
    private CmsObject m_cms;

    /** Flag to indicate whether the validation was successful. */
    private boolean m_hasErrors;

    /**
     * Creates a new helper object.<p>
     *
     * @param cms the current CMS context
     */
    public CmsAliasBulkEditHelper(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Imports uploaded aliases from a request.<p>
     *
     * @param request the request containing the uploaded aliases
     * @param response the response
     * @throws Exception if something goes wrong
     */
    public void importAliases(HttpServletRequest request, HttpServletResponse response) throws Exception {

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(request);
        byte[] data = null;
        String siteRoot = null;
        String separator = ",";
        for (FileItem fileItem : items) {
            String name = fileItem.getFieldName();
            if (PARAM_IMPORTFILE.equals(name)) {
                data = fileItem.get();
            } else if (PARAM_SITEROOT.equals(name)) {
                siteRoot = new String(fileItem.get(), request.getCharacterEncoding());
            } else if (PARAM_SEPARATOR.equals(name)) {
                separator = new String(fileItem.get(), request.getCharacterEncoding());
            }
        }
        List<CmsAliasImportResult> result = new ArrayList<CmsAliasImportResult>();
        if ((siteRoot != null) && (data != null)) {
            result = OpenCms.getAliasManager().importAliases(m_cms, data, siteRoot, separator);
        }
        String key = CmsVfsSitemapService.addAliasImportResult(result);
        // only respond with a key, then the client can get the data for the key via GWT-RPC
        response.getWriter().print(key);
    }

    /**
     * Saves alias changes to the database.<p>
     *
     * @param saveRequest an object containing the alias changes to save
     *
     * @return a validation error if the alias data is invalid, or null otherwise
     *
     * @throws CmsException if something goes wrong
     */
    public CmsAliasEditValidationReply saveAliases(CmsAliasSaveValidationRequest saveRequest) throws CmsException {

        CmsAliasEditValidationReply reply = validateAliases(saveRequest);
        if (m_hasErrors) {
            return reply;
        } else {
            List<CmsRewriteAliasTableRow> rewriteData = saveRequest.getRewriteData();
            OpenCms.getAliasManager().saveRewriteAliases(
                m_cms,
                m_cms.getRequestContext().getSiteRoot(),
                convertRewriteData(rewriteData));
            Set<CmsUUID> allTouchedIds = new HashSet<CmsUUID>();
            List<CmsAliasTableRow> rows = saveRequest.getEditedData();
            for (CmsAliasTableRow row : rows) {
                if (row.isEdited()) {
                    allTouchedIds.add(row.getStructureId());
                    if (row.getOriginalStructureId() != null) {
                        allTouchedIds.add(row.getOriginalStructureId());
                    }
                }
            }
            allTouchedIds.addAll(saveRequest.getDeletedIds());
            CmsAliasManager aliasManager = OpenCms.getAliasManager();
            String siteRoot = saveRequest.getSiteRoot();
            List<CmsAlias> aliases = aliasManager.getAliasesForSite(m_cms, siteRoot);
            Set<CmsAlias> aliasSet = new HashSet<CmsAlias>();
            Set<CmsAlias> editedAliasSet = new HashSet<CmsAlias>();
            aliasSet.addAll(aliases);
            for (CmsAliasTableRow row : rows) {
                CmsAlias editedAlias = new CmsAlias(row.getStructureId(), siteRoot, row.getAliasPath(), row.getMode());
                editedAliasSet.add(editedAlias);
            }
            Set<CmsAlias> toDelete = Sets.difference(aliasSet, editedAliasSet);
            toDelete = filterStructureId(toDelete, allTouchedIds);
            Set<CmsAlias> toAdd = Sets.difference(editedAliasSet, aliasSet);
            toAdd = filterStructureId(toAdd, allTouchedIds);

            aliasManager.updateAliases(m_cms, toDelete, toAdd);
            return null;
        }
    }

    /**
     * Validates the alias data.<p>
     *
     * @param validationRequest an object containing the alias data to validate
     *
     * @return the validation result
     */
    public CmsAliasEditValidationReply validateAliases(CmsAliasEditValidationRequest validationRequest) {

        List<CmsAliasTableRow> editedData = validationRequest.getEditedData();
        CmsAliasTableRow newEntry = validationRequest.getNewEntry();
        if (newEntry != null) {
            newEntry.setKey((new CmsUUID()).toString());
            editedData.add(newEntry);

        }
        CmsObject cms = m_cms;
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        for (CmsAliasTableRow row : editedData) {
            row.clearErrors();
            validateSingleAliasRow(cms, row);
        }
        Set<String> foundAliasPaths = new HashSet<String>();
        Set<String> duplicateAliasPaths = new HashSet<String>();
        for (CmsAliasTableRow row : editedData) {
            String aliasPath = row.getAliasPath();
            if (foundAliasPaths.contains(aliasPath)) {
                duplicateAliasPaths.add(aliasPath);
            }
            foundAliasPaths.add(aliasPath);
        }
        for (CmsAliasTableRow row : editedData) {
            if (duplicateAliasPaths.contains(row.getAliasPath())) {
                if (row.getPathError() == null) {
                    row.setAliasError(messageDuplicateAliasPath(locale));
                    m_hasErrors = true;
                }
            }
        }
        CmsAliasEditValidationReply result = new CmsAliasEditValidationReply();
        List<CmsAliasTableRow> changedRows = new ArrayList<CmsAliasTableRow>();
        for (CmsAliasTableRow row : editedData) {
            if (row.isChanged()) {
                changedRows.add(row);
            }
        }
        if (newEntry != null) {
            changedRows.remove(newEntry);
        }
        result.setChangedRows(changedRows);
        result.setValidatedNewEntry(newEntry);
        return result;
    }

    /**
     * Filters all aliases from a set whose structure id is in a given set of structure ids.<p>
     *
     * @param aliases the aliases to filter
     * @param structureIds the structure ids for which we want the aliases
     *
     * @return the filtered structure ids
     */
    protected Set<CmsAlias> filterStructureId(Set<CmsAlias> aliases, Set<CmsUUID> structureIds) {

        Set<CmsAlias> result = new HashSet<CmsAlias>();
        for (CmsAlias alias : aliases) {
            if (structureIds.contains(alias.getStructureId())) {
                result.add(alias);
            }
        }
        return result;
    }

    /**
     * Converts rewrite alias table rows to rewrite alias objects.<p>
     *
     * @param rewriteData the rewrite data
     *
     * @return the converted rewrite aliases
     */
    private List<CmsRewriteAlias> convertRewriteData(List<CmsRewriteAliasTableRow> rewriteData) {

        String siteRoot = m_cms.getRequestContext().getSiteRoot();
        List<CmsRewriteAlias> result = new ArrayList<CmsRewriteAlias>();
        for (CmsRewriteAliasTableRow row : rewriteData) {
            CmsRewriteAlias alias = new CmsRewriteAlias(
                row.getId(),
                siteRoot,
                row.getPatternString(),
                row.getReplacementString(),
                row.getMode());
            result.add(alias);
        }
        return result;
    }

    /**
     * Message accessor.
     *
     * @param locale the locale for messages
     *
     * @return the message string
     */
    private String messageDuplicateAliasPath(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_DUPLICATE_ALIAS_PATH_0);
    }

    /**
     * Message accessor.
     *
     * @param locale the locale for messages
     *
     * @return the message string
     */
    private String messageInvalidAliasPath(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_INVALID_ALIAS_PATH_0);
    }

    /**
     * Message accessor.
     *
     * @param locale the locale for messages
     *
     * @return the message string
     */
    private String messageResourceNotFound(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_RESOURCE_NOT_FOUND_0);

    }

    /**
     * Validates a single alias row.<p>
     *
     * @param cms the current CMS context
     * @param row the row to validate
     */
    private void validateSingleAliasRow(CmsObject cms, CmsAliasTableRow row) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        if (row.getStructureId() == null) {
            String path = row.getResourcePath();
            try {
                CmsResource resource = cms.readResource(path, CmsResourceFilter.ALL);
                row.setStructureId(resource.getStructureId());
                if (row.getOriginalStructureId() == null) {
                    row.setOriginalStructureId(resource.getStructureId());
                }
            } catch (CmsException e) {
                row.setPathError(messageResourceNotFound(locale));
                m_hasErrors = true;
            }
        }
        if (!CmsAlias.ALIAS_PATTERN.matcher(row.getAliasPath()).matches()) {
            row.setAliasError(messageInvalidAliasPath(locale));
            m_hasErrors = true;
        }
    }
}
