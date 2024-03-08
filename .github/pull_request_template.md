## Kuvaus muutoksista

TODO: kirjoita

TODO: PR:iin liittyvä Jira-tiketti (tunniste ja linkki)

## Muistilista PR:n tekijälle ja katselmoijille

### Ennen asettamista katselmointiin
  - [ ] Build onnistuu ilman virheitä
  - [ ] Toiminnallisuuden kattavat yksikkötestit on tehty osana PR:ia
  - [ ] PR:n sisältämät muutokset noudattavat [sovittuja koodikäytänteitä](../doc/code-guidelines.md)
  - [ ] Koodi on riittävästi dokumentoitu tai se on muuten yksiselitteistä
  - [ ] Nimet (muuttujat, funktiot, ...) kuvaavat koodia hyvin

❗ **Katselmoijat tarkastavat, että yllä mainitut kohdat toteutuvat**

### Ennen mergeämistä `master`-haaralle
  - [ ] Vähintään yksi kehittäjä on katselmoinut ja hyväksynyt muutokset
    - Jos muutoksilla voi jotain rikkoessaan olla kauaskantoiset vaikutukset, kannattaa muutokset hyväksyttää useammalla katselmoijalla
  - [ ] Katselmoijien esittämät muutosehdotukset on huomioitu
  - [ ] Muutokset on testattu QA-ympäristössä
    - [ ] Testausohje kirjoitettu
    - [ ] Testaus delegoitu OPH:lle mikäli mahdollista
  - [ ] Yli jääneet kehityskohteet on tiketöity
