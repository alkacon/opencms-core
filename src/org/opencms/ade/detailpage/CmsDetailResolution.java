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

package org.opencms.ade.detailpage;

import org.opencms.file.CmsResource;

/**
 * The result of resolving a detail page URI: the container page that renders the request, and the
 * detail content it shows (or, for a named function, the function default page).<p>
 *
 * Instances are created by {@link CmsDetailPageUtil#resolveDetail(org.opencms.file.CmsObject, java.lang.String)}.<p>
 *
 * @since 21.0.0
 */
public final class CmsDetailResolution {

    /** The detail content, or <code>null</code> if this is a detail function request. */
    private CmsResource m_detailContent;

    /** The detail name, i.e. the last URI segment the request was resolved from. */
    private String m_detailName;

    /** The detail page that renders the request. */
    private CmsResource m_detailPage;

    /** The function default page, or <code>null</code> if this is a detail content request. */
    private CmsResource m_functionPage;

    /**
     * Creates a new detail resolution.<p>
     *
     * @param detailPage the detail page that renders the request
     * @param detailContent the detail content, or <code>null</code> for a detail function request
     * @param functionPage the function default page, or <code>null</code> for a detail content request
     * @param detailName the detail name the request was resolved from
     */
    private CmsDetailResolution(
        CmsResource detailPage,
        CmsResource detailContent,
        CmsResource functionPage,
        String detailName) {

        m_detailPage = detailPage;
        m_detailContent = detailContent;
        m_functionPage = functionPage;
        m_detailName = detailName;
    }

    /**
     * Creates the resolution of a detail content request.<p>
     *
     * @param detailPage the detail page that renders the content
     * @param detailContent the detail content
     * @param detailName the detail name the request was resolved from
     *
     * @return the detail resolution
     */
    static CmsDetailResolution forContent(CmsResource detailPage, CmsResource detailContent, String detailName) {

        return new CmsDetailResolution(detailPage, detailContent, null, detailName);
    }

    /**
     * Creates the resolution of a named function request.<p>
     *
     * @param detailPage the detail page that renders the function
     * @param functionPage the function default page
     * @param detailName the detail name the request was resolved from
     *
     * @return the detail resolution
     */
    static CmsDetailResolution forFunction(CmsResource detailPage, CmsResource functionPage, String detailName) {

        return new CmsDetailResolution(detailPage, null, functionPage, detailName);
    }

    /**
     * Returns the detail content, or <code>null</code> if this is a detail function request.<p>
     *
     * @return the detail content
     */
    public CmsResource getDetailContent() {

        return m_detailContent;
    }

    /**
     * Returns the detail name, i.e. the last URI segment the request was resolved from.<p>
     *
     * A detail name which is a valid UUID means the URI addressed the detail content by its
     * structure id rather than by a mapped URL name.<p>
     *
     * @return the detail name
     */
    public String getDetailName() {

        return m_detailName;
    }

    /**
     * Returns the detail page that renders the request.<p>
     *
     * @return the detail page
     */
    public CmsResource getDetailPage() {

        return m_detailPage;
    }

    /**
     * Returns the function default page, or <code>null</code> if this is a detail content request.<p>
     *
     * @return the function default page
     */
    public CmsResource getFunctionPage() {

        return m_functionPage;
    }

    /**
     * Returns <code>true</code> if this is a named function request rather than a detail content request.<p>
     *
     * @return <code>true</code> for a named function request
     */
    public boolean isFunctionDetail() {

        return m_functionPage != null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "CmsDetailResolution["
            + m_detailName
            + " -> "
            + (m_detailPage == null ? "null" : m_detailPage.getRootPath())
            + ", "
            + (isFunctionDetail()
            ? m_functionPage.getRootPath()
            : (m_detailContent == null ? "null" : m_detailContent.getRootPath()))
            + "]";
    }
}
