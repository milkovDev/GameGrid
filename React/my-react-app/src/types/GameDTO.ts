import { DeveloperDTO } from "./DeveloperDTO";
import { GameGenreDTO } from "./GameGenreDTO";
import { GamePlatformDTO } from "./PlatformGenreDTO";
import { PublisherDTO } from "./PublisherDTO";

export interface GameDTO {
    id?: number;
    title: string;
    description: string;
    releaseDate: string;
    coverUrl?: string | null;
    developer: DeveloperDTO;
    publisher: PublisherDTO;
    gameGenres: GameGenreDTO[];
    gamePlatforms: GamePlatformDTO[];
}
