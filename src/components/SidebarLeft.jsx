import React from 'react';
import { Paintbrush, LayoutGrid, Sparkles, Laptop, Plus, HelpCircle } from 'lucide-react';

export default function SidebarLeft({
  activeTab,
  setActiveTab,
  currentTool,
  setCurrentTool,
  setBrushColor,
  brushColor,
  lineStabilizerValue,
  setLineStabilizerValue,
  injectPoseTemplate,
  storyboard,
  setStoryboard,
  triggerAIColoring,
  triggerAIBackground,
  triggerAIInterpolation,
  bgPrompt,
  setBgPrompt,
  customCode,
  setCustomCode,
  customCodeType,
  setCustomCodeType,
  applyInjectedCustomCode,
  isColoringLoading,
  isBgLoading,
  isInterpolationLoading,
}) {
  
  const paletteColors = [
    '#bd93f9', '#8be9fd', '#ff79c6', '#50fa7b', '#f1fa8c', '#ffffff',
    '#ffb86c', '#ff5555', '#6272a4', '#f8f8f2', '#1a1c29', '#ff2a2a'
  ];

  const tools = [
    { id: 'brush', label: '🖊️ Solid Pen' },
    { id: 'pencil', label: '✏️ Charcoal' },
    { id: 'watercolor', label: '💧 Watercolor' },
    { id: 'neon', label: '✨ Neon Glow' },
    { id: 'rainbow', label: '🌈 Rainbow' },
    { id: 'spray', label: '💨 Spray Paint' },
    { id: 'multi', label: '🎗️ Parallel' },
    { id: 'eraser', label: '🧹 Eraser' },
  ];

  const handleAddStoryboardNode = () => {
    const nextId = storyboard.length + 1;
    setStoryboard([...storyboard, {
      id: nextId,
      title: `Scene ${nextId}: Focus Frame`,
      text: 'Describe the upcoming action, keyframes, or camera movements here...'
    }]);
  };

  const handleUpdateStoryboard = (id, field, value) => {
    setStoryboard(storyboard.map(item => item.id === id ? { ...item, [field]: value } : item));
  };

  const getStabilizerDesc = (val) => {
    if (val < 4) return 'Low';
    if (val > 10) return 'High Fluid';
    return 'Medium';
  };

  return (
    <div className="flex select-none">
      {/* Visual Navigation Icons */}
      <aside className="w-18 bg-theme-surface border-r border-theme-border flex flex-col items-center py-4 gap-4 z-50">
        <button
          onClick={() => setActiveTab('draw')}
          className={`w-12 h-12 rounded-xl flex flex-col justify-center items-center gap-1 text-[9px] font-bold transition-all duration-200 border ${
            activeTab === 'draw'
              ? 'bg-theme-primary text-[#000] border-theme-primary shadow-[0_0_12px_rgba(189,147,249,0.35)]'
              : 'text-theme-textSecondary border-transparent hover:text-theme-primary hover:bg-theme-hover'
          }`}
          title="Studio Drawing & Painting tools"
        >
          <Paintbrush className="w-5 h-5" />
          <span>Studio</span>
        </button>

        <button
          onClick={() => setActiveTab('creative')}
          className={`w-12 h-12 rounded-xl flex flex-col justify-center items-center gap-1 text-[9px] font-bold transition-all duration-200 border ${
            activeTab === 'creative'
              ? 'bg-theme-primary text-[#000] border-theme-primary shadow-[0_0_12px_rgba(189,147,249,0.35)]'
              : 'text-theme-textSecondary border-transparent hover:text-theme-primary hover:bg-theme-hover'
          }`}
          title="Storyboards, Poses & Assets"
        >
          <LayoutGrid className="w-5 h-5" />
          <span>Creative</span>
        </button>

        <button
          onClick={() => setActiveTab('ai')}
          className={`w-12 h-12 rounded-xl flex flex-col justify-center items-center gap-1 text-[9px] font-bold transition-all duration-200 border ${
            activeTab === 'ai'
              ? 'bg-theme-primary text-[#000] border-theme-primary shadow-[0_0_12px_rgba(189,147,249,0.35)]'
              : 'text-theme-textSecondary border-transparent hover:text-theme-primary hover:bg-theme-hover'
          }`}
          title="AI Assistant Shaders & Backdrops"
        >
          <Sparkles className="w-5 h-5" />
          <span>AI Art</span>
        </button>

        <button
          onClick={() => setActiveTab('pwa')}
          className={`w-12 h-12 rounded-xl flex flex-col justify-center items-center gap-1 text-[9px] font-bold transition-all duration-200 border ${
            activeTab === 'pwa'
              ? 'bg-theme-primary text-[#000] border-theme-primary shadow-[0_0_12px_rgba(189,147,249,0.35)]'
              : 'text-theme-textSecondary border-transparent hover:text-theme-primary hover:bg-theme-hover'
          }`}
          title="PWA Status & Live Code Injections"
        >
          <Laptop className="w-5 h-5" />
          <span>Publisher</span>
        </button>
      </aside>

      {/* Main Left Active Controls Panel */}
      <aside className="w-64 bg-theme-surface border-r border-theme-border flex flex-col p-4 gap-4 overflow-y-auto z-40">
        
        {/* TAB 1: STUDIO BRUSHES AND COLORS */}
        {activeTab === 'draw' && (
          <div className="flex flex-col gap-4">
            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3">
                DRAWING BRUSHES
              </div>
              <div className="grid grid-cols-2 gap-2">
                {tools.map((t) => (
                  <button
                    key={t.id}
                    onClick={() => setCurrentTool(t.id)}
                    className={`flex flex-col items-center justify-center p-2 rounded-lg border text-xs font-semibold transition-all duration-150 ${
                      currentTool === t.id
                        ? 'bg-[#bd93f915] border-theme-primary text-theme-primary'
                        : 'bg-[#ffffff02] border-theme-border text-theme-text hover:bg-theme-hover'
                    }`}
                  >
                    <span>{t.label}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="border-t border-theme-border my-1"></div>

            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3">
                COLOR PALETTE
              </div>
              <div className="grid grid-cols-6 gap-2">
                {paletteColors.map((color) => (
                  <button
                    key={color}
                    onClick={() => setBrushColor(color)}
                    style={{ backgroundColor: color }}
                    className={`w-8 h-8 rounded-full border-2 transition-transform duration-100 ${
                      brushColor.toLowerCase() === color.toLowerCase()
                        ? 'border-theme-text scale-110 shadow-lg'
                        : 'border-transparent hover:scale-110'
                    }`}
                  />
                ))}
              </div>
            </div>

            <div className="border-t border-theme-border my-1"></div>

            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-2">
                LINE STABILIZER
              </div>
              <div className="flex flex-col gap-1.5">
                <div className="flex justify-between text-[11px] font-bold text-theme-text">
                  <span>Smoothness factor</span>
                  <span className="text-theme-secondary">
                    {lineStabilizerValue} ({getStabilizerDesc(lineStabilizerValue)})
                  </span>
                </div>
                <input
                  type="range"
                  min="1"
                  max="15"
                  value={lineStabilizerValue}
                  onChange={(e) => setLineStabilizerValue(parseInt(e.target.value))}
                  className="w-full accent-theme-primary cursor-pointer"
                />
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: CREATIVE POSE LIBRARY & STORYBOARD */}
        {activeTab === 'creative' && (
          <div className="flex flex-col gap-4">
            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3">
                POSE REFERENCE
              </div>
              <div className="flex flex-col gap-2">
                {['walking', 'running', 'jumping', 'standing'].map((pose) => (
                  <button
                    key={pose}
                    onClick={() => injectPoseTemplate(pose)}
                    className="flex justify-between items-center p-2.5 rounded-lg border border-theme-border bg-[#ffffff01] hover:border-theme-secondary hover:bg-[#8be9fd08] transition-all text-xs font-semibold text-theme-text"
                  >
                    <span className="capitalize">
                      {pose === 'walking' && '🚶 Active Walking Pose'}
                      {pose === 'running' && '🏃 Running Motion Dynamic'}
                      {pose === 'jumping' && '🤸 Jumping High Action'}
                      {pose === 'standing' && '🧍 Neutral Standing Anchor'}
                    </span>
                    <span className="text-[10px] text-theme-textSecondary uppercase font-extrabold">
                      Apply
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <div className="border-t border-theme-border my-1"></div>

            <div>
              <div className="flex justify-between items-center mb-3">
                <span className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase">
                  STORYBOARD LIST
                </span>
                <button
                  onClick={handleAddStoryboardNode}
                  className="p-1 hover:bg-theme-hover rounded border border-theme-border text-theme-primary"
                  title="Add Storyboard Frame"
                >
                  <Plus className="w-3.5 h-3.5" />
                </button>
              </div>

              <div className="flex flex-col gap-3">
                {storyboard.map((item) => (
                  <div
                    key={item.id}
                    className="p-2.5 rounded-lg border border-theme-border bg-[#ffffff01] flex flex-col gap-1.5"
                  >
                    <input
                      type="text"
                      value={item.title}
                      onChange={(e) => handleUpdateStoryboard(item.id, 'title', e.target.value)}
                      className="bg-transparent border-none text-xs font-bold text-theme-text outline-none p-0 focus:text-theme-primary"
                    />
                    <textarea
                      value={item.text}
                      onChange={(e) => handleUpdateStoryboard(item.id, 'text', e.target.value)}
                      className="bg-transparent border-none text-[10px] text-theme-textSecondary outline-none p-0 resize-none h-14"
                    />
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: AI COLORING & SHADING & GENERATORS */}
        {activeTab === 'ai' && (
          <div className="flex flex-col gap-4">
            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-2">
                AI COLORING & SHADING
              </div>
              <button
                onClick={triggerAIColoring}
                disabled={isColoringLoading}
                className="w-full flex justify-center items-center gap-2 py-2.5 px-4 rounded-lg bg-[#bd93f90e] border border-[#bd93f940] hover:bg-theme-primary hover:text-[#000] hover:border-theme-primary text-theme-primary font-bold text-xs transition-all duration-200 disabled:opacity-40"
              >
                {isColoringLoading ? (
                  <div className="w-3.5 h-3.5 border-2 border-theme-primary border-t-transparent rounded-full animate-spin" />
                ) : (
                  '✨ Auto Color Layer'
                )}
              </button>
              <div className="text-[9px] text-theme-textSecondary mt-2 leading-relaxed">
                Uses local canvas palette propagation algorithms to automatically paint and shade line drawings seamlessly.
              </div>
            </div>

            <div className="border-t border-theme-border my-1"></div>

            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-2">
                AI BACKDROP GENERATOR
              </div>
              <div className="flex flex-col gap-2">
                <input
                  type="text"
                  value={bgPrompt}
                  onChange={(e) => setBgPrompt(e.target.value)}
                  placeholder="Describe backdrop: 'aurora borealis'..."
                  className="bg-black/20 border border-theme-border text-xs rounded-lg px-3 py-2 text-theme-text outline-none focus:border-theme-primary"
                />
                <button
                  onClick={triggerAIBackground}
                  disabled={isBgLoading || !bgPrompt}
                  className="w-full flex justify-center items-center gap-2 py-2.5 px-4 rounded-lg bg-[#bd93f90e] border border-[#bd93f940] hover:bg-theme-primary hover:text-[#000] hover:border-theme-primary text-theme-primary font-bold text-xs transition-all duration-200 disabled:opacity-40"
                >
                  {isBgLoading ? (
                    <div className="w-3.5 h-3.5 border-2 border-theme-primary border-t-transparent rounded-full animate-spin" />
                  ) : (
                    '🌌 Generate Backdrop'
                  )}
                </button>
              </div>
            </div>

            <div className="border-t border-theme-border my-1"></div>

            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-2">
                AI FRAME IN-BETWEENS
              </div>
              <button
                onClick={triggerAIInterpolation}
                disabled={isInterpolationLoading}
                className="w-full flex justify-center items-center gap-2 py-2.5 px-4 rounded-lg bg-[#bd93f90e] border border-[#bd93f940] hover:bg-theme-primary hover:text-[#000] hover:border-theme-primary text-theme-primary font-bold text-xs transition-all duration-200 disabled:opacity-40"
              >
                {isInterpolationLoading ? (
                  <div className="w-3.5 h-3.5 border-2 border-theme-primary border-t-transparent rounded-full animate-spin" />
                ) : (
                  '⚡ Interpolate Transition'
                )}
              </button>
              <div className="text-[9px] text-theme-textSecondary mt-2 leading-relaxed">
                Generates smooth mathematical transition keyframes between frames to scale up active viewport motion fluidity.
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: PUBLISHER / PWA SETTINGS */}
        {activeTab === 'pwa' && (
          <div className="flex flex-col gap-4">
            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-3">
                PWA LIVE SETTINGS
              </div>
              <div className="border border-theme-border rounded-lg p-3 bg-black/10 flex flex-col gap-2">
                <span className="text-xs font-bold text-theme-secondary">
                  Offline Cache Status
                </span>
                <div className="flex items-center gap-2 text-[10px] text-theme-text font-semibold">
                  <span className="w-2 h-2 rounded-full bg-theme-success" />
                  <span>Active (Service Worker Enabled)</span>
                </div>
              </div>
            </div>

            <div className="border-t border-theme-border my-1"></div>

            <div>
              <div className="text-[11px] font-extrabold text-theme-primary tracking-widest uppercase mb-2">
                LIVE CODE INJECTION
              </div>
              <div className="flex flex-col gap-2">
                <select
                  value={customCodeType}
                  onChange={(e) => setCustomCodeType(e.target.value)}
                  className="bg-black/20 border border-theme-border text-xs rounded-lg px-3 py-2 text-theme-text outline-none focus:border-theme-primary cursor-pointer"
                >
                  <option value="css">CSS stylesheet</option>
                  <option value="js">JS script injector</option>
                </select>
                <textarea
                  value={customCode}
                  onChange={(e) => setCustomCode(e.target.value)}
                  placeholder={
                    customCodeType === 'css'
                      ? 'body { filter: hue-rotate(45deg); }'
                      : 'showToast("Dynamic JavaScript triggered!");'
                  }
                  className="w-full bg-[#0a0b10] border border-theme-border text-theme-success text-[10px] font-mono rounded-lg p-2.5 h-28 resize-none outline-none focus:border-theme-primary"
                />
                <button
                  onClick={applyInjectedCustomCode}
                  className="w-full py-2 px-4 rounded-lg bg-theme-primary text-[#000] font-bold text-xs hover:bg-theme-accent transition-all duration-200"
                >
                  Apply Injection
                </button>
              </div>
            </div>
          </div>
        )}

      </aside>
    </div>
  );
}
