package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the file list tree of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/02/01 08:19:58 $
 */
public class CmsFileList extends CmsWorkplaceDefault implements I_CmsWpConstants,
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

    /** Thestylesheet class to be used for a file or folder entry */
    private final static String C_CLASS_VALUE="OUTPUT_CLASS";
    
    /** The suffic for file list values */
    private final static String C_SUFFIX_VALUE="_VALUE";

    /** The name value column */
    private final static String C_NAME_VALUE="NAME_VALUE";
    
    /** The title value column */
    private final static String C_TITLE_VALUE="TITLE_VALUE";
  
    /** The changed value column */
    private final static String C_CHANGED_VALUE="CHANGED_VALUE";

    /** The size value column */
    private final static String C_SIZE_VALUE="SIZE_VALUE";

    /** The owner value column */
    private final static String C_OWNER_VALUE="OWNER_VALUE";

    /** The group value column */
    private final static String C_GROUP_VALUE="GROUP_VALUE";
    
    /**
   * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
   * Gets the content of the file list template and processe the data input.
   * @param cms The CmsObject.
   * @param templateFile The file list template file
   * @param elementName not used
   * @param parameters Parameters of the request and the template.
   * @param templateSelector Selector of the template tag to be displayed.
   * @return Bytearre containgine the processed data of the template.
   * @exception Throws CmsException if something goes wrong.
   */
   public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
         
     CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);        
    
     // process the selected template
     return startProcessing(cms,xmlTemplateDocument,"",parameters,"template");
   }
    
    
     public Object getFilelist(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
            Hashtable parameters = (Hashtable)userObj;
            Hashtable preferences=new Hashtable();
            StringBuffer output=new StringBuffer();  
            String foldername;
            String currentFolder;
            
            //get the template
            CmsXmlWpTemplateFile template=(CmsXmlWpTemplateFile)doc;
            
            // vectors to store all files and folders in the current folder.
            Vector files;
            Vector folders;
            Enumeration enum;
            
            // file and folder object required to create the file list.
            CmsFile file;
            CmsFolder folder;
            
            HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
            preferences=(Hashtable)session.getValue(C_ADDITIONAL_INFO_PREFERENCES);            
            
            //check if a folder parameter was included in the request.
            // if a foldername was included, overwrite the value in the session for later use.
            foldername=cms.getRequestContext().getRequest().getParameter(C_PARA_FOLDER);
            if (foldername != null) {
                session.putValue(C_PARA_FOLDER,foldername);
            }

            // get the current folder 
            currentFolder=(String)session.getValue(C_PARA_FOLDER);
            if (currentFolder == null) {
                 currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
            }
          
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
            output.append(template.getProcessedXmlDataValue(C_LIST_HEAD,this));
            
            // get all files and folders of the current folder
            folders=cms.getSubFolders(currentFolder);
            files=cms.getFilesInFolder(currentFolder);
            
            // go through all files 
            enum=files.elements();
            while (enum.hasMoreElements()) {
                file=(CmsFile)enum.nextElement();
                //set the output style class according to the project and state of the file
                // HACK
                template.setXmlData(C_CLASS_VALUE,"dateingeaendert");        
                
                // set the filename
                template.setXmlData(C_NAME_VALUE,file.getName());     
                // set the file title
                String title=cms.readMetainformation(file.getAbsolutePath(),C_METAINFO_TITLE);
                if (title==null) {
                    title="";
                }
                template.setXmlData(C_TITLE_VALUE,title);   
                // set the file type
                // todo
                // get the file date
                long time=file.getDateLastModified();
                template.setXmlData(C_CHANGED_VALUE,getNiceDate(time));   
                // get the file size
                template.setXmlData(C_SIZE_VALUE,new Integer(file.getLength()).toString());   
                // get the file state
                // todo
                // get the owner of the file
                A_CmsUser owner = cms.readOwner(file);
                template.setXmlData(C_OWNER_VALUE,owner.getName());
                // get the group of the file
                A_CmsGroup group = cms.readGroup(file);
                template.setXmlData(C_GROUP_VALUE,group.getName());
                
                // as a last step, check which colums must be displayed and add the file
                // to the output.
                template=checkDisplayedColumns(filelist,template,C_SUFFIX_VALUE);
                output.append(template.getProcessedXmlDataValue(C_LIST_ENTRY,this));                
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
