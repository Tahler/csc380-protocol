package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.Serializer;
import edu.neumont.csc380.scalablesystem.io.Files;
import edu.neumont.csc380.scalablesystem.protocol.request.PutRequest;
import edu.neumont.csc380.scalablesystem.protocol.request.Request;
import edu.neumont.csc380.scalablesystem.protocol.request.UpdateRequest;
import edu.neumont.csc380.scalablesystem.protocol.response.*;
import edu.neumont.csc380.scalablesystem.protocol.serialization.RequestReader;
import edu.neumont.csc380.scalablesystem.protocol.serialization.ResponseWriter;
import edu.neumont.csc380.scalablesystem.repo.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import rx.Completable;
import rx.Single;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Node {
    private final RingNodeInfo info;
    private final RxHallaStor repository;
    private boolean running;

    public Node(String host, int port, LocalRepository localRepo, RingInfo info) {
        this.info = new RingNodeInfo(host, port);
        this.repository = new RingCoordinator(this.info, localRepo, info);
        this.running = false;
    }

    public Node(String host, int port) {
        this(host, port, new LocalRepository(), new RingInfo());
    }

    public void start() {
        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            ServerSocket server = new ServerSocket(this.info.port);
            LOGGER.info("Listening on port " + this.info.port);

            this.running = true;
            while (this.running) {
                Socket client = server.accept();
                LOGGER.debug("Received request.");

                new Thread(() -> this.handle(client)).start();
            }
        } catch (IOException e) {
            LOGGER.fatal("Failed to start server on port 3000.");
            LOGGER.fatal(e.getMessage());
        }
    }

    public void stop() {
        this.running = false;
    }

    private void handle(Socket client) {
        RequestReader requestReader = new RequestReader(client);
        Request request = requestReader.readRequest();
        this.handleRequest(request)
                .subscribe(response -> {
                    ResponseWriter responseWriter = new ResponseWriter(client);
                    responseWriter.writeResponse(response);
                });
    }

    private Single<Response> handleRequest(Request request) {
        Request.Type operation = request.getType();
        LOGGER.debug("Handling " + operation);
        Single<Response> response;
        switch (operation) {
            case CONTAINS_KEY:
                response = this.responseForContainsKey(request.getKey());
                break;
            case GET:
                response = this.responseForGet(request.getKey());
                break;
            case PUT:
                PutRequest putRequest = (PutRequest) request;
                response = this.responseForPut(putRequest.getKey(), putRequest.getValue());
                break;
            case UPDATE:
                UpdateRequest updateRequest = (UpdateRequest) request;
                response = this.responseForUpdate(updateRequest.getKey(), updateRequest.getValue());
                break;
            case DELETE:
                response = this.responseForDelete(request.getKey());
                break;
            default:
                throw new RuntimeException("Impossible request type: " + operation);
        }
        return response;
    }

    private Single<Response> responseForContainsKey(String key) {
        return this.repository.containsKey(key)
                .map(ContainsKeySuccessResponse::new);
    }

    private Single<Response> responseForPut(String key, Object value) {
        return Single.create(subscriber -> {
            this.repository.put(key, value)
                    .subscribe(
                            () -> subscriber.onSuccess(new PutSuccessResponse()),
                            err -> {
                                if (err instanceof KeyAlreadyExistsException) {
                                    subscriber.onSuccess(new KeyAlreadyExistsResponse());
                                } else if (err instanceof RepositoryFullException) {
                                    subscriber.onSuccess(new ServerFullResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    private Single<Response> responseForGet(String key) {
        return Single.create(subscriber -> {
            this.repository.get(key)
                    .subscribe(
                            val -> subscriber.onSuccess(new GetSuccessResponse(val)),
                            err -> {
                                if (err instanceof KeyDoesNotExistException) {
                                    subscriber.onSuccess(new KeyDoesNotExistResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    private Single<Response> responseForUpdate(String key, Object value) {
        return Single.create(subscriber -> {
            this.repository.update(key, value)
                    .subscribe(
                            () -> subscriber.onSuccess(new UpdateSuccessResponse()),
                            err -> {
                                if (err instanceof KeyDoesNotExistException) {
                                    subscriber.onSuccess(new KeyDoesNotExistResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    private Single<Response> responseForDelete(String key) {
        return Single.create(subscriber -> {
            this.repository.delete(key)
                    .subscribe(
                            () -> subscriber.onSuccess(new DeleteSuccessResponse()),
                            err -> {
                                if (err instanceof KeyDoesNotExistException) {
                                    subscriber.onSuccess(new KeyDoesNotExistResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("Can't parse host and port");
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        LOGGER = createLogger(port + ".log");
        LOGGER.info(String.format("Starting node at %s:%d...", host, port));

        Node node = new Node(host, port);

        if (args.length == 4) {
            String repoFile = args[2];
            Map<String, Object> repo = Serializer.consumeObjectFromTempFile(repoFile);

            String ringInfoFile = args[3];
            RingInfo ringInfo = Serializer.consumeObjectFromTempFile(ringInfoFile);

            preLoadNode(node, repo, ringInfo);
        }

        node.start();

        LOGGER.info("...started.");
    }

    private static void preLoadNode(Node node, Map<String, Object> repo, RingInfo ringInfo) {
        Collection<Completable> all = new ArrayList<>(repo.size());
        for (Map.Entry<String, Object> entry : repo.entrySet()) {
            Completable toDo = node.repository.put(entry.getKey(), entry.getValue());
            all.add(toDo);
        }
        Completable.merge(all).toObservable().toBlocking();
    }

    public static Logger LOGGER;
    private static Logger createLogger(String fileName) {
        String logFilePath = String.format("logs/%s", fileName);
        Files.deleteFileIfExists(logFilePath);

        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        builder.setStatusLevel(Level.ALL);
        builder.setConfigurationName(fileName);

        // create a file appender
        AppenderComponentBuilder appenderBuilder = builder
                .newAppender(fileName, "File")
                .addAttribute("fileName", logFilePath)
                .addAttribute("immediateFlush", true)
                .add(builder
                        .newLayout("PatternLayout")
                        .addAttribute("pattern", "%d [%t] %-5level: %msg%n"));
        builder.add(appenderBuilder);

        // create the new logger
        builder.add(builder
                .newLogger(fileName, Level.DEBUG)
                .add(builder.newAppenderRef(fileName))
                .addAttribute("additivity", false)
                .addAttribute("append", false));

        LoggerContext ctx = Configurator.initialize(builder.build());
        return ctx.getLogger(fileName);
    }

    @Override
    protected void finalize() throws Throwable {
        LOGGER.fatal("Stopping...");
        super.finalize();
    }
}

