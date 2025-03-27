# Integraatiot

Tässä on Alla on eHOKS-palvelun integraatiot lyhyesti kuvattuja, sekä linkit,
joiden takaa löytyvät palveluiden tai palvelukokonaisuuksien tarkempi kuvaus,
sekä oleellisten vastuuhenkilöiden yhteystiedot.

## Arvo

Arvo on opetushallinnon alueelle räätälöity kyselytiedonkeruun järjestelmä
(palvelun tarkempi kuvaus sekä oleelliset yhteystiedot löytyvät [tältä
Eduuni-wikin sivulta](https://wiki.eduuni.fi/display/CscArvo)). eHOKS lähettää
erityyppisten palautekyselyiden taustatiedot Arvoon, ja Arvo puolestaan
muodostaa näiden taustatietojen perusteella palautekyselyt. eHOKS vastaa
kyselylinkkien lähettämisestä kyselyjen vastaajille, hyödyntäen
sähköpostilähetyksessä
[Viestinvälityspalvelua](#viestinv%C3%A4lityspalvelu) ja tekstiviestilähetyksessä
[Elisa Dialogia](#elisa-dialogi).

## OTUVA

[OTUVA](https://wiki.eduuni.fi/display/OPHPALV/OTUVA+Opetushallituksen+tunnistus-+ja+valtuutuspalvelu)
on Opetushallituksen tunnistus- ja valtuutuspalvelu, joka koostuu useasta
erillisestä sovelluksesta. eHOKS hyödyntää näistä kolmea: Käyttöoikeuspalvelua,
cas-virkailijaa sekä cas-oppijaa. Käyttöoikeuspalvelusta eHOKS hakee virkailija-
ja palvelukäyttäjien käyttöoikeuksia, tarkistaakseen riittävätkö käyttäjän
käyttöoikeudet pyydettyihin operaatiohin. cas-virkailija toimii
kertakirjautumisjärjestelmänä (SSO) virkailija- ja palvelukäyttäjille
Virkailijan Opintopolkuun. cas-oppija toimii vastaavasti
kertakirjaumisjärjestelmänä oppijan Oma Opintopolkuun. Opintopolun palveluiden
autentikaatio pohjautuu CAS-protokollaan. Tunnistautumisen jälkeen palvelut, ml.
eHOKS, huolehtivat itse SSO-sessionhallinnasta. CAS-integraation tarkempi kuvaus
löytyy
[täältä](https://github.com/Opetushallitus/otuva/blob/master/cas-virkailija/cas-integration.md).

## ePerusteet & AMOSAA

eHOKS hakee ePerusteet-palvelun julkisen rajapinnan kautta tietoja tutkintojen
ja koulutusten perusteista, näytettäväksi eHOKS-virkailijan ja eHOKS-oppijan
käyttöliittymänäkymiin. Näitä tietoja haetaan [Koodiston](#koodisto) koodiUria
käyttäen. Palvelukokonaisuuden tarkempi kuvaus sekä vastuuhenkilöiden
yhteystiedot löytyvät Eduuni-wikistä
[ePerusteet-palvelukortilta](https://wiki.eduuni.fi/display/OPHPALV/Palvelukortti).

[AMOSAA](https://wiki.eduuni.fi/display/OPHPALV/ePerusteet+AMOSAA) on
koulutuksenjärjestäjille tarkoitettu työkalu
opetussuunnitelmien, tutkintokohtaisten toteutussuunnitelmien ja suorituspolkujen
kuvaamiseen ja julkaisuun.

TODO

## Elisa Dialogi

[Elisa Dialogi](https://docs.dialogi.elisa.fi/docs/dialogi/send-sms)
on Elisan yrityksille tarjoama palvelu kohdennettujen tekstiviestien
lähetykseen. eHOKS hyödyntää palvelua palautekyselylinkkien lähettämiseen
tekstiviestitse kyselyjen vastaajille.

## Herätepalvelu

[Herätepalvelu](https://github.com/Opetushallitus/heratepalvelu) on tähän
mennessä huolehtinut opiskelijapalautteisiin sekä työelämäpalautteisiin
liittyvistä prosesseista. Palvelusta vastaavat samat henkilöt kuin eHOKSista.
Kirjoitushetkellä (29.1.2025), Herätepalvelua ollaan korvaamassa eHOKSiin
läheisimmin integroituvalla palaute-backendillä. Tämän korvausprosessin
tavoitteita ja vaiheita on tarkemmin kuvattu [tällä
Eduuni-wikisivulla](https://wiki.eduuni.fi/pages/viewpage.action?pageId=415248744).
Prosessin etenemistä puolestaan voi seurata Eduuni Jirasta eHOKS-projektin alle
luodusta [epicistä](https://jira.eduuni.fi/browse/EH-1495).

## Koodisto

[Koodisto](https://wiki.eduuni.fi/display/OPHPALV/Koodistopalvelu) on OPH:n
keskitetty tallennus- ja hallintapaikka koodistoille. eHOKSin käyttöliittymä
pyytää Koodiston koodeja (esim. urasuunnitelma, tutkinnon osat, osaamisen
hankkimistapa, yms.) käyttäen tietoja [ePerusteista](#eperusteet--amosaa)
välillisesti eHOKS-backendin kautta.

## Koski

Nykyisellään eHOKSin toiminta on suuressa määrin riippuvainen
[Koskesta](https://wiki.eduuni.fi/spaces/OPHPALV/pages/190613272/KOSKI-palvelukortti),
sillä tietoja joudutaan hakemaan kyseisestä palvelusta varsin usein. Kullekin
aktiiviselle hoksille pitää olla olemassa voimassa oleva opiskeluoikeus, joten
hoksien tallennuksen (ml. päivitys) yhteydessä Koskesta haetaan aina
ajantasaisimmat opiskeluoikeuden tiedot. Lisäksi palautekyselyiden muodostamista
varten, opiskeluoikeuden tiedoista on haettava useita eri tietoja (mm.
koulutustoimija, suorituksen tyyppi, opiskeluoikeuden keskeytymisajanjaksot,
ym.).

## Lokalisointipalvelu

[Lokalisointipalvelu](https://wiki.eduuni.fi/display/ophpolku/Lokalisointipalvelu)
huolehtii käännöksistä (ruotsi ja englanti) eHOKSin käyttöliittymässä.

## Oppijanumerorekisteri (ONR)

Palvelun tarkempi kuvaus, dokumentaatio sekä vastuuhenkilöiden yhteystiedot
löytyvät
[palvelukortilta](https://wiki.eduuni.fi/display/OPHPALV/Oppijanumerorekisterin+palvelukortti).
eHOKSiin ei ole tallennettu oppijoiden henkilötietoja, vaan eHOKSin
käyttöliittymässä näytettävät henkilötiedot haetaan suoraan ONR:stä
käyttöliittymänäkymän muodostamisen hetkellä.

## Organisaatiopalvelu

[Organisaatiopalvelussa](https://wiki.eduuni.fi/display/OPHPALV/Organisaatiopalvelu)
ylläpidetään koulutustoimijoiden, näiden ylläpitämien oppilaitosten ja
toimipisteiden sekä oppisopimustoimipisteiden tietoja. eHOKS hyödyntää
Organisaatiopalvelun tarjoamia tietoja käyttäjien käyttöoikeusien tarkistamiseen
sekä erinäisiin validointeihin. Tietoja haetaan palvelusta myös osaksi
palautekyselyjen taustatietoja.

## Viestinvälityspalvelu

Viestinvälityspalvelu on OPH:n oma viestinvälitykseen tarkoitettu palvelu. eHOKS
hyödyntää Viestinvälityspalvelua palautekyselylinkkien lähettämiseen vastaajille
sähköpostitse. Viestinvälityspalvelua on uudistettu hiljattain
(kirjoitushetkellä 29.1.2025), uudistetun palvelun tarkemmat tiedot sekä
yhteyshenkilöt löytyvät [tältä Eduuni-wikin
sivulta](https://wiki.eduuni.fi/pages/viewpage.action?pageId=358075229).

## Vipunen

[Vipunen](https://vipunen.fi/fi-fi) on opetushallinnon tilastopalvelu, jonka
sisällöstä vastaavat yhdessä opetus- ja kulttuuriministeriö ja Opetushallitus.

## Lampi

Opetushallituksen tietoallas.

TODO
