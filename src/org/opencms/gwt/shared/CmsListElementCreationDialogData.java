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

package org.opencms.gwt.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data needed by the dialog for creating new list elements.
 */
public class CmsListElementCreationDialogData implements IsSerializable {

    /** The dialog caption. */
    private String m_caption;

    /** The resource info bean to display on top. */
    private CmsListInfoBean m_listInfo;

    /** The error message to display. */
    private String m_message;

    /** The list of options (types). */
    private List<CmsListElementCreationOption> m_options = new ArrayList<>();

    /** The post-create handler to use. */
    private String m_postCreateHandler;

    /** The upload folder. */
    private String m_uploadFolder;

    /**
     * Creates a new instance.
     */
    public CmsListElementCreationDialogData() {}

    /**
     * Adds an option.
     *
     * @param option the option to add
     */
    public void add(CmsListElementCreationOption option) {

        m_options.add(option);
    }

    /**
     * Gets the caption.
     *
     * @return the caption
     */
    public String getCaption() {

        return m_caption;
    }

    /**
     * Gets the resource list info for the element to display on top of the dialog.
     *
     * @return the list info
     */
    public CmsListInfoBean getListInfo() {

        return m_listInfo;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * Gets the options.
     *
     * @return the options
     */
    public List<CmsListElementCreationOption> getOptions() {

        return Collections.unmodifiableList(m_options);
    }

    /**
     * Gets the post create handler.
     *
     * @return the post create handler
     */
    public String getPostCreateHandler() {

        return m_postCreateHandler;
    }

    /**
     * Gets the upload folder.
     * 
     * @return the upload folder
     */
    public String getUploadFolder() {

        return m_uploadFolder;
    }

    /** 
     * Checks if adding to the list should trigger upload mode.
     * 
     * @return true if this requires upload mode 
     */
    public boolean isUpload() {

        return m_uploadFolder != null;
    }

    /**
     * Sets the caption.
     *
     * @param caption the new caption
     */
    public void setCaption(String caption) {

        m_caption = caption;
    }

    /**
     * Sets the list info.
     *
     * @param listInfo the new list info
     */
    public void setListInfo(CmsListInfoBean listInfo) {

        m_listInfo = listInfo;
    }

    /**
     * Sets the message.
     *
     * @param message the new message
     */
    public void setMessage(String message) {

        m_message = message;
    }

    /**
     * Sets the post create handler.
     *
     * @param postCreateHandler the new post create handler
     */
    public void setPostCreateHandler(String postCreateHandler) {

        m_postCreateHandler = postCreateHandler;
    }

    /**
     * Sets the upload folder.
     * 
     * @param uploadFolder the upload folder
     */
    public void setUploadFolder(String uploadFolder) {

        m_uploadFolder = uploadFolder;
    }

}
