/**
 * EdgeVision Web Viewer
 * Displays processed frames from the Android application
 */

interface FrameStats {
    fps: number;
    resolution: string;
    processingTime: number;
}

class EdgeVisionViewer {
    private processedImage: HTMLImageElement;
    private placeholder: HTMLElement;
    private fpsValue: HTMLElement;
    private resolutionValue: HTMLElement;
    private processingTimeValue: HTMLElement;
    private uploadArea: HTMLElement;
    private fileInput: HTMLInputElement;
    private loadSampleBtn: HTMLButtonElement;
    private clearBtn: HTMLButtonElement;

    constructor() {
        this.processedImage = document.getElementById('processedImage') as HTMLImageElement;
        this.placeholder = document.getElementById('placeholder') as HTMLElement;
        this.fpsValue = document.getElementById('fpsValue') as HTMLElement;
        this.resolutionValue = document.getElementById('resolutionValue') as HTMLElement;
        this.processingTimeValue = document.getElementById('processingTimeValue') as HTMLElement;
        this.uploadArea = document.getElementById('uploadArea') as HTMLElement;
        this.fileInput = document.getElementById('fileInput') as HTMLInputElement;
        this.loadSampleBtn = document.getElementById('loadSampleBtn') as HTMLButtonElement;
        this.clearBtn = document.getElementById('clearBtn') as HTMLButtonElement;

        this.initializeEventListeners();
        this.initializeWebSocket();
    }

    private initializeEventListeners(): void {
        // File upload
        this.uploadArea.addEventListener('click', () => {
            this.fileInput.click();
        });

        this.fileInput.addEventListener('change', (e: Event) => {
            const target = e.target as HTMLInputElement;
            if (target.files && target.files[0]) {
                this.loadImage(target.files[0]);
            }
        });

        // Drag and drop
        this.uploadArea.addEventListener('dragover', (e: DragEvent) => {
            e.preventDefault();
            this.uploadArea.classList.add('dragover');
        });

        this.uploadArea.addEventListener('dragleave', () => {
            this.uploadArea.classList.remove('dragover');
        });

        this.uploadArea.addEventListener('drop', (e: DragEvent) => {
            e.preventDefault();
            this.uploadArea.classList.remove('dragover');
            
            if (e.dataTransfer?.files && e.dataTransfer.files[0]) {
                this.loadImage(e.dataTransfer.files[0]);
            }
        });

        // Buttons
        this.loadSampleBtn.addEventListener('click', () => {
            this.loadSampleImage();
        });

        this.clearBtn.addEventListener('click', () => {
            this.clearImage();
        });
    }

    private loadImage(file: File): void {
        const reader = new FileReader();
        
        reader.onload = (e: ProgressEvent<FileReader>) => {
            if (e.target?.result) {
                this.processedImage.src = e.target.result as string;
                this.processedImage.style.display = 'block';
                this.placeholder.style.display = 'none';
                
                // Update stats when image loads
                this.processedImage.onload = () => {
                    this.updateStats({
                        fps: this.generateRandomFPS(),
                        resolution: `${this.processedImage.naturalWidth}x${this.processedImage.naturalHeight}`,
                        processingTime: this.generateRandomProcessingTime()
                    });
                };
            }
        };

        reader.readAsDataURL(file);
    }

    private loadSampleImage(): void {
        // Create a sample edge-detected image using canvas
        const canvas = document.createElement('canvas');
        canvas.width = 640;
        canvas.height = 480;
        const ctx = canvas.getContext('2d');

        if (ctx) {
            // Create a gradient background
            const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
            gradient.addColorStop(0, '#000000');
            gradient.addColorStop(1, '#1a1a1a');
            ctx.fillStyle = gradient;
            ctx.fillRect(0, 0, canvas.width, canvas.height);

            // Draw some edge-like patterns
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 2;

            // Draw random edge patterns
            for (let i = 0; i < 50; i++) {
                ctx.beginPath();
                const x1 = Math.random() * canvas.width;
                const y1 = Math.random() * canvas.height;
                const x2 = x1 + (Math.random() - 0.5) * 100;
                const y2 = y1 + (Math.random() - 0.5) * 100;
                ctx.moveTo(x1, y1);
                ctx.lineTo(x2, y2);
                ctx.stroke();
            }

            // Draw some circles (edge detected objects)
            for (let i = 0; i < 10; i++) {
                ctx.beginPath();
                const x = Math.random() * canvas.width;
                const y = Math.random() * canvas.height;
                const r = Math.random() * 50 + 20;
                ctx.arc(x, y, r, 0, Math.PI * 2);
                ctx.stroke();
            }

            // Add text
            ctx.font = 'bold 24px Arial';
            ctx.fillStyle = '#ffffff';
            ctx.fillText('Sample Edge Detection', 20, 40);
            ctx.font = '16px Arial';
            ctx.fillText('Generated by EdgeVision', 20, 70);

            // Convert canvas to image
            this.processedImage.src = canvas.toDataURL('image/png');
            this.processedImage.style.display = 'block';
            this.placeholder.style.display = 'none';

            this.updateStats({
                fps: 15,
                resolution: '640x480',
                processingTime: 33
            });
        }
    }

    private clearImage(): void {
        this.processedImage.src = '';
        this.processedImage.style.display = 'none';
        this.placeholder.style.display = 'block';
        
        this.updateStats({
            fps: 0,
            resolution: '--',
            processingTime: 0
        });
    }

    private updateStats(stats: FrameStats): void {
        this.fpsValue.textContent = stats.fps > 0 ? stats.fps.toString() : '--';
        this.resolutionValue.textContent = stats.resolution;
        this.processingTimeValue.textContent = stats.processingTime > 0 
            ? `${stats.processingTime}ms` 
            : '--';
    }

    private loadSampleData(): void {
        // Initialize with default values
        this.updateStats({
            fps: 0,
            resolution: '--',
            processingTime: 0
        });
    }

    private generateRandomFPS(): number {
        // Generate a random FPS between 10-30
        return Math.floor(Math.random() * 20) + 10;
    }

    private generateRandomProcessingTime(): number {
        // Generate a random processing time between 20-50ms
        return Math.floor(Math.random() * 30) + 20;
    }

    /**
     * Simulate receiving data from Android app (for future WebSocket integration)
     */
    private initializeWebSocket(): void {
        const ws = new WebSocket(`ws://${window.location.host}`);

        ws.onopen = () => {
            console.log('Connected to WebSocket server');
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                if (data.image) {
                    this.receiveFrame(data.image, {
                        fps: data.fps || 0,
                        resolution: data.resolution || '--',
                        processingTime: data.processingTime || 0
                    });
                }
            } catch (error) {
                console.error('Error parsing WebSocket message:', error);
            }
        };

        ws.onclose = () => {
            console.log('Disconnected from WebSocket server');
        };

        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
        };
    }

    public receiveFrame(base64Image: string, stats: FrameStats): void {
        this.processedImage.src = `data:image/jpeg;base64,${base64Image}`;
        this.processedImage.style.display = 'block';
        this.placeholder.style.display = 'none';
        this.updateStats(stats);
    }
}

// Initialize the viewer when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const viewer = new EdgeVisionViewer();
    
    // Make viewer available globally for potential external calls
    (window as any).edgeVisionViewer = viewer;
    
    console.log('EdgeVision Web Viewer initialized');
});
