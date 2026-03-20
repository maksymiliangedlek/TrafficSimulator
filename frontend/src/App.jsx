import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import SimulationMap from './SimulationMap';

function App() {
  const [cars, setCars] = useState([]);
  const [lights, setLights] = useState([]);
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
      .then(response => {
        if (!response.ok) throw new Error('Brak odpowiedzi z serwera');
        return response.json();
      })
      .then(data => {
        setNodes(data.nodes || []);
        setRoads(data.roads || []);
      })
      .catch(error => console.error("❌ Błąd pobierania mapy:", error));
  }, []);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/traffic-stream'),
      onConnect: () => {
        setIsConnected(true);
        setStompClient(client);
        console.log('🚀 Połączono ze Springiem!');

        client.subscribe('/topic/state', (message) => {
          if (message.body) {
            const state = JSON.parse(message.body);
            setCars(state.cars || []);
            setLights(state.lights || []);
          }
        });
      },
      onStompError: (frame) => {
        console.error('Błąd STOMP:', frame.headers['message']);
      },
      onDisconnect: () => {
        setIsConnected(false);
        setStompClient(null);
        console.log('❌ Rozłączono');
      }
    });

    client.activate();
    return () => { if (client.active) client.deactivate(); };
  }, []);

  const handleStartSpawning = () => {
    if (!stompClient || !stompClient.connected) {
      alert("Czekaj, brak połączenia z serwerem!");
      return;
    }
    setSelectionMode('start');
    setSelectedStartNode(null);
  };

  const handleNodeClick = (node) => {
    if (selectionMode === 'start') {
      setSelectedStartNode(node);
      setSelectionMode('target');
    } 
    else if (selectionMode === 'target') {
      if (node.id === selectedStartNode.id) {
        alert("Cel musi być inny niż start! Wybierz jeszcze raz.");
        return;
      }

      const payload = {
        startX: selectedStartNode.x,
        startY: selectedStartNode.y,
        targetX: node.x,
        targetY: node.y
      };

      stompClient.publish({
        destination: '/app/addCar',
        body: JSON.stringify(payload)
      });
      console.log(" Wysłano auto:", payload);

      setSelectionMode('idle');
      setSelectedStartNode(null);
    }
  };

  const renderStatusText = () => {
    if (selectionMode === 'start') return <span>Kliknij skrzyżowanie <strong>STARTOWE</strong> na mapie</span>;
    if (selectionMode === 'target') return <span>Teraz kliknij skrzyżowanie <strong>DOCELOWE</strong></span>;
    return <span>Dodaj auto -></span>;
  };

  return (
    <div style={{ 
      backgroundColor: '#ffffff', 
      minHeight: '100vh', 
      fontFamily: '"Plus Jakarta Sans", Arial, sans-serif', 
      color: '#1a1a1a',
      padding: '40px 20px'
    }}>
      {/* HEADER */}
      <header style={{ textAlign: 'center', marginBottom: '40px' }}>
        <h1 style={{ fontSize: '3.5rem', fontWeight: '900', margin: '0 0 10px 0', letterSpacing: '-2px' }}>
          Traffic Simulator<span style={{ fontSize: '2.5rem' }}></span>
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
            Auta na mapie: {cars.length}
          </div>
        </div>
      </header>

      {/* WYSPA STEROWANIA */}
      <div style={{
        backgroundColor: selectionMode === 'idle' ? '#f4d06f' : '#26a69a', // Żółty lub Miętowy
        color: selectionMode === 'idle' ? '#1a1a1a' : '#ffffff',
        border: thickBorder,
        boxShadow: hardShadow,
        borderRadius: '20px',
        padding: '25px 40px',
        maxWidth: '750px',
        margin: '0 auto 40px auto',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        transition: 'all 0.3s ease'
      }}>
        <div style={{ fontSize: '1.2rem', fontWeight: '800' }}>
          {renderStatusText()}
        </div>

        {selectionMode === 'idle' ? (
          <button 
            onClick={handleStartSpawning}
            style={{
              backgroundColor: '#fff', border: thickBorder, borderRadius: '12px',
              padding: '12px 24px', fontWeight: '900', cursor: 'pointer',
              fontSize: '1rem', transition: 'transform 0.1s', color: '#1a1a1a'
            }}
            onMouseDown={(e) => e.currentTarget.style.transform = 'scale(0.95)'}
            onMouseUp={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            + DODAJ AUTO
          </button>
        ) : (
          <button 
            onClick={() => { setSelectionMode('idle'); setSelectedStartNode(null); }}
            style={{
              backgroundColor: '#1a1a1a', color: '#fff', border: 'none', borderRadius: '12px',
              padding: '12px 24px', fontWeight: '900', cursor: 'pointer', fontSize: '1rem'
            }}
          >
            ❌ ANULUJ
          </button>
        )}
      </div>

      {/* MAPA */}
      <SimulationMap 
        cars={cars} 
        nodes={nodes} 
        roads={roads}
        lights={lights} 
        onNodeClick={selectionMode !== 'idle' ? handleNodeClick : null}
        selectedStartNode={selectedStartNode}
      />
      
    </div>
  );
}

export default App;