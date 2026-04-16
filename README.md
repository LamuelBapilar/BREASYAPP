# BREASY APP

## 📌 Overview

BREASY APP is a mobile health monitoring system designed to help users track respiratory health data in real time. It connects to a hardware device (ESP32-based nebulizer/monitoring system) and displays key health metrics such as heart rate (BPM) and Session Time which is used by doctors to provide accurate Asthma prescriptions.

Thesis Link: https://drive.google.com/file/d/1DgXuO94WTUQIKa6s8t1t6pJgTdAVcGL7/view?usp=sharing

Documentation and Images: https://drive.google.com/drive/folders/1y0Dl5jnTFbLRE3-vIhDn_OQU8lZ4fP4r?usp=sharing


---

## ⚙️ Features

### 🔐 Authentication

- User Registration
- User Login
- Firebase Authentication integration

### 📊 Health Monitoring

- Real-time heart rate (BPM) monitoring
- Bluetooth connection to ESP32 device
- Live data display
  
### 📈 Data Visualization

- Real-time line graph using MPAndroidChart
- Updates every 30 seconds
- Session-based graph history

### 📅 Session System

- Create and manage monitoring sessions
- View past session records
- Load session data into charts

### ⏰ Session Scheduler

- Schedule monitoring sessions in advance
- System checks scheduled time automatically
- Organizes user monitoring routines

### 🔔 Background Notifier

- Runs in background when a scheduled session is due
- Sends notification when session time is reached
- Alerts user even if app is not open

### 🚨 Emergency Alert System

- Sends emergency SMS when user triggers alert
- Includes real-time location (GPS-based)
- Helps share location with emergency contacts
- Designed for users with respiratory health conditions (e.g., asthma support use case)

### ☁️ Cloud Storage

- Firebase Realtime Database integration
- Stores BPM records per session
- Groups data using session IDs and timestamps

---

## 🏗️ Tech Stack

- Frontend (Mobile): Android (Java)
- UI: XML Layouts
- Charting: MPAndroidChart
- Backend: Firebase Realtime Database
- Authentication: Firebase Auth
- Hardware Communication: Bluetooth (ESP32)

---

## 🔄 How It Works
1. User logs in using Firebase Authentication
2. User schedules a monitoring session
3. Scheduler stores session time
4. Background notifier triggers at scheduled time
5. User connects to Breasy Nebulizer via Bluetooth
6. Device sends BPM data in real time
7. App displays live heart rate graph
8. Data is saved to Firebase per session
9. If emergency alert is triggered, the app sends an SMS with GPS location to the respective emergency responder/contact
10. User reviews past sessions in Records

--

