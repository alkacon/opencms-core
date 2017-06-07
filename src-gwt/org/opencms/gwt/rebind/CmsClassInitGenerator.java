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

package org.opencms.gwt.rebind;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * This generator class generates a class with a method which calls all static initClass()
 * methods of classes that implement the {@link org.opencms.gwt.client.I_CmsHasInit} marker interface.<p>
 *
 *  @since 8.0.0
 */
public class CmsClassInitGenerator extends Generator {

    /** The name of the class to generate. */
    private static final String CLASS_NAME = "CmsClassInitializerImpl";

    /** The name of the interface passed into GWT.create(). */
    private static final String INIT_INTERFACE_NAME = "org.opencms.gwt.client.I_CmsClassInitializer";

    /** The name of the marker interface for the generator. */
    private static final String MARKER_INTERFACE_NAME = "org.opencms.gwt.client.I_CmsHasInit";

    /** The package of the class to generate. */
    private static final String PACKAGE_NAME = "org.opencms.gwt.client";

    /**
     * @see com.google.gwt.core.ext.Generator#generate(com.google.gwt.core.ext.TreeLogger, com.google.gwt.core.ext.GeneratorContext, java.lang.String)
     */
    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName)
    throws UnableToCompleteException {

        TypeOracle oracle = context.getTypeOracle();
        JClassType initClass = oracle.findType(MARKER_INTERFACE_NAME);
        List<JClassType> initTypes = new ArrayList<JClassType>();
        for (JClassType subtype : initClass.getSubtypes()) {
            try {
                JMethod method = subtype.getMethod("initClass", new JType[] {});
                if (!method.isStatic()) {
                    throw new NotFoundException();
                }
                initTypes.add(subtype);
            } catch (NotFoundException e) {
                logger.log(
                    TreeLogger.ERROR,
                    "Could not find initClass() method in class " + subtype.getQualifiedSourceName());
                throw new UnableToCompleteException();
            }
        }
        generateClass(logger, context, initTypes);
        return PACKAGE_NAME + "." + CLASS_NAME;
    }

    /**
     * This method generates the source code for the class initializer class.<p>
     *
     * @param logger the logger to be used
     * @param context the generator context
     * @param subclasses the classes for which the generated code should the initClass() method
     */
    public void generateClass(TreeLogger logger, GeneratorContext context, List<JClassType> subclasses) {

        PrintWriter printWriter = context.tryCreate(logger, PACKAGE_NAME, CLASS_NAME);
        if (printWriter == null) {
            return;
        }
        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(PACKAGE_NAME, CLASS_NAME);
        composer.addImplementedInterface(INIT_INTERFACE_NAME);
        SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);
        sourceWriter.println("public void initClasses() {");
        sourceWriter.indent();
        for (JClassType type : subclasses) {
            sourceWriter.println(type.getQualifiedSourceName() + ".initClass();");
        }
        sourceWriter.outdent();
        sourceWriter.println("}");
        sourceWriter.outdent();
        sourceWriter.println("}");
        context.commit(logger, printWriter);
    }
}
