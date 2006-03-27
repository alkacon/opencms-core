/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsDifferenceDialog.java,v $
 * Date   : $Date: 2006/03/27 14:52:44 $
 * Version: $Revision: 1.2 $
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 * All rights reserved.
 * 
 * This source code is the intellectual property of Alkacon Software GmbH.
 * It is PROPRIETARY and CONFIDENTIAL.
 * Use of this source code is subject to license terms.
 *
 * In order to use this source code, you need written permission from 
 * Alkacon Software GmbH. Redistribution of this source code, in modified 
 * or unmodified form, is not allowed unless written permission by 
 * Alkacon Software GmbH has been given.
 *
 * ALKACON SOFTWARE GMBH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOURCE CODE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ALKACON SOFTWARE GMBH SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOURCE CODE OR ITS DERIVATIVES.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 */

package org.opencms.workplace.comparison;

import org.opencms.jsp.CmsJspActionElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a GUI for the file comparison dialog.<p> 
 *
 * @author Jan Baudisch  
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDifferenceDialog extends A_CmsDiffViewDialog {

    private String m_copySource;

    private String m_originalSource;

    private String m_paramCompare;

    /** Parameter value for the element name. */
    private String m_paramElement;

    private String m_paramLocale;

    private String m_paramPath1;

    private String m_paramPath2;

    private String m_paramTagId1;

    private String m_paramTagId2;

    private String m_paramTextmode;

    /** Parameter value for the configuration file name. */
    private String m_paramVersion1;

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
     * Returns the paramPath1.<p>
     *
     * @return the paramPath1
     */
    public String getParamPath1() {

        return m_paramPath1;
    }

    /**
     * Returns the paramPath2.<p>
     *
     * @return the paramPath2
     */
    public String getParamPath2() {

        return m_paramPath2;
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
     * Sets the paramPath1.<p>
     *
     * @param paramPath1 the paramPath1 to set
     */
    public void setParamPath1(String paramPath1) {

        m_paramPath1 = paramPath1;
    }

    /**
     * Sets the paramPath2.<p>
     *
     * @param paramPath2 the paramPath2 to set
     */
    public void setParamPath2(String paramPath2) {

        m_paramPath2 = paramPath2;
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
    protected String getCopySource() {

        return m_copySource;
    }

    /**
     * 
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#getLinesBeforeSkip()
     */
    protected int getLinesBeforeSkip() {

        return 2;
    }

    /**
     * 
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#getOriginalSource()
     */
    protected String getOriginalSource() {

        return m_originalSource;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * 
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#validateParamaters()
     */
    protected void validateParamaters() {

        // noop
    }
}