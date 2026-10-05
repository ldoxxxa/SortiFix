# SortiFX — Baggage Sorting System Simulator

**Software engineering team project**  
Hochschule RheinMain | Summer Semester 2026

---

## 📌 About the Project

SortiFX is a JavaFX desktop application for designing and simulating baggage sorting systems. Users can build a layout from conveyor elements, configure how baggage moves through the system, and watch the sorting process in an animated simulation.

---

## ✨ Features

- Create and edit baggage sorting layouts
- Place conveyor elements, switches, destination stations, and baggage
- Configure switches to control baggage routes
- Validate routes and detect dead ends, direction errors, and loops
- Run an animated baggage transport simulation
- Save and load layouts locally

---

## 🛠️ Technologies

- **Java 21** — application and simulation logic
- **JavaFX** — desktop user interface
- **Gradle** — build management

---

## 🧩 Architecture

The project separates the user interface from the application logic and file storage. Its main components are organized into `view`, `controller`, `model`, and `persistence`.

---
## 📸 Screenshots

### Start Screen
Choose the grid dimensions to create a new layout or load an existing baggage sorting system.

![Start screen](<Screenshots/Bildschirmfoto 2026-10-05 um 16.22.34(1).png>)

### Layout Editor
Build and edit conveyor layouts using straight sections, bends, switches, destination stations, and baggage.

![Layout editor](<Screenshots/Bildschirmfoto 2026-10-05 um 16.22.51(1).png>)

### Switch Routing Rules
Configure incoming and outgoing directions and define routing rules based on baggage destinations.

![Switch routing rules](<Screenshots/Bildschirmfoto 2026-10-05 um 16.38.41.png>)

### Destination Configuration
Assign a destination name, such as Terminal A, to a destination station.

![Destination configuration](<Screenshots/Bildschirmfoto 2026-10-05 um 16.40.07.png>)

### Simulation View
Start, pause, or reset the simulation to follow baggage along the configured conveyor route.

![Simulation view](<Screenshots/Bildschirmfoto 2026-10-05 um 16.26.52(1).png>)

### Completed Transport
The baggage has reached its destination, completing the simulation.

![Completed transport](<Screenshots/Bildschirmfoto 2026-10-05 um 16.27.00(1).png>)
