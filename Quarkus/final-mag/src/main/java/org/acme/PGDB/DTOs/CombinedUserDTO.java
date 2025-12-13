package org.acme.PGDB.DTOs;

import org.acme.N4JDB.DTOs.UserNodeDTO;

public class CombinedUserDTO {
    public UserDTO relationalData;
    public UserNodeDTO graphData;

    public CombinedUserDTO() {}

    public CombinedUserDTO(UserDTO relationalData, UserNodeDTO graphData) {
        this.relationalData = relationalData;
        this.graphData = graphData;
    }

    public UserDTO getRelationalData() {
        return relationalData;
    }

    public void setRelationalData(UserDTO relationalData) {
        this.relationalData = relationalData;
    }

    public UserNodeDTO getGraphData() {
        return graphData;
    }

    public void setGraphData(UserNodeDTO graphData) {
        this.graphData = graphData;
    }
}
