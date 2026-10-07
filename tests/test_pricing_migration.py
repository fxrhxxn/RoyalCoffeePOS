"""Run the actual Kotlin migration SQL using host SQLite (no Android SDK required).
This checks data conversion and analytics, not Room's generated schema validation.
Run: python -m unittest discover -s tests -v
"""
from pathlib import Path
import re
import sqlite3
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/royalcoffee/pos'

class PricingMigrationTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(':memory:')
        self.db.executescript('''
            CREATE TABLE products(product_id INTEGER PRIMARY KEY, product_name TEXT, category TEXT, unit_price INTEGER, cost_price INTEGER);
            CREATE TABLE receipt_headers(receipt_id INTEGER PRIMARY KEY, subtotal INTEGER, discount INTEGER, tax INTEGER, total INTEGER, amount_paid INTEGER, change_given INTEGER, timestamp INTEGER, payment_method TEXT);
            CREATE TABLE receipt_details(receipt_id INTEGER, product_name TEXT, quantity INTEGER, unit_price INTEGER, cost_price INTEGER, line_total INTEGER);
            CREATE TABLE users(name TEXT); INSERT INTO users VALUES ('Original cashier');
            CREATE TABLE categories(name TEXT); INSERT INTO categories VALUES ('Coffee');
            INSERT INTO products VALUES(1,'Coffee','Coffee',99,30);
            INSERT INTO receipt_headers VALUES(1,99,0,14,113,NULL,NULL,1,'CARD');
            INSERT INTO receipt_details VALUES(1,'Coffee',1,99,NULL,99);
        ''')
        source = (JAVA / 'data/local/database/RoyalCoffeeDatabase.kt').read_text()
        block = source.split('val MIGRATION_9_10')[1].split('/**')[0]
        self.sql = re.findall(r'db.execSQL\("([^"\n]+)"\)', block)
        self.assertEqual(6, len(self.sql))
        with self.db:
            for statement in self.sql:
                self.db.execute(statement)

    def tearDown(self):
        self.db.close()

    def test_legacy_money_nulls_and_identity_preserved(self):
        self.assertEqual((9900,0,1400,11300,None,None,None,0), self.db.execute(
            'SELECT subtotal,discount,tax,total,amount_paid,change_given,discount_percent,vat_included FROM receipt_headers').fetchone())
        self.assertEqual((9900,3000), self.db.execute('SELECT unit_price,cost_price FROM products').fetchone())
        self.assertEqual((9900,None,9900,0), self.db.execute('SELECT unit_price,cost_price,line_total,discount_amount FROM receipt_details').fetchone())
        self.assertEqual('Original cashier', self.db.execute('SELECT name FROM users').fetchone()[0])
        self.assertEqual('Coffee', self.db.execute('SELECT name FROM categories').fetchone()[0])

    def test_dashboard_queries_use_net_line_revenue(self):
        self.db.execute('DELETE FROM receipt_headers')
        self.db.execute('DELETE FROM receipt_details')
        self.db.execute("INSERT INTO receipt_headers VALUES(2,9950,498,0,9452,10000,548,2,'CASH',5,1)")
        self.db.execute("INSERT INTO receipt_details VALUES(2,'Coffee',1,9950,3000,9950,498)")
        source = (JAVA / 'data/local/dao/ReceiptDao.kt').read_text()
        queries = dict((name, sql) for sql, name in re.findall(r'@Query\(\s*"""((?:(?!""").)*)"""\s*\)\s*fun (\w+)', source, re.S))
        def query(name):
            cur = self.db.execute(queries[name], {'start':0,'end':10,'limit':5})
            return dict(zip((d[0] for d in cur.description),cur.fetchone()))
        self.assertEqual(9452, query('getSalesSummary')['totalSales'])
        self.assertEqual(9452, query('getCategorySales')['totalRevenue'])
        self.assertEqual(9452, query('getGrossProfitStats')['knownCostRevenue'])
        self.assertEqual(6452, query('getProfitTrend')['totalGrossProfit'])
        self.assertEqual(6452, query('getMostProfitableProducts')['totalGrossProfit'])

if __name__ == '__main__':
    unittest.main()
