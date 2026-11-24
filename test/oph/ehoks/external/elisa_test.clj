(ns oph.ehoks.external.elisa-test
  (:require [cheshire.core :refer [generate-string]]
            [clojure.test :refer [deftest is testing]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [oph.ehoks.external.elisa :as elisa])
  (:import (clojure.lang ExceptionInfo)))

(def mock-tep-msg-body
  (str "Testilaitos, Testilaitos 2: Työpaikkaohjaajakysely - "
       "Enkät till arbetsplatshandledaren - Survey to workplace instructors"
       "\n\nhttps://kysely.linkki/123?t=s\n\nKiitos koulutussopimus-/"
       "oppisopimusopiskelijoiden ohjaamisesta! Pyydämme arvioimaan "
       "oppilaitoksemme toimintaa ja yhteistyömme onnistumista.\n\n"
       "Tack för att du handleder studerande på utbildningsavtal/läroavtal! "
       "Utvärdera gärna vår läroanstalts verksamhet och hur vårt samarbete "
       "har lyckats.\n\nThank you for guiding students with a training "
       "agreement/apprenticeship! Please evaluate the activities of our "
       "institution and the success of our co-operation."
       "\n\nOsoitelähde: Opetushallitus, eHOKS-rekisteri"))

(deftest test-tep-msg-body
  (testing "Varmista, että msg-body tekee formatointia oikein"
    (is (= (elisa/tep-msg-body "https://kysely.linkki/123"
                               [{:fi "Testilaitos"}
                                {:fi "Testilaitos 2"}])
           mock-tep-msg-body))))

(def mock-tep-muistutus-msg-body
  (str "Testilaitos, Testilaitos 2: Muistutus: Työpaikkaohjaajakysely - "
       "Påminnelse: Enkät till arbetsplatshandledaren - Reminder: Survey to "
       "workplace instructors\n\n"
       "https://kysely.linkki/123?t=s\n\nMuistathan antaa meille "
       "palautetta. Kiitos, kun vastaat!\n\nKom ihåg att ge din respons till "
       "läroanstalten. Tack för att du svarar!\n\nPlease give your feedback to "
       "the institution. Thank you for responding!"
       "\n\nOsoitelähde: Opetushallitus, eHOKS-rekisteri"))

(deftest test-tep-muistutus-msg-body
  (testing "Varmista, että muistutus-msg-body tekee formatointia oikein"
    (is (= (elisa/tep-muistutus-msg-body
             "https://kysely.linkki/123"
             [{:fi "Testilaitos"} {:fi "Testilaitos 2"}])
           mock-tep-muistutus-msg-body))))

(def mock-amis-msg-body
  (str "Testilaitos: Päättökysely - Slutenkät - VET-feedback survey\n\n"
       "https://kysely.linkki/123?t=s\n\nMissä onnistuimme ja mitä voisimme "
       "tehdä paremmin? Kiitos, kun vastaat! Palaute annetaan nimettömänä.\n\n"
       "Vad har vi lyckats med och vad kan vi göra bättre? Tack för att du "
       "svarar! Responsen ges anonymt.\n\nWhere did we succeed? What could we "
       "do better? Thank you for replying! All feedback is anonymous.\n\n"
       "Osoitelähde: Opetushallitus, eHOKS-rekisteri"))

(deftest test-amis-msg-body
  (testing "Varmista, että amis-msg-body tekee formatointia oikein"
    (is (= (elisa/amis-msg-body "https://kysely.linkki/123" "Testilaitos")
           mock-amis-msg-body))))

(deftest test-send-sms-send-messages-disabled
  (testing "send-sms palauttaa oikean arvon, kun viestit eivät lähde"
    (with-redefs [oph.ehoks.config/config {:elisa-sms-apikey ""}]
      (with-log
        (let [number "12345"
              message "Test message"
              results {:body {:messages {:12345 {:converted "12345"
                                                 :status "mock-lahetys"}}}}]
          (is (= (elisa/send-sms number message) results))
          (is (logged? 'oph.ehoks.external.elisa :info
                       #"(?s).*Test message.*")))))))

(defn- mock-client-post [url options] {:url url :options options})

(deftest test-send-sms-no-error
  (testing "Varmista, että send-sms kutsuu client/post oikein"
    (with-redefs [oph.ehoks.config/config {:elisa-sms-apikey "asdf"}
                  oph.ehoks.external.http-client/post mock-client-post]
      (let [number "12345"
            message "Test message"
            results {:url "https://viestipalvelu-api.elisa.fi/api/v1/"
                     :options {:headers {:Authorization "apikey asdf"
                                         :content-type "application/json"}
                               :body    (generate-string
                                          {:sender "OPH"
                                           :destination ["12345"]
                                           :text "Test message"})
                               :as      :json}}]
        (is (= (elisa/send-sms number message) results))))))

(defn- mock-client-post-with-error [_ _] (throw (ex-info "ABCDE" {})))

(deftest test-send-sms-with-error
  (testing "Varmista, että send-sms käsittelee virheitä oikein"
    (with-redefs [oph.ehoks.config/config {:elisa-sms-apikey "asdf"}
                  oph.ehoks.external.http-client/post
                  mock-client-post-with-error]
      (with-log
        (let [number "12345"
              message "Test message"]
          (is (thrown? ExceptionInfo (elisa/send-sms number message)))
          (is (logged? 'oph.ehoks.external.elisa :error
                       [ExceptionInfo #"ABCDE"]
                       #"Virhe send-sms -funktiossa")))))))

(deftest test-valid-finnish-number?
  (testing
    "Funktio valid-finnish-number? tunnistaa puhelinnumeroja oikein"
    (is (elisa/valid-finnish-number? "040 654 3210"))
    (is (elisa/valid-finnish-number? "+35840 654 3210"))
    (is (elisa/valid-finnish-number? "+35845 333 377"))
    (is (not (elisa/valid-finnish-number? "040")))
    (is (not (elisa/valid-finnish-number? "+4144 123 4567")))
    (is (not (elisa/valid-finnish-number? "+1 517 987 5432")))
    (is (not (elisa/valid-finnish-number? "laksj fdaiu fd098098asdf")))
    (is (not (elisa/valid-finnish-number? "+358 40 987 6543à")))))
