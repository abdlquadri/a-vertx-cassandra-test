import com.datastax.driver.core.utils.UUIDs;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import ng.abdlquadri.pastes.Constants;
import ng.abdlquadri.pastes.EntryVerticle;
import ng.abdlquadri.pastes.entity.Entry;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Created by abdlquadri on 10/9/16.
 */

@RunWith(VertxUnitRunner.class)
public class PastesAPITest {

    private final static int PORT = 8080;
    private final static String SERVER = "127.0.0.1";
    private Vertx vertx;


    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        final DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", PORT)
                ).setWorker(true);

        EntryVerticle verticle = new EntryVerticle();

        vertx.deployVerticle(verticle, options,
                context.asyncAssertSuccess());
    }

    @After
    public void after(TestContext context) {

        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 5000L)
    public void testNewEntryViaJSON(TestContext context) throws Exception {

        //If entry is public vertx.EventBus().publish()

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

    @Test(timeout = 5000L)
    public void testNewEntryViaHTMLForm(TestContext context) throws Exception {

        //If entry is public vertx.EventBus().publish()


        HttpClient client = vertx.createHttpClient();
        Async async = context.async();

        UUID id = UUIDs.random();

        String salt = new SecureRandomNumberGenerator().nextBytes().toHex();
        String secret = new Sha512Hash("An Entry Body Text Dump FORM", salt, 300000).toHex();

        Instant now = Instant.now();
        Instant expires = now.plus(30, ChronoUnit.DAYS);

        String formData = "id=" + id +
                "&body=An Entry Body Text Dump FORM" +
                "&title=An Entry Title FORM" +
                "&expires=" + expires.getEpochSecond() +
                "&creationDate=" + now.getEpochSecond() +
                "&visible=" + true +
                "&secret" + secret;
        String formDataEncoded = URLEncoder.encode(formData, "UTF-8");

        client.post(PORT, SERVER, Constants.API_CREATE, response -> {
            context.assertEquals(201, response.statusCode());
            client.close();
            async.complete();
        }).putHeader("content-type", "application/x-www-form-urlencoded").end(formDataEncoded);

    }

    @Test(timeout = 3000L)
    public void testPaginatedListEntries(TestContext context) throws Exception {

        context.assertTrue(false);
    }

    @Test(timeout = 3000L)
    public void testEditEntry(TestContext context) throws Exception {

        context.assertTrue(false);
    }

    @Test(timeout = 3000L)
    public void testDeleteEntry(TestContext context) throws Exception {

        context.assertTrue(false);
    }

}
