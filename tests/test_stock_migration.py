"""Execute the shipping migration SQL against SQLite, preserving existing stock."""
import re
import sqlite3
import unittest
from pathlib import Path

class StockMigrationTest(unittest.TestCase):
    def test_existing_units_have_unknown_expiry_and_zero_stock_has_no_batch(self):
        db = sqlite3.connect(':memory:')
        db.execute('CREATE TABLE products(product_id INTEGER PRIMARY KEY, stock_quantity INTEGER)')
        db.executemany('INSERT INTO products VALUES (?,?)', [(1, 12), (2, 0), (3, 4)])
        source = Path('app/src/main/java/com/royalcoffee/pos/data/local/database/RoyalCoffeeDatabase.kt').read_text()
        block = source.split('val MIGRATION_10_11 =', 1)[1].split('/**', 1)[0]
        sql = re.findall(r'db.execSQL\("([^"]+)"\)', block)
        self.assertEqual(len(sql), 4)
        for statement in sql:
            db.execute(statement)
        self.assertEqual(db.execute('SELECT productId,remaining,expiryDay FROM stock_batches ORDER BY productId').fetchall(), [(1,12,None),(3,4,None)])
        self.assertEqual(db.execute('SELECT SUM(stock_quantity) FROM products').fetchone()[0], 16)
        db.execute('INSERT INTO stock_batches(productId,remaining,expiryDay,receivedAt) VALUES (1,2,20000,1)')
        self.assertEqual(db.execute('SELECT expiryDay FROM stock_batches WHERE productId=1 ORDER BY expiryDay IS NULL,expiryDay,receivedAt,id').fetchall(), [(20000,), (None,)])

if __name__ == '__main__': unittest.main()
