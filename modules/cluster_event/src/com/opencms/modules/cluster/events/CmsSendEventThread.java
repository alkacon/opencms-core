package com.opencms.modules.cluster.events;

import com.opencms.modules.cluster.comm.*;
import com.opencms.core.*;
import java.net.*;
import java.io.*;

/**
 * Title:        OpenCms
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsSendEventThread extends Thread {

    private CmsCommandContainer m_container;
    private URL m_url;

    public CmsSendEventThread(CmsCommandContainer container, URL url) {
        m_container = container;
        m_url = url;
    }

    public void run() {
        try{
            m_container.sendCommand(m_url);
        }catch(IOException e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_MODULE_DEBUG,
                    " module com.opencms.modules.cluster.events cant execute command "
                    +e.getMessage());
            }
        }
    }
}