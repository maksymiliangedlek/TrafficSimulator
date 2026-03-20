import React, { useRef, useEffect, useState } from 'react';

const SimulationMap = ({ cars, nodes, roads, onNodeClick, selectedStartNode, lights }) => {
  const canvasRef = useRef(null);
  const [imageLoaded, setImageLoaded] = useState(false);
  const carImgRef = useRef(null);
  const mapImgRef = useRef(null); 

  useEffect(() => {
    const carImg = new Image();
    carImg.src = '/car.png'; 
    carImg.onload = () => {
      carImgRef.current = carImg; 
      setImageLoaded(true);    
    };

    const mapImg = new Image();
    mapImg.src = '/krakow.png'; 
    mapImg.onload = () => {
        mapImgRef.current = mapImg;
    };
  }, []);

  const handleCanvasClick = (event) => {
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const clickX = (event.clientX - rect.left) * scaleX;
    const clickY = (event.clientY - rect.top) * scaleY;

    console.log(`Kliknięto w: x=${Math.round(clickX)}, y=${Math.round(clickY)}`); 

    const clickedNode = nodes.find(node => {
      const distance = Math.hypot(node.x - clickX, node.y - clickY);
      return distance <= 20; 
    });

    if (clickedNode && onNodeClick) {
      onNodeClick(clickedNode);
    }
  };

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#2c2f33';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    if (mapImgRef.current) {
        ctx.globalAlpha = 0.8; 
        ctx.drawImage(mapImgRef.current, 0, 0, canvas.width, canvas.height);
        ctx.globalAlpha = 1.0;
    }

    ctx.strokeStyle = '#555555'; 
    ctx.lineWidth = 12; 
    ctx.lineCap = 'round';
    
    roads.forEach(road => {
        const fromNode = nodes.find(node => node.id === road.from.id);
        const toNode = nodes.find(node => node.id === road.to.id);
        
        if (fromNode && toNode) {
          ctx.beginPath();
          ctx.moveTo(fromNode.x, fromNode.y);
          ctx.lineTo(toNode.x, toNode.y);
          ctx.stroke();

          const lightState = lights?.find(l => l.roadId === road.id);
          const isGreen = lightState ? lightState.isGreen : false;
          const color = isGreen ? '#2ecc71' : '#ff4757';

          const dx = toNode.x - fromNode.x;
          const dy = toNode.y - fromNode.y;
          const dist = Math.hypot(dx, dy);

          if (dist > 25) { 
            const lightX = toNode.x - (dx / dist) * 20;
            const lightY = toNode.y - (dy / dist) * 20;

            ctx.beginPath();
            ctx.arc(lightX, lightY, 6, 0, 2 * Math.PI); 
            ctx.fillStyle = color;
            
            ctx.shadowBlur = 10;
            ctx.shadowColor = color;
            ctx.fill();
            
            ctx.shadowBlur = 0;
            ctx.strokeStyle = '#1a1a1a';
            ctx.lineWidth = 2;
            ctx.stroke();
          }
        }
    });

    nodes.forEach(node => {
        ctx.beginPath();
        ctx.arc(node.x, node.y, 8, 0, 2 * Math.PI); 
        ctx.fillStyle = '#ffcc00'; 
        ctx.fill();
    });

    if (selectedStartNode) {
      ctx.beginPath();
      ctx.arc(selectedStartNode.x, selectedStartNode.y, 18, 0, 2 * Math.PI);
      ctx.strokeStyle = '#00ffcc'; 
      ctx.lineWidth = 4;
      ctx.stroke();
    }

    // 5. AUTA
    cars.forEach(car => {
      if (imageLoaded && carImgRef.current) {
        const width = 30;  
        const height = 16; 
        ctx.drawImage(carImgRef.current, car.x - width / 2, car.y - height / 2, width, height);
      } else {
        ctx.beginPath();
        ctx.arc(car.x, car.y, 6, 0, 2 * Math.PI);
        ctx.fillStyle = '#00ffcc';
        ctx.fill();
      }
    });

  }, [cars, nodes, roads, lights, imageLoaded, selectedStartNode]); 

  return (
    <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20px' }}>
      <canvas 
        ref={canvasRef} 
        width={1500} 
        height={1000} 
        onClick={handleCanvasClick} 
        style={{ 
          borderRadius: '8px', 
          boxShadow: '0 4px 8px rgba(0,0,0,0.5)',
          cursor: onNodeClick ? 'crosshair' : 'default', 
        }}
      />
    </div>
  );
};

export default SimulationMap;