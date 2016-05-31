(ns wedding-site.view
  (:require [hiccup.page :as h]
            [wedding-site.db :as db]
            [wedding-site.utils :as utils]))

(defn- page
  "Template for a full HTML page."
  [title & body]
  (h/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:title title]
    (h/include-css "/r/styles/main.css")]
   [:body
    [:header
     [:h1.page-title title]]
    body]))

(defn- wedding-nav-bar
  "Navigation bar for the wedding pages."
  []
  [:nav
    [:ul.nav-bar
     [:li.nav-bar__item [:a.nav-bar__link {:href "/wedding"} "Intro"]]
     [:li.nav-bar__item [:a.nav-bar__link {:href "/wedding/story"} "Story"]]
     [:li.nav-bar__item [:a.nav-bar__link {:href "/wedding/road-trip"} "Road Trip"]]]])

(defn wedding-home []
  (page
   ;; TODO (TS 2016-05-30) Change to "We got married!" after Oct 8, 2016.
   "We're getting married!"
   (wedding-nav-bar)
   [:img.full-bleed {:src "/r/photos/card.jpg"
                     :alt "Cartoon drawing of Teresa and Tom"}]
   [:section#intro
    [:p
     "Welcome to our wedding website. "
     "You can find out more information about "
     [:a {:href "/wedding/road-trip"} "our road trip"]
     ". And, if you're curious, you can read "
     [:a {:href "/wedding/story"} "our story"]
     "."]]))

(defn wedding-story []
  (page
   "Our story"
   (wedding-nav-bar)
   [:p "Coming soon."]))

(defn road-trip []
  (let [receptions (db/all-receptions)
        receptions-by-date (sort-by :day receptions)]
    (page
     "Road trip"
     (wedding-nav-bar)
     [:section#intro
      [:p
       "We're going on a road trip right after our wedding, "
       "so we can celebrate with our family and friends. "]
      [:p
       "We'll keep this page updated with the latest information. "
       "We hope you can join us!"]]
     [:section#reception-list
      [:ol.item-list
       (for [r receptions-by-date]
         [:li.item-list__item
          [:h1.item-list__heading (:city r) ", " (:state r)]
          [:h2.item-list__subhead (utils/formatted-date (:day r))]])]])))
