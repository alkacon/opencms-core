/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/validation/Attic/CmsValidationController.java,v $
 * Date   : $Date: 2010/06/15 12:18:20 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.validation;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for coordinating the synchronous and asynchronous validation for 
 * a set of form fields.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 *
 * @since 8.0.0
 */
public class CmsValidationController implements I_CmsValidationController {

    /** A map containing all validation queries which should be executed asynchronously. */
    protected Map<String, CmsValidationQuery> m_validationQueries = new HashMap<String, CmsValidationQuery>();

    /** The list of form fields which should be validated. */
    private Collection<I_CmsFormField> m_fields;

    /** The validation handler which will receive the results of the validation. */
    private I_CmsValidationHandler m_handler;

    /** A flag to make sure that the validation controller is not used twice. */
    private boolean m_isNew = true;

    /** A flag indicating whether the validation has been successful. */
    private boolean m_validationOk;

    /**
     * Creates a new validation controller for a list of form fields.<p>
     * 
     * @param fields the fields which should be validated
     * @param handler the validation handler 
     */
    public CmsValidationController(Collection<I_CmsFormField> fields, I_CmsValidationHandler handler) {

        m_fields = fields;
        m_handler = handler;
    }

    /**
     * Creates a new validation controller for a single form field.<p>
     * 
     * @param field the form field 
     * @param handler the validation handler
     */
    public CmsValidationController(I_CmsFormField field, I_CmsValidationHandler handler) {

        m_fields = new ArrayList<I_CmsFormField>();
        m_fields.add(field);
        m_handler = handler;
    }

    /**
     * @see org.opencms.gwt.client.validation.I_CmsValidationController#provideValidationResult(java.lang.String, org.opencms.gwt.shared.CmsValidationResult)
     */
    public void provideValidationResult(String field, CmsValidationResult result) {

        if (result.getErrorMessage() != null) {
            m_validationOk = false;
        }

        m_handler.onValidationResult(field, result);
    }

    /**
     * Starts the validation.<p>
     */
    public void startValidation() {

        assert m_isNew : "A validation controller can only be used once!";
        m_isNew = false;

        m_validationOk = true;
        for (I_CmsFormField field : m_fields) {
            I_CmsFormField.ValidationStatus status = field.getValidationStatus();
            switch (status) {
                case unknown:
                    I_CmsValidator validator = field.getValidator();
                    validator.validate(field, this);
                    break;
                case invalid:
                    m_validationOk = false;
                    break;
                default:
                    break;
            }
        }
        if (m_validationQueries.isEmpty()) {
            m_handler.onValidationFinished(m_validationOk);
        } else {
            startAsyncValidation();
        }

    }

    /**
     * @see org.opencms.gwt.client.validation.I_CmsValidationController#validateAsync(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void validateAsync(String field, String value, String validator, String config) {

        m_validationQueries.put(field, new CmsValidationQuery(validator, value, config));
    }

    /**
     * Internal method which is executed when the results of the asynchronous validation are received from the server.<p>
     * 
     * @param results the validation results 
     */
    protected void onReceiveValidationResults(Map<String, CmsValidationResult> results) {

        for (Map.Entry<String, CmsValidationResult> resultEntry : results.entrySet()) {
            String fieldName = resultEntry.getKey();
            CmsValidationResult result = resultEntry.getValue();
            provideValidationResult(fieldName, result);
        }
        m_handler.onValidationFinished(m_validationOk);
    }

    /**
     * Starts the asynchronous validation.<p>
     */
    private void startAsyncValidation() {

        CmsRpcAction<Map<String, CmsValidationResult>> action = new CmsRpcAction<Map<String, CmsValidationResult>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0);
                CmsCoreProvider.getService().validate(m_validationQueries, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Map<String, CmsValidationResult> result) {

                stop(false);
                onReceiveValidationResults(result);
            }

        };
        action.execute();
    }

}
