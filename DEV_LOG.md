# Royal Coffee POS - Development Log

## Current Status

Completed:
- Android Studio project created
- Kotlin + Jetpack Compose project running
- Landscape orientation configured
- Royal green and gold theme added
- Login screen created
- Fake login added
    - admin / 1234
    - barista / 1234
- Role-based side menu working
    - Admin sees Sales, Dashboard, Inventory, Settings
    - Barista sees Sales and Settings only
- Sales/POS screen created
- Product cards displayed
- Product click adds item to ticket/cart
- Repeated product click increases quantity
- + and - controls update quantity
- Subtotal, tax, and total are calculated
- Charge button updates based on cart total

## Current Test Credentials

Admin:
username: admin
PIN: 1234

Barista:
username: barista
PIN: 1234

## Next Step

Next task:
- Implement category filtering

Expected behaviour:
- Coffee button shows only coffee products
- Cold button shows only cold products
- Hot button shows only hot products
- Pastries button shows only pastry products
- Add an All button to show all products

## Known Issues / Improvements Later

- Cart panel may need scrolling when many products are added
- Product cards currently use placeholder images
- Login is fake and will later be replaced with Room database authentication
- Products are temporary sample data and will later come from Room database
- Payment screen is not implemented yet