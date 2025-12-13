import { ArticleBlockDTO } from './ArticleBlockDTO';

export interface ArticleDTO {
    id?: number;
    title: string;
    author: string;
    authorId: string;
    publishedAt: string;
    featuredImageUrl: string | null;
    articleBlocks: ArticleBlockDTO[];
}