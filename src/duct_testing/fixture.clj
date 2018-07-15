(ns duct-testing.fixture
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.java.io   :as io]
    [duct.core         :as duct]
    [integrant.core    :as ig]))

(def system-atom
  ^{:doc "Keeps initialized system."}
  (atom nil))

(defn read-config
  []
  (-> "/testing.edn" io/resource duct/read-config))

;; allow to have personal settings
(when (io/resource "/testing-local.edn")
  (load "/testing_local"))

(defn init-system!
  []
  (duct/load-hierarchy)
  (-> (read-config) duct/prep ig/init))

(defn- db-url-value
  [config]
  (or (get-in config [:duct.module/sql :database-url])
      (get-in config [:duct-env-dbs.module/sql :testing :database-url])))

(def db-url (-> (read-config) duct/prep db-url-value))

(defn with-system!
  "Starts whole system for testing and shutdown after test is done.
   Used as an fixture for whole test file:
   `(use-fixtures :once with-system!)`"
  [test-fn]
  (try
    (when (nil? @system-atom)
      (reset! system-atom (init-system!)))
    (test-fn)
    (catch Exception e
      (do
        (.printStackTrace e)
        (throw e)))
    (finally
      (when @system-atom
        (reset! system-atom (ig/halt! @system-atom))
        (reset! system-atom nil)))))
        