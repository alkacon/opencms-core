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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListCsvExportIAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMetadata.I_CsvItemFormatter;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Group dependencies list view.<p>
 */
public class CmsAllPrincipalDependenciesList extends A_CmsListDialog {

    /**
     * Helper class for generating the list entries.
     */
    protected static class ElementGenerator {

        /** The access control entry cache. */
        private Multimap<CmsUUID, CmsAccessControlEntry> m_ace;

        /** The CMS object used for file operations. */
        private CmsObject m_cms;

        /** The generated list entries. */
        private List<String[]> m_entries = new ArrayList<String[]>();

        /**
         * Creates a new instance.<p>
         *
         * @param cms the CMS context to use for file operations
         *
         * @throws CmsException if something goes wrong
         */
        public ElementGenerator(CmsObject cms)
        throws CmsException {

            m_cms = OpenCms.initCmsObject(cms);
            m_cms.getRequestContext().setSiteRoot("");
        }

        /**
         * Generates the list entries.<p>
         *
         * @return a list of string arrays of the form ( name, credential, permissions, path )
         *
         * @throws CmsException if something goes wrong
         */
        public List<String[]> generateEntries() throws CmsException {

            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
            addDirectEntries(
                CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME,
                locale);
            addDirectEntries(
                CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
                locale);
            for (CmsUser user : getUsers()) {
                CmsUUID userId = user.getId();
                String name = user.getName();
                addDirectEntries(userId, name, locale);
                for (CmsGroup group : m_cms.getGroupsOfUser(user.getName(), false)) {
                    for (CmsAccessControlEntry ace : getAces(group.getId())) {
                        for (CmsResource resource : getResources(ace.getResource())) {
                            String credentials = Messages.get().getBundle(locale).key(
                                Messages.GUI_CREDENTIAL_GROUP_1,
                                group.getName());
                            addEntry(
                                user.getName(),
                                credentials,
                                getAceString(ace),

                                resource.getRootPath());

                        }
                    }
                }
                Set<CmsUUID> processedRoles = new HashSet<CmsUUID>();
                List<CmsRole> roles = OpenCms.getRoleManager().getRolesOfUser(
                    m_cms,
                    user.getName(),
                    "",
                    true,
                    false,
                    true);
                for (CmsRole role : roles) {
                    // only generate one entry for each role, independent of the number of OUs
                    if (processedRoles.contains(role.getId())) {
                        continue;
                    } else {
                        processedRoles.add(role.getId());
                    }
                    for (CmsAccessControlEntry ace : getAces(role.getId())) {
                        for (CmsResource resource : getResources(ace.getResource())) {
                            String credentials = Messages.get().getBundle(locale).key(
                                Messages.GUI_CREDENTIAL_ROLE_1,
                                role.getName(locale));
                            addEntry(
                                user.getName(),
                                credentials,
                                getAceString(ace),

                                resource.getRootPath());

                        }
                    }
                }
            }
            LOG.info("Generated " + m_entries.size() + " entries for " + this.getClass().getName());
            return m_entries;
        }

        /**
         * Helper method to add the direct ACEs for a principal.<p>
         *
         * @param principalId the principal id
         * @param name the principal name
         * @param locale the locale
         *
         * @throws CmsException if something goes wrong
         */
        protected void addDirectEntries(CmsUUID principalId, String name, Locale locale) throws CmsException {

            for (CmsAccessControlEntry ace : getAces(principalId)) {
                for (CmsResource resource : getResources(ace.getResource())) {
                    String credentials = Messages.get().getBundle(locale).key(Messages.GUI_CREDENTIAL_DIRECT_0);
                    addEntry(name, credentials, getAceString(ace), resource.getRootPath());
                }
            }
        }

        /**
         * Adds a new entry.<p>
         *
         * @param user the user name
         * @param principal the credential name
         * @param permissions the permission string
         * @param path the resource path
         */
        private void addEntry(String user, String principal, String permissions, String path) {

            m_entries.add(new String[] {user, "" + principal, permissions, path});
        }

        /**
         * Gets the access control entries for a given principal id.<p>
         *
         * @param principalId the principal id
         * @return the access control entries for that principal id
         *
         * @throws CmsException if something goes wrong
         */
        private Collection<CmsAccessControlEntry> getAces(CmsUUID principalId) throws CmsException {

            if (m_ace == null) {
                m_ace = ArrayListMultimap.create();
                List<CmsAccessControlEntry> entries = m_cms.getAllAccessControlEntries();
                for (CmsAccessControlEntry entry : entries) {
                    m_ace.put(entry.getPrincipal(), entry);
                }
            }
            return m_ace.get(principalId);
        }

        /**
         * Creates a string representation of an access control entry.<p>
         *
         * @param ace the access control entry
         *
         * @return the string representation of the access control entry
         */
        private String getAceString(CmsAccessControlEntry ace) {

            String result = ace.getPermissions().getPermissionString()
                + (ace.isResponsible() ? ace.getResponsibleString() : "")
                + ace.getInheritingString();
            if ((ace.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) != 0) {
                result = result
                    + " ("
                    + Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms)).key(
                        Messages.GUI_PERMISSION_COLUMN_OVERWRITE_0)
                    + ")";
            }
            return result;

        }

        /**
         * Reads all resources with a given resource id.<p>
         *
         * @param resourceId the resource id
         * @return the resources with the given id
         * @throws CmsException if something goes wrong
         */
        private List<CmsResource> getResources(CmsUUID resourceId) throws CmsException {

            return m_cms.readSiblingsForResourceId(resourceId, CmsResourceFilter.ALL);
        }

        /**
         * Gets all users.<p>
         *
         * @return the list of all users
         *
         * @throws CmsException if something goes wrong
         */
        private List<CmsUser> getUsers() throws CmsException {

            List<CmsUser> users = OpenCms.getOrgUnitManager().getUsersWithoutAdditionalInfo(m_cms, "", true);
            return users;
        }

    }

    /**Column id. */
    public static final String LIST_COLUMN_CREDENTIAL = "apxu_credential";

    /**Column id. */
    public static final String LIST_COLUMN_PERMISSIONS = "apxu_permissions";

    /**Column id. */
    public static final String LIST_COLUMN_USER = "apxu_user";

    /** List id constant. */
    public static final String LIST_ID = "allpermissionsxusers";

    /** Logger instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsAllPrincipalDependenciesList.class);

    /**Column id. */
    static final String LIST_COLUMN_PATH = "apxu_path";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsAllPrincipalDependenciesList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAllPrincipalDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsAllPrincipalDependenciesList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_ALL_PRINCIPAL_DEPENDENCIES_LIST_0),
            LIST_COLUMN_USER,
            CmsListOrderEnum.ORDER_ASCENDING,
            LIST_COLUMN_USER);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#decodeParamValue(java.lang.String, java.lang.String)
     *
     * Overridden because we don't want to 'decode' '+' characters for the search
     */
    @Override
    protected String decodeParamValue(String paramName, String paramValue) {

        return paramValue;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        try {

            ElementGenerator generator = new ElementGenerator(getCms());
            List<String[]> entries = generator.generateEntries();
            List<CmsListItem> result = new ArrayList<CmsListItem>();
            for (String[] entry : entries) {
                String user = entry[0];
                String principal = entry[1];
                String permissions = entry[2];
                String path = entry[3];
                CmsListItem item = getList().newItem("" + user + "_" + principal + "_" + path);
                item.set(LIST_COLUMN_USER, user);
                item.set(LIST_COLUMN_CREDENTIAL, principal);
                item.set(LIST_COLUMN_PERMISSIONS, permissions);
                item.set(LIST_COLUMN_PATH, path);
                result.add(item);
            }
            return result;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add cms dialog resource bundle
        addMessages(org.opencms.workplace.Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_ALLDEP_LIST_COL_USER_0));

        CmsListColumnDefinition credentialCol = new CmsListColumnDefinition(LIST_COLUMN_CREDENTIAL);
        credentialCol.setName(Messages.get().container(Messages.GUI_ALLDEP_LIST_COL_CREDENTIALS_0));

        CmsListColumnDefinition pathCol = new CmsListColumnDefinition(LIST_COLUMN_PATH);
        pathCol.setName(Messages.get().container(Messages.GUI_ALLDEP_LIST_COL_PATH_0));

        CmsListColumnDefinition permCol = new CmsListColumnDefinition(LIST_COLUMN_PERMISSIONS);
        permCol.setName(Messages.get().container(Messages.GUI_ALLDEP_LIST_COL_PERMISSIONS_0));

        List<CmsListColumnDefinition> columns = Arrays.asList(userCol, credentialCol, pathCol, permCol);
        for (CmsListColumnDefinition col : columns) {
            metadata.addColumn(col);
        }
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_PATH));
        metadata.setSearchAction(searchAction);
        for (CmsListColumnDefinition col : columns) {
            if (!searchAction.getColumns().contains(col)) {
                searchAction.addColumn(col);
            }
        }
        metadata.setCsvItemFormatter(new I_CsvItemFormatter() {

            public String csvHeader() {

                return "";
            }

            public String csvItem(CmsListItem item) {

                StringWriter sw = new StringWriter();
                try {
                    CSVWriter csvWriter = new CSVWriter(sw);
                    try {
                        csvWriter.writeNext(
                            new String[] {
                                (String)item.get(LIST_COLUMN_USER),
                                (String)item.get(LIST_COLUMN_CREDENTIAL),
                                (String)item.get(LIST_COLUMN_PATH),
                                (String)item.get(LIST_COLUMN_PERMISSIONS)});
                    } finally {
                        csvWriter.close();
                    }
                } catch (IOException e) {
                    // should never happen, log anyway
                    LOG.error(e.getLocalizedMessage(), e);
                }
                return sw.getBuffer().toString();
            }
        });

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        metadata.addIndependentAction(new CmsListCsvExportIAction());

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }
}