# OOP explanation for Campus Marketplace

## User and Student

`User` is an abstract parent class. It stores account information that any future marketplace user would need: a system ID, name, email, password verification, and common getters.

`Student` extends `User`. It inherits those common features and adds information only a campus student needs: student ID, department, and academic year.

```text
User (abstract)
├── id
├── name
├── email
├── passwordMatches()
└── common getters

Student extends User
├── studentId
├── department
└── year
```

The application creates `Student` accounts, never plain `User` accounts. Buyer and seller are not separate account classes: one student may sell an item in one order and buy an item in another.

## Four OOP ideas in this project

- **Encapsulation:** classes keep their data private. For example, `Product` controls when its status changes and `Order` controls valid order transitions.
- **Inheritance:** `Student extends User`, so common account behavior is written once in `User`.
- **Abstraction:** `MarketplaceRepository` describes the storage operations that the app needs. The current `InMemoryMarketplaceRepository` implements them for a session-only local project.
- **Polymorphism:** `MarketplaceService` depends on the `MarketplaceRepository` interface rather than a specific storage class. A database repository could be substituted later without changing the marketplace rules.

## Object relationships

```text
Student (seller) ── lists ──> Product
Student (buyer)  ── places ─> Order
Order ── refers to ──> Product
Student ── owns ──> Wishlist ── saves ──> Product
```

## Product status and order status

When a buyer places an order, the product moves from `AVAILABLE` to `SOLD`. The order starts as `PLACED`. Cancelling the order changes it to `CANCELLED` and restores the product to `AVAILABLE`. Completing the exchange changes the order to `COMPLETED`.
