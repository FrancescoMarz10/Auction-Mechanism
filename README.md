# Auction Mechanism
Ogni peer può vendere e acquistare beni utilizzando un meccanismo di Second-Price Auction. Ogni partecipante all'asta fa un'offerta, il migliore offerente vince l'asta e paga il prezzo della seconda offerta più alta. Il sistema permette agli utenti di:

1. Creare nuove aste
2. Verificare lo stato di un'asta
3. Effettuare una nuova offerta per un'asta

## Autore: Marzullo Francesco

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


