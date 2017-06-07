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

package org.opencms.search.solr.spellchecking;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsServlet;
import org.opencms.util.CmsStringUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

/**
 * Helping class for manipulating the Solr spellchecker indices.
 */
public final class CmsSpellcheckDictionaryIndexer {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(OpenCmsServlet.class);

    /** The default directory that's holding the dictionary files. */
    public static final String DEFAULT_DICTIONARY_DIRECTORY = "/system/modules/org.opencms.workplace.spellcheck/resources";

    /** A regex pattern that applies to the Solr spellcheck directories.
     * Matching string example: "spellchecker_en" */
    public static final String INDEXES_REGEX = "spellchecker_[a-z]{2}";

    /** A regex pattern that applies to custom dictionaries.
     * Matching string example: "custom_dict_en.txt" */
    public static final String CUSTOM_DICTIONARY = "custom_dict_[a-z]{2}.txt";

    /** A regex pattern that applies to the naming of the dictionary files.
     * Matching string example: "dict_en.txt" */
    public static final String DICTIONARY_NAME_REGEX = "dict_[a-z]{2}.txt";

    /** A regex pattern that applies to the naming of zipped dictionary files.
     * Matching string example: "dict_en.zip" */
    public static final String ZIP_NAME_REGEX = "dict_[a-z]{2}.zip";

    /** Maximum amount of entries while parsing the dictionary. This variable is needed
     * in order to prevent OutOfMemoryExceptions while parsing large dictionaries. If you
     * encounter such exceptions you can adjust its value to a smaller number. */
    private static final int MAX_LIST_SIZE = 100000;

    /**
     * FileFilter implementation that returns only directories whose name matches
     * the spellchecker indices regex.
     */
    private static final FileFilter SPELLCHECKING_DIRECTORY_NAME_FILTER = new FileFilter() {

        public boolean accept(File f) {

            return f.isDirectory() && f.getName().matches(INDEXES_REGEX);
        }
    };

    /**
     * Default constructor is private as each method is static.
     */
    private CmsSpellcheckDictionaryIndexer() {

    }

    /**
     * Adds all dictionaries that are available in the default directory. <p>
     *
     * @param client The SolrClient instance object.
     * @param cms the cms context
     */
    public static void parseAndAddDictionaries(SolrClient client, CmsObject cms) {

        if ((null == client) || (null == cms)) {
            return;
        }

        // Set the correct cms context
        setCmsOfflineProject(cms);

        try {
            // Get all file resources in the default dictionary directory
            final List<CmsResource> resources = cms.getResourcesInFolder(
                DEFAULT_DICTIONARY_DIRECTORY,
                CmsResourceFilter.DEFAULT_FILES);

            for (final CmsResource resource : resources) {
                final String resourceName = resource.getName();
                // Check if the name of the file matches the dictionary naming scheme
                String lang = null;
                if (resourceName.matches(DICTIONARY_NAME_REGEX)) {
                    // Extract the language code that consists of two letters (de, en, es, ...)
                    lang = resourceName.substring(5, 7);
                } else if (resourceName.matches(CUSTOM_DICTIONARY)) {
                    lang = resourceName.substring(12, 14);
                }

                if (null != lang) {
                    // Read the file
                    final CmsFile file = cms.readFile(resource);

                    // Parse file content and add it to the server
                    final List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();

                    readAndAddDocumentsFromStream(
                        client,
                        lang,
                        new ByteArrayInputStream(file.getContents()),
                        documents,
                        true);

                    // Add and commit the remaining documents to the server
                    addDocuments(client, documents, true);
                }
            }

        } catch (CmsException e) {
            LOG.warn("Could not read from resource. ");
        } catch (IOException e) {
            LOG.warn("Could not successfully parse the dictionary. ");
        } catch (SolrServerException e) {
            LOG.warn("Exception while adding documents to Solr server. ");
        }
    }

    /**
     *
     * @param client The SolrClient instance object.
     * @param cms The OpenCms instance object.
     */
    public static void parseAndAddZippedDictionaries(SolrClient client, CmsObject cms) {

        try {
            final List<CmsResource> resources = cms.getResourcesInFolder(
                DEFAULT_DICTIONARY_DIRECTORY,
                CmsResourceFilter.DEFAULT_FILES);

            // List holding all input documents, regardless of language
            final List<SolrInputDocument> documents = new LinkedList<SolrInputDocument>();

            for (CmsResource resource : resources) {
                final String zipFileName = resource.getName();
                if (zipFileName.matches(ZIP_NAME_REGEX)) {
                    final CmsFile cmsFile = cms.readFile(resource);

                    // Read zip file content
                    final ZipInputStream zipStream = new ZipInputStream(
                        new ByteArrayInputStream(cmsFile.getContents()));

                    // Holds several entries (files) of the zipfile
                    ZipEntry entry = zipStream.getNextEntry();

                    // Iterate over each files in the zip file
                    while (null != entry) {
                        // Extract name to check if name matches the regex and to guess the
                        // language from the filename
                        final String name = entry.getName();

                        if (name.matches(DICTIONARY_NAME_REGEX)) {

                            // The (matching) filename reveals the language
                            final String lang = name.substring(5, 7);

                            // Parse and add documents
                            readAndAddDocumentsFromStream(client, lang, zipStream, documents, false);

                            // Get the next file in the zip
                            entry = zipStream.getNextEntry();
                        }

                    }
                }
            }

            // Add all documents
            addDocuments(client, documents, true);
        } catch (IOException e) {
            LOG.warn("Failed while reading from " + DEFAULT_DICTIONARY_DIRECTORY + ". ");
        } catch (CmsException e) {
            LOG.warn("Failed reading resource " + DEFAULT_DICTIONARY_DIRECTORY + ". ");
        } catch (SolrServerException e) {
            LOG.warn("Failed adding documents to Solr server. ");
        }
    }

    /**
     * Checks whether a built of the indices is necessary.
     * @param cms The appropriate CmsObject instance.
     * @return true, if the spellcheck indices have to be rebuilt, otherwise false
     */
    public static boolean updatingIndexNecessesary(CmsObject cms) {

        // Set request to the offline project.
        setCmsOfflineProject(cms);

        // Check whether the spellcheck index directories are empty.
        // If they are, the index has to be built obviously.
        if (isSolrSpellcheckIndexDirectoryEmpty()) {
            return true;
        }

        // Compare the most recent date of a dictionary with the oldest timestamp
        // that determines when an index has been built.
        long dateMostRecentDictionary = getMostRecentDate(cms);
        long dateOldestIndexWrite = getOldestIndexDate(cms);

        return dateMostRecentDictionary > dateOldestIndexWrite;
    }

    /**
     * Add a list of documents to the Solr client.<p>
     *
     * @param client The SolrClient instance object.
     * @param documents The documents that should be added.
     * @param commit boolean flag indicating whether a "commit" call should be made after adding the documents
     *
     * @throws IOException in case something goes wrong
     * @throws SolrServerException in case something goes wrong
     */
    static void addDocuments(SolrClient client, List<SolrInputDocument> documents, boolean commit)
    throws IOException, SolrServerException {

        if ((null == client) || (null == documents)) {
            return;
        }

        if (!documents.isEmpty()) {
            client.add(documents);
        }

        if (commit) {
            client.commit();
        }
    }

    /**
     * Deletes all documents from the Solr client.<p>
     *
     * @param client The SolrClient instance object.
     *
     * @throws IOException in case something goes wrong
     * @throws SolrServerException in case something goes wrong
     */
    static void deleteAllFiles(SolrClient client) throws IOException, SolrServerException {

        if (null == client) {
            return;
        }

        client.deleteByQuery("*:*");
        client.commit();
    }

    /**
     * Deletes a single document from the Solr client.<p>
     *
     * @param client The SolrClient instance object.
     * @param lang The affected language.
     * @param word The word that should be removed.
     *
     * @throws IOException in case something goes wrong
     * @throws SolrServerException in case something goes wrong
     */
    static void deleteDocument(SolrClient client, String lang, String word) throws IOException, SolrServerException {

        if ((null == client)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(lang)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(word)) {
            return;
        }

        // Make sure the parameter holding the word that should be deleted
        // contains just a single word
        if (word.trim().contains(" ")) {
            final String query = String.format("entry_%s:%s", lang, word);
            client.deleteByQuery(query);
        }
    }

    /**
     * Determines and returns the timestamp of the most recently modified spellchecker file.<p>
     *
     * @param cms the OpenCms instance.
     * @return timestamp of type long.
     */
    private static long getMostRecentDate(CmsObject cms) {

        long mostRecentDate = Long.MIN_VALUE;

        try {
            final List<CmsResource> resources = cms.getResourcesInFolder(
                DEFAULT_DICTIONARY_DIRECTORY,
                CmsResourceFilter.DEFAULT_FILES);

            for (final CmsResource resource : resources) {
                final String resourceName = resource.getName();
                // Check whether the resource matches the desired patterns
                if (resourceName.matches(DICTIONARY_NAME_REGEX)
                    || resourceName.matches(ZIP_NAME_REGEX)
                    || resourceName.matches(CUSTOM_DICTIONARY)) {
                    if (resource.getDateLastModified() > mostRecentDate) {
                        mostRecentDate = resource.getDateLastModified();
                    }
                }
            }
        } catch (CmsException e) {
            LOG.error("Could not read spellchecker dictionaries. ");
        }

        return mostRecentDate;
    }

    /**
     * Returns the timestamp of the index whose index-built operation lies the
     * furthest back in the past.<p>
     *
     * @param cms the OpenCms instance.
     * @return timestamp as type long.
     */
    private static long getOldestIndexDate(CmsObject cms) {

        final File path = new File(getSolrSpellcheckRfsPath());
        final File[] directories = path.listFiles(SPELLCHECKING_DIRECTORY_NAME_FILTER);

        // Initialize with the greatest value a long type can hold
        long oldestIndexDate = Long.MAX_VALUE;

        for (final File dir : directories) {
            long date = dir.lastModified();
            if (date < oldestIndexDate) {
                oldestIndexDate = date;
            }
        }

        // If no file(s) have been found oldestIndexDate is still holding
        // Long.MAX_VALUE. In that case return Long.MIN_VALUE to ensure
        // that no indexing operation takes place.
        if (Long.MAX_VALUE == oldestIndexDate) {
            LOG.warn("It appears that no spellcheck indices have been found in " + getSolrSpellcheckRfsPath() + ". ");
            return Long.MIN_VALUE;
        }

        return oldestIndexDate;
    }

    /**
     * Returns the path in the RFS where the Solr spellcheck files reside.
     * @return String representation of Solrs spellcheck RFS path.
     */
    private static String getSolrSpellcheckRfsPath() {

        String sPath = OpenCms.getSystemInfo().getWebInfRfsPath();

        if (!OpenCms.getSystemInfo().getWebInfRfsPath().endsWith(File.separator)) {
            sPath += File.separator;
        }

        return sPath + "solr" + File.separator + "spellcheck" + File.separator + "data";
    }

    /**
     * Returns whether the Solr spellchecking index directories are empty
     * (not initiliazed) or not.
     * @return true, if the directories contain no indexed data, otherwise false.
     */
    private static boolean isSolrSpellcheckIndexDirectoryEmpty() {

        final File path = new File(getSolrSpellcheckRfsPath());
        final File[] directories = path.listFiles(SPELLCHECKING_DIRECTORY_NAME_FILTER);

        // Each directory that has been created by Solr but hasn't been indexed yet
        // contains exactly two files. If there are more files, at least one index has
        // already been built, so return false in that case.
        if (directories != null) {
            for (final File directory : directories) {
                if (directory.list().length > 2) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Parses the dictionary from an InputStream.
     *
     * @param client The SolrClient instance object.
     * @param lang The language of the dictionary.
     * @param is The InputStream object.
     * @param documents List to put the assembled SolrInputObjects into.
     * @param closeStream boolean flag that determines whether to close the inputstream
     * or not.
     */
    private static void readAndAddDocumentsFromStream(
        final SolrClient client,
        final String lang,
        final InputStream is,
        final List<SolrInputDocument> documents,
        final boolean closeStream) {

        final BufferedReader br = new BufferedReader(new InputStreamReader(is));

        try {
            String line = br.readLine();
            while (null != line) {

                final SolrInputDocument document = new SolrInputDocument();
                // Each field is named after the schema "entry_xx" where xx denotes
                // the two digit language code. See the file spellcheck/conf/schema.xml.
                document.addField("entry_" + lang, line);
                documents.add(document);

                // Prevent OutOfMemoryExceptions ...
                if (documents.size() >= MAX_LIST_SIZE) {
                    addDocuments(client, documents, false);
                    documents.clear();
                }

                line = br.readLine();
            }
        } catch (IOException e) {
            LOG.error("Could not read spellcheck dictionary from input stream.");
        } catch (SolrServerException e) {
            LOG.error("Error while adding documents to Solr server. ");
        } finally {
            try {
                if (closeStream) {
                    br.close();
                }
            } catch (Exception e) {
                // Nothing to do here anymore ....
            }
        }
    }

    /**
     * Sets the appropriate OpenCms context.
     * @param cms The OpenCms instance object.
     */
    private static void setCmsOfflineProject(CmsObject cms) {

        if (null == cms) {
            return;
        }

        final CmsRequestContext cmsContext = cms.getRequestContext();
        final CmsProject cmsProject = cmsContext.getCurrentProject();

        if (cmsProject.isOnlineProject()) {
            CmsProject cmsOfflineProject;
            try {
                cmsOfflineProject = cms.readProject("Offline");
                cmsContext.setCurrentProject(cmsOfflineProject);
            } catch (CmsException e) {
                LOG.warn("Could not set the current project to \"Offline\". ");
            }
        }
    }
}
