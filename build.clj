;;; Task dispatching.

(defmulti task first)

(defmethod task :default
  [args]
  (let [all-tasks  (-> task methods (dissoc :default) keys sort)
        interposed (->> all-tasks (interpose ", ") (apply str))]
    (println "Unknown or missing task. Choose one of:" interposed)
    (System/exit 1)))

(require '[cljs.build.api :as api])

(def source-dir "src")

(def compiler-config {:main                 'qlkit-todo.core
                      :asset-path           "js/compiled/out"
                      :output-to            "resources/public/js/compiled/qlkit_todo.js"
                      :output-dir           "resources/public/js/compiled/out"
                      :source-map-timestamp true})

(defmethod task "compile" [args]
  (api/build source-dir compiler-config))

(require '[figwheel-sidecar.repl-api :as repl-api :refer [cljs-repl]]
         '[qlkit-todo.server :refer [handler]])

(def dev-config (merge compiler-config
                       {:optimizations :none
                        :source-map    true}))

(defmethod task "figwheel" [[_ port]]
  (repl-api/start-figwheel!
   {:figwheel-options (when port
                        {:nrepl-port       (some-> port Long/parseLong)
                         :ring-handler 'qlkit-todo.server/handler
                         :css-dirs ["resources/public/css"]
                         :nrepl-middleware ["cider.nrepl/cider-middleware"
                                            "refactor-nrepl.middleware/wrap-refactor"
                                            "cemerick.piggieback/wrap-cljs-repl"]})
    :all-builds       [{:id           "dev"
                        :figwheel     true
                        :source-paths [source-dir]
                        :compiler     dev-config}]})
  (when-not port
    (cljs-repl)))

;;; Build script entrypoint. This should be the last expression.

(task *command-line-args*)


