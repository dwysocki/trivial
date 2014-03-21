(ns trivial.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [trivial.client :as client]
            [trivial.server :as server])
  (:import [java.net InetAddress])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 8888
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-H" "--hostname HOST" "Remote host"
    :default (InetAddress/getByName "localhost")
    :default-desc "localhost"
    :parse-fn #(InetAddress/getByName %)]
   ["-v" nil "Verbose"
    :id :verbose
    ; I might be able to leave out :default
    ; find out once the program works
    :default false]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["A proxy server/client program using a modified TFTP."
        ""
        "Usage: trivial [options] mode"
        ""
        "Options:"
        options-summary
        ""
        "Modes:"
        "  server   Run in server mode"
        "  client   Run in client mode"]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "Runs either the server or client"
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
     (:help options) (exit 0 (usage summary))
     (not= (count arguments) 1) (exit 1 (usage summary))
     errors (exit 1 (error-msg errors)))
    (case (first arguments)
      "server" (server/start options)
      "client" (client/start options)
      (exit 1 (usage summary)))))
