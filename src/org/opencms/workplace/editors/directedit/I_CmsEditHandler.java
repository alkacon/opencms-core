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

package org.opencms.workplace.editors.directedit;

import org.opencms.ade.containerpage.shared.CmsDialogOptions;
import org.opencms.file.CmsObject;
import org.opencms.file.collectors.I_CmsCollectorPostCreateHandler;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.Locale;
import java.util.Map;

/**
 * Edit handlers are optional and can be configured within the XSD-schema of a resource type.<p>
 * Edit handlers may be used to enhance content editing within the container page editor.
 * They allow edit pre-processing, and specific delete operations.<p>
 */
public interface I_CmsEditHandler {

    /**
     * Returns a map of delete options. The value being the option description displayed to the user.<p>
     *
     * @param cms the cms context
     * @param elementBean the container element to be deleted
     * @param pageContextId the structure id of the context containerpage
     * @param requestParams the request parameters
     *
     * @return the available delete options
     */
    CmsDialogOptions getDeleteOptions(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams);

    /**
     * Returns a map of edit options. The value being the option description displayed to the user.<p>
     *
     * @param cms the cms context
     * @param elementBean the container element to be edited
     * @param pageContextId the structure id of the context containerpage
     * @param requestParams the request parameters
     * @param isListElement in case a list element, not a container element is about to be edited
     *
     * @return the available edit options
     */
    CmsDialogOptions getEditOptions(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams,
        boolean isListElement);

    /**
     * Gets the options for the 'New' (plus) operation in the page editor.<p>
     *
     * If this returns null, the default behavior for the 'New' operation will be used instead.
     *
     * @param cms the cms context
     * @param elementBean the container element bean from which the 'New' operation was initiated
     * @param pageContextId the structure id of the container page
     * @param requestParam the request parameters
     *
     * @return the available options, or null if the default behavior should be used
     */
    CmsDialogOptions getNewOptions(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        CmsUUID pageContextId,
        Map<String, String[]> requestParam);

    /**
     * Executes the actual delete.<p>
     *
     * @param cms the cms context
     * @param elementBean the container element to delete
     * @param deleteOption the selected delete option
     * @param pageContextId the structure id of the context containerpage
     * @param requestParams the request parameters
     *
     * @throws CmsException if something goes wrong
     */
    void handleDelete(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        String deleteOption,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams)
    throws CmsException;

    /**
     * Creates a new resource to edit.<p>
     *
     * @param cms The CmsObject of the current request
     * @param newLink A string, specifying where which new content should be created.
     * @param locale The locale
     * @param referenceSitePath site path of the currently edited content.
     * @param modelFileSitePath site path of the model file
     * @param postCreateHandler optional class name of an {@link I_CmsCollectorPostCreateHandler} which is invoked after the content has been created.
     * @param element the container element bean
     * @param pageId the page id
     * @param requestParams the request parameters
     * @param choice the option chosen by the user
     *
     * @return The site-path of the newly created resource.
     * @throws CmsException if something goes wrong
     */
    String handleNew(
        CmsObject cms,
        String newLink,
        Locale locale,
        String referenceSitePath,
        String modelFileSitePath,
        String postCreateHandler,
        CmsContainerElementBean element,
        CmsUUID pageId,
        Map<String, String[]> requestParams,
        String choice)
    throws CmsException;

    /**
     * Prepares the resource to be edited.<p>
     *
     * @param cms the cms context
     * @param elementBean the container element to be edited
     * @param editOption the selected edit option
     * @param pageContextId the structure id of the context containerpage
     * @param requestParams the request parameters
     *
     * @return the structure id of the resource to be edited, may differ from the original element id
     *
     * @throws CmsException if something goes wrong
     */
    CmsUUID prepareForEdit(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        String editOption,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams)
    throws CmsException;

    /**
     * Sets parameters for the edit handler.<p>
     *
     * @param params the parameters
     */
    void setParameters(Map<String, String> params);

}
