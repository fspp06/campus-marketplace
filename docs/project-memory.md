# Project memory and reproduction guide

This document records the decisions agreed for the Campus Marketplace class project. It is a guide for continuing, presenting, or rebuilding the project without needing a server or external service.

## Project purpose

Campus Marketplace is a Java Swing simulation in which campus students can list items for sale and buy items from other students. The purpose is to demonstrate object-oriented programming with a usable desktop interface.

## Agreed scope

- The application is local and session-only. Data is kept in memory and resets when the application closes.
- Students can sign up and log in during the same run.
- One student account can be both a buyer and a seller. There are no separate buyer or seller account classes.
- Checkout is simulated cash on delivery (COD). No payment gateway, API, or paid service is used.
- A student can create, edit, and remove their own available listings.
- A buyer can place an order for an available item. The product becomes `SOLD` immediately to prevent a second order.
- Buyer or seller can cancel a placed order. Cancellation restores the product to `AVAILABLE`.
- Buyer or seller can complete a placed order after the handoff.
- Students can add available products to a session-only Wishlist.
- Listings can include a JPG, PNG, or GIF product photo up to 5 MB. The image is held only for the current run.

## Features intentionally removed or excluded

- Administrator accounts and admin screens
- Notification classes and notification UI
- Chat between users
- Reviews and ratings
- Persistent database or file storage
- Shared state across laptops
- Real payments, delivery tracking, maps, third-party APIs, and paid services

These exclusions keep the project focused, simple to demonstrate, and aligned with an OOP class assignment.

## Two-computer behavior

Each computer runs a separate Java process with a separate `InMemoryMarketplaceRepository`. A signup, product, Wishlist, or order created on one laptop cannot appear on another laptop. Multi-laptop sharing would require a backend service and shared database, which are outside this project scope.

## OOP concepts to explain during presentation

| Concept | Project example |
| --- | --- |
| Encapsulation | Models keep fields private and expose methods for controlled changes. |
| Inheritance | `Student` extends the abstract `User` class. |
| Abstraction | `MarketplaceRepository` defines storage operations; the UI uses `MarketplaceService` rather than directly changing models. |
| Polymorphism | Code can treat a `Student` as a `User` where common user behavior is needed. |
| Composition | A `Student` owns a `Wishlist`; an `Order` refers to one product, buyer, and seller. |
| Enums | `ProductStatus` and `OrderStatus` represent valid states without free-text errors. |

## Folder structure

```text
CampusMarketplace_GUI/
├── README.md                         # setup, walkthrough, and project overview
├── .gitignore                        # excludes compiled output and editor files
├── docs/
│   ├── design-decisions.md            # architecture decisions
│   ├── oop-guide.md                   # class relationships and OOP explanation
│   ├── project-memory.md              # this project history and reproduction guide
│   └── requirements.md                # agreed included and excluded requirements
└── src/
    └── main/
        ├── java/edu/campusmarketplace/
        │   ├── Main.java
        │   ├── enums/                 # ProductStatus and OrderStatus
        │   ├── model/                 # User, Student, Product, Order, Wishlist, Category
        │   ├── repository/            # repository interface and in-memory storage
        │   ├── service/               # marketplace rules and seeded starter data
        │   └── ui/                    # Java Swing authentication and screens
        └── resources/images/          # starter product photos
```

`out/` is created by compilation and is intentionally excluded from Git.

## Reproduce from a fresh clone

### Prerequisites

- Git for Windows
- JDK 17 or newer

### Commands

```powershell
git clone https://github.com/fspp06/campus-marketplace.git
Set-Location .\campus-marketplace
$sources = Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $sources
Copy-Item -Recurse .\src\main\resources\* .\out
java -cp out edu.campusmarketplace.Main
```

### Starter accounts

| Role used in walkthrough | Email | Password |
| --- | --- | --- |
| Seller | `maya@campus.local` | `campus123` |
| Buyer | `arjun@campus.local` | `campus123` |

## Manual acceptance checklist

1. Start the app and sign in with either starter account.
2. Browse listings, search or filter, and open a listing detail screen.
3. Add an item to the Wishlist and confirm it appears on the Wishlist tab.
4. Sign in as the buyer and place a COD order. Confirm the item appears in My orders and no longer appears as available in Browse products.
5. Cancel the placed order and confirm that item is available again.
6. Place another order, sign in as the seller, and mark the order completed.
7. Create a listing, attach a permitted image, edit it, and remove it while still available.
8. Close and reopen the application. Confirm the session data resets to the starter data.

## Git publishing workflow

The repository should contain source code, documentation, and the small starter images. It must not include `out/` or compiled `.class` files. After changes:

```powershell
git status
git add README.md docs src .gitignore
git commit -m "Describe change"
git push
```
