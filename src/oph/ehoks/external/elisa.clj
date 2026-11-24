(ns oph.ehoks.external.elisa
  "Apufunktiot SMS-viestien luomiseen ja lähettämiseen."
  (:require [cheshire.core :refer [generate-string]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.http-client :as client])
  (:import (clojure.lang ExceptionInfo)
           (com.google.i18n.phonenumbers PhoneNumberUtil
                                         NumberParseException)))

(def elisa-url "https://viestipalvelu-api.elisa.fi/api/v1/")

(defn apikey
  "Elisan API key.  Jos sitä ei ole konfiguroitu, viestejä ei lähetetä
  oikeasti vaan ne vain lokitetaan."
  []
  (not-empty (:elisa-sms-apikey config)))

(defn tep-msg-body
  "Luo työpaikkaohjaajakyselyn viestin tekstin."
  [linkki oppilaitokset]
  (str (str/join ", " (map :fi oppilaitokset)) ": "
       "Työpaikkaohjaajakysely - Enkät till arbetsplatshandledaren - "
       "Survey to workplace instructors"
       "\n\n"
       linkki
       "?t=s"
       "\n\n"
       "Kiitos koulutussopimus-/oppisopimusopiskelijoiden ohjaamisesta! "
       "Pyydämme arvioimaan oppilaitoksemme toimintaa ja yhteistyömme "
       "onnistumista."
       "\n\n"
       "Tack för att du handleder studerande på utbildningsavtal/läroavtal! "
       "Utvärdera gärna vår läroanstalts verksamhet och hur vårt samarbete "
       "har lyckats."
       "\n\n"
       "Thank you for guiding students with a training "
       "agreement/apprenticeship! Please evaluate the activities of our "
       "institution and the success of our co-operation."
       "\n\n"
       "Osoitelähde: Opetushallitus, eHOKS-rekisteri"))

(defn tep-muistutus-msg-body
  "Luo työpaikkaohjaajakyselyn muistutuksen viestin tekstin."
  [linkki oppilaitokset]
  (str (str/join ", " (map :fi oppilaitokset)) ": "
       "Muistutus: Työpaikkaohjaajakysely - "
       "Påminnelse: Enkät till arbetsplatshandledaren - "
       "Reminder: Survey to workplace instructors"
       "\n\n"
       linkki
       "?t=s"
       "\n\n"
       "Muistathan antaa meille palautetta. Kiitos, kun vastaat!"
       "\n\n"
       "Kom ihåg att ge din respons till läroanstalten. Tack för att du svarar!"
       "\n\n"
       "Please give your feedback to the institution. Thank you for responding!"
       "\n\n"
       "Osoitelähde: Opetushallitus, eHOKS-rekisteri"))

(defn amis-msg-body
  "Luo opiskelijapalauteviestin tekstin."
  [linkki oppilaitos]
  (str oppilaitos ": Päättökysely - Slutenkät - VET-feedback survey"
       "\n\n"
       linkki
       "?t=s"
       "\n\n"
       "Missä onnistuimme ja mitä voisimme tehdä paremmin? "
       "Kiitos, kun vastaat! Palaute annetaan nimettömänä."
       "\n\n"
       "Vad har vi lyckats med och vad kan vi göra bättre? "
       "Tack för att du svarar! Responsen ges anonymt."
       "\n\n"
       "Where did we succeed? What could we do better? "
       "Thank you for replying! All feedback is anonymous."
       "\n\n"
       "Osoitelähde: Opetushallitus, eHOKS-rekisteri"))

(defn send-sms
  "Lähettää SMS-viestin viestintäpalveluun."
  [number message]
  (if-let [apikey (apikey)]
    (try
      (client/post elisa-url
                   {:headers {:Authorization  (str "apikey " apikey)
                              :content-type "application/json"}
                    :body    (generate-string {:sender "OPH"
                                               :destination [number]
                                               :text message})
                    :as      :json})
      (catch ExceptionInfo e
        (log/error e "Virhe send-sms -funktiossa")
        (throw e)))
    (do
      (log/info "Elisa API key not configured, not sending SMS:" message)
      {:body
       {:messages {(keyword number) {:converted number
                                     :status "mock-lahetys"}}}})))

(def ^PhoneNumberUtil pnutil (PhoneNumberUtil/getInstance))
(def non-alphabetic? (partial every? #(not (Character/isLetter ^Character %))))
(def smsable-phone-type? #{"FIXED_LINE_OR_MOBILE" "MOBILE"})

(defn valid-finnish-number?
  "Sallii vain numeroita, joihin voi lähettää SMS-viestejä ja jotka
  ovat Suomessa (kustannussyistä).  Jos funktio ei hyväksy numeroa,
  jonka tiedät olevan validi, tarkista, miten kirjasto luokittelee sen:
  https://libphonenumber.appspot.com/."
  [number]
  (try
    (let [numberobj (.parse pnutil number "FI")]
      (and (non-alphabetic? number)
           (.isValidNumber pnutil numberobj)
           (= 358 (.getCountryCode numberobj))
           (smsable-phone-type? (str (.getNumberType pnutil numberobj)))))
    (catch NumberParseException e
      (log/warn e "PhoneNumberUtils failed to parse phonenumber")
      false)))
