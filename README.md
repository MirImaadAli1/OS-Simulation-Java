# OS-Simulation-Java

A multithreaded operating system simulation in Java implementing processor allocation, priority-based scheduling, and safe concurrency using locks and condition variables.

## 🚀 Overview

This project simulates core behavior of an operating system scheduler, including:
- Process registration with priorities
- Dynamic processor allocation
- Preemptive scheduling
- Blocking and signaling using `Condition` variables
- Thread-safe operations with `ReentrantLock`

It’s designed to emulate how an OS might manage limited CPU cores and prioritize tasks while ensuring thread safety and correctness.

---

## 🧠 Features

- ✅ **Priority Queues (0–9)** for handling processes by urgency  
- ✅ **Multiple Processors Support** (configurable dynamically)  
- ✅ **Fair Locking** using `ReentrantLock(true)`  
- ✅ **Condition Variables** to block and wake threads efficiently  
- ✅ **Graceful Handling** of process termination and scheduling

---

## 📁 Project Structure

```
OS-Simulation-Java/
├── OS.java               # Core operating system simulation
├── OS_sim_interface.java# Interface definition (provided)
├── Tests.java            # Suite of unit & functional tests
├── Main.java             # Entry point to run tests
├── README.md             # You are here :)
```

---

## 🔧 How It Works

- **Registering a Process:**  
  Each process is assigned a unique PID and a priority from 0 (lowest) to 9 (highest).

- **Starting a Process:**  
  If a processor is free, it's immediately assigned. Otherwise, the process is enqueued based on its priority and blocks using `Condition.await()`.

- **Scheduling / Terminating:**  
  Releases the processor, then signals the next-highest priority waiting process.

- **Concurrency Management:**  
  All shared state (queues, assignments) is guarded by a single `ReentrantLock`, ensuring correctness under multithreading.

---
