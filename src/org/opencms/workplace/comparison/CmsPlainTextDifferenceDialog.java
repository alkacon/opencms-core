/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/Attic/CmsPlainTextDifferenceDialog.java,v $
 * Date   : $Date: 2005/12/02 16:22:41 $
 * Version: $Revision: 1.1.2.1 $
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

import com.alkacon.diff.Diff;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.page.CmsXmlPageFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Provides a GUI for the file comparison dialog.<p> 
 *
 * @author Jan Baudisch  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPlainTextDifferenceDialog extends CmsElementDifferenceDialog {

    private String m_copySource;

    private String m_originalSource;

    private String m_paramCompare;

    /** Parameter value for the element name. */
    private String m_paramElement;

    private String m_paramLocale;

    private String m_paramTagId1;

    private String m_paramTagId2;

    private String m_paramTextmode;

    /** Parameter value for the configuration file name. */
    private String m_paramVersion1;

    private String m_paramVersion2;

    private String m_paramPath1;
    
    private String m_paramPath2;
    
    /**
     * Default constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPlainTextDifferenceDialog(CmsJspActionElement jsp) {
        
        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPlainTextDifferenceDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Converts an attribute list to a string.<p>
     * 
     * @param attributes the attributes to compare
     * @return a string respresentation of the attribute list
     */
    protected String[] getAttributesAsString(List attributes) {

        Iterator i = attributes.iterator();
        StringBuffer res1 = new StringBuffer(512);
        StringBuffer res2 = new StringBuffer(512);
        while (i.hasNext()) {
            CmsAttributeComparison compare = (CmsAttributeComparison)i.next();
            res1.append(compare.getName()).append(": ").append(compare.getVersion1()).append("<br/>\n");
            res2.append(compare.getName()).append(": ").append(compare.getVersion2()).append("<br/>\n");
        }
        return new String[] {res1.toString(), res2.toString()};
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        boolean changed = !getOriginalSource().equals(getCopySource());
        if (getAction() == ACTION_CANCEL) {
            actionCloseDialog();
        }
        JspWriter out = getJsp().getJspContext().getOut();
        out.print("<link rel='stylesheet' type='text/css' href='");
        out.print(getStyleUri(getJsp()));
        out.println("diff.css'>");
        out.print("<form name='diff-form' method='post' action='");
        out.print(getDialogUri());
        out.println("'>");
        out.println(allParamsAsHidden());
        out.println("</form>");
        out.println("<p style='text-align: right;'>");
        String iconPath = null;
        String onClic = "javascript:document.forms['diff-form'].mode.value = '";
        if (getMode() == CmsDiffViewMode.ALL) {
            iconPath = A_CmsListDialog.ICON_DETAILS_HIDE;
            onClic += CmsDiffViewMode.DIFF_ONLY;

        } else {
            iconPath = A_CmsListDialog.ICON_DETAILS_SHOW;
            onClic += CmsDiffViewMode.ALL;
        }
        onClic += "'; document.forms['diff-form'].submit();";
        if (changed) {
            // print show / hide button
            out.println(A_CmsHtmlIconButton.defaultButtonHtml(
                getJsp(),
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                "id",
                getMode().getName().key(getLocale()),
                null,
                true,
                iconPath,
                null,
                onClic));
        } else {
            // display all text, if there are no differences
            setMode(CmsDiffViewMode.ALL);
        }
        out.println("</p>");
        out.println(dialogBlockStart(null));
        out.println("<table cellspacing='0' cellpadding='0' class='xmlTable'>\n<tr><td><pre>");
        try {
            CmsHtmlDiffConfiguration conf = new CmsHtmlDiffConfiguration(getMode() == CmsDiffViewMode.ALL ? -1
            : getLinesBeforeSkip(), getLocale());
            String diff = Diff.diffAsHtml(getOriginalSource(), getCopySource(), conf);
            if (CmsStringUtil.isNotEmpty(diff)) {
                out.println(diff);
            } else {
                out.println(wrapLinesWithUnchangedStyle(getOriginalSource())); // print original source, if there are no differences
            }
        } catch (Exception e) {
            out.print(e);
        }
        out.println("</pre></td></tr>\n</table>");
        out.println(dialogBlockEnd());
        out.println(dialogContentEnd());
        out.println(dialogEnd());
        out.println(bodyEnd());
        out.println(htmlEnd());
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
     * Returns the paramOldversionid.<p>
     *
     * @return the paramOldversionid
     */
    public String getParamVersion1() {

        return m_paramVersion1;
    }

    /**
     * Returns the paramNewversionid.<p>
     *
     * @return the paramNewversionid
     */
    public String getParamVersion2() {

        return m_paramVersion2;
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
     * Sets the paramOldversionid.<p>
     *
     * @param paramOldversionid the paramOldversionid to set
     */
    public void setParamVersion1(String paramOldversionid) {

        m_paramVersion1 = paramOldversionid;
    }

    /**
     * Sets the paramNewversionid.<p>
     *
     * @param paramNewversionid the paramNewversionid to set
     */
    public void setParamVersion2(String paramNewversionid) {

        m_paramVersion2 = paramNewversionid;
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamTextmode())) {
            // ensure a valid mode is set
            setParamTextmode(MODE_TEXT);
        }
        try {
            CmsFile file1;
            CmsFile file2;
            if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion1())) {
                file1 = getCms().readFile(getCms().getRequestContext().removeSiteRoot(getParamPath1()));
            } else {
                file1 = getCms().readBackupFile(getCms().getRequestContext().removeSiteRoot(getParamPath1()), 
                    Integer.parseInt(getParamTagId1()));
            }
            if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion2())) {
                file2 = getCms().readFile(getCms().getRequestContext().removeSiteRoot(getParamPath2()));
            } else {
                file2 = getCms().readBackupFile(getCms().getRequestContext().removeSiteRoot(getParamPath2()), 
                    Integer.parseInt(getParamTagId2()));
            }
            if ("properties".equals(getParamCompare())) {

                setPropertiesAsSource(file1, file2);
            } else if ("attributes".equals(getParamCompare())) {

                setAttributesAsSource(file1, file2);
            } else {

                setElementsAsSource(file1, file2);
            }
        } catch (CmsException e) {
            // no-op
        } catch (UnsupportedEncodingException e) {
            // no-op
        }
    }

    /**
     * 
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#validateParamaters()
     */
    protected void validateParamaters() {

        // noop
    }

    private void setAttributesAsSource(CmsFile file1, CmsFile file2) throws CmsException {

        CmsResourceComparison comparison = new CmsResourceComparison(getCms(), file1, file2);
        String[] propertyStrings = getAttributesAsString(comparison.getComparedAttributes());
        m_originalSource = propertyStrings[0];
        m_copySource = propertyStrings[1];
    }

    private void setElementsAsSource(CmsFile file1, CmsFile file2) throws CmsException, UnsupportedEncodingException {

        CmsObject cms = getCms();
        if (CmsStringUtil.isNotEmpty(getParamElement())) {

            I_CmsXmlDocument resource1;
            I_CmsXmlDocument resource2;
            if (file1.getTypeId() == CmsResourceTypeXmlPage.getStaticTypeId()) {
                resource1 = CmsXmlPageFactory.unmarshal(cms, file1);
            } else {
                resource1 = CmsXmlContentFactory.unmarshal(cms, file1);
            }
            if (file2.getTypeId() == CmsResourceTypeXmlPage.getStaticTypeId()) {
                resource2 = CmsXmlPageFactory.unmarshal(cms, file2);
            } else {
                resource2 = CmsXmlContentFactory.unmarshal(cms, file2);
            }
            I_CmsXmlContentValue value1 = resource1.getValue(getParamElement(), new Locale(getParamLocale()));
            I_CmsXmlContentValue value2 = resource2.getValue(getParamElement(), new Locale(getParamLocale()));
            if (value1 == null) {
                m_originalSource = "";
            } else {
                m_originalSource = value1.getStringValue(cms);
            }
            if (value2 == null) {
                m_copySource = "";
            } else {
                m_copySource = value2.getStringValue(cms);
            }
        } else { 
            // compare whole plain text file
            m_originalSource = new String(file1.getContents(), cms.getRequestContext().getEncoding());
            m_copySource = new String(file2.getContents(), cms.getRequestContext().getEncoding());
        }
    }

    private void setPropertiesAsSource(CmsFile file1, CmsFile file2) throws CmsException {

        CmsResourceComparison comparison = new CmsResourceComparison(getCms(), file1, file2);
        String[] propertyStrings = getAttributesAsString(comparison.getComparedProperties());
        m_originalSource = propertyStrings[0];
        m_copySource = propertyStrings[1];
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
     * Sets the paramPath1.<p>
     *
     * @param paramPath1 the paramPath1 to set
     */
    public void setParamPath1(String paramPath1) {
    
        m_paramPath1 = paramPath1;
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
     * Sets the paramPath2.<p>
     *
     * @param paramPath2 the paramPath2 to set
     */
    public void setParamPath2(String paramPath2) {
    
        m_paramPath2 = paramPath2;
    }
}