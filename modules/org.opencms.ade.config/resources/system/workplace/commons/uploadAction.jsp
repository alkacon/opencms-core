<% 
	org.opencms.ade.upload.CmsUploadBean upload = new org.opencms.ade.upload.CmsUploadBean(pageContext, request, response);
	upload.setUploadDelay(0);
	out.write(upload.start());
%>
