import React, { useRef, useEffect, useState } from 'react';

export function drawPathImmediate(ctx, path) {
  if (!path || !path.points || path.points.length < 1) return;
  ctx.save();
  ctx.lineJoin = 'round';
  ctx.lineCap = 'round';

  const opacityHex = Math.round((path.opacity || 1.0) * 255).toString(16).padStart(2, '0');

  if (path.tool === 'eraser') {
    ctx.globalCompositeOperation = 'destination-out';
    ctx.lineWidth = path.size;
    ctx.strokeStyle = 'rgba(0,0,0,1)';
  } else {
    ctx.globalCompositeOperation = 'source-over';
    ctx.lineWidth = path.size;
    ctx.strokeStyle = path.color + opacityHex;

    if (path.tool === 'neon') {
      ctx.shadowBlur = path.size * 0.7;
      ctx.shadowColor = path.color;
    }
    if (path.tool === 'watercolor') {
      ctx.shadowBlur = path.size * 0.4;
      ctx.shadowColor = path.color;
      ctx.strokeStyle = path.color + '22'; 
    }
    if (path.tool === 'pencil') {
      ctx.strokeStyle = path.color + '99';
      ctx.setLineDash([2, 4]);
    }
    if (path.tool === 'rainbow') {
      ctx.strokeStyle = `hsl(${(Date.now() / 10) % 360}, 90%, 65%)`;
    }
  }

  if (path.tool === 'spray') {
    ctx.fillStyle = path.color + opacityHex;
    const lastPoint = path.points[path.points.length - 1];
    if (lastPoint) {
      for (let i = 0; i < 15; i++) {
        const angle = Math.random() * Math.PI * 2;
        const radius = Math.random() * path.size;
        const sx = lastPoint.x + Math.cos(angle) * radius;
        const sy = lastPoint.y + Math.sin(angle) * radius;
        ctx.beginPath();
        ctx.arc(sx, sy, 1 + Math.random() * 1.5, 0, Math.PI * 2);
        ctx.fill();
      }
    }
  } else if (path.tool === 'multi') {
    const offsets = [-8, 0, 8];
    offsets.forEach(offset => {
      ctx.beginPath();
      ctx.moveTo(path.points[0].x + offset, path.points[0].y + offset);
      for (let i = 1; i < path.points.length; i++) {
        ctx.lineTo(path.points[i].x + offset, path.points[i].y + offset);
      }
      ctx.stroke();
    });
  } else {
    ctx.beginPath();
    ctx.moveTo(path.points[0].x, path.points[0].y);
    for (let i = 1; i < path.points.length; i++) {
      ctx.lineTo(path.points[i].x, path.points[i].y);
    }
    ctx.stroke();
  }
  ctx.restore();
}

export default function DrawingBoard({
  frames,
  currentFrameIndex,
  activeLayer,
  currentTool,
  brushSize,
  brushColor,
  brushOpacity,
  lineStabilizerValue,
  onionSkinEnabled,
  canvasGridEnabled,
  zoomScale,
  setZoomScale,
  panX,
  setPanX,
  panY,
  setPanY,
  setFrames,
  setHudPressure,
  mainLayerVisible,
  backgroundLayerVisible,
  showToast,
  onContextMenuAction,
}) {
  const containerRef = useRef(null);
  const drawingCanvasRef = useRef(null);
  const onionCanvasRef = useRef(null);
  const wrapperRef = useRef(null);

  const [isDrawing, setIsDrawing] = useState(false);
  const [isPanning, setIsPanning] = useState(false);
  const [startPanX, setStartPanX] = useState(0);
  const [startPanY, setStartPanY] = useState(0);
  const [pointerPoints, setPointerPoints] = useState([]);
  const [contextMenu, setContextMenu] = useState({ visible: false, x: 0, y: 0 });

  const canvasWidth = 600;
  const canvasHeight = 450;

  // Viewport mapping coordinates helper
  const getCanvasCoords = (clientX, clientY) => {
    if (!drawingCanvasRef.current) return { x: 0, y: 0 };
    const rect = drawingCanvasRef.current.getBoundingClientRect();
    const x = (clientX - rect.left) * (canvasWidth / rect.width);
    const y = (clientY - rect.top) * (canvasHeight / rect.height);
    return { x, y };
  };

  // Keyboard and Wheel events
  useEffect(() => {
    const handleWheel = (e) => {
      e.preventDefault();
      const zoomFactor = 1.1;
      if (e.deltaY < 0) {
        setZoomScale(prev => Math.min(64.0, prev * zoomFactor));
      } else {
        setZoomScale(prev => Math.max(0.1, prev / zoomFactor));
      }
    };

    const handleKeyDown = (e) => {
      if (e.code === 'Space') {
        setIsPanning(true);
        if (wrapperRef.current) wrapperRef.current.style.cursor = 'grab';
      }
    };

    const handleKeyUp = (e) => {
      if (e.code === 'Space') {
        setIsPanning(false);
        if (wrapperRef.current) wrapperRef.current.style.cursor = 'default';
      }
    };

    const wrapper = wrapperRef.current;
    if (wrapper) {
      wrapper.addEventListener('wheel', handleWheel, { passive: false });
    }
    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);

    return () => {
      if (wrapper) {
        wrapper.removeEventListener('wheel', handleWheel);
      }
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, [setZoomScale]);

  // Pointer drawing loop
  const handlePointerDown = (e) => {
    if (e.button === 2) {
      // Right-click
      e.preventDefault();
      setContextMenu({
        visible: true,
        x: e.clientX,
        y: e.clientY
      });
      return;
    }
    setContextMenu({ visible: false, x: 0, y: 0 });

    if (isPanning || e.button === 1) {
      setIsPanning(true);
      setStartPanX(e.clientX - panX);
      setStartPanY(e.clientY - panY);
      return;
    }

    setIsDrawing(true);
    const pt = getCanvasCoords(e.clientX, e.clientY);
    const initialPoints = [pt];
    setPointerPoints(initialPoints);

    if (e.pointerType === 'pen') {
      setHudPressure(`Pressure: ${Math.round(e.pressure * 100)}%`);
    } else {
      setHudPressure('Stylus Off');
    }
  };

  const handlePointerMove = (e) => {
    if (isPanning) {
      setPanX(e.clientX - startPanX);
      setPanY(e.clientY - startPanY);
      return;
    }

    if (!isDrawing) return;

    const rawPt = getCanvasCoords(e.clientX, e.clientY);
    const lastPt = pointerPoints[pointerPoints.length - 1];
    
    let smoothedPt = rawPt;
    if (lastPt) {
      const weight = 1 / (lineStabilizerValue + 1);
      smoothedPt = {
        x: lastPt.x + (rawPt.x - lastPt.x) * weight,
        y: lastPt.y + (rawPt.y - lastPt.y) * weight
      };
    }

    const nextPoints = [...pointerPoints, smoothedPt];
    setPointerPoints(nextPoints);

    if (e.pointerType === 'pen') {
      setHudPressure(`Pressure: ${Math.round(e.pressure * 100)}%`);
    }

    // Render workspace frame + live stroke
    renderWorkspace(nextPoints);
  };

  const handlePointerUp = () => {
    if (isPanning) {
      setIsPanning(false);
      return;
    }
    if (!isDrawing) return;
    setIsDrawing(false);

    if (pointerPoints.length > 0) {
      // Save path
      const nextStroke = {
        tool: currentTool,
        size: brushSize,
        color: brushColor,
        opacity: brushOpacity,
        points: pointerPoints,
      };

      setFrames(prev => {
        const copy = [...prev];
        const activeFrame = copy[currentFrameIndex];
        if (activeLayer === 'main') {
          activeFrame.drawingPaths = [...activeFrame.drawingPaths, nextStroke];
        } else {
          activeFrame.backgroundPaths = [...activeFrame.backgroundPaths, nextStroke];
        }
        return copy;
      });
    }

    setPointerPoints([]);
  };

  // Main Canvas Render loop
  const renderWorkspace = (livePoints = null) => {
    if (!drawingCanvasRef.current || !onionCanvasRef.current) return;
    const dCtx = drawingCanvasRef.current.getContext('2d');
    const oCtx = onionCanvasRef.current.getContext('2d');

    // Clear main canvas
    dCtx.clearRect(0, 0, canvasWidth, canvasHeight);

    const activeFrame = frames[currentFrameIndex];
    if (!activeFrame) return;

    // Draw active background layers
    if (backgroundLayerVisible && activeFrame.backgroundPaths) {
      activeFrame.backgroundPaths.forEach(path => drawPathImmediate(dCtx, path));
    }

    // Draw active main layers
    if (mainLayerVisible && activeFrame.drawingPaths) {
      activeFrame.drawingPaths.forEach(path => drawPathImmediate(dCtx, path));
    }

    // Draw live stroke under cursor
    if (livePoints && livePoints.length > 0) {
      const livePath = {
        tool: currentTool,
        size: brushSize,
        color: brushColor,
        opacity: brushOpacity,
        points: livePoints,
      };
      drawPathImmediate(dCtx, livePath);
    }

    // Draw onion skins
    oCtx.clearRect(0, 0, canvasWidth, canvasHeight);
    if (onionSkinEnabled) {
      // Prev Frame
      if (currentFrameIndex > 0) {
        const prevFrame = frames[currentFrameIndex - 1];
        oCtx.save();
        oCtx.globalAlpha = 0.35;
        if (prevFrame.backgroundPaths) prevFrame.backgroundPaths.forEach(p => drawPathImmediate(oCtx, p));
        if (prevFrame.drawingPaths) prevFrame.drawingPaths.forEach(p => drawPathImmediate(oCtx, p));
        oCtx.restore();
      }
      // Next Frame
      if (currentFrameIndex < frames.length - 1) {
        const nextFrame = frames[currentFrameIndex + 1];
        oCtx.save();
        oCtx.globalAlpha = 0.2;
        if (nextFrame.backgroundPaths) nextFrame.backgroundPaths.forEach(p => drawPathImmediate(oCtx, p));
        if (nextFrame.drawingPaths) nextFrame.drawingPaths.forEach(p => drawPathImmediate(oCtx, p));
        oCtx.restore();
      }
    }
  };

  // Re-run rendering whenever states change
  useEffect(() => {
    renderWorkspace();
  }, [frames, currentFrameIndex, onionSkinEnabled, mainLayerVisible, backgroundLayerVisible]);

  // Context Menu Action Handler
  const handleContextMenuAction = (action) => {
    setContextMenu({ visible: false, x: 0, y: 0 });
    onContextMenuAction(action);
  };

  return (
    <div
      ref={wrapperRef}
      className="flex-1 flex flex-col justify-center items-center relative overflow-hidden bg-theme-bg p-4"
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onContextMenu={e => e.preventDefault()}
    >
      {/* Canvas Position & Zoom Indicators */}
      <div className="absolute top-4 left-4 bg-[#0d0e11e0] backdrop-blur-md border border-theme-border rounded-lg px-4 py-2 text-[10px] font-mono flex gap-4 text-theme-text pointer-events-none z-50 shadow-md">
        <span>Zoom: {Math.round(zoomScale * 100)}%</span>
        <span>Pan: ({Math.round(panX)}px, {Math.round(panY)}px)</span>
        <span>Frame: {currentFrameIndex + 1}/{frames.length}</span>
      </div>

      {/* Styled Canvas Stage container with Dynamic Panning & Zooming */}
      <div
        ref={containerRef}
        id="studio-canvas-container"
        className={`relative shadow-2xl rounded-lg overflow-visible border-2 border-theme-border origin-center transition-transform duration-75 select-none ${canvasGridEnabled ? 'transparent-checkerboard' : 'bg-[#12131a]'}`}
        style={{
          width: `${canvasWidth}px`,
          height: `${canvasHeight}px`,
          transform: `translate(${panX}px, ${panY}px) scale(${zoomScale})`,
          cursor: isPanning ? 'grab' : 'crosshair'
        }}
      >
        <canvas
          ref={onionCanvasRef}
          width={canvasWidth}
          height={canvasHeight}
          className="absolute top-0 left-0 pointer-events-none"
        />
        <canvas
          ref={drawingCanvasRef}
          width={canvasWidth}
          height={canvasHeight}
          className="absolute top-0 left-0"
        />
      </div>

      {/* Right-click Context Menu */}
      {contextMenu.visible && (
        <div
          className="absolute bg-theme-surface border border-theme-border rounded-lg shadow-2xl py-1 w-44 z-[1000] text-theme-text"
          style={{ top: `${contextMenu.y}px`, left: `${contextMenu.x}px` }}
        >
          <div className="px-3 py-2 text-[10px] text-theme-textSecondary font-bold border-b border-theme-border">
            FRAME OPTIONS
          </div>
          <button
            onClick={() => handleContextMenuAction('clear')}
            className="w-full text-left px-4 py-2 text-xs font-semibold hover:bg-theme-hover hover:text-theme-primary transition-colors"
          >
            🧹 Clear Frame
          </button>
          <button
            onClick={() => handleContextMenuAction('duplicate')}
            className="w-full text-left px-4 py-2 text-xs font-semibold hover:bg-theme-hover hover:text-theme-primary transition-colors"
          >
            📋 Duplicate Frame
          </button>
          <button
            onClick={() => handleContextMenuAction('add')}
            className="w-full text-left px-4 py-2 text-xs font-semibold hover:bg-theme-hover hover:text-theme-primary transition-colors"
          >
            ➕ Add Blank Frame
          </button>
          <button
            onClick={() => handleContextMenuAction('onion')}
            className="w-full text-left px-4 py-2 text-xs font-semibold hover:bg-theme-hover hover:text-theme-primary transition-colors"
          >
            🧅 Toggle Onion Skin
          </button>
          <button
            onClick={() => handleContextMenuAction('reset')}
            className="w-full text-left px-4 py-2 text-xs font-semibold hover:bg-theme-hover hover:text-theme-primary transition-colors"
          >
            🎯 Reset Viewport
          </button>
        </div>
      )}
    </div>
  );
}
