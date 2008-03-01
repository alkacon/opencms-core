<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />

<h1>Congratulations!</h1>

<h2>You have setup OpenCms successfully.</h2>

<p>Your installed OpenCms version is: <cms:info property="opencms.version" /><br>
<span class="small">Running on 
<cms:info property="java.vm.vendor" /> 
<cms:info property="java.vm.name" /> 
<cms:info property="java.vm.version" /> 
<cms:info property="java.vm.info" /> with
<cms:info property="os.name" /> 
<cms:info property="os.version" /> 
(<cms:info property="os.arch" />)</span></p>

<p>As you may have guessed by now, this is the default OpenCms home page. 
It can <em>not</em> be found on the local file system ;-) but in the OpenCms
<em>virtual file system</em> or VFS, which is served from the connected database.
You can access the VFS though the OpenCms workplace.</p>

<div class="high">
<p>To login to the OpenCms workplace, point your browser to the following URL:</p>
<p><a href="#" onclick="window.open('<cms:link>/system/login/</cms:link>');return false;"><cms:link>/system/login/</cms:link></a></p>
</div>

<div class="login">
You are currently identified as:<br>
User: <tt><cms:user property="name" /></tt><br>
Description: <tt><cms:user property="description" /></tt>
</div>

<p>Use the following account information for your first login:</p>
<p>
Username: <tt>Admin</tt><br/>
Password: <tt>admin</tt>
</p>

<p><strong>Important:</strong> You should change this default password immediately,
before someone else does it for you.</p>

<p>If you're seeing this page, and you don't think you should be, 
then either you're a a user who has arrived at a new installation of OpenCms, 
or you're an administrator who hasn't got his/her setup quite right. 
Providing the latter is the case, please refer to the OpenCms Documentation 
available at <a href="http://www.opencms.org">http://www.opencms.org</a>
for more detailed setup and administration information.</p>

<p>In case you have questions about OpenCms, please use the <b>opencms-dev</b> mailing list.
This is currently the list for all questions regarding OpenCms setup, configuration and development.
Check the <a href="<cms:link>/release/mailinglist.html</cms:link>">mailing list</a> page to lean how to subscribe to this list.
</p>

<p>Have fun exploring OpenCms!</p>

<cms:include property="template" element="foot" />


