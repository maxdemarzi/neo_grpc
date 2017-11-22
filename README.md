# neo_grpc
POC gRPC Kernel Extension for Neo4j

1. Build it:

        mvn protobuf:compile
        mvn protobuf:compile-custom
        mvn clean package

2. Copy target/grpc-server-1.0-SNAPSHOT-jar-with-dependencies.jar to the plugins/ directory of your Neo4j server.

3. (Re)Start Neo4j server.

4. See /test/java/com.maxdemarzi/Neo4jGRPCServiceTest.java file for an example

5. Create some data:

        CREATE (n:Person {name:'max'})

6. Run the Neo4jGRPCBenchmark

        Benchmark                               Mode  Cnt     Score     Error  Units
        Neo4jGRPCBenchmark.measureBoltRequest  thrpt  100  5946.789 ±  68.063  ops/s
        Neo4jGRPCBenchmark.measuregRPCRequest  thrpt  100  9321.269 ± 239.146  ops/s
        Neo4jGRPCBenchmark.measureHTTPRequest  thrpt  100  7225.882 ± 225.242  ops/s