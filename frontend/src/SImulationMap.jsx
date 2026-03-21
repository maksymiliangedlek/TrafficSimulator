import React, { useRef, useEffect, useState, useCallback } from 'react';
import { Map, useMap } from '@vis.gl/react-google-maps';

const CanvasOverlay = ({ cars, nodes, roads, lights, onNodeClick, selectedStartNode }) => {
    const map = useMap();
    const canvasRef = useRef(null);
    const [imageLoaded, setImageLoaded] = useState(false);
    const carImgRef = useRef(null);

    useEffect(() => {
        const carImg = new Image();
        carImg.src = '/car.png'; 
        carImg.onload = () => {
            carImgRef.current = carImg; 
            setImageLoaded(true);    
        };
    }, []);

    const getPixelXY = useCallback((lat, lng, projection) => {
        if (!projection) return null;
        const point = projection.fromLatLngToContainerPixel(new window.google.maps.LatLng(lat, lng));
        return point;
    }, []);

    useEffect(() => {
        if (!map) return;

        let animationFrameId;

        const renderFrame = () => {
            const canvas = canvasRef.current;
            if (!canvas) return;

            const ctx = canvas.getContext('2d');
            const projection = map.getProjection();
            
            const parent = canvas.parentElement;
            if (parent && (canvas.width !== parent.clientWidth || canvas.height !== parent.clientHeight)) {
               canvas.width = parent.clientWidth;
               canvas.height = parent.clientHeight;
            }

            const overlayProjection = map.__overlayProjection;

            ctx.clearRect(0, 0, canvas.width, canvas.height);

            if (!projection || !overlayProjection) {
                animationFrameId = requestAnimationFrame(renderFrame);
                return;
            }

            ctx.strokeStyle = '#333333'; 
            ctx.lineWidth = 10; 
            ctx.lineCap = 'round';
            
            roads.forEach(road => {
                const fromNode = nodes.find(node => node.id === road.from.id);
                const toNode = nodes.find(node => node.id === road.to.id);
                
                if (fromNode && toNode) {
                    const fromPt = getPixelXY(fromNode.lat, fromNode.lng, overlayProjection);
                    const toPt = getPixelXY(toNode.lat, toNode.lng, overlayProjection);
                    
                    if(fromPt && toPt) {
                      ctx.lineWidth = 2;     
                      ctx.strokeStyle = '#333333'; 
                      ctx.shadowBlur = 0;        
                      ctx.lineCap = 'round';
                        ctx.beginPath();
                
                        ctx.moveTo(fromPt.x, fromPt.y);
                        ctx.lineTo(toPt.x, toPt.y);
                        ctx.stroke();

                        const lightState = lights?.find(l => l.roadId === road.id);
                        const isGreen = lightState ? lightState.isGreen : false;
                        const color = isGreen ? '#2ecc71' : '#ff4757';

                        const dx = toPt.x - fromPt.x;
                        const dy = toPt.y - fromPt.y;
                        const dist = Math.hypot(dx, dy);

                        if (dist > 25) { 
                            const lightX = toPt.x - (dx / dist) * 15; 
                            const lightY = toPt.y - (dy / dist) * 15;

                            ctx.beginPath();
                            ctx.arc(lightX, lightY, 5, 0, 2 * Math.PI); 
                            ctx.fillStyle = color;
                            ctx.shadowBlur = 8;
                            ctx.shadowColor = color;
                            ctx.fill();
                            ctx.shadowBlur = 0;
                            ctx.strokeStyle = '#1a1a1a';
                            ctx.lineWidth = 2;
                            ctx.stroke();
                        }
                    }
                }
            });

            nodes.forEach(node => {
                const pt = getPixelXY(node.lat, node.lng, overlayProjection);
                if(pt) {
                    ctx.beginPath();
                    ctx.arc(pt.x, pt.y, 6, 0, 2 * Math.PI);
                    ctx.fillStyle = '#ffcc00'; 
                    ctx.fill();
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 2;
                    ctx.stroke();
                }
            });

            if (selectedStartNode) {
                const pt = getPixelXY(selectedStartNode.lat, selectedStartNode.lng, overlayProjection);
                if(pt) {
                    ctx.beginPath();
                    ctx.arc(pt.x, pt.y, 14, 0, 2 * Math.PI);
                    ctx.strokeStyle = '#00ffcc'; 
                    ctx.lineWidth = 4;
                    ctx.stroke();
                }
            }

            cars.forEach(car => {
                const pt = getPixelXY(car.lat, car.lng, overlayProjection);
                if(pt) {
                    if (imageLoaded && carImgRef.current) {
                        const width = 24;  
                        const height = 12; 
                        ctx.drawImage(carImgRef.current, pt.x - width / 2, pt.y - height / 2, width, height);
                    } else {
                        ctx.beginPath();
                        ctx.arc(pt.x, pt.y, 5, 0, 2 * Math.PI);
                        ctx.fillStyle = '#00ffcc';
                        ctx.fill();
                    }
                }
            });

            animationFrameId = requestAnimationFrame(renderFrame);
        };

        const overlay = new window.google.maps.OverlayView();
        overlay.draw = function() {
            map.__overlayProjection = this.getProjection();
        };
        overlay.setMap(map);

        renderFrame();

        return () => {
            cancelAnimationFrame(animationFrameId);
            overlay.setMap(null);
        };
    }, [map, cars, nodes, roads, lights, imageLoaded, selectedStartNode, getPixelXY]);


    const handleCanvasClick = (event) => {
        if (!map || !map.__overlayProjection) return;

        const canvas = canvasRef.current;
        const rect = canvas.getBoundingClientRect();
        const clickX = event.clientX - rect.left;
        const clickY = event.clientY - rect.top;

        const clickedNode = nodes.find(node => {
            const pt = getPixelXY(node.lat, node.lng, map.__overlayProjection);
            if (!pt) return false;
            const distance = Math.hypot(pt.x - clickX, pt.y - clickY);
            return distance <= 15; 
        });

        if (clickedNode && onNodeClick) {
            onNodeClick(clickedNode);
        }
    };

    return (
        <canvas 
            ref={canvasRef} 
            onClick={handleCanvasClick} 
            style={{ 
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                pointerEvents: onNodeClick ? 'auto' : 'none',
                zIndex: 1000,
                cursor: onNodeClick ? 'crosshair' : 'default', 
            }}
        />
    );
};

const SimulationMap = ({ cars, nodes, roads, onNodeClick, selectedStartNode, lights }) => {
    // 50.0614, 19.9383 środek Krakowa
    return (
        <Map
            defaultZoom={12.5}
            defaultCenter={{ lat: 50.0614, lng: 19.9383 }}
            gestureHandling={'greedy'}
            disableDefaultUI={true}
            // mapId="TWOJE_ID" 
        >
            <CanvasOverlay 
                cars={cars} 
                nodes={nodes} 
                roads={roads}
                lights={lights}
                onNodeClick={onNodeClick}
                selectedStartNode={selectedStartNode}
            />
        </Map>
    );
};

export default SimulationMap;