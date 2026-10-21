# Campus Marketplace

A production-grade Java Swing desktop marketplace simulation designed specifically for university campus communities. Students can buy, sell, barter/swap items, coordinate campus meetups, and build peer seller reputation.

Data lives in memory during the application session and resets on restart. Each laptop runs an independent instance without requiring any external servers, database setup, or third-party dependencies.

---

## Quick Launch (Powershell)

From the project root:

```powershell
Set-Location .\CampusMarketplace_GUI
$sources = Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $sources
Copy-Item -Recurse -Force .\src\main\resources\* .\out
java -cp out edu.campusmarketplace.Main
```

### Pre-Seeded Starter Accounts (Password: `campus123`)

| Student | Email | Department & Year | Starter "My Items" |
| :--- | :--- | :--- | :--- |
| **Maya Rao** | `maya@campus.local` | Computer Science, Year 3 | 7 items (Books, Desk Lamp, Lab Coat, etc.) |
| **Arjun Mehta** | `arjun@campus.local` | Electronics, Year 2 | 7 items (Mouse, Calculator, USB-C Hub, Fan, etc.) |
| **Rohan Kumar** | `rohan@campus.local` | Mechanical Engg, Year 4 | 6 items (Mini Drafter, Kettle, Badminton, etc.) |
| **Sneha Patel** | `sneha@campus.local` | Information Tech, Year 1 | 6 items (Headphones, Backpack, Organizer, etc.) |

> **Note:** The Login dialog comes pre-filled with `maya@campus.local` / `campus123` for instant 1-click access. You can also sign in or create an account with **any custom email**—the system will automatically provision starter items and pre-populate your wishlist so your marketplace is never empty!

---

## Key Features

### 1. 7 Student-Centric Categories
- **Books & Notes:** Textbooks, handwritten revision guides, solved question banks.
- **Electronics & Gadgets:** Calculators, wireless mice, multiport adapters, headphones.
- **Lab & Engineering Tools:** Mini drafters, wooden drawing boards, chemistry lab coats.
- **Hostel & Dorm Essentials:** Fast-boiling kettles, eye-care study lamps, multi-plug surge strips.
- **Fashion & Bags:** Waterproof campus laptop backpacks.
- **Sports & Fitness:** Badminton sets, non-slip yoga/exercise mats, shaker bottles.
- **Other Essentials:** Insulated water bottles, thermal containers.

### 2. High-Resolution Bundled Visual Catalog
- 24 active student listings pre-seeded across all 7 categories.
- Built-in photo assets for books, lamps, mice, calculators, drafters, kettles, headphones, and backpacks.
- High-fidelity aspect-ratio image scaling with clean card clipping and hover states.

### 3. Cash on Delivery (COD) Checkout
- Orders can be placed immediately with simulated Cash on Delivery.
- Placed orders mark the item `SOLD` to prevent duplicate orders.
- Orders can be tracked, marked completed upon campus handoff, or cancelled (which returns the item to available status).

### 4. Campus Item Barter & Swap
- Listings can be marked with `Open to swap / exchange with other student's item` along with barter preferences.
- Items open to barter show a distinctive teal **SWAP OK** badge.
- Buyers can propose a trade by choosing one of their own active listings and specifying a proposed meetup spot and time (e.g., *"Central Library foyer, 4:30 PM"*).
- When accepted, both products transition to `EXCHANGED` status and appear under **My orders**. Cancelling restores both items back to `AVAILABLE`.

### 5. Chat & Meetup Coordination
- Buyers can click **Chat with Seller** on any listing to send their phone number and a meetup proposal.
- Includes 1-click quick message chips (e.g., *"Hi, is this still available?"*, *"Can we meet outside the Central Library today?"*).

### 6. Seller Experience Badges & Reputation
- Buyers can rate sellers from 1 to 5 stars upon completing an order.
- Supports campus experience badges:
  - `Item as described`
  - `On-time meetup`
  - `Tested in person`
  - `Quick response`
  - `Friendly senior`
  - `Fair price`
- Average seller ratings and recent reviews are displayed directly on product cards, product details, and the Home feed.

---

## Application Structure & Navigation

- **Home:** Clean introduction banner, quick navigation pills, and verified campus student review experiences.
- **Browse products:** Real-time search, category dropdown filter, and `[✓] Swaps only` toggle. Click on any card for full details, chat, wishlist saving, COD order, or exchange proposal.
- **Wishlist:** Revisit saved items.
- **My items:** Full inventory management for the logged-in student. Shows listed items with Edit, Remove, and Details actions.
- **My orders:** Split-view orders table and inspection panel displaying Cash on Delivery and Barter swap agreements.
- **Profile:** Account info (Student ID, department, year, phone) and seller reputation badge summary.

---

## Object-Oriented Programming (OOP) Architecture

| OOP Principle | Implementation in Project |
| :--- | :--- |
| **Encapsulation** | Models (`User`, `Student`, `Product`, `Order`, `Review`, `Wishlist`) maintain private fields with validation in constructors and explicit getters/mutators. |
| **Inheritance** | `Student` extends abstract `User` to inherit identity, email, and password authentication, while adding academic fields (department, student ID, year, phone). |
| **Polymorphism** | `User` instances can be handled interchangeably; UI event listeners (`ProductCardListener`) decouple component actions from business logic. |
| **Abstraction** | `MarketplaceRepository` specifies storage contracts, implemented by `InMemoryMarketplaceRepository`. `MarketplaceService` abstracts all transaction rules away from Swing UI components. |
| **Composition** | A `Student` has-a `Wishlist`; an `Order` references a `Product`, buyer, seller, and optional trade product. |
| **Enumerations** | `ProductStatus` (`AVAILABLE`, `SOLD`, `EXCHANGED`) and `OrderStatus` (`PLACED`, `COMPLETED`, `CANCELLED`) guarantee type-safe domain states. |

---

## File Layout

```text
CampusMarketplace_GUI/
├── README.md                          # Project documentation and guide
├── CONTEXT.md                         # Technical context and design blueprint
├── docs/
│   ├── design-decisions.md            # Architecture rationale
│   ├── oop-guide.md                   # Detailed OOP breakdown
│   ├── project-memory.md              # Historical iterations & test specifications
│   └── requirements.md                # Functional requirements
└── src/
    └── main/
        ├── java/edu/campusmarketplace/
        │   ├── Main.java              # Application entry point
        │   ├── enums/                 # ProductStatus, OrderStatus
        │   ├── model/                 # User, Student, Product, Order, Review, Wishlist, Category
        │   ├── repository/            # MarketplaceRepository, InMemoryMarketplaceRepository
        │   ├── service/               # MarketplaceService (business rules & sample catalog)
        │   └── ui/                    # Swing UI (MarketplaceFrame, HomePanel, BrowseProductsPanel, ProductCardPanel, UITheme)
        └── resources/images/          # Bundled PNG photo assets
```
