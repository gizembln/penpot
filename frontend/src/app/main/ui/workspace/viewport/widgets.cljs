; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020-2021 UXBOX Labs SL

(ns app.main.ui.workspace.viewport.widgets
  (:require
   [app.common.geom.point :as gpt]
   [app.common.geom.shapes :as gsh]
   [app.main.refs :as refs]
   [app.main.streams :as ms]
   [app.main.ui.hooks :as hooks]
   [app.main.ui.workspace.shapes.outline :refer [outline]]
   [app.main.ui.workspace.shapes.path.actions :refer [path-actions]]
   [clojure.set :as set]
   [rumext.alpha :as mf]))

(mf/defc coordinates
  [{:keys [colorpalette?]}]
  (let [coords (hooks/use-rxsub ms/mouse-position)]
    [:ul.coordinates {:class (when colorpalette? "color-palette-open")}
     [:span {:alt "x"}
      (str "X: " (:x coords "-"))]
     [:span {:alt "y"}
      (str "Y: " (:y coords "-"))]]))


(mf/defc shape-outlines
  {::mf/wrap-props false}
  [props]
  (let [objects   (unchecked-get props "objects")
        selected  (or (unchecked-get props "selected") #{})
        hover     (or (unchecked-get props "hover") #{})
        edition   (unchecked-get props "edition")
        outline?  (set/union selected hover)
        show-outline? (fn [shape] (and (not (:hidden shape))
                                       (not (:blocked shape))
                                       (not= edition (:id shape))
                                       (outline? (:id shape))))

        shapes    (cond->> (vals objects)
                    show-outline?   (filter show-outline?))

        transform (mf/deref refs/current-transform)
        color (if (or (> (count shapes) 1) (nil? (:shape-ref (first shapes))))
                "#31EFB8" "#00E0FF")]
    (when (nil? transform)
      [:g.outlines
       (for [shape shapes]
         [:& outline {:key (str "outline-" (:id shape))
                      :shape (gsh/transform-shape shape)
                      :color color}])])))


(mf/defc pixel-grid
  [{:keys [vbox zoom]}]
  [:g.pixel-grid
   [:defs
    [:pattern {:id "pixel-grid"
               :viewBox "0 0 1 1"
               :width 1
               :height 1
               :pattern-units "userSpaceOnUse"}
     [:path {:d "M 1 0 L 0 0 0 1"
             :style {:fill "none"
                     :stroke "#59B9E2"
                     :stroke-opacity "0.2"
                     :stroke-width (str (/ 1 zoom))}}]]]
   [:rect {:x (:x vbox)
           :y (:y vbox)
           :width (:width vbox)
           :height (:height vbox)
           :fill (str "url(#pixel-grid)")
           :style {:pointer-events "none"}}]])

(mf/defc viewport-actions
  {::mf/wrap [mf/memo]}
  []
  (let [edition (mf/deref refs/selected-edition)
        selected (mf/deref refs/selected-objects)
        shape (-> selected first)]
    (when (and (= (count selected) 1)
               (= (:id shape) edition)
               (= :path (:type shape)))
      [:div.viewport-actions
       [:& path-actions {:shape shape}]])))

(mf/defc cursor-tooltip
  [{:keys [zoom tooltip] :as props}]
  (let [coords (some-> (hooks/use-rxsub ms/mouse-position)
                       (gpt/divide (gpt/point zoom zoom)))
        pos-x (- (:x coords) 100)
        pos-y (+ (:y coords) 30)]
    [:g {:transform (str "translate(" pos-x "," pos-y ")")}
     [:foreignObject {:width 200 :height 100 :style {:text-align "center"}}
      [:span tooltip]]]))

(mf/defc selection-rect
  {:wrap [mf/memo]}
  [{:keys [data] :as props}]
  (when data
    [:rect.selection-rect
     {:x (:x data)
      :y (:y data)
      :width (:width data)
      :height (:height data)}]))
