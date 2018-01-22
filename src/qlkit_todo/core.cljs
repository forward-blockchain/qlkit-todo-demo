(ns qlkit-todo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [qlkit.core :as ql :refer-macros [defcomponent]]
            [goog.dom :as gdom]
            [qlkit-todo.parsers :as pa]
            [qlkit-material-ui.core :refer [enable-material-ui!]]
            [cljs-http.client :as http]
            [cljs.reader :as reader]))

(enable-console-print!)
(enable-material-ui!)

(defonce app-state (atom {}))

(set! (.-state js/window)
      (fn []
        (js/console.log @app-state)))

(defcomponent TodoItem
  (query [[:todo/text] [:db/id]])
  (render [{:keys [:todo/text] :as atts} state]
          [:li {:primary-text text
                :right-icon [:span {:on-click (fn []
                                                (ql/transact! [:todo/delete!]))}
                             [:navigation-cancel]]}]))

(defcomponent TodoList
  (query [[:qlkit-todo/todos (ql/get-query TodoItem)]])
  (render [{:keys [:qlkit-todo/todos] :as atts} {:keys [new-todo] :as state}]
          [:div {:max-width 300}
           [:input {:id          :new-todo
                    :value       (or new-todo "")
                    :placeholder "What needs to be done?"
                    :on-key-down (fn [e]
                                   (when (= (.-keyCode e) 13)
                                     (ql/transact! [:todo/new! {:db/id     (random-uuid)
                                                                :todo/text new-todo}])
                                     (ql/update-state! dissoc :new-todo)))
                    :on-change   (fn [e]
                                   (ql/update-state! assoc :new-todo (.-value (.-target e))))}]
           (when (seq todos)
             [:card [:ol (for [todo todos]
                           [TodoItem todo])]])]))

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
