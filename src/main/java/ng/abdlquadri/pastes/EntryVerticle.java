package ng.abdlquadri.pastes;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import ng.abdlquadri.pastes.entity.Entry;
import ng.abdlquadri.pastes.service.EntryService;
import ng.abdlquadri.pastes.service.impl.EntryServiceCasandraImpl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static ng.abdlquadri.pastes.Constants.ADDRESS_PUBLIC_ENTRY;
import static ng.abdlquadri.pastes.util.Util.paramsToJSON;

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


        router.route().handler(BodyHandler.create());
        router.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT));

        allowCORSRequests();

        setUpRouteHandlers();

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions();

        PermittedOptions outboundPermitted = new PermittedOptions().setAddress(ADDRESS_PUBLIC_ENTRY);
        options.addOutboundPermitted(outboundPermitted);
        sockJSHandler.bridge(options);

        router.route("/latest/*").handler(sockJSHandler);


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

        router.get(Constants.API_GET).handler(this::handleGetOne);
        router.get(Constants.API_LIST_ALL).handler(this::handleGetAll);
        router.post(Constants.API_CREATE).handler(this::handleCreateEntry);
        router.put(Constants.API_UPDATE).handler(this::handleUpdateEntry);
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

        MultiMap params = routingContext.request().params();
        String entryId = params.get("entryId");
        String secret = routingContext.request().getHeader("x-secret");
        entryServiceCasandra.delete(entryId, secret).setHandler(result -> {
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

    private void handleUpdateEntry(RoutingContext routingContext) {
        JsonObject bodyAsJson;

        if (routingContext.request().getHeader("content-type").equals("application/x-www-form-urlencoded")) {
            bodyAsJson = paramsToJSON(routingContext.request().formAttributes());

        } else {
            bodyAsJson = routingContext.getBodyAsJson();

        }

        System.out.println(bodyAsJson);

        Entry entry = new Entry(bodyAsJson);
        String secret = routingContext.request().getHeader("x-secret");
        entry.setSecret(secret);
        entryServiceCasandra.update(entry.getId(), entry).setHandler(result -> {
            if (result.result() != null) {
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end();

            } else {
                serviceUnavailable(routingContext);
            }

        });

    }

    private void handleCreateEntry(RoutingContext routingContext) {
        JsonObject bodyAsJson = new JsonObject();

        if (routingContext.request().getHeader("content-type").equals("application/x-www-form-urlencoded")) {

            bodyAsJson = paramsToJSON(routingContext.request().formAttributes());

            System.out.println(routingContext.getBodyAsString());
        } else {
            bodyAsJson = routingContext.getBodyAsJson();

        }

        System.out.println(bodyAsJson);

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
                        .end(createResponse.put("secret", entry.getSecret()).encode());

                if (entry.isVisible()) {
                    vertx.eventBus().publish(ADDRESS_PUBLIC_ENTRY, Json.encodePrettily(entry));

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

        entryServiceCasandra.getAll().setHandler(result -> {
            if (result.succeeded()) {
                if (result == null) {
                    serviceUnavailable(routingContext);

                } else if (result.result().isEmpty()) {
                    notFound(routingContext);
                } else {

                    String entries = Json.encodePrettily(result.result());
                    routingContext.response().setStatusCode(200).end(entries);
                }
            } else {
                serviceUnavailable(routingContext);
            }

        });
    }

    private void handleGetOne(RoutingContext routingContext) {
        MultiMap params = routingContext.request().params();
        String entryId = params.get("entryId");
        entryServiceCasandra.get(entryId).setHandler(result -> {
            if (result.succeeded()) {
                if (result == null) {
                    serviceUnavailable(routingContext);

                } else {
                    Optional<Entry> res = result.result();

                    if (!res.isPresent()) {
                        notFound(routingContext);
                        return;
                    }

                    String entry = Json.encodePrettily(res.get());
                    routingContext.response().setStatusCode(200).end(entry);
                }
            } else {
                serviceUnavailable(routingContext);
            }

        });
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
        allowMethods.add(HttpMethod.PUT);

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
