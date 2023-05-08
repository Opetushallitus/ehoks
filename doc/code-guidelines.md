## Koodikäytänteet

### Clojure

eHOKS-projektissa (sekä mahdollisesti muissa tähän dokumenttiin viittaavissa
projekteissa) noudatetaan pääsääntöisesti [Clojure-yhteisön määrittämää
tyyliopasta](https://github.com/bbatsov/clojure-style-guide). Näihin
käytänteisiin on kuitenkin sovittu kehittäjien kesken joitain poikkeuksia,
jotka on tarkemmin kuvattuna alla.

#### Funktioiden ja muuttujien nimeäminen

Muuttujat nimetään sen mukaan, mitä ne sisältävät, yleensä
mahdollisimman semanttisella tasolla.  Esimerkiksi hyvä nimi
muuttujalle, jossa on lista HOKSeista, jotka ovat muuttuneet, on
`changed-hoksit` mieluummin kuin `hoksit` mieluummin kuin `maps`
mieluummin kuin `coll` mieluummin kuin `obj`.

Funktiot nimetään sen mukaan, mitä ne palauttavat, esimerkiksi
`oppija-contact-details`.  Joskus nimeen voi ottaa myös vihjeitä siitä,
mitä ensimmäinen positionaalinen argumentti tekee, esimerkiksi
`hoks-by-opiskeluoikeus-oid`.  Jos funktio muuttaa funktion ulkopuolista
tilaa, funktion nimessä pitää kuvailla, mitä tilaa se muuttaa ja miten.
Lisäksi funktio on tällöin huutomerkillinen, katso alla.

Älä tee funktioita, jotka sekä muuttavat funktion ulkopuolista tilaa
että palauttavat jotain (muuta kuin nil).

#### Huutomerkin käyttö funktion nimessä

Clojure-yhteisön tyylioppaasta poiketen, projektissa **epäpuhtaat funktiot
merkitään funktion nimen päättävällä huutomerkillä**. Funktion katsotaan olevan
*epäpuhdas*, jos sillä on toinen tai molemmat seuraavista ominaisuuksista:

1. Funktio voi tuottaa samoilla parametreilla eri kutsukerralla eri paluuarvon.
2. Funktiolla on sivuvaikutuksia, ts. se muuttaa jollakin tapaa funktion
   ulkopuolista tilaa. 

<details>
<summary>Esimerkkejä</summary>

Lähtökohtaisesti kaikki I/O-operaatioita (tietokantakyselyt, rajapintakutsut,
tiedoston lukeminen/kirjoittaminen, yms.) sisältävät funktiot ovat epäpuhtaita.
Kehittäjien kesken on kuitenkin sovittu joitain poikkeuksia (kts.
"*Poikkeuksia*" alta). Alla on listattuna (yllä mainittujen lisäksi) joitain
esimerkkejä epäpuhtaista funktioista:

* funktio muuttaa muuttujan arvoa tämän näkyvyysalueen (scope) ulkopuolelta 
* funktio jossa muutetaan parametrina (referenssinä) saatua oliota
</details>

<details>
<summary>Poikkeuksia</summary>
Kehittäjien kesken on sovittu, että funktioiden puhtauden suhteen ei olla
turhan tiukkoja. Erityisesti tilanteissa, joissa funktio tuottaa samoilla
parametreilla aina saman paluuarvon, funktiota ei välttämättä kannata merkitä
sivuvaikutukselliseksi. Esimerkiksi, jos funktio sisältää yhden tai useamman
seuraavista, ei huutomerkkiä tarvitse laittaa funktion nimeen:

* lokitukset
* konsoliin tulostamiset
* puhtaiden funktioiden / lausekkeiden memoisointi, delayt, cachet yms.

Muitakin vastaavanlaisia poikkeuksia varmasti löytyy. Jos jokin tietty tapaus
askarruttaa, kannattaa menettelytavasta keskustella ja sopia muiden kehittäjien
kanssa.
</details>

<details>
<summary>Motivaatio käytännön taustalla</summary>
Jakamalla funkiot puhtaisiin ja epäpuhtaisiin funktioihin, voidaan
parhaimmillaan saavuttaa seuraavat hyödyt:

* Yksinkertaisempaa ja toimintavarmempaa koodia
  - Jaottelu kannustaa strukturoimaan koodia pienemmiksi osiksi (mikä kuuluu
    toki muutenkin hyviin ohjelmointikäytänteisiin)
  - Virheenkäsittely rajoittuu suurelta osin epäpuhtaisiin funktioihin
  - Koodi on yleisesti ottaen helpommin järkeiltävissä ja debugattavissa
* Helpompi testattavuus
  - Puhtaat funktiot ovat deterministisiä ja helppoja testata
  - Mockeja vaativia epäpuhtaita funktioita on rajatusti
</details>

##### Koodin strukturoinnista

Jos kirjoittamasi funktio sisältää yhden tai useamman epäpuhtaan
funktion, myös uusi kirjoitettu funktio on automaattisesti
epäpuhdas. Koodia kannattaakin pyrkiä strukturoimaan siten, että
**sivuvaikutuksetonta koodia eriytetään mahdollisimman paljon omiin
funktioihinsa**. Näin vältytään tilanteelta, jossa huomattavan suuri osa
koodikannan funktioista on lopulta epäpuhtaita, ja uusien
puhtaiden funktioiden kirjoittamisesta tulee suorastaan mahdotonta.
Kutsupinossa alempana olevat funktiot kannattaa laatia niin, että ne
hyödyntävät puhtaita ja epäpuhtaita funktiota
["impure-pure-impure sandwich"
-periaatteella](https://blog.ploeh.dk/2020/03/02/impureim-sandwich/).

<details>
<summary>Lisälukemista</summary>

* [Discerning and maintaining purity](https://blog.ploeh.dk/2020/02/24/discerning-and-maintaining-purity/)
* [Functional architecture is Ports and Adapters](https://blog.ploeh.dk/2016/03/18/functional-architecture-is-ports-and-adapters/)
</details>
