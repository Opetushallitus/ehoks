## Kuvaus muutoksista

TODO

## PR:iin liittyvä Jira-tiketti (tunniste ja linkki)

TODO

## Muistilista PR:n tekijälle ja katselmoijille

### Ennen asettamista katselmointiin
  - [ ] Toiminnallisuuden kattavat yksikkötestit on tehty osana PR:ia
  - [ ] Olemassa olevat testit menevät muustoten jälkeen läpi
  - [ ] PR:n sisältämät muutokset noudattavat [sovittuja koodikäytänteitä](https://github.com/Opetushallitus/ehoks)
  - [ ] Koodi on riittävästi dokumentoitu tai se on muuten yksiselitteistä
    - Uskotko, että toinen kehittäjä ymmärtää nopeasti koodia lukemalla, mitä koodi tekee?
  - [ ] Koodimuutokset läpäisevät automaattisesti ajettavat linterit
    - Nämä ovat ajettavissa komennolla `lein checkall`

❗ **Katselmoijat tarkastavat, että yllä mainitut kohdat toteutuvat**

### Ennen mergeämistä `master`-haaralle
  - [ ] Vähintään yksi kehittäjä on katselmoinut ja hyväksynyt muutokset
    - Jos muutoksilla voi olla jotain rikkoessaan kauaskantoiset vaikutukset, kannattaa muutokset hyväksyttää useammalla katselmoijalla
  - [ ] Katselmoijien esittämät muutosehdotukset on huomioitu
  - [ ] Muutokset on testattu QA-ympäristössä
  - [ ] Tuotantoasennuksen ajankohdasta on sovittu
