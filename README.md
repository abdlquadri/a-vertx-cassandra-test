# a-vertx-cassandra-test
A Vert.x Cassandra Test

* The test assumes there is cassandra running in host machine, default port with the data in `data_1.csv, data_2.csv` 
using the following schema `config/schema.cql`
* There is `schema.cql` in `config` directory
* There is also sample data in `config` directory in csv format.
* run `./gradlew test` to excute the unit tests
* run `./gradlew shadowJar` to build `build/libs/pastes-3.3.3-fat.jar`. This step will also run the test task
* There is also a Dockerfile to build a docker image
* run `sudo docker build -t a_vertx_cassandra_test .
` to build the image and
* run `sudo docker run -i -t --network=host a_vertx_cassandra_test
` to run and connect to cassandra running on host machine. (this is just for test, in production, image co-ordination 
tool like docker swarm with proper network configuration will be used)
.