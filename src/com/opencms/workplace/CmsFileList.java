package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.lang.reflect.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Class for building a file list. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;FILELIST&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.8 $ $Date: 2000/02/08 18:07:29 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsFileList extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants,
                                                           I_CmsConstants{    
   
      /** The head of the file list */
    private final static String C_LIST_HEAD="LIST_HEAD";
 
    /** The entry of the file list */
    private final static String C_LIST_ENTRY="LIST_ENTRY";
    
    /** The title column*/
    private final static String C_COLUMN_TITLE="COLUMN_TITLE";
    
    /** The type column*/
    private final static String C_COLUMN_TYPE="COLUMN_TYPE";
    
    /** The changed column*/
    private final static String C_COLUMN_CHANGED="COLUMN_CHANGED";

    /** The size column*/
    private final static String C_COLUMN_SIZE="COLUMN_SIZE";
    
    /** The state column*/
    private final static String C_COLUMN_STATE="COLUMN_STATE";

    /** The owner column*/
    private final static String C_COLUMN_OWNER="COLUMN_OWNER";
    
    /** The group column*/
    private final static String C_COLUMN_GROUP="COLUMN_GROUP";

    /** The access column*/
    private final static String C_COLUMN_ACCESS="COLUMN_ACCESS";

    /** The locked column*/
    private final static String C_COLUMN_LOCKED="COLUMN_LOCKED";

    /** The stylesheet class to be used for a file or folder entry */
    private final static String C_CLASS_VALUE="OUTPUT_CLASS";
    
    /** The link for a file or folder entry */
    private final static String C_LINK_VALUE="LINK_VALUE";    
    
    /** The suffic for file list values */
    private final static String C_SUFFIX_VALUE="_VALUE";

    /** The lock value column */
    private final static String C_LOCK_VALUE="LOCK_VALUE";
    
    /** The name value column */
    private final static String C_NAME_VALUE="NAME_VALUE";
    
    /** The title value column */
    private final static String C_TITLE_VALUE="TITLE_VALUE";

    /** The icon value column */
    private final static String C_ICON_VALUE="ICON_VALUE";
    
    /** The type value column */
    private final static String C_TYPE_VALUE="TYPE_VALUE";
  
    /** The changed value column */
    private final static String C_CHANGED_VALUE="CHANGED_VALUE";

    /** The size value column */
    private final static String C_SIZE_VALUE="SIZE_VALUE";

    /** The state value column */
    private final static String C_STATE_VALUE="STATE_VALUE";
    
    /** The owner value column */
    private final static String C_OWNER_VALUE="OWNER_VALUE";

    /** The group value column */
    private final static String C_GROUP_VALUE="GROUP_VALUE";

    /** The access value column */
    private final static String C_ACCESS_VALUE="ACCESS_VALUE";

    /** The lockedby value column */
    private final static String C_LOCKED_VALUE="LOCKED_VALUE";

    /** The lockedby value column */
    private final static String C_LOCKED_VALUE_NOLOCK="COLUMN_LOCK_VALUE_NOLOCK";
    
    /** The lockedby value column */
    private final static String C_LOCKED_VALUE_OWN="COLUMN_LOCK_VALUE_OWN";
    
    /** The lockedby value column */
    private final static String C_LOCKED_VALUE_USER="COLUMN_LOCK_VALUE_USER";

    /** The name value column */
    private final static String C_NAME_VALUE_FILE="COLUMN_NAME_VALUE_FILE";

    /** The name value column */
    private final static String C_NAME_VALUE_FOLDER="COLUMN_NAME_VALUE_FOLDER";
    
    /** The lockedby key */  
    private final static String C_LOCKEDBY = "LOCKEDBY";
    
    /** The filefolder key */  
    private final static String C_NAME_FILEFOLDER="NAME_FILEFOLDER";
    
    /** The style for unchanged files or folders */
    private final static String C_STYLE_UNCHANGED="dateingeaendert";

    /** The style for files or folders not in project*/
    private final static String C_STYLE_NOTINPROJECT="dateintprojekt";
    
    /** The style for new files or folders */
    private final static String C_STYLE_NEW="dateineu";
    
    /** The style for deleted files or folders */
    private final static String C_STYLE_DELETED="dateigeloescht";
    
    /** The style for changed files or folders */
    private final static String C_STYLE_CHANGED="dateigeaendert";
    
    /** The prefix for the icon images */
    private final static String C_ICON_PREFIX="ic_file_";                  
    
    /** The extension for the icon images */
    private final static String C_ICON_EXTENSION=".gif";        

    /** The default icon */
    private final static String C_ICON_DEFAULT="ic_file_othertype.gif";

    /** Storage for caching icons */
    private Hashtable m_iconCache=new Hashtable();
    
    /**
     * Handling of the special workplace <CODE>&lt;FILELIST&gt;</CODE> tags.
     * <P>
     * Reads the code of a file list from the file list definition file
     * and returns the processed code with the actual elements.
     * <P>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;FILELIST&gt;</code> tag.
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc,
                                            Object callingObject, Hashtable parameters, 
                                            CmsXmlLanguageFile lang) throws CmsException {
       
        String method= n.getAttribute(C_FILELIST_METHOD);
        String template=n.getAttribute(C_FILELIST_TEMPLATE);
        
        CmsXmlWpTemplateFile filelistTemplate = new CmsXmlWpTemplateFile(cms,template);
        
        CmsXmlWpConfigFile configFile = this.getConfigFile(cms);
        
        Vector filelist=new Vector();
        
        Method groupsMethod=null;
         try {
            groupsMethod = callingObject.getClass().getMethod(method, new Class[] {A_CmsObject.class});
            filelist = ((Vector)groupsMethod.invoke(callingObject, new Object[] {cms}));
        } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find method " + method + " in calling class " + callingObject.getClass().getName() + " for generating select box content.", CmsException.C_NOT_FOUND);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                e.printStackTrace();
                throwException("User method " + method + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("User method " + method + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
       return getFilelist(cms,filelist,filelistTemplate,lang,parameters,callingObject,
                          configFile);
    }          
    
     /**
      * Gets a list of file and folders of a given vector of folders and files.
      * @param cms The CmsObject.
      * @param list Vector of folders and files.
      * @param doc The Template containing the list definitions.
      * @param lang The language defintion template.
      * @param parameters  Hashtable containing all user parameters.
      * @param callingObject The object calling this class.
      * @param config The config file.
      * @return HTML-Code of the file list. 
      */
     private Object getFilelist(A_CmsObject cms, Vector list,
                                A_CmsXmlContent doc, CmsXmlLanguageFile lang,
                                Hashtable parameters,Object callingObject,
                                CmsXmlWpConfigFile config) 
            throws CmsException {
            Hashtable preferences=new Hashtable();
            StringBuffer output=new StringBuffer();  
            String foldername;
            String currentFolder;
            int contextNumber=0;
            
            String servlets=((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
            
            //get the template
            CmsXmlWpTemplateFile template=(CmsXmlWpTemplateFile)doc;
                      
            Enumeration enum;
            
            // file and folder object required to create the file list.
            CmsFile file;
            CmsFolder folder;
            CmsResource res;
            
            HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
            preferences=(Hashtable)session.getValue(C_ADDITIONAL_INFO_PREFERENCES);            
                    
            // hack do this for testing the explorer
            if (preferences == null ) {
                preferences=getDefaultPreferences();
            }
            
            // show the tablehead with all required columns.
            // Check which flags in the user preferences are NOT set and delete those columns in 
            // the table generating the file list.           
            int filelist=((Integer)preferences.get(C_USERPREF_FILELIST)).intValue();
            
            template=checkDisplayedColumns(filelist,template,"");
            
            // add the list header to the output.
            output.append(template.getProcessedXmlDataValue(C_LIST_HEAD,callingObject));
            
            // go through all folders and files
            enum=list.elements();
            while (enum.hasMoreElements()) {
                
                res=(CmsResource)enum.nextElement();
                if (res.isFolder()) {
                    folder=(CmsFolder)res; 
                    // Set output style class according to the project and state of the file.
                    template.setXmlData(C_CLASS_VALUE,getStyle(cms,folder));   
                     // set the icon
                    template.setXmlData("CONTEXT_MENU","plainlock");
                    template.setXmlData("CONTEXT_NUMBER",new Integer(contextNumber++).toString());
                    
                    A_CmsResourceType type=cms.getResourceType(folder.getType());
                    String icon=icon=getIcon(cms,type,config);
                    template.setXmlData(C_ICON_VALUE,config.getPictureUrl()+icon);
                    // set the link
                    template.setXmlData(C_LINK_VALUE,folder.getAbsolutePath());                      
                    // set the lock icon if nescessary
                    template.setXmlData(C_LOCK_VALUE,template.getProcessedXmlDataValue(getLock(cms,folder,template,lang),this));  
                    // set the folder name
                    template.setXmlData(C_NAME_VALUE,folder.getName());
                    template.setXmlData(C_NAME_FILEFOLDER,template.getProcessedXmlDataValue(getName(cms,folder),this));     
                    // set the folder title
                    String title=cms.readMetainformation(folder.getAbsolutePath(),C_METAINFO_TITLE);
                    if (title==null) {
                        title="";
                    }
                    template.setXmlData(C_TITLE_VALUE,title);   
                    template.setXmlData(C_TYPE_VALUE,type.getResourceName());   
                    // get the folder date
                    long time=folder.getDateLastModified();
                    template.setXmlData(C_CHANGED_VALUE,getNiceDate(time));   
                    // get the folder size
                    template.setXmlData(C_SIZE_VALUE,"");   
                    // get the folder state
                    template.setXmlData(C_STATE_VALUE,getState(cms,folder,lang));  
                    // get the owner of the folder
                    A_CmsUser owner = cms.readOwner(folder);
                    template.setXmlData(C_OWNER_VALUE,owner.getName());
                    // get the group of the folder
                    A_CmsGroup group = cms.readGroup(folder);
                    template.setXmlData(C_GROUP_VALUE,group.getName());
                    // get the access flags
                    int access=folder.getAccessFlags();
                    template.setXmlData(C_ACCESS_VALUE,getAccessFlags(access));
                    // get the locked by
                    int lockedby = folder.isLockedBy();
                    if (lockedby == C_UNKNOWN_ID) {
                        template.setXmlData(C_LOCKED_VALUE,"");
                    } else {
                        template.setXmlData(C_LOCKED_VALUE,cms.lockedBy(folder.getAbsolutePath()).getName());
                    }
                    // as a last step, check which colums must be displayed and add the file
                    // to the output.
                    template=checkDisplayedColumns(filelist,template,C_SUFFIX_VALUE);
                    output.append(template.getProcessedXmlDataValue(C_LIST_ENTRY,callingObject));                
                } else {        
                    file=(CmsFile)res; 
                    // Set output style class according to the project and state of the file.
                    template.setXmlData(C_CLASS_VALUE,getStyle(cms,file));                   
                    // set the icon
                    template.setXmlData("CONTEXT_MENU","plainlock");
                    template.setXmlData("CONTEXT_NUMBER",new Integer(contextNumber++).toString());
                    
                    A_CmsResourceType type=cms.getResourceType(file.getType());
                    String icon=getIcon(cms,type,config);
                    template.setXmlData(C_ICON_VALUE,config.getPictureUrl()+icon);
                    // set the link         
                    template.setXmlData(C_LINK_VALUE,servlets+file.getAbsolutePath());  
                    // set the lock icon if nescessary
                    template.setXmlData(C_LOCK_VALUE,template.getProcessedXmlDataValue(getLock(cms,file,template,lang),this));                      
                    // set the filename
                    //template.setXmlData(C_NAME_VALUE,template.getProcessedXmlDataValue("COLUMN_NAME_VALUE_FILE",this));   
                    template.setXmlData(C_NAME_VALUE,file.getName());     
                    template.setXmlData(C_NAME_FILEFOLDER,template.getProcessedXmlDataValue(getName(cms,file),this));     
                   
                    // set the file title
                    String title=cms.readMetainformation(file.getAbsolutePath(),C_METAINFO_TITLE);
                    if (title==null) {
                        title="";
                    }
                    template.setXmlData(C_TITLE_VALUE,title);   
                    // set the file type 
                    type=cms.getResourceType(file.getType());
                    template.setXmlData(C_TYPE_VALUE,type.getResourceName());   
                    // get the file date
                    long time=file.getDateLastModified();
                    template.setXmlData(C_CHANGED_VALUE,getNiceDate(time));   
                    // get the file size
                    template.setXmlData(C_SIZE_VALUE,new Integer(file.getLength()).toString());   
                    // get the file state
                    template.setXmlData(C_STATE_VALUE,getState(cms,file,lang));  
                    // get the owner of the file
                    A_CmsUser owner = cms.readOwner(file);
                    template.setXmlData(C_OWNER_VALUE,owner.getName());
                    // get the group of the file
                    A_CmsGroup group = cms.readGroup(file);
                    template.setXmlData(C_GROUP_VALUE,group.getName());
                    // get the access flags
                    int access=file.getAccessFlags();
                    template.setXmlData(C_ACCESS_VALUE,getAccessFlags(access));
                    // get the locked by
                    int lockedby = file.isLockedBy();
                    if (lockedby == C_UNKNOWN_ID) {
                        template.setXmlData(C_LOCKED_VALUE,"");
                    } else {
                        template.setXmlData(C_LOCKED_VALUE,cms.lockedBy(file.getAbsolutePath()).getName());
                    }
                    
                    // as a last step, check which colums must be displayed and add the file
                    // to the output.
                    template=checkDisplayedColumns(filelist,template,C_SUFFIX_VALUE);
                    output.append(template.getProcessedXmlDataValue(C_LIST_ENTRY,callingObject));                
                }
            }
         return output.toString();
     }            
    
     /**
      * Checks which columns in the file list must be displayed.
      * Tests which flags in the user preferences are NOT set and delete those columns in 
      * the table generating the file list.           
      * @param filelist The filelist flags of the user.
      * @param template The file list template
      * @return Updated file list template.
      */
     private CmsXmlWpTemplateFile checkDisplayedColumns(int filelist, 
                                                        CmsXmlWpTemplateFile template,
                                                        String suffix) {
            if ((filelist & C_FILELIST_TITLE) == 0) {
                template.setXmlData(C_COLUMN_TITLE+suffix,"");
            }
            if ((filelist & C_FILELIST_TYPE) == 0) {
                template.setXmlData(C_COLUMN_TYPE+suffix,"");
            }
            if ((filelist & C_FILELIST_STATE) == 0) {
                template.setXmlData(C_COLUMN_STATE+suffix,"");
            }
            if ((filelist & C_FILELIST_CHANGED) == 0) {
                template.setXmlData(C_COLUMN_CHANGED+suffix,"");
            }
            if ((filelist & C_FILELIST_SIZE) == 0) {
                template.setXmlData(C_COLUMN_SIZE+suffix,"");
            }
            if ((filelist & C_FILELIST_OWNER) == 0) {
                template.setXmlData(C_COLUMN_OWNER+suffix,"");
            }
            if ((filelist & C_FILELIST_GROUP) == 0) {
                template.setXmlData(C_COLUMN_GROUP+suffix,"");
            }                                                 
            if ((filelist & C_FILELIST_ACCESS) == 0) {
                template.setXmlData(C_COLUMN_ACCESS+suffix,"");
            }   
            if ((filelist & C_FILELIST_LOCKED) == 0) {
                template.setXmlData(C_COLUMN_LOCKED+suffix,"");
            }  
            return template;
     }

     /**
      * Gets a formated time string form a long time value.
      * @param time The time value as a long.
      * @return Formated time string.
      */
     private String getNiceDate(long time) {
         StringBuffer niceTime=new StringBuffer();
         
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(new Date(time));
         String day="0"+new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();        
         String month="0"+new Integer(cal.get(Calendar.MONTH)+1).intValue(); 
         String year=new Integer(cal.get(Calendar.YEAR)).toString();
         String hour="0"+new Integer(cal.get(Calendar.HOUR)+12*cal.get(Calendar.AM_PM)).intValue();   
         String minute="0"+new Integer(cal.get(Calendar.MINUTE));   
         if (day.length()==3) {
             day=day.substring(1,3);
         }
         if (month.length()==3) {
             month=month.substring(1,3);
         }
         if (hour.length()==3) {
             hour=hour.substring(1,3);
         }
         if (minute.length()==3) {
             minute=minute.substring(1,3);
         }
         niceTime.append(day+".");
         niceTime.append(month+".");  
         niceTime.append(year+" ");
         niceTime.append(hour+":");
         niceTime.append(minute);
         return niceTime.toString();
     }
     
      /**
      * Gets a formated access right string form a int access value.
      * @param time The access value as an int.
      * @return Formated access right string.
      */
     private String getAccessFlags(int access) {
         StringBuffer accessFlags=new StringBuffer();
         if ((access & C_ACCESS_OWNER_READ) > 0){
             accessFlags.append("r");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_OWNER_WRITE) > 0){
             accessFlags.append("w");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_OWNER_VISIBLE) > 0){
             accessFlags.append("v");
         } else {
             accessFlags.append("-");
         }
          if ((access & C_ACCESS_GROUP_READ) > 0){
             accessFlags.append("r");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_GROUP_WRITE) > 0){
             accessFlags.append("w");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_GROUP_VISIBLE) > 0){
             accessFlags.append("v");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_PUBLIC_READ) > 0){
             accessFlags.append("r");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_PUBLIC_WRITE) > 0){
             accessFlags.append("w");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_PUBLIC_VISIBLE) > 0){
             accessFlags.append("v");
         } else {
             accessFlags.append("-");
         }
         if ((access & C_ACCESS_INTERNAL_READ) > 0){
             accessFlags.append("v");
         } else {
             accessFlags.append("-");
         }
         return accessFlags.toString();
     }

     /**
     * Gets a formated file state string for a entry in the file list.
     * @param cms The CmsObject.
     * @param file The CmsResource displayed in the the file list.
     * @param lang The content definition language file.
     * @return Formated state string.
     */
     private String getState(A_CmsObject cms, CmsResource file,CmsXmlLanguageFile lang)
         throws CmsException {
         StringBuffer output=new StringBuffer();
         
         if (file.inProject(cms.getRequestContext().currentProject())) {
            int state=file.getState();
            output.append(lang.getLanguageValue("explorer.state"+state));
         } else {
            output.append(lang.getLanguageValue("explorer.statenip"));
         }
         return output.toString();
     }
     
     
     /**
     * Select which lock icon (if nescessary) is selected for a entry in the file list.
     * @param cms The CmsObject.
     * @param file The CmsResource displayed in the the file list.
     * @param template The file list template
     * @param lang The content definition language file.
     * @return HTML code for selecting a lock icon.
     */
     private String getLock(A_CmsObject cms, CmsResource file,CmsXmlWpTemplateFile template,
                                              CmsXmlLanguageFile lang)
         throws CmsException {
         StringBuffer output = new StringBuffer();
         // the file is locked
         if (file.isLocked()) {
           int locked=file.isLockedBy();
           // it is locked by the actuel user
             if (cms.getRequestContext().currentUser().getId()==locked) {
                template.setXmlData(C_LOCKEDBY,lang.getLanguageValue("explorer.lockedby")+cms.getRequestContext().currentUser().getName());
                output.append(C_LOCKED_VALUE_OWN);
            } else {
                template.setXmlData(C_LOCKEDBY,lang.getLanguageValue("explorer.lockedby")+cms.lockedBy(file.getAbsolutePath()).getName());
                output.append(C_LOCKED_VALUE_USER);
            }
         } else {
            output.append(C_LOCKED_VALUE_NOLOCK);
         }
         return output.toString();
     }
     
     /**
     * Gets the name (including link) for a entry in the file list.
     * @param cms The CmsObject.
     * @param file The CmsResource displayed in the the file list.
     * @return The name used for the actual entry.
     */
     private String getName(A_CmsObject cms, CmsResource file) {
        StringBuffer output = new StringBuffer();  
        if (file.isFile()) {
            output.append(C_NAME_VALUE_FILE);    
        } else {
            output.append(C_NAME_VALUE_FOLDER);     
        }
        return output.toString();
        
     }
     
     /**
     * Gets the style for a entry in the file list.
     * @param cms The CmsObject.
     * @param file The CmsResource displayed in the the file list.
     * @return The style used for the actual entry.
     */
     private String getStyle(A_CmsObject cms, CmsResource file) {
         StringBuffer output=new StringBuffer();
         // check if the resource is in the actual project
         if (!file.inProject(cms.getRequestContext().currentProject())) {
             output.append(C_STYLE_NOTINPROJECT); 
         }else {         
            int style=file.getState();
            switch (style) {
            case 0: output.append(C_STYLE_UNCHANGED);
                     break;
            case 1: output.append(C_STYLE_CHANGED);
                     break; 
            case 2: output.append(C_STYLE_NEW);
                     break;
            case 3: output.append(C_STYLE_DELETED);
                     break;
            default: output.append(C_STYLE_UNCHANGED);
            }
         }
         return output.toString();
     }     
     
     /**
      * Selects the icon that is displayed in the file list.<br>
      * This method includes cache to prevent to look up in the filesystem for each
      * icon to be displayed
      * @param cms The CmsObject.
      * @param type The resource type of the file entry.
      * @param config The configuration file.
      * @return String containing the complete name of the iconfile.
      * @exception Throws CmsException if something goes wrong.
      */
     private String getIcon(A_CmsObject cms,A_CmsResourceType type, CmsXmlWpConfigFile config)
     throws CmsException {
        String icon=null;
        String filename=config.getPicturePath()+C_ICON_PREFIX+type.getResourceName().toLowerCase()+C_ICON_EXTENSION;
        A_CmsResource iconFile;
        
        // check if this icon is in the cache already
        icon=(String)m_iconCache.get(type.getResourceName());
        // no icon was found, so check if there is a icon file in the filesystem
        if (icon==null) {
            try {
                 // read the icon file
                 iconFile=cms.readFileHeader(filename);
                 // add the icon to the cache
                 icon=C_ICON_PREFIX+type.getResourceName().toLowerCase()+C_ICON_EXTENSION;
                 m_iconCache.put(type.getResourceName(),icon);
            } catch (CmsException e) {                
              // no icon was found, so use the default 
              icon=C_ICON_DEFAULT;
              m_iconCache.put(type.getResourceName(),icon);
            }            
        }             
        return icon;
     }
     
     
     
     /**
     * Sets the default preferences for the current user if those values are not available.
     * THIS METHOD IS ONLY ADDED FOR TESTING PURPOSES!
     * @return Hashtable with default preferences.
     */
    private Hashtable getDefaultPreferences() {
        Hashtable pref=new Hashtable();
        
        // set the default columns in the filelist
        int filelist=C_FILELIST_TITLE+C_FILELIST_TYPE+C_FILELIST_CHANGED;
        filelist=4095;
        pref.put(C_USERPREF_FILELIST,new Integer(filelist));
        return pref;
    }
}
