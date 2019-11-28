package it.unisa.auctionmechanism;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class Auction implements Serializable {

    String _auction_name;
    int _creator;
    Date _end_time;
    Double _reserved_price;
    Double max_bid;
    Double second_max_bid;
    int bid_id;
    PeerAddress peerAddress_bid;
    PeerAddress old_bid_Address;
    String _description;
    HashSet<PeerAddress> users;

    //constructor
    public Auction(String name, int id_creator, Date et, double res_price, String desc){
        _auction_name = name;
        _creator = id_creator;
        _end_time = et;
        _reserved_price = res_price;
        _description = desc;
        max_bid = res_price;
        second_max_bid = res_price;
        bid_id=0;
        users = new HashSet<PeerAddress>();
    }

    public Auction(){
        //void constructor
    }

    //get and set methods


    public void set_reserved_price(Double _reserved_price) {
        this._reserved_price = _reserved_price;
    }

    public void setMax_bid(Double max_bid) {
        this.max_bid = max_bid;
    }

    public Double getSecond_max_bid() {
        return second_max_bid;
    }

    public void setSecond_max_bid(Double second_max_bid) {
        this.second_max_bid = second_max_bid;
    }

    public PeerAddress getOld_bid_Address() {
        return old_bid_Address;
    }

    public void setOld_bid_Address(PeerAddress old_bid_Address) {
        this.old_bid_Address = old_bid_Address;
    }

    public PeerAddress getPeerAddress_bid() {
        return peerAddress_bid;
    }

    public void setPeerAddress_bid(PeerAddress peerAddress_bid) {
        this.peerAddress_bid = peerAddress_bid;
    }


    public int get_creator() {
        return _creator;
    }

    public void set_creator(int _creator) {
        this._creator = _creator;
    }

    public String get_auction_name() {
        return _auction_name;
    }

    public void set_auction_name(String _auction_name) {
        this._auction_name = _auction_name;
    }

    public Date get_end_time() {
        return _end_time;
    }

    public void set_end_time(Date _end_time) {
        this._end_time = _end_time;
    }

    public Double get_reserved_price() {
        return _reserved_price;
    }

    public void set_reserved_price(double _reserved_price) {
        this._reserved_price = _reserved_price;
    }

    public Double getMax_bid() {
        return max_bid;
    }

    public void setMax_bid(double max_bid) {
        this.max_bid = max_bid;
    }

    public int getBid_id() {
        return bid_id;
    }

    public void setBid_id(int bid_id) {
        this.bid_id = bid_id;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public HashSet<PeerAddress> getUsers() {
        return users;
    }

    public void setUsers(HashSet<PeerAddress> users) {
        this.users = users;
    }

    //toString method
    @Override
    public String toString() {
        return "Auction{" +
                "_auction_name='" + _auction_name + '\'' +
                ", _end_time=" + _end_time +
                ", _reserved_price=" + _reserved_price +
                ", max_bid=" + max_bid +
                ", bid_id=" + bid_id +
                ", _description='" + _description + '\'' +
                ", users=" + users +
                '}';
    }
}
