/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/wrapper/CmsResourceWrapperXmlPage.java,v $
 * Date   : $Date: 2007/02/15 15:54:20 $
 * Version: $Revision: 1.1.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.commons.CmsPropertyAdvanced;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * A resource type wrapper for xml page files, which explodes the xml pages to folders.<p>
 *
 * The created folder contains the locales as folders and the elements as files. Additionaly
 * you find files for the control code, the individual properties and the shared properties.<p>
 *
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.2 $
 * 
 * @since 6.5.6
 */
public class CmsResourceWrapperXmlPage extends A_CmsResourceWrapper {

    /** The extension to use for elements. */
    private static final String EXTENSION_ELEMENT = "html";

    /** The name of the element to use for the controlcode. */
    private static final String NAME_ELEMENT_CONTROLCODE = "controlcode.xml";

    /** The name of the element to use for the individual properties. */
    private static final String NAME_ELEMENT_IND_PROPERTIES = "individual.properties";

    /** The name of the element to use for the shared properties. */
    private static final String NAME_ELEMENT_SHARED_PROPERTIES = "shared.properties";

    /** Table with the states of the virtual files. */
    private static final Hashtable TMP_FILE_TABLE = new Hashtable();

    /** Array with the names of the virtual files. */
    private static final String[] VIRTUAL_FILES = {
        NAME_ELEMENT_CONTROLCODE,
        NAME_ELEMENT_IND_PROPERTIES,
        NAME_ELEMENT_SHARED_PROPERTIES};

    /** List containing the names of the virtual files. */
    private static final List VIRTUAL_FILES_LIST = Arrays.asList(VIRTUAL_FILES);

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#copyResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.file.CmsResource.CmsResourceCopyMode)
     */
    public boolean copyResource(CmsObject cms, String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        // only allow copying of xml pages at whole or locales and elements inside the same xml page
        CmsResource srcXmlPage = findXmlPage(cms, source);

        if (srcXmlPage != null) {
            String srcPath = getSubPath(cms, srcXmlPage, source);

            // if the source is the xml page itself just copy the resource
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(srcPath)) {
                cms.copyResource(source, destination, siblingMode);
                return true;
            } else {

                // only a locale or an element should be copied
                CmsResource destXmlPage = findXmlPage(cms, destination);
                if (srcXmlPage.equals(destXmlPage)) {

                    // copying inside the same xml page resource
                    String destPath = getSubPath(cms, destXmlPage, destination);

                    String[] srcTokens = srcPath.split("/");
                    String[] destTokens = destPath.split("/");

                    if (srcTokens.length == destTokens.length) {

                        CmsFile srcFile = CmsFile.upgrade(srcXmlPage, cms);
                        CmsXmlPage srcXml = CmsXmlPageFactory.unmarshal(cms, srcFile);

                        if (srcTokens.length == 1) {

                            if (srcTokens[0].equals(NAME_ELEMENT_CONTROLCODE)) {

                                // do nothing
                            } else {

                                // copy locale
                                srcXml.copyLocale(new Locale(srcTokens[0]), new Locale(destTokens[0]));
                            }
                        } else if (srcTokens.length == 2) {

                            // TODO: copy element
                        }

                        // write files
                        srcFile.setContents(srcXml.marshal());
                        cms.writeFile(srcFile);

                        return true;
                    } else {

                        // TODO: error: destination path is invalid
                    }
                } else {

                    // TODO: error: copying only allowed inside the same xml page
                }
            }
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, int type, byte[] content, List properties)
    throws CmsException, CmsIllegalArgumentException {

        // cut off trailing slash
        if (resourcename.endsWith("/")) {
            resourcename = resourcename.substring(0, resourcename.length() - 1);
        }

        // creating new xml pages if type is a folder and the name ends with .html
        if ((type == CmsResourceTypeFolder.getStaticTypeId()) && resourcename.endsWith(".html")) {

            // mark in temp file table that the visual files does not exist yet
            Iterator iter = VIRTUAL_FILES_LIST.iterator();
            while (iter.hasNext()) {
                TMP_FILE_TABLE.put(resourcename + "/" + (String)iter.next(), new Integer(0));
            }

            return cms.createResource(resourcename, CmsResourceTypeXmlPage.getStaticTypeId());
        }

        // find the xml page this is for
        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {

            // get the path below the xml page
            String path = getSubPath(cms, xmlPage, resourcename);

            // and the path without the site root
            String rootPath = cms.getRequestContext().removeSiteRoot(xmlPage.getRootPath());

            CmsFile file = CmsFile.upgrade(xmlPage, cms);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            // mark visual files as created in temp file table
            if (VIRTUAL_FILES_LIST.contains(path)) {
                TMP_FILE_TABLE.remove(resourcename);

                // lock the resource
                cms.lockResource(rootPath);

                // set individual properties
                if (NAME_ELEMENT_IND_PROPERTIES.equals(path)) {

                    // set the properties read from the content
                    setProperties(cms, rootPath, content, CmsProperty.TYPE_INDIVIDUAL);
                }

                // set shared properties
                if (NAME_ELEMENT_SHARED_PROPERTIES.equals(path)) {

                    // set the properties read from the content
                    setProperties(cms, rootPath, content, CmsProperty.TYPE_SHARED);
                }

                return file;
            }

            String[] tokens = path.split("/");
            if (tokens.length == 1) {

                Locale locale = new Locale(tokens[0]);

                // workaround: empty xmlpages always have the default locale "en" set
                if (file.getLength() == 0) {
                    Iterator iter = xml.getLocales().iterator();
                    while (iter.hasNext()) {
                        xml.removeLocale((Locale)iter.next());
                    }
                }

                // create new locale
                xml.addLocale(cms, locale);

                // save the xml page
                file.setContents(xml.marshal());

                // lock the resource
                cms.lockResource(rootPath);

                // write file
                cms.writeFile(file);

            } else if (tokens.length == 2) {

                String name = tokens[1];
                if (name.endsWith(EXTENSION_ELEMENT)) {
                    name = name.substring(0, name.length() - EXTENSION_ELEMENT.length() - 1);
                }

                // create new element
                xml.addValue(name, new Locale(tokens[0]));

                // set the content
                xml.setStringValue(cms, name, new Locale(tokens[0]), getStringValue(cms, file, content));

                // save the xml page
                file.setContents(xml.marshal());

                // lock the resource
                cms.lockResource(rootPath);

                // write file
                cms.writeFile(file);
            }

            return file;
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#deleteResource(CmsObject, String, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    public boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode)
    throws CmsException {

        // find the xml page this is for
        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {

            // cut off trailing slash
            if (resourcename.endsWith("/")) {
                resourcename = resourcename.substring(0, resourcename.length() - 1);
            }

            // get the path below the xml page
            String path = getSubPath(cms, xmlPage, resourcename);

            // if sub path is empty
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {

                // delete the xml page itself
                cms.deleteResource(resourcename, siblingMode);

                // remove all virtual files for this resource
                Iterator iter = VIRTUAL_FILES_LIST.iterator();
                while (iter.hasNext()) {
                    TMP_FILE_TABLE.remove(resourcename + "/" + (String)iter.next());
                }

                return true;
            }

            CmsFile file = CmsFile.upgrade(xmlPage, cms);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            String[] tokens = path.split("/");
            if (tokens.length == 1) {

                // deleting a virtual file
                if (VIRTUAL_FILES_LIST.contains(tokens[0])) {

                    // mark the virtual file in the temp file table as deleted
                    TMP_FILE_TABLE.put(resourcename, new Integer(0));
                } else {

                    // delete locale
                    xml.removeLocale(new Locale(tokens[0]));

                    // save the xml page
                    file.setContents(xml.marshal());
                    cms.writeFile(file);
                }

            } else if (tokens.length == 2) {

                String name = tokens[1];
                if (name.endsWith(EXTENSION_ELEMENT)) {
                    name = name.substring(0, name.length() - EXTENSION_ELEMENT.length() - 1);
                }

                // delete element
                xml.removeValue(name, new Locale(tokens[0]));

                // save the xml page
                file.setContents(xml.marshal());
                cms.writeFile(file);
            }

            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        CmsResource xmlPage = findXmlPage(cms, resource.getRootPath());
        if (xmlPage != null) {
            return cms.getLock(xmlPage);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getResourcesInFolder(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public List getResourcesInFolder(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {
            String path = getSubPath(cms, xmlPage, resourcename);
            String rootPath = cms.getRequestContext().removeSiteRoot(xmlPage.getRootPath());

            ArrayList ret = new ArrayList();

            CmsFile file = CmsFile.upgrade(xmlPage, cms);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {

                // sub path is empty -> return all existing locales for the resource
                if (file.getLength() == 0) {
                    return ret;
                }

                List locales = xml.getLocales();
                Iterator iter = locales.iterator();
                while (iter.hasNext()) {
                    Locale locale = (Locale)iter.next();
                    ret.add(getResourceForLocale(xmlPage, locale));
                }

                // check temp file table to add virtual file
                iter = VIRTUAL_FILES_LIST.iterator();
                while (iter.hasNext()) {

                    String virtualFileName = (String)iter.next();
                    String virtualFilePath = rootPath + "/" + virtualFileName;

                    if ((!TMP_FILE_TABLE.containsKey(virtualFilePath))
                        || (!TMP_FILE_TABLE.get(virtualFilePath).equals(new Integer(0)))) {

                        // read the control code resource
                        if (virtualFileName.equals(NAME_ELEMENT_CONTROLCODE)) {

                            CmsFile tmpFile = new CmsFile(setRootPath(xmlPage, xmlPage.getRootPath()
                                + "/"
                                + NAME_ELEMENT_CONTROLCODE, CmsResourceTypePlain.getStaticTypeId()));
                            tmpFile.setContents(file.getContents());
                            ret.add(tmpFile);
                        } else if (virtualFileName.equals(NAME_ELEMENT_IND_PROPERTIES)) {

                            ret.add(createPropertyResource(cms, xmlPage, CmsProperty.TYPE_INDIVIDUAL));
                        } else if (virtualFileName.equals(NAME_ELEMENT_SHARED_PROPERTIES)) {

                            ret.add(createPropertyResource(cms, xmlPage, CmsProperty.TYPE_SHARED));
                        }
                    }
                }

            } else {

                // sub path is a locale -> return all elements for this locale
                List names = xml.getNames(new Locale(path));
                Iterator iter = names.iterator();
                while (iter.hasNext()) {
                    String name = (String)iter.next();
                    ret.add(getResourceForElement(xmlPage, path, name, 0));
                }

            }

            return ret;
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getSystemLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsLock getSystemLock(CmsObject cms, CmsResource resource) throws CmsException {

        CmsResource xmlPage = findXmlPage(cms, resource.getRootPath());
        if (xmlPage != null) {
            return cms.getSystemLock(xmlPage);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        try {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
            if (resType instanceof CmsResourceTypeXmlPage) {
                return true;
            }
        } catch (CmsException ex) {
            // noop
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean lockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = findXmlPage(cms, resourcename);
        if (res != null) {
            cms.lockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#moveResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException {

        // only allow copying of xml pages at whole or locales and elements inside the same xml page
        CmsResource srcXmlPage = findXmlPage(cms, source);

        if (srcXmlPage != null) {
            String srcPath = getSubPath(cms, srcXmlPage, source);

            // if the source is the xml page itself just copy the resource
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(srcPath)) {
                cms.moveResource(source, destination);
                return true;
            } else {

                // only a locale or an element should be copied
                CmsResource destXmlPage = findXmlPage(cms, destination);
                if (srcXmlPage.equals(destXmlPage)) {

                    // copying inside the same xml page resource
                    String destPath = getSubPath(cms, destXmlPage, destination);

                    String[] srcTokens = srcPath.split("/");
                    String[] destTokens = destPath.split("/");

                    if (srcTokens.length == destTokens.length) {

                        CmsFile srcFile = CmsFile.upgrade(srcXmlPage, cms);
                        CmsXmlPage srcXml = CmsXmlPageFactory.unmarshal(cms, srcFile);

                        if (srcTokens.length == 1) {

                            // copy locale
                            srcXml.moveLocale(new Locale(srcTokens[0]), new Locale(destTokens[0]));
                        } else if (srcTokens.length == 2) {

                            // TODO: move element
                        }

                        // write file
                        srcFile.setContents(srcXml.marshal());
                        cms.writeFile(srcFile);
                    } else {

                        // TODO: error: destination path is invalid
                    }
                } else {

                    // TODO: error: moving only allowed inside the same xml page
                }
            }

            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readFile(CmsObject, String, CmsResourceFilter)
     */
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        // find the xml page this is for
        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {

            // cut off trailing slash
            if (resourcename.endsWith("/")) {
                resourcename = resourcename.substring(0, resourcename.length() - 1);
            }

            // get the path below the xml page
            String path = getSubPath(cms, xmlPage, resourcename);

            CmsFile file = CmsFile.upgrade(xmlPage, cms);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            String[] tokens = path.split("/");
            if (tokens.length == 1) {

                // check temp file table to remove deleted virtual files
                if ((TMP_FILE_TABLE.containsKey(resourcename))
                    && (TMP_FILE_TABLE.get(resourcename).equals(new Integer(0)))) {
                    return null;
                }

                // read the control code resource
                if (tokens[0].equals(NAME_ELEMENT_CONTROLCODE)) {

                    CmsFile ret = new CmsFile(setRootPath(xmlPage, xmlPage.getRootPath()
                        + "/"
                        + NAME_ELEMENT_CONTROLCODE));
                    ret.setContents(file.getContents());
                    return ret;
                } else if (tokens[0].equals(NAME_ELEMENT_IND_PROPERTIES)) {

                    return createPropertyResource(cms, xmlPage, CmsProperty.TYPE_INDIVIDUAL);
                } else if (tokens[0].equals(NAME_ELEMENT_SHARED_PROPERTIES)) {

                    return createPropertyResource(cms, xmlPage, CmsProperty.TYPE_SHARED);
                }
            } else if (tokens.length == 2) {

                // cut off the html suffix
                String name = tokens[1];
                if (name.endsWith("." + EXTENSION_ELEMENT)) {
                    name = name.substring(0, name.length() - 5);
                }

                if (xml.hasValue(name, new Locale(tokens[0]))) {

                    byte[] content = xml.getStringValue(cms, name, new Locale(tokens[0])).getBytes();
                    CmsResource resElem = getResourceForElement(xmlPage, tokens[0], name, content.length);
                    CmsFile fileElem = new CmsFile(resElem);

                    fileElem.setContents(content);
                    return fileElem;
                }
            }
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(CmsObject, String, CmsResourceFilter)
     */
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        // check if the resource exists
        if (cms.existsResource(resourcename)) {

            // return the xml page resource as a folder
            CmsResource res = cms.readResource(resourcename, filter);
            if (res.getTypeId() == CmsResourceTypeXmlPage.getStaticTypeId()) {
                return setFolder(res, true);
            }

            return null;
        } else {

            // find the xml page this is for
            CmsResource xmlPage = findXmlPage(cms, resourcename);
            if (xmlPage != null) {

                // cut off trailing slash
                if (resourcename.endsWith("/")) {
                    resourcename = resourcename.substring(0, resourcename.length() - 1);
                }

                // get the path below the xml page
                String path = getSubPath(cms, xmlPage, resourcename);

                CmsFile file = CmsFile.upgrade(xmlPage, cms);
                CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

                String[] tokens = path.split("/");
                if (tokens.length == 1) {

                    // check temp file table to remove deleted virtual files
                    if ((TMP_FILE_TABLE.containsKey(resourcename))
                        && (TMP_FILE_TABLE.get(resourcename).equals(new Integer(0)))) {
                        return null;
                    }

                    // read the control code resource
                    if (tokens[0].equals(NAME_ELEMENT_CONTROLCODE)) {

                        return setRootPath(xmlPage, xmlPage.getRootPath() + "/" + NAME_ELEMENT_CONTROLCODE);
                    } else if (tokens[0].equals(NAME_ELEMENT_IND_PROPERTIES)) {

                        return createPropertyResource(cms, xmlPage, CmsProperty.TYPE_INDIVIDUAL);
                    } else if (tokens[0].equals(NAME_ELEMENT_SHARED_PROPERTIES)) {

                        return createPropertyResource(cms, xmlPage, CmsProperty.TYPE_SHARED);
                    } else {

                        Locale locale = new Locale(tokens[0]);
                        if (xml.hasLocale(locale) && (file.getLength() > 0)) {
                            return getResourceForLocale(xmlPage, locale);
                        }
                    }
                } else if (tokens.length == 2) {

                    // cut off the html suffix
                    String name = tokens[1];
                    if (name.endsWith("." + EXTENSION_ELEMENT)) {
                        name = name.substring(0, name.length() - 5);
                    }

                    if (xml.hasValue(name, new Locale(tokens[0]))) {
                        return getResourceForElement(xmlPage, tokens[0], name, xml.getStringValue(
                            cms,
                            name,
                            new Locale(tokens[0])).getBytes().length);
                    }
                }

            }

            return null;
        }
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#unlockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean unlockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = findXmlPage(cms, resourcename);
        if (res != null) {
            cms.unlockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#wrapResource(CmsObject, CmsResource)
     */
    public CmsResource wrapResource(CmsObject cms, CmsResource res) {

        return setFolder(res, true);
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        CmsResource xmlPage = findXmlPage(cms, resource.getRootPath());
        if (xmlPage != null) {
            String path = getSubPath(cms, xmlPage, cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
            String rootPath = cms.getRequestContext().removeSiteRoot(xmlPage.getRootPath());

            CmsFile file = CmsFile.upgrade(xmlPage, cms);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            // set individual properties
            if (NAME_ELEMENT_IND_PROPERTIES.equals(path)) {

                // lock the resource
                cms.lockResource(rootPath);

                // set the properties read from the content
                setProperties(cms, rootPath, resource.getContents(), CmsProperty.TYPE_INDIVIDUAL);
            }

            // set individual properties
            if (NAME_ELEMENT_SHARED_PROPERTIES.equals(path)) {

                // lock the resource
                cms.lockResource(rootPath);

                // set the properties read from the content
                setProperties(cms, rootPath, resource.getContents(), CmsProperty.TYPE_SHARED);
            }

            String[] tokens = path.split("/");
            if (tokens.length == 2) {

                // cut off the html suffix
                String name = tokens[1];
                if (name.endsWith("." + EXTENSION_ELEMENT)) {
                    name = name.substring(0, name.length() - 5);
                }

                // set content
                xml.setStringValue(cms, name, new Locale(tokens[0]), getStringValue(cms, file, resource.getContents()));

                // write file
                file.setContents(xml.marshal());
                cms.writeFile(file);

                return file;
            }

        }

        return null;
    }

    /**
     * Creates a CmsFile with the individual properties as content.<p>
     * 
     * @param cms the initialized CmsObject
     * @param res the resource where to read the properties from
     * @param type the type of the properties to read {@link CmsProperty#TYPE_INDIVIDUAL} / {@link CmsProperty#TYPE_SHARED} 
     * 
     * @return the created CmsFile with the individual properties as the content
     * 
     * @throws CmsException
     */
    private CmsFile createPropertyResource(CmsObject cms, CmsResource res, String type) throws CmsException {

        StringBuffer content = new StringBuffer();

        List propertyDef = cms.readAllPropertyDefinitions();
        Map activeProperties = CmsPropertyAdvanced.getPropertyMap(cms.readPropertyObjects(res, false));

        // iterate over all possible properties for the resource
        Iterator i = propertyDef.iterator();
        while (i.hasNext()) {
            CmsPropertyDefinition currentPropertyDef = (CmsPropertyDefinition)i.next();

            String propName = currentPropertyDef.getName();
            CmsProperty currentProperty = (CmsProperty)activeProperties.get(propName);
            if (currentProperty == null) {
                currentProperty = new CmsProperty();
            }

            String propValue = null;
            if (CmsProperty.TYPE_SHARED.equals(type)) {
                propValue = currentProperty.getResourceValue();
            } else {
                propValue = currentProperty.getStructureValue();
            }
            if (propValue == null) {
                propValue = "";
            }

            content.append(propName);
            content.append("=");
            content.append(propValue);
            content.append("\n");
        }

        CmsFile ret = null;
        if (CmsProperty.TYPE_SHARED.equals(type)) {
            ret = new CmsFile(setRootPath(res, res.getRootPath() + "/" + NAME_ELEMENT_SHARED_PROPERTIES, CmsResourceTypePlain.getStaticTypeId()));
        } else {
            ret = new CmsFile(setRootPath(res, res.getRootPath() + "/" + NAME_ELEMENT_IND_PROPERTIES, CmsResourceTypePlain.getStaticTypeId()));
        }

        ret.setContents(content.toString().getBytes());
        return ret;
    }

    /**
     * Checks if the given resource name (full path) belongs to a xml page.<p>
     * 
     * It works up the path till a resource for the path exists in the VFS. If the found resource is a xml page, 
     * this resource is returned. If the path does not belong to a xml page null will be returned.<p> 
     * 
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource (full path) to check
     * 
     * @return the found resource of type xml page or null if not found
     */
    private CmsResource findXmlPage(CmsObject cms, String resourcename) {

        // get the full folder path of the resource to start from
        String path = cms.getRequestContext().removeSiteRoot(resourcename);
        do {

            // check if the current folder exists
            if (cms.existsResource(path)) {

                try {
                    CmsResource res = cms.readResource(path);
                    I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
                    if (resType instanceof CmsResourceTypeXmlPage) {
                        return res;
                    } else {
                        break;
                    }
                } catch (CmsException ex) {
                    break;
                }

            } else {

                // folder does not exist, go up one folder
                path = CmsResource.getParentFolder(path);
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
            }

            if (CmsStringUtil.isEmpty(path)) {

                // site root or root folder reached and no folder found
                break;
            }
        } while (true);

        return null;
    }

    /**
     * Returns the element for the locale and the name as a virtual resource.<p>
     * 
     * A new (virtual) resource is created with the element of the locale found in the xml page resource. The
     * new created resource uses the values of the origin resource of the xml page where it is possible.<p>
     * 
     * @param xmlPage the xml page resource with the element to create a resource
     * @param locale the locale to use for the element
     * @param name the name of the element to use
     * @param length the length of the element content
     * 
     * @return a new created CmsResource
     */
    private CmsResource getResourceForElement(CmsResource xmlPage, String locale, String name, int length) {

        return new CmsResource(
            null,
            null,
            xmlPage.getRootPath() + "/" + locale + "/" + name + "." + EXTENSION_ELEMENT,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            xmlPage.getFlags(),
            xmlPage.getProjectLastModified(),
            xmlPage.getState(),
            xmlPage.getDateCreated(),
            xmlPage.getUserCreated(),
            xmlPage.getDateLastModified(),
            xmlPage.getUserLastModified(),
            xmlPage.getDateReleased(),
            xmlPage.getDateExpired(),
            xmlPage.getSiblingCount(),
            length);
    }

    /**
     * Creates a new virtual resource for the locale in the xml page.<p>
     * 
     * The new created resource uses the values of the origin resource of the xml page where it is possible.<p>
     * 
     * @param xmlPage the xml page resource with the locale to create a resource of
     * @param locale the locale in the xml page to use for the new resource
     * 
     * @return a new created CmsResource
     */
    private CmsResource getResourceForLocale(CmsResource xmlPage, Locale locale) {

        return new CmsResource(
            null,
            null,
            xmlPage.getRootPath() + "/" + locale.getLanguage() + "/",
            CmsResourceTypeFolder.getStaticTypeId(),
            true,
            xmlPage.getFlags(),
            xmlPage.getProjectLastModified(),
            xmlPage.getState(),
            xmlPage.getDateCreated(),
            xmlPage.getUserCreated(),
            xmlPage.getDateLastModified(),
            xmlPage.getUserLastModified(),
            xmlPage.getDateReleased(),
            xmlPage.getDateExpired(),
            xmlPage.getSiblingCount(),
            xmlPage.getLength());
    }

    /**
     * Returns the content as a string.<p>
     * 
     * Uses the correct encoding.<p>
     * 
     * @param cms the initialized CmsObject
     * @param resource the resource where the content belongs to to find the correct encoding
     * @param content the byte array which should be converted into a string
     * 
     * @return the content as a string
     * 
     * @throws CmsException if something goes wrong
     */
    private String getStringValue(CmsObject cms, CmsResource resource, byte[] content) throws CmsException {

        // get the encoding for the resource
        CmsProperty prop = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true);
        String enc = prop.getValue();
        if (enc == null) {
            enc = OpenCms.getSystemInfo().getDefaultEncoding();
        }

        // create a String with the right encoding
        return CmsEncoder.createString(content, enc);
    }

    /**
     * Returns the path inside a xml page.<p>
     * 
     * The remaining path inside a xml page can be the locale and the element name.<p>
     * 
     * @param cms the initialized CmsObject
     * @param xmlPage the xml page where the resourcename belongs to
     * @param resourcename the full path of the resource (pointing inside the xml page)
     * @return the remaining path inside the xml page without the leading slash
     */
    private String getSubPath(CmsObject cms, CmsResource xmlPage, String resourcename) {

        if (xmlPage != null) {
            String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
            String path = rootPath.substring(xmlPage.getRootPath().length());

            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            return path;
        }

        return null;
    }

    /**
     * Change the folder type of the resource.<p>
     * 
     * @param res the resource where to change the path
     * @param isFolder the value which should be set at the resource
     * 
     * @return the resource with the changed folder type
     */
    private CmsResource setFolder(CmsResource res, boolean isFolder) {

        String path = res.getRootPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        
        return new CmsResource(
            res.getStructureId(),
            res.getResourceId(),
            path,
            res.getTypeId(),
            isFolder,
            res.getFlags(),
            res.getProjectLastModified(),
            res.getState(),
            res.getDateCreated(),
            res.getUserCreated(),
            res.getDateLastModified(),
            res.getUserLastModified(),
            res.getDateReleased(),
            res.getDateExpired(),
            res.getSiblingCount(),
            res.getLength());
    }

    /**
     * Set the content (formatted as a property file) as properties to the resource.<p>
     * 
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource where to set the properties
     * @param content the properties to set (formatted as a property file)
     * @param type the type of the properties to read {@link CmsProperty#TYPE_INDIVIDUAL} / {@link CmsProperty#TYPE_SHARED}
     * 
     * @throws CmsException if something goes wrong
     */
    private void setProperties(CmsObject cms, String resourcename, byte[] content, String type) throws CmsException {

        Properties properties = new Properties();
        try {
            List propList = new ArrayList();
            properties.load(new ByteArrayInputStream(content));

            Iterator iter = properties.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                String value = (String)properties.get(key);

                if (CmsProperty.TYPE_SHARED.equals(type)) {
                    propList.add(new CmsProperty(key, null, value));
                } else {
                    propList.add(new CmsProperty(key, value, null));
                }
            }

            cms.writePropertyObjects(resourcename, propList);
        } catch (IOException e) {
            // noop
        }

    }

    /**
     * Change the root path and the type id of the resource.<p>
     * 
     * @param res the resource where to change the path
     * @param rootPath the root path which should be set at the resource
     * @param typeId the type id to be set at the resource
     * 
     * @return the resource with the changed root path
     */

    private CmsResource setRootPath(CmsResource res, String rootPath, int typeId) {

        return new CmsResource(
            res.getStructureId(),
            res.getResourceId(),
            rootPath,
            typeId,
            res.isFolder(),
            res.getFlags(),
            res.getProjectLastModified(),
            res.getState(),
            res.getDateCreated(),
            res.getUserCreated(),
            res.getDateLastModified(),
            res.getUserLastModified(),
            res.getDateReleased(),
            res.getDateExpired(),
            res.getSiblingCount(),
            res.getLength());
    }

    /**
     * Change the root path of the resource.<p>
     * 
     * @param res the resource where to change the path
     * @param rootPath the root path which should be set at the resource
     * 
     * @return the resource with the changed root path
     */

    private CmsResource setRootPath(CmsResource res, String rootPath) {

        return setRootPath(res, rootPath, res.getTypeId());
    }
}
