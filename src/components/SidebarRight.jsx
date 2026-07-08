import React from 'react';
import { Eye, EyeOff, Layers, Settings, Palette } from 'lucide-react';

export default function SidebarRight({
  brushSize,
  setBrushSize,
  brushOpacity,
  setBrushOpacity,
  brushColor,
  setBrushColor,
  activeLayer,
  setActiveLayer,
  mainLayerVisible,
  setMainLayerVisible,
  backgroundLayerVisible,
  setBackgroundLayerVisible,
  layerOpacity,
  setLayerOpacity,
  onionSkinEnabled,
  setOnionSkinEnabled,
  canvasGridEnabled,
  setCanvasGridEnabled,
}) {
  return (
    <aside className="w-64 bg-theme-surface border-l border-theme-border flex flex-col p-4 gap-5 overflow-y-auto z-50 select-none">
      
      {/* SECTION 1: BRUSH PROPERTIES */}
      <div>
        <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3 flex items-center gap-1.5">
          <Settings className="w-3.5 h-3.5" />
          <span>BRUSH DYNAMICS</span>
        </div>

        <div className="flex flex-col gap-3">
          {/* Brush Size */}
          <div className="flex flex-col gap-1">
            <div className="flex justify-between text-[11px] font-bold text-theme-text">
              <span>Brush Size</span>
              <span className="text-theme-secondary">{brushSize}px</span>
            </div>
            <input
              type="range"
              min="2"
              max="100"
              value={brushSize}
              onChange={(e) => setBrushSize(parseInt(e.target.value))}
              className="w-full accent-theme-primary cursor-pointer"
            />
          </div>

          {/* Brush Opacity */}
          <div className="flex flex-col gap-1">
            <div className="flex justify-between text-[11px] font-bold text-theme-text">
              <span>Brush Opacity</span>
              <span className="text-theme-secondary">{Math.round(brushOpacity * 100)}%</span>
            </div>
            <input
              type="range"
              min="10"
              max="100"
              value={brushOpacity * 100}
              onChange={(e) => setBrushOpacity(parseFloat(e.target.value) / 100)}
              className="w-full accent-theme-primary cursor-pointer"
            />
          </div>
        </div>
      </div>

      <div className="border-t border-theme-border"></div>

      {/* SECTION 2: MANUAL COLOR CONTROL */}
      <div>
        <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3 flex items-center gap-1.5">
          <Palette className="w-3.5 h-3.5" />
          <span>HEX COLOR PICKER</span>
        </div>
        <div className="flex gap-2">
          <div className="relative w-8 h-8 rounded-lg overflow-hidden border border-theme-border">
            <input
              type="color"
              value={brushColor}
              onChange={(e) => setBrushColor(e.target.value)}
              className="absolute -top-1 -left-1 w-10 h-10 border-none bg-none cursor-pointer p-0"
            />
          </div>
          <input
            type="text"
            value={brushColor}
            onChange={(e) => {
              if (e.target.value.startsWith('#') && e.target.value.length <= 7) {
                setBrushColor(e.target.value);
              }
            }}
            className="flex-1 bg-black/20 border border-theme-border rounded-lg text-xs px-3 text-theme-text font-mono outline-none focus:border-theme-primary"
          />
        </div>
      </div>

      <div className="border-t border-theme-border"></div>

      {/* SECTION 3: LAYERS SUITE */}
      <div>
        <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3 flex items-center gap-1.5">
          <Layers className="w-3.5 h-3.5" />
          <span>LAYERS SUITE</span>
        </div>

        <div className="flex flex-col gap-2">
          {/* Main Canvas Layer Row */}
          <div
            onClick={() => setActiveLayer('main')}
            className={`flex items-center justify-between p-2.5 rounded-lg border cursor-pointer transition-all duration-150 ${
              activeLayer === 'main'
                ? 'bg-[#bd93f910] border-theme-primary'
                : 'bg-[#ffffff02] border-theme-border hover:bg-theme-hover'
            }`}
          >
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-theme-text">Main Canvas</span>
            </div>
            <button
              onClick={(e) => {
                e.stopPropagation();
                setMainLayerVisible(!mainLayerVisible);
              }}
              className="p-1 hover:bg-black/20 rounded text-theme-textSecondary hover:text-theme-primary"
            >
              {mainLayerVisible ? <Eye className="w-3.5 h-3.5" /> : <EyeOff className="w-3.5 h-3.5" />}
            </button>
          </div>

          {/* Background FX Layer Row */}
          <div
            onClick={() => setActiveLayer('background')}
            className={`flex items-center justify-between p-2.5 rounded-lg border cursor-pointer transition-all duration-150 ${
              activeLayer === 'background'
                ? 'bg-[#bd93f910] border-theme-primary'
                : 'bg-[#ffffff02] border-theme-border hover:bg-theme-hover'
            }`}
          >
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-theme-text">Background FX</span>
            </div>
            <button
              onClick={(e) => {
                e.stopPropagation();
                setBackgroundLayerVisible(!backgroundLayerVisible);
              }}
              className="p-1 hover:bg-black/20 rounded text-theme-textSecondary hover:text-theme-primary"
            >
              {backgroundLayerVisible ? <Eye className="w-3.5 h-3.5" /> : <EyeOff className="w-3.5 h-3.5" />}
            </button>
          </div>
        </div>

        {/* Layer Opacity */}
        <div className="flex flex-col gap-1 mt-3">
          <div className="flex justify-between text-[11px] font-bold text-theme-text">
            <span>Layer Opacity</span>
            <span className="text-theme-secondary">{layerOpacity}%</span>
          </div>
          <input
            type="range"
            min="0"
            max="100"
            value={layerOpacity}
            onChange={(e) => setLayerOpacity(parseInt(e.target.value))}
            className="w-full accent-theme-primary cursor-pointer"
          />
        </div>
      </div>

      <div className="border-t border-theme-border"></div>

      {/* SECTION 4: OVERLAYS */}
      <div>
        <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3">
          WORKSPACE OVERLAYS
        </div>
        <div className="flex flex-col gap-2">
          <label className="flex items-center gap-2 text-xs font-semibold text-theme-text cursor-pointer">
            <input
              type="checkbox"
              checked={onionSkinEnabled}
              onChange={(e) => setOnionSkinEnabled(e.target.checked)}
              className="accent-theme-primary rounded cursor-pointer w-3.5 h-3.5"
            />
            <span>Onion skin enabled</span>
          </label>

          <label className="flex items-center gap-2 text-xs font-semibold text-theme-text cursor-pointer">
            <input
              type="checkbox"
              checked={canvasGridEnabled}
              onChange={(e) => setCanvasGridEnabled(e.target.checked)}
              className="accent-theme-primary rounded cursor-pointer w-3.5 h-3.5"
            />
            <span>Show Canvas grid</span>
          </label>
        </div>
      </div>

    </aside>
  );
}
