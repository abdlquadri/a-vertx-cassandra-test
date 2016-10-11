package ng.abdlquadri.pastes;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import ng.abdlquadri.pastes.entity.Entry;
import ng.abdlquadri.pastes.service.EntryService;
import ng.abdlquadri.pastes.service.impl.EntryServiceCasandraImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by abdlquadri on 10/9/16.
 */
public class EntryVerticle extends AbstractVerticle {

    private Router router;
    private EntryService entryServiceCasandra;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        entryServiceCasandra = new EntryServiceCasandraImpl();

        initializeDataStore();

        router = Router.router(vertx);


        router.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT));

        allowCORSRequests();

        setUpRouteHandlers();

        //Start the REST Server
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port"), result -> {
                    if (result.succeeded())
                        startFuture.complete();
                    else
                        startFuture.fail(result.cause());
                });
    }

    private void setUpRouteHandlers() {

        router.route().handler(BodyHandler.create());
        router.get(Constants.API_GET).handler(this::handleGetOne);
        router.get(Constants.API_LIST_ALL).handler(this::handleGetAll);
        router.post(Constants.API_CREATE).handler(this::handleCreateEntry);
        router.patch(Constants.API_UPDATE).handler(this::handleUpdateEntry);
        router.delete(Constants.API_DELETE).handler(this::handleDeleteOne);
        router.delete(Constants.API_DELETE_ALL).handler(this::handleDeleteAll);
    }

    private void handleDeleteAll(RoutingContext routingContext) {
        entryServiceCasandra.deleteAll().setHandler(result -> {
            if (result.succeeded()) {
                if (result.result()) {
                    routingContext.response().setStatusCode(204).end();
                } else {
                    serviceUnavailable(routingContext);
                }
            } else {
                serviceUnavailable(routingContext);
            }

        });
    }

    private void handleDeleteOne(RoutingContext routingContext) {
//        entryService.delete()
    }

    private void handleUpdateEntry(RoutingContext routingContext) {

    }

    private void handleCreateEntry(RoutingContext routingContext) {

        JsonObject bodyAsJson = routingContext.getBodyAsJson();
        if (!validateParams(bodyAsJson)) {
            badRequest(routingContext);
            return;
        }

        Entry entry = new Entry(bodyAsJson);

        JsonObject createResponse = new JsonObject();
        entryServiceCasandra.insert(entry).setHandler(result -> {
            if (result.result()) {
                routingContext.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .end(createResponse.encode());

                if (entry.isVisible()) {
                    vertx.eventBus().publish(Constants.ADDRESS_PUBLIC_ENTRY, Json.encodePrettily(entry));

                }
            } else {
                serviceUnavailable(routingContext);
            }

        });


    }

    private boolean validateParams(JsonObject body) {
        return body.containsKey("body");
    }

    private void handleGetAll(RoutingContext routingContext) {

    }

    private void handleGetOne(RoutingContext routingContext) {

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void notFound(RoutingContext context) {
        context.response().setStatusCode(404).end();
    }

    private void badRequest(RoutingContext context) {
        context.response().setStatusCode(400).end();
    }

    private void serviceUnavailable(RoutingContext context) {
        context.response().setStatusCode(503).end();
    }

    private void allowCORSRequests() {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));
    }

    private void initializeDataStore() {
        entryServiceCasandra.initializeDatastore().setHandler(result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
            }
        });

    }

}