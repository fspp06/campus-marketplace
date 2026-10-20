# Campus Marketplace

A Java Swing, single-process marketplace simulation for campus students. Every account can buy and sell. Data lives only in memory and resets when the app closes. Each laptop runs an independent copy; there is no shared server or cross-laptop data.

## Requirements and launch

- JDK 17 or newer.
- No third-party libraries or database.
- From this project directory, compile and launch with:

```powershell
$sources = Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $sources
Copy-Item -Recurse .\src\main\resources\* .\out
java -cp out edu.campusmarketplace.Main
```

## Scope and rules

- Starter accounts and sample listings are loaded at startup. Students can also sign up and log in during the current run using name, email, password, student ID, department, and year. Duplicate emails and student IDs are rejected.
- Listings are owned by their seller; only that seller may edit or remove an available listing.
- A purchase is a simulated **cash on delivery** order. It changes the product to `SOLD` immediately so another buyer cannot order it.
- Product listings can include an optional JPG, PNG, or GIF image up to 5 MB. The image is kept in memory for this run and resets when the app closes.
- A placed order may be cancelled by its buyer or seller; cancellation restores the product to `AVAILABLE`. Completed/cancelled orders cannot be cancelled again.
- The buyer or seller can mark a placed order completed after the exchange.
- Students can save available listings to a personal Wishlist for the current app session.
- There is no administrator role, notification system, chat, real payment processing, persistent storage, or multi-device synchronization in this agreed scope.

Passwords exist only in memory for the local login flow. This is not production authentication or secure credential storage.

## Main navigation

- **Browse products:** Search and filter all available student listings. Open a full listing view, save an item, or continue to cash-on-delivery checkout.
- **Wishlist:** Revisit saved available listings and remove items you no longer want to keep.
- **My items:** View active and sold listings that belong to the signed-in student. Edit or remove available listings, or create a new listing.
- **My orders:** Track purchases and sales, inspect the selected item's details, cancel a placed order, or mark an exchange completed.
- **Profile:** View the signed-in account and the session-only storage note.

## OOP design

`User` is an abstract parent class for common account details: ID, name, email, password verification, and login-related behavior. `Student` extends `User` and adds student ID, department, and year. A student may be the buyer in one order and the seller in another, so buyer and seller are order roles rather than separate account classes.

## Run it and check the UI

From the workspace parent folder (`C:\Users\darkm\OneDrive\Desktop\campus marketplace`), run these commands. The first command changes into the project folder; keep that PowerShell window there for both compile and launch:

```powershell
Set-Location .\CampusMarketplace_GUI
$sources = Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $sources
Copy-Item -Recurse .\src\main\resources\* .\out
java -cp out edu.campusmarketplace.Main
```

The login screen shows two starter accounts. Both use password `campus123`:

- Seller: `maya@campus.local`
- Buyer: `arjun@campus.local`

Suggested UI walkthrough:

1. Log in as the seller. Browse the product cards, open a detail view, and try editing a listing or adding one. The listing form lets you choose a photo and preview it before saving.
2. Log out and log in as the buyer. Buy the textbook using its **Buy · COD** button. The product should disappear from available listings and appear in **My orders** as `PLACED`.
3. Cancel that order. The textbook should return to the marketplace as available.
4. Place the order again. Log out, return to the seller account, and mark the order completed from **My orders**.
5. From Browse products, select the heart button on the desk lamp. Open the **Wishlist** tab to see the saved item, then remove it if needed.
6. Try signing up with a new email. The new account can also list and buy products during this run.

Close and relaunch the app to reset the in-memory data to the seeded starter accounts and listings.

## Project layout

```text
CampusMarketplace_GUI/
├── README.md
├── docs/
│   ├── design-decisions.md
│   ├── oop-guide.md
│   ├── project-memory.md
│   └── requirements.md
└── src/
    └── main/java/edu/campusmarketplace/
        ├── Main.java
        ├── enums/       # ProductStatus, OrderStatus
        ├── model/       # User, Student, Wishlist, Product, Order, Category
        ├── repository/  # repository boundary and in-memory implementation
        ├── service/     # signup, listing, wishlist, checkout and cancellation rules
        └── ui/          # Swing authentication, marketplace, listings and orders screens
```
