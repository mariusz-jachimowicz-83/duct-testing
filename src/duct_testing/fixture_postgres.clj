(ns duct-testing.fixture-postgres
  (:require
    [clojure.java.jdbc :as jdbc]))

;; !!! Approach taken from Ruby On Rails

(defn constraint-defer-sql
  [table constraint]
  (format "ALTER TABLE %s ALTER CONSTRAINT %s DEFERRABLE"
          table
          constraint))

(defn constraint-not-defer-sql
  [table constraint]
  (format "ALTER TABLE %s ALTER CONSTRAINT %s NOT DEFERRABLE"
          table
          constraint))


(defn constraints-to-disable
  [db-spec]
  (jdbc/query db-spec
              ["SELECT table_name, constraint_name
                FROM information_schema.table_constraints
                WHERE constraint_type = 'FOREIGN KEY'
                AND is_deferrable = 'NO'"]))


(defn constraints-to-enable
  [db-spec]
  (jdbc/query db-spec
              ["SELECT table_name, constraint_name
                FROM information_schema.table_constraints
                WHERE constraint_type = 'FOREIGN KEY'
                AND is_deferrable = 'YES'"]))


(defn disable-referential-integrity!
  [db-spec]
  (doseq [{:keys [table_name constraint_name]} (constraints-to-disable db-spec)]
    (jdbc/execute! db-spec (constraint-defer-sql table_name constraint_name)))
  (jdbc/execute! db-spec "SET CONSTRAINTS ALL DEFERRED"))


(defn enable-referential-integrity!
  [db-spec]
  (doseq [{:keys [table_name constraint_name]} (constraints-to-enable db-spec)]
    (jdbc/execute! db-spec (constraint-not-defer-sql table_name constraint_name))))


(defn tables-to-clean
  [db-spec]
  (jdbc/query db-spec
              ["SELECT tablename
                FROM pg_tables
                WHERE tablename NOT LIKE ('pg_%') AND
                      tablename not like ('sql_%') AND
                      tablename != 'ragtime_migrations'"]
              {:row-fn :tablename}))


(defn truncate-tables!
  [db-spec]
  (doseq [table (tables-to-clean db-spec)]
    (jdbc/execute! db-spec (format "TRUNCATE %s CASCADE" table))))


(defn with-clear-db!
  "Wraps a function call with binding to clear db.
   Used as an fixture in tests:
   `(use-fixtures :each (fn [test-fn]
                          (with-clear-db! system-atom test-fn)))`"
  [system-atom test-fn]
  (let [db-spec (-> @system-atom :duct.database.sql/hikaricp :spec)]
    (disable-referential-integrity! db-spec)
    (truncate-tables!               db-spec)
    (enable-referential-integrity!  db-spec)
    (test-fn)))
