[![progress-banner](https://backend.codecrafters.io/progress/redis/6bd7f976-ea62-48ca-81a4-ae9f7c8dbe0a)](https://app.codecrafters.io/users/le-pp2402?r=2qF)

### "Build Your Own Redis" Challenge

In this challenge, you'll build a simple Redis clone that supports basic commands like `PING`, `SET`, and `GET`. Along the way, you'll learn about event loops, the Redis protocol, and more.

---

### RedisInputStream

#### What is it for?

**User Space** and **Kernel Space**:

![User Space vs Kernel Space](./resources/kernel-user-mode.jpg)

- **User Space**: Where user applications run, with limited access to system resources.
- **Kernel Space**: Where the operating system kernel runs, with full access to hardware.

When an application needs to perform privileged operations (like reading from disk or sending data over the network), it makes a **system call** to the kernel. This triggers a **context switch**:

1. The OS saves the current state of the user process.
2. Control switches to the kernel to perform the requested operation.
3. The OS restores the user process state and returns control.

**Context switches** have costs:
- **CPU time** for saving/restoring state.
- **Cache misses** as CPU caches may need to be reloaded.

These costs occur during system calls, task scheduling, or hardware interrupts.


When a program connects to a server (such as Redis), it cannot predict exactly how much data the server will send. Allocating a buffer that's too large wastes memory, while one that's too small is inefficient.

Performance: Reading data in a loop using a buffer is much more efficient. Instead of making a system call to read 1 byte from the socket each time you need a byte, the program makes a single I/O call to read 8KB (the default buffer size), then processes those 8KB in memory. This significantly reduces the cost of **context switching** between kernel and user space.
