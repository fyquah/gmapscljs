(ns gmapscljs.utils
  (:require [reagent.core :as reagent])
  (:refer-clojure :exclude [flatten]))

; GENERAL PURPOSE UTILITIES
(defn flatten
  "Flattens everything but vector that starts with a function" 
  ([arr]
   (loop [v arr res []]
     (let [head (first v)]
       (if (empty? v)
         res
         (recur (next v) 
                (apply conj res 
                       (if (or (not (coll? head)) 
                               (fn? (first head))
                               (symbol? (first head)))
                         [head]
                         (flatten head)))))))))

(defn make-differ 
  "Return a differ function based on the given option keys
  ((make-differ [:a :b :c]) {:a 1 :d 2 :b 3} {:a 1 :b 4})
    => {:b 4}"
  [option-keys]
  (fn [old-state new-state]
    (reduce #(if (= (get new-state %2) (get old-state %2))
               %1
               (assoc %1 %2 (get new-state %2)))
            nil option-keys)))

(defn rand-in-range
  "Returns a random number in the given range.
  Optional random number generator third arg" 
  ([lo hi & [rand-fn]]
   (assert (>= hi lo))
   (+ lo (* (- hi lo)
            ((or rand-fn rand))))))

; GOOGLE MAP SPECIFIC UTILITIES
(defn lat-lon
  "Returns a google maps lat-lon object. argument can be either
  (lat-lon _lat_ _lon_) or
  (lat-lon {:lat 1 :lon 2}) or
  (lat-lon {:lat 1 :lng 2})
  why lat-lon over lat-lng? LATitude for lat, so LONgitude for lon?"
  ([m]
   (let [lat (:lat m)
         lon (or (:lon m) (:lng m))]
     (lat-lon lat lon)))
  ([lat lon]
  (google.maps.LatLng. lat lon)))

(defn make-handler [obj fnc]
  (fn [ & args]
    (apply fnc obj args)))

(defn random-point
  ([] (random-point rand))
  ([rand]
   (lat-lon (rand-in-range -85 85)
            (rand-in-range -170 170))))

(defn make-component-fnc 
  ([klass option-keys]
   (let [differ (make-differ option-keys)
         old-props (atom nil)]
     (fn 
       ([{map-ref :map handlers :handlers :as props}]
        (reagent/create-class 
          {:component-did-mount
           (fn [this]

             (let [obj (->> (select-keys props option-keys) 
                            (clj->js) 
                            (new klass))]
               (reagent/set-state this {:-component-obj obj})
               (doseq [[event-name handler] handlers]
                 (.addListener obj (name event-name)
                               (make-handler obj handler)))))
           :should-component-update
           (fn [this old-args new-args]

             (let [component-obj (:-component-obj (reagent/state this))
                   prop-updates (differ (second old-args) 
                                        (second new-args))
                   {:keys [lat lng]} (:center (second new-args))]
               (if (and component-obj 
                        (not (empty? prop-updates)))
                   (.setOptions component-obj
                                (clj->js prop-updates))))
             false)
           :render
           (fn 
             [this]
             ; fn is never called, updates are made at shouldComponentUpdate
             [:noscript])
           :component-will-unmount
           (fn [this]
             (.setMap (:-component-obj (reagent/state this))
                      nil))}))))))
