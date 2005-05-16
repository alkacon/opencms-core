/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsDialog.java,v $
 * Date   : $Date: 2005/05/16 13:46:56 $
 * Version: $Revision: 1.68 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 *  
 */

package org.opencms.workplace;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.CmsToolDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for building the dialog windows of OpenCms.<p> 
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.68 $
 * 
 * @since 5.1
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

    /** Value for the action: default (show initial dialog form). */
    public static final int ACTION_DEFAULT = 0;

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

    /** Request parameter name for the resource. */
    public static final String PARAM_RESOURCE = I_CmsWpConstants.C_PARA_RESOURCE;

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

    private int m_action;
    private String m_paramAction;
    private String m_paramCloseLink;
    private String m_paramDialogtype;
    private String m_paramErrorstack;
    private String m_paramFrameName;
    private String m_paramIsPopup;
    private String m_paramMessage;
    private String m_paramReasonSuggestion;
    private String m_paramResource;
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
     * @return an initialized CmsDialog instance that is read from the request attributes
     */
    public static CmsDialog initCmsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        CmsDialog wp = (CmsDialog)req.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_CLASS);
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
     * in the framename parameter.<p>
     * 
     * @throws JspException if including an element fails
     */
    public void actionCloseDialog() throws JspException {

        // create a map with empty "resource" parameter to avoid changing the folder when returning to explorer file list
        Map params = new HashMap();
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
                getJsp().include(C_FILE_EXPLORER_FILELIST, null, params);
            }
        } else if (getParamCloseLink() != null) {
            // close link parameter present, redirect to it...
            try {
                getJsp().getResponse().sendRedirect(getParamCloseLink());
            } catch (IOException e) {
                // error redirecting, include explorer file list
                getJsp().include(C_FILE_EXPLORER_FILELIST, null, params);
            }
        } else if (getParamFramename() != null) {
            // framename parameter found, get URI
            String frameUri = (String)getSettings().getFrameUris().get(getParamFramename());
            if (frameUri != null) {
                // URI found, include it
                if (frameUri.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
                    // remove context path from URI before inclusion
                    frameUri = frameUri.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
                }
                if (frameUri.endsWith("administration_content_top.html")) {
                    // TODO: remove this workaround for legacy backoffice
                    String wpClass = this.getClass().getName();
                    String wpPackage = this.getClass().getPackage().getName();
                    if ((wpPackage.endsWith("commons") || wpPackage.endsWith("gallery"))
                        && (!wpClass.endsWith("Report"))) {
                        // for returning from common workplace action, show explorer filelist again (i.e. gallery & project views)
                        getJsp().include(C_FILE_EXPLORER_FILELIST, null, params);
                    } else {
                        try {
                            // redirect to administration body frame with "sender" parameter
                            getJsp().getResponse().sendRedirect(
                                getJsp().link(frameUri) + "?sender=/system/workplace/administration/");
                        } catch (IOException e) {
                            params.put("sender", "/system/workplace/administration/");
                            getJsp().include(frameUri, null, params);
                        }
                    }
                } else {
                    // include the found frame URI
                    getJsp().include(frameUri, null, params);
                }
            } else {
                // no URI found, include the explorer file list
                getJsp().include(C_FILE_EXPLORER_FILELIST, null, params);
            }
        } else {
            // no framename parameter found, include the explorer file list
            getJsp().include(C_FILE_EXPLORER_FILELIST, null, params);
        }
    }

    /**
     * Builds the outer dialog window border.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param attributes optional additional attributes for the opening dialog table
     * @return a dialog window start / end segment
     */
    public String dialog(int segment, String attributes) {

        if (segment == HTML_START) {
            StringBuffer html = new StringBuffer(512);
            if (useNewStyle()) {
                html.append(super.dialogTitle());
            }
            html.append("<table class=\"dialog\" cellpadding=\"0\" cellspacing=\"0\"");
            if (attributes != null) {
                html.append(" ");
                html.append(attributes);
            }
            html.append("><tr><td>\n<table class=\"dialogbox\" cellpadding=\"0\" cellspacing=\"0\">\n");
            html.append("<tr><td>\n");
            if (useNewStyle()) {
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
     * @return 3D block start / end segment
     */
    public String dialogBlock(int segment, String headline, boolean error) {

        if (segment == HTML_START) {
            StringBuffer retValue = new StringBuffer(512);
            String errorStyles = "";
            if (error) {
                errorStyles = " dialogerror textbold";
            }
            retValue.append("<!-- 3D block start -->\n");
            if (headline != null && !"".equals(headline)) {
                retValue.append("<div class=\"dialogblockborder dialogblockborderheadline\" unselectable=\"on\">\n");
                retValue.append("<div class=\"dialogblock" + errorStyles + "\" unselectable=\"on\">\n");
                retValue.append("<span class=\"dialogblockhead" + errorStyles + "\" unselectable=\"on\">");
                retValue.append(headline);
                retValue.append("</span>\n");
            } else {
                retValue.append("<div class=\"dialogblockborder\" unselectable=\"on\">\n");
                retValue.append("<div class=\"dialogblock" + errorStyles + "\" unselectable=\"on\">\n");
            }
            return retValue.toString();
        } else {
            return "</div>\n</div>\n<!-- 3D block end -->";
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
     * @return 3D block start / end segment
     */
    public String dialogBlockStart(String headline) {

        return dialogBlock(HTML_START, headline, false);
    }

    /**
     * Builds the button row under the dialog content area without the buttons.<p>
     * 
     * @param segment the HTML segment (START / END)
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
            return "<!-- dialogcontent end -->\n</div>";
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
     * @return the HTML title String for the dialog window
     */
    public String dialogHead(String title) {

        return "<div class=\"dialoghead\" unselectable=\"on\">" + (title == null ? "" : title) + "</div>";
    }

    /**
     * Builds an invisible horiziontal spacer with the specified width.<p>
     * @param width the width of the spacer in pixels
     * @return an invisible horiziontal spacer with the specified width
     */
    public String dialogHorizontalSpacer(int width) {

        return "<td><span style=\"display:block; height: 1px; width: " + width + "px;\"></span></td>";
    }

    /**
     * Builds a dialog line without break (display: block).<p>
     * 
     * @param segment the HTML segment (START / END)
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

        int todo = 0;
        // TODO: Check logic change here...

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
     * @return the start html for the outer dialog window border
     */
    public String dialogStart(String attributes) {

        return dialog(HTML_START, attributes);
    }

    /**
     * Builds a subheadline in the dialog content area.<p>
     * 
     * @param headline the desired headline string
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
     * Builds a white box in the dialog content area.<p>
     * 
     * @param segment the HTML segment (START / END)
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
            return getToolManager().cmsLinkForPath(getJsp(), getCurrentToolPath(), null);
        }
    }

    /**
     * Returns the default error suggestion String for error messages.<p>
     * 
     * @return the default error suggestion String
     */
    public String getErrorSuggestionDefault() {

        return key("error.reason." + getParamDialogtype())
            + "<br>\n"
            + key("error.suggestion." + getParamDialogtype())
            + "\n";
    }

    /**
     * Returns the formatted value of the errorstack parameter for inclusion in an iframe, 
     * or an empty String if this parameter was not provided.<p>
     * 
     * The error stack is used by the common error screen 
     * that is displayed if an error occurs.<p>
     * 
     * @return the formatted value of the errorstack parameter
     */
    public String getFormattedErrorstack() {

        String exception = m_paramErrorstack;
        if (exception == null || "".equals(exception)) {
            return "";
        } else {
            exception = CmsStringUtil.escapeJavaScript(exception);
            exception = CmsStringUtil.substitute(exception, ">", "&gt;");
            exception = CmsStringUtil.substitute(exception, "<", "&lt;");
            return "<html><body style='background-color: Window; overflow: scroll;'><pre>"
                + exception
                + "</pre></body></html>";
        }
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
     * Returns the value of the closelink parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * @return the value of the closelink parameter
     */
    public String getParamCloseLink() {

        if (m_paramCloseLink != null && !"null".equals(m_paramCloseLink)) {
            return m_paramCloseLink;
        } else {
            return null;
        }
    }

    /**
     * Returns the value of the dialogtype parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * This parameter is very important. 
     * It must match to the localization keys,
     * e.g. "copy" for the copy dialog.<p>
     * 
     * This parameter must be set manually by the subclass during 
     * first initialization.<p> 
     * 
     * @return the value of the dialogtype parameter
     */
    public String getParamDialogtype() {

        return m_paramDialogtype;
    }

    /**
     * Returns the value of the errorstack parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The error stack is used by the common error screen 
     * that is displayed if an error occurs.<p>
     * 
     * @return the value of the errorstack parameter
     */
    public String getParamErrorstack() {

        return CmsStringUtil.substitute(m_paramErrorstack, "\n", "\n<br>");
    }

    /**
     * Returns the value of the frame name parameter.<p>
     * 
     * @return the value of the errorstack parameter
     */
    public String getParamFramename() {

        if (m_paramFrameName != null && !"null".equals(m_paramFrameName)) {
            return m_paramFrameName;
        } else {
            return null;
        }
    }

    /**
     * Returns the ispopup parameter.<p>
     * 
     * Use this parameter to indicate that the dialog is shown in a popup window.<p>
     * 
     * @return the ispopup parameter
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
     * Returns the value of the reasonSuggestion parameter.<p>
     *  
     * @return the value of the reasonSuggestion parameter
     */
    public String getParamReasonSuggestion() {

        return m_paramReasonSuggestion;
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

        if (m_paramResource != null && !"null".equals(m_paramResource)) {
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
     * @throws CmsException if something goes wrong
     */
    public String getState() throws CmsException {

        if (CmsStringUtil.isNotEmpty(getParamResource())) {
            CmsResource file = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            if (getCms().isInsideCurrentProject(getParamResource())) {
                return key("explorer.state" + file.getState());
            } else {
                return key("explorer.statenip");
            }
        }
        return "+++ resource parameter not found +++";
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
     * @return the start html of the page
     */
    public String htmlStart(String helpUrl) {

        return pageHtml(HTML_START, helpUrl);
    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * @param helpUrl the key for the online help to include on the page
     * @param title the title for the page
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
     * @return the start html of the page
     */
    public String htmlStartStyle(String title, String stylesheet) {

        return pageHtmlStyle(HTML_START, title, stylesheet);
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
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param helpUrl the url for the online help to include on the page
     * @return the start html of the page
     */
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
                result.append("top.head.helpUrl=\"");
                result.append(helpUrl + "\";\n}\n");
            }
            result.append("</script>\n");
            return result.toString();
        } else {
            return super.pageHtml(segment, null);
        }

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
     * Sets the value of the closelink parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamCloseLink(String value) {

        m_paramCloseLink = value;
    }

    /**
     * Sets the value of the dialogtype parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamDialogtype(String value) {

        m_paramDialogtype = value;
    }

    /**
     * Sets the value of the errorstack parameter,
     * this should be a stack trace as String.<p>
     * 
     * @param value the value to set
     */
    public void setParamErrorstack(String value) {

        m_paramErrorstack = value;
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
     * Sets the ispopup parameter.<p>
     * 
     * @param value the ispopup parameter value
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
     * Sets the value of the reasonSuggestion parameter.<p>
     * 
     * @param value the value of the reasonSuggestion parameter
     */
    public void setParamReasonSuggestion(String value) {

        m_paramReasonSuggestion = value;
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
     * Appends a space char. between tag attribs.<p>
     * 
     * @param attribute a tag attribute
     * @return the tag attribute with a leading space char.
     */
    protected String appendDelimiter(String attribute) {

        if (attribute != null && !"".equals(attribute)) {
            if (!attribute.startsWith(" ")) {
                // add a delimiter space between the beginning button HTML and the button tag attribs
                return " " + attribute;
            } else {
                return attribute;
            }
        }

        return "";
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
                result.append(key("button.ok") + "\"");
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
                result.append(key("button.cancel") + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_EDIT:
                result.append("<input name=\"ok\" value=\"");
                result.append(key("button.edit") + "\"");
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
                result.append(key("button.discard") + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_CLOSE:
                result.append("<input name=\"close\" type=\"button\" value=\"");
                result.append(key("button.close") + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_ADVANCED:
                result.append("<input name=\"advanced\" type=\"button\" value=\"");
                result.append(key("button.advanced") + "\"");
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_SET:
                result.append("<input name=\"set\" type=\"button\" value=\"");
                result.append(key("button.submit") + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_SET + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_BACK:
                result.append("<input name=\"set\" type=\"button\" value=\"");
                result.append(key("button.back") + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_BACK + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_CONTINUE:
                result.append("<input name=\"set\" type=\"button\" value=\"");
                result.append(key("button.continue") + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CONTINUE + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_DETAILS:
                result.append("<input name=\"details\" type=\"button\" value=\"");
                result.append(key("button.detail") + "\"");
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

        return I_CmsWpConstants.C_VFS_PATH_WORKPLACE
            + "action/administration_content_top.html"
            + "?sender="
            + CmsResource.getParentFolder(getJsp().getRequestContext().getFolderUri());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
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