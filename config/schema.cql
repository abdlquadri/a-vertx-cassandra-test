CREATE KEYSPACE IF NOT EXISTS entriesP WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 1 };

DROP TABLE IF EXISTS entriesP.entry;
DROP TABLE IF EXISTS entriesP.entryPublic;

CREATE TABLE IF NOT EXISTS entriesP.entry (entry_id TEXT, body TEXT,title TEXT, creation_date TIMESTAMP,
 expires TIMESTAMP, publicly_visible BOOLEAN, secret TEXT, PRIMARY KEY(entry_id, secret));

CREATE TABLE IF NOT EXISTS entriesP.entryPublic (entry_id TEXT, body TEXT, title TEXT, creation_date TIMESTAMP,
expires TIMESTAMP, publicly_visible BOOLEAN, secret TEXT, PRIMARY KEY(publicly_visible, creation_date, entry_id, secret));

--import data from csv