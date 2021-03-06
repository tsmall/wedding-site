(ns wedding-site.routes.wedding-admin
  (:require [clojurewerkz.route-one.core :refer [defroute]]))

(defroute home "/wedding/a")
(defroute login "/wedding/a/login")

;;;; Receptions
(defroute reception-list "/wedding/a/receptions")
(defroute new-reception "/wedding/a/receptions/new")
(defroute single-reception "/wedding/a/receptions/:day")
(defroute edit-reception "/wedding/a/receptions/:previous-day")
(defroute delete-reception "/wedding/a/receptions/:day/delete")

;;;; RSVPs
(defroute rsvps "/wedding/a/rsvps/:state/:city")
