export class imageApi {
    //local
    //private static readonly BASE_URL = 'http://localhost:8080';
    //online
    private static readonly BASE_URL = 'https://quarkus.gamegrid.buzz';
    private static readonly DEFAULT_COVER_URL = '/api/images/default-cover.jpg';

    static getGameImage(coverUrl?: string | null): string {
        return (coverUrl && coverUrl !== 'default-cover.jpg') 
            ? `${this.BASE_URL}${coverUrl}` 
            : `${this.BASE_URL}${this.DEFAULT_COVER_URL}`;
    }

    static getArticleImage(coverUrl?: string | null): string {
        return (coverUrl && coverUrl !== 'default-cover.jpg') 
            ? `${this.BASE_URL}${coverUrl}` 
            : `${this.BASE_URL}${this.DEFAULT_COVER_URL}`;
    }

    static getAvatarImage(coverUrl?: string | null): string {
        return (coverUrl && coverUrl !== 'default-cover.jpg') 
            ? `${this.BASE_URL}${coverUrl}` 
            : `${this.BASE_URL}${this.DEFAULT_COVER_URL}`;
    }
}