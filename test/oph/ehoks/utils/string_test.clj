(ns oph.ehoks.utils.string-test
  (:require [clojure.test :refer [are deftest is testing]]
            [oph.ehoks.utils.string :refer [normalize]]))

(deftest test-normalize
  (testing "Characters with diacritics are converted to ASCII characters."
    (is (= (normalize "äåéëúíóöáïñ") "aaeeuiooain")))
  (testing "Whitespace characters are replaced with underscore (`_`)."
    (are [string expected] (= (normalize string) expected)
      "a b" "a_b"   "a\nb" "a_b"   "a\tb" "a_b"   "a\r\nb" "a_b"))
  (testing "Uppercase characters will be converted to lower case."
    (is (= (normalize "ABCDEF") "abcdef")))
  (testing "There won't be consecutive underscore characters."
    (are [string expected] (= (normalize string) expected)
      "a!#$b" "a_b"   "!#$ab^&*" "ab"))
  (testing "Non-alphanumeric characters are converted to underscores (`_`)."
    (is (= (normalize "a!b@c#d$e%f^g&h*i(j)k_l+m-n=o\"p'q]r[s{t}u\\v|w/x.y,z")
           "a_b_c_d_e_f_g_h_i_j_k_l_m_n_o_p_q_r_s_t_u_v_w_x_y_z")))
  (testing (str "The function strips underscore characters (`_`) from the "
                "beginning and the end of the string.")
    (are [string expected] (= (normalize string) expected)
      "Severi (testaaja)" "severi_testaaja"   "#testi" "testi")))
