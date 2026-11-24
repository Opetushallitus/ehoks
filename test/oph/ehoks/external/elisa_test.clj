(ns oph.heratepalvelu.external.elisa-test
  (:require [cheshire.core :refer [generate-string]]
            [clojure.test :refer :all]
            [oph.heratepalvelu.external.elisa :as elisa]
            [oph.heratepalvelu.test-util :as tu])
  (:import (clojure.lang ExceptionInfo)))

(use-fixtures :each tu/clear-logs-before-test)

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
    (with-redefs [clojure.tools.logging/log* tu/mock-log*
                  environ.core/env {:send-messages "false"}]
      (let [number "12345"
            message "Test message"
            results {:body {:messages {:12345 {:converted "12345"
                                               :status "mock-lahetys"}}}}]
        (is (= (elisa/send-sms number message) results))
        (is (tu/logs-contain? {:level :info :message message}))))))

(defn- mock-client-post [url options] {:url url :options options})

(deftest test-send-sms-no-error
  (testing "Varmista, että send-sms kutsuu client/post oikein"
    (with-redefs [environ.core/env {:send-messages "true"}
                  oph.heratepalvelu.external.elisa/apikey (delay "asdf")
                  oph.heratepalvelu.external.http-client/post mock-client-post]
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
    (with-redefs [clojure.tools.logging/log* tu/mock-log*
                  environ.core/env {:send-messages "true"}
                  oph.heratepalvelu.external.elisa/apikey (delay "asdf")
                  oph.heratepalvelu.external.http-client/post
                  mock-client-post-with-error]
      (let [number "12345"
            message "Test message"
            expected-log-line "Virhe send-sms -funktiossa"]
        (is (thrown? ExceptionInfo (elisa/send-sms number message)))
        (is (tu/logs-contain? {:level :error :message expected-log-line}))
        (is (tu/logs-contain-matching? :error #"(?s).*ABCDE.*"))))))

(deftest test-valid-finnish-number?
  (testing
    "Funktio valid-finnish-number? tunnistaa puhelinnumeroja oikein"
    (is (valid-finnish-number? "040 654 3210"))
    (is (valid-finnish-number? "+35840 654 3210"))
    (is (valid-finnish-number? "+35845 333 377"))
    (is (not (valid-finnish-number? "040")))
    (is (not (valid-finnish-number? "+4144 123 4567")))
    (is (not (valid-finnish-number? "+1 517 987 5432")))
    (is (not (valid-finnish-number? "laksj fdaiu fd098098asdf")))
    (is (not (valid-finnish-number? "+358 40 987 6543à")))))
