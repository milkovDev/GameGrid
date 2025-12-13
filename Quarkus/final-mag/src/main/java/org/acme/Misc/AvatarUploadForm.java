package org.acme.Misc;

import jakarta.ws.rs.core.MediaType;
import org.acme.PGDB.DTOs.UserDTO;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class AvatarUploadForm {

    @RestForm
    @PartType(MediaType.APPLICATION_JSON)
    public UserDTO data;

    @RestForm
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public FileUpload file;

    public AvatarUploadForm() {
    }

    public UserDTO getData() {
        return data;
    }

    public void setData(UserDTO data) {
        this.data = data;
    }

    public FileUpload getFile() {
        return file;
    }

    public void setFile(FileUpload file) {
        this.file = file;
    }
}