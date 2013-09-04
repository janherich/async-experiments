(ns async-experiments.modal.templates
  (:require [clojure.set :as set]))

(defn render-control-panel [{users :users}]
  [:div.navbar
   [:div.navbar-inner
    [:center
     [:h4 "Role management"]
     (let [edit-btn-attrs {:type "button" :value "edit roles"}
           some-users-selected? (> (count (filter (fn [[id user]] (:selected user)) users)) 0)]
       [:p
        [:input.btn.btn-primary.edit-roles (if some-users-selected? 
                                             edit-btn-attrs 
                                             (assoc edit-btn-attrs :disabled "disabled"))]])]]])

(defn render-user-list [{users :users}]
  [:table.table.table-hover
   [:thead
    [:tr
     [:th "Selected"]
     [:th "Username"]
     [:th "Roles"]]]
   (reduce (fn [tbody [id user]]
             (conj tbody [:tr 
                          [:td
                           [:div.user-check
                            [:input {:id id
                                     :type "checkbox" 
                                     :checked (:selected user)}]]]
                          [:td (:username user)]
                          [:td
                           (reduce (fn [list role]
                                     (conj list [:li (name role)])) [:ul.list-inline] (:roles user))]])) [:tbody] users)])

(defn render-edit-control [{matrix :users-roles-matrix all-roles :all-roles}]
  [:div.overlay
   [:div.modal-box
    [:table.table.table-hover
     [:thead
      [:tr
       [:th.users-row "Users"]
       [:th.assigned-roles-row "Assigned roles (click to remove)"]
       [:th.available-roles-row "Available roles (click to add)"]]]
     (reduce (fn [tbody [roles users]]
               (conj tbody [:tr
                            [:td
                             (reduce (fn [acc {username :username}]
                                       (conj acc (str username " "))) [:p] (if (> (count users) 4) (conj (into [] (take 4 users)) {:username "..."}) users))]
                            [:td
                             (reduce (fn [acc role]
                                       (conj acc [:input.btn.btn-success.remove-role {:type "button"
                                                                                      :value (name role)
                                                                                      :id (pr-str [roles role])}])) [:p] roles)]
                            [:td
                             (reduce (fn [acc role]
                                       (conj acc [:input.btn.btn-danger.add-role {:type "button"
                                                                                  :value (name role)
                                                                                  :id (pr-str [roles role])}])) [:p] (set/difference all-roles roles))]])) [:tbody] matrix)]
    [:p
     [:input.btn.btn-primary.submit-roles-form {:type "button" :value "save"}]
     [:input.btn.cancel-roles-form {:type "button" :value "cancel"}]]]])

(defn app-view [state]
  (let [basic-view [:div
                    (render-control-panel state)
                    (render-user-list state)]]
    (if (:users-roles-matrix state)
      (conj basic-view (render-edit-control state))
      basic-view)))

