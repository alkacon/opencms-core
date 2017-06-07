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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.CmsWorkplaceUserInfoBlock;
import org.opencms.workplace.CmsWorkplaceUserInfoEntry;
import org.opencms.workplace.CmsWorkplaceUserInfoManager;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit the users additional info in the administration view.<p>
 *
 * @since 6.5.6
 */
public class CmsEditUserAddInfoDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "userinfo";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The additional information. */
    protected List<CmsUserAddInfoBean> m_addInfoList;

    /** The user object that is edited on this dialog. */
    protected CmsUser m_user;

    /** The map of editable additional info entries. */
    private SortedMap<String, Object> m_addInfoEditable;

    /** The map of non-editable additional info entries. */
    private SortedMap<String, Object> m_addInfoReadOnly;

    /** Stores the value of the request parameter for the edit all infos flag. */
    private String m_paramEditall;

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsEditUserAddInfoDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditUserAddInfoDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited user to the db.<p>
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();

        try {
            if (!Boolean.valueOf(getParamEditall()).booleanValue()) {
                // fill the values
                Iterator<CmsUserAddInfoBean> it = m_addInfoList.iterator();
                while (it.hasNext()) {
                    CmsUserAddInfoBean infoBean = it.next();
                    if (infoBean.getValue() == null) {
                        m_user.deleteAdditionalInfo(infoBean.getName());
                    } else {
                        m_user.setAdditionalInfo(
                            infoBean.getName(),
                            CmsDataTypeUtil.parse(infoBean.getValue(), infoBean.getType()));
                    }
                }
            } else {
                Map<String, Object> readOnly = new HashMap<String, Object>();
                Iterator<Entry<String, Object>> itEntries = m_user.getAdditionalInfo().entrySet().iterator();
                while (itEntries.hasNext()) {
                    Entry<String, Object> entry = itEntries.next();
                    if (!CmsDataTypeUtil.isParseable(entry.getValue().getClass())) {
                        String key = entry.getKey().toString();
                        if (!entry.getValue().getClass().equals(String.class)) {
                            key += "@" + entry.getValue().getClass().getName();
                        }
                        if (m_addInfoReadOnly.containsKey(key)) {
                            readOnly.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                m_user.setAdditionalInfo(readOnly);
                itEntries = m_addInfoEditable.entrySet().iterator();
                while (itEntries.hasNext()) {
                    Entry<String, Object> entry = itEntries.next();
                    String key = entry.getKey();
                    int pos = key.indexOf("@");
                    if (pos < 0) {
                        m_user.setAdditionalInfo(key, entry.getValue());
                        continue;
                    }
                    String className = key.substring(pos + 1);
                    key = key.substring(0, pos);
                    Class<?> clazz;
                    try {
                        // try the class name
                        clazz = Class.forName(className);
                    } catch (Throwable e) {
                        try {
                            // try the class in the java.lang package
                            clazz = Class.forName(Integer.class.getPackage().getName() + "." + className);
                        } catch (Throwable e1) {
                            clazz = String.class;
                        }
                    }
                    m_user.setAdditionalInfo(key, CmsDataTypeUtil.parse((String)entry.getValue(), clazz));
                }
            }

            // write the edited user
            getCms().writeUser(m_user);
        } catch (Throwable t) {
            errors.add(t);
        }

        if (errors.isEmpty()) {
            if (getCurrentToolPath().endsWith("/orgunit/users/edit/addinfo/all")) {
                // set closelink
                Map<String, String[]> argMap = new HashMap<String, String[]>();
                argMap.put(A_CmsEditUserDialog.PARAM_USERID, new String[] {m_user.getId().toString()});
                argMap.put("oufqn", new String[] {m_user.getOuFqn()});
                setParamCloseLink(
                    CmsToolManager.linkForToolPath(
                        getJsp(),
                        getCurrentToolPath().substring(
                            0,
                            getCurrentToolPath().indexOf("/orgunit/users/edit/addinfo/all")) + "/orgunit/users/edit/",
                        argMap));
            }
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the additional info map.<p>
     *
     * @return the additional info map
     */
    public SortedMap<String, Object> getInfo() {

        return m_addInfoEditable;
    }

    /**
     * Returns the edit all flag parameter value.<p>
     *
     * @return the edit all flag parameter value
     */
    public String getParamEditall() {

        CmsWorkplaceUserInfoManager manager = OpenCms.getWorkplaceManager().getUserInfoManager();
        if ((manager == null) || (manager.getBlocks() == null) || manager.getBlocks().isEmpty()) {
            // if the configuration is empty
            return Boolean.TRUE.toString();
        }
        return m_paramEditall;
    }

    /**
     * Returns the user id parameter value.<p>
     *
     * @return the user id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
    }

    /**
     * Returns the read only add info.<p>
     *
     * @return the read only add info
     */
    public SortedMap<String, Object> getReadonly() {

        return m_addInfoReadOnly;
    }

    /**
     * Sets the modified additional information.<p>
     *
     * @param addInfo the additional information to set
     */
    public void setInfo(SortedMap<String, Object> addInfo) {

        m_addInfoEditable = new TreeMap<String, Object>(addInfo);
    }

    /**
     * Sets the edit all flag parameter value.<p>
     *
     * @param editAll the edit all flag parameter value
     */
    public void setParamEditall(String editAll) {

        m_paramEditall = editAll;
    }

    /**
     * Sets the user id parameter value.<p>
     *
     * @param userId the user id parameter value to set
     */
    public void setParamUserid(String userId) {

        m_paramUserid = userId;
    }

    /**
     * Sets the read only add info.<p>
     *
     * @param addInfoReadOnly the read only add info to set
     */
    public void setReadonly(SortedMap<String, Object> addInfoReadOnly) {

        m_addInfoReadOnly = addInfoReadOnly;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     *
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            if (!Boolean.valueOf(getParamEditall()).booleanValue()) {
                int pos = 0;
                Iterator<CmsWorkplaceUserInfoBlock> it = OpenCms.getWorkplaceManager().getUserInfoManager().getBlocks().iterator();
                while (it.hasNext()) {
                    CmsWorkplaceUserInfoBlock block = it.next();

                    result.append(dialogBlockStart(key(block.getTitle())));
                    result.append(createWidgetTableStart());
                    result.append(createDialogRowsHtml(pos, (pos - 1) + block.getEntries().size()));
                    result.append(createWidgetTableEnd());
                    result.append(dialogBlockEnd());
                    pos += block.getEntries().size();
                }
            } else {
                result.append(createWidgetBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_ADDITIONALINFO_BLOCK_0)));
                result.append(createDialogRowsHtml(0, 0));
                result.append(createWidgetBlockEnd());
                result.append(createWidgetBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_READONLY_BLOCK_0)));
                result.append(createDialogRowsHtml(1, 1));
                result.append(createWidgetBlockEnd());
            }
        }
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initUserObject();

        setKeyPrefix(KEY_PREFIX);

        int count = 0;
        if (!Boolean.valueOf(getParamEditall()).booleanValue()) {
            // widgets to display
            Iterator<CmsWorkplaceUserInfoBlock> itBlocks = OpenCms.getWorkplaceManager().getUserInfoManager().getBlocks().iterator();
            while (itBlocks.hasNext()) {
                CmsWorkplaceUserInfoBlock block = itBlocks.next();

                Iterator<CmsWorkplaceUserInfoEntry> itEntries = block.getEntries().iterator();
                while (itEntries.hasNext()) {
                    CmsWorkplaceUserInfoEntry entry = itEntries.next();

                    int min = entry.isOptional() ? 0 : 1;
                    I_CmsWidget widget = entry.getWidgetObject();
                    addWidget(
                        new CmsWidgetDialogParameter(
                            m_addInfoList.get(count),
                            "value",
                            entry.getKey(),
                            "",
                            PAGES[0],
                            widget,
                            min,
                            1));
                    count++;
                }
            }
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "info", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "readonly", PAGES[0], new CmsDisplayWidget()));
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the additional info bean to work with, depending on the dialog state and request parameters.<p>
     */
    @SuppressWarnings("unchecked")
    protected void initUserObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // edit an existing user, get the user object from db
                m_user = getCms().readUser(new CmsUUID(getParamUserid()));
                if (!Boolean.valueOf(getParamEditall()).booleanValue()) {
                    m_addInfoList = createAddInfoList(m_user);
                } else {
                    setAddInfoMaps();
                }
                return;
            } else {
                // this is not the initial call, get the user object from session
                m_user = getCms().readUser(new CmsUUID(getParamUserid()));
                if (!Boolean.valueOf(getParamEditall()).booleanValue()) {
                    m_addInfoList = (List<CmsUserAddInfoBean>)getDialogObject();
                } else {
                    Map<String, SortedMap<String, Object>> dObj = (Map<String, SortedMap<String, Object>>)getDialogObject();
                    m_addInfoEditable = dObj.get("editable");
                    m_addInfoReadOnly = dObj.get("readonly");
                }
                return;
            }
        } catch (Exception e) {
            // noop
        }
        // create a new user object
        try {
            m_user = getCms().readUser(new CmsUUID(getParamUserid()));
        } catch (CmsException e) {
            // ignore
        }
        if (!Boolean.valueOf(getParamEditall()).booleanValue()) {
            m_addInfoList = createAddInfoList(m_user);
        } else {
            setAddInfoMaps();
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state (may be changed because of the widget values)
        if (!Boolean.valueOf(getParamEditall()).booleanValue()) {
            setDialogObject(m_addInfoList);
        } else {
            Map<String, SortedMap<String, Object>> dObj = new HashMap<String, SortedMap<String, Object>>();
            dObj.put("editable", m_addInfoEditable);
            dObj.put("readonly", m_addInfoReadOnly);
            setDialogObject(dObj);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        String ou = getCms().readUser(new CmsUUID(getParamUserid())).getOuFqn();
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou));
    }

    /**
     * Creates a new additional information bean object.<p>
     *
     * @param user the user to create the bean for
     *
     * @return a new additional information bean object
     */
    private List<CmsUserAddInfoBean> createAddInfoList(CmsUser user) {

        List<CmsUserAddInfoBean> addInfoList = new ArrayList<CmsUserAddInfoBean>();
        // add beans
        Iterator<CmsWorkplaceUserInfoBlock> itBlocks = OpenCms.getWorkplaceManager().getUserInfoManager().getBlocks().iterator();
        while (itBlocks.hasNext()) {
            CmsWorkplaceUserInfoBlock block = itBlocks.next();
            Iterator<CmsWorkplaceUserInfoEntry> itEntries = block.getEntries().iterator();
            while (itEntries.hasNext()) {
                CmsWorkplaceUserInfoEntry entry = itEntries.next();
                Object value = user.getAdditionalInfo(entry.getKey());
                if (value == null) {
                    value = "";
                }
                addInfoList.add(new CmsUserAddInfoBean(entry.getKey(), value.toString(), entry.getClassType()));
            }
        }
        return addInfoList;
    }

    /**
     * Builds the additional info maps.<p>
     */
    private void setAddInfoMaps() {

        m_addInfoEditable = new TreeMap<String, Object>();
        m_addInfoReadOnly = new TreeMap<String, Object>();
        Iterator<Entry<String, Object>> itEntries = m_user.getAdditionalInfo().entrySet().iterator();
        while (itEntries.hasNext()) {
            Entry<String, Object> entry = itEntries.next();
            String key = entry.getKey().toString();
            if ((entry.getValue() == null) || CmsStringUtil.isEmptyOrWhitespaceOnly(entry.getValue().toString())) {
                // skip empty entries
                continue;
            }
            if (!entry.getValue().getClass().equals(String.class)) {
                // only show type different to string
                key += "@" + entry.getValue().getClass().getName();
            }
            if (CmsDataTypeUtil.isParseable(entry.getValue().getClass())) {
                m_addInfoEditable.put(key, entry.getValue());
            } else {
                String value = entry.getValue().toString();
                if (value.length() > (75 - key.length())) {
                    if ((75 - key.length()) > 5) {
                        value = value.substring(0, (75 - key.length())) + " ...";
                    } else {
                        value = "...";
                    }
                }
                m_addInfoReadOnly.put(key, value);
            }
        }
    }
}
