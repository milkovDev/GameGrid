package org.acme.Misc;

import jakarta.ws.rs.core.MediaType;
import org.acme.PGDB.DTOs.ArticleDTO;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class ArticleUploadForm {

    @RestForm
    @PartType(MediaType.APPLICATION_JSON)
    public ArticleDTO data;

    @RestForm
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public FileUpload file;

    public ArticleUploadForm() {
    }

    public ArticleDTO getData() {
        return data;
    }

    public void setData(ArticleDTO data) {
        this.data = data;
    }

    public FileUpload getFile() {
        return file;
    }

    public void setFile(FileUpload file) {
        this.file = file;
    }
}