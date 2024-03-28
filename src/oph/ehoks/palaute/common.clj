(ns oph.ehoks.palaute.common)

(def palautetilat
  {:ei-laheteta "ei_laheteta"
   :kysely-muodostettu "kysely_muodostettu"
   :lahetetty "lahetetty"
   :lahetys-epaonnistunut "lahetys_epaonnistunut"
   :niputettu "niputettu"
   :odottaa-kasittelya "odottaa_kasittelya"
   :tpk-niputettu "tpk_niputettu"
   :vastaajatunnus-muodostettu "vastaajatunnus_muodostettu"
   :vastattu "vastattu"
   :vastausaika-loppunut "vastausaika_loppunut"})

(def kyselytyypit
  {:aloittaneet "aloittaneet"
   :valmistuneet "valmistuneet"
   :osia-suorittaneet "osia_suorittaneet"
   :tyopaikkajakson-suorittaneet "tyopaikkajakson_suorittaneet"
   :tpo-nippu "tpo_nippu"
   :tpk-nippu "tpk_nippu"})

(def heratelahteet
  {:ehoks-update "ehoks_update"
   :koski-update "koski_update"
   :niputus "niputus"})

(def lahetystilat
  {:odottaa-lahetysta "odottaa_lahetysta"
   :lahetetty "lahetetty"
   :lahetys-epaonnistunut "lahetys_epaonnistunut"})

(def tapahtumatyypit
  {:arvo-luonti "arvo_luonti"
   :niputus "niputus"
   :tpk-niputus "tpk_niputus"})

(def viestityypit
  {:email "email"
   :sms "sms"
   :email-muistutus-1 "email_muistutus_1"
   :sms-muistutus-1 "sms_muistutus_1"
   :email-muistutus-2 "email_muistutus_2"
   :sms-muistutus-2 "sms_muistutus_2"})
