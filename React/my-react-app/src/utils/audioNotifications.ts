// utils/audioNotifications.ts

class AudioNotificationManager {
    private messageSound: HTMLAudioElement | null = null;
    private notificationSound: HTMLAudioElement | null = null;

    constructor() {
        this.initializeSounds();
    }

    private initializeSounds() {
        try {
            // Option 1: Place audio files in public/sounds/ directory
            this.messageSound = new Audio('/sounds/message.mp3');
            this.notificationSound = new Audio('/sounds/notification.mp3');
            
            // Set reasonable defaults
            if (this.messageSound) {
                this.messageSound.preload = 'auto';
                this.messageSound.volume = 0.6; // 60% volume
            }
            if (this.notificationSound) {
                this.notificationSound.preload = 'auto';
                this.notificationSound.volume = 0.6; // 60% volume
            }
        } catch (error) {
            console.warn('Failed to initialize notification sounds:', error);
        }
    }

    async playMessageSound() {
        if (!this.messageSound) return;
        
        try {
            this.messageSound.currentTime = 0;
            await this.messageSound.play();
        } catch (error) {
            console.warn('Failed to play message sound:', error);
        }
    }

    async playNotificationSound() {
        if (!this.notificationSound) return;
        
        try {
            this.notificationSound.currentTime = 0;
            await this.notificationSound.play();
        } catch (error) {
            console.warn('Failed to play notification sound:', error);
        }
    }

    // Request permission for audio (call this on user's first interaction)
    async requestAudioPermission(): Promise<boolean> {
        try {
            if (this.messageSound) {
                this.messageSound.volume = 0;
                await this.messageSound.play();
                this.messageSound.pause();
                this.messageSound.currentTime = 0;
                this.messageSound.volume = 0.6;
            }
            return true;
        } catch (error) {
            console.warn('Audio permission not granted:', error);
            return false;
        }
    }
}

// Create singleton instance
export const audioNotificationManager = new AudioNotificationManager();