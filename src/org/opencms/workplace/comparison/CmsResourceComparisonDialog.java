/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.extractors.CmsExtractorMsOfficeOLE2;
import org.opencms.search.extractors.CmsExtractorPdf;
import org.opencms.search.extractors.CmsExtractorRtf;
import org.opencms.search.extractors.I_CmsTextExtractor;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsResourceInfoDialog;
import org.opencms.workplace.list.CmsMultiListDialog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentValueVisitor;
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
 * @since 6.0.0 
 */
public class CmsResourceComparisonDialog extends CmsDialog {

    /**
     * Visitor that collects the xpath expressions of xml contents.<p>
     */
    class CmsXmlContentTextExtractor implements I_CmsXmlContentValueVisitor {

        /** The StringBuffer to write the extracted text to. */
        private StringBuffer m_buffer;

        /** The locales of the xml content. */
        private List m_locales;

        /**
         * Creates a new CmsXmlContentTextExtractor.<p>
         * 
         * @param stringBuffer the StringBuffer to append the element text to
         */
        CmsXmlContentTextExtractor(StringBuffer stringBuffer) {

            m_buffer = stringBuffer;
            m_locales = new ArrayList();
        }

        /**
         * 
         * @see org.opencms.xml.content.I_CmsXmlContentValueVisitor#visit(org.opencms.xml.types.I_CmsXmlContentValue)
         */
        public void visit(I_CmsXmlContentValue value) {

            // only add simple types
            if (value.isSimpleType()) {
                String locale = value.getLocale().toString();
                if (!m_locales.contains(locale)) {
                    m_buffer.append("\n\n[").append(locale).append(']');
                    m_locales.add(locale);
                }
                m_buffer.append("\n\n[").append(value.getPath()).append("]\n\n");
                try {
                    I_CmsWidget widget = value.getDocument().getContentDefinition().getContentHandler().getWidget(value);
                    m_buffer.append(widget.getWidgetStringValue(
                        getCms(),
                        new CmsResourceInfoDialog(getJsp()),
                        (I_CmsWidgetParameter)value));
                } catch (CmsXmlException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /** Constant indicating that all elements are compared.<p> */
    public static final String COMPARE_ALL_ELEMENTS = "allelements";

    /** Constant indicating that the attributes are compared.<p> */
    public static final String COMPARE_ATTRIBUTES = "attributes";

    /** Constant indicating that the properties are compared.<p> */
    public static final String COMPARE_PROPERTIES = "properties";

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsResourceComparisonDialog.class);

    /** The difference dialog. */
    private CmsDifferenceDialog m_differenceDialog;

    /** Parameter value indicating whether to compare properties, attributes or elements. */
    private String m_paramCompare;

    /** Parameter value for the element name. */
    private String m_paramElement;

    /** Parameter value for the structure id of the first file. */
    private String m_paramId1;

    /** Parameter value for the structure id of the second file. */
    private String m_paramId2;

    /** Parameter value for the locale. */
    private String m_paramLocale;

    /** Parameter value for the text mode. */
    private String m_paramTextmode;

    /** Parameter value for the version of the first file. */
    private String m_paramVersion1;

    /** Parameter value for the version of the second file. */
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
     * Returns either the historical file or the offline file, depending on the version number.<p>
     * 
     * @param cms the CmsObject to use
     * @param structureId the structure id of the file
     * @param version the historical version number
     * 
     * @return either the historical file or the offline file, depending on the version number
     * 
     * @throws CmsException if something goes wrong
     */
    protected static CmsFile readFile(CmsObject cms, CmsUUID structureId, String version) throws CmsException {

        if (Integer.parseInt(version) == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
            // offline
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return cms.readFile(resource);
        } else {
            int ver = Integer.parseInt(version);
            if (ver < 0) {
                // online
                CmsProject project = cms.getRequestContext().getCurrentProject();
                try {
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                    return cms.readFile(resource);
                } finally {
                    cms.getRequestContext().setCurrentProject(project);
                }
            }
            // backup
            return cms.readFile((CmsHistoryFile)cms.readResource(structureId, ver));
        }
    }

    /**
     * Returns either the historical resource or the offline resource, depending on the version number.<p>
     * 
     * @param cms the CmsObject to use
     * @param id the structure id of the resource
     * @param version the historical version number
     * 
     * @return either the historical resource or the offline resource, depending on the version number
     * 
     * @throws CmsException if something goes wrong
     */
    protected static CmsResource readResource(CmsObject cms, CmsUUID id, String version) throws CmsException {

        if (Integer.parseInt(version) == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
            return cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
        } else {
            int ver = Integer.parseInt(version);
            if (ver < 0) {
                CmsProject project = cms.getRequestContext().getCurrentProject();
                try {
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    return cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
                } finally {
                    cms.getRequestContext().setCurrentProject(project);
                }
            }
            return (CmsResource)cms.readResource(id, ver);
        }
    }

    /**
     * Display method for two list dialogs.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void displayDialog() throws Exception {

        CmsResourceInfoDialog fileInfo = new CmsResourceInfoDialog(getJsp()) {

            protected String defaultActionHtmlEnd() {

                return "";
            }
        };
        fileInfo.displayDialog(true);
        if (fileInfo.isForwarded()) {
            return;
        }

        CmsPropertyComparisonList propertyDiff = new CmsPropertyComparisonList(getJsp());
        CmsAttributeComparisonList attributeDiff = new CmsAttributeComparisonList(getJsp());
        List lists = new ArrayList();
        lists.add(attributeDiff);
        I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(propertyDiff.getResourceType());

        if ((resourceType instanceof CmsResourceTypeXmlContent) || (resourceType instanceof CmsResourceTypeXmlPage)) {

            // display attributes, properties and compared elements
            CmsElementComparisonList contentDiff = new CmsElementComparisonList(getJsp());
            lists.add(contentDiff);
            lists.add(propertyDiff);
            CmsMultiListDialog threeLists = new CmsMultiListDialog(lists);
            // perform the active list actions
            threeLists.displayDialog(true);
            // write the dialog just if no list has been forwarded
            if (threeLists.isForwarded()) {
                return;
            }
            fileInfo.writeDialog();
            threeLists.writeDialog();
        } else if (resourceType instanceof CmsResourceTypeImage) {

            // display attributes, properties and images
            lists.add(propertyDiff);
            CmsMultiListDialog twoLists = new CmsMultiListDialog(lists) {

                /**
                 * @see org.opencms.workplace.list.CmsMultiListDialog#defaultActionHtmlEnd()
                 */
                public String defaultActionHtmlEnd() {

                    return "";
                }
            };
            twoLists.displayDialog(true);
            if (twoLists.isForwarded()) {
                return;
            }
            CmsImageComparisonDialog images = new CmsImageComparisonDialog(getJsp());
            fileInfo.writeDialog();
            twoLists.writeDialog();
            // this is very dangerous
            // it is possible that here a forward is tried, what should not be sence we already wrote to the output stream.
            // CmsImageComparisonDialog should implement isForwarded, writeDialog and displayDialog(boolean) methods
            images.displayDialog();
        } else if (resourceType instanceof CmsResourceTypePointer) {

            lists.add(propertyDiff);
            CmsMultiListDialog twoLists = new CmsMultiListDialog(lists) {

                /**
                 * @see org.opencms.workplace.list.CmsMultiListDialog#defaultActionHtmlEnd()
                 */
                public String defaultActionHtmlEnd() {

                    return "";
                }
            };
            twoLists.displayDialog(true);
            if (twoLists.isForwarded()) {
                return;
            }
            CmsPointerComparisonDialog pointers = new CmsPointerComparisonDialog(getJsp());
            fileInfo.writeDialog();
            twoLists.writeDialog();
            // same as for CmsImageComparisonDialog
            pointers.displayDialog();
        } else if (propertyDiff.getResource1().isFile()) {

            // display attributes and properties 
            lists.add(propertyDiff);
            CmsMultiListDialog twoLists = new CmsMultiListDialog(lists);
            twoLists.displayDialog(true);
            if (twoLists.isForwarded()) {
                return;
            }

            String path1 = propertyDiff.getResource1().getRootPath();
            String path2 = propertyDiff.getResource2().getRootPath();

            byte[] content1 = readFile(getCms(), propertyDiff.getResource1().getStructureId(), getParamVersion1()).getContents();
            byte[] content2 = readFile(getCms(), propertyDiff.getResource2().getStructureId(), getParamVersion2()).getContents();

            String originalSource = null;
            String copySource = null;

            I_CmsTextExtractor textExtractor = null;
            if (path1.endsWith(".pdf") && path2.endsWith(".pdf")) {
                textExtractor = CmsExtractorPdf.getExtractor();
            } else if (path1.endsWith(".doc") && path2.endsWith(".doc")) {
                textExtractor = CmsExtractorMsOfficeOLE2.getExtractor();
            } else if (path1.endsWith(".xls") && path2.endsWith(".xls")) {
                textExtractor = CmsExtractorMsOfficeOLE2.getExtractor();
            } else if (path1.endsWith(".rtf") && path2.endsWith(".rtf")) {
                textExtractor = CmsExtractorRtf.getExtractor();
            } else if (path1.endsWith(".ppt") && path2.endsWith(".ppt")) {
                textExtractor = CmsExtractorMsOfficeOLE2.getExtractor();
            }
            if (textExtractor != null) {
                try {
                    // extract the content
                    originalSource = textExtractor.extractText(content1).getContent();
                    copySource = textExtractor.extractText(content2).getContent();
                } catch (Exception e) {
                    // something goes wrong on extracting content 
                    // set the content to null, so the content dialog will not be shown
                    originalSource = null;
                    copySource = null;
                    LOG.error(e.getMessage(), e);
                }
            } else if ((resourceType instanceof CmsResourceTypePlain) || (resourceType instanceof CmsResourceTypeJsp)) {
                originalSource = new String(content1);
                copySource = new String(content2);
            }
            fileInfo.writeDialog();
            twoLists.writeDialog();
            if (CmsStringUtil.isNotEmpty(originalSource) && CmsStringUtil.isNotEmpty(copySource)) {
                m_differenceDialog.setCopySource(copySource);
                m_differenceDialog.setOriginalSource(originalSource);
                // same as for CmsImageComparisonDialog
                m_differenceDialog.displayDialog();
            }
        } else {
            // display attributes and properties 
            lists.add(propertyDiff);
            CmsMultiListDialog twoLists = new CmsMultiListDialog(lists);
            twoLists.displayDialog(true);
            if (twoLists.isForwarded()) {
                return;
            }

            fileInfo.writeDialog();
            twoLists.writeDialog();
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
     * Returns the paramLocale.<p>
     *
     * @return the paramLocale
     */
    public String getParamLocale() {

        return m_paramLocale;
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
     * Sets the paramLocale.<p>
     *
     * @param paramLocale the paramLocale to set
     */
    public void setParamLocale(String paramLocale) {

        m_paramLocale = paramLocale;
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
            CmsResource resource1 = CmsResourceComparisonDialog.readResource(
                getCms(),
                new CmsUUID(getParamId1()),
                getParamVersion1());
            CmsResource resource2 = CmsResourceComparisonDialog.readResource(
                getCms(),
                new CmsUUID(getParamId2()),
                getParamVersion2());
            // if certain element is compared, use html difference dialog
            if (CmsStringUtil.isNotEmpty(getParamElement())) {
                m_differenceDialog = new CmsHtmlDifferenceDialog(getJsp());
            } else {
                m_differenceDialog = new CmsDifferenceDialog(getJsp());
            }
            if (CmsResourceComparisonDialog.COMPARE_ATTRIBUTES.equals(getParamCompare())) {
                List comparedAttributes = CmsResourceComparison.compareAttributes(getCms(), resource1, resource2);
                String[] attributeStrings = getAttributesAsString(comparedAttributes);
                m_differenceDialog.setOriginalSource(attributeStrings[0]);
                m_differenceDialog.setCopySource(attributeStrings[1]);
            } else if (CmsResourceComparisonDialog.COMPARE_PROPERTIES.equals(getParamCompare())) {
                List comparedProperties = CmsResourceComparison.compareProperties(
                    getCms(),
                    resource1,
                    getParamVersion1(),
                    resource2,
                    getParamVersion2());
                String[] propertyStrings = getPropertiesAsString(comparedProperties);
                m_differenceDialog.setOriginalSource(propertyStrings[0]);
                m_differenceDialog.setCopySource(propertyStrings[1]);
            } else if (resource1.isFile()) {
                CmsFile file1 = readFile(getCms(), new CmsUUID(getParamId1()), getParamVersion1());
                CmsFile file2 = readFile(getCms(), new CmsUUID(getParamId2()), getParamVersion2());
                setContentAsSource(file1, file2);
            }
        } catch (CmsException e) {
            LOG.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
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
        if (xmlDoc instanceof CmsXmlPage) {
            List locales = xmlDoc.getLocales();
            Iterator i = locales.iterator();
            boolean firstIter = true;
            while (i.hasNext()) {
                if (!firstIter) {
                    result.append("\n\n-----");
                }
                Locale locale = (Locale)i.next();
                result.append("\n\n[").append(locale.toString()).append(']');
                List elements = xmlDoc.getValues(locale);
                Iterator j = elements.iterator();
                while (j.hasNext()) {
                    I_CmsXmlContentValue value = (I_CmsXmlContentValue)j.next();
                    result.append("\n\n[");
                    // output value of name attribute
                    result.append(value.getElement().attribute(0).getValue());
                    result.append("]\n\n");
                    try {
                        I_CmsWidget widget = value.getDocument().getHandler().getWidget(value);
                        result.append(widget.getWidgetStringValue(
                            getCms(),
                            new CmsResourceInfoDialog(getJsp()),
                            (I_CmsWidgetParameter)value));
                    } catch (CmsXmlException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                firstIter = false;
            }
        } else if (xmlDoc instanceof CmsXmlContent) {
            CmsXmlContentTextExtractor visitor = new CmsXmlContentTextExtractor(result);
            ((CmsXmlContent)xmlDoc).visitAllValuesWith(visitor);

        }
        return result.toString();
    }

    /**
     * Extracts the content from the files according to the file type.<p>
     * 
     * @param file1 the first file to compare
     * @param file2 the second file to compare
     * 
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException if the encoding of one file is not supported
     */
    private void setContentAsSource(CmsFile file1, CmsFile file2) throws CmsException, UnsupportedEncodingException {

        CmsObject cms = getCms();
        if (CmsStringUtil.isNotEmpty(getParamElement())) {
            I_CmsXmlDocument resource1;
            I_CmsXmlDocument resource2;
            if (CmsResourceTypeXmlPage.isXmlPage(file1)) {
                resource1 = CmsXmlPageFactory.unmarshal(cms, file1);
            } else {
                resource1 = CmsXmlContentFactory.unmarshal(cms, file1);
            }
            if (CmsResourceTypeXmlPage.isXmlPage(file2)) {
                resource2 = CmsXmlPageFactory.unmarshal(cms, file2);
            } else {
                resource2 = CmsXmlContentFactory.unmarshal(cms, file2);
            }
            I_CmsXmlContentValue value1 = resource1.getValue(
                getParamElement(),
                CmsLocaleManager.getLocale(getParamLocale()));
            I_CmsXmlContentValue value2 = resource2.getValue(
                getParamElement(),
                CmsLocaleManager.getLocale(getParamLocale()));
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
            if (CmsResourceTypeXmlPage.isXmlPage(file1)) {
                resource1 = CmsXmlPageFactory.unmarshal(cms, file1);
            } else {
                resource1 = CmsXmlContentFactory.unmarshal(cms, file1);
            }
            if (CmsResourceTypeXmlPage.isXmlPage(file2)) {
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
