const width = 440;
const height = 720;
const scene = Canvas.scene;
const oldBackground = scene.background;
const sourceRenderer = Preview.selected.renderer;
const renderer = new THREE.WebGLRenderer({antialias: true, alpha: false, preserveDrawingBuffer: true});
renderer.setSize(width, height);
renderer.setPixelRatio(1);
if (sourceRenderer.outputColorSpace !== undefined) renderer.outputColorSpace = sourceRenderer.outputColorSpace;
if (sourceRenderer.outputEncoding !== undefined) renderer.outputEncoding = sourceRenderer.outputEncoding;
renderer.toneMapping = sourceRenderer.toneMapping;
renderer.toneMappingExposure = sourceRenderer.toneMappingExposure;
const views = params.views || [[0, 17.5, -100], [65, 38, -100], [-65, 32, 100]];
const canvas = document.createElement('canvas');
canvas.width = width * views.length;
canvas.height = height;
const context = canvas.getContext('2d');
const camera = new THREE.OrthographicCamera(-11.9, 11.9, 19.48, -19.48, 0.1, 400);
try {
    scene.background = new THREE.Color('#827c75');
    Canvas.withoutGizmos(() => {
        for (let i = 0; i < views.length; i++) {
            camera.position.fromArray(views[i]);
            camera.lookAt(0, 17.5, 0);
            camera.updateProjectionMatrix();
            renderer.render(scene, camera);
            context.drawImage(renderer.domElement, i * width, 0);
        }
    });
    return canvas.toDataURL('image/png');
} finally {
    scene.background = oldBackground;
    renderer.dispose();
    renderer.forceContextLoss();
}
