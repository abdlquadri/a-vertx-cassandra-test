package ng.abdlquadri.pastes.service.impl;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import ng.abdlquadri.pastes.Constants;
import ng.abdlquadri.pastes.entity.Entry;
import ng.abdlquadri.pastes.service.EntryService;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by abdlquadri on 10/9/16.
 */
public class EntryServiceCasandraImpl implements EntryService {

    private static Cluster cluster;
    private static Session session;

    @Override
    public Future<Boolean> initializeDatastore() {
        Future<Boolean> future = Future.future();

        cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .build();

        Metadata metadata = cluster.getMetadata();

        System.out.printf("Connected to %s\n", metadata.getClusterName());

        for (Host host : metadata.getAllHosts()) {
            System.out.printf("Host: %s \n", host.getAddress());
        }

        session = cluster.connect();


        session.execute("CREATE KEYSPACE IF NOT EXISTS entriesP " +
                "WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 1 };");

        session.execute("DROP TABLE IF EXISTS entriesP.entry");

        session.execute("CREATE TABLE IF NOT EXISTS entriesP.entry (" +
                "entry_id TEXT, body TEXT, " +
                "title TEXT, creation_date TIMESTAMP, expires TIMESTAMP, " +
                "publicly_visible BOOLEAN, secret TEXT, " +
                "PRIMARY KEY(entry_id, secret));");

        future.complete(session != null);

        return future;

    }

    @Override
    public Future<Boolean> insert(Entry entry) {

//    * `body` - contents
//    * `title` - optional name/title
//    * `expires` - optional entry exiration timestamp
//    * `private` - optional flag indicating that entry is private and shouldn't be added to list of entries

        Future<Boolean> future = Future.future();
        boolean result = false;

        PreparedStatement preparedEntryInsert = session.prepare("INSERT INTO entriesP.entry " +
                "(entry_id, secret, body, title,creation_date, expires, publicly_visible)" +
                "VALUES" +
                "(?,?,?,?,?,?,?) IF NOT EXISTS");
        System.out.println(entry.getSecret());
        BoundStatement boundEntryInsert = new BoundStatement(preparedEntryInsert);
        boundEntryInsert
                .bind()
                .setString("entry_id", entry.getId())
                .setString("secret", entry.getSecret())
                .setString("body", entry.getBody())
                .setString("title", entry.getTitle())
                .setTimestamp("creation_date", new Date(entry.getCreationDate()))
                .setTimestamp("expires", new Date(entry.getExpires()))
                .setBool("publicly_visible", entry.isVisible())
        ;

        session.execute(boundEntryInsert);

        result = true;
        future.complete(result);
        return future;
    }

    @Override
    public Future<List<Entry>> getAll() {

        Future<List<Entry>> future = Future.future();

        PreparedStatement preparedEntryDelete = session.prepare("SELECT * FROM entriesP.entry " +
                "ORDER BY creation_date DESC" +
                "WHERE publicly_visible=? ");

        BoundStatement boundEntryDelete = new BoundStatement(preparedEntryDelete);
        boundEntryDelete
                .bind()
                .setBool("publicly_visible", true)
        ;

        session.execute(boundEntryDelete);

        future.complete();
        return future;
    }

    @Override
    public Future<Optional<Entry>> get(String entryID) {
        return null;
    }

    @Override
    public Future<Entry> update(String entryId, Entry newEntry) {
        Future<Entry> future = Future.future();

        PreparedStatement preparedEntryUpdate = session.prepare("UPDATE entriesP.entry " +
                "SET body=?, title=?, publicly_visible=? WHERE entry_id=? AND secret=?");

        BoundStatement boundEntryUpdate = new BoundStatement(preparedEntryUpdate);
        boundEntryUpdate
                .bind()
                .setString("body", newEntry.getBody())
                .setString("title", newEntry.getTitle())
                .setBool("publicly_visible", newEntry.isVisible())
                .setString("entry_id", newEntry.getId())
                .setString("secret", newEntry.getSecret())
        ;

        session.execute(boundEntryUpdate);

        future.complete(newEntry);
        return future;
    }

    @Override
    public Future<Boolean> delete(String entryId, String secret) {
        Future<Boolean> future = Future.future();
        Boolean result = false;
        PreparedStatement preparedEntryDelete = session.prepare("DELETE FROM entriesP.entry " +
                "WHERE entry_id=? AND secret=?");

        BoundStatement boundEntryDelete = new BoundStatement(preparedEntryDelete);
        boundEntryDelete
                .bind()
                .setString("entry_id", entryId)
                .setString("secret", secret)
        ;

        session.execute(boundEntryDelete);

        result = true;
        future.complete(result);
        return future;
    }

    @Override
    public Future<Boolean> deleteAll() {
        return null;
    }

    @Override
    public Future<Boolean> closeDatastore() {
        Future<Boolean> future = Future.future();


        session.close();


        return future;
    }
}
