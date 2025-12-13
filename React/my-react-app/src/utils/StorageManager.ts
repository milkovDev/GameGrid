const STORAGE_KEY = 'activeChats';
const LAST_CHAT_KEY = 'lastOpenChat';

export class StorageManager {
    private userId: string | null;

    constructor(userId: string | null) {
        this.userId = userId;
    }

    getActiveChats(): string[] {
        if (!this.userId) return [];
        
        const stored = localStorage.getItem(STORAGE_KEY);
        if (!stored) return [];
        
        try {
            const allUserChats = JSON.parse(stored);
            return allUserChats[this.userId] || [];
        } catch (error) {
            console.error('Failed to parse stored chats:', error);
            return [];
        }
    }

    saveActiveChats(userActiveChats: string[]): void {
        if (!this.userId) return;
        
        const stored = localStorage.getItem(STORAGE_KEY);
        let allUserChats = {};
        
        if (stored) {
            try {
                allUserChats = JSON.parse(stored);
            } catch (error) {
                console.error('Failed to parse stored chats:', error);
            }
        }
        
        allUserChats = { ...allUserChats, [this.userId]: userActiveChats };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(allUserChats));
    }

    getLastOpenChat(): string | null {
        if (!this.userId) return null;
        
        const stored = localStorage.getItem(LAST_CHAT_KEY);
        if (!stored) return null;
        
        try {
            const allUserLastChats = JSON.parse(stored);
            return allUserLastChats[this.userId] || null;
        } catch (error) {
            console.error('Failed to parse stored last chats:', error);
            return null;
        }
    }

    saveLastOpenChat(userId: string): void {
        if (!this.userId) return;
        
        const stored = localStorage.getItem(LAST_CHAT_KEY);
        let allUserLastChats = {};
        
        if (stored) {
            try {
                allUserLastChats = JSON.parse(stored);
            } catch (error) {
                console.error('Failed to parse stored last chats:', error);
            }
        }
        
        allUserLastChats = { ...allUserLastChats, [this.userId]: userId };
        localStorage.setItem(LAST_CHAT_KEY, JSON.stringify(allUserLastChats));
    }
}