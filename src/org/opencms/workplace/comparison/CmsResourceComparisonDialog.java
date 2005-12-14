/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsResourceComparisonDialog.java,v $
 * Date   : $Date: 2005/12/14 16:12:40 $
 * Version: $Revision: 1.1.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.extractors.CmsExtractorMsExcel;
import org.opencms.search.extractors.CmsExtractorMsPowerPoint;
import org.opencms.search.extractors.CmsExtractorMsWord;
import org.opencms.search.extractors.CmsExtractorPdf;
import org.opencms.search.extractors.CmsExtractorRtf;
import org.opencms.search.extractors.I_CmsTextExtractor;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.CmsMultiListDialog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Helper class for managing three lists on the same dialog.<p>
 * 
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceComparisonDialog extends CmsDialog {

    /** Constant indicating that all elements are compared.<p> */
    public static final String COMPARE_ALL_ELEMENTS = "allelements";
    /** Constant indicating that the attributes are compared.<p> */
    public static final String COMPARE_ATTRIBUTES = "attributes";
    /** Constant indicating that the properties are compared.<p> */
    public static final String COMPARE_PROPERTIES = "properties";
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceComparisonDialog.class);

    private CmsDifferenceDialog m_differenceDialog;

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
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsResourceComparisonDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsResourceComparisonDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Display method for two list dialogs.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void displayDialog() throws Exception {

        CmsFileInfoDialog fileInfo = new CmsFileInfoDialog(getJsp()) {

            protected String defaultActionHtmlEnd() {

                return "";
            }
        };
        if (fileInfo.isForwarded()) {
            return;
        }
        fileInfo.displayDialog(true);
        fileInfo.writeDialog();

        CmsPropertyComparisonList propertyDiff = new CmsPropertyComparisonList(getJsp());
        CmsAttributeComparisonList attributeDiff = new CmsAttributeComparisonList(getJsp());
        List lists = new ArrayList();
        lists.add(attributeDiff);
        I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(propertyDiff.getResourceType());

        if (resourceType instanceof CmsResourceTypeXmlContent || resourceType instanceof CmsResourceTypeXmlPage) {

            // display attributes, properties and compared elements
            CmsElementComparisonList contentDiff = new CmsElementComparisonList(getJsp());
            lists.add(contentDiff);
            lists.add(propertyDiff);
            CmsMultiListDialog threeLists = new CmsMultiListDialog(lists);
            // perform the active list actions
            threeLists.displayDialog(true);
            // write the content of list dialog
            threeLists.writeDialog();

        } else if (resourceType instanceof CmsResourceTypeImage) {

            // display attributes, properties and images
            lists.add(propertyDiff);
            CmsMultiListDialog twoLists = new CmsMultiListDialog(lists) {

                public String defaultActionHtmlEnd() {

                    return "";
                }
            };
            twoLists.displayDialog(true);
            twoLists.displayDialog();
            CmsImageComparisonDialog images = new CmsImageComparisonDialog(getJsp());
            images.displayDialog();

        } else {

            // display attributes and properties 
            lists.add(propertyDiff);
            CmsMultiListDialog twoLists = new CmsMultiListDialog(lists);
            twoLists.displayDialog(true);
            twoLists.displayDialog();

            String path1 = propertyDiff.getParamPath1();
            String path2 = propertyDiff.getParamPath2();

            byte[] content1 = propertyDiff.getFile1().getContents();
            byte[] content2 = propertyDiff.getFile2().getContents();

            String originalSource = null;
            String copySource = null;

            I_CmsTextExtractor textExtractor = null;
            if (path1.endsWith(".pdf") && path2.endsWith(".pdf")) {
                textExtractor = CmsExtractorPdf.getExtractor();
            } else if (path1.endsWith(".doc") && path2.endsWith(".doc")) {
                textExtractor = CmsExtractorMsWord.getExtractor();
            } else if (path1.endsWith(".xls") && path2.endsWith(".xls")) {
                textExtractor = CmsExtractorMsExcel.getExtractor();
            } else if (path1.endsWith(".rtf") && path2.endsWith(".rtf")) {
                textExtractor = CmsExtractorRtf.getExtractor();
            } else if (path1.endsWith(".ppt") && path2.endsWith(".ppt")) {
                textExtractor = CmsExtractorMsPowerPoint.getExtractor();
            }
            if (textExtractor != null) {
                // extract the content
                originalSource = textExtractor.extractText(content1).getContent();
                copySource = textExtractor.extractText(content2).getContent();
            } else if (resourceType instanceof CmsResourceTypePlain || resourceType instanceof CmsResourceTypeJsp) {
                originalSource = new String(content1);
                copySource = new String(content2);
            }
            if (CmsStringUtil.isNotEmpty(originalSource) && CmsStringUtil.isNotEmpty(copySource)) {
                m_differenceDialog.setCopySource(copySource);
                m_differenceDialog.setOriginalSource(originalSource);
                m_differenceDialog.displayDialog();
            }
        }
    }

    /**
     * Displays the difference dialog.<p>
     * @throws Exception if something goes wrong
     */
    public void displayDifferenceDialog() throws Exception {

        m_differenceDialog.displayDialog();
    }

    /**
     * Converts an attribute list to a string.<p>
     * 
     * @param attributes a list of compared attributes to be converted to a string
     * @return a string respresentation of the attribute list
     */
    public String[] getAttributesAsString(List attributes) {

        Iterator i = attributes.iterator();
        StringBuffer res1 = new StringBuffer(512);
        StringBuffer res2 = new StringBuffer(512);
        while (i.hasNext()) {
            CmsAttributeComparison compare = (CmsAttributeComparison)i.next();
            res1.append(key(compare.getName())).append(": ").append(compare.getVersion1()).append("\n");
            res2.append(key(compare.getName())).append(": ").append(compare.getVersion2()).append("\n");
        }
        return new String[] {res1.toString(), res2.toString()};
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
     * Converts an attribute list to a string.<p>
     * 
     * @param properties a list of compared properties to be converted to a string
     * @return a string respresentation of the attribute list
     */
    public String[] getPropertiesAsString(List properties) {

        Iterator i = properties.iterator();
        StringBuffer res1 = new StringBuffer(512);
        StringBuffer res2 = new StringBuffer(512);
        while (i.hasNext()) {
            CmsAttributeComparison compare = (CmsAttributeComparison)i.next();
            res1.append(compare.getName()).append(": ").append(compare.getVersion1()).append("\n");
            res2.append(compare.getName()).append(": ").append(compare.getVersion2()).append("\n");
        }
        return new String[] {res1.toString(), res2.toString()};
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        try {
            getCms().getRequestContext().saveSiteRoot();
            getCms().getRequestContext().setSiteRoot("/");
            
            CmsFile file1;
            CmsFile file2;
            if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion1())) {
                file1 = getCms().readFile(getParamPath1());
            } else {
                file1 = getCms().readBackupFile(getParamPath1(),
                    Integer.parseInt(getParamTagId1()));
            }
            if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion2())) {
                file2 = getCms().readFile(getParamPath2());
            } else {
                file2 = getCms().readBackupFile(getParamPath2(),
                    Integer.parseInt(getParamTagId2()));
            }
            // if certain element is compared, use html difference dialog
            if (CmsStringUtil.isNotEmpty(getParamElement())) {
                m_differenceDialog = new CmsHtmlDifferenceDialog(getJsp());
            } else {
                m_differenceDialog = new CmsDifferenceDialog(getJsp());
            }
            if (CmsResourceComparisonDialog.COMPARE_ATTRIBUTES.equals(getParamCompare())) {
                List comparedAttributes = CmsResourceComparison.compareAttributes(getCms(), file1, file2);
                String[] attributeStrings = getAttributesAsString(comparedAttributes);
                m_differenceDialog.setOriginalSource(attributeStrings[0]);
                m_differenceDialog.setCopySource(attributeStrings[1]);
            } else if (CmsResourceComparisonDialog.COMPARE_PROPERTIES.equals(getParamCompare())) {
                List comparedProperties = CmsResourceComparison.compareProperties(getCms(), file1, file2);
                String[] propertyStrings = getPropertiesAsString(comparedProperties);
                m_differenceDialog.setOriginalSource(propertyStrings[0]);
                m_differenceDialog.setCopySource(propertyStrings[1]);
            } else {

                setContentAsSource(file1, file2);
            }

        } catch (CmsException e) {

            LOG.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {

            LOG.error(e.getMessage(), e);
        } finally {
            getCms().getRequestContext().restoreSiteRoot();
        }
    }

    /** 
     * Returns the content of all elements of an xml document appended.<p>
     * 
     * @param xmlDoc the xml document to extract the elements from
     * @return the content of all elements of an xml document appended
     */
    private String extractElements(I_CmsXmlDocument xmlDoc) {

        StringBuffer result = new StringBuffer();
        List locales = xmlDoc.getLocales();
        Iterator i = locales.iterator();
        boolean firstIter = true;
        while (i.hasNext()) {
            if (!firstIter) {
                result.append("\n\n-----");
            }
            Locale locale = (Locale)i.next();
            result.append("\n\n[");
            result.append(locale.toString()).append(']');
            List elements = xmlDoc.getValues(locale);
            Iterator j = elements.iterator();
            while (j.hasNext()) {
                I_CmsXmlContentValue value = (I_CmsXmlContentValue)j.next();
                result.append("\n\n[");
                if (xmlDoc instanceof CmsXmlPage) {
                    // output value of name attribute
                    result.append(value.getElement().attribute(0).getValue());
                } else {
                    // for xml content, output name of node
                    result.append(value.getName());
                }
                result.append("]\n\n");
                try {
                    result.append(value.getDocument().getContentDefinition().getContentHandler().getWidget(value).getWidgetStringValue(
                        getCms(),
                        new CmsFileInfoDialog(getJsp()),
                        (I_CmsWidgetParameter)value));
                } catch (CmsXmlException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            firstIter = false;
        }
        return result.toString();
    }

    private void setContentAsSource(CmsFile file1, CmsFile file2) throws CmsException, UnsupportedEncodingException {

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
                m_differenceDialog.setOriginalSource("");
            } else {
                m_differenceDialog.setOriginalSource(value1.getStringValue(cms));
            }
            if (value2 == null) {
                m_differenceDialog.setCopySource("");
            } else {
                m_differenceDialog.setCopySource(value2.getStringValue(cms));
            }
        } else if (CmsResourceComparisonDialog.COMPARE_ALL_ELEMENTS.equals(getParamCompare())) {

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
            m_differenceDialog.setOriginalSource(extractElements(resource1));
            m_differenceDialog.setCopySource(extractElements(resource2));

        } else {
            // compare whole plain text file
            m_differenceDialog.setOriginalSource(new String(file1.getContents(), cms.getRequestContext().getEncoding()));
            m_differenceDialog.setCopySource(new String(file2.getContents(), cms.getRequestContext().getEncoding()));
        }
    }
}
