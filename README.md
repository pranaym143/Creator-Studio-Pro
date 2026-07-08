# Creator Studio Pro - Web Animation Suite (React + Vite)

Welcome to the production-ready **React + Vite** code workspace for **Creator Studio Pro**. 

This project is fully structured as a standard, high-performance React application, utilizing **Tailwind CSS** for fluid visual themes, **Lucide React** for professional interface iconography, and a **highly-optimized Canvas-based drawing & animation engine**.

It is 100% compatible with the **Vercel Vite Application Preset**, enabling zero-configuration deployments in seconds.

---

## 📁 Workspace Folder Structure

```text
├── package.json         # Project dependencies & build commands
├── vite.config.js       # Vite build & plugin configurations
├── tailwind.config.js   # Custom Tailwind CSS configuration with variable-driven themes
├── postcss.config.js    # PostCSS styling plugin declarations
├── vercel.json          # Vercel deployment & routing rules
├── index.html           # Main SPA entry page
├── public/              # Static progressive web assets (sw.js, manifest.json)
└── src/                 # Application source
    ├── main.jsx         # React application entrypoint
    ├── index.css        # Global CSS stylesheet, custom theme variables, and keyframe animations
    ├── App.jsx          # Central application coordinator, state managers, and AI assistants
    └── components/      # Modular, reusable React components
        ├── DrawingBoard.jsx  # Interactive canvas drawing mechanics & gesture handlers
        ├── SidebarLeft.jsx   # Sidebar tabs, tool selector grid, and AI action bars
        ├── SidebarRight.jsx  # Dynamic brush modifiers, layer controllers, and overlay toggles
        └── Timeline.jsx      # Animation loop controllers and interactive keyframe ribbon
```

---

## ⚡ Local Development

Get your desktop animation suite running locally in under a minute:

1. **Install Dependencies**:
   ```bash
   npm install
   ```
2. **Run Development Server**:
   ```bash
   npm run dev
   ```
3. **Build Production Bundle**:
   ```bash
   npm run build
   ```
   *Output bundle is generated under `dist/`.*

---

## 🚀 Deploying to Vercel

### Option 1: Import via Vercel Dashboard (Recommended)

1. **Push your code**: Commit and push this repository to your GitHub, GitLab, or Bitbucket account.
2. **Connect on Vercel**: Head to the [Vercel Dashboard](https://vercel.com) and click **Add New** -> **Project**.
3. **Select Preset**: Import your repository. Vercel will automatically detect the **Vite framework preset** and configure:
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. **Deploy**: Click **Deploy**! Your web app is live on a globally-distributed, lightning-fast edge network with automatic SSL.

### Option 2: Deploy using Vercel CLI

1. Install the Vercel CLI globally:
   ```bash
   npm install -g vercel
   ```
2. Run the deployment setup from your project root:
   ```bash
   vercel
   ```
3. To deploy directly to production, run:
   ```bash
   vercel --prod
   ```

---

## 🌟 Premium Suite Highlights

- **Multi-Theme Palette Engine**: Toggle between **Dracula (Deep Slate)**, **Matrix (Neon Green)**, and **Vaporwave (Synthwave Magenta)** aesthetics instantly!
- **8 Custom Brushes**: Includes Solid Pen, Pencil/Charcoal, Watercolor, Neon Glow, HSL Rainbow, Spray Paint, Parallel Strokes, and Eraser.
- **AI-Powered Creator Assistants**:
  - *AI Auto-Coloring*: Propagates color selections and applies beautiful shadows and highlights automatically.
  - *AI Backdrop Generator*: Injects custom-generated vector star fields, aurora gradients, or sunsets on your background layer based on your text prompt.
  - *AI Frame In-Betweens*: Performs vector interpolation to mathematically synthesize smooth transition frames.
- **Precision Viewport Navigation**: Hold `Spacebar` (or middle-mouse click) to pan, and scroll your mouse wheel to zoom anywhere from **10% to 6400%**.
- **Onion Skinning & Grid Overlays**: Track preceding and succeeding frames with configurable transparencies to perfect your timing.
- **Storyboard Planner**: Manage key scenes, write narrative cues, and design animation beats side-by-side with your canvas.
- **Interactive Pose References**: Inject walking, running, jumping, or standing stick-figure wireframes instantly as vector guide layers.
- **Live Code Injector**: Code custom CSS rules or run dynamic JS commands inside the live studio viewport in real-time.
