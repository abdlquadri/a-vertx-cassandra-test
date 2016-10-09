import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import ng.abdlquadri.pastes.PasteVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by abdlquadri on 10/9/16.
 */

@RunWith(VertxUnitRunner.class)
public class PastesAPITest {

    private final static int PORT = 8084;
    private Vertx vertx;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        final DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                );

        PasteVerticle verticle = new PasteVerticle();

        vertx.deployVerticle(verticle, options,
                context.asyncAssertSuccess());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 3000L)
    public void testNewEntryViaJSON(TestContext context) throws Exception {

        //If entry is public vertx.EventBus().publish()

        context.assertTrue(false);
    }

    @Test(timeout = 3000L)
    public void testNewEntryViaHTMLForm(TestContext context) throws Exception {

        //If entry is public vertx.EventBus().publish()

        context.assertTrue(false);
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
