(ns wedding-site.db
  (:require [clojure.java.jdbc :as sql]
            [clojure.set :as set]
            [wedding-site.utils :as utils]))

(def spec (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/wedding"))

(defn all-receptions
  "Get all of the receptions in the database."
  []
  (sql/with-db-connection [db spec]
    (sql/query db ["SELECT DISTINCT city , state , day FROM reception"])))

(defn- slug->sql-date
  "Convert a date string in the slug format to a SqlDate object."
  [date-slug]
  (-> date-slug
      utils/slug->date
      .getTime
      java.sql.Date.))

(defn reception-by-day
  "Get a single reception given the day it occurs on."
  [day]
  (let [sql-date (slug->sql-date day)]
    (sql/with-db-connection [db spec]
      (first
       (sql/query
        db
        ["SELECT DISTINCT city , state , day , info
          FROM reception WHERE day = ?"
         sql-date])))))

(defn create-reception
  "Create a new reception."
  [city state day info]
  (let [sql-date (slug->sql-date day)]
    (sql/with-db-connection [db spec]
      (sql/insert! db :reception
                   {:city city
                    :state state
                    :day sql-date
                    :info info}))))

(defn update-reception
  "Change one or more pieces of data for an existing reception."
  [original-day city state new-day info]
  (let [sql-date (slug->sql-date new-day)
        original-sql-date (slug->sql-date original-day)]
   (sql/with-db-connection [db spec]
     (sql/update! db :reception
                  {:city city
                   :state state
                   :day sql-date
                   :info info}
                  ["day = ?" original-sql-date]))))

(defn delete-reception
  "Delete an existing reception."
  [day]
  (let [sql-date (slug->sql-date day)]
    (sql/with-db-connection [db spec]
      (sql/delete! db :reception ["day = ?" sql-date]))))

(defn- now
  "Get the current time, as a java.sql.Date object."
  []
  (-> (java.util.Date.)
      .getTime
      java.sql.Timestamp.))

(defn create-rsvp
  "Create a new RSVP record."
  [{:keys [city state name email attending party-size]}]
  (sql/with-db-connection [db spec]
    (sql/insert! db :rsvp
                 {:city city
                  :state state
                  :rsvp_time (now)
                  :guest_name name
                  :guest_email email
                  :attending attending
                  :party_size party-size})))

(defn rsvp-stats
  "Returns xset with statistics about RSVPs for each reception."
  []
  (sql/with-db-connection [db spec]
    (into
     #{}
     (sql/query
      db
      "SELECT
         city
         , state
         , count(*) AS num_rsvps
         , sum(party_size) AS num_attending
       FROM
         current_rsvp
       GROUP BY
         city , state"
      {:identifiers #(clojure.string/replace % "_" "-")}))))

(defn rsvp-stats-for-reception
  "Gets map containing stats for a single reception, or nil."
  ([reception]
   (rsvp-stats-for-reception (rsvp-stats) reception))
  ([rsvp-stats reception]
   (first
    (set/select
     #(and (= (:city %) (:city reception))
           (= (:state %) (:state reception)))
     rsvp-stats))))

(defn rsvps-for-reception
  "Gets the specific RSVPs for a single reception."
  [reception]
  (sql/with-db-connection [db spec]
    (sql/query
     db
     ["SELECT
         city
         , state
         , guest_name
         , guest_email
         , attending
         , party_size
       FROM
         current_rsvp
       WHERE
         city = ?
         AND state = ?"
      (:city reception)
      (:state reception)]
     {:identifiers #(clojure.string/replace % "_" "-")})))
