# Krakow Traffic Simulator

A real-time traffic simulation system built with a Spring Boot backend and a React frontend, integrated with the Google Maps API.

## Demo
<img src="./assets/demo.gif" alt="Traffic Simulator Demo" width="100%">

## Key Features
* **Pathfinding:** Implemented the A* (A-Star) algorithm for efficient shortest-path calculation between user-selected start and target points.
* **Real-time Synchronization:** Utilizes WebSockets to push live vehicle coordinates from the backend to the frontend with minimal latency.
* **Dynamic Traffic:** Users can spawn vehicles dynamically and observe real-time routing decisions.

## Tech Stack
* **Backend:** Java, Spring Boot, WebSockets
* **Frontend:** React.js, Google Maps API
* **Algorithms:** A* Pathfinding on Graph Data Structures

## Current Development
The logic for road networks and complex intersections is currently under active development and optimization.
