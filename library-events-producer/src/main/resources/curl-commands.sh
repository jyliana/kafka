POST WITH-NULL-LIBRARY-EVENT-ID
---------------------
curl -i \
-d '{"id":null,"type": "NEW","time": "2024-08-23T23:16:43","book":{"id":456,"name":"Kafka Using Spring Boot","author":"Dilip"}}' \
-H "Content-Type: application/json" \
-X POST http://localhost:8080/v1/libraryevent

PUT WITH ID - 1
--------------
curl -i \
-d '{"id":1,"type": "NEW","time": "2024-08-23T18:33:43.724427","book":{"id":456,"name":"Kafka Using Spring Boot 2.X","author":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/libraryevent

curl -i \
-d '{"id":2,"type": "UPDATE","time": "2024-08-23T18:53:43","book":{"id":456,"name":"Kafka Using Spring Boot 2.X","author":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/libraryevent



PUT WITH ID
---------------------
curl -i \
-d '{"id":123,"type": "UPDATE","book":{"id":456,"name":"Kafka Using Spring Boot","author":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/libraryevent

curl -i \
-d '{"id":999,"type": "UPDATE","book":{"id":456,"name":"Kafka Using Spring Boot","author":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/libraryevent

curl -i \
-d '{"id":2,"type": "UPDATE","book":{"id":456,"name":"Kafka Using Spring Boot","author":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/libraryevent


PUT WITHOUT ID
---------------------
curl -i \
-d '{"id":null,"type": "UPDATE","book":{"id":456,"name":"Kafka Using Spring Boot","author":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/libraryevent


./kafka-topics.sh --create --topic library-events.DLT --replication-factor 1 --partitions 4 --bootstrap-server localhost:9092
