import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import SimulationMap from './SimulationMap';

function App() {
  const [cars, setCars] = useState([]);
  const [nodes, setNodes] = useState([]);
  const [roads, setRoads] = useState([]);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    fetch('http://localhost:8080/api/map')
      .then(response => {
        if (!response.ok) throw new Error('Brak odpowiedzi z serwera');
        return response.json();
      })
      .then(data => {
        console.log("Pobrano mapę z API:", data);
        setNodes(data.nodes || []);
        setRoads(data.roads || []);
      })
      .catch(error => console.error(" Błąd pobierania mapy:", error));
  }, []);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/traffic-stream'),
      debug: (str) => console.log('STOMP Debug:', str),
      onConnect: () => {
        setIsConnected(true);
        console.log('🚀 Połączono ze Springiem!');

        client.subscribe('/topic/state', (message) => {
          if (message.body) {
            const state = JSON.parse(message.body);
            setCars(state.cars || []); 
          }
        });
      },
      onStompError: (frame) => {
        console.error('Błąd STOMP:', frame.headers['message']);
      },
      onDisconnect: () => {
        setIsConnected(false);
        console.log('❌ Rozłączono');
      }
    });

    client.activate();

    return () => {
      if (client.active) {
        client.deactivate();
      }
    };
  }, []);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#282c34', color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <h1>Traffic Simulator 🚦</h1>
      
      <div style={{ marginBottom: '20px' }}>
        Status połączenia: 
        <span style={{ color: isConnected ? '#4caf50' : '#f44336', fontWeight: 'bold', marginLeft: '10px' }}>
          {isConnected ? 'POŁĄCZONO' : 'ROZŁĄCZONO'}
        </span>
      </div>

      <div style={{ marginBottom: '10px' }}>
        Auta na mapie: <strong>{cars.length}</strong>
      </div>

      {}
      <SimulationMap cars={cars} nodes={nodes} roads={roads} />
      
    </div>
  );
}

export default App;