package com.opencms.modules.cluster.events;

import com.opencms.defaults.I_CmsLifeCycle;
import com.opencms.file.CmsObject;
import com.opencms.core.*;
import com.opencms.flex.*;
import com.opencms.modules.cluster.comm.*;
import java.util.*;
import java.net.*;

/**
 * Title:        OpenCms
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsMaster implements I_CmsLifeCycle, I_CmsEventListener {

    /**
     * names of the used parameter of this module
     */
    public static String C_PARA_USER = "user";
    public static String C_PARA_PASSWORD = "password";
    public static String C_PARA_FILENAME = "filename";
    public static String C_PARA_SLAVE_LIST = "slaveserverlist";

    public CmsMaster(){
    }

    public void startUp(CmsObject cms) {
        com.opencms.core.A_OpenCms.addCmsEventListener(this);
    }
    public void shutDown() {
    }

    /**
     * Handles the opencms events.
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(CmsEvent event){
        /*  This is only needed when static export is active
        if(event.getType() == EVENT_STATIC_EXPORT){
            Vector data = (Vector)event.getData();
            if(data != null && data.size() > 0){
                // we will start the command on the slave systems
                // we need the module parameters: user, password, listOfSlaveServer
                CmsObject cms = event.getCmsObject();
                try{
                    String user = cms.getRegistry().getModuleParameter(
                            "com.opencms.modules.cluster.events", C_PARA_USER);
                    String password = cms.getRegistry().getModuleParameter(
                            "com.opencms.modules.cluster.events", C_PARA_PASSWORD);
                    String list = cms.getRegistry().getModuleParameter(
                            "com.opencms.modules.cluster.events", C_PARA_SLAVE_LIST);
                    StringTokenizer seperatedList = new StringTokenizer(list, ",");
                    while(seperatedList.hasMoreTokens()){
                        // for each of this tokens we send a command to that server
                        // todo: create the thread and start the exportStaticResources(Vector linksToExport)
                        // method on the cms Object.
                    }
                }catch(CmsException e){
                }
            }
        }
        */
        if(event.getType() == EVENT_PUBLISH_PROJECT){
                // we will start the command on the slave systems
                // we need the module parameters: user, password, listOfSlaveServer
                CmsObject cms = event.getCmsObject();
                try{
                    String user = cms.getRegistry().getModuleParameter(
                            "com.opencms.modules.cluster.events", C_PARA_USER);
                    String password = cms.getRegistry().getModuleParameter(
                            "com.opencms.modules.cluster.events", C_PARA_PASSWORD);
                    String file = cms.getRegistry().getModuleParameter(
                            "com.opencms.modules.cluster.events", C_PARA_FILENAME);
                    String list = cms.getRegistry().getModuleParameter(
                            "com.opencms.modules.cluster.events", C_PARA_SLAVE_LIST);
                    StringTokenizer seperatedList = new StringTokenizer(list, ",");
                    // we need two commands: clearCache and clearElementCache
                    // create the container
                    CmsCommandContainer container = new CmsCommandContainer(user, password, false);
                    container.addCommand(new CmsCommand("clearcache", new Object[]{}));
                    container.addCommand(new CmsCommand("clearElementCache", new Object[]{}));
                    container.addCommand(new CmsCommand("exportStaticResources", new Object[]{new Vector()}));
                    while(seperatedList.hasMoreTokens()){
                        // for each of this tokens we send a command to that server
                        try{
                            URL target = new URL(seperatedList.nextToken() + file);
                            Thread sender = new CmsSendEventThread(container, target);
                            sender.start();
                        }catch(MalformedURLException e){
                            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                                A_OpenCms.log(A_OpenCms.C_MODULE_DEBUG,
                                    " module com.opencms.modules.cluster.events cant send command "
                                    +e.getMessage());
                            }
                        }
                    }
                }catch(CmsException e){
                }
        }
    }

}