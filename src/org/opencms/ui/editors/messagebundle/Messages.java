
package org.opencms.ui.editors.messagebundle;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/*
 package org.opencms.workplace;

import org.opencms.db.CmsResourceState;
import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 10.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ui.editors.messagebundle.messages";

    /** Message constant for key in the resource bundle. */
    public static final String ERROR_LOADING_BUNDLE_CMS_OBJECT_NULL_0 = "ERROR_LOADING_BUNDLE_CMS_OBJECT_NULL_0";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /** Message constant for key in the resource bundle. */
    public static final String ERROR_FILE_NOT_A_BUNDLE_1 = "ERROR_FILE_NOT_A_BUNDLE_1";
    /** Message constant for key in the resource bundle. */
    public static final String ERROR_LOADING_BUNDLE_FILENAME_NULL_0 = "ERROR_LOADING_BUNDLE_FILENAME_NULL_0";
    /** Message constant for key in the resource bundle. */
    public static final String GUI_COLUMN_HEADER_KEY_0 = "GUI_COLUMN_HEADER_KEY_0";
    /** Message constant for key in the resource bundle. */
    public static final String GUI_COLUMN_HEADER_DESCRIPTION_0 = "GUI_COLUMN_HEADER_DESCRIPTION_0";
    /** Message constant for key in the resource bundle. */
    public static final String GUI_COLUMN_HEADER_TRANSLATION_0 = "GUI_COLUMN_HEADER_TRANSLATION_0";
    /** Message constant for key in the resource bundle. */
    public static final String GUI_COLUMN_HEADER_DEFAULT_0 = "GUI_COLUMN_HEADER_DEFAULT_0";
    /** Message constant for key in the resource bundle. */
    public static final String GUI_APP_TITLE_0 = "GUI_APP_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNLOCKING_RESOURCES_0 = "ERR_UNLOCKING_RESOURCES_0";
    /** Message constant for key in the resource bundle. */
    public static final String ERR_LOADING_RESOURCES_0 = "ERR_LOADING_RESOURCES_0";
    /** Message constant for key in the resource bundle. */
    public static final String ERR_SAVING_CHANGES_0 = "ERR_SAVING_CHANGES_0";
    /** Message constant for key in the resource bundle. */
    public static final String ERROR_BUNDLE_DESCRIPTOR_NOT_UNIQUE_1 = "ERROR_BUNDLE_DESCRIPTOR_NOT_UNIQUE_1";
    /** Message constant for key in the resource bundle. */
    public static final String ERROR_UNSUPPORTED_BUNDLE_TYPE_1 = "ERROR_UNSUPPORTED_BUNDLE_TYPE_1";
    /** Message constant for key in the resource bundle. */
    public static final String GUI_LANGUAGE_SWITCHER_LABEL_0 = "GUI_LANGUAGE_SWITCHER_LABEL_0";

    public static final String GUI_VIEW_SWITCHER_LABEL_0 = "GUI_VIEW_SWITCHER_LABEL_0";

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     *
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#getBundleName()
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}
