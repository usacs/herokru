(defproject herokru "0.1.0-SNAPSHOT"
  :description "stupid heroku clone"
  :url "http://example.com/FIXME"
  :license {:name "Gnu Affero General Public License"
            :url "http://gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
  		 [clj-http "3.10.0"]
                 [cheshire "5.6.1"
                  :exclusions [com.fasterxml.jackson.core/jackson-core]]
                 [slingshot "0.12.2"]
                 [com.novemberain/monger "3.1.0"]
                 [lispyclouds/clj-docker-client "0.5.1"]])
