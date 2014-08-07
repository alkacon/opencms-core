/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client.entity;

import org.opencms.acacia.shared.I_Type;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A value type.<p>
 */
public final class Type extends JavaScriptObject implements I_Type {

    /**
     * Hiding constructor.<p>
     */
    protected Type() {

        // nothing to do
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#addAttribute(java.lang.String, java.lang.String, int, int)
     */
    public native void addAttribute(String attributeName, String attributeType, int minOccurrence, int maxOccurrence) /*-{
                                                                                                                      var attribute = this.attributes
                                                                                                                      .add(
                                                                                                                      @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName),
                                                                                                                      @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeType),
                                                                                                                      minOccurrence, maxOccurrence);

                                                                                                                      }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeMaxOccurrence(java.lang.String)
     */
    public native int getAttributeMaxOccurrence(String attributeName) /*-{

                                                                      var attr = this.attributes
                                                                      .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName));
                                                                      return attr.max;
                                                                      }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeMinOccurrence(java.lang.String)
     */
    public native int getAttributeMinOccurrence(String attributeName) /*-{

                                                                      var attr = this.attributes
                                                                      .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName));
                                                                      return attr.min;
                                                                      }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeNames()
     */
    public List<String> getAttributeNames() {

        List<String> attributes = new ArrayList<String>();
        prepareNames(attributes);
        return attributes;
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeType(java.lang.String)
     */
    public native I_Type getAttributeType(String attributeName) /*-{
                                                                var attr = this.attributes
                                                                .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName));
                                                                return (attr === undefined) ? null : this.vie.types.get(attr.range[0]);
                                                                }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeTypeName(java.lang.String)
     */
    public native String getAttributeTypeName(String attributeName) /*-{

                                                                    var attr = this.attributes
                                                                    .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName));
                                                                    var result = (attr === undefined) ? null : attr.range[0];
                                                                    return @org.opencms.acacia.client.entity.Vie::removePointyBrackets(Ljava/lang/String;)(result);
                                                                    }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#getChoiceMaxOccurrence()
     */
    public native int getChoiceMaxOccurrence() /*-{

                                               return this.choiceMaxOccurrence ? this.choiceMaxOccurrence : 0;
                                               }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#getId()
     */
    public String getId() {

        return Vie.removePointyBrackets(getTypeName());
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#isChoice()
     */
    public native boolean isChoice() /*-{

                                     return this.choiceMaxOccurrence ? this.choiceMaxOccurrence > 0 : false;
                                     }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#isSimpleType()
     */
    public native boolean isSimpleType() /*-{

                                         return this.attributes == null || this.attributes.list().length == 0;
                                         }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Type#setChoiceMaxOccurrence(int)
     */
    public native void setChoiceMaxOccurrence(int choiceMaxOccurrence)/*-{
                                                                      this.choiceMaxOccurrence = choiceMaxOccurrence;
                                                                      }-*/;

    /**
     * Returns the internal type name - including pointy brackets.<p>
     * 
     * @return the type name
     */
    private native String getTypeName() /*-{

                                        return this.id;
                                        }-*/;

    /**
     * Prepares the list of attribute names.<p>
     * 
     * @param list the attribute list 
     */
    private native void prepareNames(List<String> list) /*-{
                                                        var attributes = this.attributes.list();
                                                        for ( var i = 0; i < attributes.length; i++) {
                                                        var name = attributes[i].id;
                                                        name = @org.opencms.acacia.client.entity.Vie::removePointyBrackets(Ljava/lang/String;)(name);
                                                        list.@java.util.List::add(Ljava/lang/Object;)(name);
                                                        }
                                                        }-*/;
}
