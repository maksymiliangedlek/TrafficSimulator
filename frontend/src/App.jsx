import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import SimulationMap from './SimulationMap';

function App() {
  const [cars, setCars] = useState([]);
  const [nodes, setNodes] = useState([]);
  const [roads, setRoads] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [stompClient, setStompClient] = useState(null);
  const [selectionMode, setSelectionMode] = useState('idle');
  const [selectedStartNode, setSelectedStartNode] = useState(null);

  const thickBorder = '3px solid #1a1a1a';
  const hardShadow = '6px 6px 0px #1a1a1a';

  useEffect(() => {
    fetch('http://localhost:8080/api/map')
      .then(res => res.json())
      .then(data => {
        setNodes(data.nodes || []);
        setRoads(data.roads || []);
      });
  }, []);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/traffic-stream'),
      onConnect: () => {
        setIsConnected(true);
        setStompClient(client);
        client.subscribe('/topic/state', (msg) => {
          const state = JSON.parse(msg.body);
          setCars(state.cars || []);
        });
      },
    });
    client.activate();
    return () => client.deactivate();
  }, []);

  const handleNodeClick = (node) => {
    if (selectionMode === 'start') {
      setSelectedStartNode(node);
      setSelectionMode('target');
    } else if (selectionMode === 'target') {
      if (node.id === selectedStartNode.id) return;
      stompClient.publish({
        destination: '/app/addCar',
        body: JSON.stringify({
          startX: selectedStartNode.x, startY: selectedStartNode.y,
          targetX: node.x, targetY: node.y
        })
      });
      setSelectionMode('idle');
      setSelectedStartNode(null);
    }
  };

  return (
    <div style={{ 
      backgroundColor: '#fdfbf7', 
      minHeight: '100vh', 
      fontFamily: '"Plus Jakarta Sans", sans-serif', 
      color: '#1a1a1a',
      padding: '40px 20px'
    }}>
      {}
      <header style={{ textAlign: 'center', marginBottom: '40px' }}>
        <h1 style={{ fontSize: '3.5rem', fontWeight: '900', margin: '0 0 10px 0', letterSpacing: '-2px' }}>
          Traffic Simulator <span style={{ fontSize: '2.5rem' }}></span>
        </h1>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '15px' }}>
          <div style={{ 
            padding: '8px 16px', backgroundColor: '#fff', border: thickBorder, borderRadius: '50px', fontWeight: 'bold' 
          }}>
            Status: <span style={{ color: isConnected ? '#2ecc71' : '#e74c3c' }}>{isConnected ? '● POŁĄCZONO' : '○ ROZŁĄCZONO'}</span>
          </div>
          <div style={{ 
            padding: '8px 16px', backgroundColor: '#fff', border: thickBorder, borderRadius: '50px', fontWeight: 'bold' 
          }}>
            Auta: {cars.length}
          </div>
        </div>
      </header>

      {}
      <div style={{
        backgroundColor: selectionMode === 'idle' ? '#f4d06f' : '#26a69a', 
        border: thickBorder,
        boxShadow: hardShadow,
        borderRadius: '20px',
        padding: '25px 40px',
        maxWidth: '700px',
        margin: '0 auto 40px auto',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        transition: 'all 0.3s ease'
      }}>
        <div style={{ fontSize: '1.2rem', fontWeight: '800' }}>
          {selectionMode === 'idle' && "Gotowy do drogi? Dodaj nowy pojazd!"}
          {selectionMode === 'start' && "📍 Wybierz punkt startowy na mapie"}
          {selectionMode === 'target' && "🎯 Teraz wybierz cel podróży"}
        </div>

        {selectionMode === 'idle' ? (
          <button 
            onClick={() => setSelectionMode('start')}
            style={{
              backgroundColor: '#fff', border: thickBorder, borderRadius: '12px',
              padding: '12px 24px', fontWeight: '900', cursor: 'pointer',
              fontSize: '1rem', transition: 'transform 0.1s',
            }}
            onMouseDown={(e) => e.currentTarget.style.transform = 'scale(0.95)'}
            onMouseUp={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            + DODAJ AUTO
          </button>
        ) : (
          <button 
            onClick={() => {setSelectionMode('idle'); setSelectedStartNode(null);}}
            style={{
              backgroundColor: '#1a1a1a', color: '#fff', border: 'none', borderRadius: '12px',
              padding: '12px 24px', fontWeight: '900', cursor: 'pointer'
            }}
          >
            ANULUJ
          </button>
        )}
      </div>

      <SimulationMap 
        cars={cars} nodes={nodes} roads={roads} 
        onNodeClick={selectionMode !== 'idle' ? handleNodeClick : null}
        selectedStartNode={selectedStartNode}
        style={{ border: thickBorder, boxShadow: hardShadow, borderRadius: '24px' }}
      />
    </div>
  );
}

export default App;