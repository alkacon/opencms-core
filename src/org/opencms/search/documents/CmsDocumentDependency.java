/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.search.documents;

import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleComparator;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;

/**
 * Provides the dependency information about one search result document, 
 * used to generate the list of LGT document search results.<p> 
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.10 $
 */
public final class CmsDocumentDependency {

    /** 
     * Defines the possible dependency types.<p> 
     */
    public static enum DependencyType {

        /** A attachment dependency. */
        attachment,

        /** The main document dependency. */
        document,

        /** A variant dependency. */
        variant
    }

    /**
     * Comparator for locale sorted lists of dependencies.<p>
     */
    private static final class DepLocaleComparator implements Comparator<CmsDocumentDependency> {

        /**
         * Hides the public constructor.<p> 
         */
        DepLocaleComparator() {

            // NOOP
        }

        /**
         * Compares two dependencies by locale.<p>
         * 
         * @param d1 the first dependency to compare
         * @param d2 the second dependency to compare
         * 
         * @return the comparison result based on the locales of the dependencies
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsDocumentDependency d1, CmsDocumentDependency d2) {

            return CmsLocaleComparator.getComparator().compare(d1.getLocale(), d2.getLocale());
        }
    }

    /**
     * Comparator for title sorted lists of dependencies.<p>
     */
    private static final class DepTitleComparator implements Comparator<CmsDocumentDependency> {

        /**
         * Hides the public constructor.<p> 
         */
        DepTitleComparator() {

            // NOOP
        }

        /**
         * Compares two dependencies by title.<p>
         * 
         * @param d1 the first dependency to compare
         * @param d2 the second dependency to compare
         * 
         * @return the comparison result based on the titles of the dependencies
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsDocumentDependency d1, CmsDocumentDependency d2) {

            if ((d1.getTitle() != null) && (d2.getTitle() != null)) {
                return d1.getTitle().compareTo(d2.getTitle());
            }

            if ((d1.getTitle() == null) && (d2.getTitle() == null)) {
                return 0;
            }

            if (d1.getTitle() == null) {
                return -1;
            }

            return 1;
        }
    }

    /** Pattern to determine the document locale. */
    public static final Pattern DOC_PATTERN_LOCALE = Pattern.compile("(.*)_([a-z]{2}(?:_[A-Z]{2})?)($|[$|\\.|_])");

    /** Pattern to determine the document attachment number. */
    public static final Pattern DOC_PATTERN_NUMBER = Pattern.compile("(.*)_(\\d+)(\\.[^\\.^\\n]*)?$");

    /** Prefix for context attributes. */
    private static final String ATTR_DOC_DEPENDENCY = "CmsDocumentDependency.";

    /** Static locale comparator. */
    private static final Comparator<CmsDocumentDependency> COMPARATOR_LOCALE = new DepLocaleComparator();

    /** Static title comparator. */
    private static final Comparator<CmsDocumentDependency> COMPARATOR_TITLE = new DepTitleComparator();

    /** Field name in JSON. */
    private static final String JSON_ATTACHMENTS = "attachments";

    /** Field name in JSON. */
    private static final String JSON_DATE_CREATED = "dateCreated";

    /** Field name in JSON. */
    private static final String JSON_DATE_MODIFIED = "dateModified";

    /** Field name in JSON. */
    private static final String JSON_LANGUAGES = "languages";

    /** Field name in JSON. */
    private static final String JSON_MAIN = "main";

    /** Field name in JSON. */
    private static final String JSON_PATH = "path";

    /** Field name in JSON. */
    private static final String JSON_TITLE = "title";

    /** Field name in JSON. */
    private static final String JSON_UUID = "uuid";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDocumentDependency.class);

    /** The attachment number. */
    private Integer m_attachmentNumber;

    /** The document attachments . */
    private List<CmsDocumentDependency> m_attachments;

    /** Information used for displaying the creation date of this document. */
    private Date m_dateCreated;

    /** Information used for displaying the expiration date of this document. */
    private Date m_dateExpired;

    /** Information used for displaying the last modification date of this document. */
    private Date m_dateLastModified;

    /** Information used for displaying the owner department of this document. */
    private String m_department;

    /** The list of resources the main resource depends on, including the main resource itself. */
    private List<CmsDocumentDependency> m_dependencies;

    /** The attachment version number. */
    private String m_documentId;

    /** The file name of the document without attachment or locale suffixes. */
    private String m_documentName;

    /** The file suffix of the document. */
    private String m_documentSuffix;

    /** Information used for displaying the format of this document. */
    private String m_format;

    /** Information used for displaying the home division of this document. */
    private String m_homeDivisionOwner;

    /** Information used for displaying the valid home division of this document. */
    private String m_homeDivisionValidIn;

    /** The list of additional language version locales. */
    private Set<Locale> m_languageVersionLocales;

    /** The language version dependencies. */
    private List<CmsDocumentDependency> m_languageVersions;

    /** The locale of this document container. */
    private Locale m_locale;

    /** The main document in case this document is an attachment. */
    private CmsDocumentDependency m_mainDocument;

    /** The VFS resource for which the dependencies are calculated. */
    private CmsPublishedResource m_resource;

    /** The root path of this document. */
    private String m_rootPath;

    /** Information used for displaying the title of this document. */
    private String m_title;

    /** The dependency type for this dependency. */
    private DependencyType m_type;

    /**
     * Creates a new dependency container for the given VFS resource.<p> 
     * 
     * @param res the VFS resource for which the dependencies are calculated
     */
    private CmsDocumentDependency(CmsPublishedResource res) {

        this(res, res.getRootPath());
    }

    /**
     * Creates a new dependency container for the given VFS resource.<p> 
     * 
     * Additional constructor with extra root path, to be used only for test cases.<p>
     * 
     * @param resource the VFS resource for which the dependencies are calculated
     * @param rootPath the root path to use
     */
    private CmsDocumentDependency(CmsPublishedResource resource, String rootPath) {

        m_resource = resource;
        m_rootPath = rootPath;

        m_languageVersionLocales = new HashSet<Locale>();
        m_dependencies = new ArrayList<CmsDocumentDependency>();

        String docName = CmsResource.getName(m_rootPath);

        // check if an attachment number is present
        Matcher matcher = DOC_PATTERN_NUMBER.matcher(docName);
        if (matcher.find()) {
            docName = matcher.group(1);
            Integer partNumber = new Integer(Integer.parseInt(matcher.group(2)));
            setAttachmentNumber(partNumber);
        }

        Locale locale = null;
        matcher = DOC_PATTERN_LOCALE.matcher(docName);
        if (matcher.find()) {
            docName = matcher.group(1);
            String suffix = matcher.group(2);
            String langString = suffix.substring(0, 2);
            locale = suffix.length() == 5 ? new Locale(langString, suffix.substring(3, 5)) : new Locale(langString);
        }
        if ((locale == null) || !OpenCms.getLocaleManager().getDefaultLocales().contains(locale)) {
            // check if a locale information is present
            locale = CmsLocaleManager.getDefaultLocale();
        }
        setLocale(locale);

        // we must remove file suffixes like ".doc", ".pdf" etc. because attachments can have different types
        int index = getRootPath().lastIndexOf('.');
        if (index != -1) {
            // store the suffix for comparison to decide if this is a main document or a variation later
            setDocumentSuffix(getRootPath().substring(index));
        }

        setDocumentName(CmsResource.getFolderPath(m_rootPath) + docName);
    }

    /**
     * Creates a new dependency container for the given VFS resource.<p> 
     * 
     * @param res the VFS resource for which the dependencies are calculated
     */
    private CmsDocumentDependency(CmsResource res) {

        this(new CmsPublishedResource(res, -1, CmsResourceState.STATE_CHANGED));
    }

    /**
     * Returns the locale (language) of the given resource based on the resource root path.<p>
     * 
     * @param rootPath the resource name to check for the locale information
     * 
     * @return the locale of the given resource based on the resource root path
     */
    public static Locale getLocale(String rootPath) {

        return (new CmsDocumentDependency(null, rootPath)).getLocale();
    }

    /**
     * Loads or creates a dependency object for the given parameters.<p>
     * 
     * @param cms the current OpenCms user context
     * @param pubRes the published resource to get the dependency object for
     * 
     * @return a dependency object for the given parameters
     */
    public static CmsDocumentDependency load(CmsObject cms, CmsPublishedResource pubRes) {

        CmsDocumentDependency result = readFromContext(cms, pubRes.getRootPath());
        if (result == null) {
            result = new CmsDocumentDependency(pubRes);
            result.readDependencies(cms);
        }
        return result;
    }

    /**
     * Loads or creates a dependency object for the given parameters.<p>
     * 
     * @param cms the current OpenCms user context
     * @param res the VFS resource to get the dependency object for
     * 
     * @return a dependency object for the given parameters
     */
    public static CmsDocumentDependency load(CmsObject cms, CmsResource res) {

        CmsDocumentDependency result = readFromContext(cms, res.getRootPath());
        if (result == null) {
            result = new CmsDocumentDependency(res);
            result.readDependencies(cms);
        }
        return result;
    }

    /**
     * Loads or creates a dependency object for the given parameters.<p>
     * 
     * @param cms the current OpenCms user context
     * @param res the VFS resource to get the dependency object for
     * @param resources the resource folder data to check for dependencies
     * 
     * @return a dependency object for the given parameters
     */
    public static CmsDocumentDependency load(CmsObject cms, CmsResource res, List<CmsResource> resources) {

        CmsDocumentDependency result = readFromContext(cms, res.getRootPath());
        if (result == null) {
            result = new CmsDocumentDependency(res);
            result.readDependencies(cms, resources);
        }
        return result;
    }

    /**
     * Creates a dependency object for the given root path, to be used only for test cases.<p>
     * 
     * @param rootPath the root path to create the dependency object for
     * 
     * @return a dependency object for the given parameters
     */
    protected static CmsDocumentDependency loadForTest(String rootPath) {

        return new CmsDocumentDependency(null, rootPath);
    }

    /**
     * Removes the dependency object for a published resource from the OpenCms runtime context.<p>
     * 
     * <b>Please note:</b> This must be used with caution since the information may be required to generate 
     * documents for several configured indexes. It must be ensured that this is called only when all indexes 
     * have been updated.<p> 
     *  
     * @param cms the current OpenCms user context
     * @param pubRes the published resource info 
     * 
     * @see #storeInContext(CmsObject)
     */
    protected static void removeFromContext(CmsObject cms, CmsPublishedResource pubRes) {

        cms.getRequestContext().removeAttribute(getAttributeKey(pubRes.getRootPath()));
    }

    /** 
     * Generates a context attribute name for a root path.
     * 
     * @param rootPath the root path
     *  
     * @return the Attr_Doc_Deps + rootPapth
     */
    private static String getAttributeKey(String rootPath) {

        return ATTR_DOC_DEPENDENCY + rootPath;
    }

    /**
     * Reads the dependency object for the given root path in the OpenCms runtime context.<p>  
     * 
     * @param cms the current OpenCms user context
     * @param rootPath the root path to look up the dependency object for
     * 
     * @return the deps 
     */
    private static CmsDocumentDependency readFromContext(CmsObject cms, String rootPath) {

        return (CmsDocumentDependency)cms.getRequestContext().getAttribute(getAttributeKey(rootPath));
    }

    /**
     * Adds another document attachment dependency to this document.<p>
     * 
     * @param dep the document attachment dependency to add
     */
    public void addAttachment(CmsDocumentDependency dep) {

        if (m_attachments == null) {
            m_attachments = new ArrayList<CmsDocumentDependency>(4);
        }
        // don't add attachment if this is a language version of an already existing attachment
        boolean exist = false;
        for (CmsDocumentDependency att : m_attachments) {
            if (att.getAttachmentNumber() == dep.getAttachmentNumber()) {

                if (m_locale.equals(dep.getLocale())) {
                    // if dependency has same locale as main document it is added as attachment 
                    // and gets the old attachment as a language-version with all previous language-versions  
                    for (CmsDocumentDependency langAtt : att.getAllLanguageVersions()) {
                        dep.addLanguageVersion(langAtt);
                    }
                    dep.addLanguageVersion(att);
                    m_attachments.remove(att);
                } else {
                    exist = true;
                    att.addLanguageVersion(dep);
                }
                break;
            }
        }

        if (!exist) {
            dep.setType(DependencyType.attachment);
            m_attachments.add(dep);
        } else {
            dep.setType(DependencyType.variant);
        }
        addDependency(dep);
    }

    /**
     * Adds another document dependency to this document.<p>
     * 
     * @param dep the document dependency to add
     */
    public void addDependency(CmsDocumentDependency dep) {

        m_dependencies.add(dep);
    }

    /**
     * Adds another language version document dependency to this document.<p>
     * 
     * @param dep the language version document dependency to add
     */
    public void addLanguageVersion(CmsDocumentDependency dep) {

        if (m_languageVersions == null) {
            m_languageVersions = new ArrayList<CmsDocumentDependency>(4);
        }

        // check if already exists
        for (CmsDocumentDependency lang : m_languageVersions) {
            if (lang.getLocale().equals(dep.getLocale())) {
                return;
            }
        }
        dep.setType(DependencyType.variant);
        m_languageVersions.add(dep);
        addDependency(dep);
        addLanguageVersionLocale(dep.getLocale());
    }

    /**
     * Adds another dependency language version locale to this document.<p>
     * 
     * @param loc the dependency language version locale to add
     */
    public void addLanguageVersionLocale(Locale loc) {

        if (!m_locale.equals(loc)) {
            m_languageVersionLocales.add(loc);
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsDocumentDependency) {
            CmsDocumentDependency other = (CmsDocumentDependency)obj;
            return m_rootPath.equals(other.m_rootPath) && m_locale.equals(other.m_locale);
        }
        return false;
    }

    /**
     * Returns all language versions, including the language version of this document itself.<p>
     *
     * @return all language versions, including the language version of this document itself
     */
    public List<CmsDocumentDependency> getAllLanguageVersions() {

        List<CmsDocumentDependency> langVersions = getLanguageVersions();
        Set<CmsDocumentDependency> all = langVersions == null
        ? new HashSet<CmsDocumentDependency>()
        : new HashSet<CmsDocumentDependency>(langVersions);
        all.add(this);
        List<CmsDocumentDependency> result = new ArrayList<CmsDocumentDependency>(all);
        Collections.sort(result, COMPARATOR_LOCALE);
        return result;
    }

    /**
     * Returns the attachment number.<p>
     *
     * @return the attachment number
     */
    public int getAttachmentNumber() {

        if (m_attachmentNumber != null) {
            return m_attachmentNumber.intValue();
        }
        return 0;
    }

    /**
     * Returns the attachments.<p>
     *
     * @return the attachments
     */
    public List<CmsDocumentDependency> getAttachments() {

        if (m_attachments != null) {
            Collections.sort(m_attachments, COMPARATOR_TITLE);
        }
        return m_attachments;
    }

    /**
     * Returns the the creation date of this document.<p>
     *
     * @return the the creation date of this document
     */
    public Date getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the the expiration date of this document.<p>
     *
     * @return the the expiration date of this document
     */
    public Date getDateExpired() {

        return m_dateExpired;
    }

    /**
     * Returns the last modification date of this document.<p>
     *
     * @return the last modification date of this document
     */
    public Date getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * Returns the the owner department of this document.<p>
     *
     * @return the the owner department of this document
     */
    public String getDepartment() {

        return m_department;
    }

    /**
     * Returns the list of resources the main resource depends on, including the main resource itself.<p>
     *  
     * @return the list of resources the main resource depends on, including the main resource itself
     */
    public List<CmsDocumentDependency> getDependencies() {

        return m_dependencies;
    }

    /**
     * Returns the document id.<p>
     *
     * @return the document id
     */
    public String getDocumentId() {

        return m_documentId;
    }

    /**
     * Returns the file name of the document without attachment or locale suffixes.<p>
     *
     * @return the file name of the document without attachment or locale suffixes
     */
    public String getDocumentName() {

        return m_documentName;
    }

    /**
     * Returns the suffix of the document.<p>
     * 
     * @return the suffix of the document
     */
    public String getDocumentSuffix() {

        return m_documentSuffix;
    }

    /**
     * Returns the the format of this document.<p>
     *
     * @return the the format of this document
     */
    public String getFormat() {

        return m_format;
    }

    /**
     * Returns the the home division of this document.<p>
     *
     * @return the the home division of this document
     */
    public String getHomeDivisionOwner() {

        return m_homeDivisionOwner;
    }

    /**
     * Returns the the valid home division of this document.<p>
     *
     * @return the the valid home division of this document
     */
    public String getHomeDivisionValidIn() {

        return m_homeDivisionValidIn;
    }

    /**
     * Returns all valid languages for this document.<p>
     * 
     * Valid languages of a document are the language of the document itself,
     * as returned by {@link #getLocale()}, plus all other languages 
     * where this document must serve as default.<p>
     * 
     * @return all valid languages for the given LGT document root path
     */
    @SuppressWarnings("unchecked")
    public List<Locale> getLanguagesValid() {

        // check all additional locales for the resource
        if (CmsLocaleManager.getDefaultLocale().equals(getLocale())
            || !m_languageVersionLocales.contains(CmsLocaleManager.getDefaultLocale())) {
            // this document is the default locale, return all missing locales
            // OR this document does not exist in the default locale at all
            return ListUtils.removeAll(OpenCms.getLocaleManager().getAvailableLocales(), m_languageVersionLocales);
        }
        // else - just return the single locale of the document
        List<Locale> result = new ArrayList<Locale>();
        result.add(getLocale());
        return result;
    }

    /**
     * Returns the other locales this document is available in, does not include the locale of the docuemnt itself.<p>
     *
     * @return the other locales this document is available in
     */
    public List<Locale> getLanguageVersionLocales() {

        return new ArrayList<Locale>(m_languageVersionLocales);
    }

    /**
     * Returns the language versions.<p>
     *
     * @return the language versions
     */
    public List<CmsDocumentDependency> getLanguageVersions() {

        return m_languageVersions;
    }

    /**
     * Returns the locale of this document container.<p>
     *
     * @return the locale of this document container
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the main document in case this document is an attachment.<p>
     *
     * @return the main document in case this document is an attachment
     */
    public CmsDocumentDependency getMainDocument() {

        return m_mainDocument;
    }

    /**
     * Returns the VFS resource for which the dependencies are calculated.<p>
     *
     * @return the VFS resource for which the dependencies are calculated
     */
    public CmsPublishedResource getResource() {

        return m_resource;
    }

    /**
     * Returns the root path of this document.<p>
     *
     * @return the root path of this document
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the structure id.<p>
     * 
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_resource.getStructureId();
    }

    /**
     * Returns the the title of this document.<p>
     *
     * @return the the title of this document
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public DependencyType getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_rootPath.hashCode();
    }

    /**
     * Returns true if this document is an attachment, i.e. an attachment number is provided.<p>
     * 
     * @return true if this document is an attachment, otherwise false
     */
    public boolean isAttachment() {

        return m_attachmentNumber != null;
    }

    /**
     * Reads all dependencies that exist for this main resource in the OpenCms VFS.<p>
     * 
     * To be used when incremental updating an index.<p>
     * 
     * @param cms the current users OpenCms context
     */
    public void readDependencies(CmsObject cms) {

        try {
            // read all resources in the parent folder of the published resource
            List<CmsResource> folderContent = cms.getResourcesInFolder(
                CmsResource.getParentFolder(cms.getRequestContext().removeSiteRoot(getRootPath())),
                CmsResourceFilter.DEFAULT);
            // now calculate the dependencies form the folder content that has been read
            readDependencies(cms, folderContent);
        } catch (CmsException e) {
            LOG.error("Unable to read dependencies for " + getRootPath(), e);
        }
    }

    /**
     * Reads all dependencies that exist for this main resource in provided list of resources.<p>
     * 
     * @param cms the current users OpenCms context
     * @param folderContent the contents of the folder to check the dependencies for
     */
    public void readDependencies(CmsObject cms, List<CmsResource> folderContent) {

        Map<Integer, CmsDocumentDependency> attachments = new HashMap<Integer, CmsDocumentDependency>();
        if (isAttachment()) {
            attachments.put(new Integer(this.getAttachmentNumber()), this);
        }

        // iterate all resources in the folder to check if this is a language version
        Iterator<CmsResource> i = folderContent.iterator();
        while (i.hasNext()) {
            CmsResource r = i.next();
            // only add files and don't add the resource itself again
            if (r.isFile() && !getRootPath().equals(r.getRootPath())) {
                CmsPublishedResource pubRes = new CmsPublishedResource(
                    r,
                    getResource().getPublishTag(),
                    CmsResourceState.STATE_CHANGED);
                CmsDocumentDependency dep = new CmsDocumentDependency(pubRes);
                if (getDocumentName().equals(dep.getDocumentName())) {
                    if (isAttachment()) {
                        // this document is an attachment
                        if (dep.isAttachment()) {
                            if (getAttachmentNumber() == dep.getAttachmentNumber()) {
                                // this must be a language version of this attachment document
                                addLanguageVersion(dep);
                            } else {
                                Integer attNum = new Integer(dep.getAttachmentNumber());
                                CmsDocumentDependency att = attachments.get(attNum);
                                if (att != null) {
                                    att.addLanguageVersion(dep);
                                } else {
                                    attachments.put(attNum, dep);
                                }
                            }
                        } else {
                            // this is the main document of this attachment
                            setMainDocument(dep);
                        }
                    } else {
                        // this document is a main document
                        if (dep.isAttachment()) {
                            // add this dependency as an attachment
                            addAttachment(dep);
                        } else if (CmsStringUtil.isEqual(getDocumentSuffix(), dep.getDocumentSuffix())) {
                            // if this is no attachment, and the file suffix is equal, 
                            // this must be a language version of the main document
                            addLanguageVersion(dep);
                        }
                        // if the file suffix is NOT equal, this is a new main document
                    }
                }
            }
        }

        if (m_mainDocument != null) {
            for (CmsDocumentDependency att : attachments.values()) {
                m_mainDocument.addAttachment(att);
            }
            // add the main document as dependency for this attachment 
            addDependency(m_mainDocument);
        }
    }

    /**
     * Sets the attachment number.<p>
     *
     * @param attachmentNumber the attachment number
     */
    public void setAttachmentNumber(Integer attachmentNumber) {

        m_attachmentNumber = attachmentNumber;
    }

    /**
     * Sets the the creation date of this document.<p>
     *
     * @param dateCreated the the creation date of this document to set
     */
    public void setDateCreated(Date dateCreated) {

        m_dateCreated = dateCreated;
    }

    /**
     * Sets the the expiration date of this document.<p>
     *
     * @param dateExpired the the expiration date of this document to set
     */
    public void setDateExpired(Date dateExpired) {

        m_dateExpired = dateExpired;
    }

    /**
     * Sets the last modification date of this document.<p>
     *
     * @param dateLastModified the last modification date of this document to set
     */
    public void setDateLastModified(Date dateLastModified) {

        m_dateLastModified = dateLastModified;
    }

    /**
     * Sets the the owner department of this document.<p>
     *
     * @param department the the owner department of this document to set
     */
    public void setDepartment(String department) {

        m_department = department;
    }

    /**
     * Sets the document id.<p>
     *
     * @param id the document id
     */
    public void setDocumentId(String id) {

        m_documentId = id;
    }

    /**
     * Sets the file name of the document without attachment or locale suffixes.<p>
     *
     * @param documentName the file name of the document without attachment or locale suffixes
     */
    public void setDocumentName(String documentName) {

        m_documentName = documentName;
    }

    /**
     * Sets the suffix (.pdf, .doc etc.) of the document.<p>
     * 
     * @param documentSuffix the suffix to set
     */
    public void setDocumentSuffix(String documentSuffix) {

        m_documentSuffix = documentSuffix;
    }

    /**
     * Sets the the format of this document.<p>
     *
     * @param format the the format of this document to set
     */
    public void setFormat(String format) {

        m_format = format;
    }

    /**
     * Sets the the home division of this document.<p>
     *
     * @param homeDivisionOwner the the home division of this document to set
     */
    public void setHomeDivisionOwner(String homeDivisionOwner) {

        m_homeDivisionOwner = homeDivisionOwner;
    }

    /**
     * Sets the the valid home division of this document.<p>
     *
     * @param homeDivisionValidIn the the valid home division of this document to set
     */
    public void setHomeDivisionValidIn(String homeDivisionValidIn) {

        m_homeDivisionValidIn = homeDivisionValidIn;
    }

    /**
     * Sets the locale of this document container.<p>
     *
     * @param locale the locale of this document container
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the main document in case this document is an attachment.<p>
     *
     * @param mainDocument the main document to set
     */
    public void setMainDocument(CmsDocumentDependency mainDocument) {

        mainDocument.setType(DependencyType.document);

        if (m_mainDocument == null) {
            // we currently have no main document at all
            m_mainDocument = mainDocument;
        } else {
            // check if we find a better match for the main document locale
            if (mainDocument.getLocale().equals(getLocale())) {
                mainDocument.addLanguageVersion(m_mainDocument);
                // main document locale is the "best" one
                m_mainDocument = mainDocument;
            } else {
                // check if the new document is a "better" one
                List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales();
                int pos1 = locales.indexOf(m_mainDocument.getLocale());
                if (pos1 > 0) {
                    int pos2 = locales.indexOf(mainDocument.getLocale());
                    if (pos2 < pos1) {
                        mainDocument.addLanguageVersion(m_mainDocument);
                        // locale is closer to the default
                        m_mainDocument = mainDocument;
                    }
                } else {
                    m_mainDocument.addLanguageVersion(mainDocument);
                }
            }
        }
    }

    /**
     * Sets the the title of this document.<p>
     *
     * @param title the the title of this document to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the type for this dependency.<p>
     * 
     * @param type the type to set
     */
    public void setType(DependencyType type) {

        m_type = type;
    }

    /**
     * Stores this dependency object for a published resource in the OpenCms runtime context.<p> 
     * 
     * This done to optimize indexing speed. When the index update information is calculated, 
     * all dependencies for a resource must be calculated also. The same information is later needed when 
     * the Lucene document is created, for example in order to store the list of other available languages.<p>  
     * 
     * @param cms the current OpenCms user context
     */
    public void storeInContext(CmsObject cms) {

        cms.getRequestContext().setAttribute(getAttributeKey(getRootPath()), this);
    }

    /**
     * Creates the String representation of this dependency object.<p>
     * 
     * @param cms the current OpenCms user context
     * 
     * @return the String representation of this dependency object
     */
    public String toDependencyString(CmsObject cms) {

        JSONObject jsonDoc = toJSON(cms, true);

        try {
            if ((!isAttachment()) && (m_attachments != null)) {

                // iterate through all attachments
                JSONArray jsonAttachments = new JSONArray();
                for (CmsDocumentDependency att : m_attachments) {
                    JSONObject jsonAttachment = att.toJSON(cms, true);
                    jsonAttachments.put(jsonAttachment);
                }
                jsonDoc.put(JSON_ATTACHMENTS, jsonAttachments);
            } else if (isAttachment()) {

                CmsDocumentDependency main = getMainDocument();
                if (main != null) {
                    JSONObject jsonMain = main.toJSON(cms, true);

                    // iterate through all attachments of the main document
                    List<CmsDocumentDependency> attachments = main.getAttachments();
                    if (attachments != null) {
                        JSONArray jsonAttachments = new JSONArray();
                        for (CmsDocumentDependency att : attachments) {
                            JSONObject jsonAttachment = att.toJSON(cms, true);
                            jsonAttachments.put(jsonAttachment);
                        }
                        jsonMain.put(JSON_ATTACHMENTS, jsonAttachments);
                    }
                    jsonDoc.put(JSON_MAIN, jsonMain);
                }
            }

            //LOG.debug(jsonDoc.toString(2));

            return jsonDoc.toString();
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        return null;
    }

    /**
     * Returns a JSON object describing this dependency document.<p>
     * 
     * @param cms the current cms object
     * @param includeLang flag if language versions should be included
     * 
     * @return a JSON object describing this dependency document
     */
    public JSONObject toJSON(CmsObject cms, boolean includeLang) {

        try {
            JSONObject jsonAttachment = new JSONObject();

            // id and path
            jsonAttachment.put(JSON_UUID, getStructureId());
            jsonAttachment.put(JSON_PATH, getRootPath());

            CmsResource res = cms.readResource(getRootPath(), CmsResourceFilter.IGNORE_EXPIRATION);
            Map<String, String> props = CmsProperty.toMap(cms.readPropertyObjects(res, false));

            // title
            jsonAttachment.put(JSON_TITLE, props.get(CmsPropertyDefinition.PROPERTY_TITLE));

            // date created
            jsonAttachment.put(JSON_DATE_CREATED, res.getDateCreated());

            // date modified
            jsonAttachment.put(JSON_DATE_MODIFIED, res.getDateLastModified());

            if (includeLang) {
                // get all language versions of the document
                List<CmsDocumentDependency> langs = getLanguageVersions();
                if (langs != null) {
                    JSONArray jsonLanguages = new JSONArray();
                    for (CmsDocumentDependency lang : langs) {

                        JSONObject jsonLanguage = lang.toJSON(cms, false);
                        jsonLanguages.put(jsonLanguage);
                    }
                    jsonAttachment.put(JSON_LANGUAGES, jsonLanguages);
                }
            }

            return jsonAttachment;
        } catch (Exception ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_resource.toString();
    }
}
