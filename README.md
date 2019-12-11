# Auction Mechanism
Un meccanismo di aste basato su una rete P2P. Ogni peer può vendere e acquistare beni utilizzando un meccanismo di Second-Price Auctions (EBay). Quest'ultimo è un meccanismo di asta non veritiero per più oggetti. Ogni offerente effettua un'offerta. Il miglior offerente ottiene il primo slot, il secondo più alto, il secondo slot e così via, ma il miglior offerente paga il prezzo offerto dal secondo miglior offerente, il secondo più alto paga il prezzo offerto dal terzo più alto, e presto. Il sistema consente agli utenti di creare una nuova asta (con un orario di fine, un prezzo di vendita riservato e una descrizione), controllare lo stato di un'asta e infine fare una nuova offerta per un'asta. Come descritto nell'API Java di AuctionMechanism.

# Implementazione
La classe Auction è costituita dalle seguenti variabili di istanza:

- _auction_name, il nome dell'asta
- _creator, l'id del peer creatore dell'asta
- _end_time, data e ora di scadenza dell'asta
- _reserved_price, il tempo di partenza
- _description, descrizione del prodotto messo all'asta
- max_bid, la migliore offerta ricevuta
- second_max_bid, la seconda migliore offerta ricevuta
- bid_id, l'id del miglior offerente
- peerAddress_bid, il peer address del miglior offerente
- old_bid_Address, il peer address del precedente miglior offerente
- users, lista di peer partecipanti all'asta

Di seguito è riportata l'implementazione di tale classe:

```
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
    
    //toString() method
    @Override
    public String toString() {
        return "Auction{" +
                "_auction_name='" + _auction_name + '\'' +
                ", _creator=" + _creator +
                ", _end_time=" + _end_time +
                ", _reserved_price=" + _reserved_price +
                ", max_bid=" + max_bid +
                ", second_max_bid=" + second_max_bid +
                ", bid_id=" + bid_id +
                ", peerAddress_bid=" + peerAddress_bid +
                ", old_bid_Address=" + old_bid_Address +
                ", _description='" + _description + '\'' +
                ", users=" + users +
                '}';
    }
}

```

## Interfaccia AuctionMechanism
L'interfaccia fornita per lo sviluppo del progetto AuctionMechanism è costituita dai seguenti metodi:

1. createAuction: per creare un'asta
2. checkAuction: per verificare lo stato dell'asta
3. placeAbid: per fare un'offerta

#### Metodo createAuction
Il metodo createAuction prende in input i seguenti valori:
    - _auction_name, nome dell'asta
    - _end_time, tempo di terminazione dell'asta
    - _reserved_price, prezzo di partenza dell'asta
    - _description, descrizione dell'oggetto in asta
    
Tale funzione si sviluppa attraverso i seguenti step:
1. Controlla che non sia già esistente un asta con il medesimo nome 
2. Crea una nuova asta con tutti parametri ricevuti.
3. Ricerca la presenza della lista di aste all'interno della dht.
4. In caso affermativo ottiene tale lista, aggiunge l'asta ad essa e la ricarica nella dht. 
    
##### Implementazione 
```
public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) throws IOException, ClassNotFoundException {

        if(checkAuction(_auction_name) == null){

            Auction auction = new Auction(_auction_name,  peer_id,_end_time, _reserved_price,_description);
            FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();

            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
               auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }
            
            auctions.put(_auction_name,auction);
            dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
            return true;
        }
        return false;
    }
```

#### Metodo checkAuction

##### Implementazione
```
 public String checkAuction(String _auction_name) throws IOException, ClassNotFoundException {

        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            if (futureGet.isEmpty()) {
                dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
                return null;
            }
            
            HashMap<String, Auction> auctions;
            auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            
            if(auctions.containsKey(_auction_name)) {
                Auction auction = auctions.get(_auction_name);
                Date actual_date = new Date();

                if (actual_date.after(auction.get_end_time())) {
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
```
