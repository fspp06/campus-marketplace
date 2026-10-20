# Design decisions

- **UI boundary:** Swing authentication, listings and order screens live in `ui`; they call `MarketplaceService` rather than enforcing marketplace rules themselves.
- **Business rules:** `MarketplaceService` handles authentication-in-session, listing ownership, wishlist membership, checkout, and order transitions.
- **Storage boundary:** `MarketplaceRepository` isolates storage. `InMemoryMarketplaceRepository` is the only storage implementation in this local project.
- **Inheritance:** `User` is an abstract parent class for common account data and password verification. `Student` extends `User` with student ID, department, and year. Every student may act as buyer or seller; there is no admin subtype.
- **Entities:** `Student`, `Wishlist`, `Product`, `Order`, and `Category` are domain concepts. Buyer and seller are roles held by students within an order.
- **Order lifecycle:** `PLACED` can become `CANCELLED` or `COMPLETED`. Cancellation restores product availability; the product is marked sold as soon as checkout creates a placed order.
- **Payment:** The checkout concept is cash on delivery only. It is a label/simulation, not a payment integration.
- **Wishlist:** Each `Student` owns a `Wishlist` that contains saved available products. Removing a listing removes it from every wishlist.
- **Session and privacy:** All information is transient. The password field supports local signup/login and is not suitable for real accounts.
