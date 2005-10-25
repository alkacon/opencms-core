/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheckProperty.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.I_CmsToolHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

/**
 * This implementation of the I_CmsContentCheck interface implments a check for 
 * resource properties.<p>
 * 
 * The following items can be configured and checked:
 * <ul>
 * <li>Property not set</li>
 * <li>Property value contains filename</li>
 * <li>Property value is shorter than a minimum size</li>
 * <li>Property value contains a given value (with RegEx)</li>
 * <li>Property value does not contain a given value (with RegEx)</li>
 * </ul>
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.2
 */
public class CmsContentCheckProperty extends A_CmsContentCheck implements I_CmsContentCheck, I_CmsToolHandler {

    /** Path to the configuration file. */
    private static final String CONFIGURATION = CmsContentCheck.VFS_PATH_PLUGIN_FOLDER
        + "propertycheck/configuration.xml";

    /** Name of the dialog parameter. */
    private static final String DIALOG_PARAMETER = "property";

    /** Path to the configuration icon. */
    private static final String ICONPATH = "tools/contenttools/icons/big/contentcheck_property_configuration.png";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContentCheckProperty.class);

    /** Name of this content check. */
    private static final String NAME = "Property Check";

    /** The xpath for the empty configuration. */
    private static final String XPATH_EMPTY = "empty";

    /** The xpath for the error configuration. */
    private static final String XPATH_ERROR = "error";

    /** The xpath for the filename configuration. */
    private static final String XPATH_FILENAME = "filename";

    /** The xpath for the length configuration. */
    private static final String XPATH_LENGTH = "length";

    /** The xpath for the propertyname configuration. */
    private static final String XPATH_PROPERTYNAME = "propertyname";

    /** The xpath for the type configuration. */
    private static final String XPATH_TYPE = "type";

    /** The xpath for the value configuration. */
    private static final String XPATH_VALUE = "value";

    /** The xpath for the warning configuration. */
    private static final String XPATH_WARNUING = "warning";

    /** The active flag, signaling if this content check is active. */
    private boolean m_active = true;

    /** The CmsObject. */
    private CmsObject m_cms;

    /** List of all configured error checks. */
    private List m_configuredErrorChecks;

    /** List of all configured warning checks. */
    private List m_configuredWarningChecks;

    /** Locale to be used to extrace xml content. */
    private Locale m_locale;

    /**
     * 
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#executeContentCheck(org.opencms.file.CmsObject, org.opencms.workplace.tools.content.check.CmsContentCheckResource)
     */
    public CmsContentCheckResource executeContentCheck(CmsObject cms, CmsContentCheckResource testResource)
    throws CmsException {

        getConfiguration();
        // check for errors
        List errors = processProperties(m_configuredErrorChecks, testResource);
        if (errors.size() > 0) {
            testResource.addErrors(errors);
        }
        // check for warnings
        List warnings = processProperties(m_configuredWarningChecks, testResource);
        if (warnings.size() > 0) {
            testResource.addWarnings(warnings);
        }
        return testResource;
    }

    /**
     * 
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#getDialogParameterName()
     */
    public String getDialogParameterName() {

        return DIALOG_PARAMETER;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getHelpText()
     */
    public String getHelpText() {

        return Messages.get().key(Messages.GUI_CHECKCONTENT_CONFIGURATION_PROPERTY_HELP_0);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getIconPath()
     */
    public String getIconPath() {

        return ICONPATH;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getLink()
     */
    public String getLink() {

        return "/system/workplace/views/admin/admin-editor.html?resource=/system/workplace/admin/contenttools/check/plugin/propertycheck/configuration.xml";

        // String editor = "/system/workplace/editors/editor.jsp?resource=";
        // String resource = CmsEncoder.encode("/system/workplace/admin/contenttools/check/plugin/propertycheck/configuration.xml");

    }

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#getMessageBundles()
     */
    public List getMessageBundles() {

        List messages = new ArrayList();
        messages.add(org.opencms.workplace.tools.content.check.Messages.get().getBundleName());
        return messages;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getName()
     */
    public String getName() {

        return NAME;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPath()
     */
    public String getPath() {

        return "/contenttools/checkconfig/checkproperty";
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPosition()
     */
    public float getPosition() {

        return 1;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getShortName()
     */
    public String getShortName() {

        return NAME;
    }

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#init(org.opencms.file.CmsObject)
     */
    public void init(CmsObject cms) {

        m_cms = cms;
        m_locale = m_cms.getRequestContext().getLocale();
    }

    /**
     * Gets the active flag.<p>
     * 
     * @return true if this content check is active, false otherwise.
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * Sets the active flag.<p>
     * 
     * This method is required to build the widget dialog frontend.
     *
     * @param value true if this content check is set to be active, false otherwise.
     */
    public void setActive(boolean value) {

        m_active = value;
    }

    /**
     * Gets the configuration of the property check.<p>
     *
     *@throws CmsException if an error occurs reading the configuration
     */
    private void getConfiguration() throws CmsException {

        // get the configuration file
        CmsResource res = m_cms.readResource(CONFIGURATION);
        CmsFile file = CmsFile.upgrade(res, m_cms);
        CmsXmlContent configuration = CmsXmlContentFactory.unmarshal(m_cms, file);

        // now extract the configured error checks from it
        m_configuredErrorChecks = getConfiguredChecks(configuration, XPATH_ERROR);
        m_configuredWarningChecks = getConfiguredChecks(configuration, XPATH_WARNUING);
    }

    /**
     * Reads the configuration for a given xpath and stored all results in a list.<p>
     * 
     * @param configuration the configuration to read from
     * @param xpath the xpath prefix
     * @return list of CmsContentCheckProperetyObject objects
     */
    private List getConfiguredChecks(CmsXmlContent configuration, String xpath) {

        List checks = new ArrayList();
        int size = configuration.getIndexCount(xpath, m_locale);
        for (int i = 1; i <= size; i++) {
            // extract the values from the configuration
            String propertyname = configuration.getValue(xpath + "[" + i + "]/" + XPATH_PROPERTYNAME, m_locale).getStringValue(
                m_cms);
            String type = configuration.getValue(xpath + "[" + i + "]/" + XPATH_TYPE, m_locale).getStringValue(m_cms);
            String empty = configuration.getValue(xpath + "[" + i + "]/" + XPATH_EMPTY, m_locale).getStringValue(m_cms);
            String filename = configuration.getValue(xpath + "[" + i + "]/" + XPATH_FILENAME, m_locale).getStringValue(
                m_cms);
            String length = configuration.getValue(xpath + "[" + i + "]/" + XPATH_LENGTH, m_locale).getStringValue(
                m_cms);
            int values = configuration.getIndexCount(xpath + "[" + i + "]/" + XPATH_VALUE, m_locale);

            //String value = configuration.getValue(xpath + "[" + i + "]/" + XPATH_VALUE, m_locale).getStringValue(m_cms);

            // store them in the CmsContentCheckProperetyObject obejct for fürther processing
            CmsContentCheckProperetyObject propObject = new CmsContentCheckProperetyObject();

            if (CmsStringUtil.isNotEmpty(propertyname)) {
                propObject.setPropertyname(propertyname);
            }
            if (CmsStringUtil.isNotEmpty(type)) {
                propObject.setType(type);
            }
            if (CmsStringUtil.isNotEmpty(empty)) {
                propObject.setEmpty(empty.equals("true"));
            }
            if (CmsStringUtil.isNotEmpty(filename)) {
                propObject.setFilename(filename.equals("true"));
            }
            if (CmsStringUtil.isNotEmpty(length)) {
                propObject.setLength(new Integer(length).intValue());
            }
            if (values > 0) {
                List valueList = new ArrayList();
                for (int j = 1; j <= values; j++) {
                    String value = configuration.getValue(
                        xpath + "[" + i + "]/" + XPATH_VALUE + "[" + j + "]",
                        m_locale).getStringValue(m_cms);
                    if (CmsStringUtil.isNotEmpty(value)) {
                        valueList.add(value);
                    }
                }
                propObject.setValue(valueList);
            }

            checks.add(propObject);
        }
        return checks;
    }

    /**
     * Processes a list of CmsContentCheckProperetyObject and runs all available tests on them.<p>
     * 
     * All errors or warnings found are collected in a list returned to the calling method.
     * 
     * @param properties list of CmsContentCheckProperetyObject to process
     * @param testResource the CmsContentCheckResource to run all tests on
     * @return list of Strings containing either errors or warinings
     */
    private List processProperties(List properties, CmsContentCheckResource testResource) {

        List results = new ArrayList();

        //loop through all property tests
        for (int i = 0; i < properties.size(); i++) {
            try {
                CmsContentCheckProperetyObject propObject = (CmsContentCheckProperetyObject)properties.get(i);

                // check if this test must be done for thies kind of resource
                if ((propObject.getType().equals(CmsContentCheckProperetyObject.TYPE_BOTH))
                    || ((propObject.getType().equals(CmsContentCheckProperetyObject.TYPE_FILE) && (testResource.getResource().isFile())))
                    || ((propObject.getType().equals(CmsContentCheckProperetyObject.TYPE_FOLDER) && (testResource.getResource().isFolder())))

                ) {

                    // read the property
                    String prop = m_cms.readPropertyObject(
                        testResource.getResource(),
                        propObject.getPropertyname(),
                        false).getValue();

                    // test if the property is empty
                    if (propObject.isEmpty() && CmsStringUtil.isEmpty(prop)) {
                        results.add(Messages.get().key(Messages.CHECK_NO_PROPERTYNAME_1, propObject.getPropertyname()));
                    }

                    // test if the property does not start with the filename
                    if (!CmsStringUtil.isEmpty(prop)) {
                        if (propObject.isFilename()
                            && testResource.getResource().getName().toLowerCase().startsWith(prop.toLowerCase())) {
                            results.add(Messages.get().key(
                                Messages.CHECK_CONTAINS_FILENAME_2,
                                propObject.getPropertyname(),
                                prop));
                        }

                        // test if the minmal property length is valid
                        if (propObject.getLength() > -1) {
                            if (prop.length() < propObject.getLength()) {
                                results.add(Messages.get().key(
                                    Messages.CHECK_TOO_SHORT_3,
                                    propObject.getPropertyname(),
                                    prop,
                                    new Integer(prop.length())));
                            }
                        }

                        // test if the value matches a regex
                        if (propObject.getValue().size() > 0) {
                            for (int j = 0; j < propObject.getValue().size(); j++) {

                                String regex = new String((String)propObject.getValue().get(j));

                                boolean matchResult = true;
                                if (regex.charAt(0) == '!') {
                                    // negate the pattern
                                    matchResult = false;
                                    regex = regex.substring(1);
                                }
                                String matchValue = prop;
                                boolean match = Pattern.matches(regex, matchValue);
                                if (matchResult != match) {
                                    results.add(Messages.get().key(
                                        Messages.CHECK_MATCH_3,
                                        propObject.getPropertyname(),
                                        prop,
                                        propObject.getValue().get(j)));
                                }
                            }
                        }
                    }
                }

            } catch (CmsException e) {
                LOG.error(Messages.get().key(
                    Messages.LOG_ERROR_PROCESSING_PROPERTIES_2,
                    testResource.getResourceName(),
                    e));
            }
        }

        return results;
    }

}
