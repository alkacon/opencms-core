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

import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.shared.CmsDialogOptions;
import org.opencms.ade.containerpage.shared.CmsDialogOptions.Option;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.serialdate.CmsSerialDateBeanFactory;
import org.opencms.widgets.serialdate.CmsSerialDateValue;
import org.opencms.widgets.serialdate.I_CmsSerialDateBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlSerialDateValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;

/** Special edit handler for contents that define multiple instances in a date series. */
public class CmsDateSeriesEditHandler implements I_CmsEditHandler {

    /** Handler, that does the real work. We use an internal handler to allow for state. */
    private static class InternalHandler {

        /** Logger for the class. */
        private static final Log LOG = CmsLog.getLog(InternalHandler.class);

        /** Option: Edit / delete the only a single item instance of the series. */
        private static final String OPTION_INSTANCE = "instance";

        /** Edit option: Edit the whole series. */
        private static final String OPTION_SERIES = "series";

        /** The cms object with the current context. */
        private CmsObject m_cms;

        /** The content that should be edited/deleted. */
        private CmsXmlContent m_content;

        /** The content value that holds the definition of the date series. */
        private I_CmsXmlContentValue m_contentValue;

        /** The edited container page element. */
        private CmsContainerElementBean m_elementBean;

        /** The file of the content that should be edited/deleted. */
        private CmsFile m_file;

        /** The date of the current instance of the series. */
        private Date m_instanceDate;

        /** UUID of the container page we currently act on. */
        private CmsUUID m_pageContextId;

        /** The current request parameters. */
        private Map<String, String[]> m_requestParameters;

        /** The date series as defined in the content. */
        private I_CmsSerialDateBean m_series;

        /** The definition of the date series from the content. */
        private CmsSerialDateValue m_value;

        /**
         * Constructor for the internal handler, basically taking the information that is provided in all methods of {@link I_CmsEditHandler} to do the common initialization.
         * @param cms the current cms object.
         * @param elementBean the currently edited container page element
         * @param requestParams the current request parameters
         * @param pageContextId the structure id of the container page where editing takes place
         */
        public InternalHandler(
            CmsObject cms,
            CmsContainerElementBean elementBean,
            Map<String, String[]> requestParams,
            CmsUUID pageContextId) {

            try {
                m_cms = cms;
                m_elementBean = elementBean;
                m_requestParameters = requestParams;
                m_pageContextId = pageContextId;
                elementBean.initResource(cms);
                CmsResource res = elementBean.getResource();
                m_file = cms.readFile(res);
                m_content = CmsXmlContentFactory.unmarshal(cms, m_file);
                m_contentValue = getSerialDateContentValue(m_content, null);
                m_value = m_contentValue != null ? new CmsSerialDateValue(m_contentValue.getStringValue(cms)) : null;
                m_series = CmsSerialDateBeanFactory.createSerialDateBean(m_value);
                setInstanceDate();

            } catch (Exception e) {
                LOG.error("Failed to determine all information to edit the instance of the date series.", e);
            }
        }

        /**
         * Returns the options for the delete dialog or <code>null</code> if no special options are available.
         * @return the options for the delete dialog or <code>null</code> if no special options are available.
         */
        public CmsDialogOptions getDeleteOptions() {

            if (null != m_instanceDate) {
                Locale wpl = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
                CmsMessages messages = Messages.get().getBundle(wpl);
                if (!m_value.getPatternType().equals(PatternType.NONE)) {
                    List<Option> options = new ArrayList<>(2);
                    String instanceDate = DateFormat.getDateInstance(DateFormat.LONG, wpl).format(m_instanceDate);
                    Option oInstance = new Option(
                        OPTION_INSTANCE,
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_DELETE_OPTION_INSTANCE_1, instanceDate),
                        messages.key(
                            Messages.GUI_DATE_SERIES_HANDLER_DELETE_OPTION_INSTANCE_HELP_ACTIVE_1,
                            instanceDate),
                        false);
                    options.add(oInstance);
                    Option oSeries = new Option(
                        CmsDialogOptions.REGULAR_DELETE,
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_DELETE_OPTION_SERIES_0),
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_DELETE_OPTION_SERIES_HELP_ACTIVE_0),
                        false);
                    options.add(oSeries);
                    return new CmsDialogOptions(
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_DELETE_DIALOG_HEADING_0),
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_DELETE_DIALOG_INFO_1, getTitle(wpl)),
                        options);
                }
            }
            return null;

        }

        /**
         * Returns the options for the edit dialog or <code>null</code> if no special options are available.
         * @param isListElement flag, indicating if the edited element is in a list.
         * @return the options for the edit dialog or <code>null</code> if no special options are available.
         */
        public CmsDialogOptions getEditOptions(boolean isListElement) {

            if (null != m_instanceDate) {
                Locale wpl = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
                CmsMessages messages = Messages.get().getBundle(wpl);
                if (!m_value.getPatternType().equals(PatternType.NONE)) {
                    List<Option> options = new ArrayList<>(2);
                    String instanceDate = DateFormat.getDateInstance(DateFormat.LONG, wpl).format(m_instanceDate);
                    Option oInstance;
                    if (!isListElement && !isContainerPageLockable()) {
                        oInstance = new Option(
                            OPTION_INSTANCE,
                            messages.key(Messages.GUI_DATE_SERIES_HANDLER_EDIT_OPTION_INSTANCE_1, instanceDate),
                            messages.key(
                                Messages.GUI_DATE_SERIES_HANDLER_EDIT_OPTION_INSTANCE_HELP_INACTIVE_1,
                                instanceDate),
                            true);
                    } else {
                        oInstance = new Option(
                            OPTION_INSTANCE,
                            messages.key(Messages.GUI_DATE_SERIES_HANDLER_EDIT_OPTION_INSTANCE_1, instanceDate),
                            messages.key(
                                Messages.GUI_DATE_SERIES_HANDLER_EDIT_OPTION_INSTANCE_HELP_ACTIVE_1,
                                instanceDate),
                            false);
                    }
                    options.add(oInstance);
                    Option oSeries = new Option(
                        OPTION_SERIES,
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_EDIT_OPTION_SERIES_0),
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_EDIT_OPTION_SERIES_HELP_ACTIVE_0),
                        false);
                    options.add(oSeries);
                    return new CmsDialogOptions(
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_EDIT_DIALOG_HEADING_0),
                        messages.key(Messages.GUI_DATE_SERIES_HANDLER_EDIT_DIALOG_INFO_1, getTitle(wpl)),
                        options);
                }
            }
            return null;

        }

        /**
         * Handles a delete operation.
         * @param deleteOption the delete option.
         * @throws CmsException thrown if deletion fails.
         */
        public void handleDelete(String deleteOption) throws CmsException {

            if (Objects.equals(deleteOption, OPTION_INSTANCE)) {
                addExceptionForInstance();
            } else {
                throw new CmsException(
                    new CmsMessageContainer(
                        Messages.get(),
                        Messages.ERR_DATE_SERIES_HANDLER_INVALID_DELETE_OPTION_1,
                        deleteOption));
            }

        }

        /**
         * Handles the preparation of the edit action, except for the series case, since in that case no specific handling is necessary.
         * @param editOption the edit option.
         * @return the structure id of the content that should be edited.
         * @throws CmsException thrown if preparing the edit operation fails.
         */
        public CmsUUID prepareForEdit(String editOption) throws CmsException {

            if (Objects.equals(OPTION_INSTANCE, editOption)) {
                return extractDate();
            } else {
                throw new CmsException(
                    new CmsMessageContainer(
                        Messages.get(),
                        Messages.ERR_DATE_SERIES_HANDLER_INVALID_EDIT_OPTION_1,
                        editOption));
            }

        }

        /**
         * Adds an exception in the series content and writes the changed content back to the file.
         * @throws CmsException thrown if something goes wrong.
         */
        private void addExceptionForInstance() throws CmsException {

            if (null != m_instanceDate) {
                try {
                    m_cms.lockResource(m_file);
                    m_value.addException(m_instanceDate);
                    String stringValue = m_value.toString();
                    for (Locale l : m_content.getLocales()) {
                        I_CmsXmlContentValue contentValue = getSerialDateContentValue(m_content, l);
                        contentValue.setStringValue(m_cms, stringValue);
                    }
                    m_file.setContents(m_content.marshal());
                    m_cms.writeFile(m_file);
                    m_cms.unlockResource(m_file);
                } catch (Exception e) {
                    throw new CmsException(
                        new CmsMessageContainer(
                            Messages.get(),
                            Messages.ERR_DATE_SERIES_HANDLER_ADD_EXCEPTION_FAILED_0),
                        e);
                }
            } else {
                throw new CmsException(
                    new CmsMessageContainer(
                        Messages.get(),
                        Messages.ERR_DATE_SERIES_HANDLER_ADD_EXCEPTION_FAILED_MISSING_DATE_0));
            }
        }

        /**
         * Creates a copy of the series content and adjusts the dates in the copy. As well, adds an exception to the original series content.
         * @return the structure id of the newly created content.
         * @throws CmsException thrown if something goes wrong.
         */
        private CmsUUID extractDate() throws CmsException {

            if (null != m_instanceDate) {
                try {
                    CmsResource page = m_cms.readResource(m_pageContextId);
                    CmsResourceTypeConfig typeConfig = OpenCms.getADEManager().lookupConfiguration(
                        m_cms,
                        page.getRootPath()).getResourceType(
                            OpenCms.getResourceManager().getResourceType(m_file).getTypeName());
                    String pattern = typeConfig.getNamePattern(true);
                    String newSitePath = OpenCms.getResourceManager().getNameGenerator().getNewFileName(
                        m_cms,
                        CmsResource.getFolderPath(m_file.getRootPath()) + pattern,
                        5);
                    String oldSitePath = m_cms.getSitePath(m_file);
                    m_cms.copyResource(oldSitePath, newSitePath);
                    CmsFile newFile = m_cms.readFile(newSitePath);
                    CmsXmlContent newContent = CmsXmlContentFactory.unmarshal(m_cms, newFile);
                    CmsSerialDateValue newValue = new CmsSerialDateValue();
                    newValue.setStart(m_instanceDate);
                    if ((m_value.getEnd() != null) && (m_series.getEventDuration() != null)) {
                        newValue.setEnd(new Date(m_instanceDate.getTime() + m_series.getEventDuration().longValue()));
                    }
                    newValue.setParentSeriesId(m_file.getStructureId());
                    newValue.setWholeDay(Boolean.valueOf(m_value.isWholeDay()));
                    newValue.setPatternType(PatternType.NONE);
                    String newValueString = newValue.toString();
                    for (Locale l : newContent.getLocales()) {
                        I_CmsXmlContentValue newContentValue = getSerialDateContentValue(newContent, l);
                        newContentValue.setStringValue(m_cms, newValueString);
                    }
                    newFile.setContents(newContent.marshal());
                    m_cms.writeFile(newFile);
                    m_cms.unlockResource(newFile);

                    addExceptionForInstance();

                    return newFile.getStructureId();
                } catch (Exception e) {
                    throw new CmsException(
                        new CmsMessageContainer(
                            Messages.get(),
                            Messages.ERR_DATE_SERIES_HANDLER_EXTRACT_CONTENT_FAILED_0),
                        e);
                }
            } else {
                throw new CmsException(
                    new CmsMessageContainer(
                        Messages.get(),
                        Messages.ERR_DATE_SERIES_HANDLER_EXTRACT_CONTENT_FAILED_MISSING_DATE_0));
            }
        }

        /**
         * Returns the content value that contains the date series definition.
         * @param content the XML content to read the value from.
         * @param locale the locale to get the value in.
         * @return the content value that contains the date series definition.
         */
        private I_CmsXmlContentValue getSerialDateContentValue(CmsXmlContent content, Locale locale) {

            if ((null == locale) && !content.getLocales().isEmpty()) {
                locale = content.getLocales().get(0);
            }
            for (I_CmsXmlContentValue value : content.getValues(locale)) {
                if (value.getTypeName().equals(CmsXmlSerialDateValue.TYPE_NAME)) {
                    return value;
                }
            }
            return null;
        }

        /**
         * Returns the gallery title of the series content.
         * @param l the locale to show the title in.
         * @return the gallery title of the series content.
         */
        private String getTitle(Locale l) {

            CmsGallerySearchResult result;
            try {
                result = CmsGallerySearch.searchById(m_cms, m_contentValue.getDocument().getFile().getStructureId(), l);
                return result.getTitle();
            } catch (CmsException e) {
                LOG.error("Could not retrieve title of series content.", e);
                return "";
            }

        }

        /**
         * Checks, if the container page the edit operation takes place on can be locked by the current user.
         * @return a flag, indicating if the page can be locked by the current user.
         */
        private boolean isContainerPageLockable() {

            try {
                return m_cms.getLock(m_cms.readResource(m_pageContextId)).isLockableBy(
                    m_cms.getRequestContext().getCurrentUser());
            } catch (Exception e) {
                LOG.error("Failed to check if the container page is lockable by the current user.", e);
                return false;
            }
        }

        /**
         * Sets the date of the currently edited instance of the series.
         */
        private void setInstanceDate() {

            String sl = null;
            Map<String, String> settings = m_elementBean.getSettings();
            if (settings.containsKey(PARAM_INSTANCEDATE)) {
                sl = settings.get(PARAM_INSTANCEDATE);
            } else if (m_requestParameters.containsKey(PARAM_INSTANCEDATE)) {
                String[] sls = m_requestParameters.get(PARAM_INSTANCEDATE);
                if ((sls != null) && (sls.length > 0)) {
                    sl = sls[0];
                }
            }
            if (sl != null) {
                try {
                    long l = Long.parseLong(sl);
                    Date d = new Date(l);
                    if (m_series.getDates().contains(d)) {
                        m_instanceDate = d;
                    } else {
                        throw new Exception("Instance date is not a date of the series.");
                    }
                } catch (Exception e) {
                    LOG.error(
                        "Could not read valid date from setting or request parameter \"" + PARAM_INSTANCEDATE + "\".",
                        e);
                }
            } else {
                // TODO: Handle possible other element settings that could determine the actual instance date.
            }
        }

    }

    /** The key of the parameter/setting the instance date of the instance that should be edited is read from. */
    public static final String PARAM_INSTANCEDATE = "instancedate";

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsEditHandler#getDeleteOptions(org.opencms.file.CmsObject, org.opencms.xml.containerpage.CmsContainerElementBean, org.opencms.util.CmsUUID, java.util.Map)
     */
    @Override
    public CmsDialogOptions getDeleteOptions(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams) {

        InternalHandler internalHandler = new InternalHandler(cms, elementBean, requestParams, pageContextId);
        return internalHandler.getDeleteOptions();
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsEditHandler#getEditOptions(org.opencms.file.CmsObject, org.opencms.xml.containerpage.CmsContainerElementBean, org.opencms.util.CmsUUID, java.util.Map, boolean)
     */
    @Override
    public CmsDialogOptions getEditOptions(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams,
        boolean isListElement) {

        InternalHandler internalHandler = new InternalHandler(cms, elementBean, requestParams, pageContextId);
        return internalHandler.getEditOptions(isListElement);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsEditHandler#getNewOptions(org.opencms.file.CmsObject, org.opencms.xml.containerpage.CmsContainerElementBean, org.opencms.util.CmsUUID, java.util.Map)
     */
    public CmsDialogOptions getNewOptions(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        CmsUUID pageContextId,
        Map<String, String[]> requestParam) {

        return null;
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsEditHandler#handleDelete(org.opencms.file.CmsObject, org.opencms.xml.containerpage.CmsContainerElementBean, java.lang.String, org.opencms.util.CmsUUID, java.util.Map)
     */
    @Override
    public void handleDelete(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        String deleteOption,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams)
    throws CmsException {

        InternalHandler internalHandler = new InternalHandler(cms, elementBean, requestParams, pageContextId);

        internalHandler.handleDelete(deleteOption);

    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsEditHandler#handleNew(org.opencms.file.CmsObject, java.lang.String, java.util.Locale, java.lang.String, java.lang.String, java.lang.String, org.opencms.xml.containerpage.CmsContainerElementBean, org.opencms.util.CmsUUID, java.util.Map, java.lang.String)
     */
    public String handleNew(
        CmsObject cms,
        String newLink,
        Locale locale,
        String referenceSitePath,
        String modelFileSitePath,
        String postCreateHandler,
        CmsContainerElementBean element,
        CmsUUID pageId,
        Map<String, String[]> requestParams,
        String choice) {

        return null;
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsEditHandler#prepareForEdit(org.opencms.file.CmsObject, org.opencms.xml.containerpage.CmsContainerElementBean, java.lang.String, org.opencms.util.CmsUUID, java.util.Map)
     */
    @Override
    public CmsUUID prepareForEdit(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        String editOption,
        CmsUUID pageContextId,
        Map<String, String[]> requestParams)
    throws CmsException {

        if (Objects.equals(InternalHandler.OPTION_SERIES, editOption)) {
            return elementBean.getId();
        }
        InternalHandler internalHandler = new InternalHandler(cms, elementBean, requestParams, pageContextId);
        return internalHandler.prepareForEdit(editOption);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsEditHandler#setParameters(java.util.Map)
     */
    public void setParameters(Map<String, String> params) {
        // this handler doesn't need parameters
    }
}
