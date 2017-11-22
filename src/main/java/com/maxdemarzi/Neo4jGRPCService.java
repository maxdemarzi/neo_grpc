package com.maxdemarzi;

import io.grpc.stub.StreamObserver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class Neo4jGRPCService extends Neo4jQueryGrpc.Neo4jQueryImplBase {
    private static GraphDatabaseService db;

    Neo4jGRPCService(GraphDatabaseService db) {
        Neo4jGRPCService.db = db;
    }

    @Override
    public void executeQuery(CypherQueryString req, StreamObserver<CypherQueryResult> responseObserver) {
        try (Transaction tx = db.beginTx()) {
            Result result = db.execute(req.getQuery());
            result.stream().forEach(stringObjectMap -> {
                CypherQueryResult r = CypherQueryResult.newBuilder().setResult(stringObjectMap.toString()).build();
                responseObserver.onNext(r);
            });
            tx.success();
        }
        responseObserver.onCompleted();
    }
}
