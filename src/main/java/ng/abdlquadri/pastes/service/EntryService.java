package ng.abdlquadri.pastes.service;

import io.vertx.core.Future;
import ng.abdlquadri.pastes.entity.Entry;

import java.util.List;
import java.util.Optional;

/**
 * Created by abdlquadri on 10/9/16.
 */
public interface EntryService {

    Future<Boolean> initializeDatastore();

    Future<Boolean> insert(Entry entry);

    Future<List<Entry>> getAll();

    Future<Optional<Entry>> get(String entryID);

    Future<Entry> update(String entryId, Entry newEntry);

    Future<Boolean> delete(String entryId, String secret);

    Future<Boolean> deleteAll();

    Future<Boolean> closeDatastore();
}
