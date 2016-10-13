import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import ng.abdlquadri.pastes.Constants;
import ng.abdlquadri.pastes.EntryVerticle;
import ng.abdlquadri.pastes.entity.Entry;
import ng.abdlquadri.pastes.service.impl.EntryServiceCasandraImpl;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static ng.abdlquadri.pastes.service.impl.EntryServiceCasandraImpl.session;

/**
 * Created by abdlquadri on 10/9/16.
 */

@RunWith(VertxUnitRunner.class)
public class PastesAPITest {

    private final static int PORT = 8080;
    private final static String SERVER = "127.0.0.1";
    private Vertx vertx;
    private static final String id = UUIDs.random().toString();
    private static final String salt = new SecureRandomNumberGenerator().nextBytes().toHex();
    private static final String secret = new Sha512Hash("An Entry Body Text Dump FORM", salt, 300000).toHex();
    private static final Instant now = Instant.now();
    private static final Instant expires = now.plus(30, ChronoUnit.DAYS);

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        final DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", PORT)
                ).setWorker(true);

        EntryVerticle verticle = new EntryVerticle();

        vertx.deployVerticle(verticle, options, context.asyncAssertSuccess());
    }

    @After
    public void after(TestContext context) {

        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testDeleteEntry(TestContext context) throws Exception {


        HttpClient client = vertx.createHttpClient();
        Async async = context.async();

        client.delete(PORT, SERVER, "/entries/" + id, response -> {
            context.assertEquals(204, response.statusCode());
            client.close();
            async.complete();
        }).putHeader("content-type", "application/json")
                .putHeader("x-secret", "mnnv")//this may break some clients, we can pass a second param
                .end();

    }

    @Test
    public void testEditEntry(TestContext context) throws Exception {

        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String requestURI = "/entries/668afb50-9095-11e6-94cb-f151169f5d76"; //this id should be created first

        EntryServiceCasandraImpl entryServiceCasandra = new EntryServiceCasandraImpl();
        Entry payload = new Entry("668afb50-9095-11e6-94cb-f151169f5d76", "An Entry Body Text Dump JSON", "An Entry Title JSON", expires.getEpochSecond(), true, secret, now.getEpochSecond());
        entryServiceCasandra.insert(payload);

        client.get(PORT, SERVER, requestURI, response -> {

            response.bodyHandler(buffer -> {
                JsonObject entryUpdated = buffer.toJsonObject().put("body", "Overridden");//override body field;

                client.put(PORT, SERVER, requestURI, response2 -> {
                    context.assertEquals(200, response2.statusCode());
                    client.close();
                    async.complete();
                }).putHeader("content-type", "application/json").end(entryUpdated.encode());
            });
        }).putHeader("content-type", "application/json")
                .end();

    }


    @Test
    public void testPaginatedListEntries(TestContext context) throws Exception {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();

        client.get(PORT, SERVER, "/entries", response -> {
            context.assertEquals(200, response.statusCode());
            client.close();
            async.complete();
        }).putHeader("content-type", "application/json")
                .end();
    }

    @Test
    public void testGetOneEntry(TestContext context) throws Exception {

        HttpClient client = vertx.createHttpClient();
        Async async = context.async();

        Instant now = Instant.now();
        Instant expires = now.plus(30, ChronoUnit.DAYS);

        UUID uuid = UUIDs.timeBased();

        Entry payload = new Entry(uuid.toString(), "An Entry Body Text Dump JSON", "An Entry Title JSON", expires.getEpochSecond(), true, secret, now.getEpochSecond());

        client.post(PORT, SERVER, "/entries", response -> {

            client.get(PORT, SERVER, "/entries/" + uuid, response2 -> {
                context.assertEquals(200, response2.statusCode());
                client.close();
                async.complete();
            }).putHeader("content-type", "application/json")
                    .end();

        }).putHeader("content-type", "application/json")
                .end(Json.encode(payload));


    }

    @Test
    public void testNewEntryViaJSON(TestContext context) throws Exception {


        HttpClient client = vertx.createHttpClient();
        Async async = context.async();

        String id = UUIDs.random().toString();

        String salt = new SecureRandomNumberGenerator().nextBytes().toHex();
        String secret = new Sha512Hash("An Entry Body Text Dump JSON", salt, 300000).toHex();

        Instant now = Instant.now();
        Instant expires = now.plus(30, ChronoUnit.DAYS);

        Entry entry = new Entry(id, "An Entry Body Text Dump JSON", "An Entry Title JSON", expires.getEpochSecond(), true, secret, now.getEpochSecond());

        client.post(PORT, SERVER, Constants.API_CREATE, response -> {
            context.assertEquals(201, response.statusCode());
            client.close();
            async.complete();
        }).putHeader("content-type", "application/json").end(Json.encodePrettily(entry));

    }

    @Test
    public void testNewEntryViaHTMLForm(TestContext context) throws Exception {

        HttpClient client = vertx.createHttpClient();
        Async async = context.async();


        Instant now = Instant.now();
        Instant expires = now.plus(30, ChronoUnit.DAYS);

        String formData = "id=" + id +
                "&body=An Entry Body Text Dump FORM" +
                "&title=An Entry Title FORM" +
                "&expires=" + expires.getEpochSecond() +
                "&creationDate=" + now.getEpochSecond() +
                "&visible=" + true +
                "&secret=" + secret;

        client.post(PORT, SERVER, Constants.API_CREATE, response -> {
            context.assertEquals(201, response.statusCode());
            client.close();
            async.complete();
        }).putHeader("content-type", "application/x-www-form-urlencoded").end(formData);

    }


}
