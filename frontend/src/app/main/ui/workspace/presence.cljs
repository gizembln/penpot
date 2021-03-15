; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020-2021 UXBOX Labs SL

(ns app.main.ui.workspace.presence
  (:require
   [rumext.alpha :as mf]
   #_[cuerdas.core :as str]
   #_[beicon.core :as rx]
   [app.main.refs :as refs]
   [app.main.store :as st]
   #_[app.util.time :as dt]
   #_[app.util.timers :as tm]
   [app.util.router :as rt]))

(mf/defc session-widget
  [{:keys [session self?] :as props}]
  (let [photo (:photo-uri session "/images/avatar.jpg")]
    [:li.tooltip.tooltip-bottom
     {:alt (:fullname session)
      :on-click (when self?
                  #(st/emit! (rt/navigate :settings/profile)))}
     [:img {:style {:border-color (:color session)}
            :src photo}]]))

(mf/defc active-sessions
  {::mf/wrap [mf/memo]}
  []
  (let [profile  (mf/deref refs/profile)
        sessions (mf/deref refs/workspace-presence)]
    [:ul.active-users
     (for [session (vals sessions)]
       [:& session-widget
        {:session session
         :self? (= (:id session) (:id profile))
         :key (:id session)}])]))
