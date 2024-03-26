# Load Balancer

A functional load balancer built with http4s and cats-effect that sits in front of 1 or more http server instances.

Uses a round-robin algorithm to balance the load, perform periodic health checks, and handle client requests/server responses.

