<%@ page session="false" taglibs="cms" %>
<div>
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

</div>