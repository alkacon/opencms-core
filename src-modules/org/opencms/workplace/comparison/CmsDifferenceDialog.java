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

package org.opencms.workplace.comparison;

import org.opencms.jsp.CmsJspActionElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a GUI for the file comparison dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsDifferenceDialog extends A_CmsDiffViewDialog {

    /** The content of the second file to compare. */
    private String m_copySource;

    /** The content of the first file to compare. */
    private String m_originalSource;

    /** Parameter value indicating wether to compare properties, attributes or elements. */
    private String m_paramCompare;

    /** Parameter value for the element name. */
    private String m_paramElement;

    /** Parameter value for the locale. */
    private String m_paramLocale;

    /** Parameter value for the structure id of the first file. */
    private String m_paramId1;

    /** Parameter value for the structure id of the second file. */
    private String m_paramId2;

    /** Parameter value for the tag id of the first file. */
    private String m_paramTagId1;

    /** Parameter value for the tag id of the second file. */
    private String m_paramTagId2;

    /** Parameter value for the text mode. */
    private String m_paramTextmode;

    /** Parameter value for the version of the first file. */
    private String m_paramVersion1;

    /** Parameter value for the version of the second file. */
    private String m_paramVersion2;

    /**
     * Default constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDifferenceDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDifferenceDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the paramCompare.<p>
     *
     * @return the paramCompare
     */
    public String getParamCompare() {

        return m_paramCompare;
    }

    /**
     * Returns the paramElement.<p>
     *
     * @return the paramElement
     */
    public String getParamElement() {

        return m_paramElement;
    }

    /**
     * Returns the paramLocale.<p>
     *
     * @return the paramLocale
     */
    public String getParamLocale() {

        return m_paramLocale;
    }

    /**
     * Returns the paramId1.<p>
     *
     * @return the paramId1
     */
    public String getParamId1() {

        return m_paramId1;
    }

    /**
     * Returns the paramId2.<p>
     *
     * @return the paramId2
     */
    public String getParamId2() {

        return m_paramId2;
    }

    /**
     * Returns the paramTagId1.<p>
     *
     * @return the paramTagId1
     */
    public String getParamTagId1() {

        return m_paramTagId1;
    }

    /**
     * Returns the paramTagId2.<p>
     *
     * @return the paramTagId2
     */
    public String getParamTagId2() {

        return m_paramTagId2;
    }

    /**
     * Returns the paramTextmode.<p>
     *
     * @return the paramTextmode
     */
    public String getParamTextmode() {

        return m_paramTextmode;
    }

    /**
     * Returns the paramVersion1.<p>
     *
     * @return the paramVersion1
     */
    public String getParamVersion1() {

        return m_paramVersion1;
    }

    /**
     * Returns the paramVersion2.<p>
     *
     * @return the paramVersion2
     */
    public String getParamVersion2() {

        return m_paramVersion2;
    }

    /**
     * Sets the copySource.<p>
     *
     * @param copySource the copySource to set
     */
    public void setCopySource(String copySource) {

        m_copySource = copySource;
    }

    /**
     * Sets the originalSource.<p>
     *
     * @param originalSource the originalSource to set
     */
    public void setOriginalSource(String originalSource) {

        m_originalSource = originalSource;
    }

    /**
     * Sets the paramCompare.<p>
     *
     * @param paramCompare the paramCompare to set
     */
    public void setParamCompare(String paramCompare) {

        m_paramCompare = paramCompare;
    }

    /**
     * Sets the paramElement.<p>
     *
     * @param paramElement the paramElement to set
     */
    public void setParamElement(String paramElement) {

        m_paramElement = paramElement;
    }

    /**
     * Sets the paramLocale.<p>
     *
     * @param paramLocale the paramLocale to set
     */
    public void setParamLocale(String paramLocale) {

        m_paramLocale = paramLocale;
    }

    /**
     * Sets the paramId1.<p>
     *
     * @param paramId1 the paramId1 to set
     */
    public void setParamId1(String paramId1) {

        m_paramId1 = paramId1;
    }

    /**
     * Sets the paramId2.<p>
     *
     * @param paramId2 the paramId2 to set
     */
    public void setParamId2(String paramId2) {

        m_paramId2 = paramId2;
    }

    /**
     * Sets the paramTagId1.<p>
     *
     * @param paramTagId1 the paramTagId1 to set
     */
    public void setParamTagId1(String paramTagId1) {

        m_paramTagId1 = paramTagId1;
    }

    /**
     * Sets the paramTagId2.<p>
     *
     * @param paramTagId2 the paramTagId2 to set
     */
    public void setParamTagId2(String paramTagId2) {

        m_paramTagId2 = paramTagId2;
    }

    /**
     * Sets the paramTextmode.<p>
     *
     * @param paramTextmode the paramTextmode to set
     */
    public void setParamTextmode(String paramTextmode) {

        m_paramTextmode = paramTextmode;
    }

    /**
     * Sets the paramVersion1.<p>
     *
     * @param paramVersion1 the paramVersion1 to set
     */
    public void setParamVersion1(String paramVersion1) {

        m_paramVersion1 = paramVersion1;
    }

    /**
     * Sets the paramVersion2.<p>
     *
     * @param paramVersion2 the paramVersion2 to set
     */
    public void setParamVersion2(String paramVersion2) {

        m_paramVersion2 = paramVersion2;
    }

    /**
     *
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#getCopySource()
     */
    @Override
    protected String getCopySource() {

        return m_copySource;
    }

    /**
     *
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#getLinesBeforeSkip()
     */
    @Override
    protected int getLinesBeforeSkip() {

        return 2;
    }

    /**
     *
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#getOriginalSource()
     */
    @Override
    protected String getOriginalSource() {

        return m_originalSource;
    }

    /**
     *
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() {

        // noop
    }
}