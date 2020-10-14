
package org.opencms.report.wrapper;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Bundle with the keys of report messages<p>
 *
 * @author Daniel Seidel
 *
 * @version $Revision: 1.0 $
 *
 * @since 12.0.0
 */
public final class DefaultReportMessages extends A_CmsMessageBundle {

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.report.wrapper.defaultreportmessages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new DefaultReportMessages();

    /** Message key. */
    public static final String REPORT_OK_0 = "REPORT_OK_0";

    /** Message key. */
    public static final String REPORT_OK_NO_DOTS_0 = "REPORT_OK_NO_DOTS_0";

    /** Message key. */
    public static final String REPORT_FAILED_0 = "REPORT_FAILED_0";

    /** Message key. */
    public static final String REPORT_FAILED_NO_DOTS_0 = "REPORT_FAILED_NO_DOTS_0";

    /** Message key. */
    public static final String REPORT_SKIPPED_0 = "REPORT_SKIPPED_0";

    /** Message key. */
    public static final String REPORT_SKIPPED_NO_DOTS_0 = "REPORT_SKIPPED_NO_DOTS_0";

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private DefaultReportMessages() {

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
     * Returns the bundle name for this OpenCms package.<p>
     *
     * @return the bundle name for this OpenCms package
     */
    @Override
    public String getBundleName() {

        return BUNDLE_NAME;
    }

}
