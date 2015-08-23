(ns gmapscljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [gmapscljs.utils :refer [make-component-fnc
                                     make-handler]
                             :as utils]))

; TODO : Have some sort of state machine to resue google map instances
; useful if user is making lots of maps (otherwise, the effect is not noticable anyway ... )
; ref: http://stackoverflow.com/questions/10485582/what-is-the-proper-way-to-destroy-a-map-instance

(defonce map-options [:zoom :center :heading :mapTypeId
                      :streetView :tilt :zoom])

(defn google-maps [{handlers :handlers :as props}]
  (reagent/create-class {:component-did-mount
                         (fn [this]
                           (let [map-ref  
                                 (google.maps.Map. 
                                   (reagent/dom-node this)
                                   (clj->js (select-keys props 
                                                         map-options)))]
                             (doseq [[event-name handler] handlers]
                               (.addListener map-ref (name event-name) 
                                             (make-handler map-ref handler)))
                             ; set the map here (YUCK), but i can't think of any
                             ; better way other than using states
                             (reagent/set-state this {:map map-ref})))
                         :render
                         (fn [this]
                           (let [map-ref (:map (reagent/state this))]
                             [:div (dissoc props :handlers)
                              (when-not (nil? map-ref)
                                (doall
                                  (for [child (utils/flatten (reagent/children this))]
                                    (if (and (coll? child) (fn? (first child)))
                                      (assoc-in child [1 :map] map-ref)))))]))}))

(def circle (make-component-fnc google.maps.Circle
                                [:center :clickable :draggable :editable
                                 :fillColor :fillOpacity :map :radius
                                 :strokeColor :strokeOpacity :strokePosition
                                 :strokeWeight :visible :zIndex]))

(def polyline (make-component-fnc google.maps.Polyline
                                  [:clickable :draggable :editable :geodesic
                                   :icons :map :path :strokeColor :strokeOpacity
                                   :strokeWeight :visible :zIndex]))


(def marker (make-component-fnc google.maps.Marker
                                [:anchorPoint :animation :attribution :clickable
                                 :crossOnDrag :cursor :draggable :icon 
                                 :label :map :opacity :optimized :place 
                                 :position :shape :title :visible :zIndex]))

