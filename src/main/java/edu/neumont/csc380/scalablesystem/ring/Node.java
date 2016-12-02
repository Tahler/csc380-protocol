package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.Serializer;
import edu.neumont.csc380.scalablesystem.io.Files;
import edu.neumont.csc380.scalablesystem.logging.Env;
import edu.neumont.csc380.scalablesystem.protocol.request.PutRequest;
import edu.neumont.csc380.scalablesystem.protocol.request.Request;
import edu.neumont.csc380.scalablesystem.protocol.request.UpdateRequest;
import edu.neumont.csc380.scalablesystem.protocol.response.*;
import edu.neumont.csc380.scalablesystem.protocol.serialization.RequestReader;
import edu.neumont.csc380.scalablesystem.protocol.serialization.ResponseWriter;
import edu.neumont.csc380.scalablesystem.ring.repo.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import rx.Single;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Node {
    private final RingNodeInfo info;
    private final LocalRepository localRepo;
    private final RingCoordinator ringRepo;
    private boolean running;

    public Node(String host, int port, RingInfo info) {
        Env.LOGGER.debug("INSTANTIATING NODE");
        this.info = new RingNodeInfo(host, port);
        VnodeRepository vnodeRepo = new VnodeRepository(this.info);
        this.localRepo = new LocalRepository(vnodeRepo);
        this.ringRepo = new RingCoordinator(vnodeRepo, info);
        this.running = false;
        Env.LOGGER.debug("Created node at " + this.info);
    }

    public void start() {
        new Thread(this::listen).start();
    }

    private void listen() {
        this.running = true;
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(this.info.port);
                Env.LOGGER.info("Listening on port " + this.info.port);

                while (this.running) {
                    Socket client = server.accept();
                    Env.LOGGER.debug("Received request.");

                    new Thread(() -> this.handleRequest(client)).start();
                }
            } catch (IOException e) {
                Env.LOGGER.fatal("Failed to start server on port 3000.");
                Env.LOGGER.fatal(e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                ServerSocket intercomServer = new ServerSocket(this.info.intercomPort);
                Env.LOGGER.info("Listening for intercom on port " + this.info.intercomPort);

                while (this.running) {
                    Socket client = intercomServer.accept();
                    Env.LOGGER.debug("Received intercom request.");

                    new Thread(() -> this.handleIntercomRequest(client)).start();
                }
            } catch (IOException e) {
                Env.LOGGER.fatal("Failed to start server on port 3001.");
                Env.LOGGER.fatal(e.getMessage());
            }
        }).start();
    }

    public void stop() {
        this.running = false;
    }

    private void handleRequest(Socket client) {
        RequestReader requestReader = new RequestReader(client);
        Request request = requestReader.readRequest();
        this.handleRequest(this.ringRepo, request)
                .subscribe(response -> {
                    ResponseWriter responseWriter = new ResponseWriter(client);
                    responseWriter.writeResponse(response);
                });
    }

    private void handleIntercomRequest(Socket client) {
        RequestReader requestReader = new RequestReader(client);
        Env.LOGGER.debug("reading intercom request...");
        Request request = requestReader.readRequest();
        Env.LOGGER.debug("...done.");
        this.handleRequest(this.localRepo, request)
                .subscribe(response -> {
                    ResponseWriter responseWriter = new ResponseWriter(client);
                    responseWriter.writeResponse(response);
                });
    }

    private Single<Response> handleRequest(RxHallaStor repo, Request request) {
        Request.Type operation = request.getType();
        Env.LOGGER.debug("Handling " + operation);
        Single<Response> response;
        switch (operation) {
            case CONTAINS_KEY:
                response = this.responseForContainsKey(repo, request.getKey());
                break;
            case GET:
                response = this.responseForGet(repo, request.getKey());
                break;
            case PUT:
                PutRequest putRequest = (PutRequest) request;
                response = this.responseForPut(repo, putRequest.getKey(), putRequest.getValue());
                break;
            case UPDATE:
                UpdateRequest updateRequest = (UpdateRequest) request;
                response = this.responseForUpdate(repo, updateRequest.getKey(), updateRequest.getValue());
                break;
            case DELETE:
                response = this.responseForDelete(repo, request.getKey());
                break;
            default:
                throw new RuntimeException("Impossible request type: " + operation);
        }
        return response;
    }

    private Single<Response> responseForContainsKey(RxHallaStor repo, String key) {
        return repo.containsKey(key)
                .map(ContainsKeySuccessResponse::new);
    }

    private Single<Response> responseForPut(RxHallaStor repo, String key, Object value) {
        return Single.create(subscriber -> {
            repo.put(key, value)
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

    private Single<Response> responseForGet(RxHallaStor repo, String key) {
        return Single.create(subscriber -> {
            repo.get(key)
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

    private Single<Response> responseForUpdate(RxHallaStor repo, String key, Object value) {
        return Single.create(subscriber -> {
            repo.update(key, value)
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

    private Single<Response> responseForDelete(RxHallaStor repo, String key) {
        return Single.create(subscriber -> {
            repo.delete(key)
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
        try {
            System.out.println("***STARTING MAIN***");

            String host = args[0];
            int port = Integer.parseInt(args[1]);

            Env.LOGGER = createLogger(port + ".log");
            Env.LOGGER.info("MAIN launching with " + Arrays.toString(args));
            Env.LOGGER.info(String.format("Starting node at %s:%d...", host, port));

            Env.LOGGER.debug("getting ready!!!");
            String ringInfoFile = args[2];
            Env.LOGGER.debug("Loading ring info from " + ringInfoFile);
            RingInfo ringInfo = Serializer.consumeObjectFromTempFile(ringInfoFile);

//        Env.LOGGER.debug("getting ready 2!!!");
//        String vnodeKeysFile = args[3];
//        Env.LOGGER.debug("Loading vnode key set from " + vnodeKeysFile);
//        Set<String> vnodeKeySet = Serializer.consumeObjectFromTempFile(vnodeKeysFile);

            Node node = new Node(host, port, ringInfo);
            node.start();

            // Indicate that the node has started.
            System.out.println("STARTED");

            Env.LOGGER.info("...started.");
            System.out.println("***ENDING MAIN***");
        } catch (Exception e) {
            Env.LOGGER.fatal(e);
        }
    }

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
        Env.LOGGER.fatal("Stopping...");
        super.finalize();
    }
}

