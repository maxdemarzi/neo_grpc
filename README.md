# neo_grpc
POC gRPC Kernel Extension for Neo4j

1. Build it:

        mvn protobuf:compile
        mvn protobuf:compile-custom
        mvn clean package

2. Copy target/grpc-server-1.0-SNAPSHOT-jar-with-dependencies.jar to the plugins/ directory of your Neo4j server.

3. (Re)Start Neo4j server.

4. See /test/java/com.maxdemarzi/Neo4jGRPCServiceTest.java file for an example