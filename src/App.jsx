import React, { useState, useEffect, useRef } from 'react';
import SidebarLeft from './components/SidebarLeft';
import SidebarRight from './components/SidebarRight';
import DrawingBoard from './components/DrawingBoard';
import Timeline from './components/Timeline';
import { Sparkles, Sun, Moon, Terminal, Play, ShieldAlert, MonitorPlay, Undo2 } from 'lucide-react';

export default function App() {
  // Theme state
  const [theme, setTheme] = useState('dracula'); // dracula, matrix, vaporwave

  // Frame sequence state
  const [frames, setFrames] = useState([
    { id: 1, drawingPaths: [], backgroundPaths: [] }
  ]);
  const [currentFrameIndex, setCurrentFrameIndex] = useState(0);

  // Playback state
  const [isPlaying, setIsPlaying] = useState(false);
  const [animationFPS, setAnimationFPS] = useState(12);

  // Overlay state
  const [onionSkinEnabled, setOnionSkinEnabled] = useState(true);
  const [canvasGridEnabled, setCanvasGridEnabled] = useState(true);

  // Tool dynamic values
  const [currentTool, setCurrentTool] = useState('brush'); // brush, pencil, watercolor, neon, rainbow, spray, multi, eraser
  const [brushSize, setBrushSize] = useState(12);
  const [brushOpacity, setBrushOpacity] = useState(1.0);
  const [brushColor, setBrushColor] = useState('#bd93f9');
  const [activeLayer, setActiveLayer] = useState('main'); // main or background
  const [lineStabilizerValue, setLineStabilizerValue] = useState(5);

  // Viewport states
  const [zoomScale, setZoomScale] = useState(1.0);
  const [panX, setPanX] = useState(0);
  const [panY, setPanY] = useState(0);

  // HUD and UI state
  const [hudPressure, setHudPressure] = useState('Stylus Off');
  const [activeTab, setActiveTab] = useState('draw'); // draw, creative, ai, pwa
  const [mainLayerVisible, setMainLayerVisible] = useState(true);
  const [backgroundLayerVisible, setBackgroundLayerVisible] = useState(true);
  const [layerOpacity, setLayerOpacity] = useState(100);

  // Storyboard planner
  const [storyboard, setStoryboard] = useState([
    { id: 1, title: 'Scene 1: Opening Shot', text: 'Introduction of the character drawing. The camera slow-pans leftward.' },
    { id: 2, title: 'Scene 2: Transition Loop', text: 'Character transforms with active colors glowing.' }
  ]);

  // AI Generator inputs
  const [bgPrompt, setBgPrompt] = useState('cosmic starry nebula');
  const [customCode, setCustomCode] = useState('/* Add live CSS here */\nbody {\n  animation: pulse-bg 10s infinite alternate;\n}\n@keyframes pulse-bg {\n  0% { filter: contrast(1); }\n  100% { filter: contrast(1.1); }\n}');
  const [customCodeType, setCustomCodeType] = useState('css');

  // Loading States
  const [isColoringLoading, setIsColoringLoading] = useState(false);
  const [isBgLoading, setIsBgLoading] = useState(false);
  const [isInterpolationLoading, setIsInterpolationLoading] = useState(false);

  // Toast feedback overlay
  const [toast, setToast] = useState({ visible: false, message: '' });

  const showToast = (msg) => {
    setToast({ visible: true, message: msg });
  };

  useEffect(() => {
    if (toast.visible) {
      const timer = setTimeout(() => {
        setToast({ visible: false, message: '' });
      }, 2500);
      return () => clearTimeout(timer);
    }
  }, [toast.visible]);

  // Handle Playback Loop
  useEffect(() => {
    let interval = null;
    if (isPlaying) {
      interval = setInterval(() => {
        setCurrentFrameIndex((prevIndex) => {
          if (prevIndex < frames.length - 1) {
            return prevIndex + 1;
          } else {
            return 0;
          }
        });
      }, 1000 / animationFPS);
    } else {
      clearInterval(interval);
    }
    return () => clearInterval(interval);
  }, [isPlaying, frames.length, animationFPS]);

  // Apply custom CSS/JS
  const applyInjectedCustomCode = () => {
    try {
      if (customCodeType === 'css') {
        let styleEl = document.getElementById('injected-studio-css');
        if (!styleEl) {
          styleEl = document.createElement('style');
          styleEl.id = 'injected-studio-css';
          document.head.appendChild(styleEl);
        }
        styleEl.innerHTML = customCode;
        showToast('Successfully injected dynamic stylesheet rules!');
      } else {
        // Run as functional script
        const runner = new Function('showToast', 'frames', 'currentFrameIndex', customCode);
        runner(showToast, frames, currentFrameIndex);
        showToast('Successfully executed dynamic script binding!');
      }
    } catch (err) {
      showToast(`Injection error: ${err.message}`);
    }
  };

  // Setup Stick Pose references
  const injectPoseTemplate = (poseName) => {
    const poses = {
      walking: [
        { points: [{ x: 300, y: 150 }, { x: 300, y: 250 }], color: '#8be9fd', size: 6, tool: 'brush', opacity: 0.8 }, // Torso
        { points: [{ x: 300, y: 150 }, { x: 270, y: 190 }, { x: 290, y: 220 }], color: '#ff79c6', size: 5, tool: 'brush', opacity: 0.8 }, // Left Arm
        { points: [{ x: 300, y: 150 }, { x: 330, y: 190 }, { x: 310, y: 220 }], color: '#50fa7b', size: 5, tool: 'brush', opacity: 0.8 }, // Right Arm
        { points: [{ x: 300, y: 250 }, { x: 275, y: 310 }, { x: 250, y: 370 }], color: '#ffb86c', size: 6, tool: 'brush', opacity: 0.8 }, // Left Leg
        { points: [{ x: 300, y: 250 }, { x: 325, y: 310 }, { x: 345, y: 370 }], color: '#f1fa8c', size: 6, tool: 'brush', opacity: 0.8 }, // Right Leg
      ],
      running: [
        { points: [{ x: 330, y: 160 }, { x: 285, y: 235 }], color: '#8be9fd', size: 6, tool: 'brush', opacity: 0.8 }, // Torso Tilted
        { points: [{ x: 330, y: 160 }, { x: 370, y: 190 }, { x: 400, y: 160 }], color: '#ff79c6', size: 5, tool: 'brush', opacity: 0.8 }, // Left Arm Out
        { points: [{ x: 330, y: 160 }, { x: 280, y: 180 }, { x: 245, y: 200 }], color: '#50fa7b', size: 5, tool: 'brush', opacity: 0.8 }, // Right Arm Back
        { points: [{ x: 285, y: 235 }, { x: 330, y: 285 }, { x: 295, y: 345 }], color: '#ffb86c', size: 6, tool: 'brush', opacity: 0.8 }, // Left Leg Bent
        { points: [{ x: 285, y: 235 }, { x: 235, y: 275 }, { x: 195, y: 310 }], color: '#f1fa8c', size: 6, tool: 'brush', opacity: 0.8 }, // Right Leg trailing
      ],
      jumping: [
        { points: [{ x: 300, y: 120 }, { x: 300, y: 195 }], color: '#8be9fd', size: 6, tool: 'brush', opacity: 0.8 }, // Torso
        { points: [{ x: 300, y: 120 }, { x: 240, y: 80 }, { x: 195, y: 65 }], color: '#ff79c6', size: 5, tool: 'brush', opacity: 0.8 }, // Left Arm Raised
        { points: [{ x: 300, y: 120 }, { x: 360, y: 80 }, { x: 405, y: 65 }], color: '#50fa7b', size: 5, tool: 'brush', opacity: 0.8 }, // Right Arm Raised
        { points: [{ x: 300, y: 195 }, { x: 250, y: 250 }, { x: 225, y: 310 }], color: '#ffb86c', size: 6, tool: 'brush', opacity: 0.8 }, // Left Leg Out
        { points: [{ x: 300, y: 195 }, { x: 350, y: 250 }, { x: 375, y: 310 }], color: '#f1fa8c', size: 6, tool: 'brush', opacity: 0.8 }, // Right Leg Out
      ],
      standing: [
        { points: [{ x: 300, y: 140 }, { x: 300, y: 260 }], color: '#8be9fd', size: 6, tool: 'brush', opacity: 0.8 }, // Torso
        { points: [{ x: 300, y: 140 }, { x: 260, y: 190 }, { x: 260, y: 245 }], color: '#ff79c6', size: 5, tool: 'brush', opacity: 0.8 }, // Left Arm Hanging
        { points: [{ x: 300, y: 140 }, { x: 340, y: 190 }, { x: 340, y: 245 }], color: '#50fa7b', size: 5, tool: 'brush', opacity: 0.8 }, // Right Arm Hanging
        { points: [{ x: 300, y: 260 }, { x: 275, y: 365 }], color: '#ffb86c', size: 6, tool: 'brush', opacity: 0.8 }, // Left Leg
        { points: [{ x: 300, y: 260 }, { x: 325, y: 365 }], color: '#f1fa8c', size: 6, tool: 'brush', opacity: 0.8 }, // Right Leg
      ]
    };

    const targetPose = poses[poseName];
    if (targetPose) {
      setFrames(prev => {
        const copy = [...prev];
        const activeFrame = copy[currentFrameIndex];
        activeFrame.drawingPaths = [...activeFrame.drawingPaths, ...targetPose];
        return copy;
      });
      showToast(`Applied ${poseName.toUpperCase()} skeleton pose vector reference!`);
    }
  };

  // AI Coloring Simulation
  const triggerAIColoring = () => {
    setIsColoringLoading(true);
    setTimeout(() => {
      setFrames(prev => {
        const copy = [...prev];
        const activeFrame = copy[currentFrameIndex];
        
        // Generate beautiful highlights and shadows for each existing stroke
        const nextPaths = [...activeFrame.drawingPaths];
        activeFrame.drawingPaths.forEach(path => {
          if (path.tool !== 'eraser' && path.points.length > 1) {
            // Shadow stroke: offset by 10px down, dark transparent color
            const shadowPoints = path.points.map(pt => ({ x: pt.x + 8, y: pt.y + 8 }));
            nextPaths.push({
              tool: 'brush',
              color: '#0a0a0f',
              size: path.size * 1.1,
              opacity: 0.25,
              points: shadowPoints
            });
            // Highlight stroke: offset by 4px up, light yellow transparent color
            const highlightPoints = path.points.map(pt => ({ x: pt.x - 4, y: pt.y - 4 }));
            nextPaths.push({
              tool: 'watercolor',
              color: '#f1fa8c',
              size: path.size * 0.7,
              opacity: 0.35,
              points: highlightPoints
            });
          }
        });
        activeFrame.drawingPaths = nextPaths;
        return copy;
      });
      setIsColoringLoading(false);
      showToast('AI Coloring: Layer propagation and drop-shadows applied!');
    }, 1200);
  };

  // AI Backdrop Simulation
  const triggerAIBackground = () => {
    setIsBgLoading(true);
    setTimeout(() => {
      setFrames(prev => {
        const copy = [...prev];
        const activeFrame = copy[currentFrameIndex];

        // Generate abstract starry nebula or celestial backdrop patterns using sprays and glow watercolor arcs
        const genPaths = [];
        
        if (bgPrompt.toLowerCase().includes('space') || bgPrompt.toLowerCase().includes('nebula') || bgPrompt.toLowerCase().includes('star')) {
          // Purple/Cyan gas clouds
          genPaths.push({
            tool: 'watercolor',
            color: '#bd93f9',
            size: 150,
            opacity: 0.15,
            points: [{ x: 150, y: 150 }, { x: 300, y: 200 }, { x: 450, y: 300 }]
          });
          genPaths.push({
            tool: 'watercolor',
            color: '#8be9fd',
            size: 130,
            opacity: 0.12,
            points: [{ x: 450, y: 100 }, { x: 300, y: 250 }, { x: 100, y: 350 }]
          });
          // Star fields using spray
          genPaths.push({
            tool: 'spray',
            color: '#ffffff',
            size: 80,
            opacity: 0.8,
            points: [{ x: 100, y: 100 }, { x: 200, y: 300 }, { x: 500, y: 150 }]
          });
          genPaths.push({
            tool: 'spray',
            color: '#f1fa8c',
            size: 60,
            opacity: 0.9,
            points: [{ x: 400, y: 80 }, { x: 150, y: 380 }]
          });
        } else {
          // Standard warm sunset backdrop
          genPaths.push({
            tool: 'watercolor',
            color: '#ff5555',
            size: 120,
            opacity: 0.2,
            points: [{ x: 300, y: 400 }, { x: 300, y: 250 }]
          });
          genPaths.push({
            tool: 'watercolor',
            color: '#ffb86c',
            size: 100,
            opacity: 0.2,
            points: [{ x: 300, y: 400 }, { x: 100, y: 300 }]
          });
          genPaths.push({
            tool: 'watercolor',
            color: '#f1fa8c',
            size: 80,
            opacity: 0.25,
            points: [{ x: 300, y: 400 }, { x: 500, y: 320 }]
          });
        }

        activeFrame.backgroundPaths = [...activeFrame.backgroundPaths, ...genPaths];
        return copy;
      });
      setIsBgLoading(false);
      showToast(`AI Backdrop: Generated backdrop based on prompt "${bgPrompt}"!`);
    }, 1500);
  };

  // AI Frame Interpolation
  const triggerAIInterpolation = () => {
    if (frames.length < 2) {
      showToast('Add at least 2 frames to perform vector in-betweening!');
      return;
    }
    setIsInterpolationLoading(true);
    setTimeout(() => {
      setFrames(prev => {
        const copy = [...prev];
        const nextIndex = currentFrameIndex < copy.length - 1 ? currentFrameIndex + 1 : 0;
        const frameA = copy[currentFrameIndex];
        const frameB = copy[nextIndex];

        // Synthesize intermediate points mathematically
        const inBetweenDrawingPaths = [];
        const limit = Math.min(frameA.drawingPaths.length, frameB.drawingPaths.length);

        for (let i = 0; i < limit; i++) {
          const pathA = frameA.drawingPaths[i];
          const pathB = frameB.drawingPaths[i];
          const pointsA = pathA.points;
          const pointsB = pathB.points;
          const interpolatedPoints = [];
          const ptsLimit = Math.min(pointsA.length, pointsB.length);

          for (let j = 0; j < ptsLimit; j++) {
            interpolatedPoints.push({
              x: (pointsA[j].x + pointsB[j].x) / 2,
              y: (pointsA[j].y + pointsB[j].y) / 2
            });
          }

          inBetweenDrawingPaths.push({
            tool: pathA.tool,
            size: pathA.size,
            color: pathA.color,
            opacity: (pathA.opacity + pathB.opacity) / 2,
            points: interpolatedPoints
          });
        }

        const newFrame = {
          id: Date.now(),
          drawingPaths: inBetweenDrawingPaths,
          backgroundPaths: [...frameA.backgroundPaths] // Copy backdrop as fallback
        };

        // Insert new frame directly in-between
        copy.splice(currentFrameIndex + 1, 0, newFrame);
        return copy;
      });
      setIsInterpolationLoading(false);
      showToast('AI Frame Interpolation: In-between motion frames successfully synthesized!');
    }, 1400);
  };

  // Timeline operations
  const handleAddBlankFrame = () => {
    setFrames((prev) => {
      const copy = [...prev];
      copy.splice(currentFrameIndex + 1, 0, {
        id: Date.now(),
        drawingPaths: [],
        backgroundPaths: []
      });
      return copy;
    });
    setCurrentFrameIndex((prev) => prev + 1);
    showToast('Created new blank keyframe!');
  };

  const handleDuplicateFrame = () => {
    setFrames((prev) => {
      const copy = [...prev];
      const active = copy[currentFrameIndex];
      const dup = {
        id: Date.now(),
        drawingPaths: JSON.parse(JSON.stringify(active.drawingPaths)),
        backgroundPaths: JSON.parse(JSON.stringify(active.backgroundPaths))
      };
      copy.splice(currentFrameIndex + 1, 0, dup);
      return copy;
    });
    setCurrentFrameIndex((prev) => prev + 1);
    showToast('Successfully duplicated keyframe!');
  };

  const handleDeleteCurrentFrame = () => {
    if (frames.length <= 1) return;
    setFrames((prev) => {
      const copy = prev.filter((_, i) => i !== currentFrameIndex);
      return copy;
    });
    setCurrentFrameIndex((prev) => Math.max(0, prev - 1));
    showToast('Deleted active frame.');
  };

  const handleContextMenuAction = (action) => {
    if (action === 'clear') {
      setFrames(prev => {
        const copy = [...prev];
        const active = copy[currentFrameIndex];
        if (activeLayer === 'main') active.drawingPaths = [];
        else active.backgroundPaths = [];
        return copy;
      });
      showToast('Cleared active layer elements.');
    } else if (action === 'duplicate') {
      handleDuplicateFrame();
    } else if (action === 'add') {
      handleAddBlankFrame();
    } else if (action === 'onion') {
      setOnionSkinEnabled(!onionSkinEnabled);
      showToast(`Onion skin set to: ${!onionSkinEnabled}`);
    } else if (action === 'reset') {
      setZoomScale(1.0);
      setPanX(0);
      setPanY(0);
      showToast('Reset drawing viewport!');
    }
  };

  const undoLastStroke = () => {
    setFrames(prev => {
      const copy = [...prev];
      const active = copy[currentFrameIndex];
      if (activeLayer === 'main') {
        if (active.drawingPaths.length > 0) active.drawingPaths.pop();
      } else {
        if (active.backgroundPaths.length > 0) active.backgroundPaths.pop();
      }
      return copy;
    });
    showToast('Undo last drawing stroke');
  };

  return (
    <div className={`h-screen w-screen flex flex-col overflow-hidden text-theme-text ${theme === 'matrix' ? 'matrix-theme' : theme === 'vaporwave' ? 'vaporwave-theme' : ''}`}>
      {/* Dynamic Toast banner overlay */}
      {toast.visible && (
        <div className="fixed top-18 right-6 bg-[#161824f0] backdrop-blur-md border-2 border-theme-primary text-theme-text rounded-xl px-5 py-3 text-xs font-bold shadow-2xl z-[9999] flex items-center gap-2 animate-bounce">
          <Sparkles className="w-4 h-4 text-theme-secondary animate-spin" />
          <span>{toast.message}</span>
        </div>
      )}

      {/* Main Suite Top Bar Header */}
      <header className="h-14 bg-theme-surface border-b border-theme-border flex justify-between items-center px-4 shrink-0 z-50 shadow-md">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-theme-primary flex items-center justify-center text-black font-extrabold shadow-[0_0_10px_rgba(189,147,249,0.35)] select-none animate-pulse text-lg">
            C
          </div>
          <div className="flex flex-col">
            <h1 className="text-sm font-extrabold tracking-tight font-sans text-theme-text">
              CREATOR STUDIO PRO
            </h1>
            <span className="text-[10px] font-bold text-theme-secondary font-mono tracking-wider">
              VITE ANIMATION WEB SUITE
            </span>
          </div>
        </div>

        {/* Core Actions, theme switcher & indicators */}
        <div className="flex items-center gap-3">
          {/* Pressure indicator */}
          <div className="hidden sm:flex items-center gap-1.5 px-3 py-1 bg-black/30 border border-theme-border rounded-lg text-[10px] font-mono text-theme-textSecondary">
            <span className="w-1.5 h-1.5 rounded-full bg-theme-secondary animate-ping" />
            <span>{hudPressure}</span>
          </div>

          <div className="h-4 w-px bg-theme-border" />

          {/* Theme switcher */}
          <div className="flex bg-black/35 rounded-lg border border-theme-border p-0.5">
            <button
              onClick={() => { setTheme('dracula'); showToast('Loaded Dracula Theme'); }}
              className={`px-2.5 py-1 text-[10px] font-bold rounded-md transition-all ${
                theme === 'dracula' ? 'bg-[#bd93f9] text-[#000]' : 'text-theme-textSecondary hover:text-theme-text'
              }`}
            >
              Dracula
            </button>
            <button
              onClick={() => { setTheme('matrix'); showToast('Loaded Matrix Green Theme'); }}
              className={`px-2.5 py-1 text-[10px] font-bold rounded-md transition-all ${
                theme === 'matrix' ? 'bg-[#00ff41] text-[#000]' : 'text-theme-textSecondary hover:text-theme-text'
              }`}
            >
              Matrix
            </button>
            <button
              onClick={() => { setTheme('vaporwave'); showToast('Loaded Retro Vaporwave'); }}
              className={`px-2.5 py-1 text-[10px] font-bold rounded-md transition-all ${
                theme === 'vaporwave' ? 'bg-[#ff007f] text-[#000]' : 'text-theme-textSecondary hover:text-theme-text'
              }`}
            >
              Vapor
            </button>
          </div>

          <div className="h-4 w-px bg-theme-border" />

          {/* Undo Action */}
          <button
            onClick={undoLastStroke}
            className="p-1.5 rounded-lg border border-theme-border hover:bg-theme-hover text-theme-text hover:text-theme-primary transition-all"
            title="Undo stroke (Ctrl+Z)"
          >
            <Undo2 className="w-4 h-4" />
          </button>
        </div>
      </header>

      {/* Main Studio Suite Workspace split */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Sidebar and tool panels */}
        <SidebarLeft
          activeTab={activeTab}
          setActiveTab={setActiveTab}
          currentTool={currentTool}
          setCurrentTool={setCurrentTool}
          setBrushColor={setBrushColor}
          brushColor={brushColor}
          lineStabilizerValue={lineStabilizerValue}
          setLineStabilizerValue={setLineStabilizerValue}
          injectPoseTemplate={injectPoseTemplate}
          storyboard={storyboard}
          setStoryboard={setStoryboard}
          triggerAIColoring={triggerAIColoring}
          triggerAIBackground={triggerAIBackground}
          triggerAIInterpolation={triggerAIInterpolation}
          bgPrompt={bgPrompt}
          setBgPrompt={setBgPrompt}
          customCode={customCode}
          setCustomCode={setCustomCode}
          customCodeType={customCodeType}
          setCustomCodeType={setCustomCodeType}
          applyInjectedCustomCode={applyInjectedCustomCode}
          isColoringLoading={isColoringLoading}
          isBgLoading={isBgLoading}
          isInterpolationLoading={isInterpolationLoading}
        />

        {/* Central interactive vector viewport canvas drawing board */}
        <DrawingBoard
          frames={frames}
          currentFrameIndex={currentFrameIndex}
          activeLayer={activeLayer}
          currentTool={currentTool}
          brushSize={brushSize}
          brushColor={brushColor}
          brushOpacity={brushOpacity}
          lineStabilizerValue={lineStabilizerValue}
          onionSkinEnabled={onionSkinEnabled}
          canvasGridEnabled={canvasGridEnabled}
          zoomScale={zoomScale}
          setZoomScale={setZoomScale}
          panX={panX}
          setPanX={setPanX}
          panY={panY}
          setPanY={setPanY}
          setFrames={setFrames}
          setHudPressure={setHudPressure}
          mainLayerVisible={mainLayerVisible}
          backgroundLayerVisible={backgroundLayerVisible}
          showToast={showToast}
          onContextMenuAction={handleContextMenuAction}
        />

        {/* Right Sidebar and brush dynamics / layer manager */}
        <SidebarRight
          brushSize={brushSize}
          setBrushSize={setBrushSize}
          brushOpacity={brushOpacity}
          setBrushOpacity={setBrushOpacity}
          brushColor={brushColor}
          setBrushColor={setBrushColor}
          activeLayer={activeLayer}
          setActiveLayer={setActiveLayer}
          mainLayerVisible={mainLayerVisible}
          setMainLayerVisible={setMainLayerVisible}
          backgroundLayerVisible={backgroundLayerVisible}
          setBackgroundLayerVisible={setBackgroundLayerVisible}
          layerOpacity={layerOpacity}
          setLayerOpacity={setLayerOpacity}
          onionSkinEnabled={onionSkinEnabled}
          setOnionSkinEnabled={setOnionSkinEnabled}
          canvasGridEnabled={canvasGridEnabled}
          setCanvasGridEnabled={setCanvasGridEnabled}
        />
      </div>

      {/* Interactive Timeline Bar */}
      <Timeline
        frames={frames}
        currentFrameIndex={currentFrameIndex}
        setCurrentFrameIndex={setCurrentFrameIndex}
        isPlaying={isPlaying}
        setIsPlaying={setIsPlaying}
        animationFPS={animationFPS}
        setAnimationFPS={setAnimationFPS}
        handleAddBlankFrame={handleAddBlankFrame}
        handleDuplicateFrame={handleDuplicateFrame}
        handleDeleteCurrentFrame={handleDeleteCurrentFrame}
      />
    </div>
  );
}
