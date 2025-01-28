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

package org.opencms.ade.contenteditor.shared;

import org.opencms.acacia.shared.CmsValidationResult;
import org.opencms.util.CmsPair;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores the editor save result information.<p>
 */
public class CmsSaveResult implements IsSerializable {

    /** If container element settings where changed. */
    private boolean m_hasChangedSettings;

    /** If validation warnings caused a save error. */
    private boolean m_warningsAsError;

    /** The validation result. */
    private CmsValidationResult m_validationResult;

    /**
     * Map from locale to issues with information to display in the validation result dialog.
     * It contains already the information to display, localized wrt the workplace locale
     * and in the correct order.
     */
    private Map<String, List<List<CmsPair<String, Integer>>>> m_validationIssueInformation;

    /**
     * Constructor.<p>
     *
     * @param hasChangedSettings if container element settings where changed
     * @param validationResult the validation result
     * @param warningsAsError flag, indicating if warnings should be treated as errors
     * @param issuesToDisplay the issues (warnings or errors) to display, already localized and sorted as needed.
     */
    public CmsSaveResult(
        boolean hasChangedSettings,
        CmsValidationResult validationResult,
        boolean warningsAsError,
        Map<String, List<List<CmsPair<String, Integer>>>> issuesToDisplay) {

        m_hasChangedSettings = hasChangedSettings;
        m_validationResult = validationResult;
        m_warningsAsError = warningsAsError;
        m_validationIssueInformation = issuesToDisplay;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsSaveResult() {

    }

    /**
     * Returns the validation issue information to display.
     * @return the validation issue information to display.
     */
    public Map<String, List<List<CmsPair<String, Integer>>>> getIssueInformation() {

        return m_validationIssueInformation;
    }

    /**
     * Returns the validation result.<p>
     *
     * @return the validation result
     */
    public CmsValidationResult getValidationResult() {

        return m_validationResult;
    }

    /**
     * Returns whether the validation result has errors.<p>
     *
     * @return <code>true</code> in case the validation result has errors
     */
    public boolean hasErrors() {

        return (m_validationResult != null)
            && (m_validationResult.hasErrors() || (m_warningsAsError && m_validationResult.hasWarnings()));
    }

    /**
     * Returns whether container element settings where changed.<p>
     *
     * @return <ode>true</code> in case container element settings where changed
     */
    public boolean isHasChangedSettings() {

        return m_hasChangedSettings;
    }
}
