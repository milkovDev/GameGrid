import { UserGameListEntryDTO } from "./UserGameListEntryDTO";

export interface UserDTO {
    id: string; // UUID converted to string
    displayName: string;
    bio: string;
    avatarUrl?: string;
    userGameListEntries: UserGameListEntryDTO[];
  }