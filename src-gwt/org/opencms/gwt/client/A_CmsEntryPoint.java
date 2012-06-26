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

package org.opencms.gwt.client;

import org.opencms.gwt.client.rpc.CmsLog;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.shared.CmsCoreData;

import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.UmbrellaException;

/**
 * Handles exception handling and more for entry points.<p>
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogServiceAsync
 */
public abstract class A_CmsEntryPoint implements EntryPoint {

    /** Flag which indicates whether initClasses() has already been called. */
    private static boolean initializedClasses;

    /**
     * Default constructor.<p> 
     */
    protected A_CmsEntryPoint() {

        // just for subclassing
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    public void onModuleLoad() {

        enableRemoteExceptionHandler();
        I_CmsLayoutBundle bundle = I_CmsLayoutBundle.INSTANCE;
        bundle.buttonCss().ensureInjected();
        bundle.contentEditorCss().ensureInjected();
        bundle.contextmenuCss().ensureInjected();
        bundle.dialogCss().ensureInjected();
        bundle.errorDialogCss().ensureInjected();
        bundle.notificationCss().ensureInjected();
        bundle.dragdropCss().ensureInjected();
        bundle.floatDecoratedPanelCss().ensureInjected();
        bundle.generalCss().ensureInjected();
        bundle.headerCss().ensureInjected();
        bundle.highlightCss().ensureInjected();
        bundle.linkWarningCss().ensureInjected();
        bundle.listItemWidgetCss().ensureInjected();
        bundle.listTreeCss().ensureInjected();
        bundle.stateCss().ensureInjected();
        bundle.tabbedPanelCss().ensureInjected();
        bundle.toolbarCss().ensureInjected();
        bundle.listItemCss().ensureInjected();
        bundle.availabilityCss().ensureInjected();
        bundle.fieldsetCss().ensureInjected();
        bundle.resourceStateCss().ensureInjected();
        bundle.dateBoxCss().ensureInjected();
        bundle.selectAreaCss().ensureInjected();
        bundle.singleLineItemCss().ensureInjected();
        bundle.menuButtonCss().ensureInjected();
        bundle.progressBarCss().ensureInjected();
        bundle.propertiesCss().ensureInjected();

        I_CmsInputLayoutBundle.INSTANCE.inputCss().ensureInjected();

        I_CmsImageBundle.INSTANCE.style().ensureInjected();
        I_CmsImageBundle.INSTANCE.contextMenuIcons().ensureInjected();

        I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().ensureInjected();

        initClasses();
    }

    /**
     * Checks whether the build id of the server-side module is greater than the client-build id, and displays 
     * an error message if this is the case.<p>
     * 
     * @param moduleName the name of the module for which the check should be performed
     *  
     * @return returns <code>true</code> if the check was successful 
     */
    protected boolean checkBuildId(String moduleName) {

        Map<String, String> buildIds = CmsCoreProvider.get().getGwtBuildIds();
        String serverBuildId = buildIds.get(moduleName);
        String config = org.opencms.gwt.client.I_CmsConfigBundle.INSTANCE.gwtProperties().getText();
        Map<String, String> configProperties = CmsCollectionUtil.parseProperties(config);
        String clientBuildId = configProperties.get(CmsCoreData.KEY_GWT_BUILDID);
        if ((serverBuildId != null) && (clientBuildId != null)) {
            int compareResult = clientBuildId.compareTo(serverBuildId);
            if (compareResult == -1) {
                String title = Messages.get().key(Messages.GUI_BUILD_ID_MESSAGE_TITLE_0);
                String content = Messages.get().key(Messages.GUI_BUILD_ID_MESSAGE_CONTENT_0);
                CmsAlertDialog alert = new CmsAlertDialog(title, content);
                alert.center();
                return false;
            }
        }
        return true;
    }

    /**
     * Enables client exception logging on the server.<p>
     */
    protected void enableRemoteExceptionHandler() {

        if (!GWT.isScript()) {
            // In hosted mode, uncaught exceptions are easier to debug 
            return;
        }

        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

            /**
             * @see com.google.gwt.core.client.GWT.UncaughtExceptionHandler#onUncaughtException(java.lang.Throwable)
             */
            public void onUncaughtException(Throwable t) {

                if (t instanceof UmbrellaException) {
                    t = ((UmbrellaException)t).getCauses().iterator().next();
                }
                String message = CmsClientStringUtil.getMessage(t);
                CmsNotification.get().send(CmsNotification.Type.WARNING, message);
                CmsLog.log(message + "\n" + CmsClientStringUtil.getStackTrace(t, "\n"));
            }
        });
    }

    /**
     * Helper method for initializing the classes implementing {@link I_CmsHasInit}.<p>
     * 
     * Calling this method more than once will have no effect.<p>
     */
    private void initClasses() {

        if (!initializedClasses) {
            I_CmsClassInitializer initializer = GWT.create(I_CmsClassInitializer.class);
            initializer.initClasses();
            initializedClasses = true;
        }
    }
}
