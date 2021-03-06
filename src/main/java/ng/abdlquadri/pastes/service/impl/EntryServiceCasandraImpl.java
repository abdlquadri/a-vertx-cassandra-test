package ng.abdlquadri.pastes.service.impl;

import com.datastax.driver.core.*;
import io.vertx.core.Future;
import ng.abdlquadri.pastes.entity.Entry;
import ng.abdlquadri.pastes.service.EntryService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by abdlquadri on 10/9/16.
 */
public class EntryServiceCasandraImpl implements EntryService {

    public static Cluster cluster;
    public static Session session;

    @Override
    public Future<Boolean> initializeDatastore(String cassandraHost) {
        Future<Boolean> future = Future.future();

        cluster = Cluster.builder()
                .addContactPoint(cassandraHost)
                .build();

        Metadata metadata = cluster.getMetadata();

        System.out.printf("Connected to %s\n", metadata.getClusterName());

        for (Host host : metadata.getAllHosts()) {
            System.out.printf("Host: %s \n", host.getAddress());
        }

        session = cluster.connect();


        session.execute("CREATE KEYSPACE IF NOT EXISTS entriesP " +
                "WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 1 };");

//
        session.execute("CREATE TABLE IF NOT EXISTS entriesP.entry (" +
                "entry_id TEXT, body TEXT, " +
                "title TEXT, creation_date TIMESTAMP, expires TIMESTAMP, " +
                "publicly_visible BOOLEAN, secret TEXT, " +
                "PRIMARY KEY(entry_id, secret));");//design for query, cassandra is interesting

        session.execute("CREATE TABLE IF NOT EXISTS entriesP.entryPublic (" +
                "entry_id TEXT, body TEXT, " +
                "title TEXT, creation_date TIMESTAMP, expires TIMESTAMP, " +
                "publicly_visible BOOLEAN, secret TEXT, " +
                "PRIMARY KEY(publicly_visible, creation_date, entry_id, secret));");//design for query, cassandra is interesting

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
                .setTimestamp("creation_date", entry.getCreationDate())
                .setTimestamp("expires", entry.getExpires())
                .setBool("publicly_visible", entry.isVisible())
        ;

        ResultSet resultSet = session.execute(boundEntryInsert);

        result = resultSet.wasApplied();
        future.complete(result);
        return future;
    }

    @Override
    public Future<List<Entry>> getAll() {

        Future<List<Entry>> future = Future.future();

        PreparedStatement preparedEntryDelete = session.prepare("SELECT * FROM entriesP.entryPublic " +
                " WHERE publicly_visible=? " +
                "ORDER BY creation_date DESC ALLOW FILTERING"
        );

        BoundStatement boundEntryDelete = new BoundStatement(preparedEntryDelete);
        boundEntryDelete
                .bind()
                .setBool("publicly_visible", true)
        ;

        boundEntryDelete.setFetchSize(100);//we could include a parameter to allow clients set this

        ResultSet resultSet = session.execute(boundEntryDelete);

        PagingState pagingState = resultSet.getExecutionInfo().getPagingState();


        List<Entry> entries = resultSet.all().stream()
                .map(Entry::new)
                .collect(Collectors.toList());

        future.complete(entries);
        return future;
    }

    @Override
    public Future<Optional<Entry>> get(String entryID) {
        Future<Optional<Entry>> future = Future.future();

        PreparedStatement preparedEntryDelete = session.prepare("SELECT * FROM entriesP.entry " +
                " WHERE entry_id=? "
        );

        BoundStatement boundEntryDelete = new BoundStatement(preparedEntryDelete);
        boundEntryDelete
                .bind()
                .setString("entry_id", entryID)
        ;

        ResultSet resultSet = session.execute(boundEntryDelete);

        Row row = resultSet.one();

        if (row == null) {
            future.complete(null);
        }

        Entry entry = new Entry();
        entry.setId(row.getString("entry_id"));
        entry.setBody(row.getString("body"));
        entry.setTitle(row.getString("title"));
        entry.setExpires(row.getTimestamp("expires"));
        entry.setVisible(row.getBool("publicly_visible"));
        entry.setSecret(row.getString("secret"));
        entry.setCreationDate(row.getTimestamp("creation_date"));

        future.complete(Optional.ofNullable(entry));
        return future;

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

        ResultSet resultSet = session.execute(boundEntryUpdate);

        if(resultSet.wasApplied()){

        future.complete(newEntry);}
        else {
            future.complete(null);
        }
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

        ResultSet resultSet = session.execute(boundEntryDelete);

        result = resultSet.wasApplied();
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
