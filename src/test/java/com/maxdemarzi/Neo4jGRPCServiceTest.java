package com.maxdemarzi;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;

import java.util.Iterator;

public class Neo4jGRPCServiceTest {

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT);

    private static Neo4jQueryGrpc.Neo4jQueryBlockingStub blockingStub;

    @Before
    public void setup() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true)
                .build();
        blockingStub = Neo4jQueryGrpc.newBlockingStub(channel);
    }

    @Test
    public void testQuery() throws Exception {

        CypherQueryString queryString = CypherQueryString.newBuilder().setQuery(QUERY).build();
        CypherQueryResult response;

        Iterator<CypherQueryResult> iterator = blockingStub.executeQuery(queryString);
        while (iterator.hasNext()) {
            response = iterator.next();
            Assert.assertEquals("{n.name=max}", response.getResult());
        }
    }

    private static final String QUERY = "MATCH (n:Person) WHERE n.name = 'max' RETURN n.name";

    private static final String MODEL_STATEMENT =
            "CREATE (n:Person {name:'max'})";

}
