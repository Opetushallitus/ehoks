## Kuvaus muutoksista

TODO

## PR:iin liittyvä Jira-tiketti (tunniste ja linkki)

TODO

## Muistilista PR:n tekijälle ja katselmoijille

### Ennen asettamista katselmointiin
  - [ ] Toiminnallisuuden kattavat yksikkötestit on tehty osana PR:ia
  - [ ] Olemassa olevat testit menevät muutosten jälkeen läpi
  - [ ] PR:n sisältämät muutokset noudattavat [sovittuja koodikäytänteitä](../doc/code-guidelines.md)
  - [ ] Koodi on riittävästi dokumentoitu tai se on muuten yksiselitteistä
  - [ ] Koodimuutokset läpäisevät automaattisesti ajettavat linterit

❗ **Katselmoijat tarkastavat, että yllä mainitut kohdat toteutuvat**

### Ennen mergeämistä `master`-haaralle
  - [ ] Vähintään yksi kehittäjä on katselmoinut ja hyväksynyt muutokset
    - Jos muutoksilla voi jotain rikkoessaan olla kauaskantoiset vaikutukset, kannattaa muutokset hyväksyttää useammalla katselmoijalla
  - [ ] Katselmoijien esittämät muutosehdotukset on huomioitu
  - [ ] Muutokset on testattu QA-ympäristössä
  - [ ] Tuotantoasennuksen ajankohdasta on sovittu