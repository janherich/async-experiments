(ns async-experiments.modal.core
  (:require
   [cljs.reader :as reader]
   [cljs.core.async :as async :refer [<! >! chan close! sliding-buffer put!]]
   [domina :as dom]
   [domina.events :as dom-events]
   [domina.css :as css]
   [crate.core :as crate]
   [clojure.string :refer [join blank?]]
   [async-experiments.modal.templates :refer [app-view]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]]))

(defn event-chan [input-chan selector ev-type ev-name]
  (dom-events/listen! selector ev-type (fn [e]
                                         (dom-events/prevent-default e)
                                         (put! input-chan [ev-name e]))))

(defn filter-chan [pred channel]
  (go (loop []
        (let [res (<! channel)]
          (if (pred res) res (recur))))))

(defn filter-events [name-set channel]
  (let [name-set (if (set? name-set) name-set #{name-set})]
    (filter-chan #(and (pos? (count %)) (name-set (first %)))
                 channel)))

(defn render-page [state]
  (dom/set-html! (css/sel ".container") (crate/html (app-view state))))

(defn select-user [{users :users :as state} e]
  (let [user-id (reader/read-string (dom/attr (dom-events/target e) :id))
        user (get users user-id)]
    (assoc-in state [:users user-id] (assoc user :selected (not (:selected user))))))

(let [assoc-func (fnil (fn [v user] (conj v user)) [])]
	(defn get-users-roles-matrix [{users :users}]
	  (reduce (fn [acc [id user]]
	            (update-in acc [(:roles user)] assoc-func (-> user
                                                         (assoc :id id)
                                                         (dissoc :selected)
                                                         (dissoc :roles)))) 
           {} 
           (filter (fn [[id user]] (:selected user)) users))))

(defn update-form-state [op {matrix :users-roles-matrix :as form-state} e]
  (let [[roles-key role] (reader/read-string (dom/attr (dom-events/target e) :id))
        former-users (get matrix roles-key)
        new-roles-key (op roles-key role)
        new-matrix (-> matrix
                     (update-in [new-roles-key] (fn [users] (into [] (concat former-users users))))
                     (dissoc roles-key))]
    (assoc form-state :users-roles-matrix new-matrix)))

(defn update-user-roles [{:keys [users users-roles-matrix] :as form-state}]
  (let [reverse-matrix (reduce (fn [acc [role-set users]]
                                 (merge acc (reduce (fn [acc {id :id}]
                                                      (assoc acc id role-set)) {} users))) {} users-roles-matrix)
        updated-users (reduce (fn [users [id user]]
                                (assoc users id (if (contains? reverse-matrix id)
                                                  (assoc user :roles (get reverse-matrix id))
                                                  user))) {} users)]
    (-> form-state
      (dissoc :users-roles-matrix)
      (assoc :users updated-users))))

(defn edit-roles-form [input-chan state]
  (go
   (loop [form-state (assoc state :users-roles-matrix (get-users-roles-matrix state))]
     (render-page form-state)
     (event-chan input-chan (css/sel ".add-role") :click :add-role)
     (event-chan input-chan (css/sel ".remove-role") :click :remove-role)
     (event-chan input-chan (css/sel ".submit-roles-form") :click :edit-form-submit)
     (event-chan input-chan (css/sel ".cancel-roles-form") :click :edit-form-cancel)
     (let [[ev-name ev-data] (<! (filter-events
                                   #{:add-role
                                     :remove-role
                                     :edit-form-submit 
                                     :edit-form-cancel}
                                   input-chan))]
       (condp = ev-name
         :add-role (recur ((partial update-form-state conj) form-state ev-data))
         :remove-role (recur ((partial update-form-state disj) form-state ev-data))
         :edit-form-cancel state
         :edit-form-submit (update-user-roles form-state)
         (recur form-state))))))

(defn app-loop [start-state]
  (let [input-chan (chan)]
    (go
     (loop [state start-state]
       (render-page state)
       (event-chan input-chan (css/sel ".user-check") :change :select-user)
       (event-chan input-chan (css/sel ".edit-roles") :click :edit-roles)
       (let [[ev-name ev-data] (<! input-chan)]
         (condp = ev-name
           :select-user (recur (select-user state ev-data))
           :edit-roles (recur (<! (edit-roles-form input-chan state)))          
           (recur state)))))))

(defn setup-page []
  (app-loop {:all-roles #{:expert :lead :customer}
             :users 
             {1 {:selected false :username "user-1" :roles #{:expert :lead}}
              2 {:selected false :username "user-2" :roles #{:expert}}
              3 {:selected false :username "user-3" :roles #{:expert}}
              4 {:selected false :username "user-4" :roles #{:expert}}
              5 {:selected false :username "user-5" :roles #{:expert :lead}}
              6 {:selected false :username "user-6" :roles #{:expert}}
              7 {:selected false :username "user-7" :roles #{:expert}}
              8 {:selected false :username "user-8" :roles #{:expert :customer}}
              9 {:selected false :username "user-9" :roles #{:expert :lead}}
              10 {:selected false :username "user-10" :roles #{:customer}}}}))

(setup-page)
