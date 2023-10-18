<% 
	org.opencms.ade.upload.CmsUploadBean upload = new org.opencms.ade.upload.CmsUploadBean(pageContext, request, response);
	org.opencms.main.OpenCms.getWorkplaceManager().checkAdeGalleryUpload(upload.getCmsObject());
	upload.setUploadDelay(0);
	out.write(upload.start());
%>
