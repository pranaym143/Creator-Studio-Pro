import React from 'react';
import { Play, Square, SkipBack, SkipForward, Plus, Copy, Trash2 } from 'lucide-react';

export default function Timeline({
  frames,
  currentFrameIndex,
  setCurrentFrameIndex,
  isPlaying,
  setIsPlaying,
  animationFPS,
  setAnimationFPS,
  handleAddBlankFrame,
  handleDuplicateFrame,
  handleDeleteCurrentFrame,
}) {

  const handlePrevFrame = () => {
    if (currentFrameIndex > 0) {
      setCurrentFrameIndex(currentFrameIndex - 1);
    } else {
      setCurrentFrameIndex(frames.length - 1);
    }
  };

  const handleNextFrame = () => {
    if (currentFrameIndex < frames.length - 1) {
      setCurrentFrameIndex(currentFrameIndex + 1);
    } else {
      setCurrentFrameIndex(0);
    }
  };

  return (
    <div className="bg-[#0b0c11] border-t border-theme-border flex flex-col p-4 gap-3 z-50 select-none">
      
      {/* Playback Controls & Frame Operations */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-4">
        {/* Playback row */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => setIsPlaying(!isPlaying)}
            className={`flex items-center gap-1.5 px-4 py-1.5 rounded-lg font-bold text-xs border transition-all duration-150 ${
              isPlaying
                ? 'bg-[#50fa7b15] border-theme-success text-theme-success hover:bg-theme-success hover:text-[#000]'
                : 'bg-[#ffffff02] border-theme-border text-theme-text hover:border-theme-success hover:text-theme-success'
            }`}
          >
            <Play className={`w-3.5 h-3.5 ${isPlaying ? 'fill-current animate-pulse' : ''}`} />
            <span>{isPlaying ? '⏸ Pause Loop' : '▶ Play Loop'}</span>
          </button>

          <button
            onClick={() => setIsPlaying(false)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-semibold text-xs border border-theme-border text-theme-text hover:bg-theme-hover transition-all"
            title="Stop playback"
          >
            <Square className="w-3.5 h-3.5" />
            <span>Stop</span>
          </button>

          <button
            onClick={handlePrevFrame}
            className="p-1.5 rounded-lg border border-theme-border text-theme-text hover:bg-theme-hover transition-all"
            title="Previous Frame"
          >
            <SkipBack className="w-3.5 h-3.5" />
          </button>

          <button
            onClick={handleNextFrame}
            className="p-1.5 rounded-lg border border-theme-border text-theme-text hover:bg-theme-hover transition-all"
            title="Next Frame"
          >
            <SkipForward className="w-3.5 h-3.5" />
          </button>

          <span className="text-[11px] font-bold text-theme-textSecondary ml-2">
            Speed: {animationFPS} FPS
          </span>
        </div>

        {/* Dynamic Controls Slider & Add Frame buttons */}
        <div className="flex items-center gap-3 w-full md:w-auto justify-end">
          <input
            type="range"
            min="1"
            max="30"
            value={animationFPS}
            onChange={(e) => setAnimationFPS(parseInt(e.target.value))}
            className="w-24 md:w-28 accent-theme-primary cursor-pointer"
            title="Adjust Animation FPS"
          />

          <div className="h-4 w-px bg-theme-border" />

          <button
            onClick={handleAddBlankFrame}
            className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-theme-primary text-[#000] font-bold text-xs hover:bg-theme-accent transition-all duration-150"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>+ Add Frame</span>
          </button>

          <button
            onClick={handleDuplicateFrame}
            className="flex items-center gap-1 px-3 py-1.5 rounded-lg border border-theme-border text-theme-text hover:bg-theme-hover font-semibold text-xs transition-all duration-150"
            title="Duplicate Frame"
          >
            <Copy className="w-3.5 h-3.5" />
            <span>Duplicate</span>
          </button>

          <button
            onClick={handleDeleteCurrentFrame}
            disabled={frames.length <= 1}
            className="flex items-center gap-1 px-3 py-1.5 rounded-lg border border-[#ff555540] text-theme-error hover:bg-theme-error hover:text-[#000] font-semibold text-xs transition-all duration-150 disabled:opacity-30 disabled:pointer-events-none"
            title="Delete current frame"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Delete</span>
          </button>
        </div>
      </div>

      {/* Frame Sequence Ribbon */}
      <div className="flex gap-2.5 overflow-x-auto py-2.5 border border-theme-border rounded-lg bg-[#07080c] px-3">
        {frames.map((frame, index) => {
          const mainPathsCount = frame.drawingPaths ? frame.drawingPaths.length : 0;
          const bgPathsCount = frame.backgroundPaths ? frame.backgroundPaths.length : 0;
          const totalPaths = mainPathsCount + bgPathsCount;

          return (
            <div
              key={frame.id}
              onClick={() => {
                setIsPlaying(false);
                setCurrentFrameIndex(index);
              }}
              className={`flex-shrink-0 w-20 h-16 rounded-lg border cursor-pointer flex flex-col justify-between p-2 relative overflow-hidden transition-all duration-150 ${
                index === currentFrameIndex
                  ? 'bg-[#bd93f90b] border-theme-primary shadow-[0_0_8px_rgba(189,147,249,0.25)]'
                  : 'bg-theme-surface border-theme-border hover:border-theme-secondary hover:bg-theme-hover'
              }`}
            >
              <div className="flex justify-between items-center z-10">
                <span className={`text-[10px] font-extrabold ${index === currentFrameIndex ? 'text-theme-primary' : 'text-theme-textSecondary'}`}>
                  #{index + 1}
                </span>
                <span className="text-[8px] px-1 bg-black/40 rounded text-theme-text font-semibold">
                  {totalPaths} path{totalPaths !== 1 && 's'}
                </span>
              </div>

              {/* Decorative mini canvas placeholder visualization */}
              <div className="absolute inset-x-0 bottom-0 h-8 bg-black/10 flex items-center justify-center border-t border-[#ffffff03] select-none">
                <div className="flex gap-0.5">
                  <div className={`w-1 h-3 rounded-full ${mainPathsCount > 0 ? 'bg-theme-primary' : 'bg-theme-border'}`} />
                  <div className={`w-1 h-3 rounded-full ${bgPathsCount > 0 ? 'bg-theme-secondary' : 'bg-theme-border'}`} />
                </div>
              </div>
            </div>
          );
        })}
      </div>

    </div>
  );
}
