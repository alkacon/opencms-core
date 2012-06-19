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

import org.opencms.db.CmsAlias;
import org.opencms.db.CmsAliasImportResult;
import org.opencms.db.CmsAliasManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationReply;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasImportStatus;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.collect.Sets;

public class CmsAliasBulkEditHelper {

    private CmsObject m_cms;

    private Set<CmsUUID> m_freshStructureIds = new HashSet<CmsUUID>();

    private boolean m_hasErrors;

    public CmsAliasBulkEditHelper(CmsObject cms) {

        m_cms = cms;
    }

    public List<CmsAliasImportResult> importAliases(byte[] aliasData, String siteRoot) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(aliasData),
            CmsEncoder.ENCODING_UTF_8));
        String line = reader.readLine();
        List<CmsAliasImportResult> totalResult = new ArrayList<CmsAliasImportResult>();
        CmsAliasImportResult result;
        while (line != null) {
            result = processAliasLine(siteRoot, line);
            if (result != null) {
                totalResult.add(result);
            }
            line = reader.readLine();
        }
        return totalResult;
    }

    public void importAliases(HttpServletRequest request, HttpServletResponse response) throws Exception {

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(request);
        byte[] data = null;
        String siteRoot = null;
        for (FileItem fileItem : items) {
            String name = fileItem.getFieldName();
            if ("importfile".equals(name)) {
                data = fileItem.get();
            } else if ("siteroot".equals(name)) {
                siteRoot = new String(fileItem.get(), CmsEncoder.ENCODING_UTF_8);
            }
        }
        List<CmsAliasImportResult> result = new ArrayList<CmsAliasImportResult>();
        if ((siteRoot != null) && (data != null)) {
            result = importAliases(data, siteRoot);
        }
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (CmsAliasImportResult r : result) {
            array.put(r.toJson());
        }
        obj.put("result", array);

        response.getWriter().print(obj.toString());
    }

    public CmsAliasEditValidationReply saveAliases(CmsAliasSaveValidationRequest saveRequest) throws CmsException {

        CmsAliasEditValidationReply reply = validateAliases(saveRequest);
        if (m_hasErrors) {
            return reply;
        } else {
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
            //toDelete = filterStructureId(toDelete, allTouchedIds);
            Set<CmsAlias> toAdd = Sets.difference(editedAliasSet, aliasSet);
            //toAdd = filterStructureId(toAdd, allTouchedIds);

            aliasManager.updateAliases(m_cms, toDelete, toAdd);
            return null;
        }
    }

    public CmsAliasEditValidationReply validateAliases(CmsAliasEditValidationRequest validationRequest) {

        List<CmsAliasTableRow> editedData = validationRequest.getEditedData();
        CmsAliasTableRow newEntry = validationRequest.getNewEntry();
        if (newEntry != null) {
            newEntry.setKey((new CmsUUID()).toString());
            editedData.add(newEntry);

        }
        CmsObject cms = m_cms;
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
                    row.setAliasError("Duplicate alias path!");
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

    protected Set<CmsAlias> filterStructureId(Set<CmsAlias> aliases, Set<CmsUUID> structureIds) {

        Set<CmsAlias> result = new HashSet<CmsAlias>();
        for (CmsAlias alias : aliases) {
            if (structureIds.contains(alias.getStructureId())) {
                result.add(alias);
            }
        }
        return result;
    }

    protected CmsAliasImportResult processAliasImport(
        String siteRoot,
        String aliasPath,
        String vfsPath,
        CmsAliasMode mode) {

        CmsAliasManager manager = OpenCms.getAliasManager();
        try {
            return manager.importAlias(m_cms, siteRoot, aliasPath, vfsPath, mode);
        } catch (CmsException e) {
            return new CmsAliasImportResult(CmsAliasImportStatus.aliasError, e.getLocalizedMessage());
        }
    }

    protected CmsAliasImportResult processAliasLine(String siteRoot, String line) {

        line = line.trim();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(line) || line.startsWith("#")) {
            return null;
        }
        boolean comma = line.contains(",");
        boolean semicolon = line.contains(";");
        String separatorRegex;
        // CSV files generated by German excel versions may use ';' as separator 
        if (comma && !semicolon) {
            separatorRegex = ",";
        } else if (semicolon && !comma) {
            separatorRegex = ";";
        } else {
            return new CmsAliasImportResult(
                line,
                CmsAliasImportStatus.aliasError,
                "No column separator or ambiguous column separator!");
        }
        String[] tokens = line.split(separatorRegex);
        if ((tokens.length == 2) || (tokens.length == 3)) {
            String alias = tokens[0].trim();
            String vfsPath = tokens[1].trim();
            CmsAliasMode mode = CmsAliasMode.permanentRedirect;
            if (tokens.length == 3) {
                try {
                    mode = CmsAliasMode.valueOf(tokens[2].trim());
                } catch (Exception e) {
                    return new CmsAliasImportResult(line, CmsAliasImportStatus.aliasError, "Invalid format");
                }
            }
            CmsAliasImportResult returnValue = processAliasImport(siteRoot, alias, vfsPath, mode);
            returnValue.setLine(line);
            return returnValue;
        } else {
            return new CmsAliasImportResult(line, CmsAliasImportStatus.aliasError, "Invalid format");
        }
    }

    private void validateSingleAliasRow(CmsObject cms, CmsAliasTableRow row) {

        if (row.getStructureId() == null) {
            String path = row.getResourcePath();
            try {
                CmsResource resource = cms.readResource(path);
                row.setStructureId(resource.getStructureId());
                row.setOriginalStructureId(resource.getStructureId());
                m_freshStructureIds.add(resource.getStructureId());
            } catch (CmsException e) {
                row.setPathError("Resource not found!");
                m_hasErrors = true;
            }
        }
        if (!CmsAlias.ALIAS_PATTERN.matcher(row.getAliasPath()).matches()) {
            row.setAliasError("Invalid alias path!");
            m_hasErrors = true;
        }
    }
}
