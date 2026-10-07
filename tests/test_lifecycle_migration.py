"""Run the actual lifecycle migration and verify deletion cannot erase history."""
import re
import sqlite3
import unittest
from pathlib import Path

class LifecycleMigrationTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(':memory:')
        self.db.execute('PRAGMA foreign_keys=ON')
        self.db.executescript('''
          CREATE TABLE products(product_id INTEGER PRIMARY KEY, stock_quantity INTEGER);
          CREATE TABLE receipt_details(receipt_detail_id INTEGER PRIMARY KEY, product_name TEXT);
          CREATE TABLE inventory_history(history_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, product_id INTEGER NOT NULL, product_name_snapshot TEXT NOT NULL, transaction_type TEXT NOT NULL, quantity_change INTEGER NOT NULL, previous_stock INTEGER NOT NULL, new_stock INTEGER NOT NULL, reason TEXT, timestamp INTEGER NOT NULL, FOREIGN KEY(product_id) REFERENCES products(product_id) ON DELETE CASCADE);
          CREATE INDEX index_inventory_history_product_id ON inventory_history(product_id);
          CREATE INDEX index_inventory_history_timestamp ON inventory_history(timestamp);
          INSERT INTO products VALUES(1,0),(2,0);
          INSERT INTO receipt_details VALUES(1,'Old renamed item');
          INSERT INTO inventory_history VALUES(1,1,'Original name','SALE',-1,1,0,'Saved sale',12345);
        ''')
        source = Path('app/src/main/java/com/royalcoffee/pos/data/local/database/RoyalCoffeeDatabase.kt').read_text()
        block = source.split('val MIGRATION_11_12 =',1)[1].split('val MIGRATION_10_11',1)[0]
        sql = re.findall(r'db.execSQL\("([^"]+)"\)', block)
        self.assertEqual(len(sql),10)
        for statement in sql: self.db.execute(statement)

    def test_preserves_history_and_blocks_cascading_delete(self):
        self.assertEqual(self.db.execute('SELECT * FROM inventory_history').fetchall(), [(1,1,'Original name','SALE',-1,1,0,'Saved sale',12345)])
        with self.assertRaises(sqlite3.IntegrityError): self.db.execute('DELETE FROM products WHERE product_id=1')
        self.assertEqual(self.db.execute('SELECT product_id FROM receipt_details').fetchall(), [(None,)])
        self.assertEqual(self.db.execute('SELECT legacy_history_uncertain FROM products').fetchall(), [(1,),(1,)])

    def test_new_products_can_be_deleted_until_referenced(self):
        self.db.execute('INSERT INTO products(product_id,stock_quantity) VALUES(3,0),(4,0)')
        self.assertEqual(self.db.execute('SELECT legacy_history_uncertain FROM products WHERE product_id=3').fetchone()[0],0)
        self.db.execute('DELETE FROM products WHERE product_id=3')
        self.db.execute("INSERT INTO receipt_details VALUES(2,'New product',4)")
        with self.assertRaises(sqlite3.IntegrityError): self.db.execute('DELETE FROM products WHERE product_id=4')

if __name__ == '__main__': unittest.main()
