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
      // debug: (str) => console.log('STOMP Debug:', str),
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
      console.log("📤 Wysłano auto:", payload);

      setSelectionMode('idle');
      setSelectedStartNode(null);
    }
  };

  const renderStatusText = () => {
    if (selectionMode === 'start') return <span style={{ color: '#00ffcc' }}>👉 Kliknij skrzyżowanie STARTOWE na mapie</span>;
    if (selectionMode === 'target') return <span style={{ color: '#ffcc00' }}>🎯 Teraz kliknij skrzyżowanie DOCELOWE</span>;
    return <span>Wybierz trasę i wypuść auto.</span>;
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#282c34', color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <h1>Traffic Simulator 🚦</h1>
      
      <div style={{ marginBottom: '10px' }}>
        Status połączenia: 
        <span style={{ color: isConnected ? '#4caf50' : '#f44336', fontWeight: 'bold', marginLeft: '10px' }}>
          {isConnected ? 'POŁĄCZONO' : 'ROZŁĄCZONO'}
        </span>
      </div>

      <div style={{ marginBottom: '20px' }}>Auta na mapie: <strong>{cars.length}</strong></div>

      {}
      <div style={{ display: 'flex', gap: '20px', alignItems: 'center', marginBottom: '20px', backgroundColor: '#1e2124', padding: '15px 25px', borderRadius: '8px', minWidth: '400px', justifyContent: 'space-between' }}>
        <div style={{ fontWeight: 'bold', fontSize: '16px' }}>
          {renderStatusText()}
        </div>

        {selectionMode === 'idle' && (
          <button 
            onClick={handleStartSpawning}
            style={{ padding: '10px 20px', backgroundColor: '#ffcc00', color: '#000', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' }}
          >
            ➕ Dodaj Auto
          </button>
        )}
        
        {}
        {selectionMode !== 'idle' && (
          <button 
            onClick={() => { setSelectionMode('idle'); setSelectedStartNode(null); }}
            style={{ padding: '10px 20px', backgroundColor: '#f44336', color: '#fff', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' }}
          >
            ❌ Anuluj
          </button>
        )}
      </div>

      {}
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