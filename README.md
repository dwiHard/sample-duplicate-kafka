# Sample maintain message ordering and no message duplication

### Running docker compose
```
docker compose up -d
```
### Create topic
```
docker exec -t broker kafka-topics --bootstrap-server broker:9092 --topic myTopic --create --replication-factor 1 --partitions 2
```
### Decs topic
```
docker exec -t broker kafka-topics --bootstrap-server broker:9092 --topic myTopic --describe
```
### Build java
```
mvn clean compile assembly:single
```
### Running jar file
```
java -jar target/Kafka-sample-1.0-SNAPSHOT-jar-with-dependencies.jar config.properties input.txt
```
### View all records in the topic
```
docker exec -t broker kafka-console-consumer --topic myTopic \
 --bootstrap-server broker:9092 \
 --from-beginning \
 --property print.key=true \
 --property key.separator=" : "
```
### Consume the data in partition 0
```
docker exec -t broker kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic myTopic \
  --property print.key=true \
  --property key.separator=, \
  --partition 0 \
  --from-beginning
```
### Consume the data in partition 1
```
docker exec -t broker kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic myTopic \
  --property print.key=true \
  --property key.separator=, \
  --partition 1 \
  --from-beginning
```
### View the broker log segment file for partition 0
```
docker exec -t broker kafka-dump-log \
  --print-data-log \
  --files '/var/lib/kafka/data/myTopic-0/00000000000000000000.log' \
  --deep-iteration
```

## Thank you