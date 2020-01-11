package it.unisa.auctionmechanism;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class AuctionImplementation implements AuctionMechanism {

    final private PeerDHT dht;
    final private int DEFAULT_MASTER_PORT = 4000;
    final private Peer peer;
    int peer_id;
    ArrayList<String> auctions_names = new ArrayList<String>();


    //Constructor
    public AuctionImplementation(int id_peer,String _master_peer, final MessageListener _listener) throws Exception {
        peer= new PeerBuilder(Number160.createHash(id_peer)).ports(DEFAULT_MASTER_PORT+id_peer).start();
        dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
        fb.awaitUninterruptibly();
        if(fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
        peer_id = id_peer;

        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(PeerAddress sender, Object request) throws Exception {
                return _listener.parseMessage(request);
            }
        });

    }

    //Creation of a new Auction.
    /**
     * Creates a new auction for a good.
     * @param _auction_name a String, the name identify the auction.
     * @param _end_time a Date that is the end time of an auction.
     * @param _reserved_price a double value that is the reserve minimum pricing selling.
     * @param _description a String describing the selling goods in the auction.
     * @return true if the auction is correctly created, false otherwise.
     */
    public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) throws IOException, ClassNotFoundException {

        //Starting checking the auction...
        if(checkAuction(_auction_name) == null){

            //Creating the auction...
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

            //Creating the dht...
            dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
            dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
            //Added to the dht...
            return true;
        }
        return false;
    }

    //Checking the status of an auction.
    /**
     * Checks the status of the auction.
     * @param _auction_name a String, the name of the auction.
     * @return a String value that is the status of the auction.
     */
    public String checkAuction(String _auction_name) throws IOException, ClassNotFoundException {

        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {

            //Do we need to put a new ArrayList?...
            if (futureGet.isEmpty()) {
                dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
                //Yes...
                return null;
            }

            //No, we can take auctions_names...
            auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

            //Checking the auction...
            if(auctions_names.contains(_auction_name)) {
                futureGet = dht.get(Number160.createHash(_auction_name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                    //Taking the auction...
                        Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                        Date actual_date = new Date();

                        if (actual_date.after(auction.get_end_time())) {
                            //Checking if the auction is ended..

                            if(Double.compare(auction.get_reserved_price(),auction.getMax_bid())==0){
                                return "The Auction is ended with no winner!";
                            } else {
                                if (auction.getBid_id() == peer_id) {
                                    return "The Auction is ended and the winner is you, " + auction.getBid_id() + ", with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                                } else
                                    return "The Auction is ended and the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                            }

                        } else {
                            //auction is still active.
                            //Creating the auction status...
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

    //Placing a new bid.
    /**
     * Places a bid for an auction if it is not already ended.
     * @param _auction_name a String, the name of the auction.
     * @param _bid_amount a double value, the bid for an auction.
     * @return a String value that is the status of the auction.
     */
    public String placeAbid(String _auction_name, double _bid_amount) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        //Checking the presence of the names list of auctions on dht
        if (futureGet.isSuccess()) {

            Collection<Data> dataMapValues = futureGet.dataMap().values();

            if(dataMapValues.isEmpty()){
                return null;
            }
            else{
                //Taking the list
                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            }

            //Checking if the researched auction is in the list and so in the dht
            if (auctions_names.contains(_auction_name)) {
                futureGet = dht.get(Number160.createHash(_auction_name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                    //Taking the auction...
                    Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();

                    //Taking the actual date
                    Date actual_date = new Date();

                    //Checking if the peer is the creator of the auction
                    if (auction.get_creator() == peer_id) {
                        return "The creator can't do a bid!";
                    }

                    //Checking if the peer is already the best offerer
                    if (auction.getBid_id() == peer_id) {
                        return "You have already offered the highest bid!";
                    }

                    //Checking if the auction is ended
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

                        //Putting the updated auction in the dht again
                        dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
                        sendMessage("The new best bid on the " + _auction_name + " auction is " + auction.getMax_bid() + " by " + auction.getBid_id(), _auction_name,1);

                        return "The auction is active until " + auction.get_end_time() + " and the highest offer is yours with: " + auction.getMax_bid();
                    } else {
                        return "You can't do a bid lower then the biggest bid!";
                    }
                }
            }
        }
        return null;
    }

    //Send a new message when a bid is outdated.
    /**
     * Send a new message when a bid is outdated, your bid has been exceeded, either the auction creator or the highest bidder has left the network.
     * @param _obj, the text of the message.
     * @param _auction_name a String, the name of the auction.
     * @param type an integer value, the type of the message.
     * @return true if the message is correctly sended, false otherwise.
     */
    public boolean sendMessage(Object _obj,String _auction_name, int type) throws IOException, ClassNotFoundException {

        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        //Checking the presence of the names list of auctions on dht
        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();

            if (dataMapValues.isEmpty()) {
                return false;
            } else {
                //Taking the list
                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            }

            //Checking if the researched auction is in the list and so in the dht
            if (auctions_names.contains(_auction_name)) {

                futureGet = dht.get(Number160.createHash(_auction_name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                    //Taking the auction...
                    Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                    HashSet<PeerAddress> users = auction.getUsers();

                    //Sending the notify of the new best bid to the old best offerer
                    for (PeerAddress mypeer : users) {
                        if (mypeer.equals(auction.getOld_bid_Address()) && users.size() > 1 && type == 1) {
                            FutureDirect futureDirect = dht.peer().sendDirect(mypeer).object(_obj).start();
                            futureDirect.awaitUninterruptibly();
                        }
                        else if(type != 1){
                            FutureDirect futureDirect = dht.peer().sendDirect(mypeer).object(_obj).start();
                            futureDirect.awaitUninterruptibly();
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Leave the net. Remove every auction created and every bid done thank to the function removeMyAuctionsAndOffers().
     * @return true if the peer left the network correctly.
     */
    public boolean exit(){
        try {
            removeMyAuctionsAndOffers();
            dht.peer().announceShutdown().start().awaitUninterruptibly();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Visualize all the auctions and their informations.
     * @return a String value that is the status of all the auctions.
     */
    public String checkAllAuctions() throws IOException, ClassNotFoundException {

        String all_auctions = "";
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();
        String status = "";

        //Checking the presence of the names list of auctions on dht
        if (futureGet.isSuccess()) {
            if(!futureGet.dataMap().values().isEmpty()) {

                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

                //Checking if the list of names is not empty
                if (!auctions_names.isEmpty()) {

                    //Taking all the auctions and their informations with a for loop.
                    for (String name : auctions_names) {

                        Date actual_date = new Date();
                        futureGet = dht.get(Number160.createHash(name)).start();
                        futureGet.awaitUninterruptibly();

                        if (futureGet.isSuccess()) {
                            //Taking the auction...
                            Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();

                            if (actual_date.after(auction.get_end_time())) {
                                status = "ENDED";
                            } else {
                                status = "ACTIVE";
                            }
                            if (auction.getUsers().isEmpty()) {
                                all_auctions += "Name: " + name + ", Reserved Price: " + auction.getMax_bid() + ", Status: " + status + ", Description: " + auction.get_description() + "\n";

                            } else {
                                all_auctions += "Name: " + name + ", Best Bid: " + auction.getMax_bid() + ", Status: " + status + ", Description: " + auction.get_description() + "\n";
                            }

                        }
                    }
                    return all_auctions;
                }
            }
        }
        return null;
    }


    //Removing an auction. Only the creator can remove an auction.
    /**
     * Remove an auction. Only the creator can remove an auction.
     * @param _auction_name a String, the name of the auction.
     * @return true if the auction is correctly removed, false otherwise.
     */
    public boolean removeAnAuction(String _auction_name) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        //Checking the presence of the names list of auctions on dht
        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();

            if(dataMapValues.isEmpty()){
                return false;
            }
            else{
                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            }

            for (String name : auctions_names) {
                futureGet = dht.get(Number160.createHash(name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                    //Taking the auction...
                    Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();

                    if (name.equals(_auction_name) && auction.get_creator() == peer_id) {
                        auctions_names.remove(name);

                        //Removing the auction from the list and in the dht
                        dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
                        FutureRemove fr = dht.remove(Number160.createHash(_auction_name)).start().awaitUninterruptibly();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Removes all the auctions and offers made by the peer who left the network.
     */
    public void removeMyAuctionsAndOffers() throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        //Checking the presence of the names list of auctions on dht
        if (futureGet.isSuccess()) {
            if (!futureGet.dataMap().values().isEmpty()) {

                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

                //Checking if the list of names is not empty
                if (!auctions_names.isEmpty()) {

                    //Taking all the auctions and their informations with a for loop.
                    for (String name : auctions_names) {
                        futureGet = dht.get(Number160.createHash(name)).start();
                        futureGet.awaitUninterruptibly();

                        if (futureGet.isSuccess()) {
                            Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                          
                            Date actual_date = new Date();
                            if (actual_date.after(auction.get_end_time())) {
                                return;
                            }
                            
                            if(auction.get_creator()==peer_id){
                                sendMessage("The auction "+ name+ " has been deleted because the creator left the network!", name,2);
                                removeAnAuction(name);
                            }
                            if(auction.getBid_id()==peer_id){
                                auction.setBid_id(-1);
                                auction.setMax_bid(auction._reserved_price);
                                auction.getUsers().remove(peer.peerAddress());
                                dht.put(Number160.createHash(name)).data(new Data(auction)).start().awaitUninterruptibly();
                                sendMessage("The best bid for the auction "+ name+ " has been resetted because the best bidder left the network!", name,3);
                            }
                        }
                    }
                }
            }
        }
    }

}
