/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opencms.gwt.rebind.rpc;

import java.util.Map;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.rpc.ProxyCreator;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracle;

/**
 * Creates proxies supporting optionally synchronized RPC methods 
 * using the {@link SynchronizedRpcRequest} annotation.<p>
 */
public class CmsRpcProxyCreator extends ProxyCreator {

    /**
     * Constructor.<p>
     * 
     * @param serviceIntf the service interface
     */
    public CmsRpcProxyCreator(@SuppressWarnings("hiding") JClassType serviceIntf) {

        super(serviceIntf);
    }

    /**
     * @see com.google.gwt.user.rebind.rpc.ProxyCreator#generateProxyMethods(com.google.gwt.user.rebind.SourceWriter, com.google.gwt.user.rebind.rpc.SerializableTypeOracle, com.google.gwt.core.ext.typeinfo.TypeOracle, java.util.Map)
     */
    @Override
    protected void generateProxyMethods(
        SourceWriter w,
        SerializableTypeOracle serializableTypeOracle,
        TypeOracle typeOracle,
        Map<JMethod, JMethod> syncMethToAsyncMethMap) {

        super.generateProxyMethods(w, serializableTypeOracle, typeOracle, syncMethToAsyncMethMap);
        generateSyncOverride(w, syncMethToAsyncMethMap);
    }

    /**
     * Generates a method to check if a given RPC method has to be synchronized.<p> 
     * 
     * @param srcWriter the source write to generate the code with
     * @param syncMethToAsyncMethMap the method map
     */
    protected void generateSyncOverride(SourceWriter srcWriter, Map<JMethod, JMethod> syncMethToAsyncMethMap) {

        srcWriter.println("@Override");
        srcWriter.println("public boolean isSync(String methodName) {");

        JMethod[] syncMethods = serviceIntf.getOverridableMethods();
        for (JMethod syncMethod : syncMethods) {
            JMethod asyncMethod = syncMethToAsyncMethMap.get(syncMethod);
            if (!asyncMethod.isAnnotationPresent(SynchronizedRpcRequest.class)) {
                continue;
            }
            srcWriter.indentln("if (methodName.equals(\""
                + getProxySimpleName()
                + "."
                + syncMethod.getName()
                + "\")) {");
            srcWriter.indentln("return true;");
            srcWriter.indentln("}");
        }
        srcWriter.indentln("return false;");
        srcWriter.println("}");
    }
}
