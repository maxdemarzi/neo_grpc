package com.maxdemarzi;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

public class RegistergRPCExtensionFactory extends KernelExtensionFactory<RegistergRPCExtensionFactory.Dependencies> {

    @Override
    public Lifecycle newInstance(KernelContext kernelContext, final Dependencies dependencies) throws Throwable {
        return new LifecycleAdapter() {

            private Server server;

            @Override
            public void start() throws Throwable {
                server  = ServerBuilder.forPort(9999).addService(new Neo4jGRPCService(dependencies.getGraphDatabaseService())).build();
                server.start();
                System.out.println("Started gRPC Server.");
            }

            @Override
            public void shutdown() throws Throwable {
                server.shutdown();
            }
        };
    }

    interface Dependencies {
        GraphDatabaseService getGraphDatabaseService();
    }

    public RegistergRPCExtensionFactory() {
        super("registergRPCExtensionFactory");
    }

}
