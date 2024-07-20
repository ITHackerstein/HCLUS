# Backend

## Indice

* [1. Introduzione](#1-introduzione)
* [2. Motivazione](#2-motivazione)
* [3. La nuova rappresentazione](#3-la-nuova-rappresentazione)
* [4. Il nuovo algoritmo](#4-il-nuovo-algoritmo)
  * [4.1. Possibili migliorie](#41-possibili-migliorie)

## 1. Introduzione

Con _backend_ ci riferiamo alle strutture dati e gli algoritmi utilizzati per la rappresentazione e l'estrazione di un 
clustering sul dataset.

Rispetto alla base questi sono state totalmente rivisti per poter avere una struttura dati più semplice e, di 
conseguenza, più semplice e veloce da trasmettere in rete.

## 2. Motivazione

Si è scelto di modificare il _backend_ la mancanza di un'informazione nella precedente struttura dati. 
Nello specifico, durante la visualizzazione del dendrogramma c'è bisogno di conoscere, per ogni livello, quali due cluster 
vengono uniti per ottenere il nuovo in modo da poter visualizzare i collegamenti corretti fra i 3. Supponiamo 
infatti di trovarci nella seguente situazione:
```
livello n + 1: <0> <1> <2> <3> <4 , 5>
livello     n: <0> <1> <2> <3> <4> <5>
```
Come possiamo notare non abbiamo un modo "ovvio" per poter saper quali due cluster del livello `n` vengono uniti, la 
soluzione potrebbe essere il seguente algoritmo:
* Per ogni coppia di cluster `c_1, c_2` nel livello `n`:
  * Unisci i cluster `c_1` e `c_2` e verifica se l'unione si trova nel livello `n + 1`:
    * Se è vero, allora vuol dire che abbiamo trovato i due cluster che vengono uniti e ci possiamo fermare.
    * Se è falso, procedi alla prossima coppia di cluster.

Questo algoritmo, però, risulta essere lento dunque si è deciso di riguardare come il clustering veniva estratto e 
rappresentato.

## 3. La nuova rappresentazione

La nuova rappresentazione del clustering si basa su una semplice osservazione, ovvero che possiamo riguardare un 
clustering come la sequenza di passi che si esegue per estrarlo, dove per passo intendiamo la scelta di due cluster 
e la loro unione.

Dunque vengono rimosse le classi `Cluster`, `ClusterSet` e `Dendrogram` e vengono sostituite da `ClusteringStep` e 
`Clustering` dove la prima non è altro che l'aggregazione dei due indici dei cluster scelti per l'unione e la 
dimensione del nuovo cluster (data dalla somma delle dimensioni dei cluster scelti) e la seconda è l'aggregazione 
del numero di esempi di partenza (necessario nel caso in cui si scelga di generare un dendrogramma non completo).

## 4. Il nuovo algoritmo

Il vecchio algoritmo presentava un problema principale, ovvero il calcolo ridondante delle stesse distanze fra esempi.

Il nuovo algoritmo, invece, fa due semplici osservazioni:
* invece che utilizzare direttamente il dataset per il calcolo delle distanze possiamo sfruttare la sua matrice 
  triangolare di distanza, ovvero la matrice dove un elemento in posizione `(i, j)` contiene la distanza fra 
  l'esempio `i` e l'esempio `j` nel dataset;
* una volta scelti i due cluster da unire ne possiamo scartare uno e rimpiazzare l'altro con il nuovo cluster;
* possiamo vedere l'operazione di calcolo delle distanze con un approccio diverso, invece che calcolare la distanza 
  fra due cluster per poi selezionare la coppia più vicina, possiamo aggiornare la matrice delle distanze.

  Dunque l'operazione diventa la seguente: scelti due cluster (`x` e `y`) quale è la distanza tra il nuovo cluster 
  `x U y` e tutti gli altri cluster `i`?

L'algoritmo è il seguente, dati `depth` (profondità desiderata), `distanza` (algoritmo per il calcolo della distanza)
e `dataset` (il dataset) restituisce una sequenza di passi `steps` facendo le seguenti operazioni:
* Calcola la matrice delle distanze di `dataset`;
* Ripeti `depth - 1` volte:
  * Trova gli indici dei due cluster (`x` e `y`) più vicini usando la matrice delle distanze;
  * Alla lista dei passi si aggiunga il nuovo passo composto da `x`, `y` e la dimensione del nuovo cluster;
  * Aggiorna la matrice delle distanze.

> **NOTA**: L'algoritmo dato sopra è una leggera semplificazione di ciò che accade in realtà.

### 4.1. Possibili migliorie

L'algoritmo fornito sopra funziona bene per questo caso d'uso ma potrebbe essere ulteriormente migliorato:
* per prima cosa la matrice delle distanze potrebbe essere memorizzata in maniera più efficiente in termini di 
  spazio: questa, infatti, è simmetrica rispetto alla diagonale principale per cui si potrebbe memorizzare solo la 
  porzione al di sopra di essa in un array mono-dimensionale e convertire una posizione `(i, j)` nella rispettiva 
  posizione di quest'ultimo.
* l'algoritmo fornito è generico per ogni tipo di distanza ma risulta essere, comunque, piuttosto inefficiente.

  La sua complessità, infatti, è `O(n³)` in tempo e `O(n²)` in spazio.

  Questa però può essere migliorata in primo luogo utilizzando una heap per arrivare a `O(n² log(n))` e poi, per 
  alcuni metodi di calcolo della distanza, esistono delle versioni più efficienti.

  Ad esempio per la distanza Single-Link e Complete-Link esistono
  
  > R. Sibson, SLINK: An optimally efficient algorithm for the single-link cluster method, The Computer Journal, Volume 16, Issue 1, 1973, Pages 30–34, https://doi.org/10.1093/comjnl/16.1.30
  
  > D. Defays, An efficient algorithm for a complete link method, The Computer Journal, Volume 20, Issue 4, 1977, Pages 364–366, https://doi.org/10.1093/comjnl/20.4.364

  che riescono a ottenere una complessità `O(n²)`.