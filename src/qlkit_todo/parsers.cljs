(ns qlkit-todo.parsers
  (:require [qlkit.core :refer [parse-children parse-children-remote parse-children-sync]]))

(defmulti read (fn [qterm & _] (first qterm)))

(defmethod read :qlkit-todo/todos
  [[dispatch-key params :as query-term] env {:keys [:todo/by-id] :as state}]
  (let [{:keys [todo-id]} params]
    (if todo-id
      [(parse-children query-term (assoc env :todo-id todo-id))]
      (sort-by :todo/text (for [id (keys by-id)]
                            (parse-children query-term (assoc env :todo-id id)))))))

(defmethod read :db/id
  [query-term {:keys [todo-id] :as env} state]
  (when (get-in state [:todo/by-id todo-id])
      todo-id))

(defmethod read :todo/text
  [query-term {:keys [todo-id] :as env} state]
  (get-in state [:todo/by-id todo-id :todo/text]))

(defmulti mutate (fn [qterm & _] (first qterm)))

(defmethod mutate :todo/new!
  [[dispatch-key params :as query-term] env state-atom]
  (let [{:keys [:db/id]} params]
    (swap! state-atom assoc-in [:todo/by-id id] params)))

(defmethod mutate :todo/delete!
  [query-term {:keys [todo-id] :as env} state-atom]
  (swap! state-atom update :todo/by-id dissoc todo-id))

(defmulti remote (fn [qterm & _] (first qterm)))

(defmethod remote :todo/new!
  [query-term state]
  query-term)

(defmethod remote :todo/delete!
  [query-term state]
  query-term)

(defmethod remote :todo/text
  [query-term state]
  query-term)

(defmethod remote :db/id
  [query-term state]
  query-term)

(defmethod remote :qlkit-todo/todos
  [query-term state]
  (parse-children-remote query-term)) 

(defmulti sync (fn [qterm & _] (first qterm)))

(defmethod sync :qlkit-todo/todos
  [[_ params :as query-term] result env state-atom]
  (for [{:keys [db/id] :as todo} result]
    (parse-children-sync query-term todo (assoc env :db/id id))))

(defmethod sync :todo/text
  [query-term result {:keys [:db/id] :as env} state-atom]
  (when id
    (swap! state-atom assoc-in [:todo/by-id id :todo/text] result)))

(defmethod sync :db/id
  [query-term result {:keys [:db/id] :as env} state-atom]
  (when id
    (swap! state-atom assoc-in [:todo/by-id id :db/id] result)))

(defmethod sync :todo/new!
  [query-term result env state-atom]
  (let [[temp-id permanent-id] result]
    (swap! state-atom
           update
           :todo/by-id
           (fn [by-id]
             (-> by-id
                 (dissoc temp-id)
                 (assoc permanent-id (assoc (by-id temp-id) :db/id permanent-id)))))))
