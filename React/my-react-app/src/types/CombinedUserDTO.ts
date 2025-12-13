import { UserDTO } from "./UserDTO";
import { UserNodeDTO } from "./UserNodeDTO";

export interface CombinedUserDTO {
    relationalData: UserDTO;
    graphData: UserNodeDTO;
}