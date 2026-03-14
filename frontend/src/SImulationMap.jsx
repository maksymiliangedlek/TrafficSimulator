import React, { useRef, useEffect, useState } from 'react';

const SimulationMap = ({ cars, nodes, roads }) => {
  const canvasRef = useRef(null);
  const [imageLoaded, setImageLoaded] = useState(false);
  const carImgRef = useRef(null);

  useEffect(() => {
    const img = new Image();
    img.src = '/car.png'; 
    
    img.onload = () => {
      carImgRef.current = img; 
      setImageLoaded(true);    
    };
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#2c2f33';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.strokeStyle = '#555555'; 
    ctx.lineWidth = 16; 
    ctx.lineCap = 'round';
    roads.forEach(road => {
        const fromNode = nodes.find(node => node.id === road.from.id);
        const toNode = nodes.find(node => node.id === road.to.id);
        
        if (fromNode && toNode) {
          ctx.beginPath();
          ctx.moveTo(fromNode.x, fromNode.y);
          ctx.lineTo(toNode.x, toNode.y);
          ctx.stroke();
          ctx.closePath();
        }
    });

    nodes.forEach(node => {
        ctx.beginPath();
        ctx.arc(node.x, node.y, 8, 0, 2 * Math.PI); 
        ctx.fillStyle = '#ffcc00'; 
        ctx.fill();
        ctx.closePath();
    });

    cars.forEach(car => {
      if (imageLoaded && carImgRef.current) {
        const width = 30;  
        const height = 16; 
      
        ctx.drawImage(
          carImgRef.current,
          car.x - width / 2,
          car.y - height / 2,
          width,
          height
        );
      } else {
        ctx.beginPath();
        ctx.arc(car.x, car.y, 6, 0, 2 * Math.PI);
        ctx.fillStyle = '#00ffcc';
        ctx.fill();
        ctx.closePath();
      }
    });

  }, [cars, nodes, roads, imageLoaded]); 

  return (
    <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20px' }}>
      <canvas 
        ref={canvasRef} 
        width={1000} 
        height={1000} 
        style={{ borderRadius: '8px', boxShadow: '0 4px 8px rgba(0,0,0,0.5)' }}
      />
    </div>
  );
};

export default SimulationMap;