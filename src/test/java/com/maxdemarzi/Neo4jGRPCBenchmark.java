package com.maxdemarzi;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Assert;
import org.neo4j.driver.v1.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
public class Neo4jGRPCBenchmark {

    private static Neo4jQueryGrpc.Neo4jQueryBlockingStub blockingStub;
    private static Driver driver;

    @Setup(Level.Trial)
    public void prepare() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true)
                .build();
        blockingStub = Neo4jQueryGrpc.newBlockingStub(channel);

        driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "swordfish" ) );
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 50)
    @Fork(2)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measuregRPCRequest() throws IOException {
        CypherQueryString queryString = CypherQueryString.newBuilder().setQuery(QUERY).build();
        CypherQueryResult response;

        Iterator<CypherQueryResult> iterator = blockingStub.executeQuery(queryString);
        while (iterator.hasNext()) {
            response = iterator.next();
            Assert.assertEquals("{n.name=max}", response.getResult());
        }
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 50)
    @Fork(2)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureBoltRequest() throws IOException {

        try ( Session session = driver.session() ) {
            session.readTransaction((TransactionWork<String>) transaction -> {
                        StatementResult statementResult = transaction.run(QUERY);
                        Assert.assertEquals("max", statementResult.single().get( 0 ).asString());
                        return null;
                    });
        }

    }

    private static final String QUERY = "MATCH (n:Person) WHERE n.name = 'max' RETURN n.name";
}
