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

package org.opencms.ugc;

import org.opencms.main.CmsLog;
import org.opencms.ugc.shared.CmsUgcConstants;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * A helper class which processes and stores uploaded form data belonging to a single form edit session.<p>
 */
public class CmsUgcUploadHelper {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUgcUploadHelper.class);

    /** The stored form data. */
    private ConcurrentHashMap<String, List<FileItem>> m_storedFormData = new ConcurrentHashMap<String, List<FileItem>>();

    /**
     * Passes the form data with the given ID to the handler object, then removes it and deletes its stored data.<p>
     *
     * The form data is removed even if an exception is thrown while calling the form data handler.
     *
     * @param formDataId the id of the form data to process
     * @param handler the handler to which the form data should be passed
     * @throws Exception if something goes wrong
     */
    public void consumeFormData(String formDataId, I_CmsFormDataHandler handler) throws Exception {

        List<FileItem> items = m_storedFormData.get(formDataId);

        if (items != null) {
            Map<String, I_CmsFormDataItem> itemMap = Maps.newHashMap();
            LOG.debug(formDataId + ": Processing file items");
            for (FileItem item : items) {
                LOG.debug(formDataId + ": " + item.toString());
                if (!item.isFormField() && CmsStringUtil.isEmptyOrWhitespaceOnly(item.getName())) {
                    LOG.debug(formDataId + ": skipping previous file field because it is empty");
                } else {
                    itemMap.put(item.getFieldName(), new CmsUgcDataItem(item));
                }
            }
            Exception storedException = null;
            try {
                handler.handleFormData(itemMap);
            } catch (Exception e) {
                storedException = e;
            }
            for (FileItem item : items) {
                item.delete();
            }
            m_storedFormData.remove(formDataId);
            if (storedException != null) {
                throw storedException;
            }
        }
    }

    /**
     * Processes a request caused by a form submit.<p>
     *
     * @param request the request to process
     */
    void processFormSubmitRequest(HttpServletRequest request) {

        String formDataId = getFormDataId(request);
        List<FileItem> items = CmsRequestUtil.readMultipartFileItems(request);
        m_storedFormData.put(formDataId, items);
    }

    /**
     * Gets the id to be used for storing form data from the request.<p>
     *
     * @param request the request containing the form data
     *
     * @return the id to use for storing the form data
     */
    private String getFormDataId(HttpServletRequest request) {

        String result = request.getParameter(CmsUgcConstants.PARAM_FORM_DATA_ID);
        return result;
    }

}
