import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

function App() {
    const [cars, setCars] = useState([]);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        // Ponieważ Twój backend używa .withSockJS(), musimy użyć webSocketFactory
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/traffic-stream'),
            debug: (str) => console.log('STOMP Debug:', str), // Logi w konsoli przeglądarki
            onConnect: () => {
                setIsConnected(true);
                console.log('🚀 Połączono ze Springiem!');

                // Subskrypcja kanału, na który nadaje Twój SimulationBroadcaster
                client.subscribe('/topic/state', (message) => {
                    if (message.body) {
                        const state = JSON.parse(message.body);
                        setCars(state.cars || []); // Zapisujemy auta do stanu
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

        // Funkcja czyszcząca - rozłącza przy zamknięciu strony
        return () => {
            if (client.active) {
                client.deactivate();
            }
        };
    }, []);

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#282c34', color: 'white', minHeight: '100vh' }}>
            <h1>Traffic Simulator 🚦</h1>

            <div style={{ marginBottom: '20px' }}>
                Status połączenia:
                <span style={{ color: isConnected ? '#4caf50' : '#f44336', fontWeight: 'bold', marginLeft: '10px' }}>
          {isConnected ? 'POŁĄCZONO' : 'ROZŁĄCZONO'}
        </span>
            </div>

            <div style={{ display: 'flex', gap: '20px' }}>
                <div style={{ flex: 1, border: '1px solid #555', padding: '15px', borderRadius: '8px' }}>
                    <h3>Aktywne Auta ({cars.length})</h3>
                    {cars.length === 0 ? (
                        <p style={{ fontStyle: 'italic', color: '#888' }}>Brak aut na mapie. Dodaj auto przez backend lub Postmana.</p>
                    ) : (
                        <ul style={{ listStyle: 'none', padding: 0 }}>
                            {cars.map((car) => (
                                <li key={car.id} style={{ marginBottom: '10px', padding: '10px', backgroundColor: '#3a3f4b', borderRadius: '4px' }}>
                                    <strong>Auto #{car.id.substring(0, 8)}</strong> <br />
                                    Pozycja: X: {car.x.toFixed(2)}, Y: {car.y.toFixed(2)}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <div style={{ flex: 2, border: '1px solid #555', padding: '15px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <p>Tutaj w następnym kroku narysujemy mapę (HTML5 Canvas)</p>
                </div>
            </div>
        </div>
    );
}

export default App;