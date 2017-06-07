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

package org.opencms.gwt.client;

import org.opencms.util.CmsUUID;

/**
 * Bean holding data needed to open the xml content editor.<p>
 *
 * @since 8.0.1
 */
public class CmsEditableData implements I_CmsEditableData {

    /** The context id, identifying the collector list  instance. */
    private String m_contextId;

    /** The edit id. */
    private String m_editId;

    /** The element language. */
    private String m_elementLanguage;

    /** The element name. */
    private String m_elementName;

    /** The main language to copy in case the element language node does not exist yet. */
    private String m_mainLanguage;

    /** The new link. */
    private String m_newLink;

    /** The new title. */
    private String m_newTitle;

    /** The no edit reason. */
    private String m_noEditReason;

    /** The optional class name of a post-create handler. */
    private String m_postCreateHandler;

    /** The site path. */
    private String m_sitePath;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The unreleased or expired flag. */
    private boolean m_unreleaseOrExpired;

    /**
     * Default constructor.<p>
     */
    public CmsEditableData() {

        // do nothing
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getContextId()
     */
    public String getContextId() {

        return m_contextId;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getEditId()
     */
    public String getEditId() {

        return m_editId;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getElementLanguage()
     */
    public String getElementLanguage() {

        return m_elementLanguage;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getElementName()
     */
    public String getElementName() {

        return m_elementName;
    }

    /**
     * Returns the main language to copy in case the element language node does not exist yet.<p>
     *
     * @return the main language to copy in case the element language node does not exist yet
     */
    public String getMainLanguage() {

        return m_mainLanguage;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getNewLink()
     */
    public String getNewLink() {

        return m_newLink;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getNewTitle()
     */
    public String getNewTitle() {

        return m_newTitle;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getNoEditReason()
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getPostCreateHandler()
     */
    public String getPostCreateHandler() {

        return m_postCreateHandler;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getSitePath()
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#getStructureId()
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#isUnreleasedOrExpired()
     */
    public boolean isUnreleasedOrExpired() {

        return m_unreleaseOrExpired;
    }

    /**
     * Sets  the collector context id.<p>
     *
     * @param id the collector context id
     */
    public void setContextId(String id) {

        m_contextId = id;

    }

    /**
     * Sets the edit id.<p>
     *
     * @param editId the edit id to set
     */
    public void setEditId(String editId) {

        m_editId = editId;
    }

    /**
     * Sets the element language.<p>
     *
     * @param elementLanguage the element language to set
     */
    public void setElementLanguage(String elementLanguage) {

        m_elementLanguage = elementLanguage;
    }

    /**
     * Sets the element name.<p>
     *
     * @param elementName the element name to set
     */
    public void setElementName(String elementName) {

        m_elementName = elementName;
    }

    /**
     * Sets the main language to copy in case the element language node does not exist yet.<p>
     *
     * @param mainLanguage the main language to copy in case the element language node does not exist yet
     */
    public void setMainLanguage(String mainLanguage) {

        m_mainLanguage = mainLanguage;
    }

    /**
     * Sets the new link.<p>
     *
     * @param newLink the new link to set
     */
    public void setNewLink(String newLink) {

        m_newLink = newLink;
    }

    /**
     * Sets the new title.<p>
     *
     * @param newTitle the new title to set
     */
    public void setNewTitle(String newTitle) {

        m_newTitle = newTitle;
    }

    /**
     * Sets the no edit reason.<p>
     *
     * @param noEditReason the no edit reason to set
     */
    public void setNoEditReason(String noEditReason) {

        m_noEditReason = noEditReason;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsEditableData#setSitePath(java.lang.String)
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;

    }

    /**
     * Sets the structure id.<p>
     *
     * @param structureId the structure id to set
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the unreleased or expired flag.<p>
     *
     * @param unreleaseOrExpired the unreleased or expired flag
     */
    public void setUnreleaseOrExpired(boolean unreleaseOrExpired) {

        m_unreleaseOrExpired = unreleaseOrExpired;
    }
}
