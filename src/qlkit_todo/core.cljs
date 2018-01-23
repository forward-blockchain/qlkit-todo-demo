(ns qlkit-todo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [qlkit.core :as ql :refer-macros [defcomponent]]
            [goog.dom :as gdom]
            [qlkit-todo.parsers :as pa]
            [cljs-http.client :as http]
            [cljs.reader :as reader]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defonce app-state (atom {}))

(defcomponent TodoItem
  (query [[:todo/text] [:db/id]])
  (render [this {:keys [:todo/text] :as atts} state]
          (html [:li 
                 text
                 [:button {:on-click (fn []
                                       (ql/transact! this [:todo/delete!]))}
                  "X"]])))

(defcomponent TodoList
  (query [[:qlkit-todo/todos (ql/get-query TodoItem)]])
  (render [this {:keys [:qlkit-todo/todos] :as atts} {:keys [new-todo] :as state}]
          (html [:div {}
                 [:input {:id          :new-todo
                          :value       (or new-todo "")
                          :placeholder "What needs to be done?"
                          :on-key-down (fn [e]
                                         (when (= (.-keyCode e) 13)
                                           (ql/transact! this [:todo/new! {:db/id     (random-uuid)
                                                                           :todo/text new-todo}])
                                           (ql/update-state! this dissoc :new-todo)))
                          :on-change   (fn [e]
                                         (ql/update-state! this assoc :new-todo (.-value (.-target e))))}]
                 (when (seq todos)
                   [:ol (for [todo todos]
                          (ql/react-element TodoItem todo))])])))

(defn remote-handler [query callback]
  (go (let [{:keys [status body] :as result} (<! (http/post "endpoint" {:edn-params query}))]
        (if (not= status 200)
          (print "server error: " body)
          (callback (reader/read-string body))))))

(ql/mount {:component      TodoList
           :dom-element    (gdom/getElement "app")
           :state          app-state
           :remote-handler remote-handler
           :parsers        {:read   pa/read
                            :mutate pa/mutate
                            :remote pa/remote
                            :sync   pa/sync}})
