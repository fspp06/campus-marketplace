# Campus Marketplace — Architecture & Technical Context

## 1. System Overview
**Campus Marketplace** is a standalone, single-process Java 17+ desktop application built with pure Java Swing. It provides a secure, simulated peer-to-peer campus marketplace where students can buy, sell, exchange (barter), and coordinate in-person campus meetups.

The application operates without external database or network dependencies. All state resides in-memory via the Repository pattern and resets cleanly on application restart, making it ideal for classroom presentations and local evaluation.

---

## 2. Technical Stack
- **Language / Runtime:** Java 17+ (Uses Records, Pattern Matching, Switch Expressions, and Modern Collections API).
- **GUI Framework:** Java Swing (`javax.swing.*`, `java.awt.*`) customized with modern design tokens (inspired by Notion, Airbnb, and Vinted).
- **Typography:** Adaptive font resolver prioritized for high-readability (`Segoe UI`, `Inter`, `Helvetica Neue`, `Arial`).
- **Data Persistence:** In-Memory Repository (`InMemoryMarketplaceRepository`).
- **Zero Third-Party Dependencies:** Runs out-of-the-box on standard JDK without Maven/Gradle/external JAR requirements.

---

## 3. Core Domain Models & Relationships

```
                     ┌────────────────────────┐
                     │   User (Abstract)      │
                     │  - id: UUID            │
                     │  - name: String        │
                     │  - email: String       │
                     │  - password: String    │
                     └───────────▲────────────┘
                                 │
                     ┌───────────┴────────────┐
                     │        Student         │
                     │  - studentId: String   │
                     │  - department: String  │
                     │  - year: int           │
                     │  - phoneNumber: String │
                     │  - wishlist: Wishlist  │
                     └───────────┬────────────┘
                                 │ owns / lists
                                 ▼
                     ┌────────────────────────┐
                     │        Product         │
                     │  - id: UUID            │
                     │  - name: String        │
                     │  - price: double       │
                     │  - category: Category  │
                     │  - status: Status      │
                     │  - openToExchange: bool│
                     │  - imageBytes: byte[]  │
                     └───────────┬────────────┘
                                 │ involved in
                                 ▼
                     ┌────────────────────────┐
                     │         Order          │
                     │  - id: UUID            │
                     │  - buyer: Student      │
                     │  - seller: Student     │
                     │  - product: Product    │
                     │  - offeredItem: Product│
                     │  - status: Status      │
                     └────────────────────────┘
```

- **`User` (Abstract):** Base identity containing UUID, name, normalized email, and password comparison. Implements email-based equality and hashing.
- **`Student` (Extends `User`):** Represents an authenticated student on campus. Holds academic credentials (Student ID, Department, Academic Year, Phone Number) and has a personal `Wishlist`.
- **`Product`:** Represents an item listed by a student. Contains title, price, category, photo bytes, exchange preferences, and lifecycle status (`AVAILABLE`, `SOLD`, `EXCHANGED`).
- **`Order`:** Cash on Delivery transaction or mutual item swap agreement between a buyer and seller.
- **`Review`:** Peer feedback with 1–5 star rating, text comment, and experience badges (`Item as described`, `On-time meetup`, `Tested in person`, `Quick response`, `Friendly senior`, `Fair price`).
- **`Category`:** 7 structured categories: Books & Notes, Electronics & Gadgets, Lab & Engineering Tools, Hostel & Dorm Essentials, Fashion & Bags, Sports & Fitness, Other Essentials.

---

## 4. State Machines

### Product Lifecycle:
```
[AVAILABLE] ──(Place COD Order)──► [SOLD] ──(Cancel Order)──► [AVAILABLE]
     │
     └──(Confirm Exchange)──► [EXCHANGED] ──(Cancel Exchange)──► [AVAILABLE]
```

### Order Lifecycle:
```
[PLACED] ──(Mark Completed)──► [COMPLETED] ──► (Eligible for Review)
    │
    └──(Cancel Order)────────► [CANCELLED]
```

---

## 5. UI Architecture & View Hierarchy

The main window (`MarketplaceFrame`) uses `CardLayout` to toggle between the **Auth View** and the **Marketplace View**.

Inside the Marketplace View, navigation is controlled by a modern `SegmentedNavBar`:
1. **Home (`HomePanel`):** Welcome banner, quick action buttons, statistics, and real student reviews with star ratings and experience badges.
2. **Browse Products (`BrowseProductsPanel`):** Real-time text search, category dropdown, and barter swap filter with responsive card grid (`WrapLayout`).
3. **Wishlist (`buildWishlistView`):** Saved items grid with instant un-saving and quick checkout options.
4. **My Items (`buildMyListingsView`):** Inventory listed by the active user with Edit, Delete, and Details actions.
5. **My Orders (`buildOrdersView`):** Split layout featuring an interactive Orders table on the left and a detailed inspection card on the right.
6. **Profile (`buildProfileView`):** Identity card with initial avatar, contact info, and campus seller reputation score.

---

## 6. Frictionless Onboarding & Auto-Provisioning

To ensure smooth evaluation and live presentations:
- **Starter Accounts:** 4 pre-seeded students (`maya@campus.local`, `arjun@campus.local`, `rohan@campus.local`, `sneha@campus.local`) with password `campus123`.
- **Instant Login:** The login form is pre-filled with `maya@campus.local` and `campus123` for 1-click entry.
- **Auto-Provisioning for Any Account:** If a user logs in or creates an account with **any custom email**, the service automatically provisions starter listings in their "My items" and pre-populates 3 items in their "Wishlist", ensuring the interface is never empty.

---

## 7. Build and Run Commands

```powershell
Set-Location .\CampusMarketplace_GUI
$sources = Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $sources
Copy-Item -Recurse -Force .\src\main\resources\* .\out
java -cp out edu.campusmarketplace.Main
```
