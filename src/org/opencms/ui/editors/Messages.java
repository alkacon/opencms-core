
package org.opencms.ui.editors;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 10.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_CANCEL_0 = "GUI_BUTTON_CANCEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_REDO_0 = "GUI_BUTTON_REDO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_REPLACE_0 = "GUI_BUTTON_REPLACE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_SAVE_0 = "GUI_BUTTON_SAVE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_SAVE_AND_EXIT_0 = "GUI_BUTTON_SAVE_AND_EXIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_SEARCH_0 = "GUI_BUTTON_SEARCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_TOBBLE_BRACKET_AUTOCLOSE_0 = "GUI_BUTTON_TOBBLE_BRACKET_AUTOCLOSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_TOGGLE_HIGHLIGHTING_0 = "GUI_BUTTON_TOGGLE_HIGHLIGHTING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_TOGGLE_LINE_WRAPPING_0 = "GUI_BUTTON_TOGGLE_LINE_WRAPPING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_TOGGLE_TAB_VISIBILITY_0 = "GUI_BUTTON_TOGGLE_TAB_VISIBILITY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_UNDO_0 = "GUI_BUTTON_UNDO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SOURCE_EDITOR_TITLE_0 = "GUI_SOURCE_EDITOR_TITLE_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ui.editors.messages";
    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

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
