export interface UserNodeDTO {
    id: number; // Long converted to number
    userId: string;
    displayName: string;
    following: Set<string>;
    followers: Set<string>;
  }