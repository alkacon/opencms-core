cms.loginUser("Admin", "admin.password");
project = cms.createProject("patch_4_4_1", "Project to patch OpenCms" , "Administrators", "Administrators", "1");
cms.getRequestContext().setCurrentProject(project.getId());
cms.copyResourceToProject("/");
cms.importResources("ocsetup/vfs/", "/");
cms.getRequestContext().setCurrentProject(1);
cms.unlockProject(project.getId());
cms.publishProject(project.getId());
