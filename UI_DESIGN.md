# Swarm Mobile UI Design

## Application Layout

The Swarm Mobile application features a modern Material Design interface with the following components:

### 1. Node Information Card (Top)
- **Title**: "Node Information"
- **Node ID**: Displays the unique identifier for this Swarm node
- **Status**: Shows current node status (Running/Stopped) with color coding:
  - Green (#4CAF50) when Running
  - Red (#F44336) when Stopped

### 2. Control Panel Card
- **Title**: "Node Status"
- **Start Node Button**: Primary button with play icon
  - Enabled when node is stopped
  - Disabled when node is running
- **Stop Node Button**: Outlined button with pause icon
  - Disabled when node is stopped
  - Enabled when node is running

### 3. Peer Connection Card
- **Title**: "Connected Peers"
- **Peer ID Input**: Material text input field
  - Hint: "Enter Peer ID"
  - Outlined style
- **Connect Peer Button**: Full-width button
  - Enabled only when node is running
  - Disabled when node is stopped

### 4. Peers List Card (Bottom)
- **Title**: "Connected Peers"
- **Peer List**: Displays all connected peers
  - Shows "No peers connected" when empty
  - Each peer shown with bullet point (•)
  - Updates in real-time as peers connect/disconnect

## Color Scheme
- **Primary**: Purple (#6200EE)
- **Primary Variant**: Dark Purple (#3700B3)
- **Secondary**: Teal (#03DAC5)
- **Status Running**: Green (#4CAF50)
- **Status Stopped**: Red (#F44336)
- **Card Background**: Light Gray (#F5F5F5)

## User Flow
1. User launches app → Node is stopped by default
2. User taps "Start Node" → Node starts, status turns green, peer connection enabled
3. User enters peer ID and taps "Connect Peer" → Peer is added to the list
4. User can see all connected peers in the bottom card
5. User taps "Stop Node" → Node stops, all peers cleared, status turns red

## Material Components Used
- MaterialCardView for all card sections
- MaterialButton for action buttons
- TextInputLayout/TextInputEditText for text input
- Material color theming throughout
