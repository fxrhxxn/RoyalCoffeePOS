# Fix for 'getReceiptDetailsOnce' build error

The build error `Function 'getReceiptDetailsOnce' must have a body` was caused by the function being defined as a top-level function without an implementation body. In Room, `@Query` methods must be defined within a `@Dao` interface or abstract class. Additionally, the corresponding repository function was also defined outside the class, preventing it from accessing the required `receiptDao`.

## Changes Made

### Data Layer

#### [ReceiptDao.kt](file:///C:/Users/hassa/AndroidStudioProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/data/local/dao/ReceiptDao.kt)
- Moved the `getReceiptDetailsOnce` function inside the `ReceiptDao` interface. This allows Room to correctly associate the `@Query` annotation with the interface method.

#### [ReceiptRepository.kt](file:///C:/Users/hassa/AndroidStudioProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/data/local/repository/ReceiptRepository.kt)
- Moved the `getReceiptDetailsOnce` function inside the `ReceiptRepository` class. This resolved the `Unresolved reference 'receiptDao'` error and allowed the function to correctly delegate to the DAO.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:compileDebugKotlin` which now completes successfully.

```
$ ./gradlew :app:compileDebugKotlin
BUILD SUCCESSFUL in 5s
```
