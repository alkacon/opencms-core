<% 
	org.opencms.ade.upload.CmsUploadBean upload = new org.opencms.ade.upload.CmsUploadBean(pageContext, request, response);
	org.opencms.main.OpenCms.getRoleManager().checkRole(upload.getCmsObject(), org.opencms.security.CmsRole.EDITOR);
	upload.setUploadDelay(0);
	out.write(upload.start());
%>
