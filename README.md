# Auction Mechanism [![Build Status](https://travis-ci.org/FrancescoMarz10/Auction-Mechanism.svg?branch=master)](https://travis-ci.org/FrancescoMarz10/Auction-Mechanism)
An auction mechanism based on P2P Network. Each peer can sell and buy goods using a Second-Price Auctions (EBay). Each bidder places a bid. The highest bidder gets the first slot, but pays the price bid by the second-highest bidder. The systems allows the users to create new auction (with an ending time, a reserved selling price and a description), check the status of an auction, and eventually place new bid for an auction. As described in the AuctionMechanism Java API.
```
Autore: Marzullo Francesco - Matricola: 0522500679
```

# Technologies involved
- Java 8
- Tom P2P
- JUnit
- Apache Maven
- Docker
- IntelliJ

# Project Structure
Thanks to the use of Maven it is possible to insert the dependencies to Tom P2P into the pom file, as follows:
```
<repositories>
    <repository>
        <id>tomp2p.net</id>
         <url>http://tomp2p.net/dev/mvn/</url>
     </repository>
</repositories>
<dependencies>
   <dependency>
     <groupId>net.tomp2p</groupId>
     <artifactId>tomp2p-all</artifactId>
      <version>5.0-Beta8</version>
   </dependency>
</dependencies>
```
The ```src/main/java/it/unisa/auctionmechanism``` package provides the following Java classes:

    - MessageListener, an interface for the listener of messages received from peers.
    - AuctionMechanism, an interface that defines the main methods of the AuctionMechanism paradigm.
    - Auction, the class representing the auction object.
    - AuctionImplementation, an implementation of the AuctionMechanism interface that takes advantage of the Tom P2P library.
    - Example, an example of application of the peer network capable of using the developed auction mechanism.

# Development
The *Auction* class consists of the following instance variables:

- _auction_name, the name of the auction
- _creator, the id of the peer creator of the auction
- _end_time, auction expiration date and time
- _reserved_price, the departure time
- _description, description of the product put up for auction
- *max_bid, the best offer received
- *second_max_bid, the second best offer received
- *bid_id, the id of the highest bidder
- *peerAddress_bid, the peer address of the highest bidder
- *old_bid_Address, the peer address of the previous highest bidder
- *users, list of peers participating in the auction


## AuctionMechanism Interface
The interface provided for the development of the AuctionMechanism project consists of the following methods:

1. createAuction: to create an auction
2. checkAuction: to check the status of the auction
3. placeAbid: to make an offer

#### createAuction Method
The *createAuction* method takes the following values as input:

     - _auction_name, name of the auction
     - _end_time, auction end time
     - _reserved_price, starting price of the auction
     - _description, description of the object in auction
    
This function develops through the following steps:
1. Check that an auction with the same name does not already exist
2. Create a new auction with all parameters received.
3. Search for the presence of the list of auction names within the dht.
4. If so, get this list and add the name of the auction to it and reload it in the dht.
5. Finally, upload the entire auction to the dht.
    
##### Implementation 
```
public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) throws IOException, ClassNotFoundException {

        if(checkAuction(_auction_name) == null){
            Date actual_date = new Date();
            if (actual_date.after(_end_time)){
                return false;
            }
            Auction auction = new Auction(_auction_name,  peer_id,_end_time, _reserved_price,_description);
            FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            }
            auctions_names.add(_auction_name);
            dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
            dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
            return true;
        }
        return false;
    }
```

#### checkAuction Method
The *checkAuction* method takes as input only the name of the auction to be searched with the aim of verifying its presence in the auction list and its possible status.

This function develops through the following steps:
1. Search for the presence of the list of auction names within the dht
2. If the search obtains an affirmative result, download the entire list, otherwise create a new one to upload to the dht
3. Once the list is obtained, check for an auction with the name obtained as a parameter
4. In the event that the search results are affirmative, download the corresponding 'Auction' object from the dht
5. The function then checks whether the auction is active or ended, based on the expiry date and time
5. In both cases, it returns the status of the auction, showing the eventual winner or the highest bid and all related information

##### Implementation
```
public String checkAuction(String _auction_name) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            if (futureGet.isEmpty()) {
                dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
                return null;
            }
            
            auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            
            if(auctions_names.contains(_auction_name)) {
                futureGet = dht.get(Number160.createHash(_auction_name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                        Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                        Date actual_date = new Date();
                        if (actual_date.after(auction.get_end_time())) {
                            if(Double.compare(auction.get_reserved_price(),auction.getMax_bid())==0){
                                return "The Auction is ended with no winner!";
                            } else {
                                if (auction.getBid_id() == peer_id) {
                                    return "The Auction is ended and the winner is you, " + auction.getBid_id() + ", with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                                } else
                                    return "The Auction is ended and the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                            }

                        } else {
                            if (auction.getUsers().isEmpty()) {
                                return "The auction is active until " + auction.get_end_time() + " and the reserved price is: " + auction.get_reserved_price();
                            } else {
                                if (auction.getBid_id() == peer_id) {
                                    return "The auction is active until " + auction.get_end_time() + " and the highest offer is yours with: " + auction.getMax_bid();
                                } else
                                    return "The auction is active until " + auction.get_end_time() + " and the highest offer is: " + auction.getMax_bid();
                            }
                        }
                    }
            }
            return null;
        }
        return null;
    }

```
#### placeABid Method
The *placeABid* method takes as input the name of the auction and the value of the offer you wish to present.

This function develops through the following steps:
1. Search for the presence of the list of auction names within the dht
2. If the search yields an affirmative result, download the entire list
3. Once the list is obtained, check for an auction with the name obtained as a parameter
4. If the search results are affirmative, download the corresponding auction and check if it is active or ended, based on the expiry date and time.
5. If the auction is still active, the bidder is not the creator or the best bidder of the auction and the new offer exceeds the current one updates all the information relating to the new proposal and its author.
6. Update the auction within the dht so as to make the changes visible to all peers.

##### Implementation

```
 public String placeAbid(String _auction_name, double _bid_amount) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();
        
        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();

            if(dataMapValues.isEmpty()){
                return null;
            }
            else{
                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            }
            if (auctions_names.contains(_auction_name)) {
                futureGet = dht.get(Number160.createHash(_auction_name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                    Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                    Date actual_date = new Date();
                    if (auction.get_creator() == peer_id) {
                        return "The creator can't do a bid!";
                    }
                    if (auction.getBid_id() == peer_id) {
                        return "You have already offered the highest bid!";
                    }
                    if (actual_date.after(auction.get_end_time())) {
                         if(Double.compare(auction.get_reserved_price(),auction.getMax_bid())==0){
                            return "You can't do a bid! The Auction is ended with no winner!";
                        } else {
                            return "You can't do a bid! The Auction is ended, the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                        }

                     //Checking if the new bid is better than the old one and updating all the variables of the auction
                    } else if (_bid_amount > auction.getMax_bid()) {

                        auction.setSecond_max_bid(auction.getMax_bid());
                        auction.setMax_bid(_bid_amount);

                        auction.setOld_bid_Address(auction.getPeerAddress_bid());
                        auction.setPeerAddress_bid(peer.peerAddress());

                        auction.setBid_id(peer_id);

                        if (!auction.getUsers().contains(peer_id)) {
                            auction.getUsers().add(peer.peerAddress());
                            auction.setPeerAddress_bid(peer.peerAddress());
                        }
                        dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
                        sendMessage("The new best bid on the " + _auction_name + " auction is " + auction.getMax_bid() + " by " + auction.getBid_id(), _auction_name);

                        return "The auction is active until " + auction.get_end_time() + " and the highest offer is yours with: " + auction.getMax_bid();
                    } else {
                        return "You can't do a bid lower then the biggest bid!";
                    }
                }
            }
        }
        return null;
    }
```

## Other methods implemented

#### sendMessage Method
The *sendMessage* method is used to notify a peer that its offer has just been surpassed by a new proposal of greater value, to alert participants of an auction of the sudden abandonment of the network by the creator of the auction and the elimination of this or the abandonment of the highest bidder and the consecutive reset of the auction at the basic price.
This method takes as input the message to be delivered, the name of the auction to which it refers and the type of message to be sent.

#### checkAllAuctions Method
The *checkAllAuctions* method is used to obtain the complete list of the auctions currently present and some fundamental information related to them, such as the status and the current best offer.

#### removeAnAuction Method
The *removeAnAuction* method allows the creator of an auction, using the name obtained as a parameter, to delete it.

#### exit Method
The *exit* method is used to allow a node to exit the system. Furthermore, when a node leaves the network, all the auctions created by it are eliminated, all the auctions where it is the best bidder are reset and the participants in these are notified via a message, thanks to the *removeMyAuctionsAndOffers()* method.

#### removeMyAuctionsAndOffers Method
The *removeMyAuctionsAndOffers* method is called by the exit method when a peer leaves the network and takes care to eliminate all the auctions and offers made by the peer in question.


# Testing
The test cases analyzed are the following:
 
  1. Bid made by the creator of the auction
  2. Bid made by the current highest bidder
  3. Bid made after the auction deadline
  4. Removal of an auction by the creator
  5. Removal of an auction by a simple participant
  6. Auction ended with winner
  7. Auction ended without winner
  8. Creation of an auction already present in the dht
  9. Leave the network as creator of at least one auction
  10. Leave the network as the highest bidder on an auction
  11. Check all the auctions in the dht
  12. Auction ended with multiple bids
  13. Auction ended with a single bid
  14. Check the status of a non-existent auction
  15. Check the status of an auction without bidders
  16. Check the status of an active auction with bidders
  17. Place a bid on an auction that has already ended with a winner
  18. Make an offer less than the minimum price
 
# Dockerfile
The Dockerfile was made as follows:

```
FROM alpine/git as clone
ARG url
WORKDIR /app
RUN git clone ${url}

FROM maven:3.5-jdk-8-alpine as builder
ARG project 
WORKDIR /app
COPY --from=clone /app/${project} /app
RUN mvn package

FROM openjdk:8-jre-alpine
ARG artifactid
ARG version
ENV artifact ${artifactid}-${version}.jar
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
ENV TZ="Europe/Rome"
COPY --from=builder /app/target/${artifact} /app

CMD /usr/bin/java -jar ${artifact} -m $MASTERIP -id $ID
```

 - Given the structure of the file it can be used to build any app with the following features:

         - The source code is hosted on GitHub.
         - The compilation tool is Maven.
         - The resulting output is an executable JAR file.
         
     The parameters present are:

         - The URL of the GitHub repository
         - The name of the project
         - The artifact ID and the version of Maven

     These parameters can be used to design a parametric build file. In Docker, parameters can be passed using the ENV or ARG options. Both are set using the ```--build-arg``` option on the command line during the docker build operation.

 - Usually the build phases are referenced through their index (starting from 0). Although this is not a problem, it is useful for better readability of the file to have something semantically significant. Docker allows us to label the phases and refers to these labels in the subsequent phases.
 
 - In addition, the environment variable ENV TZ = "Europe / Rome" has been inserted, to initialize the time zone with the local one.
 

# How to Build Auction-Mechanism

### In a Container Docker
The first operation to be performed in the terminal is the build of the docker container thanks to the following instruction:
```
docker build --build-arg url=https://github.com/FrancescoMarz10/Auction-Mechanism.git --build-arg project=Auction-Mechanism --build-arg artifactid=auctionmechanism --build-arg version=1.0-jar-with-dependencies -t auctionmechanism --no-cache .
```
The build of the docker involves the insertion of the parameters to be passed in input to the dockerfile with the instruction ```--build-arg```.

### Start the Master Peer
As a second operation after building the container, the master peer must be started via the following line of code within the command line in interactive (-i) mode and with two (-e) environment variables:
```
docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 auctionmechanism
```
The environment variable MASTERIP is the IP address of the master peer and the environment variable ID is the unique id of the peer. Remember that you need to start the master peer with ID = 0.

### Start a Generic Peer
Once the Master Peer has started, it is possible to start other Peers thanks to the following instruction and selecting a unique ID:
```
docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 auctionmechanism
```
