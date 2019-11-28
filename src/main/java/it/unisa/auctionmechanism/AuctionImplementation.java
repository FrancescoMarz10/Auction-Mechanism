package it.unisa.auctionmechanism;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import net.tomp2p.dht.FutureGet;
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
    HashMap<String, Auction> auctions = new HashMap<String, Auction>();


    //CONSTRUCTOR
    public AuctionImplementation(int id_peer,String _master_peer, final MessageListener _listener) throws Exception {
        peer= new PeerBuilder(Number160.createHash(id_peer)).ports(DEFAULT_MASTER_PORT+id_peer).start();
        dht = new PeerBuilderDHT(peer).start();

        //TO CHECK
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
    public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) throws IOException, ClassNotFoundException {

        //Starting checking the auction...
        if(checkAuction(_auction_name) == null){

            //Creating the auction...
            Auction auction = new Auction(_auction_name,  peer_id,_end_time, _reserved_price,_description);
            FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();

            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
               auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }
            auctions.put(_auction_name,auction);

            //Creating the dht...
            dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
            //Added to the dht...
            return true;
        }
        return false;
    }

    //Checking the status of an auction.
    public String checkAuction(String _auction_name) throws IOException, ClassNotFoundException {

        //Creating the hashmap...
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            //Hashmap found

            //Do we need to put a new HashMap?...
            if (futureGet.isEmpty()) {
                dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
               //Yes...
                return null;
            }

            //No, we can take auctions...
            HashMap<String, Auction> auctions;
            auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();

            //Checking the auction...

            if(auctions.containsKey(_auction_name)) {

                //Taking the auction...
                Auction auction = auctions.get(_auction_name);

                Date actual_date = new Date();

                if (actual_date.after(auction.get_end_time())) {
                    //Checking if the auction is ended..

                    //RECHECK DOUBLE? RECHECK!!!!!
                    if(auction.get_reserved_price().toString().equals(auction.getMax_bid().toString())){
                        return "The Auction is ended with no winner!";
                    }
                    else{
                        if(auction.getBid_id()==peer_id){
                            return "The Auction is ended and the winner is you, " + auction.getBid_id() + ", with this bid: " + auction.getMax_bid() +" and the price is " + auction.getSecond_max_bid();
                        }
                        else return "The Auction is ended and the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid()+" and the price is " + auction.getSecond_max_bid();
                    }

                } else {
                    //auction is still active.
                    //Creating the auction status...
                    if(auction.getUsers().isEmpty()){
                        return "The auction is active until "+ auction.get_end_time()+" and the reserved price is: " + auction.get_reserved_price();
                    }
                    else {
                        if(auction.getBid_id()==peer_id){
                            return "The auction is active until "+ auction.get_end_time()+" and the highest offer is yours with: " + auction.getMax_bid();
                        }
                        else return "The auction is active until "+ auction.get_end_time()+" and the highest offer is: " + auction.getMax_bid();
                    }
                }
            }
            return null;
        }
        return null;
    }

    //Placing a new bid.
    public String placeAbid(String _auction_name, double _bid_amount) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {

            Collection<Data> dataMapValues = futureGet.dataMap().values();

            HashMap<String, Auction> auctions;
            if(dataMapValues.isEmpty()){
                return null;
            }
            else{
                auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }

            if (auctions.containsKey(_auction_name)) {
                Auction auction = auctions.get(_auction_name);
                Date actual_date = new Date();

                if(auction.get_creator()== peer_id){
                    return "The creator can't do a bid!";
                }
                if(auction.getBid_id() == peer_id){
                    return "You have already offered the highest bid!";
                }

                if (actual_date.after(auction.get_end_time())) {
                    if(auction.get_reserved_price().toString().equals(auction.getMax_bid().toString())){
                        return "You can't do a bid! The Auction is ended with no winner!";
                    }
                    else{
                        return "You can't do a bid! The Auction is ended, the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid()+" and the price is " + auction.getSecond_max_bid();
                    }

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

                    auctions.put(_auction_name, auction);

                    dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
                    sendMessage("The new best bid on the "+ _auction_name+" auction is "+auction.getMax_bid()+" by "+ auction.getBid_id(),_auction_name);

                    return "The auction is active until "+ auction.get_end_time()+" and the highest offer is yours with: " + auction.getMax_bid();
                }
                else {
                    return "You can't do a bid lesser then the biggest bid!";
                }
            }
        }
        return null;
    }

    //Send a new message when a bid is outdated.
    public boolean sendMessage(Object _obj,String _auction_name) throws IOException, ClassNotFoundException {

        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {

            Collection<Data> dataMapValues = futureGet.dataMap().values();

            HashMap<String, Auction> auctions;
            if (dataMapValues.isEmpty()) {
                return false;
            } else {
                auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }
            if (auctions.containsKey(_auction_name)) {
                Auction auction = auctions.get(_auction_name);
                HashSet<PeerAddress> users = auction.getUsers();
                for (PeerAddress  mypeer : users) {
                    if(mypeer.equals(auction.getOld_bid_Address()) && users.size()>1) {
                        FutureDirect futureDirect = dht.peer().sendDirect(mypeer).object(_obj).start();
                        futureDirect.awaitUninterruptibly();
                    }

                }
            }
        }
        return true;
    }

    //Leaving the net.
    public boolean exit(){
        try {
            dht.peer().announceShutdown().start().awaitUninterruptibly();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Visualize all the auctions and their informations.
    public String checkAllAuctions() throws IOException, ClassNotFoundException {

        String all_auctions = "";
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();
        String status = "";

        if (futureGet.isSuccess()) {
            if(!futureGet.dataMap().values().isEmpty()) {
                HashMap<String, Auction> auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
                if (!auctions.isEmpty()) {
                    for (String name : auctions.keySet()) {

                        Date actual_date = new Date();

                        if (actual_date.after(auctions.get(name).get_end_time())) {
                            status = "ENDED";
                        }
                        else{
                            status = "ACTIVE";
                        }
                        if(auctions.get(name).getUsers().isEmpty()){
                            all_auctions += "Name: " + name + ", Reserved Price: " + auctions.get(name).getMax_bid() +", Status: "+status+ ", Description: " + auctions.get(name).get_description()+ "\n";

                        }
                        else{
                            all_auctions += "Name: " + name + ", Best Bid: " + auctions.get(name).getMax_bid() +", Status: "+status+ ", Description: " + auctions.get(name).get_description()+ "\n";
                        }

                    }
                    return all_auctions;
                }
            }
        }
        return null;
    }


    //Removing an auction. Only the creator can remove an auction.
    public boolean removeAnAuction(String _auction_name) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();

            HashMap<String, Auction> auctions;
            if(dataMapValues.isEmpty()){
                return false;
            }
            else{
                auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }

            for (String name : auctions.keySet()) {
                if (name.equals(_auction_name) && auctions.get(name).get_creator()==peer_id ) {
                    auctions.remove(name);
                    dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
                    return true;
                }
            }
        }
        return false;
    }

}
