import {defineConfig} from 'vite';
import {VitePWA} from 'vite-plugin-pwa';

// https://vitejs.dev/config/
export default defineConfig({
    base: "/",
    build: {
        sourcemap: true,
        assetsDir: "code",
        outDir: "build/resources/main/static"
    },
    plugins: [
        VitePWA({
            strategies: "injectManifest",
            injectManifest: {
                swSrc: 'public/sw.js',
                swDest: 'build/resources/main/static/sw.js',
                globDirectory: 'build/resources/main/static',
                globPatterns: [
                    '**/*.{html,js,css,json, png}',
                ],
            },
            devOptions: {
                enabled: true
            }
        })
    ]
})
