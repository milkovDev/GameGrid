import { GameDTO } from "./GameDTO";

export interface UserGameListEntryDTO {
    id?: number; // Long converted to number
    userId: string; // UUID converted to string
    game: GameDTO;
    status: string;
    isFavorite: boolean;
    rating: number | null; // Integer can be null in Java, so optional in TS
    reviewText: string | null; // String can be null in Java, so optional in TS
  }