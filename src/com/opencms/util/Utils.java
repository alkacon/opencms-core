/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Utils.java,v $
* Date   : $Date: 2003/07/02 11:03:13 $
* Version: $Revision: 1.43 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.util;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.OpenCms;
import com.opencms.defaults.I_CmsLifeCycle;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * This is a general helper class.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas <alexander.lucas@framfab.de>
 */
public class Utils {


    /** Constant for sorting files upward by name */
    public static final int C_SORT_NAME_UP = 1;


    /** Constant for sorting files downward by name */
    public static final int C_SORT_NAME_DOWN = 2;


    /** Constant for sorting files upward by lastmodified date */
    public static final int C_SORT_LASTMODIFIED_UP = 3;


    /** Constant for sorting files downward by lastmodified date */
    public static final int C_SORT_LASTMODIFIED_DOWN = 4;

    /**
     * This method makes the sorting desicion for the creation of index and archive pages,
     * depending on the sorting method to be used.<p>
     * 
     * @param cms Cms Object for accessign files.
     * @param sorting The sorting method to be used.
     * @param fileA One of the two CmsFile objects to be compared.
     * @param fileB The second of the two CmsFile objects to be compared.
     * @return <code>true</code> or <code>false</code>, depending if the two file objects have to be sorted.
     * @throws CmsException Is thrown when file access failed.
     *
     */
    private static boolean compare(int sorting, CmsFile fileA, CmsFile fileB)
            throws CmsException {
        boolean cmp = false;
        String titleA = fileA.getName();
        String titleB = fileB.getName();
        long lastModifiedA = fileA.getDateLastModified();
        long lastModifiedB = fileB.getDateLastModified();
        switch(sorting) {
        case C_SORT_NAME_UP:
            cmp = (titleA.compareTo(titleB) > 0);
            break;

        case C_SORT_NAME_DOWN:
            cmp = (titleB.compareTo(titleA) > 0);
            break;

        case C_SORT_LASTMODIFIED_UP:
            cmp = (lastModifiedA > lastModifiedB);
            break;

        case C_SORT_LASTMODIFIED_DOWN:
            cmp = (lastModifiedA < lastModifiedB);
            break;
            
        default:
            cmp = false;
        }
        return cmp;
    }

    /**
     * Returns the AbsolutePath of an resource based on the base and the relative
     * Path to the resource. There are three cases for the relative path:
     *   case 1:     a / at the beginning of the relPath -> return the relPath
     *   case 2:     one or more ../ at the beginning! of the relPath -> return the absolute path to the resource
     *   case 3:     ./ or something else at the beginning -> return base + relpath (without the ./ of course)
     *
     * @param basePath The folder where the relativePath starts (or the absolute path of
     *          a file (no folder) in this path)
     * @param relativePath The relative path to a reaource.
     */
    public static String mergeAbsolutePath(String basePath, String relativePath){

        if(relativePath == null || "".equals(relativePath)){
            return basePath;
        }
        if(relativePath.startsWith("/") || basePath == null){
            // case 1:     a / at the beginning of the relPath -> return the relPath
            return relativePath;
        }
        basePath = basePath.substring(0,basePath.lastIndexOf('/')+1);
        String result = null;
        if(relativePath.startsWith("./")){
            // case 3 a:     ./ at the beginning -> return base + relpath (without the ./ of course)
            relativePath = relativePath.substring(2);
        }
        if(relativePath.startsWith("../")){
            // case 2:     one or more ../ at the beginning! of the relPath -> return the absolute path to the resource
			int lastIndexOfSlash = relativePath.lastIndexOf("../");
			int count = (lastIndexOfSlash / 3)+1;
			int baseCount =basePath.lastIndexOf('/') -1;
			for(int i=0; i<count; i++){
				baseCount = basePath.lastIndexOf('/',baseCount) -1;
			}
			result = basePath.substring(0,baseCount+2) + relativePath.substring(lastIndexOfSlash+3);

        }else{
            // case 3b: something else at the beginning -> return base + relpath (without the ./ of course)
            result = basePath + relativePath;
        }
        return result;
    }

    /**
     * Returns a string representation of the full name of a user.
     * @param user The user to get the full name from
     * @return a string representation of the user fullname.
     */
    public static String getFullName(CmsUser user) {
        String retValue = "";
        if(user != null) {
            retValue += user.getFirstname() + " ";
            retValue += user.getLastname() + " (";
            retValue += user.getName() + ")";
        }
        return retValue;
    }

    /**
     * Gets a formated time string form a long time value.
     * @param time The time value as a long.
     * @return Formated time string.
     */

    public static String getNiceDate(long time) {
        StringBuffer niceTime = new StringBuffer();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(time));
        String day = "0" + new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();
        String month = "0" + new Integer(cal.get(Calendar.MONTH) + 1).intValue();
        String year = new Integer(cal.get(Calendar.YEAR)).toString();
        String hour = "0" + new Integer(cal.get(Calendar.HOUR) + 12
                * cal.get(Calendar.AM_PM)).intValue();
        String minute = "0" + new Integer(cal.get(Calendar.MINUTE));
        if(day.length() == 3) {
            day = day.substring(1, 3);
        }
        if(month.length() == 3) {
            month = month.substring(1, 3);
        }
        if(hour.length() == 3) {
            hour = hour.substring(1, 3);
        }
        if(minute.length() == 3) {
            minute = minute.substring(1, 3);
        }
        niceTime.append(day + ".");
        niceTime.append(month + ".");
        niceTime.append(year + " ");
        niceTime.append(hour + ":");
        niceTime.append(minute);
        return niceTime.toString();
    }

    /**
     * Gets a formated time string form a long time value.
     * @param time The time value as a long.
     * @return Formated time string.
     */

    public static String getNiceShortDate(long time) {
        StringBuffer niceTime = new StringBuffer();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(time));
        String day = "0" + new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();
        String month = "0" + new Integer(cal.get(Calendar.MONTH) + 1).intValue();
        String year = new Integer(cal.get(Calendar.YEAR)).toString();
        if(day.length() == 3) {
            day = day.substring(1, 3);
        }
        if(month.length() == 3) {
            month = month.substring(1, 3);
        }
        niceTime.append(day + ".");
        niceTime.append(month + ".");
        niceTime.append(year);
        return niceTime.toString();
    }

    /**
     *
     * @param cms. The CmsObject.
     * @param changedLinks A vector of STrings with the links that have changed
     *       during the publishing.
     */
    public static void getModulPublishMethods(CmsObject cms, Vector changedLinks) throws CmsException{
        // now publish the module masters
        Vector publishModules = new Vector();
        cms.getRegistry().getModulePublishables(publishModules, CmsObject.C_PUBLISH_METHOD_LINK);

        for(int i = 0; i < publishModules.size(); i++){
            // call the publishProject method of the class with parameters:
            // cms, changedLinks
            try{
                Class.forName((String)publishModules.elementAt(i)).getMethod("publishLinks",
                                        new Class[] {CmsObject.class, Vector.class}).invoke(
                                        null, new Object[] {cms, changedLinks});
            } catch(Exception ex){
            ex.printStackTrace();
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "Error when publish data of module "+(String)publishModules.elementAt(i)+"!: "+ex.getMessage());
                }
            }
        }
    }

    /**
     * Calls the startup methode on all module classes that are registerd in the registry.
     *
     * @param cms. The CmsObject.
     */
    public static void getModulStartUpMethods(CmsObject cms) throws CmsException{

        Vector startUpModules = new Vector();
        cms.getRegistry().getModuleLifeCycle(startUpModules);
        for(int i = 0; i < startUpModules.size(); i++){
            try{
                I_CmsLifeCycle lifeClass = (I_CmsLifeCycle)Class.forName((String)startUpModules.elementAt(i)).getConstructor(new Class[] {}).newInstance(new Class[] {});
                lifeClass.startUp(cms);
            } catch(Exception ex){
            }
        }
    }

    /**
     * Calls the startup methode on all module classes that are registerd in the registry.
     *
     * @param cms. The CmsObject.
     */
    public static void getModulShutdownMethods(I_CmsRegistry reg) throws CmsException{

        Vector startUpModules = new Vector();
        reg.getModuleLifeCycle(startUpModules);
        for(int i = 0; i < startUpModules.size(); i++){
            try{
                I_CmsLifeCycle lifeClass = (I_CmsLifeCycle)Class.forName((String)startUpModules.elementAt(i)).getConstructor(new Class[] {}).newInstance(new Class[] {});
                lifeClass.shutDown();
            } catch(Exception ex){
            }
        }
    }

    /**
     * Gets the stack-trace of a exception, and returns it as a string.
     * @param e The exception to get the stackTrace from.
     * @return the stackTrace of the exception.
     */
    public static String getStackTrace(Throwable e) {

        // print the stack-trace into a writer, to get its content
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        e.printStackTrace(writer);
        if(e instanceof CmsException) {
            CmsException cmsException = (CmsException)e;
            if(cmsException.getException() != null) {
                cmsException.getException().printStackTrace(writer);
            }
        }
        try {
            writer.close();
            stringWriter.close();
        }
        catch(Exception err) {


        // ignore
        }
        return stringWriter.toString();
    }

    /**
     * Replaces all line breaks in a given string object by
     * white spaces. All lines will be <code>trim</code>ed to
     * delete all unnecessary white spaces.
     * @param s Input string
     * @return Output String
     * @throws CmsException
     */

    public static String removeLineBreaks(String s) throws CmsException {
        StringBuffer result = new StringBuffer();
        BufferedReader br = new BufferedReader(new StringReader(s));
        String lineStr = null;
        try {
            while((lineStr = br.readLine()) != null) {
                result.append(lineStr.trim());
                result.append(" ");
            }
        }
        catch(IOException e) {
            throw new CmsException("Error while reading input stream in com.opencms.util.Utils.removeLineBreaks: " + e);
        }
        return result.toString();
    }

    /**
     * Checks if a resource needs the https scheme. Thats the case if the resource
     * itself or a parent folder has the property 'export' set to 'https'.
     *
     * @param cms The cms Object, used for reading the parent folder and the properties.
     * @param res The resource to be checked.
     * @throws CmsException.
     */
    public static boolean isHttpsResource(CmsObject cms, CmsResource res) throws CmsException{
        while(!cms.readAbsolutePath(res).equals(I_CmsConstants.C_ROOT)){
            // check for the property export
            String prop = cms.readProperty(cms.readAbsolutePath(res), I_CmsConstants.C_PROPERTY_EXPORT);
            if((prop != null) && "https".equalsIgnoreCase(prop)){
                // found one
                return true;
            }
            res = cms.readFileHeader(res.getParent());
        }
        return false;
    }

    /**
     * Sorts a Vector of CmsFile objects according to an included sorting method.<p>
     * 
     * @param unsortedFiles Vector containing a list of unsorted files
     * @param sorting The sorting method to be used.
     * @return Vector of sorted CmsFile objects
     * @deprecated this method is deprecated and will be removed in a later OpenCms release
     */
    public static Vector sort(Vector unsortedFiles, int sorting) {
        Vector v = new Vector();
        Enumeration enu = unsortedFiles.elements();
        CmsFile[] field = new CmsFile[unsortedFiles.size()];
        CmsFile file;
        int max = 0;
        try {

            // create an array with all unsorted files in it. This arre is later sorted in with
            // the sorting algorithem.
            while(enu.hasMoreElements()) {
                file = (CmsFile)enu.nextElement();
                field[max] = file;
                max++;
            }

            // Sorting algorithm
            // This method uses an insertion sort algorithem
            int in, out;
            int nElem = max;
            for(out = 1;out < nElem;out++) {
                CmsFile temp = field[out];
                in = out;
                while(in > 0 && compare(sorting, field[in - 1], temp)) {
                    field[in] = field[in - 1];
                    --in;
                }
                field[in] = temp;
            }

            // take sorted array and create a new vector of files out of it
            for(int i = 0;i < max;i++) {
                v.addElement(field[i]);
            }
        }
        catch(Exception e) {
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[Utils] :" + e.toString());
            }
        }
        return v;
    }

    /**
     * This method splits a overgiven string into substrings.
     *
     * @param toSplit the String to split.
     * @param at the delimeter.
     *
     * @return an Array of Strings.
     */
    public static final String[] split(String toSplit, String at) {
        Vector parts = new Vector();
        int index = 0;
        int nextIndex = toSplit.indexOf(at);
        while(nextIndex != -1) {
            parts.addElement((Object)toSplit.substring(index, nextIndex));
            index = nextIndex + at.length();
            nextIndex = toSplit.indexOf(at, index);
        }
        parts.addElement((Object)toSplit.substring(index));
        String partsArray[] = new String[parts.size()];
        parts.copyInto((Object[])partsArray);
        return (partsArray);
    }

    /**
     * This method replaces all occurences of the replaceKey in the toReplace string with the replaceWith String.
     *
     * @param toReplace the String to replace something in.
     * @param replaceKey the String that will be replaced.
     * @param replaceWith The string that is inserted in the place marked with the replaceKey.
     *
     * @return String.
     */
    public static final String replace(String toReplace, String replaceKey, String replaceWith) {
        if(toReplace == null){
            return null;
        }
        StringBuffer retValue = new StringBuffer();

        int index = 0;
        int nextIndex = toReplace.indexOf(replaceKey);
        while(nextIndex != -1) {
            retValue.append(toReplace.substring(index, nextIndex))
                    .append(replaceWith );
            index = nextIndex + replaceKey.length();
            nextIndex = toReplace.indexOf(replaceKey, index);
        }
        retValue.append(toReplace.substring(index));
        return retValue.toString();
    }

    /**
     * Converts date string to a long value.
     * @param dateString The date as a string.
     * @return long value of date.
     */

    public static long splitDate(String dateString) {
        long result = 0;
        if(dateString != null && !"".equals(dateString)) {
            String splittetDate[] = Utils.split(dateString, ".");
            GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
                    Integer.parseInt(splittetDate[1]) - 1, Integer.parseInt(splittetDate[0]), 0, 0, 0);
            result = cal.getTime().getTime();
        }
        return result;
    }

    /**
     * Sorts two vectors using bubblesort. This is a quick hack to display templates sorted by title instead of
     * by name in the template dropdown, because it is the title that is shown in the dropdown.
     * Creation date: (10/24/00 13:55:12)
     * @param names The vector to sort
     * @param data Vector with data that accompanies names.
     */

    public static void bubblesort(Vector names, Vector data) {
        for(int i = 0;i < names.size() - 1;i++) {
            int len = names.size() - i - 1;
            for(int j = 0;j < len;j++) {
                String a = (String)names.elementAt(j);
                String b = (String)names.elementAt(j + 1);
                if(a.toLowerCase().compareTo(b.toLowerCase()) > 0) {
                    names.setElementAt(a, j + 1);
                    names.setElementAt(b, j);
                    a = (String)data.elementAt(j);
                    data.setElementAt(data.elementAt(j + 1), j);
                    data.setElementAt(a, j + 1);
                }
            }
        }
    }

    /**
     * This method checks if a new password sticks to the rules for
     * new passwords (i.e. a new password must have at least 4 characters).
     * For this purpose a class defined in the opencms.properties is called.
     * If this class throws no exception the password is ok. The default class
     * only checks for the min 4 characters rule.
     *
     * @param cms The CmsObject.
     * @param password The new password that has to be checked.
     * @param oldPassword The old password or null if not needed.
     *
     * @throws CmsException is thrown if the password is not valid.
     */
    public static void validateNewPassword(CmsObject cms, String password, String oldPassword)throws CmsException{

        // first get the class from the properties
        String className = OpenCms.getPasswordValidatingClass();
        try{
            I_PasswordValidation pwClass = (I_PasswordValidation)Class.forName(className).getConstructor(new Class[] {}).newInstance(new Class[] {});
            pwClass.checkNewPassword(cms, password, oldPassword);
        }catch(Exception e){
            if(e instanceof CmsException){
                throw (CmsException)e;
            }else{
                throw new CmsException("could not validate password with class:"+className,
                            CmsException.C_UNKNOWN_EXCEPTION, e);
            }
        }
    }
}
