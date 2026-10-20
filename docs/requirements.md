# Project requirements

## Included

- Run as a standalone Java Swing application on one computer.
- Sign up and log in with a name, email, password, student ID, department, and year for the duration of the process.
- Let every student account sell products and buy other students' available products.
- Support categories and product listings held in memory.
- Use simulated cash on delivery; no money is collected or transferred.
- Place, cancel, and complete orders with explicit states.
- Allow listing management only by the listing's seller.
- Allow students to save and remove available products in a personal Wishlist.
- Reset all users, products, orders and wishlists on application exit.

## Excluded

- Shared data between computers, a backend, database, cloud storage, or persistence after exit.
- Administrator accounts, notifications, chat, and real payment processing.
- Production-grade authentication or handling of real credentials.

## Two-computer behavior

Each launched copy has its own repository in its own process. A user on another laptop will not see the first laptop's signup, listings, or orders. Sharing those would require adding a server and shared storage in a future project scope.
