/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
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
 *  
 */

package org.opencms.workplace;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.commons.CmsLock;
import org.opencms.workplace.editors.CmsPreEditorAction;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for building the dialog windows of OpenCms.<p> 
 * 
 * @since 6.0.0 
 */
public class CmsDialog extends CmsToolDialog {

    /** Value for the action: cancel. */
    public static final int ACTION_CANCEL = 4;

    /** Value for the action: close popup window. */
    public static final int ACTION_CLOSEPOPUP = 6;

    /** Value for the action: save & close popup window. */
    public static final int ACTION_CLOSEPOPUP_SAVE = 7;

    /** Value for the action: confirmed. */
    public static final int ACTION_CONFIRMED = 1;

    /** Value for the action: continue. */
    public static final int ACTION_CONTINUE = 8;

    /** Value for the action: default (show initial dialog form). */
    public static final int ACTION_DEFAULT = 0;

    /** Value for the action: locks confirmed. */
    public static final int ACTION_LOCKS_CONFIRMED = 99;

    /** Value for the action: ok. */
    public static final int ACTION_OK = 3;

    // note: action values 90 - 99 are reserved for reports
    /** Value for the action: begin the report. */
    public static final int ACTION_REPORT_BEGIN = 90;

    /** Value for the action: end the report. */
    public static final int ACTION_REPORT_END = 92;

    /** Value for the action: update the report. */
    public static final int ACTION_REPORT_UPDATE = 91;

    /** Value for the action: button "set" clicked. */
    public static final int ACTION_SET = 5;

    /** Value for the action: wait (show please wait screen). */
    public static final int ACTION_WAIT = 2;

    /** Constant for the "Advanced" button in the build button methods. */
    public static final int BUTTON_ADVANCED = 3;

    /** Constant for the "Back" button in the build button methods. */
    public static final int BUTTON_BACK = 9;

    /** Constant for the "Cancel" button in the build button methods. */
    public static final int BUTTON_CANCEL = 1;

    /** Constant for the "Close" button in the build button methods. */
    public static final int BUTTON_CLOSE = 2;

    /** Constant for the "Continue" button in the build button methods. */
    public static final int BUTTON_CONTINUE = 10;

    /** Constant for the "Details" button in the build button methods. */
    public static final int BUTTON_DETAILS = 5;

    /** Constant for the "Discard" button in the build button methods (same function as "Cancel" button but different text on button. */
    public static final int BUTTON_DISCARD = 8;

    /** Constant for the "Edit" button in the build button methods (same function as "Ok" button but different text on button. */
    public static final int BUTTON_EDIT = 7;

    /** Constant for the "OK" button in the build button methods. */
    public static final int BUTTON_OK = 0;

    /** Constant for the "OK" button in the build button methods (without form submission). */
    public static final int BUTTON_OK_NO_SUBMIT = 6;

    /** Constant for the "Set" button in the build button methods. */
    public static final int BUTTON_SET = 4;

    /** Request parameter value for the action: back. */
    public static final String DIALOG_BACK = "back";

    /** Request parameter value for the action: cancel. */
    public static final String DIALOG_CANCEL = "cancel";

    /** Request parameter value for the action: dialog confirmed. */
    public static final String DIALOG_CONFIRMED = "confirmed";

    /** Request parameter value for the action: continue. */
    public static final String DIALOG_CONTINUE = "continue";

    /** Request parameter value for the action: initial call. */
    public static final String DIALOG_INITIAL = "initial";

    /** Request parameter value for the action: dialog locks confirmed. */
    public static final String DIALOG_LOCKS_CONFIRMED = "locksconfirmed";

    /** Request parameter value for the action: ok. */
    public static final String DIALOG_OK = "ok";

    /** Request parameter value for the action: set. */
    public static final String DIALOG_SET = "set";

    /** Request parameter value for the action: show please wait screen. */
    public static final String DIALOG_WAIT = "wait";

    /** Request parameter name for the action. */
    public static final String PARAM_ACTION = "action";

    /** Request parameter name for the closelink. */
    public static final String PARAM_CLOSELINK = "closelink";

    /** Request parameter name for the dialog type. */
    public static final String PARAM_DIALOGTYPE = "dialogtype";

    /** Request parameter name for the error stack. */
    public static final String PARAM_ERRORSTACK = "errorstack";

    /** Request parameter name for the file. */
    public static final String PARAM_FILE = "file";

    /** Request parameter name for the frame name. */
    public static final String PARAM_FRAMENAME = "framename";

    /** Request parameter name for the "is popup" flag. */
    public static final String PARAM_ISPOPUP = "ispopup";

    /** Request parameter name for the lock. */
    public static final String PARAM_LOCK = "lock";

    /** Request parameter name for the error message. */
    public static final String PARAM_MESSAGE = "message";

    /** Request parameter name for the originalparams. */
    public static final String PARAM_ORIGINALPARAMS = "originalparams";

    /** Request parameter name for the preactiondone. */
    public static final String PARAM_PREACTIONDONE = "preactiondone";

    /** Request parameter name for the redirect flag. */
    public static final String PARAM_REDIRECT = "redirect";

    /** Request parameter name for the resource. */
    public static final String PARAM_RESOURCE = "resource";

    /** Request parameter name for the target. */
    public static final String PARAM_TARGET = "target";

    /** Request parameter name for the thread id. */
    public static final String PARAM_THREAD = "thread";

    /** Request parameter name for indicating if another thread is following the current one. */
    public static final String PARAM_THREAD_HASNEXT = "threadhasnext";

    /** Request parameter name for the dialog title. */
    public static final String PARAM_TITLE = "title";

    /** Request parameter value for the action: begin the report. */
    public static final String REPORT_BEGIN = "reportbegin";

    /** Request parameter value for the action: end the report. */
    public static final String REPORT_END = "reportend";

    /** Request parameter value for the action: update the report. */
    public static final String REPORT_UPDATE = "reportupdate";

    /** Key name for the throwable attribute. */
    protected static final String ATTRIBUTE_THROWABLE = "throwable";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDialog.class);

    /** The dialog action. */
    private int m_action;

    /** 
     * The custom mapping for online help.<p> 
     * 
     * It will be translated to a javascript variable called onlineHelpUriCustom. 
     * If it is set, the top.head javascript for the online help will use this value. <p> 
     */
    private String m_onlineHelpUriCustom;

    /** The dialog action parameter. */
    private String m_paramAction;

    /** The close link parameter. */
    private String m_paramCloseLink;

    /** The dialog type. */
    private String m_paramDialogtype;

    /** The frame name parameter. */
    private String m_paramFrameName;

    /** The is popup parameter. */
    private String m_paramIsPopup;

    /** The messages parameter. */
    private String m_paramMessage;

    /** The original parameters. */
    private String m_paramOriginalParams;

    /** The pre action done parameter. */
    private String m_paramPreActionDone;

    /** The redirect parameter. */
    private String m_paramRedirect;

    /** The resource parameter. */
    private String m_paramResource;

    /** The title parameter. */
    private String m_paramTitle;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns an initialized CmsDialog instance that is read from the request attributes.<p>
     * 
     * This method is used by dialog elements. 
     * The dialog elements do not initialize their own workplace class, 
     * but use the initialized instance of the "master" class.
     * This is required to ensure that parameters of the "master" class
     * can properly be kept on the dialog elements.<p>
     * 
     * To prevent null pointer exceptions, an empty dialog is returned if 
     * nothing is found in the request attributes.<p>
     *  
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * 
     * @return an initialized CmsDialog instance that is read from the request attributes
     */
    public static CmsDialog initCmsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        CmsDialog wp = (CmsDialog)req.getAttribute(CmsWorkplace.SESSION_WORKPLACE_CLASS);
        if (wp == null) {
            // ensure that we don't get null pointers if the page is directly called
            wp = new CmsDialog(new CmsJspActionElement(context, req, res));
            wp.fillParamValues(req);
        }
        return wp;
    }

    /**
     * Used to close the current JSP dialog.<p>
     * 
     * This method tries to include the URI stored in the workplace settings.
     * This URI is determined by the frame name, which has to be set 
     * in the frame name parameter.<p>
     * 
     * @throws JspException if including an element fails
     */
    public void actionCloseDialog() throws JspException {

        // create a map with empty "resource" parameter to avoid changing the folder when returning to explorer file list
        Map<String, String> params = new HashMap<String, String>();
        params.put(PARAM_RESOURCE, "");
        if (isPopup()) {
            try {
                // try to close the popup
                JspWriter out = getJsp().getJspContext().getOut();
                out.write("<html><head></head>\n");
                out.write("<body onload=\"top.close();\">\n");
                out.write("</body>\n");
                out.write("</html>\n");
            } catch (IOException e) {
                // error redirecting, include explorer file list
                getJsp().include(FILE_EXPLORER_FILELIST, null, params);
            }
        } else if (getParamCloseLink() != null) {
            // close link parameter present
            try {
                if (Boolean.valueOf(getParamRedirect()).booleanValue()) {
                    // redirect parameter is true, redirect to given close link                 
                    getJsp().getResponse().sendRedirect(getParamCloseLink());
                } else {
                    // forward JSP
                    if (!isForwarded()) {
                        setForwarded(true);
                        CmsRequestUtil.forwardRequest(
                            getParamCloseLink(),
                            getJsp().getRequest(),
                            getJsp().getResponse());
                    }
                }
            } catch (Exception e) {
                // forward failed
                throw new JspException(e.getMessage(), e);
            }
        } else if (getParamFramename() != null) {
            // no workplace frame mode (currently used for galleries)
            // frame name parameter found, get URI
            String frameUri = getSettings().getFrameUris().get(getParamFramename());
            // resetting the action parameter
            params.put(PARAM_ACTION, "");
            if (frameUri != null) {
                // URI found, include it
                if (frameUri.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
                    // remove context path from URI before inclusion
                    frameUri = frameUri.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
                }
                // include the found frame URI
                getJsp().include(frameUri, null, params);
            } else {
                // no URI found, include the explorer file list
                getJsp().include(FILE_EXPLORER_FILELIST, null, params);
            }
        } else {
            // no frame name parameter found, include the explorer file list
            getJsp().include(FILE_EXPLORER_FILELIST, null, params);
        }
    }

    /**
     * Returns the html code to build the ajax report container.<p>
     * 
     * @param title the title of the report box
     * 
     * @return html code
     */
    public String buildAjaxResultContainer(String title) {

        StringBuffer html = new StringBuffer(512);
        html.append(dialogBlockStart(title));
        html.append(dialogWhiteBoxStart());
        html.append("<div id='ajaxreport' >");
        html.append(buildAjaxWaitMessage());
        html.append("</div>\n");
        html.append(dialogWhiteBoxEnd());
        html.append(dialogBlockEnd());
        html.append("&nbsp;<br>\n");
        return html.toString();
    }

    /**
     * Override to display additional options in the lock dialog.<p>
     * 
     * @return html code to display additional options
     */
    public String buildLockAdditionalOptions() {

        return "";
    }

    /**
     * Returns the html code to build the confirmation messages.<p>
     * 
     * @return html code
     */
    public String buildLockConfirmationMessageJS() {

        StringBuffer html = new StringBuffer(512);
        html.append("<script type='text/javascript'><!--\n");
        html.append("function setConfirmationMessage(locks, blockinglocks) {\n");
        html.append("\tvar confMsg = document.getElementById('conf-msg');\n");
        html.append("\tif (locks > -1) {\n");
        html.append("\t\tif (blockinglocks > '0') {\n");
        html.append("\t\t\tshowAjaxReportContent();\n");
        html.append("\t\t\tdocument.getElementById('lock-body-id').className = '';\n");
        html.append("\t\t\tdocument.getElementById('butClose').className = '';\n");
        html.append("\t\t\tdocument.getElementById('butContinue').className = 'hide';\n");
        html.append("\t\t\tconfMsg.innerHTML = '");
        html.append(key(org.opencms.workplace.commons.Messages.GUI_OPERATION_BLOCKING_LOCKS_0));
        html.append("';\n");
        html.append("\t\t} else {\n");
        html.append("\t\t\tsubmitAction('");
        html.append(CmsDialog.DIALOG_OK);
        html.append("', null, 'main');\n");
        html.append("\t\t\tdocument.forms['main'].submit();\n");
        html.append("\t\t}\n");
        html.append("\t} else {\n");
        html.append("\t\tdocument.getElementById('butClose').className = '';\n");
        html.append("\t\tdocument.getElementById('butContinue').className = 'hide';\n");
        html.append("\t\tconfMsg.innerHTML = '");
        html.append(key(Messages.GUI_AJAX_REPORT_WAIT_0));
        html.append("';\n");
        html.append("\t}\n");
        html.append("}\n");
        html.append("// -->\n");
        html.append("</script>\n");
        return html.toString();
    }

    /**
     * Returns the html code to build the lock dialog.<p>
     * 
     * @return html code
     * 
     * @throws CmsException if something goes wrong
     */
    public String buildLockDialog() throws CmsException {

        return buildLockDialog(null, null, 2000, false);
    }

    /**
     * Returns the html code to build the lock dialog.<p>
     * 
     * @param nonBlockingFilter the filter to get all non blocking locks
     * @param blockingFilter the filter to get all blocking locks
     * @param hiddenTimeout the maximal number of milliseconds the dialog will be hidden
     * @param includeRelated indicates if the report should include related resources
     * 
     * @return html code
     * 
     * @throws CmsException if something goes wrong
     */
    public String buildLockDialog(
        CmsLockFilter nonBlockingFilter,
        CmsLockFilter blockingFilter,
        int hiddenTimeout,
        boolean includeRelated) throws CmsException {

        setParamAction(CmsDialog.DIALOG_LOCKS_CONFIRMED);
        CmsLock lockwp = new CmsLock(getJsp());
        lockwp.setBlockingFilter(blockingFilter);
        lockwp.setNonBlockingFilter(nonBlockingFilter);

        StringBuffer html = new StringBuffer(512);
        html.append(htmlStart("help.explorer.contextmenu.lock"));
        html.append(lockwp.buildIncludeJs());
        html.append(buildLockConfirmationMessageJS());
        html.append(bodyStart("dialog"));
        html.append("<div id='lock-body-id' class='hide'>\n");
        html.append(dialogStart());
        html.append(dialogContentStart(getParamTitle()));
        html.append(buildLockHeaderBox());
        html.append(dialogSpacer());
        html.append("<form name='main' action='");
        html.append(getDialogUri());
        html.append("' method='post' class='nomargin' onsubmit=\"return submitAction('");
        html.append(CmsDialog.DIALOG_OK);
        html.append("', null, 'main');\">\n");
        html.append(paramsAsHidden());
        html.append("<input type='hidden' name='");
        html.append(CmsDialog.PARAM_FRAMENAME);
        html.append("' value=''>\n");
        html.append(buildAjaxResultContainer(key(org.opencms.workplace.commons.Messages.GUI_LOCK_RESOURCES_TITLE_0)));
        html.append("<div id='conf-msg'></div>\n");
        html.append(buildLockAdditionalOptions());
        html.append(dialogContentEnd());
        html.append(dialogLockButtons());
        html.append("</form>\n");
        html.append(dialogEnd());
        html.append("</div>\n");
        html.append(bodyEnd());
        html.append(lockwp.buildLockRequest(hiddenTimeout, includeRelated));
        html.append(htmlEnd());
        return html.toString();
    }

    /**
     * Returns the html code to build the header box.<p>
     * 
     * @return html code
     * 
     * @throws CmsException if something goes wrong
     */
    public String buildLockHeaderBox() throws CmsException {

        StringBuffer html = new StringBuffer(512);
        // include resource info  
        html.append(dialogBlockStart(null));
        html.append(key(org.opencms.workplace.commons.Messages.GUI_LABEL_TITLE_0));
        html.append(": ");
        html.append(getJsp().property("Title", getParamResource(), ""));
        html.append("<br>\n");
        html.append(key(org.opencms.workplace.commons.Messages.GUI_LABEL_STATE_0));
        html.append(": ");
        html.append(getState());
        html.append("<br>\n");
        html.append(key(org.opencms.workplace.commons.Messages.GUI_LABEL_PERMALINK_0));
        html.append(": ");
        html.append(OpenCms.getLinkManager().getPermalink(getCms(), getParamResource()));
        html.append(dialogBlockEnd());
        return html.toString();
    }

    /**
     * Builds the outer dialog window border.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param attributes optional additional attributes for the opening dialog table
     * 
     * @return a dialog window start / end segment
     */
    public String dialog(int segment, String attributes) {

        if (segment == HTML_START) {
            StringBuffer html = new StringBuffer(512);
            if (useNewStyle()) {
                html.append(dialogTitle());
            }
            html.append("<table class=\"dialog\" cellpadding=\"0\" cellspacing=\"0\"");
            if (attributes != null) {
                html.append(" ");
                html.append(attributes);
            }
            html.append("><tr><td>\n<table class=\"dialogbox\" cellpadding=\"0\" cellspacing=\"0\">\n");
            html.append("<tr><td>\n");
            if (useNewStyle() && getToolManager().hasToolPathForUrl(getJsp().getRequestContext().getUri())) {
                html.append(getAdminTool().groupHtml(this));
            }
            return html.toString();
        } else {
            return "</td></tr></table>\n</td></tr></table>\n<p>&nbsp;</p>\n";
        }
    }

    /**
     * Builds a block with 3D border and optional subheadline in the dialog content area.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param headline the headline String for the block
     * @param error if true, an error block will be created
     * 
     * @return 3D block start / end segment
     */
    public String dialogBlock(int segment, String headline, boolean error) {

        if (segment == HTML_START) {
            StringBuffer result = new StringBuffer(512);
            String errorStyle = "";
            if (error) {
                errorStyle = " dialogerror";
            }
            result.append("<!-- 3D block start -->\n");
            result.append("<fieldset class=\"dialogblock\">\n");
            if (CmsStringUtil.isNotEmpty(headline)) {
                result.append("<legend>");
                result.append("<span class=\"textbold");
                result.append(errorStyle);
                result.append("\" unselectable=\"on\">");
                result.append(headline);
                result.append("</span></legend>\n");
            }
            return result.toString();
        } else {
            return "</fieldset>\n<!-- 3D block end -->\n";
        }
    }

    /**
     * Builds the end HTML for a block with 3D border in the dialog content area.<p>
     * 
     * @return 3D block start / end segment
     */
    public String dialogBlockEnd() {

        return dialogBlock(HTML_END, null, false);
    }

    /**
     * Builds the start HTML for a block with 3D border and optional subheadline in the dialog content area.<p>
     * 
     * @param headline the headline String for the block
     * 
     * @return 3D block start / end segment
     */
    public String dialogBlockStart(String headline) {

        return dialogBlock(HTML_START, headline, false);
    }

    /**
     * Builds the button row under the dialog content area without the buttons.<p>
     * 
     * @param segment the HTML segment (START / END)
     * 
     * @return the button row start / end segment
     */
    public String dialogButtonRow(int segment) {

        if (segment == HTML_START) {
            return "<!-- button row start -->\n<div class=\"dialogbuttons\" unselectable=\"on\">\n";
        } else {
            return "</div>\n<!-- button row end -->\n";
        }
    }

    /**
     * Builds the end of the button row under the dialog content area without the buttons.<p>
     * 
     * @return the button row end segment
     */
    public String dialogButtonRowEnd() {

        return dialogButtonRow(HTML_END);
    }

    /**
     * Builds the start of the button row under the dialog content area without the buttons.<p>
     * 
     * @return the button row start segment
     */
    public String dialogButtonRowStart() {

        return dialogButtonRow(HTML_START);
    }

    /**
     * Builds the html for the button row under the dialog content area, including buttons.<p>
     * 
     * @param buttons array of constants of which buttons to include in the row
     * @param attributes array of Strings for additional button attributes
     * 
     * @return the html for the button row under the dialog content area, including buttons
     */
    public String dialogButtons(int[] buttons, String[] attributes) {

        StringBuffer result = new StringBuffer(256);
        result.append(dialogButtonRow(HTML_START));
        for (int i = 0; i < buttons.length; i++) {
            dialogButtonsHtml(result, buttons[i], attributes[i]);
        }
        result.append(dialogButtonRow(HTML_END));
        return result.toString();
    }

    /**
     * Builds a button row with a single "close" button.<p>
     * 
     * @return the button row 
     */
    public String dialogButtonsClose() {

        return dialogButtons(new int[] {BUTTON_CLOSE}, new String[1]);
    }

    /**
     * Builds a button row with a single "close" button.<p>
     * 
     * @param closeAttribute additional attributes for the "close" button
     * 
     * @return the button row 
     */
    public String dialogButtonsClose(String closeAttribute) {

        return dialogButtons(new int[] {BUTTON_CLOSE}, new String[] {closeAttribute});
    }

    /**
     * Builds a button row with a "close" and a "details" button.<p>
     * 
     * @param closeAttribute additional attributes for the "close" button
     * @param detailsAttribute additional attributes for the "details" button
     * 
     * @return the button row 
     */
    public String dialogButtonsCloseDetails(String closeAttribute, String detailsAttribute) {

        return dialogButtons(new int[] {BUTTON_CLOSE, BUTTON_DETAILS}, new String[] {closeAttribute, detailsAttribute});
    }

    /**
     * Builds a button row with a single "ok" button.<p>
     * 
     * @return the button row 
     */
    public String dialogButtonsOk() {

        return dialogButtons(new int[] {BUTTON_OK}, new String[1]);
    }

    /**
     * Builds a button row with a single "ok" button.<p>
     * 
     * @param okAttribute additional attributes for the "ok" button
     * 
     * @return the button row 
     */
    public String dialogButtonsOk(String okAttribute) {

        return dialogButtons(new int[] {BUTTON_OK}, new String[] {okAttribute});
    }

    /**
     * Builds a button row with an "ok" and a "cancel" button.<p>
     * 
     * @return the button row 
     */
    public String dialogButtonsOkCancel() {

        return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL}, new String[2]);
    }

    /**
     * Builds a button row with an "ok" and a "cancel" button.<p>
     * 
     * @param okAttributes additional attributes for the "ok" button
     * @param cancelAttributes additional attributes for the "cancel" button
     * 
     * @return the button row 
     */
    public String dialogButtonsOkCancel(String okAttributes, String cancelAttributes) {

        return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL}, new String[] {okAttributes, cancelAttributes});
    }

    /**
     * Builds a button row with an "ok", a "cancel" and an "advanced" button.<p>
     * 
     * @param okAttributes additional attributes for the "ok" button
     * @param cancelAttributes additional attributes for the "cancel" button
     * @param advancedAttributes additional attributes for the "advanced" button
     * 
     * @return the button row 
     */
    public String dialogButtonsOkCancelAdvanced(String okAttributes, String cancelAttributes, String advancedAttributes) {

        return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL, BUTTON_ADVANCED}, new String[] {
            okAttributes,
            cancelAttributes,
            advancedAttributes});
    }

    /**
     * Builds a button row with a "set", an "ok", and a "cancel" button.<p>
     * 
     * @param setAttributes additional attributes for the "set" button
     * @param okAttributes additional attributes for the "ok" button
     * @param cancelAttributes additional attributes for the "cancel" button
     * 
     * @return the button row 
     */
    public String dialogButtonsSetOkCancel(String setAttributes, String okAttributes, String cancelAttributes) {

        return dialogButtons(new int[] {BUTTON_SET, BUTTON_OK, BUTTON_CANCEL}, new String[] {
            setAttributes,
            okAttributes,
            cancelAttributes});
    }

    /**
     * Builds the content area of the dialog window.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param title the title String for the dialog window
     * 
     * @return a content area start / end segment
     */
    public String dialogContent(int segment, String title) {

        if (segment == HTML_START) {
            StringBuffer result = new StringBuffer(512);
            // null title is ok, we always want the title headline
            result.append(dialogHead(title));
            result.append("<div class=\"dialogcontent\" unselectable=\"on\">\n");
            result.append("<!-- dialogcontent start -->\n");
            return result.toString();
        } else {
            return "<!-- dialogcontent end -->\n</div>\n";
        }
    }

    /**
     * Returns the end html for the content area of the dialog window.<p>
     * 
     * @return the end html for the content area of the dialog window
     */
    public String dialogContentEnd() {

        return dialogContent(HTML_END, null);
    }

    /**
     * Returns the start html for the content area of the dialog window.<p>
     * 
     * @param title the title for the dialog
     * 
     * @return the start html for the content area of the dialog window
     */
    public String dialogContentStart(String title) {

        return dialogContent(HTML_START, title);
    }

    /**
     * Returns the end html for the outer dialog window border.<p>
     * 
     * @return the end html for the outer dialog window border
     */
    public String dialogEnd() {

        return dialog(HTML_END, null);
    }

    /**
     * Builds the title of the dialog window.<p>
     * 
     * @param title the title String for the dialog window
     * 
     * @return the HTML title String for the dialog window
     */
    public String dialogHead(String title) {

        String escapedTitle;
        if (title == null) {
            escapedTitle = "";
        } else {
            escapedTitle = CmsEncoder.escapeHtml(title);
        }

        return "<div class=\"dialoghead\" unselectable=\"on\">" + escapedTitle + "</div>";
    }

    /**
     * Builds an invisible horizontal spacer with the specified width.<p>
     * 
     * @param width the width of the spacer in pixels
     * 
     * @return an invisible horizontal spacer with the specified width
     */
    public String dialogHorizontalSpacer(int width) {

        return "<td><span style=\"display:block; height: 1px; width: " + width + "px;\"></span></td>";
    }

    /**
     * Builds the necessary button row.<p>
     * 
     * @return the button row 
     */
    public String dialogLockButtons() {

        StringBuffer html = new StringBuffer(512);
        html.append("<div id='butClose' >\n");
        html.append(dialogButtonsClose());
        html.append("</div>\n");
        html.append("<div id='butContinue' class='hide' >\n");
        html.append(dialogButtons(new int[] {BUTTON_CONTINUE, BUTTON_CANCEL}, new String[] {
            " onclick=\"submitAction('" + DIALOG_OK + "', form); form.submit();\"",
            ""}));
        html.append("</div>\n");
        return html.toString();
    }

    /**
     * Builds a dialog line without break (display: block).<p>
     * 
     * @param segment the HTML segment (START / END)
     * 
     * @return a row start / end segment
     */
    public String dialogRow(int segment) {

        if (segment == HTML_START) {
            return "<div class=\"dialogrow\">";
        } else {
            return "</div>\n";
        }
    }

    /**
     * Builds the end of a dialog line without break (display: block).<p>
     * 
     * @return the row end segment
     */
    public String dialogRowEnd() {

        return dialogRow(HTML_END);
    }

    /**
     * Builds the start of a dialog line without break (display: block).<p>
     * 
     * @return the row start segment
     */
    public String dialogRowStart() {

        return dialogRow(HTML_START);
    }

    /**
     * Builds the standard javascript for submitting the dialog.<p>
     * 
     * @return the standard javascript for submitting the dialog
     */
    @Override
    public String dialogScriptSubmit() {

        if (useNewStyle()) {
            return super.dialogScriptSubmit();
        }
        StringBuffer result = new StringBuffer(512);
        result.append("function submitAction(actionValue, theForm, formName) {\n");
        result.append("\tif (theForm == null) {\n");
        result.append("\t\ttheForm = document.forms[formName];\n");
        result.append("\t}\n");
        result.append("\ttheForm." + PARAM_FRAMENAME + ".value = window.name;\n");
        result.append("\tif (actionValue == \"" + DIALOG_OK + "\") {\n");
        result.append("\t\treturn true;\n");
        result.append("\t}\n");
        result.append("\ttheForm." + PARAM_ACTION + ".value = actionValue;\n");
        result.append("\ttheForm.submit();\n");
        result.append("\treturn false;\n");
        result.append("}\n");

        return result.toString();
    }

    /**
     * Builds a horizontal separator line in the dialog content area.<p>
     * 
     * @return a separator element
     */
    public String dialogSeparator() {

        return "<div class=\"dialogseparator\" unselectable=\"on\"></div>";
    }

    /**
     * Builds a space between two elements in the dialog content area.<p>
     * 
     * @return a space element
     */
    public String dialogSpacer() {

        return "<div class=\"dialogspacer\" unselectable=\"on\">&nbsp;</div>";
    }

    /**
     * Returns the start html for the outer dialog window border.<p>
     * 
     * @return the start html for the outer dialog window border
     */
    public String dialogStart() {

        return dialog(HTML_START, null);
    }

    /**
     * Returns the start html for the outer dialog window border.<p>
     * 
     * @param attributes optional html attributes to insert
     * 
     * @return the start html for the outer dialog window border
     */
    public String dialogStart(String attributes) {

        return dialog(HTML_START, attributes);
    }

    /**
     * Builds a subheadline in the dialog content area.<p>
     * 
     * @param headline the desired headline string
     * 
     * @return a subheadline element
     */
    public String dialogSubheadline(String headline) {

        StringBuffer retValue = new StringBuffer(128);
        retValue.append("<div class=\"dialogsubheader\" unselectable=\"on\">");
        retValue.append(headline);
        retValue.append("</div>\n");
        return retValue.toString();
    }

    /**
     * Builds the HTML code to fold and unfold a white-box.<p>
     * 
     * @param headline the heading to display
     * @param id the id of the toggle
     * @param show true if the white box is open at the beginning
     * 
     * @return HTML code to fold and unfold a white-box
     */
    public String dialogToggleStart(String headline, String id, boolean show) {

        StringBuffer result = new StringBuffer(512);
        // set icon and style class to use: hide user permissions
        String image = "plus.png";
        String styleClass = "hide";
        if (show) {
            // show user permissions
            image = "minus.png";
            styleClass = "show";
        }

        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        result.append("<tr>\n");
        result.append("\t<td style=\"vertical-align: bottom; padding-bottom: 2px;\"><a href=\"javascript:toggleDetail('");
        result.append(id);
        result.append("');\"><img src=\"");
        result.append(getSkinUri());
        result.append("commons/");
        result.append(image);
        result.append("\" class=\"noborder\" id=\"ic-");
        result.append(id);
        result.append("\"></a></td>\n");
        result.append("\t<td>");
        result.append(dialogSubheadline(headline));
        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");

        result.append("<div class=\"");
        result.append(styleClass);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">\n");
        return result.toString();
    }

    /**
     * Builds a white box in the dialog content area.<p>
     * 
     * @param segment the HTML segment (START / END)
     * 
     * @return the white box start / end segment
     */
    public String dialogWhiteBox(int segment) {

        if (segment == HTML_START) {
            return "<!-- white box start -->\n"
                + "<div class=\"dialoginnerboxborder\">\n"
                + "<div class=\"dialoginnerbox\" unselectable=\"off\">\n";
        } else {
            return "</div>\n</div>\n<!-- white box end -->\n";
        }
    }

    /**
     * Builds the end of a white box in the dialog content area.<p>
     * 
     * @return the white box end segment
     */
    public String dialogWhiteBoxEnd() {

        return dialogWhiteBox(HTML_END);
    }

    /**
     * Builds the start of a white box in the dialog content area.<p>
     * 
     * @return the white box start segment
     */
    public String dialogWhiteBoxStart() {

        return dialogWhiteBox(HTML_START);
    }

    /**
     * Returns the action value.<p>
     * 
     * The action value is used on JSP pages to select the proper action 
     * in a large "switch" statement.<p>
     * 
     * @return the action value
     */
    public int getAction() {

        return m_action;
    }

    /**
     * Returns the action to be carried out after a click on the cancel button..<p>
     * 
     * @return the action to be carried out after a click on the cancel button.
     */
    public String getCancelAction() {

        return DIALOG_CANCEL;
    }

    /**
     * Returns the http URI of the current dialog, to be used
     * as value for the "action" attribute of a html form.<p>
     *
     * This URI is the real one.<p>
     *  
     * @return the http URI of the current dialog
     */
    public String getDialogRealUri() {

        return getJsp().link(getJsp().getRequestContext().getUri());
    }

    /**
     * Returns the http URI of the current dialog, to be used
     * as value for the "action" attribute of a html form.<p>
     *
     * This URI could not be really the real one... <p>
     *  
     * @return the http URI of the current dialog
     */
    public String getDialogUri() {

        if (!useNewStyle()) {
            return getDialogRealUri();
        } else {
            return CmsToolManager.linkForToolPath(getJsp(), getCurrentToolPath());
        }
    }

    /**
     * Returns the custom mapping for the online help.<p>
     * 
     * @return the custom mapping for the online help
     */
    public String getOnlineHelpUriCustom() {

        if (m_onlineHelpUriCustom == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(m_onlineHelpUriCustom.length() + 4);
        result.append("\"");
        result.append(m_onlineHelpUriCustom);
        result.append("\"");
        return result.toString();
    }

    /**
     * Returns the value of the action parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The action parameter is very important, 
     * it will select the dialog action to perform.
     * The value of the {@link #getAction()} method will be
     * initialized from the action parameter.<p>
     * 
     * @return the value of the action parameter
     */
    public String getParamAction() {

        return m_paramAction;
    }

    /**
     * Returns the value of the close link parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * @return the value of the close link parameter
     */
    public String getParamCloseLink() {

        if ((m_paramCloseLink == null) || "null".equals(m_paramCloseLink)) {
            return null;
        }
        return m_paramCloseLink;
    }

    /**
     * Returns the value of the dialog type parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * This parameter is very important. 
     * It must match to the localization keys,
     * e.g. "copy" for the copy dialog.<p>
     * 
     * This parameter must be set manually by the subclass during 
     * first initialization.<p> 
     * 
     * @return the value of the dialog type parameter
     */
    public String getParamDialogtype() {

        return m_paramDialogtype;
    }

    /**
     * Returns the value of the frame name parameter.<p>
     * 
     * @return the value of the frame name parameter
     */
    public String getParamFramename() {

        if ((m_paramFrameName != null) && !"null".equals(m_paramFrameName)) {
            return m_paramFrameName;
        } else {
            return null;
        }
    }

    /**
     * Returns the is popup parameter.<p>
     * 
     * Use this parameter to indicate that the dialog is shown in a popup window.<p>
     * 
     * @return the is popup parameter
     */
    public String getParamIsPopup() {

        return m_paramIsPopup;
    }

    /**
     * Returns the value of the message parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The message parameter is used on dialogs to 
     * show any text message.<p>
     * 
     * @return the value of the message parameter
     */
    public String getParamMessage() {

        return m_paramMessage;
    }

    /**
     * Returns the value of the original parameters parameter.<p>
     * 
     * This stores the request parameter values from a previous dialog, if necessary.<p>
     * 
     * @return the value of the original parameters parameter
     */
    public String getParamOriginalParams() {

        return m_paramOriginalParams;
    }

    /**
     * Returns the value of the preaction done parameter.<p>
     * 
     * @return the value of the preaction done parameter
     */
    public String getParamPreActionDone() {

        return m_paramPreActionDone;
    }

    /**
     * Returns the value of the redirect flag parameter.<p>
     * 
     * @return the value of the redirect flag parameter
     */
    public String getParamRedirect() {

        return m_paramRedirect;
    }

    /**
     * Returns the value of the file parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The file parameter selects the file on which the dialog action
     * is to be performed.<p>
     * 
     * @return the value of the file parameter
     */
    public String getParamResource() {

        if ((m_paramResource != null) && !"null".equals(m_paramResource)) {
            return m_paramResource;
        } else {
            return null;
        }
    }

    /**
     * Returns the value of the title parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * This parameter is used to build the title 
     * of the dialog. It is a parameter so that the title 
     * can be passed to included elements.<p>
     * 
     * @return the value of the title parameter
     */
    public String getParamTitle() {

        return m_paramTitle;
    }

    /**
     * Gets a formatted file state string.<p>
     * 
     * @return formatted state string
     * 
     * @throws CmsException if something goes wrong
     */
    public String getState() throws CmsException {

        if (CmsStringUtil.isNotEmpty(getParamResource())) {
            CmsResource file = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            if (getCms().isInsideCurrentProject(getParamResource())) {
                return key(Messages.getStateKey(file.getState()));
            } else {
                return key(Messages.GUI_EXPLORER_STATENIP_0);
            }
        }
        return "+++ resource parameter not found +++";
    }

    /**
     * Checks if the current resource has lock state exclusive or inherited.<p>
     * 
     * This is used to determine whether the dialog shows the option to delete all
     * siblings of the resource or not.
     * 
     * @return true if lock state is exclusive or inherited, otherwise false
     */
    public boolean hasCorrectLockstate() {

        org.opencms.lock.CmsLock lock = null;
        try {
            // get the lock state for the current resource
            lock = getCms().getLock(getParamResource());
        } catch (CmsException e) {
            // error getting lock state, log the error and return false
            LOG.error(e.getLocalizedMessage(getLocale()), e);
            return false;
        }
        // check if auto lock feature is enabled
        boolean autoLockFeature = lock.isNullLock() && OpenCms.getWorkplaceManager().autoLockResources();
        return autoLockFeature || lock.isExclusive() || lock.isInherited();
    }

    /**
     * Checks if this resource has siblings.<p>
     * 
     * @return true if this resource has siblings
     */
    public boolean hasSiblings() {

        try {
            return getCms().readResource(getParamResource(), CmsResourceFilter.ALL).getSiblingCount() > 1;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(getLocale()), e);
            return false;
        }

    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * @return the start html of the page
     */
    public String htmlStart() {

        return pageHtml(HTML_START, null);
    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @param helpUrl the key for the online help to include on the page
     * 
     * @return the start html of the page
     */
    @Override
    public String htmlStart(String helpUrl) {

        return pageHtml(HTML_START, helpUrl);
    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * @param helpUrl the key for the online help to include on the page
     * @param title the title for the page
     * 
     * @return the start html of the page
     */
    public String htmlStart(String helpUrl, String title) {

        return pageHtml(HTML_START, helpUrl, title);
    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE, 
     * inserting a header with the content-type and choosing an individual style sheet.<p>
     * 
     * @param title the title for the page
     * @param stylesheet the style sheet to include
     * 
     * @return the start html of the page
     */
    public String htmlStartStyle(String title, String stylesheet) {

        return pageHtmlStyle(HTML_START, title, stylesheet);
    }

    /**
     * Displays the throwable on the error page and logs the error.<p>
     * 
     * @param wp the workplace class
     * @param t the throwable to be displayed on the error page
     * 
     * @throws JspException if the include of the error page jsp fails
     */
    public void includeErrorpage(CmsWorkplace wp, Throwable t) throws JspException {

        CmsLog.getLog(wp).error(Messages.get().getBundle().key(Messages.ERR_WORKPLACE_DIALOG_0), t);
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, wp);
        getJsp().getRequest().setAttribute(ATTRIBUTE_THROWABLE, t);
        getJsp().include(FILE_DIALOG_SCREEN_ERRORPAGE);
    }

    /**
     * Returns the "isPopup" flag.<p>
     *
     * @return the "isPopup" flag
     */
    public boolean isPopup() {

        return Boolean.valueOf(getParamIsPopup()).booleanValue();
    }

    /**
     * Returns if the dialog is called in direct edit mode before the editor is opened.<p>
     * 
     * @return true if the dialog is called in direct edit mode before the editor is opened
     */
    public boolean isPreEditor() {

        return CmsPreEditorAction.isPreEditorMode(this);
    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param helpUrl the url for the online help to include on the page
     * 
     * @return the start html of the page
     */
    @Override
    public String pageHtml(int segment, String helpUrl) {

        return pageHtml(segment, helpUrl, null);
    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param helpUrl the url for the online help to include on the page
     * @param title the title for the page
     * 
     * @return the start html of the page
     */
    public String pageHtml(int segment, String helpUrl, String title) {

        if (segment == HTML_START) {
            String stylesheet = null;
            if (isPopup() && !useNewStyle()) {
                stylesheet = "popup.css";
            }
            StringBuffer result = new StringBuffer(pageHtmlStyle(segment, title, stylesheet));
            if (getSettings().isViewExplorer()) {
                result.append("<script type=\"text/javascript\" src=\"");
                result.append(getSkinUri());
                result.append("commons/explorer.js\"></script>\n");
            }
            result.append("<script type=\"text/javascript\">\n");
            result.append(dialogScriptSubmit());
            if (helpUrl != null) {
                result.append("if (top.head && top.head.helpUrl) {\n");
                result.append("\ttop.head.helpUrl=\"");
                result.append(helpUrl + "\";\n");
                result.append("}\n\n");
            }
            // the variable that may be set as path: if non-null this will be 
            // used as path for the online help window. This is needed because there are pages 
            // e.g. /administration/accounts/users/new  that perform a jsp - forward while leaving the 
            // path parameter on the old page: no correct online help possible. 
            result.append("var onlineHelpUriCustom = ");
            result.append(getOnlineHelpUriCustom());
            result.append(";\n");

            result.append("</script>\n");
            return result.toString();
        } else {
            return super.pageHtml(segment, null);
        }
    }

    /**
     * Set the custom mapping for the online help. <p>
     * 
     * This value will be set to a javascript variable called onlineHelpUriCustom. 
     * If it is set, the top.head javascript for the online help will use this value. <p> 
     * 
     * This method should be called from <code>{@link #initWorkplaceRequestValues(CmsWorkplaceSettings, HttpServletRequest)}</code>,  
     * <code>{@link CmsWorkplace#initWorkplaceMembers(CmsJspActionElement)}</code> 
     * or from the jsp if the dialog class is used for several actions. 
     * It should be used whenever the online help mapping does not work (due to jsp - forwards).<p>
     * 
     * @param uri the left hand value in mapping.properties for the online help pages
     */
    public void setOnlineHelpUriCustom(String uri) {

        m_onlineHelpUriCustom = uri;
    }

    /**
     * Sets the value of the action parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamAction(String value) {

        m_paramAction = value;
    }

    /**
     * Sets the value of the close link parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamCloseLink(String value) {

        // ensure decoded chars are re-encoded again properly

        m_paramCloseLink = value;
    }

    /**
     * Sets the value of the dialog type parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamDialogtype(String value) {

        m_paramDialogtype = value;
    }

    /**
     * Sets the value of the frame name parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamFramename(String value) {

        m_paramFrameName = value;
    }

    /**
     * Sets the is popup parameter.<p>
     * 
     * @param value the is popup parameter value
     */
    public void setParamIsPopup(String value) {

        m_paramIsPopup = value;
    }

    /**
     * Sets the value of the message parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamMessage(String value) {

        m_paramMessage = value;
    }

    /**
     * Sets the value of the original parameters parameter.<p>
     * 
     * @param paramOriginalParams the value of the original parameters parameter
     */
    public void setParamOriginalParams(String paramOriginalParams) {

        m_paramOriginalParams = paramOriginalParams;
    }

    /**
     * Sets the value of the preaction done parameter.<p>
     * 
     * @param paramPreActionDone the value of the preaction done parameter
     */
    public void setParamPreActionDone(String paramPreActionDone) {

        m_paramPreActionDone = paramPreActionDone;
    }

    /**
     * Sets the value of the redirect flag parameter.<p>
     * 
     * @param redirect the value of the redirect flag parameter
     */
    public void setParamRedirect(String redirect) {

        m_paramRedirect = redirect;
    }

    /**
     * Sets the value of the file parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamResource(String value) {

        m_paramResource = value;
    }

    /**
     * Sets the value of the title parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamTitle(String value) {

        m_paramTitle = value;
    }

    /**
     * Appends a space char. between tag attributes.<p>
     * 
     * @param attribute a tag attribute
     * 
     * @return the tag attribute with a leading space char
     */
    protected String appendDelimiter(String attribute) {

        if (CmsStringUtil.isNotEmpty(attribute)) {
            if (!attribute.startsWith(" ")) {
                // add a delimiter space between the beginning button HTML and the button tag attributes
                return " " + attribute;
            } else {
                return attribute;
            }
        }

        return "";
    }

    /**
     * Returns ajax wait message.<p>
     * 
     * @return html code
     */
    protected String buildAjaxWaitMessage() {

        StringBuffer html = new StringBuffer(512);
        html.append("<table border='0' style='vertical-align:middle; height: 150px;'>\n");
        html.append("<tr><td width='40' align='center' valign='middle'><img src='");
        html.append(CmsWorkplace.getSkinUri());
        html.append("commons/wait.gif' id='ajaxreport-img' width='32' height='32' alt=''></td>\n");
        html.append("<td valign='middle'><span id='ajaxreport-txt' style='color: #000099; font-weight: bold;'>\n");
        html.append(key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0));
        html.append("</span><br></td></tr></table>\n");
        return html.toString();
    }

    /**
     * Checks if the permissions of the current user on the resource to use in the dialog are sufficient.<p>
     * 
     * Automatically generates a CmsMessageContainer object with an error message and stores it in the users session.<p>
     * 
     * @param required the required permissions for the dialog
     * @param neededForFolder if true, the permissions are required for the parent folder of the resource (e.g. for editors)
     * 
     * @return true if the permissions are sufficient, otherwise false
     */
    protected boolean checkResourcePermissions(CmsPermissionSet required, boolean neededForFolder) {

        return checkResourcePermissions(
            required,
            neededForFolder,
            Messages.get().container(
                Messages.GUI_ERR_RESOURCE_PERMISSIONS_2,
                getParamResource(),
                required.getPermissionString()));
    }

    /**
     * Checks if the permissions of the current user on the resource to use in the dialog are sufficient.<p>
     * 
     * Automatically generates a CmsMessageContainer object with an error message and stores it in the users session.<p>
     * 
     * @param required the required permissions for the dialog
     * @param neededForFolder if true, the permissions are required for the parent folder of the resource (e.g. for editors)
     * @param errorMessage the message container that is stored in the session in case the permissions are not sufficient
     * 
     * @return true if the permissions are sufficient, otherwise false
     */
    protected boolean checkResourcePermissions(
        CmsPermissionSet required,
        boolean neededForFolder,
        CmsMessageContainer errorMessage) {

        boolean hasPermissions = false;
        try {
            CmsResource res;
            if (neededForFolder) {
                // check permissions for the folder the resource is in
                res = getCms().readResource(CmsResource.getParentFolder(getParamResource()), CmsResourceFilter.ALL);
            } else {
                res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            }
            hasPermissions = getCms().hasPermissions(res, required, false, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }

        if (!hasPermissions) {
            // store the error message in the users session
            getSettings().setErrorMessage(errorMessage);
        }

        return hasPermissions;
    }

    /**
     * Returns the full path of the current workplace folder.<p>
     * 
     * @return the full path of the current workplace folder
     */
    protected String computeCurrentFolder() {

        String currentFolder = getSettings().getExplorerResource();
        if (currentFolder == null) {
            // set current folder to root folder
            try {
                currentFolder = getCms().getSitePath(getCms().readFolder("/", CmsResourceFilter.IGNORE_EXPIRATION));
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
                currentFolder = "/";
            }
        }
        if (!currentFolder.endsWith("/")) {
            // add folder separator to currentFolder
            currentFolder += "/";
        }
        return currentFolder;
    }

    /**
     * Renders the HTML for a single input button of a specified type.<p>
     * 
     * @param result a string buffer where the rendered HTML gets appended to
     * @param button a integer key to identify the button
     * @param attribute an optional string with possible tag attributes, or null
     */
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {

        attribute = appendDelimiter(attribute);

        switch (button) {
            case BUTTON_OK:
                result.append("<input name=\"ok\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_OK_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" type=\"submit\"");
                } else {
                    result.append(" type=\"button\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_CANCEL:
                result.append("<input name=\"cancel\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_CANCEL_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_EDIT:
                result.append("<input name=\"ok\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_EDIT_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" type=\"submit\"");
                } else {
                    result.append(" type=\"button\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_DISCARD:
                result.append("<input name=\"cancel\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_DISCARD_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_CLOSE:
                result.append("<input name=\"close\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_CLOSE_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_ADVANCED:
                result.append("<input name=\"advanced\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_ADVANCED_0) + "\"");
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_SET:
                result.append("<input name=\"set\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_SET_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_SET + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_BACK:
                result.append("<input name=\"set\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_BACK_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_BACK + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_CONTINUE:
                result.append("<input name=\"set\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_CONTINUE_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CONTINUE + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_DETAILS:
                result.append("<input name=\"details\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_DIALOG_BUTTON_DETAIL_0) + "\"");
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            default:
                // not a valid button code, just insert a warning in the HTML
                result.append("<!-- invalid button code: ");
                result.append(button);
                result.append(" -->\n");
        }
    }

    /**
     * Returns the link URL to get back one folder in the administration view.<p>
     * 
     * @return the link URL to get back one folder in the administration view
     */
    protected String getAdministrationBackLink() {

        return CmsWorkplace.VFS_PATH_WORKPLACE
            + "action/administration_content_top.html"
            + "?sender="
            + CmsResource.getParentFolder(getJsp().getRequestContext().getFolderUri());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
        if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        }
    }

    /**
     * Sets the action value.<p>
     * 
     * @param value the action value
     */
    protected void setAction(int value) {

        m_action = value;
    }
}