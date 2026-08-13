# Multi-Language Online Compiler

A multi-language online compiler built with Next.js (App Router), React, Tailwind CSS, Framer Motion for the frontend and Express.js (Node.js) for the execution backend.

## Setup Instructions

### 1. Prerequisites
- Node.js (v18+)
- Python (v3+)
- g++ (for C++ compilation)

### 2. Backend Setup
1. Open a terminal and navigate to the backend directory:
   ```bash
   cd online-compiler/backend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the execution server:
   ```bash
   node server.js
   ```
   *The backend will run on `http://localhost:5000`.*

### 3. Frontend Setup
1. Open another terminal and navigate to the frontend directory:
   ```bash
   cd online-compiler/frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Next.js development server:
   ```bash
   npm run dev
   ```
   *The frontend will run on `http://localhost:3000`.*

### Features
- Cyberpunk theme with custom scrollbars, neon glows, and glassmorphism.
- Code execution for Python and C++.
- Built-in static analysis filter to prevent the execution of dangerous system commands (`system()`, `import os`, etc.).
- Robust 5-second execution timeout.
