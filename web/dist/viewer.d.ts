/**
 * EdgeVision Web Viewer
 * Displays processed frames from the Android application
 */
interface FrameStats {
    fps: number;
    resolution: string;
    processingTime: number;
}
declare class EdgeVisionViewer {
    private processedImage;
    private placeholder;
    private fpsValue;
    private resolutionValue;
    private processingTimeValue;
    private uploadArea;
    private fileInput;
    private loadSampleBtn;
    private clearBtn;
    constructor();
    private initializeEventListeners;
    private loadImage;
    private loadSampleImage;
    private clearImage;
    private updateStats;
    private loadSampleData;
    private generateRandomFPS;
    private generateRandomProcessingTime;
    /**
     * Simulate receiving data from Android app (for future WebSocket integration)
     */
    private initializeWebSocket;
    receiveFrame(base64Image: string, stats: FrameStats): void;
}
//# sourceMappingURL=viewer.d.ts.map