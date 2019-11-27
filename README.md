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
- bid_id, l'id del miglior offerente
- peerAddress_bid, il peer address del miglior offerente
- old_bid_Address, il peer address del precedente miglior offerente
- users, lista di peer partecipanti all'asta


