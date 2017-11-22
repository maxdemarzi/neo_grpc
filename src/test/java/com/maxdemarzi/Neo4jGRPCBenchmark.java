package com.maxdemarzi;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Assert;
import org.neo4j.driver.v1.*;
import org.neo4j.test.server.HTTP;
import org.openjdk.jmh.annotations.*;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;


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
        Assert.assertEquals("{n.name=max}", blockingStub.executeQuery(queryString).next().getResult());
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

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 50)
    @Fork(2)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureHTTPRequest() throws Exception {
        HTTP.Response response = HTTP.withHeaders(HttpHeaders.AUTHORIZATION, "Basic bmVvNGo6c3dvcmRmaXNo").POST("http://localhost:7474/db/data/transaction/commit", HTTPQUERY);
        Assert.assertEquals("\"max\"", response.get("results").get(0).get("data").get(0).get("row").get(0).toString());
    }

    private static final String QUERY = "MATCH (n:Person) WHERE n.name = 'max' RETURN n.name";
    private static final Map HTTPQUERY =
                singletonMap("statements",asList(singletonMap("statement", "MATCH (n:Person) WHERE n.name = 'max' RETURN n.name")));
}
