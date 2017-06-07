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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.validation.I_CmsValidator;

import java.util.HashMap;

/**
 * The interface for a form field.<p>
 *
 * A form field consists of a widget for entering values, some metadata like a label text/description,
 * and a set of validators for validating the field.
 *
 * @since 8.0.0
 *
 */
public interface I_CmsFormField {

    /**
     * A simple map class containing strings to direct the layout of a form field.<p>
     */
    public class LayoutData extends HashMap<String, String> {

        /** Serial id for serialization. */
        private static final long serialVersionUID = -1938342399843773050L;
        // nothing new
    }

    /**
     * An enum which represents the validation status of a field.<p>
     */
    enum ValidationStatus {
        /** The field's content is invalid. */
        invalid, /** It is unknown whether the field's content is valid. */
        unknown, /** The field's content is valid. */
        valid
    }

    /**
     * Binds a model object to the form field.<p>
     *
     * @param model the model object
     */
    void bind(I_CmsStringModel model);

    /**
     * Returns the default value for the form field.<p>
     *
     * @return the default value for the form field
     */
    Object getDefaultValue();

    /**
     * The description of the form field, or null if there is no description.<p>
     *
     * @return a description string or null
     */
    String getDescription();

    /**
     * Returns the id of this form field, or null if the form field id has not been set.
     *
     * If a form field without an id is added to a form, typically it will receive a synthetic id.
     *
     * @return the id
     */
    String getId();

    /**
     * The label of the form field, or null if there is no label.<p>
     *
     * @return a label or null
     */
    String getLabel();

    /**
     * Returns the layout data for this field.<p>
     *
     * @return the layout data for this field
     */
    LayoutData getLayoutData();

    /**
     * Returns the model object for this field.
     *
     * @return the model used for this field
     */
    I_CmsStringModel getModel();

    /**
     * Returns the model id.<p>
     *
     * @return the model id
     */
    String getModelId();

    /**
     * Returns the model value.<p>
     *
     * @return the model value
     */
    String getModelValue();

    /**
     * Returns the validation status of this form field.<p>
     *
     * If the field has no validator, this should always return <code>valid</code>.
     *
     * @return the validation status
     */
    ValidationStatus getValidationStatus();

    /**
     * Returns the validator for this form field, or null if the field has no validator.<p>
     *
     * @return a validator or null
     */
    I_CmsValidator getValidator();

    /**
     * Returns the widget used by this form field.<p>
     *
     * @return a widget
     */
    I_CmsFormWidget getWidget();

    /**
     * Sets the id of this form field.<p>
     *
     * @param id the new id
     */
    void setId(String id);

    /**
     * Updates the validation status of this form field.<p>
     *
     * This will only have an effect if the field has a validator.<p>
     *
     * @param status the new validation status
     */
    void setValidationStatus(ValidationStatus status);

    /**
     * Sets the validator for this form field.<p>
     *
     * @param validator the new validator
     */
    void setValidator(I_CmsValidator validator);

    /**
     * Removes the binding to this form field's model.<p>
     */
    void unbind();

}
